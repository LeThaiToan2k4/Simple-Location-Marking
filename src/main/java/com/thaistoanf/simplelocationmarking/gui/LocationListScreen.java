package com.thaistoanf.simplelocationmarking.gui;

import com.thaistoanf.simplelocationmarking.util.DimensionNames;
import com.thaistoanf.simplelocationmarking.waypoint.Waypoint;
import com.thaistoanf.simplelocationmarking.waypoint.WaypointManager;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.input.KeyInput;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

/**
 * Main Location Management Screen opened with the {@code P} keybind.
 * <p>
 * Styled with a Translucent Dark Glass aesthetic: renders a subtle dark translucent backdrop over
 * the live game world, a centered glass container with a top header bar, dimension and coordinate
 * pill badges, card-styled waypoint rows, and a slim 4px semi-transparent scrollbar.
 */
public final class LocationListScreen extends Screen {
    private static final int PANEL_MARGIN_Y = 10;
    private static final int HEADER_HEIGHT = 42;
    private static final int FOOTER_HEIGHT = 34;
    private static final int ROW_HEIGHT = 24;
    private static final int ROW_PADDING = 5;
    private static final int SCROLLBAR_WIDTH = 4;

    private final List<Waypoint> dimensionWaypoints = new ArrayList<>();
    private String currentDimensionId = "minecraft:overworld";

    private Waypoint selected;
    private double scrollOffset;
    private boolean draggingScrollbar;

    private int panelLeft;
    private int panelTop;
    private int panelRight;
    private int panelBottom;

    private int listLeft;
    private int listRight;
    private int listTop;
    private int listBottom;

    private ButtonWidget addButton;
    private ButtonWidget editButton;
    private ButtonWidget hideShowButton;
    private ButtonWidget deleteButton;

    private long lastClickTimeMs;
    private Waypoint lastClickedWaypoint;

    public LocationListScreen() {
        super(Text.translatable("simplelocationmarking.screen.list.title"));
    }

    public void setSelected(Waypoint waypoint) {
        this.selected = waypoint;
        updateButtonStates();
    }

    @Override
    protected void init() {
        super.init();

        if (client != null && client.world != null) {
            currentDimensionId = WaypointManager.dimensionId(client.world);
        }
        reloadWaypoints();

        int panelWidth = Math.min(440, this.width - 20);
        panelLeft = (this.width - panelWidth) / 2;
        panelRight = panelLeft + panelWidth;
        panelTop = PANEL_MARGIN_Y;
        panelBottom = this.height - PANEL_MARGIN_Y;

        int innerWidth = panelWidth - 20;
        listLeft = panelLeft + 10;
        listRight = panelRight - 10;
        listTop = panelTop + HEADER_HEIGHT + 7;
        listBottom = panelBottom - FOOTER_HEIGHT;

        int buttonGap = 6;
        int buttonWidth = (innerWidth - buttonGap * 3) / 4;
        int buttonY = panelBottom - 26;

        addButton = addDrawableChild(ButtonWidget.builder(
                        Text.translatable("simplelocationmarking.button.add"),
                        btn -> openAddScreen())
                .dimensions(listLeft, buttonY, buttonWidth, 20)
                .build());

        editButton = addDrawableChild(ButtonWidget.builder(
                        Text.translatable("simplelocationmarking.button.edit"),
                        btn -> openEditScreen())
                .dimensions(listLeft + (buttonWidth + buttonGap), buttonY, buttonWidth, 20)
                .build());

        hideShowButton = addDrawableChild(ButtonWidget.builder(
                        Text.translatable("simplelocationmarking.button.hide_show"),
                        btn -> toggleSelectedHidden())
                .dimensions(listLeft + (buttonWidth + buttonGap) * 2, buttonY, buttonWidth, 20)
                .build());

        deleteButton = addDrawableChild(ButtonWidget.builder(
                        Text.translatable("simplelocationmarking.button.delete"),
                        btn -> deleteSelected())
                .dimensions(listLeft + (buttonWidth + buttonGap) * 3, buttonY, innerWidth - (buttonWidth + buttonGap) * 3, 20)
                .build());

        clampScroll();
        updateButtonStates();
    }

    public void reloadWaypoints() {
        WaypointManager.collectForDimension(currentDimensionId, dimensionWaypoints);
        if (selected != null && !dimensionWaypoints.contains(selected)) {
            selected = null;
        }
        clampScroll();
        updateButtonStates();
    }

