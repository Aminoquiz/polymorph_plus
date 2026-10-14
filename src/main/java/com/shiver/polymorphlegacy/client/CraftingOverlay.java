package com.shiver.polymorphlegacy.client;

import com.shiver.polymorphlegacy.Tags;
import com.shiver.polymorphlegacy.api.RecipeChoice;
import com.shiver.polymorphlegacy.api.client.CraftingGuiOrigin;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.RenderItem;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.init.SoundEvents;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextFormatting;
import org.lwjgl.input.Keyboard;

/**
 * The selector: a 16px button over the output slot and, when open, one row of at most seven
 * outputs that scrolls with the wheel, the side arrows or the arrow keys. Right-click pins
 * it open, shift-click marks a favourite (the star), shift+wheel on the output slot cycles.
 */
public final class CraftingOverlay {
    private static final ResourceLocation SELECTOR = texture("selector_button");
    private static final ResourceLocation SELECTOR_HOVER = texture("selector_button_highlighted");
    private static final ResourceLocation OUTPUT = texture("output_button");
    private static final ResourceLocation OUTPUT_HOVER = texture("output_button_highlighted");
    private static final ResourceLocation SELECTED = texture("current_output");
    private static final ResourceLocation SELECTED_HOVER = texture("current_output_highlighted");
    public static final int MAX_VISIBLE = 7;
    private static final int SIZE = 25;
    private static final int ARROW_WIDTH = 5;
    private static final int ARROW_GAP = 1;
    private static final int[] STAR = {0b0001000, 0b0001000, 0b1111111, 0b0111110, 0b0011100,
            0b0110110, 0b0100010};
    public final GuiContainer gui;
    public final int viewId;
    private final Slot outputSlot;
    private List<RecipeChoice> choices = Collections.emptyList();
    private ResourceLocation selected;
    private boolean expanded;
    private int scroll;
    private int capturedButton = -1;
    private Rectangle button = new Rectangle();
    private Rectangle row = new Rectangle();
    private Rectangle prevArrow = new Rectangle();
    private Rectangle nextArrow = new Rectangle();

    public CraftingOverlay(GuiContainer gui, int viewId, Slot outputSlot) {
        this.gui = gui;
        this.viewId = viewId;
        this.outputSlot = outputSlot;
    }

    private static ResourceLocation texture(String name) {
        return new ResourceLocation(Tags.MOD_ID, "textures/gui/" + name + ".png");
    }

    public void update(List<RecipeChoice> choices, @Nullable ResourceLocation selected) {
        boolean sameList = sameIds(this.choices, choices);
        this.choices = choices;
        this.selected = selected;
        if (choices.size() <= 1) {
            expanded = false;
        } else if (PolymorphClientConfig.isPinSelector()) {
            expanded = true;
        }
        if (!sameList) {
            scroll = 0;
        }
        layout();
        if (selected != null) {
            scrollIntoView(selected);
        }
    }

    private static boolean sameIds(List<RecipeChoice> a, List<RecipeChoice> b) {
        if (a.size() != b.size()) {
            return false;
        }
        for (int i = 0; i < a.size(); i++) {
            if (!a.get(i).getId().equals(b.get(i).getId())) {
                return false;
            }
        }
        return true;
    }

    private int left() {
        return gui instanceof CraftingGuiOrigin ? ((CraftingGuiOrigin) gui).polymorph$getCraftingLeft()
                : gui.getGuiLeft();
    }

    private int top() {
        return gui instanceof CraftingGuiOrigin ? ((CraftingGuiOrigin) gui).polymorph$getCraftingTop()
                : gui.getGuiTop();
    }

    private boolean canScroll() {
        return choices.size() > MAX_VISIBLE;
    }

    private int maxScroll() {
        return Math.max(0, choices.size() - MAX_VISIBLE);
    }

