package org.firstinspires.ftc.teamcode.pedro;

import com.pedropathing.algorithm.ForesightConfig;
import com.pedropathing.controllers.Controller;
import com.pedropathing.follower.Follower;
import com.pedropathing.math.Matrix;
import com.pedropathing.math.Vector2D;
import com.pedropathing.revhub.drivetrains.MecanumConfig;
import com.pedropathing.revhub.localizers.PinpointConfig;
import com.qualcomm.hardware.gobilda.GoBildaPinpointDriver;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.HardwareMap;

import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit;

public class Constants {
    public static Follower create(HardwareMap h) {
        // return new Follower(Drivetrain, Localizer, Foresight);
        return null;
    }

    public static MecanumConfig drivetrainConfig = new MecanumConfig(c -> {
        c.frontLeftName.set("frontLeftDrive");
        c.frontRightName.set("frontRightDrive");
        c.backLeftName.set("backLeftDrive");
        c.backRightName.set("backRightDrive");
        c.frontLeftDirection.set(DcMotorSimple.Direction.REVERSE);
        c.frontRightDirection.set(DcMotorSimple.Direction.REVERSE);
        c.backLeftDirection.set(DcMotorSimple.Direction.FORWARD);
        c.backRightDirection.set(DcMotorSimple.Direction.REVERSE);
    });

    public static PinpointConfig localizerConfig = new PinpointConfig(c -> {
        c.name.set("pinpoint");
        c.podType.set(GoBildaPinpointDriver.GoBildaOdometryPods.goBILDA_4_BAR_POD);
        c.xPodOffset.set(-3.07394553357222);
        c.yPodOffset.set(-7.16087101012703);
        c.xPodDirection.set(GoBildaPinpointDriver.EncoderDirection.FORWARD);
        c.yPodDirection.set(GoBildaPinpointDriver.EncoderDirection.FORWARD);
        c.globalDistanceUnit.set(DistanceUnit.INCH);
        c.offsetUnits.set(DistanceUnit.INCH);
    });

    public static ForesightConfig foresightConfig = new ForesightConfig(
            c -> {
                Controller primaryTranslationalForward = Controller.proportional(0.20728442885245477);
                Controller secondaryTranslationalForward = Controller.proportional(0.07658605771903465);
                Controller primaryTranslationalLateral = Controller.proportional(3.3163958846216466);
                Controller secondaryTranslationalLateral = Controller.proportional(1.225319663637602);

                c.forwardTranslational.set(Controller.piecewise(secondaryTranslationalForward).put(2.5, primaryTranslationalForward));
                c.strafeTranslational.set(Controller.piecewise(secondaryTranslationalLateral).put(2.5, primaryTranslationalLateral));

                c.coast.set(Controller.proportionalFeedforward(0.02223873631315718));
                c.brake.set(Controller.proportionalFeedforward(0.0189029258661836));

                c.headingFeedback.set(Controller.proportional(3.834647334616953));
                c.headingBrakeCoefficients.set(Vector2D.cartesian(0.06955463482893698, 0.0032877477064135));

                c.linearBrakeCoefficients.set(Matrix.diag(0.07678161491296444, 0.07998240109964999));
                c.quadraticBrakeCoefficients.set(Matrix.diag(0.0011862672426037088, 9.323569005014112E-4));

                c.maxAchievableForwardVelocity.set(54.0566708603022);
                c.maxAchievableStrafeVelocity.set(41.625122902758534);
                c.naturalForwardDeceleration.set(76.1042032405031);
                c.naturalStrafeDeceleration.set(62.69324507231072);
            }
    );
}