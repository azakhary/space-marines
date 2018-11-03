package com.rockbite.hackathon.sm.components;

import com.badlogic.ashley.core.Component;

public class GameComponent implements Component {

    public float gameDuration;
    public float timePassed;

    public float maxMana = 10;
    public float mana = 0;

    public GameComponent(float duration) {
        gameDuration = duration;
        timePassed = 0;
    }
}
