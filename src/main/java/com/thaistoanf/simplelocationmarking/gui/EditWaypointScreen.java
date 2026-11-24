package com.thaistoanf.simplelocationmarking.gui;

import com.thaistoanf.simplelocationmarking.gui.widget.HexColorPalette;
import com.thaistoanf.simplelocationmarking.waypoint.Waypoint;
import com.thaistoanf.simplelocationmarking.waypoint.WaypointManager;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import org.lwjgl.glfw.GLFW;

import java.util.Locale;
import java.util.function.Consumer;

/**
 * Screen for editing an existing waypoint.
 * <p>
 * Styled with the Translucent Dark Glass theme, placing the editable Name field, editable integer
 * {@code X/Y/Z} fields, and the MS Paint-style Hexagon Color Palette + Live Marker Preview Box
 * inside well-spaced translucent sub-panels.
 */
public final class EditWaypointScreen extends Screen {
    private static final int HEADER_HEIGHT = 24;

    private final LocationListScreen parent;
    private final Waypoint waypoint;
    private final HexColorPalette palette = new HexColorPalette();

    private String draftName;
    private String draftX;
    private String draftY;
    private String draftZ;

    private EditBox nameField;
    private EditBox xField;
    private EditBox yField;
    private EditBox zField;
    private Button submitButton;

    private int panelLeft;
    private int panelTop;
    private int panelRight;
    private int panelBottom;

    private int subLeft;
    private int subRight;
    private int namePanelTop;
    private int namePanelBottom;
    private int coordsPanelTop;
    private int coordsPanelBottom;
    private int colorPanelTop;
    private int colorPanelBottom;

    private int previewLeft;
    private int previewTop;
    private int previewRight;
    private int previewBottom;

    private int coordsFieldY;

    public EditWaypointScreen(LocationListScreen parent, Waypoint waypoint) {
        super(Component.translatable("simplelocationmarking.screen.edit.title"));
        this.parent = parent;
        this.waypoint = waypoint;
        this.draftName = waypoint.name != null ? waypoint.name : "";
        this.draftX = Integer.toString(waypoint.x);
        this.draftY = Integer.toString(waypoint.y);
        this.draftZ = Integer.toString(waypoint.z);
        this.palette.setSelectedColor(waypoint.color);
    }

    @Override
    protected void init() {
        super.init();

        int panelWidth = Math.min(340, this.width - 16);
        int panelHeight = Math.min(264, this.height - 12);
        panelLeft = (this.width - panelWidth) / 2;
        panelRight = panelLeft + panelWidth;
        panelTop = Math.max(6, (this.height - panelHeight) / 2);
        panelBottom = panelTop + panelHeight;

        subLeft = panelLeft + 10;
        subRight = panelRight - 10;
        int innerLeft = subLeft + 8;
        int innerRight = subRight - 8;
        int innerWidth = innerRight - innerLeft;

        // 1. Editable Name sub-panel
        namePanelTop = panelTop + HEADER_HEIGHT + 5;
        namePanelBottom = namePanelTop + 36;
        int nameFieldY = namePanelTop + 15;
        nameField = new EditBox(
                this.font,
                innerLeft,
                nameFieldY,
                innerWidth,
                16,
                Component.translatable("simplelocationmarking.field.name")
        );
        nameField.setMaxLength(48);
        nameField.setEditable(true);
        nameField.setValue(draftName);
        nameField.setHint(Component.translatable("simplelocationmarking.field.name.placeholder"));
        nameField.setResponder(text -> {
            draftName = text;
            updateSubmitButtonState();
        });
        addRenderableWidget(nameField);

        // 2. Editable X, Y, Z integer coordinate sub-panel
        coordsPanelTop = namePanelBottom + 4;
        coordsPanelBottom = coordsPanelTop + 36;
        coordsFieldY = coordsPanelTop + 15;

        int coordGap = 8;
        int labelPad = 12;
        int singleWidth = (innerWidth - coordGap * 2 - labelPad * 3) / 3;

        int xFieldX = innerLeft + labelPad;
        xField = createEditableCoordField(xFieldX, coordsFieldY, singleWidth, draftX, "simplelocationmarking.field.x", text -> {
            draftX = text;
            updateSubmitButtonState();
        });

        int yFieldX = xFieldX + singleWidth + coordGap + labelPad;
        yField = createEditableCoordField(yFieldX, coordsFieldY, singleWidth, draftY, "simplelocationmarking.field.y", text -> {
            draftY = text;
            updateSubmitButtonState();
        });

        int zFieldX = yFieldX + singleWidth + coordGap + labelPad;
        int zWidth = innerRight - zFieldX;
        zField = createEditableCoordField(zFieldX, coordsFieldY, zWidth, draftZ, "simplelocationmarking.field.z", text -> {
            draftZ = text;
            updateSubmitButtonState();
        });

        addRenderableWidget(xField);
        addRenderableWidget(yField);
        addRenderableWidget(zField);

        // 3. Hexagon Color Palette & Live Preview sub-panel
        int buttonY = panelBottom - 25;
        colorPanelTop = coordsPanelBottom + 4;
        colorPanelBottom = buttonY - 5;

        int contentTop = colorPanelTop + 16;
        int contentBottom = colorPanelBottom - 5;
        int availableHeight = Math.max(54, contentBottom - contentTop);

        int previewWidth = Math.min(118, Math.max(92, innerWidth / 3 + 12));
        previewRight = innerRight;
        previewLeft = previewRight - previewWidth;
        previewTop = contentTop;
        previewBottom = contentBottom;

        int paletteAreaWidth = Math.max(70, (previewLeft - 8) - innerLeft);
        float maxRadiusH = availableHeight / HexColorPalette.UNIT_HEIGHT;
        float maxRadiusW = paletteAreaWidth / HexColorPalette.UNIT_WIDTH;
        float hexRadius = Mth.clamp(Math.min(maxRadiusH, maxRadiusW), 2.8f, 5.6f);
        palette.setLayout(0, 0, hexRadius);
        int paletteX = innerLeft + (paletteAreaWidth - palette.getWidth()) / 2;
        int paletteY = contentTop + (availableHeight - palette.getHeight()) / 2;
        palette.setLayout(paletteX, paletteY, hexRadius);

        // 4. Bottom buttons: Submit & Cancel
        int btnGap = 8;
        int btnWidth = ((subRight - subLeft) - btnGap) / 2;
        submitButton = addRenderableWidget(Button.builder(
                        Component.translatable("simplelocationmarking.button.submit"),
                        btn -> submitAndClose())
                .bounds(subLeft, buttonY, btnWidth, 20)
                .build());

        addRenderableWidget(Button.builder(
                        Component.translatable("simplelocationmarking.button.cancel"),
                        btn -> onClose())
                .bounds(subLeft + btnWidth + btnGap, buttonY, (subRight - subLeft) - btnWidth - btnGap, 20)
                .build());

        setInitialFocus(nameField);
        updateSubmitButtonState();
    }

