package dev.frostedfoliage.plant;

public enum PlantGroup {
	LEAVES,
	GROUND,
	FLOWERS,
	CROPS,
	SAPLINGS,
	OTHER;

	public String translationKey() {
		return "frostedfoliage.group." + name().toLowerCase(java.util.Locale.ROOT);
	}
}
