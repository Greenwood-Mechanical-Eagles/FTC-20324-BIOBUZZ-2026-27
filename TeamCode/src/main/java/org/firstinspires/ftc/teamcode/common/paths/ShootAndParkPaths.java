package org.firstinspires.ftc.teamcode.common.paths;

import com.acmerobotics.dashboard.config.Config;
import com.pedropathing.api.Paths;
import com.pedropathing.api.PoseFactory;
import com.pedropathing.math.Pose;
import com.pedropathing.paths.Path;

import org.firstinspires.ftc.teamcode.common.Alliance;
import org.firstinspires.ftc.teamcode.common.subsystems.Launcher;

/**
 * SHOOT AND PARK -- the field numbers for the competition Auto, for either alliance.
 *
 *   new ShootAndParkPaths(Alliance.RED)    red as laid out
 *   new ShootAndParkPaths(Alliance.BLUE)   same plan, field flipped left-to-right
 *
 * Every number below is typed for RED exactly as the Pedro Visualizer exported
 * it. Blue costs nothing extra: Alliance.poses() flips each pose as it is built,
 * so there is only ever ONE set of numbers to re-measure. See common/Alliance.
 *
 * Source: docs/PedroPathing/ShootAndPark.pp.json (exported 2026-11-25).
 * Re-exported the path? Replace the POSES block, and the json alongside it.
 *
 * THE PLAN
 *   1. startToShoot()    drive forward off the wall to the shooting spot, facing 90
 *   2. shootToCorner()   back across to the corner
 *   3. cornerToPark()    back up the field to park
 *
 * ---------------------------------------------------------------------------
 * NOT YET DRIVEN. The shooting SPOT was checked in the Visualizer and is good.
 * The only open question is the HEADING it shoots at: SHOOT_HEADING is 90,
 * carried over from the wall start because the exported path never asks for a
 * turn (its heading goes "linear 90 -> 90"). Measured against the HIVE drawn in
 * the export, 90 is about 13 degrees off the HIVE centre.
 *
 * That only matters if the launcher fires straight along robot-forward AND the
 * HIVE really sits where the export draws it -- neither is confirmed, so treat
 * it as "worth one look on a stand", not a known fault. If pollen go wide to one
 * side, AIM_AT_HIVE_HEADING below is the number to try instead.
 * See docs/PedroPathing/README.md.
 * ---------------------------------------------------------------------------
 */
@Config
public class ShootAndParkPaths {

    // ===================== SETTINGS =====================
    /** Bearing from the shooting spot to the middle of the red HIVE as drawn in
     *  the export: 40.6 in away, 102.8 degrees. */
    public static final double HIVE_BEARING_DEG = 102.8;

    /**
     * The heading to sit at so that a REAR-firing shooter puts the pollen on the
     * HIVE. Derived from the shooter geometry rather than typed, so the two can
     * never drift apart: 102.8 - 180 = 282.8.
     *
     * Still only as good as the HIVE position drawn in the Visualizer, and those
     * shapes were placed by hand and disagree with each other by 1.5 in. So this
     * is the best estimate available, not a measurement -- confirm it on a stand
     * and replace it with whatever actually scores. See docs/PedroPathing/README.
     */
    public static final double AIM_AT_HIVE_HEADING =
            ((HIVE_BEARING_DEG - Launcher.AIM_OFFSET_DEG) % 360 + 360) % 360;

    /**
     * Where the robot points when it fires. The most important number here.
     *
     * THE SHOOTER FIRES OUT THE BACK (Launcher.AIM_OFFSET_DEG), so this is NOT
     * the direction the pollen go -- they leave on SHOOT_HEADING + 180, which
     * means the robot sits facing almost directly AWAY from the HIVE.
     */
    public static double SHOOT_HEADING = AIM_AT_HIVE_HEADING;

    /**
     * The heading the robot physically sits at against the wall, and the heading
     * it holds for the ENTIRE Auto. Same as SHOOT_HEADING on purpose.
     *
     * THIS IS A PLACEMENT INSTRUCTION, not a tuning knob: the robot is set down
     * on the start tile already facing this way, with its shooter pointing up
     * the field at the HIVE. Nothing in Auto ever turns.
     *
     * That is what makes the whole run one motion -- it reverses off the wall,
     * fires, and reverses onward to park, never rotating. The alternative was
     * starting at 90 and spinning 167 degrees at the shooting spot, which cost
     * a couple of seconds and was only ever an artefact of the exported path.
     */
    public static double START_HEADING = SHOOT_HEADING;
    // ====================================================

    private final PoseFactory poses;

    /** Which side these poses came out for. Handy for telemetry. */
    public final Alliance alliance;

    // ======================= POSES =======================
    // RED-side numbers, inches and degrees, straight from the Visualizer export.
    // The field is 144 x 144. Blue is flipped automatically -- do NOT add blue
    // numbers here.

    /** On the wall where the match starts. Robot is 16 in, so the bumper is on y = 0. */
    public final Pose START;

    /** Forward off the wall; where the opening volley is fired from. */
    public final Pose SHOOT;

    /** Across towards the corner. Reached by crabbing sideways, see below. */
    public final Pose CORNER;

    /** The parking spot, reversed into. */
    public final Pose PARK;
    // =====================================================

    public ShootAndParkPaths(Alliance alliance) {
        this.alliance = alliance;
        this.poses = alliance.poses();

        START = poses.of(56, 8.0197, START_HEADING);
        SHOOT = poses.of(56.2685, 31.1149, SHOOT_HEADING);

        // Every pose holds the SAME heading: the robot never turns during Auto.
        CORNER = poses.of(18.9244, 30.9040, SHOOT_HEADING);
        PARK = poses.of(14.6883, 94.4927, SHOOT_HEADING);
    }

    // ======================= PATHS =======================

    // -------------------------------------------------------------------------
    // ALL THREE PATHS HOLD ONE HEADING. The robot is placed facing START_HEADING
    // and never turns again: it reverses off the wall, fires, and reverses on to
    // park. constant() is what does that -- tangent() or reverseTangent() would
    // rotate the robot to follow the direction of travel, which is exactly what
    // we do not want when the shooter is bolted to the back.
    //
    // Holding one heading means the middle leg is covered SIDEWAYS (see
    // shootToCorner). Mecanum handles that; the Visualizer's own robot profile
    // puts strafing at 65 in/sec against 75 forward, so it costs about 13%.
    // -------------------------------------------------------------------------

    /** Reverses off the wall to the shooting spot. No turning. */
    public Path startToShoot() {
        return Paths.line(START, SHOOT).constant(SHOOT.heading());
    }

    /**
     * Across towards the corner, 37 in, holding the same heading.
     *
     * This leg is almost pure SIDEWAYS crab -- the travel direction is 77 deg off
     * the back of the robot. That is the price of never turning, and it is the
     * one leg of the Auto that does not look like "backing up".
     *
     * If it proves slow or drifty on the field, the fix is in the Visualizer:
     * move PARK to roughly (42, 94.5) and the robot could reverse in a straight
     * line from the shooting spot to park, dropping this leg entirely.
     */
    public Path shootToCorner() {
        return Paths.line(SHOOT, CORNER).constant(SHOOT.heading());
    }

    /** Reverses up the field into the parking spot. Still no turning. */
    public Path cornerToPark() {
        return Paths.line(CORNER, PARK).constant(SHOOT.heading());
    }
}
