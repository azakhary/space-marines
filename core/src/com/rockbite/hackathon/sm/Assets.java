package com.rockbite.hackathon.sm;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.rockbite.hackathon.sm.communications.Comm;

import java.util.HashMap;

public class Assets {

    public TextureAtlas atlas;

    public BitmapFont font;
    public BitmapFont font_small;
    public Label label;

    public Label label_small;

    public Label.LabelStyle labelStyle;
    public Label.LabelStyle labelStyleSmall;

    public Music music;

    public HashMap<String, Sound> sounds = new HashMap<String, Sound>();

    public Assets() {
        atlas = new TextureAtlas(Gdx.files.internal("pack.atlas"));

        FreeTypeFontGenerator generator = new FreeTypeFontGenerator(Gdx.files.internal("roboto.ttf"));
        FreeTypeFontGenerator.FreeTypeFontParameter parameter = new FreeTypeFontGenerator.FreeTypeFontParameter();
        parameter.size = 22;
        parameter.borderColor = Color.BLACK;
        parameter.borderWidth = 4f;
        font = generator.generateFont(parameter); // font size 12 pixels

        parameter.size = 16;
        parameter.borderColor = Color.BLACK;
        parameter.borderWidth = 2f;
        font_small = generator.generateFont(parameter); // font size 12 pixels

        generator.dispose(); // don't forget to dispose to avoid memory leaks!

        label = new Label("aaa", new Label.LabelStyle(font, Color.WHITE));

        label_small = new Label("aaa", new Label.LabelStyle(font_small, Color.WHITE));


        labelStyle    =    new Label.LabelStyle(font, Color.WHITE);
        labelStyleSmall    =    new Label.LabelStyle(font_small, Color.WHITE);

        music = Gdx.audio.newMusic(Gdx.files.internal("bg-music.mp3"));
        music.setLooping(true);


        loadSound("add_card_to_hand_1");
        loadSound("add_card_to_hand_2");
        loadSound("add_card_to_hand_3");

        loadSound("minion_summon");

        loadSoundMp3("punch");

    }

    private void loadSound(String name) {
        Sound sound = Gdx.audio.newSound(Gdx.files.internal("eff/"+name+".ogg"));
        sounds.put(name, sound);
    }

    private void loadSoundMp3(String name) {
        Sound sound = Gdx.audio.newSound(Gdx.files.internal("eff/"+name+".mp3"));
        sounds.put(name, sound);
    }
}
