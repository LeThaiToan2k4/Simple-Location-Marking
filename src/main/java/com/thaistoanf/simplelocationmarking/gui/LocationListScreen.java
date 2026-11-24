package com.thaistoanf.simplelocationmarking.gui;

import com.mojang.blaze3d.platform.InputConstants;
import com.thaistoanf.simplelocationmarking.util.DimensionNames;
import com.thaistoanf.simplelocationmarking.waypoint.Waypoint;
import com.thaistoanf.simplelocationmarking.waypoint.WaypointManager;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

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

    private final @NonNull List<Waypoint> dimensionWaypoints = new ArrayList<>();
    private @NonNull String currentDimensionId = "minecraft:overworld";

    private @Nullable Waypoint selected;
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

    private @Nullable Button addButton;
    private @Nullable Button editButton;
    private @Nullable Button hideShowButton;
    private @Nullable Button deleteButton;

    private long lastClickTimeMs;
    private @Nullable Waypoint lastClickedWaypoint;

    public LocationListScreen() {
        super(Component.translatable("simplelocationmarking.screen.list.title"));
    }

    public void setSelected(@Nullable Waypoint waypoint) {
        this.selected = waypoint;
        updateButtonStates();
    }

    @Override
    protected void init() {
        super.init();

        ClientLevel level = this.minecraft.level;
        if (level != null) {
            currentDimensionId = WaypointManager.dimensionId(level);
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

        addButton = addRenderableWidget(Button.builder(
                        Component.translatable("simplelocationmarking.button.add"),
                        btn -> openAddScreen())
                .bounds(listLeft, buttonY, buttonWidth, 20)
                .build());

        editButton = addRenderableWidget(Button.builder(
                        Component.translatable("simplelocationmarking.button.edit"),
                        btn -> openEditScreen())
                .bounds(listLeft + (buttonWidth + buttonGap), buttonY, buttonWidth, 20)
                .build());

        hideShowButton = addRenderableWidget(Button.builder(
                        Component.translatable("simplelocationmarking.button.hide_show"),
                        btn -> toggleSelectedHidden())
                .bounds(listLeft + (buttonWidth + buttonGap) * 2, buttonY, buttonWidth, 20)
                .build());

        deleteButton = addRenderableWidget(Button.builder(
                        Component.translatable("simplelocationmarking.button.delete"),
                        btn -> deleteSelected())
                .bounds(listLeft + (buttonWidth + buttonGap) * 3, buttonY, innerWidth - (buttonWidth + buttonGap) * 3, 20)
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
        Button addBtn = addButton;
        if (addBtn != null) {
            addBtn.active = true;
        }
        Button editBtn = editButton;
        if (editBtn != null) {
            editBtn.active = hasSelection;
        }
        Button hideBtn = hideShowButton;
        if (hideBtn != null) {
            hideBtn.active = hasSelection;
        }
        Button delBtn = deleteButton;
        if (delBtn != null) {
            delBtn.active = hasSelection;
        }
    }

    private void openAddScreen() {
        this.minecraft.gui.setScreen(new AddWaypointScreen(this));
    }

    private void openEditScreen() {
        Waypoint target = selected;
        if (target != null) {
            this.minecraft.gui.setScreen(new EditWaypointScreen(this, target));
        }
    }

    private void toggleSelectedHidden() {
        Waypoint target = selected;
        if (target != null) {
            WaypointManager.toggleHidden(target);
            updateButtonStates();
        }
    }

    private void deleteSelected() {
        Waypoint target = selected;
        if (target != null) {
            int idx = dimensionWaypoints.indexOf(target);
            WaypointManager.remove(target);
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
        scrollOffset = Mth.clamp(scrollOffset, 0.0, maxScroll());
    }

    // ------------------------------------------------------------------------------------------
    // Rendering
    // ------------------------------------------------------------------------------------------

    @Override
    public void extractBackground(@NonNull GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
        // Subtle dark translucent full-screen overlay over the live game world (no blur or dirt background)
        GlassUi.drawScreenBackdrop(graphics, this.width, this.height);
    }

    @Override
    public void extractRenderState(@NonNull GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
        int centerX = this.width / 2;

        // 1. Centered Translucent Glass Main Panel + Top Header Bar
        GlassUi.drawMainPanel(graphics, panelLeft, panelTop, panelRight, panelBottom, HEADER_HEIGHT);

        // 2. Header Title & Dimension / Coordinates Pill Badges
        graphics.centeredText(this.font, this.title, centerX, panelTop + 6, 0xFFFFFFFF);
        renderHeaderPills(graphics, centerX, panelTop + 21);

        // 3. Recessed Scrollable Waypoint List Viewport (0x880B0C10)
        GlassUi.drawSubPanel(graphics, listLeft, listTop, listRight, listBottom);

        if (dimensionWaypoints.isEmpty()) {
            Component emptyText = Component.translatable("simplelocationmarking.screen.list.empty");
            graphics.centeredText(
                    this.font,
                    emptyText,
                    centerX,
                    (listTop + listBottom) / 2 - 4,
                    0xFF8892A8
            );
        } else {
            renderRows(graphics, mouseX, mouseY);
            renderScrollbar(graphics, mouseX, mouseY);
        }

        // 4. Action Buttons
        super.extractRenderState(graphics, mouseX, mouseY, a);
    }

    private void renderHeaderPills(@NonNull GuiGraphicsExtractor graphics, int centerX, int pillTop) {
        Component dimName = DimensionNames.of(currentDimensionId);
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

        LocalPlayer player = this.minecraft.player;
        BlockPos pos = (player != null) ? player.blockPosition() : BlockPos.ZERO;
        String coordsStr = "X: " + pos.getX() + "  Y: " + pos.getY() + "  Z: " + pos.getZ();

        int pillHeight = 15;
        int dimWidth = this.font.width(dimStr) + 18;
        int coordsWidth = this.font.width(coordsStr) + 14;
        int gap = 8;
        int totalWidth = dimWidth + gap + coordsWidth;
        int startX = centerX - totalWidth / 2;

        // Dimension pill box with glowing status dot
        int dimRight = startX + dimWidth;
        GlassUi.drawPill(graphics, startX, pillTop, dimRight, pillTop + pillHeight, dimFill, dimBorder);
        int dotX = startX + 5;
        int dotY = pillTop + 5;
        graphics.fill(dotX - 1, dotY - 1, dotX + 6, dotY + 6, 0x44000000 | (dimBorder & 0xFFFFFF));
        graphics.fill(dotX, dotY, dotX + 5, dotY + 5, dimBorder);
        graphics.text(this.font, dimStr, startX + 13, pillTop + 4, dimText);

        // Coordinates pill box
        int coordsLeft = dimRight + gap;
        int coordsRight = coordsLeft + coordsWidth;
        GlassUi.drawPill(graphics, coordsLeft, pillTop, coordsRight, pillTop + pillHeight, 0x90131824, 0xFF384462);
        graphics.text(this.font, coordsStr, coordsLeft + 7, pillTop + 4, 0xFFDCE6FF);
    }

    private void renderRows(@NonNull GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        LocalPlayer player = this.minecraft.player;
        double px = player != null ? player.getX() : 0.0;
        double py = player != null ? player.getY() : 0.0;
        double pz = player != null ? player.getZ() : 0.0;

        int innerLeft = listLeft + ROW_PADDING;
        int innerRight = listRight - ROW_PADDING - (maxScroll() > 0 ? SCROLLBAR_WIDTH + 5 : 0);
        int innerTop = listTop + ROW_PADDING;
        int innerBottom = listBottom - ROW_PADDING;

        graphics.enableScissor(listLeft + 1, listTop + 1, listRight - 1, listBottom - 1);

        String hiddenTag = Component.translatable("simplelocationmarking.waypoint.hidden_tag").getString();
        int hiddenTagW = this.font.width(hiddenTag);

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
                graphics.fill(innerLeft, rowTop, innerRight, rowBottom, GlassUi.ROW_SELECTED_BG);
                GlassUi.drawBorder(graphics, innerLeft, rowTop, innerRight, rowBottom, GlassUi.ROW_SELECTED_BORDER);
            } else if (isHovered) {
                graphics.fill(innerLeft, rowTop, innerRight, rowBottom, GlassUi.ROW_HOVER_BG);
                GlassUi.drawBorder(graphics, innerLeft, rowTop, innerRight, rowBottom, GlassUi.ROW_HOVER_BORDER);
            } else {
                graphics.fill(innerLeft, rowTop, innerRight, rowBottom, GlassUi.ROW_NORMAL_BG);
                GlassUi.drawBorder(graphics, innerLeft, rowTop, innerRight, rowBottom, GlassUi.ROW_NORMAL_BORDER);
            }

            int cardHeight = rowBottom - rowTop;
            int textY = rowTop + (cardHeight - 8) / 2;

            // 1. Glowing colored square swatch [11x11] with 1px white/dark border
            int swatchSize = 11;
            int swatchX = innerLeft + 6;
            int swatchY = rowTop + (cardHeight - swatchSize) / 2;
            GlassUi.drawGlowingSwatch(graphics, swatchX, swatchY, swatchSize, wp.color);

            // 2. Right-aligned distance badge (m / km)
            double dist = wp.distanceTo(px, py, pz);
            String distStr = wp.distanceText(dist);
            int distTextW = wp.distanceWidth(this.font);
            int distBadgeW = distTextW + 10;
            int distRight = innerRight - 5;
            int distLeft = distRight - distBadgeW;
            int badgeTop = rowTop + 3;
            int badgeBottom = rowBottom - 3;
            GlassUi.drawPill(graphics, distLeft, badgeTop, distRight, badgeBottom, 0x880C0E15, 0xFF30384E);
            graphics.text(this.font, distStr, distLeft + 5, textY, 0xFFDCE6FF);

            int rightLimit = distLeft - 6;

            // 3. Distinct amber/red [Hidden] ([Đã ẩn]) tag when hidden
            if (wp.hidden) {
                int tagBadgeW = hiddenTagW + 8;
                int tagRight = rightLimit;
                int tagLeft = tagRight - tagBadgeW;
                GlassUi.drawPill(graphics, tagLeft, badgeTop, tagRight, badgeBottom, 0x55FF4838, 0xBBFF6E58);
                graphics.text(this.font, hiddenTag, tagLeft + 4, textY, 0xFFFFAE70);
                rightLimit = tagLeft - 6;
            }

            // 4. Waypoint name & muted gray coordinates (X, Y, Z)
            String displayName = wp.displayName();
            if (displayName.isEmpty()) {
                displayName = "?";
            }
            String coordsText = "(" + wp.x + ", " + wp.y + ", " + wp.z + ")";
            int coordsW = this.font.width(coordsText);

            int nameX = swatchX + swatchSize + 7;
            int availableForNameAndCoords = Math.max(20, rightLimit - nameX);
            int maxNameW = Math.max(24, availableForNameAndCoords - coordsW - 8);

            String fittedName = this.font.plainSubstrByWidth(displayName, maxNameW);
            if (fittedName.length() < displayName.length() && fittedName.length() > 2) {
                fittedName = this.font.plainSubstrByWidth(displayName, maxNameW - 8) + "..";
            }

            int nameColor = wp.hidden ? 0xFF9AA2B4 : 0xFFFFFFFF;
            graphics.text(this.font, fittedName, nameX, textY, nameColor);

            int coordsX = nameX + this.font.width(fittedName) + 6;
            if (coordsX + coordsW <= rightLimit) {
                int coordsColor = wp.hidden ? 0xFF6E7586 : 0xFF9098AE;
                graphics.text(this.font, coordsText, coordsX, textY, coordsColor);
            }
        }

        graphics.disableScissor();
    }

    private void renderScrollbar(@NonNull GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
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
        graphics.fill(trackLeft, trackTop, trackRight, trackBottom, 0x5008090D);

        int thumbHeight = Math.max(18, (int) ((double) viewHeight() / contentHeight() * trackHeight));
        int thumbTop = trackTop + (int) ((scrollOffset / max) * (trackHeight - thumbHeight));
        int thumbBottom = thumbTop + thumbHeight;

        boolean hovered = draggingScrollbar || (mouseX >= trackLeft && mouseX <= trackRight && mouseY >= thumbTop && mouseY <= thumbBottom);
        int thumbColor = hovered ? 0xD0A8B8DC : 0x907884A0;
        graphics.fill(trackLeft, thumbTop, trackRight, thumbBottom, thumbColor);
    }

    // ------------------------------------------------------------------------------------------
    // Input handling
    // ------------------------------------------------------------------------------------------

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (maxScroll() > 0 && mouseX >= listLeft && mouseX <= listRight && mouseY >= listTop && mouseY <= listBottom) {
            scrollOffset -= scrollY * (ROW_HEIGHT * 1.25);
            clampScroll();
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    @Override
    public boolean mouseClicked(@NonNull MouseButtonEvent event, boolean doubled) {
        if (super.mouseClicked(event, doubled)) {
            return true;
        }
        if (event.button() != 0) {
            return false;
        }

        double mouseX = event.x();
        double mouseY = event.y();

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
                this.minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0f));
                if (isDoubleClick) {
                    openEditScreen();
                }
                return true;
            }
        }

        return false;
    }

    @Override
    public boolean mouseDragged(@NonNull MouseButtonEvent event, double deltaX, double deltaY) {
        if (draggingScrollbar && event.button() == 0) {
            updateScrollFromMouse(event.y());
            return true;
        }
        return super.mouseDragged(event, deltaX, deltaY);
    }

    @Override
    public boolean mouseReleased(@NonNull MouseButtonEvent event) {
        if (event.button() == 0 && draggingScrollbar) {
            draggingScrollbar = false;
            return true;
        }
        return super.mouseReleased(event);
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
        scrollOffset = Mth.clamp(ratio * max, 0.0, max);
    }

    @Override
    public boolean keyPressed(@NonNull KeyEvent event) {
        int keyCode = event.key();
        if (keyCode == InputConstants.KEY_DELETE && selected != null) {
            deleteSelected();
            return true;
        }
        if ((keyCode == InputConstants.KEY_UP || keyCode == InputConstants.KEY_DOWN) && !dimensionWaypoints.isEmpty()) {
            int idx = selected != null ? dimensionWaypoints.indexOf(selected) : -1;
            if (keyCode == InputConstants.KEY_UP) {
                idx = idx <= 0 ? 0 : idx - 1;
            } else {
                idx = idx < 0 ? 0 : Math.min(dimensionWaypoints.size() - 1, idx + 1);
            }
            selected = dimensionWaypoints.get(idx);
            ensureVisible(idx);
            updateButtonStates();
            return true;
        }
        return super.keyPressed(event);
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
    public boolean isPauseScreen() {
        return false;
    }
}
