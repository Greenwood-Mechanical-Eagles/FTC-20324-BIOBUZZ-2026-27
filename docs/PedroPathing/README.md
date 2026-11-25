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

Every pose is typed once, for red. `common/Alliance` flips it for blue:

```
x        ->  144 - x      (mirrored across x = 72, the middle of the field)
y        ->  unchanged     (both alliances start at the same wall)
heading  ->  180 - heading
```

`Alliance.poses()` hands back a Pedro `PoseFactory` that has the flip baked in,
so a paths class just asks for red numbers and gets the right side out:

```java
PoseFactory poses = alliance.poses();
START = poses.of(56, 8.0197, 90);     // red: (56, 8.02, 90)
                                      // blue: (88, 8.02, 90)
```

**Never type a second set of blue numbers.** One set of numbers means re-measuring
the field fixes both sides at once; two sets means they quietly drift apart and
only one of them gets tested.

`PoseFactory` is immutable — `mirrorX()` returns a new factory — so blue can
never corrupt red.

### The mirror axis

`Alliance.MIRROR_AXIS_X` is **72**, the exact middle of a 144 × 144 in field.

The HIVE shapes in `ShootAndPark.pp.json` actually imply 71.25: the red HIVE is
drawn 24.7 in from centre and the blue one 23.2 in. Those shapes were dragged
into place by hand in the Visualizer and aren't measured field data, so 72 is
the number to trust. If blue is consistently ~1.5 in off while red is good,
`MIRROR_AXIS_X` is the one constant to change.

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

## Worth relaying the path: the robot now turns 167° on the spot

This is the cost of fixing the aim without touching the path. The start pose
faces 90° and the shoot pose now faces 282.8°, so the robot drives 23 in up the
field and rotates **167°** on arrival. It works, but it is slow and it is not
how anyone would lay this out on purpose.

The tidy fix is in the Visualizer, not the code: **have the robot start facing
roughly 282.8° and drive backwards off the wall to the shooting spot.** Segment
1 becomes a straight translation with no rotation at all, the shooter is already
pointed at the HIVE on arrival, and a second or two comes back into the Auto.

That means physically placing the robot at the wall facing down-field instead of
up-field, which is a decision about the robot and the start tile, not something
to change in code alone. `START_HEADING` is deliberately a separate constant
from `SHOOT_HEADING` so that this change is a one-line edit once it is decided.

## Known and accepted: the robot turns at each seam

Segment 1 holds a fixed heading; segments 2 and 3 take theirs from the direction
of travel, so the robot turns ~89.7° at the seam after the shot and ~86.5° at
the next one. Nothing collides.

It costs a couple of seconds, and the budget absorbs it comfortably:

| | Worst case |
| --- | --- |
| `settle` before the shot | 1.0 s |
| spin up (`SPIN_UP_TIMEOUT_MS`) | 3.0 s |
| volley (`VOLLEY_TIMEOUT_MS`) | 4.0 s |
| driving, 124 in total | ~4 s |
| two seam turns | ~2 s |
| **total** | **~14 s of 30** |

The volley ends on shot count rather than the clock, so it normally finishes well
inside that. If Auto ever does run short, giving segments 2 and 3 a constant
heading is the first thing to try.
