package com.rockbite.hackathon.sm.components;

import com.badlogic.ashley.core.Component;

public class DeckComponent implements Component{

    public int deckSize;
    public int playerId;

    public DeckComponent(int playerId, int deckSize) {
        this.playerId = playerId;
        this.deckSize = deckSize;
    }
}
