package org.firstinspires.ftc.teamcode.autonomous.util;

import com.pedropathing.api.Paths;
import com.pedropathing.api.PoseFactory;
import com.pedropathing.math.Pose;
import com.pedropathing.paths.Path;

public class BioBuzzPaths {

    private final PoseFactory poseFactory = PoseFactory.degrees();

    public final Pose nonShooting1 = poseFactory.of(137.0047, 54.8982, 90);
    private final Pose myFirstPath = poseFactory.of(104.8344, 127.7687, 90);

    public Path myFirstPath() {
        return Paths.line(nonShooting1, myFirstPath).linear(nonShooting1, myFirstPath);
    }
}

