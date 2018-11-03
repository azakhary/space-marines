package com.rockbite.hackathon.sm;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.PooledEngine;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.rockbite.hackathon.sm.communications.Action;
import com.rockbite.hackathon.sm.communications.Comm;
import com.rockbite.hackathon.sm.communications.Network;
import com.rockbite.hackathon.sm.communications.Observer;
import com.rockbite.hackathon.sm.communications.actions.CardDrawn;
import com.rockbite.hackathon.sm.communications.actions.EmojiShown;
import com.rockbite.hackathon.sm.communications.actions.HeroSync;
import com.rockbite.hackathon.sm.communications.actions.MinionAttackAnim;
import com.rockbite.hackathon.sm.communications.actions.MinionUpdate;
import com.rockbite.hackathon.sm.communications.actions.SummonMinion;
import com.rockbite.hackathon.sm.communications.commands.SendEmoji;
import com.rockbite.hackathon.sm.components.CardComponent;
import com.rockbite.hackathon.sm.components.DeckComponent;
import com.rockbite.hackathon.sm.components.EmojiComponent;
import com.rockbite.hackathon.sm.components.GameComponent;
import com.rockbite.hackathon.sm.components.HeroComponent;
import com.rockbite.hackathon.sm.components.MinionComponent;
import com.rockbite.hackathon.sm.components.render.DrawableComponent;
import com.rockbite.hackathon.sm.components.render.TransformComponent;
import com.rockbite.hackathon.sm.systems.ActionSystem;
import com.rockbite.hackathon.sm.systems.CardSystem;
import com.rockbite.hackathon.sm.systems.EmojiSystem;
import com.rockbite.hackathon.sm.systems.GameSystem;
import com.rockbite.hackathon.sm.systems.HeroSystem;
import com.rockbite.hackathon.sm.systems.MinionSystem;
import com.rockbite.hackathon.sm.systems.RenderSystem;
import com.rockbite.hackathon.sm.systems.SpellSystem;

import org.json.JSONException;
import org.json.JSONObject;

public class GameLogic implements Observer  {

    public static float MAX_COOLDOWN = 10;
    public static float MANA_SPEED = 0.1f;

    public GameStage stage;

    private PooledEngine engine;

    public Entity gameEntity;
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
        ActionSystem actionSystem = new ActionSystem();

        engine.addSystem(actionSystem);
        engine.addSystem(gameSystem);
        engine.addSystem(cardSystem);
        engine.addSystem(spellSystem);
        engine.addSystem(minionSystem);
        engine.addSystem(heroSystem);
        engine.addSystem(emojiSystem);

