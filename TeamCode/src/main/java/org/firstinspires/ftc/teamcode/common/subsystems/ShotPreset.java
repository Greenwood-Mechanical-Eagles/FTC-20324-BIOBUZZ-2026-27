package org.firstinspires.ftc.teamcode.common.subsystems;

/**
 * Flywheel speeds for each place we shoot from.
 * TeleOp and Auto both use these, so tuning a number here changes it everywhere.
 *
 * To add a new shot: add a line like  FAR_ZONE(3400),  and tune it on the field
 * with the D-pad in TeleOp (watch "Flywheel RPM" telemetry).
 * Keep every preset under about 3500 RPM so the motor can hold speed during a volley.
 */
public enum ShotPreset {
    LINE(2600),        // A button in TeleOp (tested)
    WALL(2800),        // B button in TeleOp (tested)
    CORNER(3600),      // Y button in TeleOp (tested)
    AUTO_START(2900);  // Auto opening shot. About the old -0.75 power. TUNE ON FIELD.

    public final double rpm;

    ShotPreset(double rpm) {
        this.rpm = rpm;
    }
}
