package com.ghostmod.mixin;

import com.ghostmod.GhostKeyBindings;
import com.ghostmod.screen.ItemGiveScreen;
import net.minecraft.client.MinecraftClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftClient.class)
public class ClientMixin {
	@Inject(method = "tick", at = @At("HEAD"))
	private void ghostmod$onTick(CallbackInfo ci) {
		MinecraftClient client = (MinecraftClient) (Object) this;

		while (GhostKeyBindings.openItemGive.wasPressed()) {
			if (client.player != null) {
				client.setScreen(new ItemGiveScreen());
			}
		}
	}
}
