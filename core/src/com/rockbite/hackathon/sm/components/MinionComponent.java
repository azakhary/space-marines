package com.rockbite.hackathon.sm.components;

import com.badlogic.ashley.core.Component;

import org.json.JSONException;
import org.json.JSONObject;

public class MinionComponent implements Component {

    public int user_id;

    public String id;
    public int atk;
    public int hp;

    public int slot;

    public void set(int user_id, JSONObject minionJson) {
        this.user_id = user_id;

        try {
            id = minionJson.getString("id");
            atk = minionJson.getInt("atk");
            hp = minionJson.getInt("hp");

            slot = minionJson.getInt("slot");

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
