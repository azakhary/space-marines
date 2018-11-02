package com.rockbite.hackathon.sm.components;

import com.badlogic.ashley.core.Component;

public class CardComponent implements Component {

    public long playerId;

    public CardComponent(int playerId) {
        this.playerId = playerId;
    }
}
