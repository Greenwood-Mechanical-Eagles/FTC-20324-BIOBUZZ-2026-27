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
 * HOW THE FLIP WORKS -- it is a ROTATION, not a mirror
 *   x        ->  2 * CENTRE - x
 *   y        ->  2 * CENTRE - y
 *   heading  ->  heading + 180
 *
 * i.e. spin the whole plan 180 degrees about the middle of the field. Two
 * reasons that is the right transform and a left-right mirror is not:
 *
 *   1. The alliances start at OPPOSITE ends of the field. A rotation sends the
 *      start pose to the far wall, which is where the other alliance actually
 *      begins. A mirror would leave both alliances starting at the same wall.
 *   2. A rotation keeps the plan's handedness. A mirror reverses it, so every
 *      sideways move would go the wrong way round for one alliance -- which is
 *      exactly what happened on the field on 2026-11-25: the mirrored Auto
 *      crabbed right where it needed to crab left and hit the teeter totter.
 *
 * Pedro does the arithmetic (PoseFactory.rotateAround), so there is nothing to
 * get wrong by hand. A PoseFactory is immutable: rotateAround gives you a NEW
 * factory and leaves the original alone, so RED can never be corrupted by BLUE.
 */
public enum Alliance {
    RED,
    BLUE;

    // ===================== SETTINGS =====================
    /**
     * The middle of the field, which the plan is rotated about.
     *
     * 72 is the exact centre of a 144 x 144 inch field, which is where a FIRST
     * field is symmetrical.
     *
     * WORTH KNOWING: the HIVE decorations drawn in the Visualizer export imply
     * 71.25 rather than 72 (the red HIVE sits 24.7 in from centre, the blue one
     * 23.2 in). Those shapes were placed by hand in the Visualizer and are not
     * measured field data, so 72 is the number to trust. If BLUE turns out to be
     * consistently about an inch and a half off while RED is good, this constant
     * is the ONE place to change.
     */
    public static final double FIELD_CENTRE = 72.0;
    // ====================================================

    /**
     * A pose factory that takes RED-side numbers (degrees) and returns poses for
     * THIS alliance. For RED it hands the numbers back unchanged.
     */
    public PoseFactory poses() {
        PoseFactory red = PoseFactory.degrees();
        return this == RED ? red : red.rotateAround(FIELD_CENTRE, FIELD_CENTRE, 180);
    }

    public Alliance other() {
        return this == RED ? BLUE : RED;
    }
}