    private void layout() {
        button = new Rectangle(left() + outputSlot.xPos, top() + outputSlot.yPos - 22, 16, 16);
        scroll = Math.max(0, Math.min(scroll, maxScroll()));
        int visible = Math.max(1, Math.min(MAX_VISIBLE, choices.size()));
        int width = visible * SIZE;
        int margin = 4 + (canScroll() ? ARROW_WIDTH + ARROW_GAP : 0);
        // like the other versions: centred on the button but kept inside the container's width
        int guiLeft = gui.getGuiLeft();
        int guiWidth = gui.getXSize();
        int x = width <= guiWidth
                ? clamp(button.x + 8 - width / 2, guiLeft, guiLeft + guiWidth - width)
                : guiLeft + (guiWidth - width) / 2;
        x = clamp(x, margin, gui.width - width - margin);
        // a scrolling row sits above the GUI's top edge, so it never covers the title (as in the other versions)
        int y = canScroll() && gui.getGuiTop() - SIZE - 1 >= 0 ? gui.getGuiTop() - SIZE - 1 : button.y - SIZE - 4;
        if (y < 4) {
            y = button.y + 20;
        }
        row = new Rectangle(x, clamp(y, 4, gui.height - SIZE - 4), width, SIZE);
        prevArrow = new Rectangle(row.x - ARROW_GAP - ARROW_WIDTH, row.y, ARROW_WIDTH, SIZE);
        nextArrow = new Rectangle(row.x + row.width + ARROW_GAP, row.y, ARROW_WIDTH, SIZE);
    }

    private static int clamp(int value, int min, int max) {
        return max < min ? min : Math.max(min, Math.min(max, value));
    }

    private void scrollIntoView(ResourceLocation id) {
        int index = indexOf(id);
        if (index < 0 || !canScroll()) {
            return;
        }
        if (index < scroll) {
            scroll = index;
        } else if (index >= scroll + MAX_VISIBLE) {
            scroll = index - MAX_VISIBLE + 1;
        }
        layout();
    }

    private int indexOf(@Nullable ResourceLocation id) {
        for (int i = 0; i < choices.size(); i++) {
            if (choices.get(i).getId().equals(id)) {
                return i;
            }
        }
        return -1;
    }

    /** The favourite with the best rank, the only one that gets a star. */
    @Nullable
    private ResourceLocation favourite() {
        ResourceLocation best = null;
        int bestRank = Integer.MAX_VALUE;
        for (RecipeChoice choice : choices) {
            int rank = PolymorphClientConfig.recipeRankOf(choice.getId().toString());
            if (rank < bestRank) {
                bestRank = rank;
                best = choice.getId();
            }
        }
        return best;
    }

    public void draw(int mouseX, int mouseY) {
        drawBody(mouseX, mouseY);
        drawTooltips(mouseX, mouseY);
    }

    public void drawBody(int mouseX, int mouseY) {
        if (choices.size() <= 1) {
            return;
        }
        layout();
        Minecraft mc = Minecraft.getMinecraft();
        boolean pinned = PolymorphClientConfig.isPinSelector();
        GlStateManager.pushMatrix();
        GlStateManager.translate(0, 0, 400);
        GlStateManager.disableDepth();
        GlStateManager.disableLighting();
        sprite(button.contains(mouseX, mouseY) ? SELECTOR_HOVER : SELECTOR, button.x, button.y, 16);
        if (pinned) {
            Gui.drawRect(button.x + 11, button.y, button.x + 16, button.y + 5, 0xFF202020);
            Gui.drawRect(button.x + 12, button.y + 1, button.x + 15, button.y + 4, 0xFFFFD000);
        }
        if (expanded) {
            RenderItem renderer = mc.getRenderItem();
            ResourceLocation favourite = favourite();
            for (int i = scroll; i < Math.min(choices.size(), scroll + MAX_VISIBLE); i++) {
                RecipeChoice choice = choices.get(i);
                int x = row.x + (i - scroll) * SIZE;
                int y = row.y;
                boolean hover = new Rectangle(x, y, SIZE, SIZE).contains(mouseX, mouseY);
                boolean active = choice.getId().equals(selected);
                sprite(active ? hover ? SELECTED_HOVER : SELECTED : hover ? OUTPUT_HOVER : OUTPUT, x, y, SIZE);
                RenderHelper.enableGUIStandardItemLighting();
                renderer.renderItemAndEffectIntoGUI(choice.getOutput(), x + 4, y + 4);
                renderer.renderItemOverlayIntoGUI(mc.fontRenderer, choice.getOutput(), x + 4, y + 4, null);
                RenderHelper.disableStandardItemLighting();
                GlStateManager.disableDepth();
                if (choice.getId().equals(favourite)) {
                    drawStar(x + SIZE - 8, y + 3, 0xFF3A2A00);
                    drawStar(x + SIZE - 9, y + 2, 0xFFFFD000);
                }
            }
            if (canScroll()) {
                drawArrow(prevArrow, false, scroll > 0);
                drawArrow(nextArrow, true, scroll < maxScroll());
            }
        }
        TutorialOverlay.renderForButton(button.x, button.y);
        if (expanded) {
            TutorialOverlay.renderForSelector(gui.getGuiLeft() + 16,
                    Math.min(gui.getGuiTop() + gui.getYSize() + 4, gui.height - 40), canScroll());
        }
        GlStateManager.enableDepth();
        GlStateManager.popMatrix();
        GlStateManager.color(1, 1, 1, 1);
    }

