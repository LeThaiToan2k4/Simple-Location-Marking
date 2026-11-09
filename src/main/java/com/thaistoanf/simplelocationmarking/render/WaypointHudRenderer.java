package com.thaistoanf.simplelocationmarking.render;

import com.thaistoanf.simplelocationmarking.gui.GlassUi;
import com.thaistoanf.simplelocationmarking.waypoint.Waypoint;
import com.thaistoanf.simplelocationmarking.waypoint.WaypointManager;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElement;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.client.world.ClientWorld;
import org.joml.Matrix3x2fStack;
import org.joml.Matrix4f;
import org.joml.Vector4f;

import java.util.List;

/**
 * Draws translucent dark-glass waypoint markers on the 2D HUD at a strictly constant screen-space size.
 * <p>
 * Each visible waypoint of the current dimension is projected from its world position
 * {@code (x + 0.5, y + 1.0, z + 0.5)} by normalizing its camera-relative direction vector onto
 * a unit sphere before applying the captured view-projection matrix. This guarantees that
 * waypoints several kilometers away (beyond Vanilla's far clip plane) are never clipped and
 * render at the exact same compact screen-space size as nearby waypoints.
 */
public final class WaypointHudRenderer implements HudElement {
    /** Crosshair must be within this angle (degrees) of a waypoint to expand it. */
    private static final double HOVER_ANGLE_DEGREES = 3.0;
    private static final float HOVER_COS_THRESHOLD = (float) Math.cos(Math.toRadians(HOVER_ANGLE_DEGREES));
    /** Expansion progress gained per game tick (about 0.2 seconds for a full expansion). */
    private static final float EXPAND_SPEED_PER_TICK = 0.25f;

    /** Fixed screen-space badge height (16x16 compact badge at scale 1.0f). */
    private static final int BOX_HEIGHT = 16;
    private static final int COMPACT_PADDING_X = 4;
    private static final int EXPANDED_PADDING_X = 6;
    private static final int SCREEN_MARGIN = 64;

    /** Fixed scale for the subtle distance pill below the marker. */
    private static final float DISTANCE_SCALE = 0.75f;
    private static final int DISTANCE_BACKGROUND = GlassUi.HUD_DISTANCE_BG; // 0x80000000
    private static final int DISTANCE_COLOR = GlassUi.HUD_DISTANCE_TEXT;    // 0xFFE0E0E0

    private final Vector4f clip = new Vector4f();

    private Waypoint[] visible = new Waypoint[32];
    private float[] screenX = new float[32];
    private float[] screenY = new float[32];
    private double[] distance = new double[32];
    private int count;

