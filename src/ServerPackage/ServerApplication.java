package ServerPackage;

import java.awt.BorderLayout;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Formatter;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;

import Utility.Card;
import Utility.Utilities;

public class ServerApplication extends JFrame
{
	//variables declaration
	private static final long serialVersionUID = 1L;
	private static final int MAX_PLAYERS_NUM = 4, MAX_OBSERVERS_NUM = 6, portNum = 43444;
	
	private ArrayList<Card> cards = new ArrayList<Card>();
	private ArrayList<Player> players;
	private ArrayList<Observer> observers;
	private Card thisCard = null;
	private Player currentPlayer, winner;
	private Observer currentObserver;
	private int playerCount = 0, observerCount = 0, currPlayerIndex = 0, count;
	private ExecutorService runGame;
	private JTextArea output;
	private JScrollPane gameBoard;
	private Socket socket;
	private Scanner inStream;
	private Formatter outStream;
	private String clientType;
	private Lock gameLock;
	private Condition otherPlayersConnected, otherPlayerTurn;
	boolean sent = false;
	private String[] familyNames= {"Dirgeable","Ballon", "Helicoptere", "Avion", "Planeur", "Fusee"} ;
	private String fName, familyCompleted;
	private boolean gameStarted = false, gameFinished = false;
	
	private ServerSocket serverSocket;
	
	//constructor for ServerApplication
	public ServerApplication()
	{
		//set the title of window
		super("Happy Families Server");
		
		//create ExecutorService with a thread for each player
		runGame = Executors.newFixedThreadPool(MAX_PLAYERS_NUM+MAX_OBSERVERS_NUM);
		
		gameLock = new ReentrantLock();
		
		otherPlayersConnected = gameLock.newCondition();
		otherPlayerTurn = gameLock.newCondition();
		
		players = new ArrayList<Player>();
		observers = new ArrayList<Observer>();
		//initializing the game
		initGame();
		try
		{
			//set up the ServerSocket 
			serverSocket = new ServerSocket(portNum, 2);
		}
		catch(IOException e)
		{
			e.printStackTrace();
			System.exit(1);
		}
		
		//create and add output board to the window
		output = new JTextArea();
		output.setEditable(false);
		gameBoard = new JScrollPane(output);
		add(gameBoard, BorderLayout.CENTER);
		output.setText("Waiting for players to join the game\n");
		
		//setSize and show the window
		setSize(300,300);
		setVisible(true);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
	}
	//initialize the game
	public void initGame()
	{
		cards.addAll(Utilities.getCards());
		Collections.shuffle(cards);
	}
	//accept clients and execute them
	public void execute()
	{
		while(true)
		{
			try
			{
				socket = serverSocket.accept();
				inStream = new Scanner(socket.getInputStream());
				outStream = new Formatter(socket.getOutputStream());
				clientType = inStream.nextLine();
				if(clientType.equalsIgnoreCase("player"))
				{
					if(playerCount>=MAX_PLAYERS_NUM)
					{
						outStream.format("invalid\n"); 
						outStream.flush();
					}
					else
					{
						outStream.format("valid\n");
						outStream.flush();
						playerCount++;
						players.add(currentPlayer = new Player(socket, playerCount));
						runGame.execute(currentPlayer);
					}
				}
				else
				{
					if(observerCount>=MAX_OBSERVERS_NUM)
					{
						outStream.format("invalid\n"); 
						outStream.flush();
					}
					else
					{
						outStream.format("valid\n");
						outStream.flush();
						observerCount++;
						observers.add(currentObserver = new Observer(socket, observerCount));
						runGame.execute(currentObserver);
					}
				}
			}
			catch(IOException e)
			{
				e.printStackTrace();
				System.exit(1);
			}
		}
	}
	//start the game
	public void startTheGame()
	{
		gameLock.lock();
		
		try
		{
			for(int i= 0; i<players.size()-1; i++)
				players.get(i).setSuspended(false);
			
			for(int i= 0; i<observers.size()-1; i++)
				observers.get(i).setSuspended(false);
			
			otherPlayersConnected.signalAll();
		}
		
		finally
		{
			gameLock.unlock();
		}
		
		for(int i=0; i<players.size(); i++)
		{
			players.get(i).writeMessage("SERVER: All 4 players joined the game. Game Started");
			
			players.get(i).recieveCards();
			
			displayTurn(i);
			
			players.get(i).writeMessage("Sending the Cards");
			for(int j=0; j<players.get(i).getMyCards().size(); j++)
			{
				players.get(i).writeMessage(Integer.toString(players.get(i).getMyCards().get(j).getNum()));
			}
		
		}
		for(int i=0; i<observers.size(); i++)
		{
			observers.get(i).writeMessage("Observer");
			observers.get(i).writeMessage("SERVER: All 4 players joined the game. Game Started");
			observers.get(i).writeMessage("Observer");
			observers.get(i).writeMessage("SERVER: It's player "+(currPlayerIndex+1)+"'s turn");
		}
		displayMessage("Game Started");
		
	}
	//display whose turn it is
	public void displayTurn(int i)
	{
		if(i == currPlayerIndex)
			players.get(i).writeMessage("SERVER: It's your turn");
		else	
			players.get(i).writeMessage("SERVER: It's player "+(currPlayerIndex+1)+"'s turn");
	}
	//display a message to all the clients
	public void displayToAll(String message, int sender, int reciever)
	{
		for(int i=0; i<players.size(); i++)
		{
			if(i+1 == sender)
				players.get(i).writeMessage("You -> Player"+reciever+": "+message);
			else if(i+1 == reciever)
				players.get(i).writeMessage("Player"+sender+" -> You: "+message);
			else
				players.get(i).writeMessage("Player"+sender+" -> Player"+reciever+": "+message);
		}
		for(int i=0; i<observers.size(); i++)
		{
			observers.get(i).writeMessage("Observer");
			observers.get(i).writeMessage("Player"+sender+" -> Player"+reciever+": "+message);
		}
		displayMessage("Player"+sender+" -> Player"+reciever+": "+message);
	}
	//display a message on the server screen
	public void displayMessage(final String message)
	{
		//display message from event-dispatch thread of execution
		SwingUtilities.invokeLater(
				new Runnable()
				{
					public void run()
					{
						output.append(message+"\n");
					}
				}
				
			);
	}
	