    private void updateButtonStates() {
        boolean hasSelection = selected != null && dimensionWaypoints.contains(selected);
        if (addButton != null) {
            addButton.active = true;
        }
        if (editButton != null) {
            editButton.active = hasSelection;
        }
        if (hideShowButton != null) {
            hideShowButton.active = hasSelection;
        }
        if (deleteButton != null) {
            deleteButton.active = hasSelection;
        }
    }

    private void openAddScreen() {
        if (client != null) {
            client.setScreen(new AddWaypointScreen(this));
        }
    }

    private void openEditScreen() {
        if (client != null && selected != null) {
            client.setScreen(new EditWaypointScreen(this, selected));
        }
    }

    private void toggleSelectedHidden() {
        if (selected != null) {
            WaypointManager.toggleHidden(selected);
            updateButtonStates();
        }
    }

    private void deleteSelected() {
        if (selected != null) {
            int idx = dimensionWaypoints.indexOf(selected);
            WaypointManager.remove(selected);
            reloadWaypoints();
            if (!dimensionWaypoints.isEmpty() && idx >= 0) {
                selected = dimensionWaypoints.get(Math.min(idx, dimensionWaypoints.size() - 1));
            } else {
                selected = null;
            }
            updateButtonStates();
        }
    }

    // ------------------------------------------------------------------------------------------
    // Scrolling helpers
    // ------------------------------------------------------------------------------------------

    private int contentHeight() {
        return dimensionWaypoints.size() * ROW_HEIGHT;
    }

    private int viewHeight() {
        return Math.max(0, listBottom - listTop - ROW_PADDING * 2);
    }

    private int maxScroll() {
        return Math.max(0, contentHeight() - viewHeight());
    }

    private void clampScroll() {
        scrollOffset = MathHelper.clamp(scrollOffset, 0.0, maxScroll());
    }

    // ------------------------------------------------------------------------------------------
    // Rendering
    // ------------------------------------------------------------------------------------------

    @Override
    public void renderBackground(DrawContext context, int mouseX, int mouseY, float delta) {
        // Subtle dark translucent full-screen overlay over the live game world (no blur or dirt background)
        GlassUi.drawScreenBackdrop(context, this.width, this.height);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        int centerX = this.width / 2;

        // 1. Centered Translucent Glass Main Panel + Top Header Bar
        GlassUi.drawMainPanel(context, panelLeft, panelTop, panelRight, panelBottom, HEADER_HEIGHT);

        // 2. Header Title & Dimension / Coordinates Pill Badges
        context.drawCenteredTextWithShadow(this.textRenderer, this.title, centerX, panelTop + 6, 0xFFFFFFFF);
        renderHeaderPills(context, centerX, panelTop + 21);

        // 3. Recessed Scrollable Waypoint List Viewport (0x880B0C10)
        GlassUi.drawSubPanel(context, listLeft, listTop, listRight, listBottom);

        if (dimensionWaypoints.isEmpty()) {
            Text emptyText = Text.translatable("simplelocationmarking.screen.list.empty");
            context.drawCenteredTextWithShadow(
                    this.textRenderer,
                    emptyText,
                    centerX,
                    (listTop + listBottom) / 2 - 4,
                    0xFF8892A8
            );
        } else {
            renderRows(context, mouseX, mouseY);
            renderScrollbar(context, mouseX, mouseY);
        }

        // 4. Action Buttons
        super.render(context, mouseX, mouseY, delta);
    }

