package org.firstinspires.ftc.teamcode.competition.auto;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;

import org.firstinspires.ftc.teamcode.common.Alliance;

/**
 * The competition Auto, RED side. All the code is in ShootAndParkAuto.
 * This file exists only to put "red" on the Driver Station.
 */
@Autonomous(name = "1 - Official Auto RED", group = "A Competition")
public class OfficialAutoRed extends ShootAndParkAuto {

    @Override
    protected Alliance alliance() {
        return Alliance.RED;
    }
}
