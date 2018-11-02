package com.rockbite.hackathon.sm.communications.actions;

import com.badlogic.ashley.core.Component;
import com.rockbite.hackathon.sm.communications.Action;
import com.rockbite.hackathon.sm.components.CardComponent;

public class CardDrawn extends Action {
    private CardComponent card;

    public void setCard(CardComponent card) {
        this.card = card;
    }

    public CardComponent getComponent() {
        return card;
    }
}
