package com.rockbite.hackathon.sm.communications.commands;

import com.rockbite.hackathon.sm.communications.Comm;
import com.rockbite.hackathon.sm.communications.Command;
import com.rockbite.hackathon.sm.communications.actions.EmojiShown;

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
        Comm.get().gameLogic.getSocket().emit("send_emoji", "{'emoji_code': " + emojiCode + "}");

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