    private void renderHeaderPills(DrawContext context, int centerX, int pillTop) {
        Text dimName = DimensionNames.of(currentDimensionId);
        String dimStr = dimName.getString();

        int dimFill;
        int dimBorder;
        int dimText;
        switch (currentDimensionId) {
            case "minecraft:overworld" -> {
                dimFill = 0x9010281C;
                dimBorder = 0xFF2ECC71;
                dimText = 0xFF66FFAA;
            }
            case "minecraft:the_nether" -> {
                dimFill = 0x902C1214;
                dimBorder = 0xFFE74C3C;
                dimText = 0xFFFF8070;
            }
            case "minecraft:the_end" -> {
                dimFill = 0x90221232;
                dimBorder = 0xFFA855F7;
                dimText = 0xFFDCA0FF;
            }
            default -> {
                dimFill = 0x90102230;
                dimBorder = 0xFF38BDF8;
                dimText = 0xFF7DD3FC;
            }
        }

        BlockPos pos = (client != null && client.player != null) ? client.player.getBlockPos() : BlockPos.ORIGIN;
        String coordsStr = "X: " + pos.getX() + "  Y: " + pos.getY() + "  Z: " + pos.getZ();

        int pillHeight = 15;
        int dimWidth = this.textRenderer.getWidth(dimStr) + 18;
        int coordsWidth = this.textRenderer.getWidth(coordsStr) + 14;
        int gap = 8;
        int totalWidth = dimWidth + gap + coordsWidth;
        int startX = centerX - totalWidth / 2;

        // Dimension pill box with glowing status dot
        int dimRight = startX + dimWidth;
        GlassUi.drawPill(context, startX, pillTop, dimRight, pillTop + pillHeight, dimFill, dimBorder);
        int dotX = startX + 5;
        int dotY = pillTop + 5;
        context.fill(dotX - 1, dotY - 1, dotX + 6, dotY + 6, 0x44000000 | (dimBorder & 0xFFFFFF));
        context.fill(dotX, dotY, dotX + 5, dotY + 5, dimBorder);
        context.drawTextWithShadow(this.textRenderer, dimStr, startX + 13, pillTop + 4, dimText);

        // Coordinates pill box
        int coordsLeft = dimRight + gap;
        int coordsRight = coordsLeft + coordsWidth;
        GlassUi.drawPill(context, coordsLeft, pillTop, coordsRight, pillTop + pillHeight, 0x90131824, 0xFF384462);
        context.drawTextWithShadow(this.textRenderer, coordsStr, coordsLeft + 7, pillTop + 4, 0xFFDCE6FF);
    }

    private void renderRows(DrawContext context, int mouseX, int mouseY) {
        ClientPlayerEntity player = client != null ? client.player : null;
        double px = player != null ? player.getX() : 0.0;
        double py = player != null ? player.getY() : 0.0;
        double pz = player != null ? player.getZ() : 0.0;

        int innerLeft = listLeft + ROW_PADDING;
        int innerRight = listRight - ROW_PADDING - (maxScroll() > 0 ? SCROLLBAR_WIDTH + 5 : 0);
        int innerTop = listTop + ROW_PADDING;
        int innerBottom = listBottom - ROW_PADDING;

        context.enableScissor(listLeft + 1, listTop + 1, listRight - 1, listBottom - 1);

        String hiddenTag = Text.translatable("simplelocationmarking.waypoint.hidden_tag").getString();
        int hiddenTagW = this.textRenderer.getWidth(hiddenTag);

        for (int i = 0, n = dimensionWaypoints.size(); i < n; i++) {
            Waypoint wp = dimensionWaypoints.get(i);
            int rowTop = (int) Math.round(innerTop + i * ROW_HEIGHT - scrollOffset);
            int rowBottom = rowTop + ROW_HEIGHT - 3; // 21px card height + 3px spacing

            if (rowBottom < innerTop || rowTop > innerBottom) {
                continue;
            }

            boolean isSelected = (wp == selected);
            boolean isHovered = mouseX >= innerLeft && mouseX <= innerRight
                    && mouseY >= Math.max(rowTop, innerTop) && mouseY <= Math.min(rowBottom, innerBottom);

            // Row card styling (0x661E222F normal, 0x99282E40 hover, 0xBB2D364D + bright 1px border selected)
            if (isSelected) {
                context.fill(innerLeft, rowTop, innerRight, rowBottom, GlassUi.ROW_SELECTED_BG);
                GlassUi.drawBorder(context, innerLeft, rowTop, innerRight, rowBottom, GlassUi.ROW_SELECTED_BORDER);
            } else if (isHovered) {
                context.fill(innerLeft, rowTop, innerRight, rowBottom, GlassUi.ROW_HOVER_BG);
                GlassUi.drawBorder(context, innerLeft, rowTop, innerRight, rowBottom, GlassUi.ROW_HOVER_BORDER);
            } else {
                context.fill(innerLeft, rowTop, innerRight, rowBottom, GlassUi.ROW_NORMAL_BG);
                GlassUi.drawBorder(context, innerLeft, rowTop, innerRight, rowBottom, GlassUi.ROW_NORMAL_BORDER);
            }

            int cardHeight = rowBottom - rowTop;
            int textY = rowTop + (cardHeight - 8) / 2;

            // 1. Glowing colored square swatch [11x11] with 1px white/dark border
            int swatchSize = 11;
            int swatchX = innerLeft + 6;
            int swatchY = rowTop + (cardHeight - swatchSize) / 2;
            GlassUi.drawGlowingSwatch(context, swatchX, swatchY, swatchSize, wp.color);

            // 2. Right-aligned distance badge (m / km)
            double dist = wp.distanceTo(px, py, pz);
            String distStr = wp.distanceText(dist);
            int distTextW = wp.distanceWidth(this.textRenderer);
            int distBadgeW = distTextW + 10;
            int distRight = innerRight - 5;
            int distLeft = distRight - distBadgeW;
            int badgeTop = rowTop + 3;
            int badgeBottom = rowBottom - 3;
            GlassUi.drawPill(context, distLeft, badgeTop, distRight, badgeBottom, 0x880C0E15, 0xFF30384E);
            context.drawTextWithShadow(this.textRenderer, distStr, distLeft + 5, textY, 0xFFDCE6FF);

            int rightLimit = distLeft - 6;

            // 3. Distinct amber/red [Hidden] ([Đã ẩn]) tag when hidden
            if (wp.hidden) {
                int tagBadgeW = hiddenTagW + 8;
                int tagRight = rightLimit;
                int tagLeft = tagRight - tagBadgeW;
                GlassUi.drawPill(context, tagLeft, badgeTop, tagRight, badgeBottom, 0x55FF4838, 0xBBFF6E58);
                context.drawTextWithShadow(this.textRenderer, hiddenTag, tagLeft + 4, textY, 0xFFFFAE70);
                rightLimit = tagLeft - 6;
            }

            // 4. Waypoint name & muted gray coordinates (X, Y, Z)
            String displayName = wp.displayName();
            if (displayName.isEmpty()) {
                displayName = "?";
            }
            String coordsText = "(" + wp.x + ", " + wp.y + ", " + wp.z + ")";
            int coordsW = this.textRenderer.getWidth(coordsText);

            int nameX = swatchX + swatchSize + 7;
            int availableForNameAndCoords = Math.max(20, rightLimit - nameX);
            int maxNameW = Math.max(24, availableForNameAndCoords - coordsW - 8);

            String fittedName = this.textRenderer.trimToWidth(displayName, maxNameW);
            if (fittedName.length() < displayName.length() && fittedName.length() > 2) {
                fittedName = this.textRenderer.trimToWidth(displayName, maxNameW - 8) + "..";
            }

            int nameColor = wp.hidden ? 0xFF9AA2B4 : 0xFFFFFFFF;
            context.drawTextWithShadow(this.textRenderer, fittedName, nameX, textY, nameColor);

            int coordsX = nameX + this.textRenderer.getWidth(fittedName) + 6;
            if (coordsX + coordsW <= rightLimit) {
                int coordsColor = wp.hidden ? 0xFF6E7586 : 0xFF9098AE;
                context.drawTextWithShadow(this.textRenderer, coordsText, coordsX, textY, coordsColor);
            }
        }

        context.disableScissor();
    }

