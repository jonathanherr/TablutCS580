TablutCS580
===========

Class project for Intro to AI CS580 with Prof DeJong

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
