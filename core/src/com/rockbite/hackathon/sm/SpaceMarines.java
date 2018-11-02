package com.rockbite.hackathon.sm;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.PooledEngine;
import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.rockbite.hackathon.sm.communications.Comm;
import com.rockbite.hackathon.sm.communications.Command;
import com.rockbite.hackathon.sm.systems.RenderSystem;

public class SpaceMarines extends ApplicationAdapter {
	PooledEngine engine;

	GameLogic gameLogic;

	@Override
	public void create () {

		engine = new PooledEngine();


		gameLogic = new GameLogic(engine);

		RenderSystem renderSystem = new RenderSystem();
		engine.addSystem(renderSystem);

		gameLogic.initGameSession(); // TODO: this has to be done at other point when integrated with backend API
	}

	@Override
	public void render () {
		Comm.get().update();
		engine.update(Gdx.graphics.getDeltaTime());
	}
	
	@Override
	public void dispose () {
		engine.removeAllEntities();
		engine.removeSystem(engine.getSystem(RenderSystem.class));
		gameLogic.dispose();
	}
}
