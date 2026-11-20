package dev.frostedfoliage.client;

import dev.frostedfoliage.config.FrostConfig;
import dev.frostedfoliage.config.FrostSettings;
import dev.frostedfoliage.plant.Plant;
import dev.frostedfoliage.plant.PlantCatalog;
import dev.frostedfoliage.plant.PlantGroup;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.options.OptionsSubScreen;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class PlantListScreen extends OptionsSubScreen {
	private final FrostSettings s = FrostConfig.get();
	private final PlantGroup group;
	private final Map<Plant, CycleButton<Boolean>> switches = new LinkedHashMap<>();

	public PlantListScreen(Screen parent, PlantGroup group) {
		super(parent, Minecraft.getInstance().options, Component.translatable(group.translationKey()));
		this.group = group;
	}

	@Override
	protected void addOptions() {
		list.addSmall(
			Button.builder(Component.translatable("frostedfoliage.screen.enable_all"), button -> setAll(true)).width(150).build(),
			Button.builder(Component.translatable("frostedfoliage.screen.disable_all"), button -> setAll(false)).width(150).build()
		);

		List<AbstractWidget> widgets = new ArrayList<>();
		for (Plant plant : PlantCatalog.inGroup(group)) {
			CycleButton<Boolean> toggle = CycleButton.onOffBuilder(s.isEnabled(plant))
				.create(plant.displayName(), (button, value) -> s.setEnabled(plant, value));
			switches.put(plant, toggle);
			widgets.add(toggle);
		}
		list.addSmall(widgets);
	}

	private void setAll(boolean on) {
		for (Map.Entry<Plant, CycleButton<Boolean>> entry : switches.entrySet()) {
			s.setEnabled(entry.getKey(), on);
			entry.getValue().setValue(on);
		}
	}

	@Override
	public void removed() {
		FrostConfig.save();
		super.removed();
	}
}
