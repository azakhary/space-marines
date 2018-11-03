package com.rockbite.hackathon.sm.communications.actions;

import com.rockbite.hackathon.sm.communications.Action;

import org.json.JSONObject;

public class SummonMinion extends Action {

    public int user_id;

    public JSONObject minionJson;

    public void set(int user_id,JSONObject minionJson) {
        this.user_id = user_id;

        this.minionJson = minionJson;
    }
}
