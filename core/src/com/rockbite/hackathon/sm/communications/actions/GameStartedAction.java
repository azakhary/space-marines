package com.rockbite.hackathon.sm.communications.actions;

import com.rockbite.hackathon.sm.communications.Action;

import org.json.JSONObject;

public class GameStartedAction extends Action {
    public int user_id;
    public float mana_speed;
    public float cooldown;
    public JSONObject obj;
}