    private void renderScrollbar(DrawContext context, int mouseX, int mouseY) {
        int max = maxScroll();
        if (max <= 0) {
            return;
        }
        int trackLeft = listRight - ROW_PADDING - SCROLLBAR_WIDTH;
        int trackRight = listRight - ROW_PADDING;
        int trackTop = listTop + ROW_PADDING;
        int trackBottom = listBottom - ROW_PADDING;
        int trackHeight = trackBottom - trackTop;

        // Slim 4px semi-transparent scrollbar track
        context.fill(trackLeft, trackTop, trackRight, trackBottom, 0x5008090D);

        int thumbHeight = Math.max(18, (int) ((double) viewHeight() / contentHeight() * trackHeight));
        int thumbTop = trackTop + (int) ((scrollOffset / max) * (trackHeight - thumbHeight));
        int thumbBottom = thumbTop + thumbHeight;

        boolean hovered = draggingScrollbar || (mouseX >= trackLeft && mouseX <= trackRight && mouseY >= thumbTop && mouseY <= thumbBottom);
        int thumbColor = hovered ? 0xD0A8B8DC : 0x907884A0;
        context.fill(trackLeft, thumbTop, trackRight, thumbBottom, thumbColor);
    }

    // ------------------------------------------------------------------------------------------
    // Input handling
    // ------------------------------------------------------------------------------------------

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        if (maxScroll() > 0 && mouseX >= listLeft && mouseX <= listRight && mouseY >= listTop && mouseY <= listBottom) {
            scrollOffset -= verticalAmount * (ROW_HEIGHT * 1.25);
            clampScroll();
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
    }

