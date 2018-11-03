package com.rockbite.hackathon.sm.components.render;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.graphics.g2d.Sprite;

public class DrawableComponent implements Component {

    public NinePatch ninePatch;
    public Sprite sprite;


    public int index = 0;
}
