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

public class LoadScreen implements Screen {

	Game myGame;
	int numFiles;
	assembleScreen myAssembler;
	String[] fileName;
	int[] loadFile;
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
	Stage stage1;
	String fileIn = "";
	int numFilesLoaded;
	
	public LoadScreen(Game g, int x, assembleScreen z)
	{
		myGame = g;
		numFiles = x;
		fileName = new String[numFiles];
		
		for(int i = 0; i < numFiles; ++i)
		{
			fileName[i] = "code" + i + ".asm";
			fileName[i] = fileName[i].split("\\.")[0] + "_pre_s.txt";
		}
		
		myAssembler = z;
		loadFile = new int[numFiles];
		numFilesLoaded = 0;
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
	
	int indCorres2File(String file)
	{
		for(int j = 0; j < numFiles; ++j)
		{
			if(file.equals(fileName[j]))
				return j;
		}
		return -1;
	}
	
	void loadCode() throws IOException
	{
		int i = 0;
		List<String> asCode = new ArrayList<String>();
		boolean myDecode = false;
	/*	@SuppressWarnings("resource")
		Scanner in = new Scanner(System.in);
		
		for(int j = 0; j < numFiles; ++j)
		{
			fileName[j] = fileName[j].split("\\.")[0];
			System.out.print("Where to Load " + fileName[j] + " : ");
			int num = in.nextInt();
			loadFile[j] = num;
		}
*/
		//Generates file which run on Simulator
		for(int j = 0; j < numFiles; ++j)
		{
			fileName[j] = fileName[j].split("\\.")[0];
			String code = getFileContent(fileName[j] + "_l_8085.txt");
			List<String> lines = Arrays.asList(code.split("\n"));	
			for(String line: lines)
			{
				line.trim();
				if(!line.equals(""))
				{
					if(myDecode || decodeAll)
					{
						System.out.println("line is : -------------");
						System.out.println(line);
						System.in.read();
					}
					
					String[] tags = line.split(" ");
					for(String tag: tags) 
					{
						if(tag.contains("$"))
						{
							int val = Integer.parseInt(tag.split("\\$")[1])+loadFile[j]+myAssembler.fileLength.get(fileName[j].split("_")[0] + "_pre.txt");
							if(myDecode || decodeAll)
							{
								System.out.println("tag contains $.. tag split + loadfile + filelength + val-------------");
								System.out.println(tag.split("\\$")[1] + " , " + loadFile[j] + " , " + myAssembler.fileLength.get(fileName[j].split("_")[0] + "_pre.txt") + " , " + val);
								System.in.read();
							}
							line = line.replace(tag, Integer.toString(val));
						}
						if(tag.contains("#"))
						{
							String add = tag.split("#")[1];
							add = add.split("\\+")[add.split("\\+").length - 1];
							if(!isNumeric(add))
								add = "0";
							String lnFile = tag.split("#")[0];
							int val = loadFile[j]+myAssembler.fileLength.get(lnFile + "_pre.txt") + myAssembler.variableTable.get(indCorres2File(lnFile)).get(tag.split("#")[1].split("\\+")[0]) + Integer.parseInt(add); 
							line = line.replace(tag,Integer.toString(val));
						}
					}
					asCode.add(line.trim());
				}
			}		
		}
		String temporary = "";
		for(String temp: asCode)
		{	
			temporary += temp + "\n";
		}
		String code =  temporary;
		
		String outputFileName = fileName[0].split("\\.")[0]+"_s_8085.txt";
		File outputFile = new File(outputFileName);
		// if file doesn't exists, then create it
			if (!outputFile.exists()) {
				outputFile.createNewFile();
			}
 
			FileWriter fw = new FileWriter(outputFile.getAbsoluteFile());
			BufferedWriter bw = new BufferedWriter(fw);
			bw.write(code);
			if(myDecode || decodeAll)
			{
				System.out.println("Final Code printed after load1");
			System.out.println(code);
			System.in.read();
		}
		bw.close();

		// Generates file which represent vitrual memory
		asCode = new ArrayList<String>();
		for(int j = 0; j < numFiles; ++j)
		{
			fileName[j] = fileName[j].split("\\.")[0];
			code = getFileContent(fileName[j] + "_l_8085.txt");
			List<String> lines = Arrays.asList(code.split("\n"));	
			
			while(i != loadFile[j])
			{
				i = i+1;
				asCode.add("");
			}
				
			for(String line: lines)
			{
				line.trim();
				if(!line.equals(""))
				{
					String[] tags = line.split(" ");
					for(String tag: tags)
					{
						if(tag.contains("$"))
						{
							int val = Integer.parseInt(tag.split("\\$")[1]) + loadFile[j];
							line = line.replace(tag,Integer.toString(val));
						}
						if(tag.contains("#"))
						{
							String add = tag.split("#")[1];
							add = add.split("\\+")[add.split("\\+").length - 1];
							if(!isNumeric(add))
								add = "0";
							String lnFile = tag.split("#")[0];
							int val = loadFile[indCorres2File(lnFile)] + myAssembler.variableTable.get(indCorres2File(lnFile)).get(tag.split("#")[1].split("\\+")[0]) + Integer.parseInt(add);
							line = line.replace(tag,Integer.toString(val));
						}
					}
					asCode.add(line.trim());
				}
				i = i+1;
			}
		}
		temporary = "";
		for(String temp: asCode)
		{	
			temporary += temp + "\n";
		}
		code =  temporary;
		outputFileName = fileName[0].split("\\.")[0]+".8085";
		outputFile = new File(outputFileName);
		// if file doesn't exists, then create it
			if (!outputFile.exists()) {
				outputFile.createNewFile();
			}
 
			fw = new FileWriter(outputFile.getAbsoluteFile());
			bw = new BufferedWriter(fw);
			bw.write(code);
			if(myDecode || decodeAll)
			{
				System.out.println("Final Code printed after load2");
			System.out.println(code);
			System.in.read();
		}
		bw.close();
			
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
	public void render(float delta) {
		Gdx.gl.glClearColor(0, 0, 0, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		
		batch.begin();
		splash.draw(batch);
		batch.end();
		if(numFilesLoaded < numFiles)
			stage1.draw();
		else
			stage.draw();

	}

	@Override
	public void resize(int width, int height) {
		// TODO Auto-generated method stub

	}

	@Override
	public void show() {
		
		screenWidth = Gdx.graphics.getWidth();
		screenHeight = Gdx.graphics.getHeight();
		
		//skin = new Skin(Gdx.files.internal("data/uiskin.json"));
        stage = new Stage();
        stage1 = new Stage();
        Gdx.input.setInputProcessor(stage1);
		batch = new SpriteBatch();
		
		Texture splashTexture = new Texture(Gdx.files.internal("data/bg.png"));
		splash = new Sprite(splashTexture, 0, 0, targetScreenWidth, targetScreenHeight);
		splash.setSize(screenWidth, screenHeight);
		splash.setPosition(0, 0);
		
		skin = new Skin(Gdx.files.internal("data/uiskin.json"));
		
		TextButton.TextButtonStyle textStyle = new TextButton.TextButtonStyle();
		BitmapFont fnt = new BitmapFont();
		textStyle.font = fnt;
		
        final TextArea textArea = new TextArea("", skin, "default");
        final TextField txtFld = new TextField("Output 8085 Simulator File", skin, "default");
        final TextButton inputButton = new TextButton("Enter", skin, "default");
        final TextButton resetButton = new TextButton("Reset", skin, "default");
        final TextField msgFld = new TextField("Enter Where to Load file : " + numFilesLoaded, skin, "default");
        final TextField inputFld = new TextField("Enter Number here..", skin, "default");
        
        txtFld.setDisabled(true);
        txtFld.setWidth(screenWidth / 4);
        txtFld.setHeight(screenHeight / 40);
        txtFld.setPosition(Gdx.graphics.getWidth() - screenWidth / 4, Gdx.graphics.getHeight()-screenHeight / 10 - screenHeight / 40);
        txtFld.setColor(0.1f, 0.4f, 0.8f, 1f);

        textArea.setX(0);
		textArea.setY(0);
		textArea.setWidth(screenWidth - screenWidth / 4);
		textArea.setHeight(screenHeight);

        msgFld.setDisabled(true);
        msgFld.setWidth(screenWidth / 4);
        msgFld.setHeight(screenHeight / 40);
        msgFld.setPosition(Gdx.graphics.getWidth()/2 - screenWidth / 8, Gdx.graphics.getHeight()/2-screenHeight / 80 + screenHeight / 40);
        msgFld.setColor(0.1f, 0.4f, 0.8f, 1f);
        
        inputFld.setWidth(screenWidth / 4);
        inputFld.setHeight(screenHeight / 40);
        inputFld.setPosition(Gdx.graphics.getWidth()/2 - screenWidth / 8, Gdx.graphics.getHeight()/2-screenHeight / 80);
        inputFld.setColor(0.1f, 0.4f, 0.8f, 1f);
        
        inputFld.addListener(new ClickListener(){
            @Override 
            public void clicked(InputEvent event, float x, float y){
            	inputFld.setText("");
                inputFld.getOnscreenKeyboard();
            }
        });

        inputButton.setWidth(screenWidth / 4);
        inputButton.setHeight(screenHeight / 10);
        inputButton.setPosition(Gdx.graphics.getWidth()/2 - screenWidth / 8, Gdx.graphics.getHeight()/2-screenHeight / 80 - screenHeight / 10);
        inputButton.setColor(0.1f, 0.4f, 0.8f, 1f);
        
        inputButton.addListener(new ClickListener(){
            @Override 
            public void clicked(InputEvent event, float x, float y){
            	if(!inputFld.getText().equals("") && isNumeric(inputFld.getText()))
            	{
            		loadFile[numFilesLoaded++] = Integer.parseInt(inputFld.getText());
            		if(numFilesLoaded < numFiles)
            		{
            			msgFld.setText("Enter Where to Load file :" + numFilesLoaded);
            		}
            		else if(numFilesLoaded == numFiles)
            		{
            			try {
							loadCode();
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
            			try {
							codeOnScreen = getFileContent("code0_pre_s_s_8085.txt");
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
            			
            			Gdx.input.setInputProcessor(stage);
            			textArea.setText(codeOnScreen);
            		}
            	}
            	
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
        
        stage1.addActor(msgFld);
        stage1.addActor(inputButton);
        stage1.addActor(inputFld);
        
        stage.addActor(textArea);
        stage.addActor(txtFld);
        stage.addActor(resetButton);
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
