package com.rockbite.hackathon.sm.communications;

import com.badlogic.gdx.utils.Pool;

public abstract class Action implements Pool.Poolable {


    private boolean isDoneDisplaying = false;

    private boolean isDoneSending = false;

    public boolean isDoneDisplaying() {
        return isDoneDisplaying;
    }

    public void setDoneDisplaying(boolean doneDisplaying) {
        isDoneDisplaying = doneDisplaying;
    }

    public boolean isDoneSending() {
        return isDoneSending;
    }

    public void setDoneSending(boolean doneSending) {
        isDoneSending = doneSending;
    }

    @Override
    public void reset() {
        isDoneDisplaying = false;
        isDoneSending = false;
    }
}
