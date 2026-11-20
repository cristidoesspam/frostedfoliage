package dev.frostedfoliage.mixin;

import com.mojang.serialization.MapCodec;
import dev.frostedfoliage.FrostedFoliage;
import dev.frostedfoliage.sprite.FrostSpriteSource;
import net.minecraft.client.renderer.texture.atlas.SpriteSource;
import net.minecraft.client.renderer.texture.atlas.SpriteSources;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ExtraCodecs;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SpriteSources.class)
public abstract class SpriteSourcesMixin {
	@Shadow
	@Final
	private static ExtraCodecs.LateBoundIdMapper<Identifier, MapCodec<? extends SpriteSource>> ID_MAPPER;

	@Inject(method = "bootstrap", at = @At("TAIL"))
	private static void frostedfoliage$registerFrostSource(CallbackInfo ci) {
		ID_MAPPER.put(Identifier.fromNamespaceAndPath(FrostedFoliage.MOD_ID, "frosted"), FrostSpriteSource.CODEC);
	}
}
