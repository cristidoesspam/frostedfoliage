package dev.frostedfoliage;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ChunkPos;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class SectionTracker {
	private static final int BUDGET_PER_SCAN = 24;
	private static final int MAX_BUDGET_PER_SCAN = 96;
	private static final int SCAN_INTERVAL = 3;

	private static final Map<Long, Integer> SECTIONS = new ConcurrentHashMap<>();
	private static int ticks;

	private SectionTracker() {}

	public static void note(BlockPos pos, int stamp) {
		SECTIONS.put(SectionPos.asLong(pos), stamp);
	}

	public static void clear() {
		SECTIONS.clear();
	}

	public static void forgetChunk(ClientLevel level, ChunkPos chunk) {
		for (int y = level.getMinSectionY(); y <= level.getMaxSectionY(); y++) {
			SECTIONS.remove(SectionPos.asLong(chunk.x(), y, chunk.z()));
		}
	}

	public static int size() {
		return SECTIONS.size();
	}

	public static void refresh(ClientLevel level) {
		if (++ticks % SCAN_INTERVAL != 0 || SECTIONS.isEmpty()) {
			return;
		}
		int stamp = FrostState.snapshot().stamp();

		List<Long> stale = new ArrayList<>();
		for (Map.Entry<Long, Integer> entry : SECTIONS.entrySet()) {
			if (entry.getValue() != stamp) {
				stale.add(entry.getKey());
			}
		}
		if (stale.isEmpty()) {
			return;
		}

		Entity camera = Minecraft.getInstance().getCameraEntity();
		int cx = camera == null ? 0 : SectionPos.blockToSectionCoord(camera.getBlockX());
		int cy = camera == null ? 0 : SectionPos.blockToSectionCoord(camera.getBlockY());
		int cz = camera == null ? 0 : SectionPos.blockToSectionCoord(camera.getBlockZ());
		stale.sort((a, b) -> Long.compare(distance(a, cx, cy, cz), distance(b, cx, cy, cz)));

		int budget = Mth.clamp(stale.size() / 8, BUDGET_PER_SCAN, MAX_BUDGET_PER_SCAN);
		int count = Math.min(budget, stale.size());
		for (int i = 0; i < count; i++) {
			long key = stale.get(i);
			SECTIONS.remove(key);
			int x = SectionPos.x(key);
			int y = SectionPos.y(key);
			int z = SectionPos.z(key);
			level.setSectionRangeDirty(x, y, z, x, y, z);
		}
	}

	private static long distance(long key, int cx, int cy, int cz) {
		long dx = SectionPos.x(key) - cx;
		long dy = SectionPos.y(key) - cy;
		long dz = SectionPos.z(key) - cz;
		return dx * dx + dy * dy + dz * dz;
	}
}
