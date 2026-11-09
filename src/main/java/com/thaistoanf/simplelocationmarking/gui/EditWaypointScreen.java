package com.thaistoanf.simplelocationmarking.gui;

import com.thaistoanf.simplelocationmarking.gui.widget.HexColorPalette;
import com.thaistoanf.simplelocationmarking.waypoint.Waypoint;
import com.thaistoanf.simplelocationmarking.waypoint.WaypointManager;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.input.KeyInput;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;
import org.lwjgl.glfw.GLFW;

import java.util.Locale;

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

    private TextFieldWidget nameField;
    private TextFieldWidget xField;
    private TextFieldWidget yField;
    private TextFieldWidget zField;
    private ButtonWidget submitButton;

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
        super(Text.translatable("simplelocationmarking.screen.edit.title"));
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
        nameField = new TextFieldWidget(
                this.textRenderer,
                innerLeft,
                nameFieldY,
                innerWidth,
                16,
                Text.translatable("simplelocationmarking.field.name")
        );
        nameField.setMaxLength(48);
        nameField.setEditable(true);
        nameField.setText(draftName);
        nameField.setPlaceholder(Text.translatable("simplelocationmarking.field.name.placeholder"));
        nameField.setChangedListener(text -> {
            draftName = text;
            updateSubmitButtonState();
        });
        addDrawableChild(nameField);

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

        addDrawableChild(xField);
        addDrawableChild(yField);
        addDrawableChild(zField);

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
        float hexRadius = MathHelper.clamp(Math.min(maxRadiusH, maxRadiusW), 2.8f, 5.6f);
        palette.setLayout(0, 0, hexRadius);
        int paletteX = innerLeft + (paletteAreaWidth - palette.getWidth()) / 2;
        int paletteY = contentTop + (availableHeight - palette.getHeight()) / 2;
        palette.setLayout(paletteX, paletteY, hexRadius);

        // 4. Bottom buttons: Submit & Cancel
        int btnGap = 8;
        int btnWidth = ((subRight - subLeft) - btnGap) / 2;
        submitButton = addDrawableChild(ButtonWidget.builder(
                        Text.translatable("simplelocationmarking.button.submit"),
                        btn -> submitAndClose())
                .dimensions(subLeft, buttonY, btnWidth, 20)
                .build());

        addDrawableChild(ButtonWidget.builder(
                        Text.translatable("simplelocationmarking.button.cancel"),
                        btn -> close())
                .dimensions(subLeft + btnWidth + btnGap, buttonY, (subRight - subLeft) - btnWidth - btnGap, 20)
                .build());

        setInitialFocus(nameField);
        updateSubmitButtonState();
    }

    private TextFieldWidget createEditableCoordField(int x, int y, int width, String initial,
                                                     String translationKey, java.util.function.Consumer<String> listener) {
        TextFieldWidget field = new TextFieldWidget(this.textRenderer, x, y, width, 16, Text.translatable(translationKey));
        field.setMaxLength(11);
        field.setEditable(true);
        field.setTextPredicate(EditWaypointScreen::isPartialInteger);
        field.setText(initial);
        field.setChangedListener(listener);
        return field;
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
        boolean validName = nameField != null && !nameField.getText().trim().isEmpty();
        boolean validX = xField != null && tryParseInt(xField.getText()) != null;
        boolean validY = yField != null && tryParseInt(yField.getText()) != null;
        boolean validZ = zField != null && tryParseInt(zField.getText()) != null;
        submitButton.active = validName && validX && validY && validZ;
    }

    private void submitAndClose() {
        if (nameField == null || xField == null || yField == null || zField == null) {
            return;
        }
        String trimmedName = nameField.getText().trim();
        Integer parsedX = tryParseInt(xField.getText());
        Integer parsedY = tryParseInt(yField.getText());
        Integer parsedZ = tryParseInt(zField.getText());
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
        close();
    }

    @Override
    public void renderBackground(DrawContext context, int mouseX, int mouseY, float delta) {
        GlassUi.drawScreenBackdrop(context, this.width, this.height);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        int centerX = this.width / 2;
        int innerLeft = subLeft + 8;

        // 1. Main Translucent Glass Panel + Header Bar
        GlassUi.drawMainPanel(context, panelLeft, panelTop, panelRight, panelBottom, HEADER_HEIGHT);
        context.drawCenteredTextWithShadow(this.textRenderer, this.title, centerX, panelTop + 8, 0xFFFFFFFF);

        // 2. Name Sub-Panel
        GlassUi.drawSubPanel(context, subLeft, namePanelTop, subRight, namePanelBottom);
        context.drawTextWithShadow(
                this.textRenderer,
                Text.translatable("simplelocationmarking.field.name"),
                innerLeft,
                namePanelTop + 4,
                0xFFD0D8EC
        );

        // 3. Coordinates Sub-Panel
        GlassUi.drawSubPanel(context, subLeft, coordsPanelTop, subRight, coordsPanelBottom);
        context.drawTextWithShadow(
                this.textRenderer,
                Text.translatable("simplelocationmarking.field.coordinates"),
                innerLeft,
                coordsPanelTop + 4,
                0xFFD0D8EC
        );
        int prefixY = coordsFieldY + 4;
        context.drawTextWithShadow(this.textRenderer, Text.translatable("simplelocationmarking.field.x"), xField.getX() - 10, prefixY, 0xFFFF8080);
        context.drawTextWithShadow(this.textRenderer, Text.translatable("simplelocationmarking.field.y"), yField.getX() - 10, prefixY, 0xFF80FF98);
        context.drawTextWithShadow(this.textRenderer, Text.translatable("simplelocationmarking.field.z"), zField.getX() - 10, prefixY, 0xFF80B8FF);

        // 4. Color Palette & Live Preview Sub-Panel
        GlassUi.drawSubPanel(context, subLeft, colorPanelTop, subRight, colorPanelBottom);
        context.drawTextWithShadow(
                this.textRenderer,
                Text.translatable("simplelocationmarking.field.color"),
                innerLeft,
                colorPanelTop + 5,
                0xFFD0D8EC
        );

        // Hexagon honeycomb palette
        palette.render(context, mouseX, mouseY);

        // Live Color & Marker Preview Box on the right side of the color sub-panel
        renderPreviewBox(context);

        super.render(context, mouseX, mouseY, delta);
    }

    private void renderPreviewBox(DrawContext context) {
        int selectedRgb = palette.getSelectedColor() & 0xFFFFFF;
        GlassUi.drawPill(context, previewLeft, previewTop, previewRight, previewBottom, 0xB010131C, 0xFF2E364C);

        int pCenterX = (previewLeft + previewRight) / 2;
        Text previewLabel = Text.translatable("simplelocationmarking.field.preview");
        context.drawCenteredTextWithShadow(this.textRenderer, previewLabel, pCenterX, previewTop + 4, 0xFFB8C4DC);

        // Live HUD Marker preview (compact badge + expanded hover pill + distance pill)
        String previewName = (nameField != null) ? nameField.getText() : draftName;
        GlassUi.drawLiveMarkerPreview(
                context,
                this.textRenderer,
                previewName,
                selectedRgb,
                previewLeft,
                previewTop + 13,
                previewRight,
                previewBottom - 16
        );

        // Glowing swatch + #RRGGBB code at the bottom of the preview box
        String hexCode = String.format(Locale.ROOT, "#%06X", selectedRgb);
        int hexW = this.textRenderer.getWidth(hexCode);
        int swatchSize = 9;
        int totalW = swatchSize + 5 + hexW;
        int startX = pCenterX - totalW / 2;
        int bottomY = previewBottom - 13;
        GlassUi.drawGlowingSwatch(context, startX, bottomY, swatchSize, selectedRgb);
        context.drawTextWithShadow(this.textRenderer, hexCode, startX + swatchSize + 5, bottomY + 1, 0xFFE4ECFF);
    }

    @Override
    public boolean mouseClicked(Click click, boolean doubled) {
        if (palette.mouseClicked(click.x(), click.y(), click.button())) {
            if (client != null) {
                client.getSoundManager().play(PositionedSoundInstance.ui(SoundEvents.UI_BUTTON_CLICK, 1.2f));
            }
            return true;
        }
        return super.mouseClicked(click, doubled);
    }

    @Override
    public boolean keyPressed(KeyInput input) {
        int key = input.getKeycode();
        if ((key == GLFW.GLFW_KEY_ENTER || key == GLFW.GLFW_KEY_KP_ENTER) && submitButton != null && submitButton.active) {
            submitAndClose();
            return true;
        }
        return super.keyPressed(input);
    }

    @Override
    public void close() {
        if (client != null) {
            client.setScreen(parent);
        }
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}