	//check to see if the game is over
	public boolean isGameOver()
	{
		for(int i=0; i<players.size();i++)
		{
			for(int k = 0; k<familyNames.length;k++)
			{
				fName = familyNames[k];
				count = 0;
				for(int j=0; j<players.get(i).getMyCards().size(); j++)
				{	
					if(fName.equalsIgnoreCase(players.get(i).getMyCards().get(j).getFamilyName()))
						count++;
				}
				if(count == 6)
				{
					winner = players.get(i);
					familyCompleted = fName;
					return true;
				}
			}
		}
		return false;
	}
	
	//process a single request from a player to another
	public boolean processRequest(int playerIndex, int askFromIndex, int cardNumIndex)
	{
		//while not current player must for turn
		while(playerIndex != (currPlayerIndex+1))
		{
			//wait for player in turn
			gameLock.lock();
			
			try
			{
				otherPlayerTurn.await();
			}
			catch(InterruptedException e)
			{
				e.printStackTrace();
			}
			finally
			{
				//unlock after waiting
				gameLock.unlock(); 
			}
		}
		boolean found = false;
		for(int i = 0; i< players.get(askFromIndex-1).getMyCards().size();i++)
		{
			thisCard = players.get(askFromIndex-1).getMyCards().get(i);
			if(thisCard.getNum() == cardNumIndex)
			{
				players.get(currPlayerIndex).addCard(thisCard);
				players.get(askFromIndex-1).removeCard(i);
				found = true;
				break;
			}
		}
		//move to the next player
		if(currPlayerIndex == 3)
			currPlayerIndex = 0;
		else
			currPlayerIndex++;
		
		gameLock.lock();
		try
		{
			otherPlayerTurn.signalAll();
		}
		finally
		{
			gameLock.unlock();
		}
		
		if(found)
			return true;
		else
			return false;
	}
	//update the client view
	public void updateClientView(int from, int cardIndex)
	{	
		int index;
		if(currPlayerIndex == 0)
			index = 3;
		else
			index = currPlayerIndex-1;
		players.get(index).writeMessage("add");
		players.get(index).writeMessage(Integer.toString(cardIndex));
		players.get(from).writeMessage("remove");
		players.get(from).writeMessage(Integer.toString(cardIndex));
	}
	//inner class Client that manages each Client as a runnable 
	private abstract class Client implements Runnable
	{
		protected Scanner input;
		protected Socket socket;
		protected Formatter output;
		boolean suspended = true;
		
		private Client(Socket socket, int num)
		{	
			try
			{
				input = new Scanner(socket.getInputStream());
				output = new Formatter(socket.getOutputStream());
				socket = this.socket;
			}
			catch(IOException e)
			{
				e.getStackTrace();
			}
		}
		public void writeMessage(String message)
		{
			output.format(message+"\n");
			output.flush();
		}
		public void setSuspended(boolean status)
		{
			suspended = false;
		}
	}
	//inner class player that extends Client
	private class Player extends Client
	{
		int playerNum;
		
		ArrayList<Card> myCards = new ArrayList<Card>();
		
		private Player(Socket socket, int num)
		{
			super(socket,num);
			playerNum = num;
			try
			{
				input = new Scanner(socket.getInputStream());
				output = new Formatter(socket.getOutputStream());
				
			}
			catch(IOException e)
			{
				e.getStackTrace();
			}
		}
		
