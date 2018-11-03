package com.rockbite.hackathon.sm.communications;

import com.rockbite.hackathon.sm.communications.actions.CardDrawn;
import com.rockbite.hackathon.sm.communications.actions.EmojiShown;
import com.rockbite.hackathon.sm.communications.actions.HeroSync;
import com.rockbite.hackathon.sm.communications.actions.MinionUpdate;
import com.rockbite.hackathon.sm.components.CardComponent;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.URISyntaxException;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;

public class Network {

    private Socket socket;

    public Network() {
        try {
            //socket = IO.socket("http://10.10.29.151:5555");
            socket = IO.socket("http://127.0.0.1:5555");

            socket.on(Socket.EVENT_CONNECT, new Emitter.Listener() {

                @Override
                public void call(Object... args) {
                    System.out.println("connected to socket. waiting for opponent.");

                    JSONObject payload = new JSONObject();
                    try {
                        payload.put("user_id", Comm.get().gameLogic.uniqueUserId);
                        socket.emit("join", payload);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

            }).on("game_started", new Emitter.Listener() {

                @Override
                public void call(Object... args) {
                    JSONObject obj = (JSONObject)args[0];
                    try {
                        int user_id = obj.getInt("user_id");
                        float mana_speed = (float) obj.getDouble("mana_speed");
                        System.out.println("room created with opponent id: " + user_id);
                        Comm.get().gameLogic.MANA_SPEED = mana_speed;
                        Comm.get().gameLogic.opponentUserId = user_id;
                        Comm.get().gameLogic.initGameEntities();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

            }).on("init_deck", new Emitter.Listener() {

                @Override
                public void call(Object... args) {
                    JSONObject obj = (JSONObject)args[0];
                    try {
                        int user_id = obj.getInt("user_id");
                        int deck_size = obj.getInt("deck_size");
                        System.out.println("deck info received: " + deck_size + " cards");
                        Comm.get().gameLogic.createDeck(user_id, deck_size);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                }
            }).on("summon_minion", new Emitter.Listener() {

                @Override
                public void call(Object... args) {
                    JSONObject obj = (JSONObject)args[0];
                    try {
                        int user_id = obj.getInt("user_id");
                        JSONObject minionJson = obj.getJSONObject("minion");
                        Comm.get().gameLogic.summonMinion(user_id, minionJson);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                }
            }).on("minion_update", new Emitter.Listener() {

                @Override
                public void call(Object... args) {
                    JSONObject obj = (JSONObject)args[0];
                    try {
                        int user_id = obj.getInt("user_id");
                        int slot_id = obj.getInt("slot_id");
                        JSONObject minionJson = obj.getJSONObject("minion");

                        MinionUpdate minionUpdate = Comm.get().getAction(MinionUpdate.class);
                        minionUpdate.set(user_id, slot_id, minionJson);
                        Comm.get().sendAction(minionUpdate);

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                }
            }).on("hero_sync", new Emitter.Listener() {

                @Override
                public void call(Object... args) {
                    JSONObject obj = (JSONObject)args[0];
                    System.out.println("hero sync response is received");
                    try {
                        int user_id = obj.getInt("user_id");
                        HeroSync action = Comm.get().getAction(HeroSync.class);
                        action.set(user_id, obj);
                        Comm.get().sendAction(action);

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }).on("draw_card", new Emitter.Listener() {

                @Override
                public void call(Object... args) {
                    JSONObject obj = (JSONObject)args[0];
                    try {
                        int user_id = obj.getInt("user_id");
                        JSONObject cardJson = obj.getJSONObject("card");

                        CardDrawn action = Comm.get().getAction(CardDrawn.class);
                        CardComponent component = Comm.get().gameLogic.getEngine().createComponent(CardComponent.class);
                        component.load(user_id, cardJson);
                        action.setCard(component);
                        Comm.get().sendAction(action);

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }).on("show_emoji", new Emitter.Listener() {

                @Override
                public void call(Object... args) {
                    JSONObject obj = (JSONObject)args[0];
                    short emojiCode = 0;
                    try {
                        emojiCode = (short) obj.getInt("emoji_code");
                        EmojiShown action = Comm.get().getAction(EmojiShown.class);
                        action.set(emojiCode);
                        Comm.get().sendAction(action);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

            }).on(Socket.EVENT_DISCONNECT, new Emitter.Listener() {

                @Override
                public void call(Object... args) {
                    System.out.println("server disconnected for no reason");
                }

            });
            socket.connect();

        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }

    public Socket getSocket() {
        return socket;
    }

    public void dispose() {
        socket.disconnect();
    }
}