        registerActionChannels();
    }

    private void initSomeDrawables() {
        Viewport viewport = engine.getSystem(RenderSystem.class).viewport;

        SpriteUtils.createSprite( engine, "bottom", -viewport.getWorldWidth()/2f, -viewport.getWorldHeight()/2f, 600, 355, 0);

        Entity top = SpriteUtils.createSprite( engine, "top", -viewport.getWorldWidth()/2f, viewport.getWorldHeight()/2f-247, 600, 247, 1);


        // test action
        //top.getComponent(TransformComponent.class).initActorIfNotInited();
        //top.getComponent(TransformComponent.class).addAction(Actions.moveTo(100, 100, 5f));


        // some fake UI
        // goes from 0 to 430
        Entity mana = SpriteUtils.createNinePatch(engine, "progress_body", -viewport.getWorldWidth()/2f + 45, -viewport.getWorldHeight()/2f + 10, 0, 30, 0.5f, 2);
        SpriteUtils.createNinePatch(engine, "progress_bg", -viewport.getWorldWidth()/2f + 10, -viewport.getWorldHeight()/2f + 5, 470, 40, 0.5f, 3);

        engine.getSystem(GameSystem.class).setManaEntity(mana);
    }

    public void initGameSession() {
        // generate random user id
        uniqueUserId = MathUtils.random(1, 100000);

        // connect to server and ask for room
        network = new Network();
        //initGameEntities();
    }

    public void initGameEntities(JSONObject data) {
        engine.removeAllEntities();

        String pBHero = "";
        String pTHero = "";
        int pB_hp = 0, pT_hp = 0, pB_max_hp = 0, pT_max_hp = 0;
        try {
            pBHero = data.getJSONObject("player_data").getString("hero");
            pTHero = data.getJSONObject("opponent_data").getString("hero");

            pB_hp = data.getJSONObject("player_data").getInt("hp");
            pT_hp = data.getJSONObject("opponent_data").getInt("hp");

            pB_max_hp = data.getJSONObject("player_data").getInt("max_hp");
            pT_max_hp = data.getJSONObject("opponent_data").getInt("max_hp");
        } catch (JSONException e) {
            e.printStackTrace();
        }


        /**
         * Initializing the game entity itself
         */
        gameEntity = engine.createEntity();
        gameEntity.add(new GameComponent(3 * 60f));
        engine.addEntity(gameEntity);

        // need to create both opponents and their decks
        players = new Entity[2];
        players[BOTTOM_PLAYER] = engine.createEntity();
        players[TOP_PLAYER] = engine.createEntity();

        players[BOTTOM_PLAYER].add(new HeroComponent(Comm.get().gameLogic.uniqueUserId, pBHero, pB_hp));
        players[TOP_PLAYER].add(new HeroComponent(Comm.get().gameLogic.opponentUserId, pTHero, pT_hp));

        TransformComponent tcB = engine.createComponent(TransformComponent.class);
        tcB.set(-55, -290, 115, 115*0.88f);
        players[BOTTOM_PLAYER].add(tcB);

        TransformComponent tcP = engine.createComponent(TransformComponent.class);
        tcP.set(-55, +300, 115, 115*0.88f);
        players[TOP_PLAYER].add(tcP);

        engine.addEntity(players[BOTTOM_PLAYER]);
        engine.addEntity(players[TOP_PLAYER]);

        //let's init their decks (30 cards per deck)
        cards = new Array<Array<Entity>>();
        cards.add(new Array<Entity>());
        cards.add(new Array<Entity>());


        createDeck(1, 30);


        initSomeDrawables();
    }

    public void dispose() {
        engine.removeSystem(engine.getSystem(GameSystem.class));
        engine.removeSystem(engine.getSystem(CardSystem.class));
        engine.removeSystem(engine.getSystem(SpellSystem.class));
        engine.removeSystem(engine.getSystem(MinionSystem.class));
        engine.removeSystem(engine.getSystem(HeroSystem.class));
        engine.removeSystem(engine.getSystem(EmojiSystem.class));
        engine.removeSystem(engine.getSystem(ActionSystem.class));

        network.dispose();
        System.out.println("socket disconnect");
    }

    @Override
    public void registerActionChannels() {
        Comm.get().registerObserver(this, EmojiShown.class);
        Comm.get().registerObserver(this, CardDrawn.class);
        Comm.get().registerObserver(this, MinionUpdate.class);
        Comm.get().registerObserver(this, HeroSync.class);
        Comm.get().registerObserver(this, MinionAttackAnim.class);
        Comm.get().registerObserver(this, SummonMinion.class);
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
            final CardDrawn cardDrawn = (CardDrawn) action;

            Entity cardEntity = engine.createEntity();
            cardEntity.add(cardDrawn.getComponent());
            TransformComponent transformComponent = engine.createComponent(TransformComponent.class);
            transformComponent.width = 100f;
            transformComponent.height = 131f;
            cardEntity.add(transformComponent);
            engine.addEntity(cardEntity);

            System.out.println("draw card " + cardDrawn.getComponent().title);

            transformComponent.initActorIfNotInited();
            transformComponent.actor.addAction(
                    Actions.sequence(
                            Actions.parallel(Actions.fadeIn(0.3f), Actions.scaleTo(1.2f, 1.2f, 0.3f, Interpolation.circleOut)),
                            Actions.run(new Runnable() {
                                @Override
                                public void run() {
                                    int rnd = MathUtils.random(1, 3);
                                    Comm.get().gameLogic.assets.sounds.get("add_card_to_hand_"+rnd).play(0.3f);
                                }
                            }),
                            Actions.scaleTo(1, 1, 0.15f),
                            Actions.delay(0.2f),
                            Actions.run(new Runnable() {
                                @Override
                                public void run() {
                                    cardDrawn.setDoneDisplaying(true);
                                }
                            })
                    )
            );
        }

        if(action instanceof MinionUpdate) {
            MinionUpdate minionUpdate = (MinionUpdate) action;

            engine.getSystem(MinionSystem.class).updateMinionData(minionUpdate);

            minionUpdate.setDoneDisplaying(true);
        }

        if(action instanceof HeroSync) {
            HeroSync heroSync = (HeroSync) action;

            engine.getSystem(HeroSystem.class).heroSync(heroSync);

            heroSync.setDoneDisplaying(true);
        }

        if(action instanceof SummonMinion) {
            final SummonMinion act = (SummonMinion) action;
            summonMinion(act.user_id, act.minionJson, act);
        }


        if(action instanceof MinionAttackAnim) {
            final MinionAttackAnim act = (MinionAttackAnim) action;

            Vector2 from = new Vector2();
            Vector2 to = new Vector2();

            Entity fromEntity = new Entity(), toEntity = new Entity();

            if(act.user_id == uniqueUserId) {
                // this is me, now do I hit hero or minion?
                if (!act.isTargetHero) {
                    // hitting minion, yey!! and it's me hitting him
                    fromEntity = engine.getSystem(MinionSystem.class).getFor(uniqueUserId, act.fromSlotId);
                    toEntity   = engine.getSystem(MinionSystem.class).getFor(opponentUserId, act.toSlotId);
                } else {
                    // hitting hero spookey
                    fromEntity = engine.getSystem(MinionSystem.class).getFor(uniqueUserId, act.fromSlotId);
                    toEntity = engine.getSystem(HeroSystem.class).getFor(opponentUserId);
                }
            } else if(act.user_id == opponentUserId) {
                // this is NOT me, now do I hit hero or minion?
                if (!act.isTargetHero) {
                    // hitting minion, yey!!
                    fromEntity = engine.getSystem(MinionSystem.class).getFor(opponentUserId, act.fromSlotId);
                    toEntity   = engine.getSystem(MinionSystem.class).getFor(uniqueUserId, act.toSlotId);
                } else {
                    // hitting hero spookey
                    fromEntity = engine.getSystem(MinionSystem.class).getFor(opponentUserId, act.fromSlotId);
                    toEntity = engine.getSystem(HeroSystem.class).getFor(uniqueUserId);
                }
            }

            from.set(fromEntity.getComponent(TransformComponent.class).x, fromEntity.getComponent(TransformComponent.class).y);
            to.set(toEntity.getComponent(TransformComponent.class).x, toEntity.getComponent(TransformComponent.class).y);

            fromEntity.getComponent(TransformComponent.class).initActorIfNotInited();
            fromEntity.getComponent(TransformComponent.class).addAction(Actions.sequence(
                    Actions.scaleTo(1.3f, 1.3f, 0.1f),
                    Actions.delay(0.10f),
                    Actions.parallel(Actions.moveTo(to.x, to.y, 0.15f), Actions.scaleTo(1, 1, 0.15f)),
                    Actions.run(new Runnable() {
                        @Override
                        public void run() {
                            Comm.get().gameLogic.assets.sounds.get("punch").play(0.3f);
                        }
                    }),
                    Actions.moveTo(from.x, from.y, 0.25f, Interpolation.circleOut),
                    Actions.delay(0.50f),
                    Actions.run(new Runnable() {
                        @Override
                        public void run() {
                            act.setDoneDisplaying(true);
                        }
                    })
            ));
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

    public void summonMinion(int user_id, JSONObject minionJson, final SummonMinion act) {
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

        engine.getSystem(MinionSystem.class).positionMinion(transformComponent, minionComponent);

        minion.getComponent(TransformComponent.class).initActorIfNotInited();
        minion.getComponent(TransformComponent.class).addAction(
                Actions.sequence(
                        Actions.run(new Runnable() {
                            @Override
                            public void run() {
                                Comm.get().gameLogic.assets.sounds.get("minion_summon").play(0.3f);
                            }
                        }),
                        Actions.parallel(Actions.fadeIn(0.3f), Actions.scaleTo(1.1f, 1.1f, 0.3f, Interpolation.circleOut)),
                        Actions.scaleTo(1, 1, 0.1f),
                        Actions.delay(0.2f),
                        Actions.run(new Runnable() {
                            @Override
                            public void run() {
                                act.setDoneDisplaying(true);
                            }
                        })
                )
        );

    }
}