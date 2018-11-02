package com.rockbite.hackathon.sm.systems;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.EntitySystem;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.utils.ImmutableArray;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.PolygonSpriteBatch;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.rockbite.hackathon.sm.communications.Comm;
import com.rockbite.hackathon.sm.components.CardComponent;
import com.rockbite.hackathon.sm.components.DeckComponent;
import com.rockbite.hackathon.sm.components.EmojiComponent;
import com.rockbite.hackathon.sm.components.MinionComponent;
import com.rockbite.hackathon.sm.components.render.TransformComponent;

import java.util.HashMap;

public class RenderSystem extends EntitySystem {

    public Viewport viewport;
    public PolygonSpriteBatch batch;


    // decks
    private ImmutableArray<Entity> deckEntities;
    private ImmutableArray<Entity> cardEntities;
    private ImmutableArray<Entity> minionEntities;

    private ComponentMapper<DeckComponent> dcMapper = ComponentMapper.getFor(DeckComponent.class);
    private ComponentMapper<MinionComponent> mcMapper = ComponentMapper.getFor(MinionComponent.class);
    private ComponentMapper<CardComponent> ccMapper = ComponentMapper.getFor(CardComponent.class);
    private ComponentMapper<TransformComponent> tcMapper = ComponentMapper.getFor(TransformComponent.class);


    public void addedToEngine(Engine engine) {
        deckEntities = engine.getEntitiesFor(Family.all(DeckComponent.class).get());
        cardEntities = engine.getEntitiesFor(Family.all(CardComponent.class).get());
        minionEntities = engine.getEntitiesFor(Family.all(MinionComponent.class).get());
    }

    public RenderSystem() {
        viewport = new FitViewport(6f, 10f);
        viewport.update(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        batch = new PolygonSpriteBatch();
    }

    @Override
    public void update (float deltaTime) {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        batch.setProjectionMatrix(viewport.getCamera().combined);

        batch.begin();

        renderDecks(deltaTime);

        renderHand(deltaTime);

        renderBoard(deltaTime);

        batch.end();
    }

    private void renderDecks(float deltaTime) {
        int currPlayerId = Comm.get().gameLogic.uniqueUserId;
        int opponentPlayerId = Comm.get().gameLogic.opponentUserId;

        for (int i = 0; i < deckEntities.size(); ++i) {
            DeckComponent component = dcMapper.get(deckEntities.get(i));
            if(component.playerId == currPlayerId) {
                // render current player deck
                batch.draw(Comm.get().gameLogic.getAssets().atlas.findRegion("card-back"), viewport.getWorldWidth()/2f - 1f - 0.2f, -viewport.getWorldHeight()/2f + 0.2f, 1f, 1.31f);
           }
        }
    }

    public void renderHand(float deltaTime) {
        int currPlayerId = Comm.get().gameLogic.uniqueUserId;
        int opponentPlayerId = Comm.get().gameLogic.opponentUserId;

        for (int i = 0; i < cardEntities.size(); ++i) {
            CardComponent card = ccMapper.get(cardEntities.get(i));
            TransformComponent transform = tcMapper.get(cardEntities.get(i));

            //if(component.playerId == currPlayerId) {
                // render current player deck
                batch.draw(Comm.get().gameLogic.getAssets().atlas.findRegion(card.id + "-front"), transform.x + transform.offsetX, transform.y+transform.offsetY, transform.width, transform.height);
            //}
        }
    }

    public void renderBoard(float deltaTime) {


        for (int i = 0; i < minionEntities.size(); ++i) {
            MinionComponent minion = mcMapper.get(minionEntities.get(i));
            TransformComponent transform = tcMapper.get(minionEntities.get(i));
            // render current player deck
             batch.draw(Comm.get().gameLogic.getAssets().atlas.findRegion(minion.id + "-board"), transform.x + transform.offsetX, transform.y+transform.offsetY, transform.width, transform.height);

        }
    }

    public void removedFromEngine (Engine engine) {
        batch.dispose();
    }
}
