package com.rockbite.hackathon.sm.communications;

import com.rockbite.hackathon.sm.communications.actions.EmojiShown;

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
            socket = IO.socket("http://10.10.29.151:5555");

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
                        System.out.println("room created with opponent id: " + user_id);
                        Comm.get().gameLogic.opponentUserId = user_id;
                        Comm.get().gameLogic.initGameEntities();
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
                public void call(Object... args) {}

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
