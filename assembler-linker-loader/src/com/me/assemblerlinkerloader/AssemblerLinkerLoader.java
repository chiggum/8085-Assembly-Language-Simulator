package com.me.assemblerlinkerloader;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;

public class AssemblerLinkerLoader extends Game {
	@Override
	public void create() {		
		
		Gdx.graphics.setTitle("Assembler-linker-loader");
		setScreen(new MainScreen(this));
	}
	

	@Override
	public void dispose() {
		super.dispose();
	}

	@Override
	public void render() {		
		super.render();
	}

	@Override
	public void resize(int width, int height) {
		super.resize(width, height);
	}

	@Override
	public void pause() {
		super.pause();
	}

	@Override
	public void resume() {
		super.resume();
	}
}
