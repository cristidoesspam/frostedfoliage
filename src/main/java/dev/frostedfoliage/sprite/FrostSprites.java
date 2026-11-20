package dev.frostedfoliage.sprite;

import dev.frostedfoliage.FrostedFoliage;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.Nullable;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class FrostSprites {
	private static final TextureAtlasSprite[] NONE = new TextureAtlasSprite[0];

	private static final Map<TextureAtlasSprite, TextureAtlasSprite[]> CACHE = new ConcurrentHashMap<>();

	private FrostSprites() {}

	public static Identifier variantId(Identifier texture, int level) {
		return Identifier.fromNamespaceAndPath(FrostedFoliage.MOD_ID, texture.getNamespace() + "/" + texture.getPath() + "_" + level);
	}

	public static void clearCache() {
		CACHE.clear();
	}

	public static @Nullable TextureAtlasSprite variant(TextureAtlas atlas, TextureAtlasSprite base, int level) {
		TextureAtlasSprite[] variants = CACHE.get(base);
		if (variants == null) {
			variants = lookup(atlas, base);
			CACHE.put(base, variants);
		}
		return level < variants.length ? variants[level] : null;
	}

	private static TextureAtlasSprite[] lookup(TextureAtlas atlas, TextureAtlasSprite base) {
		Identifier name = base.contents().name();
		TextureAtlasSprite missing = atlas.missingSprite();
		if (atlas.getSprite(variantId(name, 1)) == missing) {
			return NONE;
		}
		TextureAtlasSprite[] variants = new TextureAtlasSprite[Whitener.LEVELS + 1];
		for (int level = 1; level <= Whitener.LEVELS; level++) {
			TextureAtlasSprite sprite = atlas.getSprite(variantId(name, level));
			variants[level] = sprite == missing ? null : sprite;
		}
		return variants;
	}
}
