package com.rockbite.hackathon.sm;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.PooledEngine;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;
import com.rockbite.hackathon.sm.communications.Action;
import com.rockbite.hackathon.sm.communications.Comm;
import com.rockbite.hackathon.sm.communications.Observer;
import com.rockbite.hackathon.sm.communications.actions.EmojiShown;
import com.rockbite.hackathon.sm.communications.commands.SendEmoji;
import com.rockbite.hackathon.sm.components.CardComponent;
import com.rockbite.hackathon.sm.components.EmojiComponent;
import com.rockbite.hackathon.sm.components.GameComponent;
import com.rockbite.hackathon.sm.components.HeroComponent;
import com.rockbite.hackathon.sm.systems.CardSystem;
import com.rockbite.hackathon.sm.systems.EmojiSystem;
import com.rockbite.hackathon.sm.systems.GameSystem;
import com.rockbite.hackathon.sm.systems.HeroSystem;
import com.rockbite.hackathon.sm.systems.MinionSystem;
import com.rockbite.hackathon.sm.systems.SpellSystem;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.URISyntaxException;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;

public class GameLogic implements Observer  {

    private PooledEngine engine;

    private Entity gameEntity;
    private Entity[] players;

    private Array<Array<Entity>> cards;

    private int uniqueUserId;

    private int opponentUserId;

    public static final int BOTTOM_PLAYER = 0;
    public static final int TOP_PLAYER = 1;

    private Socket socket;

    public GameLogic(PooledEngine engine) {
        this.engine = engine;

        GameSystem gameSystem = new GameSystem();
        CardSystem cardSystem = new CardSystem();
        SpellSystem spellSystem = new SpellSystem();
        MinionSystem minionSystem = new MinionSystem();
        HeroSystem heroSystem = new HeroSystem();
        EmojiSystem emojiSystem = new EmojiSystem();

        engine.addSystem(gameSystem);
        engine.addSystem(cardSystem);
        engine.addSystem(spellSystem);
        engine.addSystem(minionSystem);
        engine.addSystem(heroSystem);
        engine.addSystem(emojiSystem);

        registerActionChannels();
    }

    public void initGameSession() {
        // generate random user id
        uniqueUserId = MathUtils.random(1, 100000);

        // connect to server and ask for room
        try {
            socket = IO.socket("http://10.10.29.151:5555");

            socket.on(Socket.EVENT_CONNECT, new Emitter.Listener() {

                @Override
                public void call(Object... args) {
                    socket.emit("join", "{'user_id': " + uniqueUserId + "}");
                    socket.disconnect();
                }

            }).on("game_started", new Emitter.Listener() {

                @Override
                public void call(Object... args) {
                    JSONObject obj = (JSONObject)args[0];
                    try {
                        int user_id = obj.getInt("user_id");
                        opponentUserId = user_id;
                        initGameEntities();
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

    public void initGameEntities() {
        engine.removeAllEntities();

        /**
         * Initializing the game entity itself
         */
        gameEntity = engine.createEntity();
        gameEntity.add(new GameComponent(10f));
        engine.addEntity(gameEntity);

        // need to create both opponents and their decks
        players = new Entity[2];
        players[BOTTOM_PLAYER] = engine.createEntity();
        players[TOP_PLAYER] = engine.createEntity();
        players[BOTTOM_PLAYER].add(new HeroComponent());
        players[TOP_PLAYER].add(new HeroComponent());

        engine.addEntity(players[BOTTOM_PLAYER]);
        engine.addEntity(players[TOP_PLAYER]);

        //let's init their decks (30 cards per deck)
        cards = new Array<Array<Entity>>();
        cards.add(new Array<Entity>());
        cards.add(new Array<Entity>());

        // bottom player cards
        for(int i = 0; i < 30; i++) {
            Entity entity = engine.createEntity();
            entity.add(new CardComponent(BOTTOM_PLAYER)); //TODO: change to actual ID
            cards.get(BOTTOM_PLAYER).add(entity);
            engine.addEntity(entity);
        }

        // top player cards
        for(int i = 0; i < 30; i++) {
            Entity entity = engine.createEntity();
            entity.add(new CardComponent(TOP_PLAYER)); //TODO: change to actual ID
            cards.get(TOP_PLAYER).add(entity);
            engine.addEntity(entity);
        }
    }

    public void dispose() {
        engine.removeSystem(engine.getSystem(GameSystem.class));
        engine.removeSystem(engine.getSystem(CardSystem.class));
        engine.removeSystem(engine.getSystem(SpellSystem.class));
        engine.removeSystem(engine.getSystem(MinionSystem.class));
        engine.removeSystem(engine.getSystem(HeroSystem.class));
        engine.removeSystem(engine.getSystem(EmojiSystem.class));
    }

    @Override
    public void registerActionChannels() {
        Comm.get().registerObserver(this, EmojiShown.class);
    }

    @Override
    public void onActionReceived(Action action) {
        if(action instanceof EmojiShown) {
            EmojiShown emojiShown = (EmojiShown) action;

            Entity emojiEntity = engine.createEntity();
            emojiEntity.add(new EmojiComponent(emojiShown));
            engine.addEntity(emojiEntity);
            System.out.println(emojiShown.getEmojiCode() + ""); // TODO: remove this
        }
    }

    public Socket getSocket() {
        return socket;
    }
}