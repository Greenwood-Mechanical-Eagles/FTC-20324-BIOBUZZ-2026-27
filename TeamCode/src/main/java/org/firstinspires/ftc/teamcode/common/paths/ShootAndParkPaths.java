package org.firstinspires.ftc.teamcode.common.paths;

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
public class ShootAndParkPaths {

    // ===================== SETTINGS =====================
    /**
     * The heading the robot physically sits at against the wall at the start of
     * the match. This is a measurement, not a tuning knob -- change it only if
     * the robot is actually placed differently.
     */
    public static final double START_HEADING = 90;

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
     *
     * Independent of START_HEADING: path 1 turns the robot from one to the other
     * on the way up the field.
     */
    public static double SHOOT_HEADING = AIM_AT_HIVE_HEADING;
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

    /** Across to the corner. Driven backwards (reverseTangent). */
    public final Pose CORNER;

    /** Up the field to the parking spot. Driven backwards (reverseTangent). */
    public final Pose PARK;
    // =====================================================

    public ShootAndParkPaths(Alliance alliance) {
        this.alliance = alliance;
        this.poses = alliance.poses();

        START = poses.of(56, 8.0197, START_HEADING);
        SHOOT = poses.of(56.2685, 31.1149, SHOOT_HEADING);

        // The headings on these two are only for telemetry and for setting up:
        // both paths below use reverseTangent(), which works the heading out from
        // the direction of travel and ignores whatever is typed here.
        CORNER = poses.of(18.9244, 30.9040, 0.32);
        PARK = poses.of(14.6883, 94.4927, 273.81);
    }

    // ======================= PATHS =======================

    /**
     * Off the wall to the shooting spot, turning from the wall heading to the
     * shooting heading on the way.
     *
     * linear(START, SHOOT) rather than a constant heading on purpose: it lets
     * SHOOT_HEADING be aimed wherever the pollen need to go without pretending
     * the robot started the match rotated. While the two headings are equal this
     * behaves exactly like holding one heading.
     */
    public Path startToShoot() {
        return Paths.line(START, SHOOT).linear(START, SHOOT);
    }

    /** Backwards across the field to the corner. */
    public Path shootToCorner() {
        return Paths.line(SHOOT, CORNER).reverseTangent();
    }

    /** Backwards up the field into the parking spot. */
    public Path cornerToPark() {
        return Paths.line(CORNER, PARK).reverseTangent();
    }
}
