package com.rockbite.hackathon.sm.communications.commands;

import com.rockbite.hackathon.sm.communications.Comm;
import com.rockbite.hackathon.sm.communications.Command;
import com.rockbite.hackathon.sm.communications.actions.EmojiShown;
import com.rockbite.hackathon.sm.components.CardComponent;

import org.json.JSONException;
import org.json.JSONObject;

public class PlayCard extends Command {

    private CardComponent cardComponent;

    @Override
    public void execute() {
        // send this to server
        JSONObject payload = new JSONObject();
        try {
            payload.put("card_slot", cardComponent.slot);
            Comm.get().gameLogic.getNetwork().getSocket().emit("play_card", payload);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public CardComponent getCardComponent() {
        return cardComponent;
    }

    public void setCardComponent(CardComponent cardComponent) {
        this.cardComponent = cardComponent;
    }
}
