package org.firstinspires.ftc.teamcode.students.ethan;

import com.pedropathing.api.Paths;
import com.pedropathing.api.PoseFactory;
import com.pedropathing.math.Pose;
import com.pedropathing.paths.Path;

/**
 * ETHAN'S PATHS. All the field numbers for Ethan's Autos live in this one file.
 *
 * HOW TO USE THE PEDRO VISUALIZER
 *   1. Lay the path out in the Visualizer and run it there until it looks right.
 *   2. Copy the poses it gives you into the POSES block below.
 *   3. Add a method that builds the path out of those poses.
 *   4. Your Auto calls paths.toPollen() and never mentions a number.
 *
 * Keeping the numbers here means you can re-measure the field once and every one
 * of your Autos follows along.
 *
 * HEADING, THE THING THAT CATCHES EVERYONE OUT
 *   .linear(a, b) ..... turn smoothly from a's heading to b's heading on the way
 *   .constant(h) ...... hold one heading the whole path (good for driving while
 *                       aimed at the goal)
 *   .tangent() ........ point the front of the robot along the path
 *   .reverseTangent() . drive the path backwards
 */
public class EthanPaths {

    private final PoseFactory poses = PoseFactory.degrees();

    // ======================= POSES =======================
    // x and y in inches, heading in degrees. The field is 144 x 144.
    // Put your Visualizer numbers here.

    /** Where the robot really sits when the match starts. Measure it, don't guess. */
    public final Pose START = poses.of(0, 0, 0);

    /** Where the pollen is. */
    public final Pose POLLEN = poses.of(0, 0, 0);

    /** A good spot to shoot from. */
    public final Pose SHOOTING_SPOT = poses.of(0, 0, 0);

    /** The parking spot. */
    public final Pose PARK = poses.of(0, 0, 0);
    // =====================================================

    // ======================= PATHS =======================

    public Path toPollen() {
        return Paths.line(START, POLLEN).linear(START, POLLEN);
    }

    public Path backToShoot() {
        return Paths.line(POLLEN, SHOOTING_SPOT).linear(POLLEN, SHOOTING_SPOT);
    }

    public Path toPark() {
        return Paths.line(SHOOTING_SPOT, PARK).linear(SHOOTING_SPOT, PARK);
    }
}
