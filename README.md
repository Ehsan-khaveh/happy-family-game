# Happy Family Game

![Player Screen](screenshots/player.png=250x?raw=true "Player Screen")

## Game Overview
Happy Family is a traditional multiplayercard game, usually with a specially made set of picture cards, featuring illustrations of fictional families, based on occupation types. The object of the game is to collect complete a family. The player whose turn it is asks another player for a specific card from the same family as a card that the player already has. If the asked player has the card, he gives it to the requester, and it will be next person's turn. The game goes on until someone has all the card for a family and wins the game.

## Implementation
The project is a simple client-server based Java application which uses Java GUI widget toolkit such as Swing and AWT got the graphical interface. To start the game, the `ServerApplication.java` needs to run first. Once the server app is up, clients (players and observers) can join the game. This is done by running `ClientApplication.java`. As soon as 4 players are in, the game begins. The game ends as soon as one of the players has one complete family in hand.



