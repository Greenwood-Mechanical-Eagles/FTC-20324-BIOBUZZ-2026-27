# Pedro Visualizer paths

Exports from the [Pedro Visualizer](https://pedropathing.com/) live in this
folder, next to the code that uses them. Keeping the `.pp.json` means you can
reopen a path a month later and move one point, instead of laying the whole
thing out again from scratch.

| File | Used by | Status |
| --- | --- | --- |
| `ShootAndPark.pp.json` | `common/paths/ShootAndParkPaths` → `competition/auto/OfficialAutoRed` / `OfficialAutoBlue` | **Not yet driven.** See the two notes at the bottom |

## Workflow

1. Lay the path out in the Visualizer, on the **red** side, and run it there.
2. Export it and drop the `.pp.json` in this folder.
3. Put the numbers into a paths class — `common/paths/` if both alliances use it,
   or `students/<you>/<You>Paths.java` if it's yours.
4. The Auto calls `paths.startToShoot()` and never mentions a number.

Always lay paths out on the **red** side. Blue comes free — see below.

## Red and blue from one set of numbers

Every pose is typed once, for red. `common/Alliance` turns it into blue by
**rotating the whole plan 180° about the middle of the field**:

```
x        ->  144 - x
y        ->  144 - y
heading  ->  heading + 180
```

`Alliance.poses()` hands back a Pedro `PoseFactory` with that baked in, so a
paths class asks for red numbers and gets the right side out:

```java
PoseFactory poses = alliance.poses();
START = poses.of(56, 8.0197, 282.8);   // red:  (56,  8.0,  282.8)
                                       // blue: (88, 136.0, 102.8)
```

**Never type a second set of blue numbers.** One set means re-measuring the
field fixes both sides at once; two sets drift apart and only one ever gets
tested.

`PoseFactory` is immutable — `rotateAround()` returns a new factory — so blue
can never corrupt red.

### It is a rotation, not a mirror

This was originally written as a left-right mirror (`mirrorX`) and that was
**wrong**. Running it on the field on 2026-11-25 showed why: the blue Auto
crabbed right where it needed to crab left, and hit the teeter totter.

Two independent reasons rotation is correct:

1. **The alliances start at opposite ends of the field.** A rotation sends the
   start pose to the far wall (y = 8 becomes y = 136), which is where the other
   alliance actually begins. A mirror left both alliances starting at y = 8,
   against the same wall — physically wrong for a FIRST field.
2. **A rotation preserves the plan's handedness; a mirror reverses it.** Our
   Auto holds one heading and crabs sideways for its middle leg, so a flipped
   handedness sends that leg the wrong way round for one alliance. Under
   rotation both alliances have identical leg geometry relative to the robot
   (−13.5°, +77.5°, −9.0° off the shooter); under the mirror the middle leg
   came out +77.5° for red and −77.5° for blue.

The symptom is worth remembering because it is confusing: red was fine, blue
drove a plausible-looking path that was subtly inside-out, and all the telemetry
looked reasonable throughout.

### The centre of rotation

`Alliance.FIELD_CENTRE` is **72**, the exact middle of a 144 × 144 in field.

The HIVE shapes in `ShootAndPark.pp.json` imply 71.25: the red HIVE is drawn
24.7 in from centre and the blue one 23.2 in. Those were dragged into place by
hand and aren't measured field data, so 72 is the number to trust. If blue is
consistently ~1.5 in off while red is good, `FIELD_CENTRE` is the one constant
to change.

Aim after the fix, against each alliance's own HIVE as drawn:

| | Shoot pose | Shooter points | Aim error |
| --- | --- | --- | --- |
| RED | (56.27, 31.11) | 102.8° | −0.03° (0.02 in) |
| BLUE | (87.73, 112.89) | 282.8° | −2.76° (2.06 in) |

Blue's couple of inches is the hand-drawn HIVE asymmetry, not a transform fault.

## What this path does

Three segments, exactly as exported:

| # | Name | From → to | Heading |
| --- | --- | --- | --- |
| 1 | `MoveToShoot` | (56, 8.02) → (56.27, 31.11) | holds 90° |
| 2 | `ShootToCorner` | → (18.92, 30.90) | reverse tangent (~0.3°) |
| 3 | `CornerToPark` | → (14.69, 94.49) | reverse tangent (~273.8°) |

The Auto drives segment 1, settles, fires the opening volley, then drives
segments 2 and 3 to park. Segments 2 and 3 are driven backwards.

Both alliances come out in bounds with the same wall clearances (checked against
a 16 × 16 in robot; the tightest is the start pose, whose bumper sits on the
y = 0 wall as you'd expect for a wall start).## The shooting heading: corrected for a rear-firing shooter

**Measured on the robot 2026-11-25: the shooter fires out the BACK, and the
feeder servo was running backwards.** Both are fixed in code. The rear-firing
part changes the aiming maths completely.

Pollen leave along `SHOOT_HEADING + 180`, so **the robot must sit facing almost
directly away from the HIVE.** That is encoded once, as
`Launcher.AIM_OFFSET_DEG = 180`, and the aim heading is derived from it rather
than typed:

```
AIM_AT_HIVE_HEADING = HIVE_BEARING_DEG - Launcher.AIM_OFFSET_DEG
                    = 102.8 - 180
                    = 282.8
```

`SHOOT_HEADING` is now 282.8. Verified by building both alliances' poses:

| | Faces | Shot leaves along | HIVE bearing | Aim error |
| --- | --- | --- | --- | --- |
| RED | 282.8° | 102.8° | 102.8° | −0.03° |
| BLUE | 257.2° | 77.2° | 79.3° | +2.12° (1.5 in) |

Blue's 1.5 in is the hand-drawn HIVE asymmetry described above, not a mirror
fault — the mirror itself is exact.

For the record, the exported 90° was not a near miss. With a rear-firing
shooter it sent the pollen out along 270°, **167° away from the HIVE** — almost
exactly backwards. This is why `AIM_OFFSET_DEG` exists as a named constant: a
180° aiming error is uniquely hard to spot from telemetry, because every number
on the screen looks perfectly reasonable.

Still confirm it on a stand. 282.8 is derived from the HIVE as *drawn*, and
those shapes remain hand-placed. Put the robot on the shoot pose, fire, rotate
by hand until it scores, and read the heading off telemetry.

## The Auto: one small turn, then no turning

The robot is pushed **flush against the wall** — square, front to the wall,
shooter pointing up the field. That is `START_HEADING`, and it is 270 for red
(90 for blue after the rotation).

Leg 1 turns 12.8° from that flush heading to `SHOOT_HEADING` (282.8) on the way
up the field. After the shot the robot never turns again: legs 2 and 3 use
`constant()`, so it crabs and reverses to park holding one heading.

| Leg | Distance | Relative to the shooter | Motion |
| --- | --- | --- | --- |
| START → SHOOT | 23.1 in | −13.5° | reversing, turning 12.8° |
| SHOOT → CORNER | 37.3 in | +77.5° | **sideways crab** |
| CORNER → PARK | 63.7 in | −9.0° | reversing |

### START_HEADING is a measurement, not a choice

`follower.setPose(START)` does not steer the robot — it **defines where the
field frame is**. If that heading disagrees with how the robot is really
sitting, the entire path rotates by the difference, and nothing downstream can
detect or correct it. There is no feedback: telemetry will cheerfully report
whatever you told it.

This bit us. `START_HEADING` was briefly set to 282.8 so that Auto never had to
turn at all. But **282.8 is 12.8° off square, and you cannot place a robot at a
non-square heading by pushing it flush against a flat wall.** The robot really
sat at 270 while the code believed 282.8, which rotated the whole plan and put
the park roughly 21 in off — matching the miss measured on the field on
2026-11-25.

The rule that falls out of it: **a flush start can only ever be 90 or 270.** If
`START_HEADING` is anything else, either the robot is being placed by eye (bad,
and unrepeatable under match pressure) or the number is simply wrong.

### Note: the code and the .pp file handle headings differently

`ShootAndPark.pp.json` still carries the Visualizer's own heading settings —
`CornerToPark`, for instance, is tangential with Reverse ticked, so the
Visualizer shows the robot rotating to face along each leg.

The code deliberately overrides that with `constant()` after leg 1, because the
shooter is bolted to the back and we want it aimed, not swinging round to follow
the direction of travel. So **the simulation and the real robot will not look
the same**, and that is expected. The `.pp` file is the source of the *positions*;
the headings live in the code.

### The middle leg is the one compromise

Holding one heading means the 37 in trip towards the corner is covered almost
entirely sideways. Mecanum does this fine and it is not expensive — the
Visualizer's own robot profile puts strafing at 65 in/sec against 75 forward, so
about 13% — but it is the one leg that does not look like "backing up", so it
will surprise anyone watching.

**If it proves slow or drifty on the field, the fix is in the Visualizer, not
the code:** move PARK to roughly **(42, 94.5)** and the robot could reverse in a
straight line from the shooting spot all the way to park, dropping the sideways
leg entirely. Backing straight up from the shooting spot currently lands at
x ≈ 42, which is 27 in away from where PARK is drawn — that gap is the only
reason the corner leg exists.

## Known and accepted: the robot turns at each seam

Segment 1 holds a fixed heading; segments 2 and 3 take theirs from the direction
of travel, so the robot turns ~89.7° at the seam after the shot and ~86.5° at
the next one. Nothing collides.

It costs a couple of seconds, and the budget absorbs it comfortably:

| | Worst case |
| --- | --- |
| `settle` before the shot | 1.0 s |
| spin up (`SPIN_UP_TIMEOUT_MS`) | 4.0 s |
| volley (`VOLLEY_TIMEOUT_MS`) | 9.0 s |
| leg 1 plus the 12.8° turn | ~1.1 s |
| legs 2 and 3 (101 in) | ~3.4 s |
| **total** | **~19.5 s of 30** |

The volley ends on shot count rather than the clock, so it normally finishes far
inside that.

**The shooting phase is hard-capped too.** It runs inside Ivy's `race()` against
`ShootAndParkAuto.SHOOT_DEADLINE_MS` (14 s), so whatever goes wrong in there —
wheel never reaching speed, shot counter mis-tuned, a jam — Auto gives up and
drives to park. Parking scores; standing still does not. The deadline sits just
above the two inner timeouts, so in normal running it never fires.

One consequence while `SHOT_DIP_RPM` is still untuned: if shot counting does not
work, `fire()` always runs to its full timeout, which now costs 9 s rather than
4 s. That is the price of giving a working shot more room, and another reason to
do TESTPLAN gate 4 before trusting Auto timings.
