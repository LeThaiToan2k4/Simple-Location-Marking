package com.thaistoanf.simplelocationmarking.gui.widget;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.math.MathHelper;
import org.joml.Matrix3x2fStack;

import java.util.Random;

/**
 * An MS Paint / Office style honeycomb color picker.
 * <p>
 * The palette is made of concentric rings of pointy-top hexagons around a white center cell.
 * The hue follows the angle around the center, saturation grows towards the outside and the
 * outermost ring is darker. A grayscale row from white to black sits below the honeycomb.
 * <p>
 * Hexagons are drawn at physical pixel resolution (the GUI matrix is scaled by the inverse of
 * the GUI scale) so the cells stay crisp at every GUI scale.
 */
public final class HexColorPalette {
    private static final int RINGS = 7;
    private static final int GRAY_COUNT = RINGS * 2 + 1;
    private static final float SQRT3 = 1.7320508f;
    private static final float GRAY_ROW_GAP = 0.6f;

    /** Saturation and value per ring index (0 = center). */
    private static final float[] RING_SATURATION = {0.0f, 0.15f, 0.30f, 0.47f, 0.65f, 0.83f, 1.0f, 1.0f};
    private static final float[] RING_VALUE = {1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 0.68f};

    /** Width of the palette in hexagon-radius units. */
    public static final float UNIT_WIDTH = 2.0f * SQRT3 * (RINGS + 0.5f);
    /** Distance from the palette top to the honeycomb center, in hexagon-radius units. */
    private static final float UNIT_CENTER_Y = 1.5f * RINGS + 1.0f;
    /** Vertical center of the grayscale row relative to the honeycomb center. */
    private static final float GRAY_ROW_Y = 1.5f * RINGS + 2.0f + GRAY_ROW_GAP;
    /** Height of the palette in hexagon-radius units. */
    public static final float UNIT_HEIGHT = UNIT_CENTER_Y + GRAY_ROW_Y + 1.0f;

    private final float[] cellX;
    private final float[] cellY;
    private final int[] cellColor;
    private final int honeycombCount;

    private int x;
    private int y;
    private float size = 6.0f;

    private int selectedIndex = -1;
    private int selectedColor = 0xFF0000;
    private int hoveredIndex = -1;

    public HexColorPalette() {
        int honeycomb = 1 + 3 * RINGS * (RINGS + 1);
        int total = honeycomb + GRAY_COUNT;
        cellX = new float[total];
        cellY = new float[total];
        cellColor = new int[total];
        honeycombCount = honeycomb;

        int index = 0;
        for (int r = -RINGS; r <= RINGS; r++) {
            for (int q = -RINGS; q <= RINGS; q++) {
                int ring = Math.max(Math.abs(q), Math.max(Math.abs(r), Math.abs(q + r)));
                if (ring > RINGS) {
                    continue;
                }
                float cx = SQRT3 * (q + r * 0.5f);
                float cy = 1.5f * r;
                cellX[index] = cx;
                cellY[index] = cy;
                if (ring == 0) {
                    cellColor[index] = 0xFFFFFF;
                } else {
                    // Hue from the angle around the center, red at the top, going clockwise.
                    float angle = (float) Math.toDegrees(Math.atan2(cy, cx)) + 90.0f;
                    float hue = ((angle % 360.0f) + 360.0f) % 360.0f / 360.0f;
                    cellColor[index] = MathHelper.hsvToRgb(hue, RING_SATURATION[ring], RING_VALUE[ring]) & 0xFFFFFF;
                }
                index++;
            }
        }
        for (int i = 0; i < GRAY_COUNT; i++) {
            cellX[index] = SQRT3 * (i - (GRAY_COUNT - 1) * 0.5f);
            cellY[index] = GRAY_ROW_Y;
            int level = Math.round(255.0f * (1.0f - i / (float) (GRAY_COUNT - 1)));
            cellColor[index] = (level << 16) | (level << 8) | level;
            index++;
        }
    }

    // ------------------------------------------------------------------------------------------
    // Layout
    // ------------------------------------------------------------------------------------------

    /** Positions the palette with its top-left corner at (x, y) and the given hexagon radius. */
    public void setLayout(int x, int y, float hexRadius) {
        this.x = x;
        this.y = y;
        this.size = hexRadius;
    }

    public int getWidth() {
        return MathHelper.ceil(UNIT_WIDTH * size);
    }

