package com.rockbite.hackathon.sm.components;

import com.badlogic.ashley.core.Component;
import com.rockbite.hackathon.sm.communications.actions.EmojiShown;

public class EmojiComponent implements Component {

    public EmojiShown action;

    public float maxShowTime;

    public float currShowTime;

    public EmojiComponent(EmojiShown action) {
        this.action = action;
        maxShowTime = 5f;
        currShowTime = 0f;
    }

}
