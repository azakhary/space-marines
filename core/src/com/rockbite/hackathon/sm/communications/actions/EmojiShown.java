package com.rockbite.hackathon.sm.communications.actions;

import com.rockbite.hackathon.sm.communications.Action;

public class EmojiShown extends Action {

    private short emojiCode;

    public short getEmojiCode() {
        return emojiCode;
    }

    public void set(short emojiCode) {
        this.emojiCode = emojiCode;
    }
}
