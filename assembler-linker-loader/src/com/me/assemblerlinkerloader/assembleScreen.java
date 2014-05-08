package com.me.assemblerlinkerloader;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.files.FileHandle;
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

public class assembleScreen implements Screen {

	Sprite splash;
	SpriteBatch batch;
	int targetScreenWidth = 1280;
	int targetScreenHeight = 800;
	int screenHeight;
	int screenWidth;
    private Stage stage;
    Skin skin;
    String lastCode;
    int numFiles;
    int queriedFile;
    Game myGame;
    String codeOnScreen = "";
    private static final int GLOBAL = 0;
	private static final int LOCAL = 1;
	String[] fileName;
	Map<String, String> opcodeLengthTable = new HashMap<String, String>();
	Map<String, Integer> fileLength = new HashMap<String, Integer>();
	List<Map<String, Integer>> symbolTable = new ArrayList<Map<String, Integer>>();
	List<Map<String, Integer>> variableTable = new ArrayList<Map<String, Integer>>();
	List<Map<String, Integer>> variableScopeTable = new ArrayList<Map<String, Integer>>();
	boolean decodeAll = false;
	preprocess myPreprocessor;
	String fileIn = "";
	assembleScreen myAssembler = this;
	MainScreen myMainScreen;
    
    public assembleScreen(Game g, int x, MainScreen z) throws IOException
    {
    	myGame = g;
    	numFiles = x;
    	myMainScreen = z;
    	
    	fileName = new String[numFiles];
		
		for(int i = 0; i < numFiles; ++i)
		{
			fileName[i] = "code" + i + ".asm";
		}
		
		// Preprocessing
	    myPreprocessor = new preprocess(numFiles, fileName);
	    myPreprocessor.macroPreprocess();
	    myPreprocessor.opCodePreprocess();
	    
	    for(int i = 0; i < numFiles; ++i)
		{
			fileName[i] = "code" + i + ".asm";
			fileName[i] = fileName[i].split("\\.")[0] + "_pre.txt";
		}
		
		createSymbolTable();
		replaceTable();
		fileIn = "_table.txt";
		
    }
    
    List<Map<String, Integer>> getList(int i)
    {
    	if(i == 0)
    		return variableTable;
    	else if(i == 1)
    		return variableScopeTable;
    	else
    		return symbolTable;
    }
    
    
    void createSymbolTable() throws IOException
	{
		boolean myDecode = false;
		createLengthTable(myDecode) ;
		int i = 0;
		
		Map<String, Integer> symbolTableMap = new HashMap<String, Integer>();
		Map<String, Integer> variableTableMap = new HashMap<String, Integer>();
		Map<String, Integer> variableScopeTableMap = new HashMap<String, Integer>();
		
		
		for(int j = 0; j < numFiles; ++j)
		{
			String code = getFileContent(fileName[j]);
			fileLength.put(fileName[j], i);	
			i = 0;
			List<String> lines = Arrays.asList(code.split("\n"));	
			for(String line: lines)
			{
				if(myDecode || decodeAll)
				{
					System.out.println("line---------------");
					System.out.println(line);
					System.in.read();
				}
				line.trim();
				String tag = "";
				
				if(line.contains(":") && line.split(":").length > 0) // MAY GET SOME PROBLEM HERE  > 0
				{
					tag = line.split(":")[0].trim();
					if(myDecode || decodeAll)
					{
						System.out.println("Line split ; length > 1.. corres tag-----------------");
						System.out.println(tag);
						System.in.read();
					}
					symbolTableMap.put(tag, i);
				}
				if(line.contains("DS"))
				{
					tag = line.split("DS")[0].trim();
					tag = tag.split(" ")[tag.split(" ").length - 1];
					variableTableMap.put(tag, i);
					
					variableScopeTableMap.put(tag, scopeVariable(line));
					if(myDecode || decodeAll)
					{
						System.out.println("line contatins DS .. corres tag + scopeVariable--------------");
						System.out.println(tag + "  " + scopeVariable(line));
						System.in.read();
					}
					i = i + Integer.parseInt(line.split("DS")[1].trim());
				}
				if(line.contains("DB"))
				{
					tag = line.split("DB")[0].trim();
					tag = tag.split(" ")[tag.split(" ").length - 1];
					variableTableMap.put(tag, i);
					
					variableScopeTableMap.put(tag, scopeVariable(line));
					
					if(myDecode || decodeAll)
					{
						System.out.println("line contatins DB .. corres tag + scopeVariable--------------");
						System.out.println(tag + "  " + scopeVariable(line));
						System.in.read();
					}
					
					i = i + line.split(",").length;
				}
				String[] tags = line.split(" ");
				for(String tag2: tags)			
				{
					if(opcodeLengthTable.containsKey(tag2))
						i = i + Integer.parseInt(opcodeLengthTable.get(tag2));
				}
		}
			symbolTable.add(symbolTableMap);
			variableTable.add(variableTableMap);
			variableScopeTable.add(variableScopeTableMap);
			String tableFileName = "";
			tableFileName = fileName[j].split("_")[0]+"_table.txt";
			File tableFile = new File(tableFileName);
			 
			if (!tableFile.exists()) {
				tableFile.createNewFile();
			}
 
			FileWriter fw = new FileWriter(tableFile.getAbsoluteFile(), true);
			BufferedWriter bw = new BufferedWriter(fw);
			String symbols = "-------------SYMBOL-------------\n";
			for(String symbol: symbolTable.get(j).keySet())
			{
					symbols = symbols + symbol + "    " + symbolTable.get(j).get(symbol) + "\n";
			}
			symbols = symbols + "-------------SYMBOL-------------\n";
			String variables = "------------VARIABLE------------\n";
			for(String variable: variableScopeTable.get(j).keySet())
			{
					variables = variables + variable + "    " + variableTable.get(j).get(variable) + "    " + variableTable.get(j).get(variable) + "    " + ((variableScopeTable.get(j).get(variable)==1)?"LOCAL":"GLOBAL") + "\n";
			}
			variables = variables + "------------VARIABLE------------\n";
			bw.write(symbols + variables);
			if(myDecode || decodeAll)
			{
				System.out.println("Final Symbol + Variable String---------------");
				System.out.println(symbols + variables);
				System.in.read();
			}
			symbols = "";
			variables = "";
			bw.close();
		}
	}
	
