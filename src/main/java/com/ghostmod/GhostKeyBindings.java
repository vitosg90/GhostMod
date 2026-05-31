package com.ghostmod;

import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

public final class GhostKeyBindings {
	public static KeyBinding openItemGive;

	private GhostKeyBindings() {
	}

	public static void register() {
		openItemGive = KeyBindingHelper.registerKeyBinding(new KeyBinding(
				"key.ghostmod.open_item_give",
				InputUtil.Type.KEYSYM,
				GLFW.GLFW_KEY_G,
				"category.ghostmod"
		));
	}
}
