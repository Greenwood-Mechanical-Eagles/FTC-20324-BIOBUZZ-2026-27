package org.firstinspires.ftc.teamcode.autonomous.util;

import com.pedropathing.api.Paths;
import com.pedropathing.api.PoseFactory;
import com.pedropathing.math.Pose;
import com.pedropathing.paths.Path;

public class BioBuzzPaths {
    // Internal Properties
    private final PoseFactory poseFactory = PoseFactory.degrees();

    // Public Positions - IE Starting Positions
    public final Pose testStartPositionX = poseFactory.of(130.75, 118.75, 180);

    // Private Positions - IE Not Starting Positions
    private final Pose xMovementTest = poseFactory.of(94.75, 118.75, 180);


    public Path xMovementTest() {
        return Paths.line(testStartPositionX, xMovementTest).linear(testStartPositionX, xMovementTest);
    }
}