    public int getHeight() {
        return MathHelper.ceil(UNIT_HEIGHT * size);
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    // ------------------------------------------------------------------------------------------
    // Selection
    // ------------------------------------------------------------------------------------------

    public int getSelectedColor() {
        return selectedColor;
    }

    /** Selects the given color and highlights the matching cell if the palette contains it. */
    public void setSelectedColor(int rgb) {
        selectedColor = rgb & 0xFFFFFF;
        selectedIndex = -1;
        for (int i = 0; i < cellColor.length; i++) {
            if (cellColor[i] == selectedColor) {
                selectedIndex = i;
                break;
            }
        }
    }

    /** Selects a random colored cell of the honeycomb (skipping the near-white center rings). */
    public void selectRandom(Random random) {
        int firstColored = 1 + 3 * 2; // skip ring 0 and ring 1
        int index = firstColored + random.nextInt(honeycombCount - firstColored);
        // Indices are not ordered by ring, so retry until a saturated enough cell is found.
        for (int attempt = 0; attempt < 32 && ringOf(index) < 2; attempt++) {
            index = random.nextInt(honeycombCount);
        }
        selectedIndex = index;
        selectedColor = cellColor[index];
    }

    private int ringOf(int index) {
        float cx = cellX[index] / SQRT3;
        float r = cellY[index] / 1.5f;
        int ri = Math.round(r);
        int qi = Math.round(cx - r * 0.5f);
        return Math.max(Math.abs(qi), Math.max(Math.abs(ri), Math.abs(qi + ri)));
    }

    // ------------------------------------------------------------------------------------------
    // Input
    // ------------------------------------------------------------------------------------------

    private float centerX() {
        return x + UNIT_WIDTH * 0.5f * size;
    }

    private float centerY() {
        return y + UNIT_CENTER_Y * size;
    }

    /** Returns the index of the cell under the given GUI position, or -1. */
    public int cellAt(double mouseX, double mouseY) {
        if (mouseX < x || mouseY < y || mouseX > x + getWidth() || mouseY > y + getHeight()) {
            return -1;
        }
        float ox = centerX();
        float oy = centerY();
        float maxDistance = size * 0.92f;
        float best = maxDistance * maxDistance;
        int bestIndex = -1;
        for (int i = 0; i < cellX.length; i++) {
            float dx = (float) mouseX - (ox + cellX[i] * size);
            float dy = (float) mouseY - (oy + cellY[i] * size);
            float d = dx * dx + dy * dy;
            if (d < best) {
                best = d;
                bestIndex = i;
            }
        }
        return bestIndex;
    }

    /** Handles a left click. Returns {@code true} when a cell was picked. */
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button != 0) {
            return false;
        }
        int index = cellAt(mouseX, mouseY);
        if (index < 0) {
            return false;
        }
        selectedIndex = index;
        selectedColor = cellColor[index];
        return true;
    }

    public boolean isMouseOver(double mouseX, double mouseY) {
        return cellAt(mouseX, mouseY) >= 0;
    }

    // ------------------------------------------------------------------------------------------
    // Rendering
    // ------------------------------------------------------------------------------------------

    public void render(DrawContext context, int mouseX, int mouseY) {
        hoveredIndex = cellAt(mouseX, mouseY);

        int scale = Math.max(1, MinecraftClient.getInstance().getWindow().getScaleFactor());
        float f = scale;

        Matrix3x2fStack matrices = context.getMatrices();
        matrices.pushMatrix();
        matrices.scale(1.0f / f, 1.0f / f);

        float ox = centerX() * f;
        float oy = centerY() * f;
        float radius = size * f;
        float gap = Math.max(1.0f, f * 0.5f);
        float cellRadius = radius - gap;

        for (int i = 0; i < cellX.length; i++) {
            if (i == selectedIndex || i == hoveredIndex) {
                continue;
            }
            fillHex(context, ox + cellX[i] * radius, oy + cellY[i] * radius, cellRadius, 0xFF000000 | cellColor[i]);
        }

        if (hoveredIndex >= 0 && hoveredIndex != selectedIndex) {
            float cx = ox + cellX[hoveredIndex] * radius;
            float cy = oy + cellY[hoveredIndex] * radius;
            fillHex(context, cx, cy, radius + f * 1.4f, 0x55FFFFFF);
            fillHex(context, cx, cy, radius + f * 0.6f, 0xFFE8F0FF);
            fillHex(context, cx, cy, cellRadius, 0xFF000000 | cellColor[hoveredIndex]);
        }

        if (selectedIndex >= 0) {
            float cx = ox + cellX[selectedIndex] * radius;
            float cy = oy + cellY[selectedIndex] * radius;
            int selRgb = cellColor[selectedIndex] & 0xFFFFFF;
            // Multi-layered glowing outline around the active hexagon cell
            fillHex(context, cx, cy, radius + f * 3.2f, 0x4C000000 | selRgb);
            fillHex(context, cx, cy, radius + f * 2.3f, 0x99FFFFFF);
            fillHex(context, cx, cy, radius + f * 1.5f, 0xFFFFFFFF);
            fillHex(context, cx, cy, radius + f * 0.55f, 0xFF0A0C12);
            fillHex(context, cx, cy, cellRadius, 0xFF000000 | selRgb);
        }

        matrices.popMatrix();
    }

    /**
     * Fills a pointy-top hexagon centered at (cx, cy) with the given circumradius, in the current
     * (physical pixel) coordinate space. The middle band is a single rectangle; the top and bottom
     * triangles are drawn as one-pixel scanlines.
     */
    private static void fillHex(DrawContext context, float cx, float cy, float r, int argb) {
        if (r <= 0.5f) {
            return;
        }
        float halfWidth = SQRT3 * 0.5f * r;
        int top = Math.round(cy - r);
        int bottom = Math.round(cy + r);
        int midTop = Math.round(cy - r * 0.5f);
        int midBottom = Math.round(cy + r * 0.5f);

        context.fill(Math.round(cx - halfWidth), midTop, Math.round(cx + halfWidth), midBottom, argb);

        for (int row = top; row < midTop; row++) {
            float dy = Math.abs(cy - (row + 0.5f));
            float hw = SQRT3 * (r - dy);
            if (hw > 0.25f) {
                context.fill(Math.round(cx - hw), row, Math.round(cx + hw), row + 1, argb);
            }
        }
        for (int row = midBottom; row < bottom; row++) {
            float dy = Math.abs((row + 0.5f) - cy);
            float hw = SQRT3 * (r - dy);
            if (hw > 0.25f) {
                context.fill(Math.round(cx - hw), row, Math.round(cx + hw), row + 1, argb);
            }
        }
    }
}

