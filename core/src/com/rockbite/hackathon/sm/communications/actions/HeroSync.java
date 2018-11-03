package com.rockbite.hackathon.sm.communications.actions;

import com.rockbite.hackathon.sm.communications.Action;

import org.json.JSONException;
import org.json.JSONObject;

public class HeroSync extends Action {

    public int user_id;

    public int hp;
    public int maxHP;

    public float mana;

    public void set(int user_id, JSONObject data) {
        this.user_id = user_id;

        try {
            hp = data.getInt("hp");
            maxHP = data.getInt("max_hp");

            mana = (float) data.getDouble("mana");
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }
}
