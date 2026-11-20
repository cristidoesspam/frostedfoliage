package dev.frostedfoliage.client;

import dev.frostedfoliage.config.FrostConfig;
import dev.frostedfoliage.config.FrostSettings;
import dev.frostedfoliage.plant.PlantGroup;
import net.minecraft.client.Minecraft;
import net.minecraft.client.OptionInstance;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.options.OptionsSubScreen;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class FrostedFoliageScreen extends OptionsSubScreen {
	private static final String KEY = "frostedfoliage.option.";

	private final FrostSettings s = FrostConfig.get();

	public FrostedFoliageScreen(Screen parent) {
		super(parent, Minecraft.getInstance().options, Component.translatable("frostedfoliage.screen.title"));
	}

	@Override
	protected void addOptions() {
		list.addBig(toggle("enabled", s.enabled, v -> s.enabled = v));

		list.addSmall(
			slider("whiten_seconds", FrostSettings.MIN_WHITEN_SECONDS, FrostSettings.MAX_WHITEN_SECONDS, s.whitenSeconds,
				v -> Component.translatable(KEY + "seconds.value", Component.translatable(KEY + "whiten_seconds"), v),
				v -> s.whitenSeconds = v),
			slider("melt_seconds", FrostSettings.MIN_MELT_SECONDS, FrostSettings.MAX_MELT_SECONDS, s.meltSeconds,
				v -> v == 0
					? Component.translatable(KEY + "melt_seconds.never", Component.translatable(KEY + "melt_seconds"))
					: Component.translatable(KEY + "seconds.value", Component.translatable(KEY + "melt_seconds"), v),
				v -> s.meltSeconds = v)
		);
		list.addSmall(
			slider("variation", 0, 100, s.variation,
				v -> Component.translatable(KEY + "percent.value", Component.translatable(KEY + "variation"), v),
				v -> s.variation = v),
			toggle("whiten_sides", s.whitenSides, v -> s.whitenSides = v)
		);
		list.addBig(slider("cover_layers", 0, FrostSettings.MAX_COVER_LAYERS, s.coverLayers,
			v -> Component.translatable(KEY + "number.value", Component.translatable(KEY + "cover_layers"), v),
			v -> s.coverLayers = v));
		list.addBig(slider("steps", FrostSettings.MIN_STEPS, FrostSettings.MAX_STEPS, s.steps,
			v -> Component.translatable(KEY + "number.value", Component.translatable(KEY + "steps"), v),
			v -> s.steps = v));

		list.addHeader(Component.translatable("frostedfoliage.screen.plants"));
		List<AbstractWidget> groups = new ArrayList<>();
		for (PlantGroup group : PlantGroup.values()) {
			groups.add(Button.builder(Component.translatable(group.translationKey()),
				button -> minecraft.gui.setScreen(new PlantListScreen(this, group))).width(150).build());
		}
		list.addSmall(groups);
	}

	@Override
	public void removed() {
		FrostConfig.save();
		super.removed();
	}

	@Override
	public void onClose() {
		super.onClose();
		FrostConfig.save();
		FrostedFoliageClient.settingsChanged();
	}

	private static OptionInstance.TooltipSupplier<Boolean> boolTooltip(String name) {
		return tooltip(name);
	}

	private static <T> OptionInstance.TooltipSupplier<T> tooltip(String name) {
		Tooltip tip = Tooltip.create(Component.translatable(KEY + name + ".tooltip"));
		return value -> tip;
	}

	private static OptionInstance<Boolean> toggle(String name, boolean value, Consumer<Boolean> setter) {
		return OptionInstance.createBoolean(KEY + name, boolTooltip(name), value, setter::accept);
	}

	private static OptionInstance<Integer> slider(String name, int min, int max, int value,
			java.util.function.IntFunction<Component> label, java.util.function.IntConsumer setter) {
		return new OptionInstance<>(KEY + name, tooltip(name), (caption, v) -> label.apply(v),
			new OptionInstance.IntRange(min, max), value, setter::accept);
	}
}
