package com.rockbite.hackathon.sm.systems;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.EntitySystem;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.utils.ImmutableArray;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.PolygonSpriteBatch;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.rockbite.hackathon.sm.communications.Comm;
import com.rockbite.hackathon.sm.components.CardComponent;
import com.rockbite.hackathon.sm.components.DeckComponent;
import com.rockbite.hackathon.sm.components.EmojiComponent;
import com.rockbite.hackathon.sm.components.GameComponent;
import com.rockbite.hackathon.sm.components.HeroComponent;
import com.rockbite.hackathon.sm.components.MinionComponent;
import com.rockbite.hackathon.sm.components.render.DrawableComponent;
import com.rockbite.hackathon.sm.components.render.TransformComponent;

import java.util.Comparator;
import java.util.HashMap;

public class RenderSystem extends EntitySystem {

    public Viewport viewport;
    public PolygonSpriteBatch batch;


    // decks
    private ImmutableArray<Entity> deckEntities;
    private ImmutableArray<Entity> cardEntities;
    private ImmutableArray<Entity> minionEntities;

    private ImmutableArray<Entity> heroEntities;

    private ImmutableArray<Entity> drawables;
    private Array<Entity> sortedDrawables = new Array<Entity>();

    private Comparator<Entity> comparator;

    private ComponentMapper<DeckComponent> dcMapper = ComponentMapper.getFor(DeckComponent.class);
    private ComponentMapper<MinionComponent> mcMapper = ComponentMapper.getFor(MinionComponent.class);
    private ComponentMapper<CardComponent> ccMapper = ComponentMapper.getFor(CardComponent.class);
    private ComponentMapper<TransformComponent> tcMapper = ComponentMapper.getFor(TransformComponent.class);
    private ComponentMapper<DrawableComponent> drawableMapper = ComponentMapper.getFor(DrawableComponent.class);
    private ComponentMapper<HeroComponent> heroMapper = ComponentMapper.getFor(HeroComponent.class);

    public void addedToEngine(Engine engine) {
        deckEntities = engine.getEntitiesFor(Family.all(DeckComponent.class).get());
        cardEntities = engine.getEntitiesFor(Family.all(CardComponent.class).get());
        minionEntities = engine.getEntitiesFor(Family.all(MinionComponent.class).get());
        drawables = engine.getEntitiesFor(Family.all(DrawableComponent.class).get());
        heroEntities = engine.getEntitiesFor(Family.all(HeroComponent.class).get());



        comparator = new Comparator<Entity>() {
            @Override
            public int compare(Entity o1, Entity o2) {
                return drawableMapper.get(o1).index -  drawableMapper.get(o2).index;
            }
        };

        sortedDrawables.clear();
        if (drawables.size() > 0) {
            for(int i = 0; i < drawables.size(); ++i) {
                sortedDrawables.add(drawables.get(i));
            }

            sortedDrawables.sort(comparator);
        }
    }

