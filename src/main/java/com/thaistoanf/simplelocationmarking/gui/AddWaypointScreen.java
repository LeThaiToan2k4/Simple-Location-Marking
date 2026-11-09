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
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import org.lwjgl.glfw.GLFW;

import java.util.Locale;
import java.util.Random;

/**
 * Screen for creating a new waypoint at the player's current block coordinates.
 * <p>
 * Styled with the Translucent Dark Glass theme, placing the Name field, read-only {@code X/Y/Z}
 * fields, and the MS Paint-style Hexagon Color Palette + Live Marker Preview Box inside
 * well-spaced translucent sub-panels.
 */
public final class AddWaypointScreen extends Screen {
    private static final int HEADER_HEIGHT = 24;

    private final LocationListScreen parent;
    private final HexColorPalette palette = new HexColorPalette();

    private String draftName = "";
    private final int playerX;
    private final int playerY;
    private final int playerZ;
    private final String dimensionId;

    private TextFieldWidget nameField;
    private TextFieldWidget xField;
    private TextFieldWidget yField;
    private TextFieldWidget zField;
    private ButtonWidget addButton;

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

    public AddWaypointScreen(LocationListScreen parent) {
        super(Text.translatable("simplelocationmarking.screen.add.title"));
        this.parent = parent;
        this.palette.selectRandom(new Random());

        var mc = net.minecraft.client.MinecraftClient.getInstance();
        BlockPos pos = (mc.player != null) ? mc.player.getBlockPos() : BlockPos.ORIGIN;
        this.playerX = pos.getX();
        this.playerY = pos.getY();
        this.playerZ = pos.getZ();
        this.dimensionId = (mc.world != null) ? WaypointManager.dimensionId(mc.world) : "minecraft:overworld";
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

        // 1. Name sub-panel
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
        nameField.setText(draftName);
        nameField.setPlaceholder(Text.translatable("simplelocationmarking.field.name.placeholder"));
        nameField.setChangedListener(text -> {
            draftName = text;
            updateAddButtonState();
        });
        addDrawableChild(nameField);

        // 2. Read-only X, Y, Z coordinate sub-panel
        coordsPanelTop = namePanelBottom + 4;
        coordsPanelBottom = coordsPanelTop + 36;
        coordsFieldY = coordsPanelTop + 15;

        int coordGap = 8;
        int labelPad = 12;
        int singleWidth = (innerWidth - coordGap * 2 - labelPad * 3) / 3;

        int xFieldX = innerLeft + labelPad;
        xField = createReadOnlyCoordField(xFieldX, coordsFieldY, singleWidth, Integer.toString(playerX), "simplelocationmarking.field.x");

        int yFieldX = xFieldX + singleWidth + coordGap + labelPad;
        yField = createReadOnlyCoordField(yFieldX, coordsFieldY, singleWidth, Integer.toString(playerY), "simplelocationmarking.field.y");

        int zFieldX = yFieldX + singleWidth + coordGap + labelPad;
        int zWidth = innerRight - zFieldX;
        zField = createReadOnlyCoordField(zFieldX, coordsFieldY, zWidth, Integer.toString(playerZ), "simplelocationmarking.field.z");

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

        // 4. Bottom buttons: Add & Cancel
        int btnGap = 8;
        int btnWidth = ((subRight - subLeft) - btnGap) / 2;
        addButton = addDrawableChild(ButtonWidget.builder(
                        Text.translatable("simplelocationmarking.button.add"),
                        btn -> saveAndClose())
                .dimensions(subLeft, buttonY, btnWidth, 20)
                .build());

        addDrawableChild(ButtonWidget.builder(
                        Text.translatable("simplelocationmarking.button.cancel"),
                        btn -> close())
                .dimensions(subLeft + btnWidth + btnGap, buttonY, (subRight - subLeft) - btnWidth - btnGap, 20)
                .build());

        setInitialFocus(nameField);
        updateAddButtonState();
    }

    private TextFieldWidget createReadOnlyCoordField(int x, int y, int width, String value, String translationKey) {
        TextFieldWidget field = new TextFieldWidget(this.textRenderer, x, y, width, 16, Text.translatable(translationKey));
        field.setText(value);
        field.setEditable(false);
        field.setUneditableColor(0xFFA8B2C8);
        field.active = false;
        return field;
    }

    private void updateAddButtonState() {
        if (addButton != null) {
            addButton.active = nameField != null && !nameField.getText().trim().isEmpty();
        }
    }

    private void saveAndClose() {
        if (nameField == null) {
            return;
        }
        String trimmedName = nameField.getText().trim();
        if (trimmedName.isEmpty()) {
            return;
        }
        Waypoint created = new Waypoint(
                trimmedName,
                playerX,
                playerY,
                playerZ,
                palette.getSelectedColor(),
                dimensionId
        );
        WaypointManager.add(created);
        parent.reloadWaypoints();
        parent.setSelected(created);
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
                Text.translatable("simplelocationmarking.field.coordinates_readonly"),
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
        if ((key == GLFW.GLFW_KEY_ENTER || key == GLFW.GLFW_KEY_KP_ENTER) && addButton != null && addButton.active) {
            saveAndClose();
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
