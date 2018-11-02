package com.rockbite.hackathon.sm.communications.commands;

import com.rockbite.hackathon.sm.communications.Comm;
import com.rockbite.hackathon.sm.communications.Command;
import com.rockbite.hackathon.sm.communications.actions.EmojiShown;

import org.json.JSONException;
import org.json.JSONObject;

public class SendEmoji extends Command {

    private short emojiCode;

    public short getEmojiCode() {
        return emojiCode;
    }

    public void set(short emojiCode) {
        this.emojiCode = emojiCode;
    }

    @Override
    public void execute() {
        JSONObject payload = new JSONObject();
        try {
            payload.put("emoji_code", emojiCode);
            Comm.get().gameLogic.getSocket().emit("send_emoji", payload);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        EmojiShown action = Comm.get().getAction(EmojiShown.class);
        action.set(emojiCode);
        Comm.get().sendAction(action);
    }

    public static Command make(short code) {
        SendEmoji sendEmoji = Comm.get().getCommand(SendEmoji.class);
        sendEmoji.set(code);
        return sendEmoji;
    }
}