    @Override
    public boolean mouseClicked(Click click, boolean doubled) {
        if (super.mouseClicked(click, doubled)) {
            return true;
        }
        if (click.button() != 0) {
            return false;
        }

        double mouseX = click.x();
        double mouseY = click.y();

        // Check scrollbar click
        if (maxScroll() > 0) {
            int trackLeft = listRight - ROW_PADDING - SCROLLBAR_WIDTH - 2;
            int trackRight = listRight - ROW_PADDING + 2;
            int trackTop = listTop + ROW_PADDING;
            int trackBottom = listBottom - ROW_PADDING;
            if (mouseX >= trackLeft && mouseX <= trackRight && mouseY >= trackTop && mouseY <= trackBottom) {
                draggingScrollbar = true;
                updateScrollFromMouse(mouseY);
                return true;
            }
        }

        // Check row click
        int innerLeft = listLeft + ROW_PADDING;
        int innerRight = listRight - ROW_PADDING - (maxScroll() > 0 ? SCROLLBAR_WIDTH + 5 : 0);
        int innerTop = listTop + ROW_PADDING;
        int innerBottom = listBottom - ROW_PADDING;

        if (mouseX >= innerLeft && mouseX <= innerRight && mouseY >= innerTop && mouseY <= innerBottom) {
            int clickedIndex = (int) ((mouseY - innerTop + scrollOffset) / ROW_HEIGHT);
            if (clickedIndex >= 0 && clickedIndex < dimensionWaypoints.size()) {
                Waypoint clicked = dimensionWaypoints.get(clickedIndex);
                long now = System.currentTimeMillis();
                boolean isDoubleClick = doubled || (clicked == lastClickedWaypoint && (now - lastClickTimeMs) < 300L);
                lastClickedWaypoint = clicked;
                lastClickTimeMs = now;

                selected = clicked;
                updateButtonStates();
                if (client != null) {
                    client.getSoundManager().play(PositionedSoundInstance.ui(SoundEvents.UI_BUTTON_CLICK, 1.0f));
                }
                if (isDoubleClick) {
                    openEditScreen();
                }
                return true;
            }
        }

        return false;
    }

    @Override
    public boolean mouseDragged(Click click, double offsetX, double offsetY) {
        if (draggingScrollbar && click.button() == 0) {
            updateScrollFromMouse(click.y());
            return true;
        }
        return super.mouseDragged(click, offsetX, offsetY);
    }

    @Override
    public boolean mouseReleased(Click click) {
        if (click.button() == 0 && draggingScrollbar) {
            draggingScrollbar = false;
            return true;
        }
        return super.mouseReleased(click);
    }

    private void updateScrollFromMouse(double mouseY) {
        int max = maxScroll();
        if (max <= 0) {
            return;
        }
        int trackTop = listTop + ROW_PADDING;
        int trackBottom = listBottom - ROW_PADDING;
        int trackHeight = trackBottom - trackTop;
        int thumbHeight = Math.max(18, (int) ((double) viewHeight() / contentHeight() * trackHeight));
        int usable = Math.max(1, trackHeight - thumbHeight);
        double ratio = (mouseY - trackTop - thumbHeight * 0.5) / usable;
        scrollOffset = MathHelper.clamp(ratio * max, 0.0, max);
    }

    @Override
    public boolean keyPressed(KeyInput input) {
        int key = input.getKeycode();
        if (key == GLFW.GLFW_KEY_DELETE && selected != null) {
            deleteSelected();
            return true;
        }
        if ((key == GLFW.GLFW_KEY_UP || key == GLFW.GLFW_KEY_DOWN) && !dimensionWaypoints.isEmpty()) {
            int idx = selected != null ? dimensionWaypoints.indexOf(selected) : -1;
            if (key == GLFW.GLFW_KEY_UP) {
                idx = idx <= 0 ? 0 : idx - 1;
            } else {
                idx = idx < 0 ? 0 : Math.min(dimensionWaypoints.size() - 1, idx + 1);
            }
            selected = dimensionWaypoints.get(idx);
            ensureVisible(idx);
            updateButtonStates();
            return true;
        }
        return super.keyPressed(input);
    }

    private void ensureVisible(int rowIndex) {
        int rowTop = rowIndex * ROW_HEIGHT;
        int rowBottom = rowTop + ROW_HEIGHT;
        if (rowTop < scrollOffset) {
            scrollOffset = rowTop;
        } else if (rowBottom > scrollOffset + viewHeight()) {
            scrollOffset = rowBottom - viewHeight();
        }
        clampScroll();
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}