	void replaceTable() throws IOException
	{
		boolean myDecode = false;
		int i = 0;
		for(int j = 0; j < numFiles; ++j)
		{
			String code = getFileContent(fileName[j]);
			List<String> lines = Arrays.asList(code.split("\n"));
			fileName[j] = fileName[j].split("\\.")[0];
			List<String> asCode = new ArrayList<String>();
			for(String line: lines)
			{
				line.trim();
				if(!line.equals(""))
				{
					if(line.contains(":"))
					{
						if(myDecode || decodeAll)
						{
							System.out.println("Line Contatins : ");
							System.out.println(line.split(":", 2)[1]);
							System.in.read();
						}
						line = line.split(":",2)[1];
					}
					if(line.contains("DS"))
					{
						if(myDecode || decodeAll)
						{
							System.out.println("Line Contatins DS ");
							System.out.println(line.split("DS", 2)[1]);
							System.in.read();
						}
						line = "DS " + line.split("DS",2)[1];
					}
					if(line.contains("DB"))
					{
						if(myDecode || decodeAll)
						{
							System.out.println("Line Contatins DB ");
							System.out.println(line.split("DB", 2)[1]);
							System.in.read();
						}
						line = "DB " + line.split("DB",2)[1];
					}
					String[] tags = line.split(" ");
					for(String tag: tags)
					{
						if(symbolTable.get(j).containsKey(tag))
						{
							line = line.replace(tag,"$" + symbolTable.get(j).get(tag));
						}
						else if(variableTable.get(j).containsKey(tag.split("\\+")[0].trim()))
						{
							String add = tag.split("\\+")[tag.split("\\+").length - 1];
							if(!isNumeric(add))
								add = "0";
							line = line.replace(tag,"$"+(variableTable.get(j).get(tag.split("\\+")[0].trim())+Integer.parseInt(add)));
						}
					}
					asCode.add(line.trim());
				}
			}
			String temporary = "";
			for(String temp: asCode)
			{	
				temporary += temp + "\n";
			}
			code =  temporary;
			
			fileName[j] = fileName[j].split("\\.")[0]+"_s.txt";
			File outputFile = new File(fileName[j]);
			if (!outputFile.exists()) {
				outputFile.createNewFile();
			}
 
			FileWriter fw = new FileWriter(outputFile.getAbsoluteFile());
			BufferedWriter bw = new BufferedWriter(fw);
			bw.write(code);
			
			if(myDecode || decodeAll)
			{
				System.out.println("Final Code printed after assemble pass2");
				System.out.println(code);
				System.in.read();
			}
			
			bw.close();
			i = i+1;
		}
	}

	int scopeVariable(String line)
	{
		if(line.contains("GLOBAL"))
			return GLOBAL;		
		else 
			return LOCAL;
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
	
	void createLengthTable(boolean myDecode) throws IOException
	{
		FileHandle file = Gdx.files.internal("data/config/opcodeslength.config");
		String opcode = file.readString();
		List<String> lines = Arrays.asList(opcode.split("\n"));
			
		for(String line: lines)
		{
			line = line.split(";")[0];
			line.trim();
			if(!line.equals(""))
			{
				String[] tags = line.split(" ");
				if(myDecode || decodeAll)
				{
					System.out.println("Length table first tag + second tag----------------");
					System.out.println(tags[0] + "  " + tags[1]);
					System.in.read();
				}
				opcodeLengthTable.put(tags[0], tags[1]);
			}
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
			codeOnScreen = getFileContent("code0_table.txt");
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
		
		final TextButton button = new TextButton("Assemble Pass 2", skin, "default");
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
            	if(fileIn.equals("_table.txt"))
            	{
            		fileIn = "_pre_s.txt";
            		button.setText("Link");
            		try {
						codeOnScreen = getFileContent("code0_pre_s.txt");
					} catch (IOException e) {
						e.printStackTrace();
					}
            		textArea.setText(codeOnScreen);
            	}
            	else if(fileIn.equals("_pre_s.txt"))
            	{
            		myGame.setScreen(new LinkScreen(myGame, numFiles, myAssembler));
            	}
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