    public RenderSystem() {
        viewport = new FitViewport(600f, 1000f);
        viewport.update(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        batch = new PolygonSpriteBatch();
    }

    @Override
    public void update (float deltaTime) {
        Gdx.gl.glClearColor(57f/255f, 57f/255f, 57f/255f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        batch.setProjectionMatrix(viewport.getCamera().combined);

        batch.begin();

        renderDrawables(deltaTime);

        renderDecks(deltaTime);

        renderHand(deltaTime);

        renderBoard(deltaTime);

        renderHeroes(deltaTime);


        // render some additional labels
        if(Comm.get().gameLogic.gameEntity != null) {
            GameComponent game =Comm.get().gameLogic.gameEntity.getComponent(GameComponent.class);
            int mana = (int) Math.floor(game.mana);
            Label label = Comm.get().gameLogic.getAssets().label;
            label.setText(mana + "");
            label.setPosition(22 - viewport.getWorldWidth() / 2f, 15 - viewport.getWorldHeight() / 2f);
            label.draw(batch, 1f);



            int secondsLeft = (int) Math.floor(game.gameDuration - game.timePassed);
            int minutes = secondsLeft/60;
            int seconds = secondsLeft - minutes*60;
            label = Comm.get().gameLogic.getAssets().label;
            label.setText(minutes + " : " + seconds);
            label.setPosition(- label.getWidth()/2f - 10, viewport.getWorldHeight() / 2f - 10f - label.getHeight());
            label.draw(batch, 1f);
        }

        batch.end();
    }

    private void renderHeroes(float deltaTime) {
        for (int i = 0; i < heroEntities.size(); ++i) {
            HeroComponent hero = heroMapper.get(heroEntities.get(i));
            TransformComponent transform = tcMapper.get(heroEntities.get(i));

            batch.draw(Comm.get().gameLogic.getAssets().atlas.findRegion("hero-"+hero.nm), transform.x, transform.y, transform.width, transform.height);


            Label label = Comm.get().gameLogic.getAssets().label;
            label.setText(hero.hp+"/"+hero.maxHP);
            label.setPosition(transform.x+18, transform.y-15);
            label.draw(batch, 1f);

        }
    }

    private void renderDrawables(float deltaTime) {
        for (int i = 0; i < drawables.size(); ++i) {
            DrawableComponent drawable = drawableMapper.get(drawables.get(i));
            TransformComponent transform = tcMapper.get(drawables.get(i));

            if(drawable.ninePatch != null) {
                drawable.ninePatch.draw(batch, transform.x, transform.y, transform.width, transform.height);
            } else if(drawable.sprite != null) {
                drawable.sprite.setPosition(transform.x, transform.y);
                drawable.sprite.setSize(transform.width, transform.height);
                drawable.sprite.draw(batch, 1);
            }
        }
    }

    private void renderDecks(float deltaTime) {
        int currPlayerId = Comm.get().gameLogic.uniqueUserId;
        int opponentPlayerId = Comm.get().gameLogic.opponentUserId;

        for (int i = 0; i < deckEntities.size(); ++i) {
            DeckComponent component = dcMapper.get(deckEntities.get(i));
            if(component.playerId == currPlayerId) {
                // render current player deck
                batch.draw(Comm.get().gameLogic.getAssets().atlas.findRegion("deck"), viewport.getWorldWidth()/2f - 100f - 20f, -viewport.getWorldHeight()/2f + 20f, 100f, 131f);
           }
        }
    }

    public void renderHand(float deltaTime) {
        int currPlayerId = Comm.get().gameLogic.uniqueUserId;
        int opponentPlayerId = Comm.get().gameLogic.opponentUserId;

        for (int i = 0; i < cardEntities.size(); ++i) {
            CardComponent card = ccMapper.get(cardEntities.get(i));
            TransformComponent transform = tcMapper.get(cardEntities.get(i));

            if(card.playerId == currPlayerId) {
                // render current player deck
                float off = 10f;
                batch.draw(Comm.get().gameLogic.getAssets().atlas.findRegion(card.id + "-card"), transform.x + transform.offsetX+off, transform.y+transform.offsetY+off, transform.width-off-10, transform.height-off-10);
                batch.draw(Comm.get().gameLogic.getAssets().atlas.findRegion("card-hand"), transform.x + transform.offsetX, transform.y+transform.offsetY, transform.width, transform.height);

                // render title
                Label label = Comm.get().gameLogic.getAssets().label_small;
                label.setText(card.title);
                label.setPosition(transform.x + transform.offsetX + transform.width/2f - label.getWidth()/2f - 22f, transform.y+105 + transform.offsetY);
                label.draw(batch, 1f);

                // render cost
                label = Comm.get().gameLogic.getAssets().label;
                label.setText(card.cost+"");
                label.setPosition(transform.x+45 + transform.offsetX, transform.y+2 + transform.offsetY);
                label.draw(batch, 1f);
            }
        }
    }

    public void renderBoard(float deltaTime) {

        for (int i = 0; i < minionEntities.size(); ++i) {
            MinionComponent minion = mcMapper.get(minionEntities.get(i));
            TransformComponent transform = tcMapper.get(minionEntities.get(i));
            // render current player deck
            float off = 10f;
            batch.draw(Comm.get().gameLogic.getAssets().atlas.findRegion(minion.id + "-card"), transform.x + transform.offsetX+off, transform.y+transform.offsetY+off, transform.width-off-10, transform.height-off-10);
            batch.draw(Comm.get().gameLogic.getAssets().atlas.findRegion("card-board"), transform.x + transform.offsetX, transform.y+transform.offsetY, transform.width, transform.height);

            Label label  = Comm.get().gameLogic.getAssets().label;
            label.setText(minion.atk + "");
            label.setPosition(transform.x + transform.offsetX + 10f, transform.y+transform.offsetY);
            label.draw(batch, 1f);

            label  = Comm.get().gameLogic.getAssets().label;
            label.setText(minion.hp + "");
            label.setPosition(transform.x + transform.offsetX + 98f, transform.y+transform.offsetY);
            label.draw(batch, 1f);
        }
    }

    public void removedFromEngine (Engine engine) {
        batch.dispose();
    }
}
