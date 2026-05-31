package com.ghostmod.screen;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.AlwaysSelectedEntryListWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.List;

public class ItemGiveScreen extends Screen {

    private TextFieldWidget searchField;
    private ItemListWidget itemList;
    private List<Item> allItems;
    private List<Item> filteredItems;

    public ItemGiveScreen() {
        super(Text.literal("GhostMod - Give Item"));
    }

    @Override
    protected void init() {
        allItems = new ArrayList<>(Registries.ITEM.stream().toList());
        filteredItems = new ArrayList<>(allItems);

        searchField = new TextFieldWidget(textRenderer, width / 2 - 100, 20, 200, 20, Text.literal("Search..."));
        searchField.setChangedListener(this::onSearchChanged);
        addDrawableChild(searchField);

        itemList = new ItemListWidget(client, width, height - 60, 50, 24);
        itemList.updateItems(filteredItems);
        addDrawableChild(itemList);
    }

    private void onSearchChanged(String query) {
        filteredItems = allItems.stream()
                .filter(item -> Registries.ITEM.getId(item).toString().contains(query.toLowerCase()))
                .toList();
        itemList.updateItems(filteredItems);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        renderBackground(context, mouseX, mouseY, delta);
        super.render(context, mouseX, mouseY, delta);
        context.drawCenteredTextWithShadow(textRenderer, title, width / 2, 5, 0xFFFFFF);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == 257 || keyCode == 335) { // Enter
            giveSelectedItem();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    private void giveSelectedItem() {
        if (itemList.getSelectedOrNull() != null) {
            Item item = itemList.getSelectedOrNull().item;
            if (client.player != null) {
                client.player.getInventory().insertStack(new ItemStack(item, item.getDefaultStack().getMaxCount()));
            }
            close();
        }
    }

    public class ItemListWidget extends AlwaysSelectedEntryListWidget<ItemListWidget.ItemEntry> {

        public ItemListWidget(MinecraftClient client, int width, int height, int top, int itemHeight) {
            super(client, width, height, top, itemHeight);
        }

        public void updateItems(List<Item> items) {
            clearEntries();
            for (Item item : items) {
                addEntry(new ItemEntry(item));
            }
        }

        public class ItemEntry extends AlwaysSelectedEntryListWidget.Entry<ItemEntry> {
            public final Item item;

            public ItemEntry(Item item) {
                this.item = item;
            }

            @Override
            public Text getNarration() {
                return Text.literal(Registries.ITEM.getId(item).toString());
            }

            @Override
            public void render(DrawContext context, int index, int y, int x, int entryWidth, int entryHeight,
                               int mouseX, int mouseY, boolean hovered, float tickDelta) {
                Identifier id = Registries.ITEM.getId(item);
                context.drawItemWithoutEntity(new ItemStack(item), x + 2, y + 2);
                context.drawTextWithShadow(client.textRenderer, id.toString(), x + 24, y + 6, hovered ? 0xFFFF55 : 0xFFFFFF);
            }

            @Override
            public boolean mouseClicked(double mouseX, double mouseY, int button) {
                ItemListWidget.this.setSelected(this);
                giveSelectedItem();
                return true;
            }
        }
    }
}
