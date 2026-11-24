package com.thaistoanf.simplelocationmarking.gui;

import com.thaistoanf.simplelocationmarking.util.TextUtil;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import org.joml.Matrix3x2fStack;

/**
 * Shared color constants and drawing primitives for the Translucent Dark Glass UI theme.
 */
public final class GlassUi {
    // ---- Screen & Container Colors ----
    /** Subtle dark translucent full-screen overlay rendered over the live game world. */
    public static final int SCREEN_BACKDROP = 0x7008090C;
    /** Centered translucent dark glass main panel background. */
    public static final int MAIN_PANEL_BG = 0xD012141C;
    /** Crisp 1px subtle border for the main panel and header divider. */
    public static final int MAIN_PANEL_BORDER = 0xFF2E3446;
    /** Top header bar background inside the main panel. */
    public static final int HEADER_BG = 0xE01A1D28;

    // ---- Sub-Panel & List Viewport Colors ----
    /** Recessed dark translucent background for the scrollable list viewport and form sub-panels. */
    public static final int SUB_PANEL_BG = 0x880B0C10;
    /** Subtle 1px border for recessed sub-panels. */
    public static final int SUB_PANEL_BORDER = 0xFF252A3A;

    // ---- Waypoint List Row Card Colors ----
    public static final int ROW_NORMAL_BG = 0x661E222F;
    public static final int ROW_NORMAL_BORDER = 0x40343B52;
    public static final int ROW_HOVER_BG = 0x99282E40;
    public static final int ROW_HOVER_BORDER = 0x804C587A;
    public static final int ROW_SELECTED_BG = 0xBB2D364D;
    public static final int ROW_SELECTED_BORDER = 0xFF68A4FF;

    // ---- In-Game HUD Waypoint Marker Colors ----
    public static final int HUD_BADGE_BG_DEFAULT = 0xA6101218;
    public static final int HUD_BADGE_BG_HOVER = 0xBF101218;
    public static final int HUD_DISTANCE_BG = 0x80000000;
    public static final int HUD_DISTANCE_TEXT = 0xFFE0E0E0;

    private GlassUi() {
    }

    /** Renders the subtle dark translucent full-screen overlay over the live game world. */
    public static void drawScreenBackdrop(DrawContext context, int width, int height) {
        context.fill(0, 0, width, height, SCREEN_BACKDROP);
    }

    /**
     * Renders the centered Translucent Glass Main Panel with a top header bar and crisp 1px border.
     */
    public static void drawMainPanel(DrawContext context, int left, int top, int right, int bottom, int headerHeight) {
        // Outer subtle drop shadow
        context.fill(left - 2, top - 2, right + 2, bottom + 2, 0x38000000);
        // Main glass panel body
        context.fill(left, top, right, bottom, MAIN_PANEL_BG);
        // Top header bar
        int headerBottom = top + headerHeight;
        context.fill(left, top, right, headerBottom, HEADER_BG);
        // Top subtle specular highlight
        context.fill(left + 1, top + 1, right - 1, top + 2, 0x16FFFFFF);
        // Header bottom separator line
        context.fill(left, headerBottom - 1, right, headerBottom, MAIN_PANEL_BORDER);
        // Crisp 1px outer border
        drawBorder(context, left - 1, top - 1, right + 1, bottom + 1, MAIN_PANEL_BORDER);
    }

    /**
     * Renders a recessed translucent dark sub-panel with a crisp 1px border.
     */
    public static void drawSubPanel(DrawContext context, int left, int top, int right, int bottom) {
        context.fill(left, top, right, bottom, SUB_PANEL_BG);
        drawBorder(context, left, top, right, bottom, SUB_PANEL_BORDER);
    }

    /**
     * Renders a translucent pill box with a 1px border.
     */
    public static void drawPill(DrawContext context, int left, int top, int right, int bottom, int fillColor, int borderColor) {
        context.fill(left, top, right, bottom, fillColor);
        drawBorder(context, left, top, right, bottom, borderColor);
    }

    /**
     * Draws a 1px rectangle border inside the bounds {@code [left, top, right, bottom]}.
     */
    public static void drawBorder(DrawContext context, int left, int top, int right, int bottom, int color) {
        if (right <= left || bottom <= top) {
            return;
        }
        context.fill(left, top, right, top + 1, color);
        context.fill(left, bottom - 1, right, bottom, color);
        if (bottom - top > 2) {
            context.fill(left, top + 1, left + 1, bottom - 1, color);
            context.fill(right - 1, top + 1, right, bottom - 1, color);
        }
    }