		public void run()
		{
			try
			{
				displayMessage("Player "+playerNum+" joined the game");
				writeMessage("Player "+playerNum);
				
				if(playerNum < MAX_PLAYERS_NUM)
				{
					writeMessage("SERVER: Waiting for Other Players to join...");
					
					gameLock.lock();
					
					try
					{
						while(suspended)
						{
							otherPlayersConnected.await();
						}
					}
					catch(InterruptedException e)
					{
						e.printStackTrace();
					}
					finally
					{
						gameLock.unlock();
					}
				}
				else
				{
					writeMessage("SERVER: Initializing the game...");
					//a short pause before starting the game
					try
					{
						Thread.sleep(2000);
					}
					catch(InterruptedException e)
					{
						e.printStackTrace();
					}
					startTheGame();
					gameStarted = true;
				}
				
				while(!isGameOver())
				{
					int askFrom = -1, cardNum = -1;
					if(input.hasNextLine())
						askFrom = Integer.parseInt(input.nextLine());
					if(input.hasNextLine())
						cardNum = Integer.parseInt(input.nextLine());
					
					if(askFrom != -1 && askFrom != -1)
					{
						try
						{
							for(int j=0; j<Utilities.getCards().size(); j++)
							{
								if(Utilities.getCards().get(j).getNum() == cardNum)
								{
									thisCard = Utilities.getCards().get(j);
									break;
								}
							}
							displayToAll("Do you have "+thisCard.getTitle()+" of "+thisCard.getFamilyName()+" family?", playerNum, askFrom);
							Thread.sleep(3000);
							if(processRequest(playerNum, askFrom, cardNum))
							{
								displayToAll("Yes, I do, here u go!", askFrom, playerNum);
								updateClientView((askFrom-1), cardNum);
								Thread.sleep(2000);
								displayToAll("Thank you!", playerNum, askFrom);
							}
							else
							{
								displayToAll("I'm afraid. I dont!", askFrom, playerNum);
							}
							Thread.sleep(2000);
						}
						catch(InterruptedException e)
						{
							e.printStackTrace();
						}
						//display whose turn it is
						for(int i=0; i<players.size(); i++)
						{
							displayTurn(i);
						}
						//display whose turn it is to all the observers 
						for(int i=0; i<observers.size();i++)
						{	
							observers.get(i).writeMessage("Observer");
							observers.get(i).writeMessage("SERVER: It's player "+(currPlayerIndex+1)+"'s turn");
						}
					}
				}
				if(!gameFinished)
				{
					for(int i=0; i<players.size(); i++)
					{
						players.get(i).writeMessage("Winner");
						players.get(i).writeMessage("SERVER: The winner is Player "+winner.getPlayerNum());
						players.get(i).writeMessage("Player "+winner.getPlayerNum());
						players.get(i).writeMessage(familyCompleted);
					}
					for(int i=0; i<observers.size(); i++)
					{
						observers.get(i).writeMessage("Observer");
						observers.get(i).writeMessage("SERVER: There is a winner");
						observers.get(i).writeMessage("Observer");
						observers.get(i).writeMessage("SERVER: Player "+winner.getPlayerNum()+" has completed "+familyCompleted+"'s family");
						observers.get(i).writeMessage("Observer");
						observers.get(i).writeMessage("SERVER: Game ends!");
					}
					displayMessage("Player "+winner.getPlayerNum()+" won the game!");
					playerCount = 0;
					observerCount = 0;
					currPlayerIndex = 0;
				}
				gameFinished = true;
			}
			finally
			{
				try
				{
					socket.close(); //close the connection after the game ends
				}
				catch(IOException e)
				{
					e.printStackTrace();
					System.exit(1);
				}
			}
			
		}
		
		public void addCard(Card card)
		{
			myCards.add(card);
		}
		
		public void removeCard(int index)
		{
			myCards.remove(index);
		}
		
		public boolean recieveCards()
		{
			while(myCards.size() < 9)
			{
				myCards.add(cards.get(0));
				cards.remove(0);
			}
			return false;
		}
		
		public ArrayList<Card> getMyCards()
		{
			return myCards;		
		}
		
		public int getPlayerNum()
		{
			return playerNum;
		}
	}
	//inner class Observer that extends Client
	private class Observer extends Client
	{
		int observerNum;
		private Observer(Socket socket, int num)
		{
			super(socket, num);
			observerNum = num;
		}
		public void run()
		{
			displayMessage("Observer "+observerNum+" joined the game");
			writeMessage("Observer "+observerNum);
			
			if(!gameStarted)
			{
				writeMessage("Observer");
				writeMessage("SERVER: Waiting for players to join...");
				
				gameLock.lock();
				
				try
				{
					while(suspended)
					{
						otherPlayersConnected.await();
					}
				}
				catch(InterruptedException e)
				{
					e.printStackTrace();
				}
				finally
				{
					gameLock.unlock();
				}
			}
			else
			{
				writeMessage("Observer");
				writeMessage("SERVER: Game is running");
				//A short pause before starting the game
				try
				{
					Thread.sleep(2000);
				}
				catch(InterruptedException e)
				{
					e.printStackTrace();
				}
				//updateObservers();
			}
		}
	}
	public static void main(String args[])
	{
		ServerApplication test = new ServerApplication();
		test.execute();
	}
}