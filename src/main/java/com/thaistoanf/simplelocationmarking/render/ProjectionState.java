package com.thaistoanf.simplelocationmarking.render;

import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;

/**
 * Holds the camera matrices of the most recent world render pass.
 * <p>
 * The matrices are copied into pre-allocated objects every frame, so capturing them never
 * allocates. The HUD renderer then projects waypoint positions with exactly the same transform
 * the world was drawn with (including FOV effects and view bobbing).
 */
public final class ProjectionState {
    /** projection * view (rotation only, camera-relative coordinates). */
    private static final Matrix4f VIEW_PROJECTION = new Matrix4f();

    private static double cameraX;
    private static double cameraY;
    private static double cameraZ;

    /** Camera forward direction in world space. */
    private static float forwardX;
    private static float forwardY;
    private static float forwardZ = 1.0f;

    private static boolean valid;

    private ProjectionState() {
    }

    public static void capture(Matrix4fc view, Matrix4fc projection, Vec3d cameraPos) {
        VIEW_PROJECTION.set(projection).mul(view);
        // Strip translational / clip-plane depth terms from the 4th column so unit-sphere
        // direction vectors project purely by camera orientation and FOV at any distance.
        VIEW_PROJECTION.m30(0.0f).m31(0.0f).m32(0.0f).m33(0.0f);
        cameraX = cameraPos.x;
        cameraY = cameraPos.y;
        cameraZ = cameraPos.z;
        // The view matrix maps world directions to view space where the camera looks down -Z.
        // Row 2 of that rotation, negated, is the camera forward vector in world space.
        forwardX = -view.m02();
        forwardY = -view.m12();
        forwardZ = -view.m22();
        valid = true;
    }

    public static void invalidate() {
        valid = false;
    }

    public static boolean isValid() {
        return valid;
    }

    public static Matrix4f viewProjection() {
        return VIEW_PROJECTION;
    }

    public static double cameraX() {
        return cameraX;
    }

    public static double cameraY() {
        return cameraY;
    }

    public static double cameraZ() {
        return cameraZ;
    }

    public static float forwardX() {
        return forwardX;
    }

    public static float forwardY() {
        return forwardY;
    }

    public static float forwardZ() {
        return forwardZ;
    }
}

