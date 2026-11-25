package org.firstinspires.ftc.teamcode.archive;

import com.pedropathing.ivy.Command;
import com.pedropathing.ivy.behaviors.EndCondition;
import com.qualcomm.robotcore.util.ElapsedTime;

public abstract class TrackedCommand {
    private boolean running = false;
    protected final ElapsedTime timer = new ElapsedTime();

    public final boolean isRunning() {
        return running;
    }

    protected void onStart() {}
    protected abstract void onExecute();
    protected abstract boolean isDone();
    protected void onEnd(EndCondition interrupted) {}

    public final Command execute() {
        return Command.build()
                .setStart(() -> {
                    running = true;
                    timer.reset();
                    onStart();
                })
                .setExecute(this::onExecute)
                .setDone(this::isDone)
                .setEnd(interrupted -> {
                    onEnd(interrupted);
                    running = false;
                });
    }
}
