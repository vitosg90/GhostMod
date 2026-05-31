package com.ghostmod.screen;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.AlwaysSelectedEntryListWidget;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

public class ItemGiveScreen extends Screen {
	private static final int LIST_WIDTH = 300;
	private static final int MAX_RESULTS = 200;

	private TextFieldWidget searchField;
	private ItemListWidget itemList;
	private String lastSearch = "";

	public ItemGiveScreen() {
		super(Text.translatable("screen.ghostmod.item_give"));
	}

	@Override
	protected void init() {
		int listLeft = width / 2 - LIST_WIDTH / 2;

		searchField = new TextFieldWidget(
				textRenderer,
				listLeft,
				40,
				LIST_WIDTH,
				20,
				Text.translatable("screen.ghostmod.search")
		);
		searchField.setMaxLength(256);
		searchField.setPlaceholder(Text.translatable("screen.ghostmod.search"));
		searchField.setChangedListener(this::updateSearch);
		searchField.setFocusUnlocked(false);
		addSelectableChild(searchField);
		setInitialFocus(searchField);

		itemList = new ItemListWidget(client, LIST_WIDTH, height - 110, 70, height - 40);
		itemList.setLeftPos(listLeft);
		addSelectableChild(itemList);

		addDrawableChild(ButtonWidget.builder(Text.translatable("gui.cancel"), button -> close())
				.dimensions(width / 2 - 100, height - 30, 200, 20)
				.build());

		updateSearch("");
	}

	private void updateSearch(String query) {
		String normalized = query.toLowerCase(Locale.ROOT);
		if (normalized.equals(lastSearch)) {
			return;
		}
		lastSearch = normalized;

		List<Item> matches = new ArrayList<>();
		for (Item item : Registries.ITEM) {
			if (item == Items.AIR) {
				continue;
			}
			Identifier id = Registries.ITEM.getId(item);
			if (id.toString().contains(normalized)) {
				matches.add(item);
				if (matches.size() >= MAX_RESULTS) {
					break;
				}
			}
		}

		matches.sort(Comparator.comparing(item -> Registries.ITEM.getId(item).toString()));
		itemList.setItems(matches);
	}

	private void giveItem(Item item) {
		if (!canGiveItems()) {
			if (client.player != null) {
				client.player.sendMessage(Text.translatable("message.ghostmod.no_permission"), false);
			}
			return;
		}

		Identifier id = Registries.ITEM.getId(item);
		client.getNetworkHandler().sendChatCommand("give @s " + id + " 64");

		if (client.player != null) {
			client.player.sendMessage(Text.translatable("message.ghostmod.given", id.toString()), false);
		}
	}

	private boolean canGiveItems() {
		if (client.isIntegratedServerRunning()) {
			return true;
		}
		return client.player != null && client.player.hasPermissionLevel(2);
	}

	private void giveSelectedOrFirst() {
		ItemEntry selected = itemList.getSelectedOrNull();
		if (selected != null) {
			giveItem(selected.item);
			return;
		}

		if (!itemList.children().isEmpty()) {
			giveItem(itemList.children().getFirst().item);
		}
	}

	@Override
	public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
		if (searchField.keyPressed(keyCode, scanCode, modifiers)) {
			return true;
		}

		if (keyCode == GLFW.GLFW_KEY_ENTER) {
			giveSelectedOrFirst();
			return true;
		}

		return super.keyPressed(keyCode, scanCode, modifiers);
	}

	@Override
	public boolean charTyped(char chr, int modifiers) {
		return searchField.charTyped(chr, modifiers) || super.charTyped(chr, modifiers);
	}

	@Override
	public void render(DrawContext context, int mouseX, int mouseY, float delta) {
		renderBackground(context, mouseX, mouseY, delta);
		context.drawCenteredTextWithShadow(textRenderer, title, width / 2, 20, 0xFFFFFF);
		searchField.render(context, mouseX, mouseY, delta);
		itemList.render(context, mouseX, mouseY, delta);
		super.render(context, mouseX, mouseY, delta);
	}

	@Override
	public void close() {
		client.setScreen(null);
	}

	private class ItemListWidget extends AlwaysSelectedEntryListWidget<ItemEntry> {
		ItemListWidget(MinecraftClient client, int width, int height, int top, int bottom) {
			super(client, width, height, top, bottom, 24);
		}

		void setItems(List<Item> items) {
			clearEntries();
			for (Item item : items) {
				addEntry(new ItemEntry(item));
			}
			setSelected(null);
		}

		@Override
		public int getRowWidth() {
			return LIST_WIDTH - 10;
		}

		@Override
		protected int getScrollbarPositionX() {
			return getRowLeft() + getRowWidth() + 4;
		}

		@Override
		public boolean mouseClicked(double mouseX, double mouseY, int button) {
			boolean clicked = super.mouseClicked(mouseX, mouseY, button);
			if (clicked) {
				ItemEntry entry = getSelectedOrNull();
				if (entry != null) {
					giveItem(entry.item);
				}
			}
			return clicked;
		}
	}

	private class ItemEntry extends AlwaysSelectedEntryListWidget.Entry<ItemEntry> {
		private final Item item;

		ItemEntry(Item item) {
			this.item = item;
		}

		@Override
		public void render(
				DrawContext context,
				int index,
				int y,
				int x,
				int entryWidth,
				int entryHeight,
				int mouseX,
				int mouseY,
				boolean hovered,
				float tickDelta
		) {
			Identifier id = Registries.ITEM.getId(item);
			ItemStack stack = new ItemStack(item);
			context.drawItem(stack, x, y + 4);
			context.drawItemInSlot(textRenderer, stack, x, y + 4);
			context.drawTextWithShadow(textRenderer, id.toString(), x + 20, y + 8, hovered ? 0xFFFF55 : 0xFFFFFF);
		}

		@Override
		public Text getNarration() {
			return Text.literal(Registries.ITEM.getId(item).toString());
		}
	}
}