    public void drawTooltips(int mouseX, int mouseY) {
        if (choices.size() <= 1) {
            return;
        }
        TutorialOverlay.flush();
        Minecraft mc = Minecraft.getMinecraft();
        RecipeChoice hovered = choiceAt(mouseX, mouseY);
        if (hovered != null) {
            ItemStack stack = hovered.getOutput();
            boolean advanced = mc.gameSettings.advancedItemTooltips;
            List<String> tooltip = new ArrayList<>(stack.getTooltip(mc.player,
                    advanced ? ITooltipFlag.TooltipFlags.ADVANCED : ITooltipFlag.TooltipFlags.NORMAL));
            if (!tooltip.isEmpty()) {
                tooltip.set(0, stack.getItem().getForgeRarity(stack).getColor() + tooltip.get(0));
                for (int i = 1; i < tooltip.size(); i++) {
                    tooltip.set(i, TextFormatting.GRAY + tooltip.get(i));
                }
            }
            if (advanced) {
                tooltip.add(TextFormatting.DARK_GRAY + hovered.getId().toString());
            }
            gui.drawHoveringText(tooltip, mouseX, mouseY);
        } else if (button.contains(mouseX, mouseY)) {
            gui.drawHoveringText(Collections.singletonList(I18n.format("polymorph_plus.choose")), mouseX, mouseY);
        } else if (expanded && canScroll() && (prevArrow.contains(mouseX, mouseY) || nextArrow.contains(mouseX, mouseY))) {
            gui.drawHoveringText(Collections.singletonList(I18n.format("polymorph_plus.pages")), mouseX, mouseY);
        }
    }

    private static void drawStar(int x, int y, int color) {
        for (int r = 0; r < STAR.length; r++) {
            for (int c = 0; c < 7; c++) {
                if ((STAR[r] & (1 << (6 - c))) != 0) {
                    Gui.drawRect(x + c, y + r, x + c + 1, y + r + 1, color);
                }
            }
        }
    }

    private static void drawArrow(Rectangle rect, boolean forward, boolean enabled) {
        Gui.drawRect(rect.x, rect.y, rect.x + rect.width, rect.y + rect.height, 0xCC000000);
        int color = enabled ? 0xFFFFFFFF : 0x66FFFFFF;
        int cy = rect.y + SIZE / 2;
        for (int i = -4; i <= 4; i++) {
            int tip = 4 - Math.abs(i);
            int px = rect.x + (forward ? tip : 4 - tip);
            Gui.drawRect(px, cy + i, px + 1, cy + i + 1, color);
        }
    }

    private static void sprite(ResourceLocation texture, int x, int y, int size) {
        GlStateManager.color(1, 1, 1, 1);
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        Minecraft.getMinecraft().getTextureManager().bindTexture(texture);
        Gui.drawModalRectWithCustomSizedTexture(x, y, 0, 0, size, size, size, size);
    }

