package com.rockbite.hackathon.sm.communications;

import com.badlogic.gdx.utils.Pool;

public abstract class Command implements Pool.Poolable{

    public abstract void execute();

    @Override
    public void reset() {

    }
}
