package dev.frostedfoliage;

import dev.frostedfoliage.config.FrostConfig;
import dev.frostedfoliage.config.FrostSettings;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.util.Mth;

public final class FrostState {
	public record Snapshot(int step, int steps) {
		static final Snapshot NONE = new Snapshot(0, 1);

		public int stamp() {
			return step | (steps << 8);
		}
	}

	private static volatile Snapshot snapshot = Snapshot.NONE;
	private static float progress;
	private static volatile float forced = -1f;

	private FrostState() {}

	public static Snapshot snapshot() {
		return snapshot;
	}

	public static float progress() {
		return progress;
	}

	public static void setForced(float value) {
		forced = value;
	}

	public static float forced() {
		return forced;
	}

	public static void reset() {
		progress = 0f;
		snapshot = Snapshot.NONE;
	}

	public static void tick(ClientLevel level) {
		FrostSettings settings = FrostConfig.get();

		float previous = progress;
		if (!settings.enabled) {
			progress = 0f;
		} else if (forced >= 0f) {
			progress = forced;
		} else {
			float rain = level.canHaveWeather() ? level.getRainLevel(1.0F) : 0.0F;
			if (rain > 0.05F) {
				progress += rain / (20f * settings.whitenSeconds);
			} else if (settings.meltSeconds > 0) {
				progress -= 1f / (20f * settings.meltSeconds);
			}
		}
		progress = Mth.clamp(progress, 0f, 1f);

		int step = Math.round(progress * settings.steps);
		Snapshot old = snapshot;
		if (step != old.step() || settings.steps != old.steps()) {
			snapshot = new Snapshot(step, settings.steps);
		}
	}

	public static float whiteness(Snapshot snap, int x, int y, int z, boolean withHeight, FrostSettings settings) {
		if (snap.step() <= 0) {
			return 0f;
		}
		float p = snap.step() / (float) snap.steps();
		float spread = settings.variation / 100f;
		float offset = jitter(x, withHeight ? y : 0, z);
		return Mth.clamp(p * (1f + spread) - spread * offset, 0f, 1f);
	}

	private static float jitter(int x, int y, int z) {
		long n = x * 0x9E3779B97F4A7C15L ^ y * 0xC2B2AE3D27D4EB4FL ^ z * 0x165667B19E3779F9L;
		n ^= n >>> 33;
		n *= 0xFF51AFD7ED558CCDL;
		n ^= n >>> 33;
		return (n >>> 40) / (float) (1 << 24);
	}
}