    public boolean mouse(int mouseX, int mouseY, int mouseButton, boolean pressed, int wheel) {
        if (mouseButton >= 0 && !pressed && capturedButton == mouseButton) {
            capturedButton = -1;
            return true;
        }
        if (choices.size() <= 1) {
            return false;
        }
        layout();
        if (wheel != 0) {
            if (GuiScreen.isShiftKeyDown() && PolymorphClientConfig.isScrollCycle() && overOutput(mouseX, mouseY)) {
                cycle(wheel > 0 ? -1 : 1);
                return true;
            }
            if (expanded && canScroll() && overRow(mouseX, mouseY)) {
                scrollBy(wheel > 0 ? -1 : 1);
                return true;
            }
            return false;
        }
        if (mouseButton < 0 || !pressed) {
            return false;
        }
        boolean pinned = PolymorphClientConfig.isPinSelector();
        if (button.contains(mouseX, mouseY)) {
            if (mouseButton == 1) {
                PolymorphClientConfig.setPinSelector(!pinned);
                expanded = !pinned || expanded;
                TutorialOverlay.onPinToggled();
            } else if (pinned) {
                PolymorphClientConfig.setPinSelector(false);
                expanded = false;
            } else {
                expanded = !expanded;
                if (expanded) {
                    TutorialOverlay.onOpened();
                }
            }
            capture(mouseButton);
            return true;
        }
        if (!expanded) {
            return false;
        }
        if (canScroll() && (prevArrow.contains(mouseX, mouseY) || nextArrow.contains(mouseX, mouseY))) {
            scrollBy(prevArrow.contains(mouseX, mouseY) ? -1 : 1);
            capture(mouseButton);
            return true;
        }
        RecipeChoice choice = choiceAt(mouseX, mouseY);
        if (choice != null) {
            ClientRecipes.select(choice.getId());
            TutorialOverlay.onPicked();
            boolean favourite = GuiScreen.isShiftKeyDown();
            if (favourite) {
                if (PolymorphClientConfig.promoteRecipe(choice.getId().toString())) {
                    ClientRecipes.uploadPriority();
                }
                TutorialOverlay.onFavourite();
            } else if (!pinned) {
                expanded = false;
            }
        } else if (pinned) {
            return false;
        } else {
            expanded = false;
        }
        // Swallow the matching release too, so the click does not reach the slot underneath.
        capture(mouseButton);
        return true;
    }

    private boolean overOutput(int x, int y) {
        int sx = left() + outputSlot.xPos;
        int sy = top() + outputSlot.yPos;
        return x >= sx && x < sx + 16 && y >= sy && y < sy + 16;
    }

    private boolean overRow(int x, int y) {
        return row.contains(x, y) || prevArrow.contains(x, y) || nextArrow.contains(x, y);
    }

    private void cycle(int step) {
        int current = indexOf(selected);
        int next = current < 0 ? (step > 0 ? 0 : choices.size() - 1) : Math.floorMod(current + step, choices.size());
        selected = choices.get(next).getId();
        scrollIntoView(selected);
        ClientRecipes.select(selected);
        TutorialOverlay.onPicked();
        TutorialOverlay.onCycled();
    }

    private void scrollBy(int delta) {
        int previous = scroll;
        scroll = Math.max(0, Math.min(scroll + delta, maxScroll()));
        if (scroll != previous) {
            layout();
        }
        TutorialOverlay.onScrolled();
    }

    private void capture(int mouseButton) {
        capturedButton = mouseButton;
        Minecraft.getMinecraft().getSoundHandler().playSound(PositionedSoundRecord.getMasterRecord(SoundEvents.UI_BUTTON_CLICK, 1));
    }

    public boolean key(int key) {
        if (!expanded || choices.size() <= 1) {
            return false;
        }
        if (key == Keyboard.KEY_ESCAPE) {
            if (PolymorphClientConfig.isPinSelector()) {
                return false;
            }
            expanded = false;
            return true;
        }
        if ((key == Keyboard.KEY_LEFT || key == Keyboard.KEY_RIGHT) && canScroll()) {
            scrollBy(key == Keyboard.KEY_LEFT ? -1 : 1);
            return true;
        }
        return false;
    }

    @Nullable
    private RecipeChoice choiceAt(int x, int y) {
        if (!expanded || !row.contains(x, y)) {
            return null;
        }
        int index = scroll + (x - row.x) / SIZE;
        return index < choices.size() && index < scroll + MAX_VISIBLE ? choices.get(index) : null;
    }

    public List<Rectangle> extraAreas() {
        if (choices.size() <= 1) {
            return Collections.emptyList();
        }
        layout();
        List<Rectangle> areas = new ArrayList<>();
        areas.add(new Rectangle(button));
        if (expanded) {
            areas.add(canScroll() ? row.union(prevArrow).union(nextArrow) : new Rectangle(row));
        }
        return areas;
    }

    @Nullable
    public ItemStack ingredientUnderMouse(int x, int y) {
        layout();
        RecipeChoice choice = choiceAt(x, y);
        return choice == null ? null : choice.getOutput();
    }
}
