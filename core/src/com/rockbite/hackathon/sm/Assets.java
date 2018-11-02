package com.rockbite.hackathon.sm;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;

public class Assets {

    public TextureAtlas atlas;

    public Assets() {
        atlas = new TextureAtlas(Gdx.files.internal("pack.atlas"));
    }
}
