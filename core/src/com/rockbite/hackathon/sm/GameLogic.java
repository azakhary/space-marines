package com.rockbite.hackathon.sm;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.PooledEngine;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;
import com.rockbite.hackathon.sm.communications.Action;
import com.rockbite.hackathon.sm.communications.Comm;
import com.rockbite.hackathon.sm.communications.Network;
import com.rockbite.hackathon.sm.communications.Observer;
import com.rockbite.hackathon.sm.communications.actions.CardDrawn;
import com.rockbite.hackathon.sm.communications.actions.EmojiShown;
import com.rockbite.hackathon.sm.communications.actions.MinionUpdate;
import com.rockbite.hackathon.sm.communications.commands.SendEmoji;
import com.rockbite.hackathon.sm.components.CardComponent;
import com.rockbite.hackathon.sm.components.DeckComponent;
import com.rockbite.hackathon.sm.components.EmojiComponent;
import com.rockbite.hackathon.sm.components.GameComponent;
import com.rockbite.hackathon.sm.components.HeroComponent;
import com.rockbite.hackathon.sm.components.MinionComponent;
import com.rockbite.hackathon.sm.components.render.TransformComponent;
import com.rockbite.hackathon.sm.systems.CardSystem;
import com.rockbite.hackathon.sm.systems.EmojiSystem;
import com.rockbite.hackathon.sm.systems.GameSystem;
import com.rockbite.hackathon.sm.systems.HeroSystem;
import com.rockbite.hackathon.sm.systems.MinionSystem;
import com.rockbite.hackathon.sm.systems.SpellSystem;

import org.json.JSONObject;

public class GameLogic implements Observer  {
    private PooledEngine engine;

    private Entity gameEntity;
    private Entity[] players;

    private Array<Array<Entity>> cards;

    public int uniqueUserId;
    public int opponentUserId;

    public static final int BOTTOM_PLAYER = 0;
    public static final int TOP_PLAYER = 1;

    private Network network;

    private Assets assets;

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
        network = new Network();
        //initGameEntities();
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


        createDeck(1, 30);
    }

    public void dispose() {
        engine.removeSystem(engine.getSystem(GameSystem.class));
        engine.removeSystem(engine.getSystem(CardSystem.class));
        engine.removeSystem(engine.getSystem(SpellSystem.class));
        engine.removeSystem(engine.getSystem(MinionSystem.class));
        engine.removeSystem(engine.getSystem(HeroSystem.class));
        engine.removeSystem(engine.getSystem(EmojiSystem.class));

        network.dispose();
        System.out.println("socket disconnect");
    }

    @Override
    public void registerActionChannels() {
        Comm.get().registerObserver(this, EmojiShown.class);
        Comm.get().registerObserver(this, CardDrawn.class);
        Comm.get().registerObserver(this, MinionUpdate.class);
    }

    @Override
    public void onActionReceived(Action action) {
        if(action instanceof EmojiShown) {
            EmojiShown emojiShown = (EmojiShown) action;

            Entity emojiEntity = engine.createEntity();
            emojiEntity.add(new EmojiComponent(emojiShown));
            engine.addEntity(emojiEntity);
            System.out.println("emoji code: " + emojiShown.getEmojiCode() + ""); // TODO: remove this
        }

        if(action instanceof CardDrawn) {
            CardDrawn cardDrawn = (CardDrawn) action;

            Entity cardEntity = engine.createEntity();
            cardEntity.add(cardDrawn.getComponent());
            TransformComponent transformComponent = engine.createComponent(TransformComponent.class);
            transformComponent.width = 100f;
            transformComponent.height = 131f;
            cardEntity.add(transformComponent);
            engine.addEntity(cardEntity);

            System.out.println("draw card " + cardDrawn.getComponent().title);
            cardDrawn.setDoneDisplaying(true);
        }

        if(action instanceof MinionUpdate) {
            MinionUpdate minionUpdate = (MinionUpdate) action;

            engine.getSystem(MinionSystem.class).updateMinionData(minionUpdate);

            minionUpdate.setDoneDisplaying(true);
        }
    }

    public Network getNetwork() {
        return network;
    }

    public void createDeck(int user_id, int deck_size) {
        Entity deck = engine.createEntity();
        deck.add(new DeckComponent(user_id, deck_size));
        engine.addEntity(deck);

    }

    public void injectAssets(Assets assets) {
        this.assets = assets;
    }


    public Assets getAssets() {
        return assets;
    }

    public PooledEngine getEngine() {
        return engine;
    }

    public void summonMinion(int user_id, JSONObject minionJson) {
        Entity minion = engine.createEntity();
        MinionComponent minionComponent = engine.createComponent(MinionComponent.class);
        TransformComponent transformComponent = engine.createComponent(TransformComponent.class);
        transformComponent.reset();
        transformComponent.width = 120f;
        transformComponent.height = 120f * 1.31f;
        minionComponent.set(user_id, minionJson);
        minion.add(minionComponent);
        minion.add(transformComponent);
        engine.addEntity(minion);
    }
}