TablutCS580
===========
Class project for Intro to AI CS580 with Prof DeJong

Requirements:
Java 1.6+
--these are included in the package
Neuroph 2.8 - Neural Net kit
GSON
Guava

Please check all licenses before proceeding. 

5/5/2014
Final release for class. Now implements TD learning with a feedforward neural net and backprop. See paper in root for details. 

usage:
java -jar Tablut.jar type=<ANN|MM|RAND> NumGames=int NumTurns=int searchdepth=int(<4) - 
Currently only games played between the same type of algorithm are supported at the commandline
Running without arguments runs with type ANN(neural net) using the 1000 game trained nets for 100 games of 150 turns and depth 1
Note - be careful with outputs, we clobber liberally right now.

Known Issues:
When a player takes all of his opponents pieces, he gets the win, but the result type is logged as null instead of ALLCAP. 
There is no way from the command line to run a tournament between different player types. 

Config:
System currently is hardcoded to use 7x7 config file. Change contents to reflect 9x9 board to see how that works.(warning: 9x9 boards untested in several iterations)
The blackfeature1.txt and whitefeature1.txt files can be hand edited and changes will be read the next time a minimax player is used(type=MM). 


4/18/2014
Interim release with the following features and known issues. 

Notes:
Tablut game rules implemented and tested. Used mostly modern rules, including a king that can take, two enemy taking of king, and corner exits. 
Random players implemented and working. 
Results file outputs for analysis of game play. 
State can be saved and reloaded. 
Rudimentary GUI in ASCII and JFrame formats. 
Minimax algorithm in progress - state tree is generated but not properly cutoff and minimax player chooses no move on each turn, effectively being skipped. 
Evaluation function implemented with most features, all weights initialized to zero. 

Known Issues
Minimax player is not yet working. 
Minimax player can quickly be overwhelmed with high memory usage. 
Code is not particularly well documented/commented yet. 
Test coverage is limited to game rules and not game playing components. 