package org.firstinspires.ftc.teamcode.competition.auto;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;

import org.firstinspires.ftc.teamcode.common.Alliance;

/**
 * The competition Auto, BLUE side. All the code is in ShootAndParkAuto.
 *
 * There are no blue field numbers anywhere: the red path is flipped
 * left-to-right by common/Alliance. Tune red and blue follows along.
 */
@Autonomous(name = "1 - Official Auto BLUE", group = "A Competition")
public class OfficialAutoBlue extends ShootAndParkAuto {

    @Override
    protected Alliance alliance() {
        return Alliance.BLUE;
    }
}