    /**
     * Renders a glowing colored square swatch with a 1px dark/white border.
     */
    public static void drawGlowingSwatch(DrawContext context, int x, int y, int size, int rgb) {
        int cleanRgb = rgb & 0xFFFFFF;
        // Soft outer colored glow
        context.fill(x - 2, y - 2, x + size + 2, y + size + 2, 0x38000000 | cleanRgb);
        // Crisp 1px outer frame (subtle light/white border)
        drawBorder(context, x - 1, y - 1, x + size + 1, y + size + 1, 0xCCFFFFFF);
        // Solid color core
        context.fill(x, y, x + size, y + size, 0xFF000000 | cleanRgb);
        // Subtle top-half specular sheen
        context.fill(x + 1, y + 1, x + size - 1, y + 2, 0x45FFFFFF);
    }

    /**
     * Brightens a waypoint RGB color so text tinted with it remains vibrant and readable
     * over the dark glass badge background.
     */
    public static int brightenForText(int rgb) {
        int r = (rgb >> 16) & 0xFF;
        int g = (rgb >> 8) & 0xFF;
        int b = rgb & 0xFF;
        int lum = (r * 299 + g * 587 + b * 114) / 1000;
        if (lum < 150) {
            float boost = (165 - lum) / 255.0f;
            r = Math.min(255, Math.round(r + (255 - r) * boost));
            g = Math.min(255, Math.round(g + (255 - g) * boost));
            b = Math.min(255, Math.round(b + (255 - b) * boost));
        } else {
            // Slightly lift towards white for crispness
            r = Math.min(255, r + (255 - r) / 5);
            g = Math.min(255, g + (255 - g) / 5);
            b = Math.min(255, b + (255 - b) / 5);
        }
        return (r << 16) | (g << 8) | b;
    }

    /**
     * Renders a live preview of both the compact default HUD badge and the expanded hover pill
     * inside the Add/Edit screen's preview panel.
     */
    public static void drawLiveMarkerPreview(DrawContext context, TextRenderer textRenderer,
                                             String rawName, int rgb,
                                             int boxLeft, int boxTop, int boxRight, int boxBottom) {
        int cleanRgb = rgb & 0xFFFFFF;
        int accent = 0xFF000000 | cleanRgb;
        int brightText = 0xFF000000 | brightenForText(cleanRgb);

        String initial = TextUtil.initialUpper(rawName);
        String display = TextUtil.capitalizeFirst(rawName);
        if (display.isEmpty()) {
            display = "Waypoint";
        }

        int centerX = (boxLeft + boxRight) / 2;
        int maxPillWidth = Math.max(32, (boxRight - boxLeft) - 14);
        String fittedName = textRenderer.trimToWidth(display, maxPillWidth - 10);
        if (fittedName.length() < display.length() && fittedName.length() > 2) {
            fittedName = textRenderer.trimToWidth(display, maxPillWidth - 16) + "..";
        }

        // 1. Compact Default Badge (16x16) near top of preview area
        int badgeSize = 16;
        int initialW = textRenderer.getWidth(initial);
        int compactW = Math.max(badgeSize, initialW + 8);
        int cLeft = centerX - compactW / 2;
        int cTop = boxTop + 6;
        int cRight = cLeft + compactW;
        int cBottom = cTop + badgeSize;

        context.fill(cLeft, cTop, cRight, cBottom, HUD_BADGE_BG_DEFAULT);
        drawBorder(context, cLeft - 1, cTop - 1, cRight + 1, cBottom + 1, accent);
        context.fill(cLeft, cTop, cRight, cTop + 2, accent);
        context.drawText(textRenderer, initial, centerX - (initialW - 1) / 2, cTop + 5, brightText, true);

        // 2. Expanded Hover Pill + Distance Label below it
        int nameW = textRenderer.getWidth(fittedName);
        int expandedW = Math.min(maxPillWidth, Math.max(badgeSize, nameW + 10));
        int eLeft = centerX - expandedW / 2;
        int eTop = cBottom + 8;
        int eRight = eLeft + expandedW;
        int eBottom = eTop + badgeSize;

        context.fill(eLeft, eTop, eRight, eBottom, HUD_BADGE_BG_HOVER);
        drawBorder(context, eLeft - 1, eTop - 1, eRight + 1, eBottom + 1, accent);
        context.fill(eLeft, eTop, eRight, eTop + 2, accent);
        context.drawText(textRenderer, fittedName, centerX - (nameW - 1) / 2, eTop + 5, 0xFFFFFFFF, true);

        // 3. Scaled Distance Pill (0.75f) below expanded pill
        String sampleDist = "125m";
        int distW = textRenderer.getWidth(sampleDist);
        Matrix3x2fStack matrices = context.getMatrices();
        matrices.pushMatrix();
        matrices.translate(centerX, eBottom + 3.0f);
        matrices.scale(0.75f, 0.75f);
        int dLeft = -distW / 2;
        context.fill(dLeft - 3, -1, dLeft + distW + 2, 9, HUD_DISTANCE_BG);
        context.drawText(textRenderer, sampleDist, dLeft, 0, HUD_DISTANCE_TEXT, true);
        matrices.popMatrix();
    }
}