    private EditBox createEditableCoordField(int x, int y, int width, String initial,
                                             String translationKey, Consumer<String> listener) {
        EditBox field = new IntEditBox(this.font, x, y, width, 16, Component.translatable(translationKey));
        field.setMaxLength(11);
        field.setEditable(true);
        field.setValue(initial);
        field.setResponder(listener);
        return field;
    }

    /**
     * EditBox that restricts typed or pasted input to optional leading minus and up to 9 digits.
     */
    private static final class IntEditBox extends EditBox {
        private IntEditBox(Font font, int x, int y, int width, int height, Component message) {
            super(font, x, y, width, height, message);
        }

        @Override
        public void insertText(String text) {
            if (text == null || text.isEmpty()) {
                return;
            }
            for (int i = 0; i < text.length(); i++) {
                char c = text.charAt(i);
                if ((c < '0' || c > '9') && c != '-') {
                    return;
                }
            }
            String before = getValue();
            int beforeCursor = getCursorPosition();
            super.insertText(text);
            if (!isPartialInteger(getValue())) {
                setValue(before);
                moveCursorTo(beforeCursor, false);
            }
        }
    }

    private static boolean isPartialInteger(String s) {
        if (s == null || s.isEmpty() || "-".equals(s)) {
            return true;
        }
        int start = s.charAt(0) == '-' ? 1 : 0;
        if (s.length() - start > 9) {
            return false;
        }
        for (int i = start; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c < '0' || c > '9') {
                return false;
            }
        }
        return true;
    }

    private static Integer tryParseInt(String s) {
        if (s == null) {
            return null;
        }
        String trimmed = s.trim();
        if (trimmed.isEmpty() || "-".equals(trimmed)) {
            return null;
        }
        try {
            return Integer.parseInt(trimmed);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private void updateSubmitButtonState() {
        if (submitButton == null) {
            return;
        }
        boolean validName = nameField != null && !nameField.getValue().trim().isEmpty();
        boolean validX = xField != null && tryParseInt(xField.getValue()) != null;
        boolean validY = yField != null && tryParseInt(yField.getValue()) != null;
        boolean validZ = zField != null && tryParseInt(zField.getValue()) != null;
        submitButton.active = validName && validX && validY && validZ;
    }

    private void submitAndClose() {
        if (nameField == null || xField == null || yField == null || zField == null) {
            return;
        }
        String trimmedName = nameField.getValue().trim();
        Integer parsedX = tryParseInt(xField.getValue());
        Integer parsedY = tryParseInt(yField.getValue());
        Integer parsedZ = tryParseInt(zField.getValue());
        if (trimmedName.isEmpty() || parsedX == null || parsedY == null || parsedZ == null) {
            return;
        }

        waypoint.name = trimmedName;
        waypoint.x = parsedX;
        waypoint.y = parsedY;
        waypoint.z = parsedZ;
        waypoint.color = palette.getSelectedColor() & 0xFFFFFF;

        WaypointManager.markChanged();
        parent.reloadWaypoints();
        parent.setSelected(waypoint);
        onClose();
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
        GlassUi.drawScreenBackdrop(graphics, this.width, this.height);
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
        int centerX = this.width / 2;
        int innerLeft = subLeft + 8;

        // 1. Main Translucent Glass Panel + Header Bar
        GlassUi.drawMainPanel(graphics, panelLeft, panelTop, panelRight, panelBottom, HEADER_HEIGHT);
        graphics.centeredText(this.font, this.title, centerX, panelTop + 8, 0xFFFFFFFF);

        // 2. Name Sub-Panel
        GlassUi.drawSubPanel(graphics, subLeft, namePanelTop, subRight, namePanelBottom);
        graphics.text(
                this.font,
                Component.translatable("simplelocationmarking.field.name"),
                innerLeft,
                namePanelTop + 4,
                0xFFD0D8EC
        );

        // 3. Coordinates Sub-Panel
        GlassUi.drawSubPanel(graphics, subLeft, coordsPanelTop, subRight, coordsPanelBottom);
        graphics.text(
                this.font,
                Component.translatable("simplelocationmarking.field.coordinates"),
                innerLeft,
                coordsPanelTop + 4,
                0xFFD0D8EC
        );
        int prefixY = coordsFieldY + 4;
        graphics.text(this.font, Component.translatable("simplelocationmarking.field.x"), xField.getX() - 10, prefixY, 0xFFFF8080);
        graphics.text(this.font, Component.translatable("simplelocationmarking.field.y"), yField.getX() - 10, prefixY, 0xFF80FF98);
        graphics.text(this.font, Component.translatable("simplelocationmarking.field.z"), zField.getX() - 10, prefixY, 0xFF80B8FF);

        // 4. Color Palette & Live Preview Sub-Panel
        GlassUi.drawSubPanel(graphics, subLeft, colorPanelTop, subRight, colorPanelBottom);
        graphics.text(
                this.font,
                Component.translatable("simplelocationmarking.field.color"),
                innerLeft,
                colorPanelTop + 5,
                0xFFD0D8EC
        );

        // Hexagon honeycomb palette
        palette.render(graphics, mouseX, mouseY);

        // Live Color & Marker Preview Box on the right side of the color sub-panel
        renderPreviewBox(graphics);

        super.extractRenderState(graphics, mouseX, mouseY, a);
    }

    private void renderPreviewBox(GuiGraphicsExtractor graphics) {
        int selectedRgb = palette.getSelectedColor() & 0xFFFFFF;
        GlassUi.drawPill(graphics, previewLeft, previewTop, previewRight, previewBottom, 0xB010131C, 0xFF2E364C);

        int pCenterX = (previewLeft + previewRight) / 2;
        Component previewLabel = Component.translatable("simplelocationmarking.field.preview");
        graphics.centeredText(this.font, previewLabel, pCenterX, previewTop + 4, 0xFFB8C4DC);

        // Live HUD Marker preview (compact badge + expanded hover pill + distance pill)
        String previewName = (nameField != null) ? nameField.getValue() : draftName;
        GlassUi.drawLiveMarkerPreview(
                graphics,
                this.font,
                previewName,
                selectedRgb,
                previewLeft,
                previewTop + 13,
                previewRight,
                previewBottom - 16
        );

        // Glowing swatch + #RRGGBB code at the bottom of the preview box
        String hexCode = String.format(Locale.ROOT, "#%06X", selectedRgb);
        int hexW = this.font.width(hexCode);
        int swatchSize = 9;
        int totalW = swatchSize + 5 + hexW;
        int startX = pCenterX - totalW / 2;
        int bottomY = previewBottom - 13;
        GlassUi.drawGlowingSwatch(graphics, startX, bottomY, swatchSize, selectedRgb);
        graphics.text(this.font, hexCode, startX + swatchSize + 5, bottomY + 1, 0xFFE4ECFF);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubled) {
        if (palette.mouseClicked(event.x(), event.y(), event.button())) {
            if (minecraft != null) {
                minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.2f));
            }
            return true;
        }
        return super.mouseClicked(event, doubled);
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        int keyCode = event.key();
        if ((keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_KP_ENTER) && submitButton != null && submitButton.active) {
            submitAndClose();
            return true;
        }
        return super.keyPressed(event);
    }

    @Override
    public void onClose() {
        if (minecraft != null) {
            minecraft.setScreen(parent);
        }
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