    @Override
    public void render(DrawContext context, RenderTickCounter tickCounter) {
        MinecraftClient client = MinecraftClient.getInstance();
        ClientPlayerEntity player = client.player;
        ClientWorld world = client.world;
        if (player == null || world == null || client.options.hudHidden || !ProjectionState.isValid()) {
            return;
        }
        List<Waypoint> waypoints = WaypointManager.all();
        if (waypoints.isEmpty()) {
            return;
        }

        String dimension = WaypointManager.dimensionId(world);
        int width = context.getScaledWindowWidth();
        int height = context.getScaledWindowHeight();
        float centerX = width * 0.5f;
        float centerY = height * 0.5f;

        Matrix4f viewProjection = ProjectionState.viewProjection();
        double camX = ProjectionState.cameraX();
        double camY = ProjectionState.cameraY();
        double camZ = ProjectionState.cameraZ();
        float fx = ProjectionState.forwardX();
        float fy = ProjectionState.forwardY();
        float fz = ProjectionState.forwardZ();

        double px = player.getX();
        double py = player.getY();
        double pz = player.getZ();

        TextRenderer textRenderer = client.textRenderer;
        float step = tickCounter.getDynamicDeltaTicks() * EXPAND_SPEED_PER_TICK;

        // ---- 1. Project normalized direction vectors onto the 2D HUD ----
        count = 0;
        Waypoint hovered = null;
        float bestScore = HOVER_COS_THRESHOLD;

        for (int i = 0, n = waypoints.size(); i < n; i++) {
            Waypoint waypoint = waypoints.get(i);
            if (waypoint.hidden || !dimension.equals(waypoint.dimension)) {
                waypoint.hoverProgress = 0.0f;
                continue;
            }

            // Camera-relative position in double precision, normalized onto the unit distance sphere
            // so waypoints at any distance (10m, 500m, or 50km+) never clip or distort in scale.
            double relX = (waypoint.x + 0.5) - camX;
            double relY = (waypoint.y + 1.0) - camY;
            double relZ = (waypoint.z + 0.5) - camZ;
            double distSq = relX * relX + relY * relY + relZ * relZ;

            float nx;
            float ny;
            float nz;
            if (distSq > 1.0e-8) {
                double invLen = 1.0 / Math.sqrt(distSq);
                nx = (float) (relX * invLen);
                ny = (float) (relY * invLen);
                nz = (float) (relZ * invLen);
            } else {
                nx = fx;
                ny = fy;
                nz = fz;
            }

            clip.set(nx, ny, nz, 1.0f);
            viewProjection.transform(clip);
            if (clip.w <= 1.0e-4f) {
                // Behind the camera.
                waypoint.hoverProgress = Math.max(0.0f, waypoint.hoverProgress - step);
                continue;
            }
            float invW = 1.0f / clip.w;
            float sx = (clip.x * invW * 0.5f + 0.5f) * width;
            float sy = (0.5f - clip.y * invW * 0.5f) * height;
            if (sx < -SCREEN_MARGIN || sx > width + SCREEN_MARGIN || sy < -SCREEN_MARGIN || sy > height + SCREEN_MARGIN) {
                waypoint.hoverProgress = Math.max(0.0f, waypoint.hoverProgress - step);
                continue;
            }

            ensureCapacity(count + 1);
            visible[count] = waypoint;
            screenX[count] = sx;
            screenY[count] = sy;
            distance[count] = waypoint.distanceTo(px, py, pz);
            count++;

            // Angular test between the camera forward vector and the normalized waypoint direction.
            float score = nx * fx + ny * fy + nz * fz;
            // Also accept the crosshair resting directly on the (collapsed) badge.
            float halfBadge = Math.max(BOX_HEIGHT, waypoint.initialWidth(textRenderer) + COMPACT_PADDING_X * 2) * 0.5f + 1.0f;
            if (Math.abs(sx - centerX) <= halfBadge && Math.abs(sy - centerY) <= BOX_HEIGHT * 0.5f + 1.0f) {
                score = Math.max(score, 1.0f);
            }
            if (score > bestScore) {
                bestScore = score;
                hovered = waypoint;
            }
        }

        if (count == 0) {
            return;
        }

        // ---- 2. Animate expansion ----
        for (int i = 0; i < count; i++) {
            Waypoint waypoint = visible[i];
            if (waypoint == hovered) {
                waypoint.hoverProgress = Math.min(1.0f, waypoint.hoverProgress + step);
            } else {
                waypoint.hoverProgress = Math.max(0.0f, waypoint.hoverProgress - step);
            }
        }

        // ---- 3. Sort far to near so closer markers are drawn on top (insertion sort, no allocation) ----
        for (int i = 1; i < count; i++) {
            Waypoint w = visible[i];
            float x = screenX[i];
            float y = screenY[i];
            double d = distance[i];
            int j = i - 1;
            while (j >= 0 && distance[j] < d) {
                visible[j + 1] = visible[j];
                screenX[j + 1] = screenX[j];
                screenY[j + 1] = screenY[j];
                distance[j + 1] = distance[j];
                j--;
            }
            visible[j + 1] = w;
            screenX[j + 1] = x;
            screenY[j + 1] = y;
            distance[j + 1] = d;
        }

        // ---- 4. Draw at fixed 1.0f screen-space scale (hovered marker last so it is always on top) ----
        int hoveredIndex = -1;
        for (int i = 0; i < count; i++) {
            if (visible[i] == hovered) {
                hoveredIndex = i;
                continue;
            }
            drawMarker(context, textRenderer, visible[i], screenX[i], screenY[i], distance[i]);
        }
        if (hoveredIndex >= 0) {
            drawMarker(context, textRenderer, visible[hoveredIndex], screenX[hoveredIndex], screenY[hoveredIndex], distance[hoveredIndex]);
        }

        // Release references so removed waypoints can be garbage collected.
        for (int i = 0; i < count; i++) {
            visible[i] = null;
        }
    }

