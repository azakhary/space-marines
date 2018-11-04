package com.rockbite.hackathon.sm.communications.commands;

import com.rockbite.hackathon.sm.communications.Comm;
import com.rockbite.hackathon.sm.communications.Command;
import com.rockbite.hackathon.sm.communications.actions.EmojiShown;
import com.rockbite.hackathon.sm.components.CardComponent;

import org.json.JSONException;
import org.json.JSONObject;

public class PlayCard extends Command {

    private CardComponent cardComponent;

    public int targetSlot;
    public int targetPlayer;
    public int targetType = 0; // 0 for minion, 1 for hero

    @Override
    public void execute() {
        // send this to server
        JSONObject payload = new JSONObject();
        try {
            payload.put("card_slot", cardComponent.slot);
            payload.put("target_slot", targetSlot);
            payload.put("target_player", targetPlayer);
            payload.put("target_type", targetType);
            System.out.println("play card msg sent to server");
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
