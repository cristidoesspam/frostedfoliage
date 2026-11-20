package dev.frostedfoliage.sprite;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.serialization.MapCodec;
import dev.frostedfoliage.FrostedFoliage;
import dev.frostedfoliage.plant.PlantCatalog;
import net.minecraft.client.renderer.texture.SpriteContents;
import net.minecraft.client.renderer.texture.atlas.SpriteResourceLoader;
import net.minecraft.client.renderer.texture.atlas.SpriteSource;
import net.minecraft.client.resources.metadata.animation.AnimationMetadataSection;
import net.minecraft.client.resources.metadata.animation.FrameSize;
import net.minecraft.client.resources.metadata.texture.TextureMetadataSection;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceMetadata;

import java.io.InputStream;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public record FrostSpriteSource() implements SpriteSource {
	public static final FrostSpriteSource INSTANCE = new FrostSpriteSource();
	public static final MapCodec<FrostSpriteSource> CODEC = MapCodec.unit(INSTANCE);

	@Override
	public void run(ResourceManager resourceManager, Output output) {
		Map<Identifier, Resource> textures = new LinkedHashMap<>();
		for (Identifier texture : PlantCatalog.allTextures()) {
			resourceManager.getResource(TEXTURE_ID_CONVERTER.idToFile(texture)).ifPresent(r -> textures.put(texture, r));
		}
		Map<Identifier, Resource> found = resourceManager.listResources("textures/block", id ->
			!id.getNamespace().equals(Identifier.DEFAULT_NAMESPACE) && id.getPath().endsWith(".png") && id.getPath().contains("leaves"));
		for (Map.Entry<Identifier, Resource> entry : found.entrySet()) {
			textures.putIfAbsent(TEXTURE_ID_CONVERTER.fileToId(entry.getKey()), entry.getValue());
		}

		for (Map.Entry<Identifier, Resource> entry : textures.entrySet()) {
			for (int level = 1; level <= Whitener.LEVELS; level++) {
				Identifier id = FrostSprites.variantId(entry.getKey(), level);
				output.add(id, new Variant(id, entry.getValue(), level));
			}
		}
	}

	@Override
	public MapCodec<? extends SpriteSource> codec() {
		return CODEC;
	}

	private record Variant(Identifier id, Resource resource, int level) implements DiscardableLoader {
		@Override
		public SpriteContents get(SpriteResourceLoader loader) {
			try {
				ResourceMetadata metadata = resource.metadata();
				Optional<AnimationMetadataSection> animation = metadata.getSection(AnimationMetadataSection.TYPE);
				Optional<TextureMetadataSection> texture = metadata.getSection(TextureMetadataSection.TYPE);

				NativeImage original;
				try (InputStream stream = resource.open()) {
					original = NativeImage.read(stream);
				}
				NativeImage white = Whitener.whiten(original, level);
				int width = original.getWidth();
				int height = original.getHeight();
				original.close();

				FrameSize frame = animation.isPresent() ? animation.get().calculateFrameSize(width, height) : new FrameSize(width, height);
				return new SpriteContents(id, frame, white, animation, List.of(), texture);
			} catch (Exception e) {
				FrostedFoliage.LOGGER.warn("Could not whiten {}", id, e);
				return null;
			}
		}
	}
}
