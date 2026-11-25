package org.firstinspires.ftc.teamcode.common;

import com.pedropathing.api.PoseFactory;

/**
 * ALLIANCE = which side of the field we are on.
 *
 * Paths are laid out ONCE for red in the Pedro Visualizer. Blue is the same plan
 * with the field flipped left-to-right, so asking this enum for a pose factory
 * gets you numbers that are already correct for the side you are on:
 *
 *   PoseFactory poses = alliance.poses();
 *   Pose start = poses.of(56, 8.02, 90);   // red as typed, blue flipped
 *
 * That means a path only ever has ONE set of numbers in the code. Fix the red
 * path and blue is fixed too. Never type a second set of blue numbers by hand --
 * that is how the two sides quietly drift apart.
 *
 * HOW THE FLIP WORKS
 *   x        ->  2 * MIRROR_AXIS_X - x   (left-right across the middle of the field)
 *   y        ->  unchanged               (both alliances start at the same wall)
 *   heading  ->  180 - heading
 *
 * Pedro does the arithmetic (PoseFactory.mirrorX), so there is nothing to get
 * wrong by hand. A PoseFactory is immutable: calling mirrorX gives you a NEW
 * factory and leaves the original alone, so RED can never be corrupted by BLUE.
 */
public enum Alliance {
    RED,
    BLUE;

    // ===================== SETTINGS =====================
    /**
     * The left-right centre line of the field that red and blue mirror across.
     *
     * 72 is the exact middle of a 144 x 144 inch field, which is where a FIRST
     * field is symmetrical.
     *
     * WORTH KNOWING: the HIVE decorations drawn in the Visualizer export imply
     * 71.25 rather than 72 (the red HIVE sits 24.7 in from centre, the blue one
     * 23.2 in). Those shapes were placed by hand in the Visualizer and are not
     * measured field data, so 72 is the number to trust. If BLUE turns out to be
     * consistently about an inch and a half off while RED is good, this constant
     * is the ONE place to change.
     */
    public static final double MIRROR_AXIS_X = 72.0;
    // ====================================================

    /**
     * A pose factory that takes RED-side numbers (degrees) and returns poses for
     * THIS alliance. For RED it hands the numbers back unchanged.
     */
    public PoseFactory poses() {
        PoseFactory red = PoseFactory.degrees();
        return this == RED ? red : red.mirrorX(MIRROR_AXIS_X);
    }

    public Alliance other() {
        return this == RED ? BLUE : RED;
    }
}
