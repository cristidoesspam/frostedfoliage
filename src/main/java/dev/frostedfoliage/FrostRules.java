package dev.frostedfoliage;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.Heightmap;

public final class FrostRules {
	private FrostRules() {}

	public static boolean snowsAt(ClientLevel level, BlockPos pos) {
		Biome biome = level.getBiome(pos).value();
		return biome.getPrecipitationAt(pos, level.getSeaLevel()) == Biome.Precipitation.SNOW;
	}

	public static boolean exposed(ClientLevel level, BlockPos pos, int coverLayers, boolean sides) {
		int x = pos.getX();
		int y = pos.getY();
		int z = pos.getZ();
		if (y >= firstFreeAbove(level, x, z) - 1 - coverLayers) {
			return true;
		}
		if (!sides) {
			return false;
		}
		return y >= firstFreeAbove(level, x + 1, z) - coverLayers
			|| y >= firstFreeAbove(level, x - 1, z) - coverLayers
			|| y >= firstFreeAbove(level, x, z + 1) - coverLayers
			|| y >= firstFreeAbove(level, x, z - 1) - coverLayers;
	}

	private static int firstFreeAbove(ClientLevel level, int x, int z) {
		return level.getHeight(Heightmap.Types.MOTION_BLOCKING, x, z);
	}
}
