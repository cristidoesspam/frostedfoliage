package dev.frostedfoliage.config;

import dev.frostedfoliage.plant.Plant;
import net.minecraft.util.Mth;

import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;

public final class FrostSettings {
	public static final int MIN_WHITEN_SECONDS = 5;
	public static final int MAX_WHITEN_SECONDS = 600;
	public static final int MIN_MELT_SECONDS = 0;
	public static final int MAX_MELT_SECONDS = 1800;
	public static final int MIN_STEPS = 4;
	public static final int MAX_STEPS = 40;
	public static final int MAX_COVER_LAYERS = 6;

	public boolean enabled = true;
	public int whitenSeconds = 90;
	public int meltSeconds = 180;
	public int variation = 35;
	public boolean whitenSides = true;
	public int coverLayers = 1;
	public int steps = 16;
	public Set<String> disabled = new TreeSet<>();

	public boolean isEnabled(Plant plant) {
		return !disabled.contains(plant.key());
	}

	public void setEnabled(Plant plant, boolean on) {
		if (on) {
			disabled.remove(plant.key());
		} else {
			disabled.add(plant.key());
		}
	}

	public FrostSettings sanitize() {
		whitenSeconds = Mth.clamp(whitenSeconds, MIN_WHITEN_SECONDS, MAX_WHITEN_SECONDS);
		meltSeconds = Mth.clamp(meltSeconds, MIN_MELT_SECONDS, MAX_MELT_SECONDS);
		variation = Mth.clamp(variation, 0, 100);
		coverLayers = Mth.clamp(coverLayers, 0, MAX_COVER_LAYERS);
		steps = Mth.clamp(steps, MIN_STEPS, MAX_STEPS);
		if (disabled == null) {
			disabled = new TreeSet<>();
		}
		return this;
	}

	public int geometryHash() {
		return Objects.hash(enabled, variation, whitenSides, coverLayers, steps, new TreeSet<>(disabled));
	}
}
