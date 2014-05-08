package com.me.assemblerlinkerloader;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextArea;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;

public class MainScreen implements Screen {

	Sprite splash;
	SpriteBatch batch;
	int targetScreenWidth = 1280;
	int targetScreenHeight = 800;
	int screenHeight;
	int screenWidth;
    private Stage stage;
    Skin skin;
    String lastCode;
    int numFileCounter;
    Game myGame;
    MainScreen myMainScreen;
    
    public MainScreen(Game g)
    {
    	myGame = g;
    	myMainScreen = this;
    }
	
	@Override
	public void render(float delta) {
		Gdx.gl.glClearColor(0, 0, 0, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		
		batch.begin();
		splash.draw(batch);
		batch.end();
		
		stage.draw();
	}


	@Override
	public void show() {
		
		numFileCounter = 0;
		screenWidth = Gdx.graphics.getWidth();
		screenHeight = Gdx.graphics.getHeight();
		
        stage = new Stage();
        Gdx.input.setInputProcessor(stage);
		
        batch = new SpriteBatch();

		Texture splashTexture = new Texture(Gdx.files.internal("data/bg.png"));
		splash = new Sprite(splashTexture, 0, 0, targetScreenWidth, targetScreenHeight);
		splash.setSize(screenWidth, screenHeight);
		splash.setPosition(0, 0);
		
		skin = new Skin(Gdx.files.internal("data/uiskin.json"));
		
		final TextButton button = new TextButton("Save On Stack", skin, "default");
		final TextButton assembleButton = new TextButton("Assemble Pass 1", skin, "default");
		final TextButton resetButton = new TextButton("Reset", skin, "default");
		TextField.TextFieldStyle txtfldstyle = new TextField.TextFieldStyle();
		txtfldstyle.font = new BitmapFont();
		txtfldstyle.fontColor = new Color(0.5f, 0.5f, 0.5f, 1f);
		final TextArea textArea = new TextArea("Enter Your Code Here...", skin, "default");
        final TextField txtFld = new TextField("Status: No files on stack", txtfldstyle);
		
        
		button.setWidth(screenWidth / 4);
	    button.setHeight(screenHeight / 10);
	    button.setPosition(Gdx.graphics.getWidth() - screenWidth / 4, Gdx.graphics.getHeight()-screenHeight / 10);
	    button.setColor(0.1f, 0.4f, 0.8f, 1f);
		
        button.addListener(new ClickListener(){
            @Override 
            public void clicked(InputEvent event, float x, float y){
            	lastCode = textArea.getText();
            	try
            	{
	            	File outputFile = new File("code" + numFileCounter++ + ".asm");
	   			 
	    			// if file doesn't exists, then create it
	    			if (!outputFile.exists()) {
	    				outputFile.createNewFile();
	    			}
	     
	    			FileWriter fw = new FileWriter(outputFile.getAbsoluteFile());
	    			BufferedWriter bw = new BufferedWriter(fw);
	    			bw.write(lastCode);
	    			bw.close();
            	}
            	catch (IOException e) {
            		txtFld.setText("Error in Saving");
        			e.printStackTrace();
        		}
                txtFld.setText("Files Saved: " + "code" + (numFileCounter - 1)+ ".asm");
            }
        });
        
        resetButton.setWidth(screenWidth / 4);
        resetButton.setHeight(screenHeight / 10);
        resetButton.setPosition(Gdx.graphics.getWidth() - screenWidth / 4, 0);
        resetButton.setColor(0.1f, 0.4f, 0.8f, 1f);
        
        resetButton.addListener(new ClickListener(){
            @Override 
            public void clicked(InputEvent event, float x, float y){
            	myGame.setScreen(new MainScreen(myGame));
            }
        });
              
        txtFld.setWidth(screenWidth / 4);
        txtFld.setHeight(screenHeight / 40);
        txtFld.setPosition(Gdx.graphics.getWidth() - screenWidth / 4, Gdx.graphics.getHeight()-screenHeight / 10 - screenHeight / 40);
        txtFld.setColor(0.1f, 0.4f, 0.8f, 1f);
        txtFld.setDisabled(true);
        
        assembleButton.setWidth(screenWidth / 4);
        assembleButton.setHeight(screenHeight / 10);
        assembleButton.setPosition(Gdx.graphics.getWidth() - screenWidth / 4, Gdx.graphics.getHeight()-screenHeight / 5 - screenHeight / 40);
        assembleButton.setColor(0.1f, 0.4f, 0.8f, 1f);
        
        assembleButton.addListener(new ClickListener(){
            @Override 
            public void clicked(InputEvent event, float x, float y){
            	if(numFileCounter != 0)
            	{
	            	try {
						myGame.setScreen(new assembleScreen(myGame, numFileCounter, myMainScreen));
					} catch (IOException e) {
						e.printStackTrace();
					}
            	}
            	else
            	{
            		txtFld.setText("No File Saved Yet");
            	}
            }
        });
        
        textArea.setX(0);
		textArea.setY(0);
		textArea.setWidth(screenWidth - screenWidth / 4);
		textArea.setHeight(screenHeight);
		
        textArea.addListener(new ClickListener(){
            @Override 
            public void clicked(InputEvent event, float x, float y){
                textArea.getOnscreenKeyboard();
            }
        });
    
        stage.addActor(textArea);
        stage.addActor(button);
        stage.addActor(txtFld);
        stage.addActor(assembleButton);
        stage.addActor(resetButton);
	}

	@Override
	public void resize(int width, int height) {
		// TODO Auto-generated method stub

	}

	@Override
	public void hide() {
		// TODO Auto-generated method stub

	}

	@Override
	public void pause() {
		// TODO Auto-generated method stub

	}

	@Override
	public void resume() {
		// TODO Auto-generated method stub

	}

	@Override
	public void dispose() {
		batch.dispose();
		splash.getTexture().dispose();
		skin.dispose();
	}

}
