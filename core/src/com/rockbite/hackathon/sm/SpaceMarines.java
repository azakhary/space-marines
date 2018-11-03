package com.rockbite.hackathon.sm;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.PooledEngine;
import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.rockbite.hackathon.sm.communications.Comm;
import com.rockbite.hackathon.sm.communications.Command;
import com.rockbite.hackathon.sm.communications.commands.SendEmoji;
import com.rockbite.hackathon.sm.systems.RenderSystem;

public class SpaceMarines extends ApplicationAdapter {
	PooledEngine engine;

	GameLogic gameLogic;

	InputController inputController;

	@Override
	public void create () {

		GameStage stage = new GameStage();

		engine = new PooledEngine();

        inputController = new InputController();

		gameLogic = new GameLogic(engine);
		gameLogic.stage = stage;
		Assets assets = new Assets();

		RenderSystem renderSystem = new RenderSystem();
		engine.addSystem(renderSystem);
		Comm.injectGameLogic(gameLogic);
		gameLogic.injectAssets(assets);

		stage.init();
		gameLogic.initGameSession(); // TODO: this has to be done at other point when integrated with backend API
	}

	@Override
	public void render () {
		Comm.get().update();
		engine.update(Gdx.graphics.getDeltaTime());

        inputController.update(Gdx.graphics.getDeltaTime());

		gameLogic.stage.act();
		gameLogic.stage.draw();
	}
	
	@Override
	public void dispose () {
		engine.removeAllEntities();
		engine.removeSystem(engine.getSystem(RenderSystem.class));
		gameLogic.dispose();

		Comm.dispose();
	}
}
