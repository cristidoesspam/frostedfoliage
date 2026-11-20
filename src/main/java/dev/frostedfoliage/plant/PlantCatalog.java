package dev.frostedfoliage.plant;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public final class PlantCatalog {
	public static final Plant MODDED_LEAVES = new Plant("modded_leaves", PlantGroup.LEAVES, List.of(), List.of(), false);

	private static final Map<String, Plant> BY_KEY = new LinkedHashMap<>();
	private static @Nullable Map<Block, Plant> byBlock;

	static {
		for (String tree : new String[]{"oak", "spruce", "birch", "jungle", "acacia", "dark_oak", "mangrove", "cherry",
			"pale_oak", "azalea", "flowering_azalea", "orange_poplar", "red_poplar", "yellow_poplar"}) {
			add(PlantGroup.LEAVES, tree + "_leaves", tree + "_leaves");
		}
		BY_KEY.put(MODDED_LEAVES.key(), MODDED_LEAVES);

		addTintedOnly(PlantGroup.GROUND, "grass_block", "grass_block", "grass_block_top", "grass_block_side_overlay");
		add(PlantGroup.GROUND, "short_grass", "short_grass");
		add(PlantGroup.GROUND, "tall_grass", "tall_grass", "tall_grass_bottom", "tall_grass_top");
		add(PlantGroup.GROUND, "fern", "fern");
		add(PlantGroup.GROUND, "large_fern", "large_fern", "large_fern_bottom", "large_fern_top");
		add(PlantGroup.GROUND, "bush", "bush");
		add(PlantGroup.GROUND, "short_dry_grass", "short_dry_grass");
		add(PlantGroup.GROUND, "tall_dry_grass", "tall_dry_grass");
		add(PlantGroup.GROUND, "dead_bush", "dead_bush");
		add(PlantGroup.GROUND, "red_shrub", "red_shrub");
		add(PlantGroup.GROUND, "leaf_litter", "leaf_litter");
		add(PlantGroup.GROUND, "pink_petals", "pink_petals", "pink_petals", "pink_petals_stem");
		add(PlantGroup.GROUND, "wildflowers", "wildflowers", "wildflowers", "wildflowers_stem");
		add(PlantGroup.GROUND, "vine", "vine");
		add(PlantGroup.GROUND, "moss_block", "moss_block,moss_carpet", "moss_block");
		add(PlantGroup.GROUND, "pale_moss_block", "pale_moss_block,pale_moss_carpet,pale_hanging_moss",
			"pale_moss_block", "pale_moss_carpet", "pale_moss_carpet_side_small", "pale_moss_carpet_side_tall",
			"pale_hanging_moss", "pale_hanging_moss_tip");

		for (String flower : new String[]{"dandelion", "golden_dandelion", "poppy", "blue_orchid", "allium", "azure_bluet",
			"red_tulip", "orange_tulip", "white_tulip", "pink_tulip", "oxeye_daisy", "cornflower", "lily_of_the_valley",
			"wither_rose", "torchflower", "closed_eyeblossom", "open_eyeblossom"}) {
			add(PlantGroup.FLOWERS, flower, flower);
		}
		add(PlantGroup.FLOWERS, "sunflower", "sunflower", "sunflower_back", "sunflower_bottom", "sunflower_front", "sunflower_top");
		add(PlantGroup.FLOWERS, "lilac", "lilac", "lilac_bottom", "lilac_top");
		add(PlantGroup.FLOWERS, "rose_bush", "rose_bush", "rose_bush_bottom", "rose_bush_top");
		add(PlantGroup.FLOWERS, "peony", "peony", "peony_bottom", "peony_top");
		add(PlantGroup.FLOWERS, "pitcher_plant", "pitcher_plant", "pitcher_crop_bottom_stage_4", "pitcher_crop_top_stage_4");

		add(PlantGroup.CROPS, "wheat", "wheat", stages("wheat_stage", 8));
		add(PlantGroup.CROPS, "carrots", "carrots", stages("carrots_stage", 4));
		add(PlantGroup.CROPS, "potatoes", "potatoes", stages("potatoes_stage", 4));
		add(PlantGroup.CROPS, "beetroots", "beetroots", stages("beetroots_stage", 4));
		add(PlantGroup.CROPS, "melon_stem", "melon_stem,attached_melon_stem", "melon_stem", "attached_melon_stem");
		add(PlantGroup.CROPS, "pumpkin_stem", "pumpkin_stem,attached_pumpkin_stem", "pumpkin_stem", "attached_pumpkin_stem");
		add(PlantGroup.CROPS, "torchflower_crop", "torchflower_crop", "torchflower_crop_stage0", "torchflower_crop_stage1");
		add(PlantGroup.CROPS, "pitcher_crop", "pitcher_crop", "pitcher_crop_bottom", "pitcher_crop_bottom_stage_1",
			"pitcher_crop_bottom_stage_2", "pitcher_crop_bottom_stage_3", "pitcher_crop_bottom_stage_4", "pitcher_crop_side",
			"pitcher_crop_top", "pitcher_crop_top_stage_3", "pitcher_crop_top_stage_4");

		for (String tree : new String[]{"oak", "spruce", "birch", "jungle", "acacia", "dark_oak", "cherry", "pale_oak", "poplar"}) {
			add(PlantGroup.SAPLINGS, tree + "_sapling", tree + "_sapling");
		}
		add(PlantGroup.SAPLINGS, "mangrove_propagule", "mangrove_propagule", "mangrove_propagule", "mangrove_propagule_hanging");
		add(PlantGroup.SAPLINGS, "bamboo_sapling", "bamboo_sapling", "bamboo_stage0");

		add(PlantGroup.OTHER, "sugar_cane", "sugar_cane");
		add(PlantGroup.OTHER, "bamboo", "bamboo", "bamboo_stalk", "bamboo_large_leaves", "bamboo_small_leaves");
		add(PlantGroup.OTHER, "sweet_berry_bush", "sweet_berry_bush", stages("sweet_berry_bush_stage", 4));
		add(PlantGroup.OTHER, "brown_mushroom", "brown_mushroom");
		add(PlantGroup.OTHER, "red_mushroom", "red_mushroom");
		add(PlantGroup.OTHER, "azalea", "azalea", "azalea_plant", "azalea_side", "azalea_top");
		add(PlantGroup.OTHER, "big_dripleaf", "big_dripleaf", "big_dripleaf_side", "big_dripleaf_stem", "big_dripleaf_tip", "big_dripleaf_top");
		add(PlantGroup.OTHER, "small_dripleaf", "small_dripleaf", "small_dripleaf_side", "small_dripleaf_stem_bottom",
			"small_dripleaf_stem_top", "small_dripleaf_top");
	}

	private PlantCatalog() {}

	private static String[] stages(String prefix, int count) {
		String[] names = new String[count];
		for (int i = 0; i < count; i++) {
			names[i] = prefix + i;
		}
		return names;
	}

	private static void add(PlantGroup group, String key, String blocks, String... textures) {
		register(group, key, blocks, false, textures);
	}

	private static void addTintedOnly(PlantGroup group, String key, String blocks, String... textures) {
		register(group, key, blocks, true, textures);
	}

	private static void register(PlantGroup group, String key, String blocks, boolean onlyTinted, String[] textures) {
		List<Identifier> blockIds = Arrays.stream(blocks.split(",")).map(Identifier::withDefaultNamespace).toList();
		String[] textureNames = textures.length == 0 ? new String[]{key} : textures;
		List<Identifier> textureIds = Arrays.stream(textureNames).map(t -> Identifier.withDefaultNamespace("block/" + t)).toList();
		BY_KEY.put(key, new Plant(key, group, blockIds, textureIds, onlyTinted));
	}

	public static List<Plant> all() {
		return List.copyOf(BY_KEY.values());
	}

	public static List<Plant> inGroup(PlantGroup group) {
		List<Plant> result = new ArrayList<>();
		for (Plant plant : BY_KEY.values()) {
			if (plant.group() == group) {
				result.add(plant);
			}
		}
		return result;
	}

	public static @Nullable Plant byKey(String key) {
		return BY_KEY.get(key);
	}

	public static Set<Identifier> allTextures() {
		Set<Identifier> textures = new LinkedHashSet<>();
		for (Plant plant : BY_KEY.values()) {
			textures.addAll(plant.textures());
		}
		return textures;
	}

	public static @Nullable Plant forState(BlockState state) {
		Plant plant = lookup().get(state.getBlock());
		if (plant == null && state.is(BlockTags.LEAVES)) {
			return MODDED_LEAVES;
		}
		return plant;
	}

	private static Map<Block, Plant> lookup() {
		Map<Block, Plant> map = byBlock;
		if (map == null) {
			map = new IdentityHashMap<>();
			for (Plant plant : BY_KEY.values()) {
				for (Identifier id : plant.blocks()) {
					Optional<Block> block = BuiltInRegistries.BLOCK.getOptional(id);
					if (block.isPresent()) {
						map.put(block.get(), plant);
					}
				}
			}
			byBlock = map;
		}
		return map;
	}
}
