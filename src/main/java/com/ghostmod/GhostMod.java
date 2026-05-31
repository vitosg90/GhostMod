package com.ghostmod;

import net.fabricmc.api.ClientModInitializer;

public class GhostMod implements ClientModInitializer {
	public static final String MOD_ID = "ghostmod";

	@Override
	public void onInitializeClient() {
		GhostKeyBindings.register();
	}
}
