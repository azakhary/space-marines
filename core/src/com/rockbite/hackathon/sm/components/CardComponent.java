package com.rockbite.hackathon.sm.components;

import com.badlogic.ashley.core.Component;

import org.json.JSONException;
import org.json.JSONObject;

public class CardComponent implements Component {

    public int playerId;

    public int slot;

    public String id;

    public String type;
    public String title;
    public int cost;
    public Minion minion;
    public Spell spell;

    public void load(int playerId, JSONObject cardJson) {
        this.playerId = playerId;

        try {
            id = cardJson.getString("id");
            type = cardJson.getString("type");
            title = cardJson.getString("title");

            cost = cardJson.getInt("cost");

            slot = cardJson.getInt("slot");

            if(type.equals("minion")) {
                minion = new Minion(cardJson.getJSONObject("minion"));
            } else if(type.equals("spell")) {
                spell = new Spell(cardJson.getJSONObject("spell"));
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public class Minion {

        int atk;
        int hp;

        public Minion(JSONObject minion) throws JSONException {
            atk = minion.getInt("atk");
            hp = minion.getInt("hp");
        }
    }

    public class Spell {

        public Spell(JSONObject spell) {

        }
    }
}
