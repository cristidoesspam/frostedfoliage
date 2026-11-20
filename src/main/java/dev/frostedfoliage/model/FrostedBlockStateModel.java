package dev.frostedfoliage.model;

import dev.frostedfoliage.FrostRules;
import dev.frostedfoliage.FrostState;
import dev.frostedfoliage.SectionTracker;
import dev.frostedfoliage.config.FrostConfig;
import dev.frostedfoliage.config.FrostSettings;
import dev.frostedfoliage.plant.Plant;
import dev.frostedfoliage.plant.PlantGroup;
import dev.frostedfoliage.sprite.FrostSprites;
import dev.frostedfoliage.sprite.Whitener;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import net.fabricmc.fabric.api.client.model.loading.v1.wrapper.WrapperBlockStateModel;
import net.fabricmc.fabric.api.client.renderer.v1.mesh.MutableQuadView;
import net.fabricmc.fabric.api.client.renderer.v1.mesh.QuadEmitter;
import net.fabricmc.fabric.api.client.renderer.v1.mesh.QuadTransform;
import net.fabricmc.fabric.api.client.renderer.v1.sprite.SpriteFinder;
import net.fabricmc.fabric.api.client.rendering.v1.BlockColorRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.BlockTintsFactory;
import net.minecraft.client.Minecraft;
import net.minecraft.client.color.block.BlockColors;
import net.minecraft.client.color.block.BlockTintSource;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.data.AtlasIds;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.function.Predicate;

public final class FrostedBlockStateModel extends WrapperBlockStateModel {
	private static final ThreadLocal<Applier> APPLIERS = ThreadLocal.withInitial(Applier::new);

	private final Plant plant;

	public FrostedBlockStateModel(BlockStateModel wrapped, Plant plant) {
		super(wrapped);
		this.plant = plant;
	}

	@Override
	public void emitQuads(QuadEmitter emitter, BlockAndTintGetter level, BlockPos pos, BlockState state, RandomSource random, Predicate<@Nullable Direction> cullTest) {
		Applier applier = APPLIERS.get();
		if (!applier.prepare(plant, level, pos, state)) {
			wrapped.emitQuads(emitter, level, pos, state, random, cullTest);
			return;
		}
		emitter.pushTransform(applier);
		try {
			wrapped.emitQuads(emitter, level, pos, state, random, cullTest);
		} finally {
			emitter.popTransform();
			applier.release();
		}
	}

	@Override
	public @Nullable Object createGeometryKey(BlockAndTintGetter level, BlockPos pos, BlockState state, RandomSource random) {
		return null;
	}

	private static final class Applier implements QuadTransform {
		private final IntArrayList tints = new IntArrayList();

		private Plant plant;
		private BlockAndTintGetter level;
		private BlockPos pos;
		private BlockState state;
		private float whiteness;
		private int spriteLevel;
		private boolean tintsReady;
		private TextureAtlas atlas;
		private SpriteFinder finder;

		boolean prepare(Plant plant, BlockAndTintGetter level, BlockPos pos, BlockState state) {
			FrostSettings settings = FrostConfig.get();
			if (!settings.enabled || !settings.isEnabled(plant) || level == BlockAndTintGetter.EMPTY) {
				return false;
			}
			Minecraft minecraft = Minecraft.getInstance();
			ClientLevel world = minecraft.level;
			if (world == null || !FrostRules.snowsAt(world, pos) || !FrostRules.exposed(world, pos, settings.coverLayers, settings.whitenSides)) {
				return false;
			}

			FrostState.Snapshot stage = FrostState.snapshot();
			SectionTracker.note(pos, stage.stamp());

			float white = FrostState.whiteness(stage, pos.getX(), pos.getY(), pos.getZ(), plant.group() == PlantGroup.LEAVES, settings);
			if (white < 0.01f) {
				return false;
			}

			this.plant = plant;
			this.level = level;
			this.pos = pos;
			this.state = state;
			this.whiteness = white;
			this.spriteLevel = Math.round(white * Whitener.LEVELS);
			this.tintsReady = false;
			this.atlas = minecraft.getAtlasManager().getAtlasOrThrow(AtlasIds.BLOCKS);
			this.finder = atlas.spriteFinder();
			return true;
		}

		void release() {
			level = null;
			pos = null;
			state = null;
			atlas = null;
			finder = null;
			tints.clear();
		}

		@Override
		public boolean transform(MutableQuadView quad) {
			int tintIndex = quad.tintIndex();
			if (plant.onlyTinted() && tintIndex < 0) {
				return true;
			}
			if (spriteLevel > 0) {
				swapSprite(quad);
			}
			if (tintIndex >= 0) {
				quad.tintIndex(-1);
				quad.multiplyColor(towardsWhite(tintColor(tintIndex), whiteness));
			}
			return true;
		}

		private static int towardsWhite(int color, float amount) {
			int red = color >> 16 & 0xFF;
			int green = color >> 8 & 0xFF;
			int blue = color & 0xFF;
			red += Math.round((255 - red) * amount);
			green += Math.round((255 - green) * amount);
			blue += Math.round((255 - blue) * amount);
			return 0xFF000000 | red << 16 | green << 8 | blue;
		}

		private void swapSprite(MutableQuadView quad) {
			TextureAtlasSprite from;
			TextureAtlasSprite to;
			try {
				from = finder.find(quad);
				to = FrostSprites.variant(atlas, from, spriteLevel);
			} catch (RuntimeException e) {
				return;
			}
			if (to == null) {
				return;
			}
			float fromWidth = from.getU1() - from.getU0();
			float fromHeight = from.getV1() - from.getV0();
			float toWidth = to.getU1() - to.getU0();
			float toHeight = to.getV1() - to.getV0();
			for (int i = 0; i < 4; i++) {
				float u = (quad.u(i) - from.getU0()) / fromWidth;
				float v = (quad.v(i) - from.getV0()) / fromHeight;
				quad.uv(i, to.getU0() + u * toWidth, to.getV0() + v * toHeight);
			}
		}

		private int tintColor(int index) {
			if (!tintsReady) {
				BlockColors colors = Minecraft.getInstance().getBlockColors();
				List<BlockTintSource> sources = colors.getTintSources(state);
				if (!sources.isEmpty()) {
					for (BlockTintSource source : sources) {
						tints.add(source.colorInWorld(state, level, pos));
					}
				} else {
					BlockTintsFactory factory = BlockColorRegistry.getFactory(state);
					if (factory != null) {
						factory.collect(state, level, pos, tints);
					}
				}
				tintsReady = true;
			}
			return index < tints.size() ? tints.getInt(index) : -1;
		}
	}
}
