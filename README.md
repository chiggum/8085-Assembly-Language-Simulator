Assembler-Linker-Loader for 8085 assembly language
====================================================
Contents
----------
- Introduction 
	- PURPOSE 
	- OVERVIEW 
- Requirements 
- Architecture / Design 
	- CODE & CONSTRUCTION PRINCIPLES 
- End User Manual 
	- USERS 

Introduction
--------------------
**PURPOSE**

The over reaching goal of the project is to prepare an application that converts the application deﬁned syntax respecting code to a GNUSIM 8085 executable code through a usual process of Assembling, Linking and Loading the code. The application developed, aims to help a general audience including CS students professors, to easily write a code using a simple application deﬁned syntax and hence convert it to the corresponding GNUSIM 8085 executable code. The application is published under GPL and is open for the developers.
Tha application is developed using JAVA and LibGDX library and can be deployed on:
1.Desktop
2.Android
3.WebPage
4.Iphone

**OVERVIEW**

-	Input
	- Software accepts multiple assembly ﬁles written using the deﬁned instruction set.
- Assembler - Pass1
	- In this part the input codes are converted to native 8085 code. Macros expansion is also taken care in here. To ease the programming for user, user can use his predeﬁned macros within another macro.
- Assembler - Pass2
	- Symbol Table for each ﬁle is generated and is stored in ﬁlename.table. After this all the labels are replaced by relative address values.
- Linker
	- Extern variables are handled in here. All the ﬁles are linked with each other.
- Loader
	- The user is asked for the memory location where he wants to load his program. The programs are then dynamically loaded into those speciﬁc memory locations.The ouput of ﬁlename.s.8085 ﬁle can then be run on GnuSim80855.

Requirements
--------------
- General:
	- JAVA/JVM or .jar execution supporting system
	- I/O: Monitor, Keyboard, Mouse

- Speciﬁc:
	- Android: support for OpenGL 2.0

- Optional:
	- Desktop: support for OpenGL 2.0
	- Iphone: support for OpenGL 2.0

Architecture / Design
------------------------
**CODE & CONSTRUCTION PRINCIPLES**
- Design specs of an assembler:
	- Identify the information necessary to perform a task.

	- Design a suitable data structure to record the information.

		- In our applicaton we used Map and List Contatiners to store the gathered information appropriately.
		- Map.put(key, value) stores the key-value pair in the corresponding map
		- Map.get(key) retrieves the value at the corresponding key.
		- Key and value both are objects.
		- Similarly, List.add(value) appends the list with the value and List.get(index) retrieves the value stored at the integer index speciﬁed.

- Determine the processing necessary to obtain and maintain the information and perform the task.

	- The Main processes/methods involved in our code are:
		- macroPreprocess : Prepreocesses Macros and creates a table
		- opCodePreprocess: preprocesses opCodes and stores length of opCodes with their actual code too using the above mentioned contatiners.
		- createSymbolTable: creates a symbol table.
		- replaceTable: replaces opcodes with their actual code.
		- linkCode: links ﬁles generated after replacing opcodes
		- loadCode: loads the code at the user deﬁned location6

End User Manual
------------------
**USERS**
	- Run systemprogramming jar runnable:
	- Copy the code on to the screen and save it on the stack.
	- Copy multiple number of codes on the screen and consequently save the m on the stack using the button provided.
	- Finally Assemble Pass 1 the code using the corresponding button.
	- Output of assemble pass 1 will be the symbol table of the all the codes provided as the input.
	- These symbol tables can be accessed by entering the index of the ﬁle and then clicking on ”Focus on Queried File”.
	- Then one can proceed with the pass 2, linking and loading sequentially with the outputs shown at every step.
	- The application demands the user to input an integer representing the location to load the input ﬁle.
	- Finally the output ﬁle is shown which can be copied into the GNUSIM 8085 and hence can be executed.