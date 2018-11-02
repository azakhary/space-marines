package com.rockbite.hackathon.sm;

import com.badlogic.ashley.core.Engine;
import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;

public class SpaceMarines extends ApplicationAdapter {
	Engine engine;

	GameLogic gameLogic;

	@Override
	public void create () {

		engine = new Engine();
		RenderSystem renderSystem = new RenderSystem();
		engine.addSystem(renderSystem);

		gameLogic = new GameLogic(engine);
	}

	@Override
	public void render () {
		engine.update(Gdx.graphics.getDeltaTime());
	}
	
	@Override
	public void dispose () {
		engine.removeAllEntities();
		engine.removeSystem(engine.getSystem(RenderSystem.class));
		gameLogic.dispose();
	}
}
