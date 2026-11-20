package dev.frostedfoliage.sprite;

import com.mojang.blaze3d.platform.NativeImage;

public final class Whitener {
	public static final int LEVELS = 8;

	private static final float MAX_BLEND = 0.78f;

	private static final int SNOW_RED = 244;
	private static final int SNOW_GREEN = 248;
	private static final int SNOW_BLUE = 255;

	private Whitener() {}

	public static NativeImage whiten(NativeImage source, int level) {
		float blend = MAX_BLEND * level / LEVELS;
		return source.mappedCopy(argb -> {
			int alpha = argb >>> 24;
			int red = argb >> 16 & 0xFF;
			int green = argb >> 8 & 0xFF;
			int blue = argb & 0xFF;
			red = Math.round(red + (SNOW_RED - red) * blend);
			green = Math.round(green + (SNOW_GREEN - green) * blend);
			blue = Math.round(blue + (SNOW_BLUE - blue) * blend);
			return alpha << 24 | red << 16 | green << 8 | blue;
		});
	}
}
