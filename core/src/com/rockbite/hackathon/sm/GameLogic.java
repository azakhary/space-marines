package com.rockbite.hackathon.sm;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.utils.Array;
import com.rockbite.hackathon.sm.communications.Action;
import com.rockbite.hackathon.sm.communications.Comm;
import com.rockbite.hackathon.sm.communications.Observer;
import com.rockbite.hackathon.sm.communications.actions.EmojiShown;
import com.rockbite.hackathon.sm.communications.commands.SendEmoji;
import com.rockbite.hackathon.sm.components.CardComponent;
import com.rockbite.hackathon.sm.components.GameComponent;
import com.rockbite.hackathon.sm.components.HeroComponent;
import com.rockbite.hackathon.sm.systems.CardSystem;
import com.rockbite.hackathon.sm.systems.GameSystem;
import com.rockbite.hackathon.sm.systems.HeroSystem;
import com.rockbite.hackathon.sm.systems.MinionSystem;
import com.rockbite.hackathon.sm.systems.SpellSystem;

public class GameLogic implements Observer  {

    private Engine engine;

    private Entity gameEntity;
    private Entity[] players;

    private Array<Array<Entity>> cards;

    public static final int BOTTOM_PLAYER = 0;
    public static final int TOP_PLAYER = 1;

    public GameLogic(Engine engine) {
        this.engine = engine;

        GameSystem gameSystem = new GameSystem();
        CardSystem cardSystem = new CardSystem();
        SpellSystem spellSystem = new SpellSystem();
        MinionSystem minionSystem = new MinionSystem();
        HeroSystem heroSystem = new HeroSystem();

        engine.addSystem(gameSystem);
        engine.addSystem(cardSystem);
        engine.addSystem(spellSystem);
        engine.addSystem(minionSystem);
        engine.addSystem(heroSystem);

        registerActionChannels();

        //TODO: remove, this is for test
        //Comm.get().executeCommand(SendEmoji.make((short)11));
        //Comm.get().executeCommand(SendEmoji.make((short)13));
    }

    public void initGameSession() {
        /**
         * Initializing the game entity itself
         */
        gameEntity = new Entity();
        gameEntity.add(new GameComponent(10f));

        // need to create both opponents and their decks
        players = new Entity[2];
        players[BOTTOM_PLAYER] = new Entity();
        players[TOP_PLAYER] = new Entity();
        players[BOTTOM_PLAYER].add(new HeroComponent());
        players[TOP_PLAYER].add(new HeroComponent());

        //let's init their decks (30 cards per deck)
        cards = new Array<Array<Entity>>();
        cards.add(new Array<Entity>());
        cards.add(new Array<Entity>());

        // bottom player cards
        for(int i = 0; i < 30; i++) {
            Entity entity = new Entity();
            entity.add(new CardComponent(BOTTOM_PLAYER)); //TODO: change to actual ID
            cards.get(BOTTOM_PLAYER).add(entity);
        }

        // top player cards
        for(int i = 0; i < 30; i++) {
            Entity entity = new Entity();
            entity.add(new CardComponent(TOP_PLAYER)); //TODO: change to actual ID
            cards.get(TOP_PLAYER).add(entity);
        }
    }

    public void dispose() {
        engine.removeSystem(engine.getSystem(GameSystem.class));
        engine.removeSystem(engine.getSystem(CardSystem.class));
        engine.removeSystem(engine.getSystem(SpellSystem.class));
        engine.removeSystem(engine.getSystem(MinionSystem.class));
        engine.removeSystem(engine.getSystem(HeroSystem.class));
    }

    @Override
    public void registerActionChannels() {
        Comm.get().registerObserver(this, EmojiShown.class);
    }

    @Override
    public void onActionReceived(Action action) {
        if(action instanceof EmojiShown) {
            EmojiShown emojiShown = (EmojiShown) action;
            System.out.println(emojiShown.getEmojiCode() + "");

            emojiShown.setDoneDisplaying(true);
        }
    }
}