    private static void drawMarker(DrawContext context, TextRenderer textRenderer, Waypoint waypoint,
                                   float sx, float sy, double distanceValue) {
        int cx = Math.round(sx);
        int cy = Math.round(sy);

        float t = smoothstep(waypoint.hoverProgress);
        int initialWidth = waypoint.initialWidth(textRenderer);
        int collapsedWidth = Math.max(BOX_HEIGHT, initialWidth + COMPACT_PADDING_X * 2);
        int expandedWidth = Math.max(collapsedWidth, waypoint.displayNameWidth(textRenderer) + EXPANDED_PADDING_X * 2);
        int boxWidth = Math.round(collapsedWidth + (expandedWidth - collapsedWidth) * t);

        int left = cx - boxWidth / 2;
        int top = cy - BOX_HEIGHT / 2;
        int right = left + boxWidth;
        int bottom = top + BOX_HEIGHT;

        int rgb = waypoint.color & 0xFFFFFF;
        int accentColor = 0xFF000000 | rgb;

        // Interpolate dark glass background from 0xA6101218 (default) to 0xBF101218 (hovered)
        int bgAlpha = Math.round(0xA6 + (0xBF - 0xA6) * t);
        int glassBg = (bgAlpha << 24) | 0x101218;
        context.fill(left, top, right, bottom, glassBg);

        // Crisp 1px outer border + 2px top accent bar using the waypoint's selected RGB color
        GlassUi.drawBorder(context, left - 1, top - 1, right + 1, bottom + 1, accentColor);
        context.fill(left, top, right, top + 2, accentColor);

        int textY = top + (BOX_HEIGHT + 2 - 8) / 2; // centered below the 2px top accent bar
        int brightInitialRgb = GlassUi.brightenForText(rgb);

        float letterAlpha = 1.0f - clamp01(t * 2.0f);
        if (letterAlpha > 0.02f) {
            int color = alpha(letterAlpha) | brightInitialRgb;
            // Glyph advance includes one trailing pixel of spacing; subtract it to center visually.
            context.drawText(textRenderer, waypoint.initial(), cx - (initialWidth - 1) / 2, textY, color, true);
        }

        float nameAlpha = clamp01(t * 2.0f - 1.0f);
        if (nameAlpha > 0.02f) {
            int nameWidth = waypoint.displayNameWidth(textRenderer);
            int color = alpha(nameAlpha) | 0xFFFFFF;
            context.enableScissor(left + 1, top + 2, right - 1, bottom - 1);
            context.drawText(textRenderer, waypoint.displayName(), cx - (nameWidth - 1) / 2, textY, color, true);
            context.disableScissor();
        }

        // Distance label below marker inside a smaller translucent dark pill (0x80000000, scale 0.75f, 0xFFE0E0E0)
        String distanceText = waypoint.distanceText(distanceValue);
        int distanceWidth = waypoint.distanceWidth(textRenderer);
        Matrix3x2fStack matrices = context.getMatrices();
        matrices.pushMatrix();
        matrices.translate(cx, bottom + 3.0f);
        matrices.scale(DISTANCE_SCALE, DISTANCE_SCALE);
        int dLeft = -distanceWidth / 2;
        context.fill(dLeft - 3, -1, dLeft + distanceWidth + 2, 9, DISTANCE_BACKGROUND);
        context.drawText(textRenderer, distanceText, dLeft, 0, DISTANCE_COLOR, true);
        matrices.popMatrix();
    }

    private void ensureCapacity(int required) {
        if (required <= visible.length) {
            return;
        }
        int size = Math.max(required, visible.length * 2);
        visible = java.util.Arrays.copyOf(visible, size);
        screenX = java.util.Arrays.copyOf(screenX, size);
        screenY = java.util.Arrays.copyOf(screenY, size);
        distance = java.util.Arrays.copyOf(distance, size);
    }

    private static int alpha(float a) {
        return Math.round(clamp01(a) * 255.0f) << 24;
    }

    private static float clamp01(float v) {
        return v < 0.0f ? 0.0f : Math.min(v, 1.0f);
    }

    private static float smoothstep(float v) {
        float x = clamp01(v);
        return x * x * (3.0f - 2.0f * x);
    }
}
