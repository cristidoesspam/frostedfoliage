package dev.frostedfoliage.plant;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Block;

import java.util.List;
import java.util.Optional;

public record Plant(String key, PlantGroup group, List<Identifier> blocks, List<Identifier> textures, boolean onlyTinted) {
	public Optional<Block> displayBlock() {
		for (Identifier id : blocks) {
			Optional<Block> block = BuiltInRegistries.BLOCK.getOptional(id);
			if (block.isPresent()) {
				return block;
			}
		}
		return Optional.empty();
	}

	public Component displayName() {
		return displayBlock()
			.<Component>map(Block::getName)
			.orElseGet(() -> Component.translatable("frostedfoliage.plant." + key));
	}
}
