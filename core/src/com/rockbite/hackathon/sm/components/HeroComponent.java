package com.rockbite.hackathon.sm.components;

import com.badlogic.ashley.core.Component;

public class HeroComponent implements Component {

    public int user_id;

    public String nm;

    public int hp;
    public int maxHP = 30;

    public HeroComponent(int user_id, String nm, int hp) {
        this.user_id = user_id;
        this.nm = nm;
        this.hp = hp;
    }
}
