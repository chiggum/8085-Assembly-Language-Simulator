package com.me.assemblerlinkerloader;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextArea;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;

public class LinkScreen implements Screen {

	Screen myAssembler;
	Game myGame;
	int numFiles;
	String[] fileName;
	ArrayList<ArrayList<String>> externTable = new ArrayList<ArrayList<String>>();
	private static final int GLOBAL = 0;
	//private static final int LOCAL = 1;
	List<Integer> fileLengthTable = new ArrayList<Integer>();
	boolean decodeAll = false;
	int screenWidth;
	int screenHeight;
	int targetScreenWidth;
	int targetScreenHeight;
	Skin skin;
	SpriteBatch batch;
	int queriedFile;
	String codeOnScreen = "";
	Sprite splash;
	Stage stage;
	String fileIn = "";
	LinkScreen myLinker = this;
	
	public LinkScreen(Game g, int x, Screen z)
	{
		myGame = g;
		numFiles = x;
		myAssembler = z;
		
		fileName = new String[numFiles];
		for(int i = 0; i < numFiles; ++i)
		{
			fileName[i] = "code" + i + ".asm";
			fileName[i] = fileName[i].split("\\.")[0] + "_pre_s.txt";
		}
		fileIn = "_pre_s_l_8085.txt";
		
		try {
			linkCode();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	@SuppressWarnings("resource")
	public String getFileContent(String path) throws IOException {
		
	    BufferedReader reader = new BufferedReader(new FileReader(path));
	    String line = null;
	    String out = "";
	    while ((line = reader.readLine()) != null) {
	        out = out + line;
	        out = out + "\n";
	    }
	    return out;
	}
	
	void linkCode() throws IOException
	{
		boolean myDecode = false;
		List<String> asCode = new ArrayList<String>();
		int i = 0;
		ArrayList<String> externList;
		for(int j = 0; j < numFiles; ++j)
		{
			String code = getFileContent(fileName[j]);
			fileName[j] = fileName[j].split("\\.")[0];
			externList = new ArrayList<String>();
			List<String> lines = Arrays.asList(code.split("\n"));	
			for(String line: lines)
			{
				line.trim();
				if(!line.equals(""))
					if(line.contains("EXTERN"))
					{
						if(validExtern(line.split(" ")[1]))
							externList.add(line.split(" ")[1]);
						else
						{
							System.out.println("ERROR :" + line);
							System.out.println("Files Required for Linking not found");
							System.exit(0);
						}
					}
					else
					{
						asCode.add(line);
						i = i+1;
					}
			}
			externTable.add(externList);
			fileLengthTable.add(i);
			
			String temporary = "";
			for(String temp: asCode)
			{	
				temporary += temp + "\n";
			}
			code =  temporary;
			
			String outputFileName = fileName[j]+"_l_8085.txt";
			File outputFile = new File(outputFileName);
			if (!outputFile.exists()) {
				outputFile.createNewFile();
			}
 
			FileWriter fw = new FileWriter(outputFile.getAbsoluteFile());
			BufferedWriter bw = new BufferedWriter(fw);
			bw.write(code);
			if(myDecode || decodeAll)
			{
				System.out.println("Final Code printed after link1");
				System.out.println(code);
				System.in.read();
			}
			asCode = new ArrayList<String>();
			bw.close();
		}

		for(int j = 0; j < numFiles; ++j)
		{
			fileName[j] = fileName[j].split("\\.")[0];
			String inputFileName = fileName[j] + "_l_8085.txt";
			String code = getFileContent(inputFileName);
			List<String> lines = Arrays.asList(code.split("\n"));
			asCode = new ArrayList<String>();
			for(String line: lines)
			{
				line.trim();
				String temporary = "";
				int myInt = 0;
				for(String temp: line.split(" "))
				{
					if(myInt < 1)
						++myInt;
					else
						temporary += temp + "";
				}
				String[] tags = temporary.split(",");
				for(String tag: tags)
				{
					if(tagPresent(tag,j))
						line =  line.replace(tag, externAddress(tag)+'#'+tag);
				}
				asCode.add(line);
			}
			String temporary = "";
			for(String temp: asCode)
			{	
				temporary += temp + "\n";
			}
			code =  temporary;
			
			String outputFileName = fileName[j]+"_l_8085.txt";
			File outputFile = new File(outputFileName);
			if (!outputFile.exists()) {
				outputFile.createNewFile();
			}
 
			FileWriter fw = new FileWriter(outputFile.getAbsoluteFile());
			BufferedWriter bw = new BufferedWriter(fw);
			bw.write(code);
			if(myDecode || decodeAll)
			{
				System.out.println("Final Code printed after link2");
				System.out.println(code);
				System.in.read();
			}
			asCode = new ArrayList<String>();
			bw.close();
		}
	}
			
	String externAddress(String tag)
	{
		for(int j = 0; j < numFiles; ++j)
		{
			for(String extern: ((assembleScreen) myAssembler).getList(0).get(j).keySet())
			{
				if(extern.equals(tag.split("\\+")[0].trim()))
					return fileName[j].split("_")[0];
			}
		}
		return "";
	}
	boolean tagPresent(String tag, int j)
	{
		for(String extern: externTable.get(j))
		{
			if(extern.equals(tag.split("\\+")[0].trim()))
				return true;
		}
		return false;		
	}
	
	boolean validExtern(String tag)
	{
		for(int j = 0; j < numFiles; ++j)
		{
			for(String extern: ((assembleScreen) myAssembler).getList(1).get(j).keySet())
			{
				if(extern.equals(tag) && ((assembleScreen) myAssembler).getList(1).get(j).get(tag) == GLOBAL)
					return true;
			}
		}
		return false;
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
		
		try {
			codeOnScreen = getFileContent("code0_pre_s_l_8085.txt");
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		
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
		
		final TextButton button = new TextButton("Load", skin, "default");
		final TextButton assembleButton = new TextButton("Focus on Queried File", skin, "default");
		final TextButton resetButton = new TextButton("Reset", skin, "default");
		final TextArea textArea = new TextArea(codeOnScreen, skin, "default");
        final TextField txtFld = new TextField("Enter File Number Only", skin, "default");
        
        button.setWidth(screenWidth / 4);
        button.setHeight(screenHeight / 10);
        button.setPosition(Gdx.graphics.getWidth() - screenWidth / 4, Gdx.graphics.getHeight()-3 * screenHeight / 10- screenHeight / 40);
        button.setColor(0.1f, 0.4f, 0.8f, 1f);
        
        button.addListener(new ClickListener(){
            @Override 
            public void clicked(InputEvent event, float x, float y){
            	myGame.setScreen(new LoadScreen(myGame, numFiles, (assembleScreen) myAssembler));
            }
        });
        
        txtFld.setWidth(screenWidth / 4);
        txtFld.setHeight(screenHeight / 40);
        txtFld.setPosition(Gdx.graphics.getWidth() - screenWidth / 4, Gdx.graphics.getHeight()-screenHeight / 10 - screenHeight / 40);
        txtFld.setColor(0.1f, 0.4f, 0.8f, 1f);
        
        txtFld.addListener(new ClickListener(){
            @Override 
            public void clicked(InputEvent event, float x, float y){
            	txtFld.setText("");
                txtFld.getOnscreenKeyboard();
            }
        });
        
        assembleButton.setWidth(screenWidth / 4);
        assembleButton.setHeight(screenHeight / 10);
        assembleButton.setPosition(Gdx.graphics.getWidth() - screenWidth / 4, Gdx.graphics.getHeight()-screenHeight / 5 - screenHeight / 40);
        assembleButton.setColor(0.1f, 0.4f, 0.8f, 1f);
        
        assembleButton.addListener(new ClickListener(){
            @Override 
            public void clicked(InputEvent event, float x, float y){
            	if(!txtFld.getText().equals("Enter File Number Only") && !txtFld.getText().equals("") && isNumeric(txtFld.getText()))
            		queriedFile = Integer.parseInt(txtFld.getText());
            	if(queriedFile < numFiles)
            	{
	            	try {
						codeOnScreen = getFileContent("code" + queriedFile + fileIn);
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
            	}
            	else
            	{
            		codeOnScreen = "No Such File.. Max Index of file is: " + (numFiles - 1);
            	}
            	textArea.setText(codeOnScreen);
            }
        });
        
        textArea.setX(0);
		textArea.setY(0);
		textArea.setWidth(screenWidth - screenWidth / 4);
		textArea.setHeight(screenHeight);
		textArea.setDisabled(true);    
		
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
        
        stage.addActor(textArea);
        stage.addActor(button);
        stage.addActor(txtFld);
        stage.addActor(assembleButton);
        stage.addActor(resetButton);
	}
	
	public static boolean isNumeric(String str)
	{
	    for (char c : str.toCharArray())
	    {
	        if (!Character.isDigit(c))
	        	return false;
	    }
	    return true;
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
