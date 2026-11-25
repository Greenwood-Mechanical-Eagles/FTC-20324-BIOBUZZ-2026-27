# archive/ — don't build on anything in here

These are the old OpModes, kept so nobody has to go digging through git history.
**Every OpMode in here is `@Disabled`**, so none of it shows up on the Driver
Station. Nothing in `common/` depends on any of it.

Don't copy from these files. Copy from your own student template, or read
`competition/auto/ShootAndParkAuto.java` and `competition/teleop/OfficialDrive.java`.

| File | Why it's here |
| --- | --- |
| `ShootAndPark.java` | Replaced by `competition/auto/ShootAndParkAuto`. Crashes on INIT: it calls `hardwareMap.get` in a field initializer, which runs before `hardwareMap` has anything in it. Shoots on raw power instead of RPM. |
| `ShootBallCommand.java`, `TrackedCommand.java` | The shooting command `ShootAndPark` used. Shot on raw `setPower(-0.75)` with no idea how fast the wheel was going. Replaced by `common/commands/RobotCommands` + `common/subsystems/Launcher`. |
| `launchAuto.java` | The old `sleep()`-timed Auto. Never worked. |
| `DriveBase.java` | A second way to drive the wheels, from before `Robot` owned the Pedro follower. Nothing uses it. Two classes writing the same four motors in the same loop makes the robot twitch. |
| `basicDrive.java`, `starterBotDrive.java` | Early TeleOps, superseded by `competition/teleop/OfficialDrive`. |
| `RapidFireDriveBlocks.java.txt` | First attempt at a `Robot`-based TeleOp. Kept as `.txt` so it isn't compiled, because it builds a `Robot` *and* a `DriveBase` and both fight over the wheels — exactly the mistake not to copy. |
| `RapidFireDrive.java.txt` | A stale copy of the TeleOp that used to sit loose in the project root, outside the build. Its numbers (F = 18, tolerance 150 RPM, rumble 200 ms) were **not** the ones that ran at the meet; the real values live in `common/subsystems/Launcher` (F = 16, tolerance 50 RPM). Kept only so those numbers aren't lost. |

## Deleting things from here

Fine to do, once nobody misses them. Git still has every version. Check nothing
imports a file before you delete it — `archive` should have no incoming
references from `common/`, `competition/` or `students/`.
