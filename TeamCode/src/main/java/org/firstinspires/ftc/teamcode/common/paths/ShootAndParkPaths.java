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
 *   new ShootAndParkPaths(Alliance.BLUE)   same plan, rotated 180 about the centre
 *
 * Every number below is typed for RED exactly as the Pedro Visualizer exported
 * it. Blue costs nothing extra: Alliance.poses() rotates each pose as it is
 * built, so there is only ever ONE set of numbers to re-measure. The flip is a
 * ROTATION, not a left-right mirror -- see common/Alliance for why that matters.
 *
 * Source: docs/PedroPathing/ShootAndPark.pp.json (exported 2026-11-25).
 * Re-exported the path? Replace the POSES block, and the json alongside it.
 *
 * THE PLAN -- one motion, no turning
 * The robot is set down already facing START_HEADING, with its rear-mounted
 * shooter pointing up the field at the HIVE, and it never rotates again.
 *
 *   1. startToShoot()    reverses off the wall to the shooting spot
 *   2. shootToCorner()   crabs sideways towards the corner
 *   3. cornerToPark()    reverses up the field into the parking spot
 *
 * ---------------------------------------------------------------------------
 * THE SHOOTER FIRES OUT THE BACK (Launcher.AIM_OFFSET_DEG = 180), so the robot
 * sits facing almost directly AWAY from the HIVE. SHOOT_HEADING is 282.8,
 * derived from the HIVE bearing rather than typed.
 *
 * Holding one heading has a visible consequence: legs 1 and 3 travel DIAGONALLY
 * rather than straight back -- about 5 in of sideways drift on leg 1 and 10 in
 * on leg 3. That looks like the robot veering, and it is the path working as
 * designed. Check the Pose readout against the target before trimming anything.
 *
 * Still not fully proven on the field: 282.8 comes from the HIVE as *drawn* in
 * the Visualizer, and those shapes were placed by hand. Confirm on a stand.
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
     * The heading the robot PHYSICALLY SITS AT against the wall.
     *
     * THIS IS A MEASUREMENT, NOT A CHOICE. follower.setPose(START) does not
     * steer the robot -- it DEFINES where the field frame is. If this number
     * disagrees with how the robot is really sitting, the entire path rotates
     * by the difference and nothing downstream can detect or correct it.
     *
     * 270 because the robot is pushed FLUSH against the wall, front to the
     * wall, shooter pointing up the field. Flush can only ever be 90 or 270;
     * any other number is physically unachievable by pushing a robot against a
     * flat wall, so it must not be set to one.
     *
     * HOW WE LEARNED THIS: it was briefly set equal to SHOOT_HEADING (282.8) so
     * that Auto never had to turn. But 282.8 is 12.8 degrees off flush, so the
     * robot really sat at 270 while the code believed 282.8. That rotated the
     * whole plan and put the park about 21 in off -- which matches the ~18 in
     * miss measured on the field on 2026-11-25.
     *
     * The cost of being honest here is one small turn: path 1 rotates 12.8
     * degrees from this heading to SHOOT_HEADING on the way up the field. Well
     * under a second, and the robot still never turns after the shot.
     */
    public static double START_HEADING = 270;

    // ---------------- FIELD TRIMS (dashboard-tunable) ----------------
    // For SYSTEMATIC offsets: the robot reliably finishes a leg a few inches or
    // a degree or two off, every single run. They do NOT fix a robot that curves
    // mid-leg or wanders differently each time -- that is a tracking or odometry
    // problem and trimming the target just moves where it goes wrong.
    //
    // BEFORE REACHING FOR THESE: this Auto holds one heading, so legs 1 and 3
    // crab DIAGONALLY on purpose -- about 5 in sideways on leg 1 and 10 in on
    // leg 3. That looks like veering and is the path working correctly. Check
    // the Pose readout against the target first.
    //
    // All three take effect on the next INIT, not mid-run, because the poses are
    // built in the constructor.

    /** Added to every held heading. Use for a consistent heading bias. */
    public static double HEADING_TRIM_DEG = 0;

    /** Shifts the driven targets in field inches. START is left alone -- that is
     *  where the robot is physically placed, not somewhere it drives to. */
    public static double TARGET_X_TRIM_IN = 0;
    public static double TARGET_Y_TRIM_IN = 0;
    // Trims are applied to the RED numbers before the alliance rotation, so they
    // rotate with it: one trim corrects both sides.
    // ------------------------------------------------------------------
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

        double h = SHOOT_HEADING + HEADING_TRIM_DEG;
        double tx = TARGET_X_TRIM_IN;
        double ty = TARGET_Y_TRIM_IN;

        // START gets the heading trim but no position trim: it is where the robot
        // is physically set down, not somewhere it drives to.
        START = poses.of(56, 8.0197, START_HEADING + HEADING_TRIM_DEG);

        // Every pose holds the SAME heading: the robot never turns during Auto.
        SHOOT = poses.of(56.2685 + tx, 31.1149 + ty, h);
        CORNER = poses.of(18.9244 + tx, 30.9040 + ty, h);
        PARK = poses.of(14.6883 + tx, 94.4927 + ty, h);
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

    /**
     * Reverses off the wall to the shooting spot, turning the 12.8 degrees from
     * the flush wall heading to the shooting heading on the way.
     *
     * linear() rather than constant() ONLY because START_HEADING must describe
     * where the robot really sits (flush, 270) rather than where we would like
     * it to point. Lying about the start heading to avoid this turn is what put
     * the park ~21 in off. Everything after the shot still holds one heading.
     */
    public Path startToShoot() {
        return Paths.line(START, SHOOT).linear(START, SHOOT);
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
