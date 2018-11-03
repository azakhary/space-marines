package com.rockbite.hackathon.sm.communications.actions;

import com.rockbite.hackathon.sm.communications.Action;

import org.json.JSONException;
import org.json.JSONObject;

public class MinionAttackAnim extends Action {

    public int user_id;
    public int fromSlotId;
    public int toSlotId;

    public boolean isTargetHero = false;

    public void set(int user_id, JSONObject minionJson) {
        this.user_id = user_id;

        try {
            fromSlotId = minionJson.getInt("from_slot_id");
            toSlotId = minionJson.getInt("to_slot_id");
            isTargetHero = minionJson.getBoolean("target_hero");

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

}
