package com.rockbite.hackathon.sm.communications.commands;

import com.rockbite.hackathon.sm.communications.Comm;
import com.rockbite.hackathon.sm.communications.Command;
import com.rockbite.hackathon.sm.components.MinionComponent;

import org.json.JSONException;
import org.json.JSONObject;

public class MinionAttack extends Command {

    public int fromSlot;

    public int targetSlot;
    public boolean isHero;

    @Override
    public void execute() {
        // send this to server
        JSONObject payload = new JSONObject();
        try {
            payload.put("from_slot", fromSlot);
            payload.put("target_slot", targetSlot);
            payload.put("is_target_hero", isHero);
            System.out.println("minion performed an attack to server");
            Comm.get().gameLogic.getNetwork().getSocket().emit("minion_attack", payload);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
