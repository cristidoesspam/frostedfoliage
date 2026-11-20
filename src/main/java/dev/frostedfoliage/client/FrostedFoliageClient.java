package dev.frostedfoliage.client;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import dev.frostedfoliage.FrostState;
import dev.frostedfoliage.FrostedFoliage;
import dev.frostedfoliage.SectionTracker;
import dev.frostedfoliage.config.FrostConfig;
import dev.frostedfoliage.config.FrostSettings;
import dev.frostedfoliage.model.FrostedBlockStateModel;
import dev.frostedfoliage.plant.Plant;
import dev.frostedfoliage.plant.PlantCatalog;
import dev.frostedfoliage.sprite.FrostSprites;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.ClientCommands;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientChunkEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin;
import net.fabricmc.fabric.api.client.model.loading.v1.ModelModifier;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.resource.v1.ResourceLoader;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;

public class FrostedFoliageClient implements ClientModInitializer {
	private static final KeyMapping.Category CATEGORY =
		KeyMapping.Category.register(Identifier.fromNamespaceAndPath(FrostedFoliage.MOD_ID, FrostedFoliage.MOD_ID));

	private static KeyMapping openSettingsKey;
	private static KeyMapping toggleKey;

	private static int builtHash;
	private static boolean rebuildPending;

	@Override
	public void onInitializeClient() {
		FrostConfig.load();
		builtHash = FrostConfig.get().geometryHash();

		ModelLoadingPlugin.register(context -> context.modifyBlockModelAfterBake().register(ModelModifier.WRAP_PHASE, (model, modelContext) -> {
			Plant plant = PlantCatalog.forState(modelContext.state());
			return plant == null ? model : new FrostedBlockStateModel(model, plant);
		}));

		ResourceLoader.get(PackType.CLIENT_RESOURCES).registerReloadListener(
			Identifier.fromNamespaceAndPath(FrostedFoliage.MOD_ID, "sprite_cache"),
			(ResourceManagerReloadListener) manager -> FrostSprites.clearCache());

		ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> reset());
		ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> reset());
		ClientChunkEvents.CHUNK_UNLOAD.register((level, chunk) -> SectionTracker.forgetChunk(level, chunk.getPos()));

		ClientTickEvents.END_LEVEL_TICK.register(level -> {
			if (rebuildPending) {
				rebuildPending = false;
				SectionTracker.clear();
				Minecraft.getInstance().levelExtractor.allChanged();
			}
			FrostState.tick(level);
			SectionTracker.refresh(level);
		});

		openSettingsKey = KeyMappingHelper.registerKeyMapping(
			new KeyMapping("key.frostedfoliage.settings", InputConstants.UNKNOWN.getValue(), CATEGORY));
		toggleKey = KeyMappingHelper.registerKeyMapping(
			new KeyMapping("key.frostedfoliage.toggle", InputConstants.UNKNOWN.getValue(), CATEGORY));

		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			while (toggleKey.consumeClick()) {
				FrostSettings settings = FrostConfig.get();
				settings.enabled = !settings.enabled;
				FrostConfig.save();
				settingsChanged();
				if (client.player != null) {
					client.player.sendOverlayMessage(Component.translatable(
						settings.enabled ? "frostedfoliage.toggle.on" : "frostedfoliage.toggle.off"));
				}
			}
			while (openSettingsKey.consumeClick()) {
				if (client.player != null) {
					client.gui.setScreen(new FrostedFoliageScreen(client.gui.screen()));
				}
			}
		});

		ClientCommandRegistrationCallback.EVENT.register((dispatcher, context) -> dispatcher.register(
			ClientCommands.literal("frostedfoliage")
				.then(ClientCommands.literal("preview")
					.then(ClientCommands.argument("percent", IntegerArgumentType.integer(0, 100)).executes(command -> {
						FrostState.setForced(IntegerArgumentType.getInteger(command, "percent") / 100f);
						command.getSource().sendFeedback(Component.translatable("frostedfoliage.command.preview",
							IntegerArgumentType.getInteger(command, "percent")));
						return 1;
					}))
					.then(ClientCommands.literal("off").executes(command -> {
						FrostState.setForced(-1f);
						command.getSource().sendFeedback(Component.translatable("frostedfoliage.command.preview_off"));
						return 1;
					})))));
	}

	private static void reset() {
		FrostState.reset();
		SectionTracker.clear();
	}

	public static void settingsChanged() {
		int hash = FrostConfig.get().geometryHash();
		if (hash != builtHash) {
			builtHash = hash;
			rebuildPending = true;
		}
	}
}
