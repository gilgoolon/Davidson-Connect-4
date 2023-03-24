# Connect-4 AI and more
### by: Gil Alpert
### release date: 24-03-2023

## Introduction
Connect-4 is a Two player board game published on 1974.
The game has sustained its legendary status in the board game community, 
and forever will be one of the more remembered and iconic board games.
The game is simple enough that anyone who wants to play could learn it
in a few minutes and play it off of intuition,
but to perfect the art of playing, might take years for some...

## Motivation
As mentioned before, Connect-4 is a Two player game
meaning that in order to play it you must find an opponent to play against.
Board games are fun to play in person, but we don't always have someone to play against.

## Description
The project has Two main options\sections to choose from.
The First, is playing online. The project has a multiplayer section which enables
its users to connect through the internet to a server and play each other from
anywhere in the world. And the Second, is choosing the level of their opponent
and playing a computer program offline. Both options allow incredible functionality
and solve the problem in Two completely different ways which together present a great
result.

## Technologies
The two main technologies I have used to create this project are in both sections
(multiplayer and single-player).

For the multiplayer section, the technology I used for
communication is multithreading Networking with a connection based protocol - the TCP protocol.
Both players establish a connection with the server, send their moves and get a response.
once the server receives a move from either player, it sends back a response and notifies
the other player. Simplified, the protocol is based on stable connection between two computers
and a stream of data instead of individual packets (UDP).

For the single player section, I have chose an algorithmic approach for the problem of
designing an engine. The most important algorithm in my opinion is the "Alpha-Beta pruning"
algorithm. The algorithm helps with making decisions on which moves to make, and evaluating
a given game position based on the future positions ahead. Simplified,
every move a player has 7 (or less) moves to choose from, and for each move there are 7 (or less)
more responses of the opponent. The algorithm recursively prunes branches based on their
evaluation, and doesn't play moves which favors the opponent.