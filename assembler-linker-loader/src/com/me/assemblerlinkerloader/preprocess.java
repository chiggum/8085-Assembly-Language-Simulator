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
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.files.FileHandle;

public class preprocess implements Screen {
	String[] fileName;
	int numFiles;
	List<String> macroTable = new ArrayList<String>();
	List<String> macroCode = new ArrayList<String>();
	List<String> opcodeTable = new ArrayList<String>();
	List<String> opcode = new ArrayList<String>();
	Map<Integer, String> macroMap = new HashMap<Integer, String>();
	Map<Integer, String> opMap = new HashMap<Integer, String>();
	boolean decodeAll = false;
	
	public preprocess(int x, String[] y)
	{
		numFiles = x;
		fileName = new String[numFiles];
		
		for(int i = 0; i < numFiles; ++i)
		{
			fileName[i] = y[i];
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
	
	void createMacroTable() throws IOException
	{
		boolean myDecode = false;
		int counter = 0;
		for(int i = 0; i < numFiles; ++i)
		{
			String code = getFileContent(fileName[i]);
			List<String> lines = Arrays.asList(code.split("\n"));
			String lastMacro = "";
			List<String> expCode = new ArrayList<String>();
			List<String> asCode = new ArrayList<String>();
			int flag = 0;
			for(String line: lines)
			{
				line = line.split(";")[0];
				line.trim();
				
				if(myDecode || decodeAll)
				{
					System.out.println("line after splitting about ; -------------");
					System.out.println("@@@" + line + "@@@");
					System.in.read();
				}
				
				if(line.contains("MACRO"))
				{
					lastMacro = line.split(" ")[1];
					macroMap.put(counter++, lastMacro);
					String temporary = "";
					int myInt = 0;
					for(String temp: line.split(" "))
					{
						if(myInt < 2)
							++myInt;
						else
							temporary += temp + " ";
					}
					macroTable.add(temporary);
					flag = 1;
				}
				else if(line.contains("MEND"))
				{
					String temporary = "";
					for(String temp: expCode)
					{
						temporary += temp + "\n";
					}
					macroCode.add(temporary);
					flag = 0;
					expCode = new ArrayList<String>();
					lastMacro = "";
				}
				else if(flag == 1)
				{
					expCode.add(line);
				}
				else
				{
					if(!line.equals(""))
					{
						if(myDecode || decodeAll)
						{
							System.out.println("line into asCode -------------");
							System.out.println("@@@" + line + "@@@");
							System.in.read();
						}
						asCode.add(line);
					}
				}
			}
			String temporary = "";
			for(String temp: asCode)
			{	
				temporary += temp + "\n";
			}
			code =  temporary;
			fileName[i] = fileName[i].split("\\.")[0] + "_pre.txt";
			File outputFile = new File(fileName[i]);
			 
			if (!outputFile.exists()) {
				outputFile.createNewFile();
			}
 
			FileWriter fw = new FileWriter(outputFile.getAbsoluteFile());
			BufferedWriter bw = new BufferedWriter(fw);
			bw.write(code);
			bw.close();
			
			String tableFileName = "";
			tableFileName = fileName[i].split("_")[0]+"_table.txt";
			File tableFile = new File(tableFileName);
			 
			if (!tableFile.exists()) {
				tableFile.createNewFile();
			}
 
			fw = new FileWriter(tableFile.getAbsoluteFile());
			bw = new BufferedWriter(fw);		
			
			String[] macroTableArray = macroTable.toArray(new String[macroTable.size()]);
			String[] macroCodeArray = macroCode.toArray(new String[macroCode.size()]);
			String mcode = "-------------MACROS-------------\n";
			int j = 0;
			for(String macros: macroTableArray)
			{
				mcode = mcode + macroMap.get(j++) + "\t" + macros;
				mcode = mcode + "\n" + macroCodeArray[j - 1] + "\n";
			}
			mcode = mcode + "-------------MACROS-------------\n";
			bw.write(mcode);
			bw.close();
		}
	}
	
	void replaceMacros() throws IOException
	{
		for(int i = 0; i < numFiles; ++i)
		{
			Boolean replacements = true;
			while(replacements)
			{
				replacements = false;
				String code = getFileContent(fileName[i]).toUpperCase();
				List<String> lines = Arrays.asList(code.split("\n"));
				List<String> asCode = new ArrayList<String>();
				for(String line: lines)
				{
					line.trim();
					int tag = macroPresent(line);
					
					if(tag != -1)
					{
						String temporary = "";
						int myInt = 0;
						for(String temp: line.split(macroMap.get(tag)))
						{
							if(myInt < 1)
								++myInt;
							else
								temporary += temp + "";
						}
						String[] pams =  temporary.trim().split(",");
						Map<String, String> tag_pam = new HashMap<String, String>();
						tag_pam = macroMapping(tag,pams);
						line = macroCode.get(tag);
						Iterator<Entry<String, String>> it = tag_pam.entrySet().iterator();
					    while (it.hasNext()) {
					        @SuppressWarnings("rawtypes")
							Map.Entry pairs = (Map.Entry)it.next();
					        System.out.print((String)pairs.getKey() + "  " + (String)pairs.getValue());
					        line = line.replace((String)pairs.getKey(), (String)pairs.getValue());
					        it.remove(); 
					    }
						replacements = true;
					}
					if(!line.equals(""))
					{
						asCode.add(line);
					}
				}
				String temporary = "";
				for(String temp: asCode)
				{	
					temporary += temp + "\n";
				}
				code =  temporary;
				File outputFile = new File(fileName[i]);
				 
				if (!outputFile.exists()) {
					outputFile.createNewFile();
				}
	 
				FileWriter fw = new FileWriter(outputFile.getAbsoluteFile());
				BufferedWriter bw = new BufferedWriter(fw);
				bw.write(code);
				bw.close();
			}
		}
	}
	
	void macroPreprocess() throws IOException
	{
		createMacroTable();
		replaceMacros();
	}
	
	void createOpCodeTable() throws IOException
	{
		boolean myDecode = false; 
		FileHandle file = Gdx.files.internal("data/config/opcodes.config");
		String code = file.readString();
		
		if(myDecode || decodeAll)
		{
			System.out.println("Complete Code-------------");
			System.out.println(code);
			System.in.read();
		}
		
		List<String> lines = Arrays.asList(code.split("\n"));
		
		if(myDecode || decodeAll)
		{
			System.out.println("All lines-------------");
			System.out.println(lines);
			System.in.read();
		}
		
		String lastOpcode = "";
		List<String> expCode = new ArrayList<String>();
		int flag = 0;
		int counter = 0;
		for(String line: lines)
		{
			line.trim();
			if(line.contains("OPCODE"))
			{
				lastOpcode  = line.split(" ")[1];
				lastOpcode.trim();
				opMap.put(counter++, lastOpcode);
				
				if(myDecode || decodeAll)
				{
					System.out.println("Last OpCode-------------");
					System.out.println(lastOpcode);
					System.in.read();
				}
				
				String temporary = "";
				int myInt = 0;
				for(String temp: line.split(" "))
				{
					if(myInt < 2)
						++myInt;
					else
						temporary += temp + " ";
				}
				
				if(myDecode || decodeAll)
				{
					System.out.println("Argumetns to Opcode-------------------");
					System.out.println(temporary);
					System.in.read();
				}
				
				opcodeTable.add(temporary);
				flag = 1;
			}
			else if(line.contains("OPEND"))
			{
				String temporary = "";
				for(String temp: expCode)
				{
					temporary += temp + "\n";
				}
				
				if(myDecode || decodeAll)
				{
					System.out.println("opcode corresponding to last opcode");
					System.out.println(temporary);
					System.in.read();
				}
				
				opcode.add(temporary);
				flag = 0;
				expCode = new ArrayList<String>();
				lastOpcode  = "";
			}
			else if(flag == 1)
				expCode.add(line);
		}
		System.out.print("DONE3");
		if(myDecode || decodeAll)
		{
			System.out.println("opcode table formed---------------------");
			System.out.println(opcodeTable);
			System.in.read();
			
			System.out.println("opcode mapping ------------------------");
			System.out.println(opMap);
			System.in.read();
		}
	}
	
	void replaceOpCodes() throws IOException
	{
		boolean myDecode = false; 
		for(int i = 0; i < numFiles; ++i)
		{
			Boolean replacements = true;
			while(replacements)
			{
				replacements = false;
				String code = getFileContent(fileName[i]);
				List<String> lines = Arrays.asList(code.split("\n"));
				List<String> asCode = new ArrayList<String>();
				for(String line: lines)
				{
					line = line.trim();
					int tag = -1;
					tag = opcodePresent(line);
					
					if(myDecode || decodeAll)
					{
						System.out.println("tag + line + opMapValue at corresponding tag----------------");
						System.out.println(tag + "  " + line + "  " + opMap.get(tag));
						System.in.read();
					}
					
					if(tag != -1)
					{
						String temporary = "";
						int myInt = 0;
						for(String temp: line.split(opMap.get(tag)))
						{
							if(myInt < 1)
								++myInt;
							else
								temporary += temp;
						}
						
						if(myDecode || decodeAll)
						{
							System.out.println("Parameters string temporary----------------");
							System.out.println(temporary);
							System.in.read();
						}
						
						String[] pams =  temporary.split(",");
						
						if(myDecode || decodeAll)
						{
							System.out.println("Splitted Parameters----------------");
							System.out.println(pams);
							System.in.read();
						}
						
						Map<String, String> tag_pam = new HashMap<String, String>();
						
						tag_pam = opMapping(tag, pams, myDecode);
						line = opcode.get(tag);
						
						if(myDecode || decodeAll)
						{
							System.out.println("Opcode corres. to tag--------");
							System.out.println(line);
							System.in.read();
						}
						
						Iterator<Entry<String, String>> it = tag_pam.entrySet().iterator();
					    while (it.hasNext()) 
					    {
					        @SuppressWarnings("rawtypes")
							Map.Entry pairs = (Map.Entry)it.next();
					        line = line.replace((String)pairs.getKey(), (String)pairs.getValue());
					        it.remove(); // avoids a ConcurrentModificationException
					    }
					    
					    if(myDecode || decodeAll)
						{
							System.out.println("new Opcode after parameters replaced--------");
							System.out.println(line);
							System.in.read();
						}
					    
						replacements = true;
					}
					line = variablePresent(line);
					
					if(myDecode || decodeAll)
					{
						System.out.println("new Opcode after parameters replaced--------");
						System.out.println(line);
						System.in.read();
					}
					
					if(!line.equals(""))
						asCode.add(line);
				}
				String temporary = "";
				for(String temp: asCode)
				{	
					temporary += temp + "\n";
				}
				code =  temporary;
				
				File outputFile = new File(fileName[i]);
				 
				if (!outputFile.exists()) {
					outputFile.createNewFile();
				}
	 
				FileWriter fw = new FileWriter(outputFile.getAbsoluteFile());
				BufferedWriter bw = new BufferedWriter(fw);
				bw.write(code);
				bw.close();
				
				code = code.replace(" DS",": DS");
				code = code.replace(" DB",": DB");
				code = code.replace("EXTERN","EXTERN:");
				
				File displayFile = new File(fileName[i].split("_")[0] + "_opReplaced.txt");
				 
				if (!displayFile.exists()) {
					displayFile.createNewFile();
				}
	 
				fw = new FileWriter(displayFile.getAbsoluteFile());
				bw = new BufferedWriter(fw);
				bw.write(code);
				bw.close();
			}
		}
		System.out.print("DONE4");
	}
	
	void opcodePreprocess() throws IOException
	{
		createOpCodeTable();
		replaceOpCodes();
	}
	
	void opCodePreprocess() throws IOException
	{
		createOpCodeTable();
		replaceOpCodes();
	}
	
	int macroPresent(String line)
	{
		String[] tags = line.split(" ");
		for(String tag: tags)
		{
			int x = 0;
			for (String value: macroMap.values()) {
		        if(value.equals(tag))
		        {
		        	return x;
		        }
		        x++;
		    }
		}
		return -1;
	}
	
	Map<String, String> macroMapping(int tag, String[] pams)
	{
		Map<String, String> tag_pam = new HashMap<String, String>();
		String[] pam_list = macroTable.get(tag).trim().split(",");
		int i = 0;
		for(String pam: pam_list)
		{
			tag_pam.put(pam, pams[i]);
			i=i+1;
		}
		return tag_pam;
	}
	
	int opcodePresent(String line)
	{
		String[] tags = line.split(" ");
		for(String tag: tags)
		{
			int x = 0;
			for (String value: opMap.values()) 
			{
		        if(value.equals(tag.toUpperCase()) || value.equals(tag.toLowerCase()))
		        	return x;
		        x++;
		    }
		}
		return -1;
	}
	
	Map<String, String> opMapping(int tag, String[] pams, boolean myDecode) throws IOException
	{
		Map<String, String> tag_pam = new HashMap<String, String>();
		String[] pam_list = opcodeTable.get(tag).trim().split(",");
	
		if(myDecode || decodeAll)
		{
			System.out.println("Parameter list from opcode table -----------");
			System.out.println(pam_list);
			System.in.read();
		}
		
		int i = 0;
		for(String pam: pam_list)
		{
			if(myDecode || decodeAll)
			{
				System.out.println("i + parameter from table + actual parameters");
				System.out.println(i + "  " + pam + "  " + pams[i] + "\n");
				System.in.read();
			}
			
			tag_pam.put(pam, pams[i]);
			i=i+1;
		}
		return tag_pam;
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
	
	String variablePresent(String line)
	{
		String[] tags = line.split(" |\\,");
		for(String tag: tags)
		{
			if(tag.contains("["))
			{
				String add = tag.split("\\[")[tag.split("\\[").length-1];
				add = add.split("]")[0];
				if(!isNumeric(add))
					add = "0";
				line = line.replace("[" + add + "]", "+" + add);
			}
		}
		return line;
	}
	@Override
	public void render(float delta) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void resize(int width, int height) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void show() {
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
		// TODO Auto-generated method stub
		
	}
}
