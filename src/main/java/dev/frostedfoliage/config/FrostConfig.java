package dev.frostedfoliage.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import dev.frostedfoliage.FrostedFoliage;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;

public final class FrostConfig {
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
	private static volatile FrostSettings current = new FrostSettings();

	private FrostConfig() {}

	public static FrostSettings get() {
		return current;
	}

	private static Path file() {
		return FabricLoader.getInstance().getConfigDir().resolve("frostedfoliage.json");
	}

	public static void load() {
		Path file = file();
		if (Files.exists(file)) {
			try (Reader reader = Files.newBufferedReader(file)) {
				FrostSettings loaded = GSON.fromJson(reader, FrostSettings.class);
				if (loaded != null) {
					current = loaded.sanitize();
				}
			} catch (Exception e) {
				FrostedFoliage.LOGGER.warn("Could not read {}, using defaults", file, e);
			}
		}
		save();
	}

	public static void save() {
		Path file = file();
		try {
			Files.createDirectories(file.getParent());
			try (Writer writer = Files.newBufferedWriter(file)) {
				GSON.toJson(current, writer);
			}
		} catch (IOException e) {
			FrostedFoliage.LOGGER.warn("Could not write {}", file, e);
		}
	}
}
