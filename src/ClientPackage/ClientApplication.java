package ClientPackage;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Formatter;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import Utility.Card;
import Utility.Utilities;

public class ClientApplication extends JFrame implements Runnable
{
	//variables declaration
	private static final long serialVersionUID = 1L;
	
	private static final int portNum = 43444;
	private static final String playerString = "player", observerString = "observer";
	
	private ArrayList<Card> cards;
	private ArrayList<Card> myCards = new ArrayList<Card>();
	private Card user;
	private Socket connection;
	private String hostName, clientType, validity, role;
	private Scanner input;
	private Formatter output;
	private boolean gameStarted = false, valid = false, myTurn = false; 
	
	private BorderLayout mainPanelLayout = new BorderLayout(12, 12);
	private EmptyBorder emptyBorder = new EmptyBorder(new Insets(15, 15, 15, 15));
	private JPanel mainPanel, gamePanel, splitPanel, playerPanel, cardsPanel,
			statusPanel, leftPanel,rightPanel, picturePane, choosePlayerPanel, statusObserverView;
	private JFrame starter;
	private TitledBorder title;
	private Border blackline;
	private JScrollPane listScrollPane, gameStatus,observerScroll;
	private JButton submit;
	private JList cardsList = new JList();
	private JButton[] cardButs = new JButton[15];
	private JTextArea jta = new JTextArea(8, 30), jtaObserver = new JTextArea(30,30);
	private JSplitPane splitPane;
	
	private int cardNum, cardSelectedIndex, Indexes[] = new int[40];
	private JLabel pic;
	private JLabel[] userIcons = new JLabel[3];
	private JRadioButton[] users = new JRadioButton[4];
	private ButtonGroup group = new ButtonGroup();
	//Constructor for ClientApplication
	public ClientApplication(String hostName) {
		this.hostName = hostName;
		cards = new ArrayList<Card>();
		cards.addAll(Utilities.getCards());
		drawGUI();
	}
	//draw the Starting Frame
	public void drawGUI() {
		
		starter = new JFrame("Welcome To Happy Families");
		JPanel starterPanel = new JPanel(new BorderLayout());
		JPanel joinPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
		starterPanel.setBorder(new EmptyBorder(new Insets(15,15,15,15)));
		starterPanel.add(new JLabel("Join the game as a: "),BorderLayout.NORTH);
		
		final JRadioButton player_button = new JRadioButton("Player");
		player_button.setMnemonic(KeyEvent.VK_P);
		player_button.setActionCommand("Player");
		player_button.setSelected(true);
		
		final JRadioButton observer_button = new JRadioButton("Observer");
		observer_button.setMnemonic(KeyEvent.VK_O);
		observer_button.setActionCommand("Observer");
		observer_button.setSelected(true);
		
		ButtonGroup group = new ButtonGroup();
	    group.add(player_button);
	    group.add(observer_button);
	    
	    JPanel choicePanel = new JPanel(new GridLayout(2,1));
	    choicePanel.add(player_button);
	    choicePanel.add(observer_button);
	    JButton join = new JButton("Join");
	    join.addActionListener(new ActionListener()
	    {
	    	public void actionPerformed(ActionEvent e)
	    	{
	    		if(player_button.isSelected())
	    		{
	    			clientType = playerString;
	    			ConnectToServer();
	    			if(!valid)
	    			{
	    				JOptionPane.showMessageDialog(null, "There are already 4 players in the game!");
	    				starter.repaint();
	    			}
	    			else
	    			{
	    				
	    				starter.dispose();
	    			}
	    			
	    		}
	    		else
	    		{
	    			clientType = observerString;
	    			ConnectToServer();
	    			if(!valid)
	    			{
	    				JOptionPane.showMessageDialog(null, "There are already 6 observers in the game!");
	    				starter.repaint();
	    			}
	    			else
	    			{
	    				starter.dispose();
	    			}
	    		}
	    		
	    	}
	    });
		joinPanel.add(join);
		
		starterPanel.add(choicePanel, BorderLayout.CENTER);
		starterPanel.add(joinPanel, BorderLayout.SOUTH); 
		
		starter.add(starterPanel);
		starter.setSize(300,200);
		starter.setMinimumSize(new Dimension(300,200));
		starter.setLocation(500,300);
		starter.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		starter.setVisible(true);
	}
	//draw player view
	public void startPlayer()
	{
		mainPanel = new JPanel(mainPanelLayout);
		mainPanel.setBorder(emptyBorder);
		
		statusPanel = new JPanel();
		
		title = BorderFactory.createTitledBorder(blackline, "Game Status");
		statusPanel.setBorder(title);
		
		jta.setEditable(false);
		gameStatus = new JScrollPane(jta);
		statusPanel.add(gameStatus);
		
		leftPanel = new JPanel(new BorderLayout());
		leftPanel.add(statusPanel, BorderLayout.NORTH);
		leftPanel.add(getGamePanel(), BorderLayout.CENTER);
		
		rightPanel = new JPanel(new BorderLayout());
		rightPanel.add(getCardsPanel(), BorderLayout.CENTER);
		
		mainPanel.add(rightPanel, BorderLayout.CENTER);
		mainPanel.add(leftPanel, BorderLayout.WEST);
		
		add(mainPanel);
		
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setVisible(true);
		setTitle(role);
		setSize(915,720);
		setMinimumSize(new Dimension(915,700));
		setLocation(150,12);
	}
	//draw observer view
	public void startObserver()
	{
		mainPanel = new JPanel(mainPanelLayout);
		mainPanel.setBorder(emptyBorder);
		
		statusObserverView = new JPanel();
		
		jtaObserver.setEditable(false);
		observerScroll = new JScrollPane(jtaObserver);
		statusObserverView.add(observerScroll);
			
		mainPanel.add(statusObserverView);
		
		add(mainPanel);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setVisible(true);
		setTitle(role);
		setSize(500,600);
		setMinimumSize(new Dimension(500,600));
		setLocation(150,12);
	}
	//get gamePanel
	public JPanel getGamePanel()
	{
		gamePanel = new JPanel();
		title = BorderFactory.createTitledBorder(blackline, "Game Panel");
		
		gamePanel.setBorder(title);
		
		pic = new JLabel();
		String[] cardsString = new String[27];
		 
		if(gameStarted)
		{
			for(int i=0; i<cards.size();i++)
			{
				if(cards.get(i) == null)
					break;
				cardsString[i] = cards.get(i).getFamilyName()+" - "+cards.get(i).getTitle();
				Indexes[i] = cards.get(i).getNum(); 
			}
			cardsList = new JList(cardsString);
			cardsList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			cardsList.setSelectedIndex(0);
			ImageIcon icon = cards.get(0).getIcon();
			pic.setIcon(icon);
			cardsList.addListSelectionListener(new ListSelectionListener()
			{
				public void valueChanged(ListSelectionEvent e) 
				{
					JList list = (JList)e.getSource();
					
					cardSelectedIndex = list.getSelectedIndex();
					ImageIcon icon = cards.get(cardSelectedIndex).getIcon();
					pic.setIcon(icon);
					if  (icon != null) {
						pic.setText(null);
					} else {
						pic.setText("Image not found");
					}
			    }
			});
		}
		
		listScrollPane = new JScrollPane(cardsList);
		picturePane = new JPanel();
		
		picturePane.setMinimumSize(new Dimension(110,150));
		picturePane.add(pic);
		
		splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                listScrollPane, picturePane);
		splitPane.setOneTouchExpandable(true);
		splitPane.setDividerLocation(170);
		splitPane.setPreferredSize(new Dimension(300, 160));
		
		choosePlayerPanel = new JPanel(new GridLayout(2,3,30,0));
		choosePlayerPanel.setBorder(emptyBorder);
		user = new Card("user","icon","",100);
		
		for(int i=0;i<3;i++)
		{	
			userIcons[i] = new JLabel();
			userIcons[i].setIcon(user.getIcon());
			choosePlayerPanel.add(userIcons[i]);
		}
		for(int j=0;j<4;j++)
		{
			if(role.equalsIgnoreCase("Player "+(j+1)))
				continue;
			
			users[j] = new JRadioButton("Player "+(j+1));
			users[j].setActionCommand(Integer.toString(j+1));
			group.add(users[j]);
			choosePlayerPanel.add(users[j]);
		}
		submit = new JButton("Submit");
		submit.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e) 
			{
				if(!gameStarted)
				{
					JOptionPane.showMessageDialog(null, "Game has not been launched yet. please wait!");
				}
				else
				{
					if(myTurn)
					{
						if(group.getSelection() != null)
						{
							String selected = group.getSelection().getActionCommand();
							output.format(selected+"\n");
							output.flush();
							output.format(Integer.toString(Indexes[cardSelectedIndex])+"\n");
							output.flush();
						}
						else
							JOptionPane.showMessageDialog(null, "Please choose a player to request the card from!");
					}
					else
						JOptionPane.showMessageDialog(null, "Please wait for your turn!");
				}
			}
		}
		);

		splitPanel = new JPanel(new BorderLayout());
		splitPanel.setBorder(emptyBorder);
		splitPanel.add(new JLabel("Ask"), BorderLayout.NORTH);
		splitPanel.add(splitPane, BorderLayout.SOUTH);
		

		playerPanel = new JPanel(new BorderLayout());
		playerPanel.setBorder(emptyBorder);
		playerPanel.add(new JLabel("From"), BorderLayout.NORTH);
		playerPanel.add(choosePlayerPanel, BorderLayout.SOUTH);

		gamePanel.add(splitPanel);
		gamePanel.add(playerPanel);
		gamePanel.add(submit);
		gamePanel.setPreferredSize(new Dimension(400,300));
		return gamePanel;
	}
	//get CardsPanel
	public JPanel getCardsPanel()
	{
		cardsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		title = BorderFactory.createTitledBorder(blackline, "Your Cards");
		cardsPanel.setBorder(title);
		if(gameStarted)
		{
			int p = 0;
			while(cardButs[p] != null)
			{
				cardsPanel.add(cardButs[p]);
				p++;
			}
		}
		return cardsPanel;
	}
	
	//connect to the server
	public void ConnectToServer()
	{
		try
		{
			//make a connection to the server
			connection = new Socket(InetAddress.getByName(hostName), portNum);
			//get streams for input and output
			input = new Scanner(connection.getInputStream());
			output = new Formatter(connection.getOutputStream());
			
			output.format(clientType+"\n");
			output.flush();
			validity = input.nextLine();
			if(validity.equalsIgnoreCase("invalid"))
			{
				try
				{
					valid = false;
					connection.close();
				}catch(IOException exc)
				{
					exc.printStackTrace();
				}
			}
			else
			{
				valid = true;
				ExecutorService worker = Executors.newFixedThreadPool(1);
				//execute player
				worker.execute(this);
			}
		}catch(IOException e)
		{
			e.printStackTrace();
		}
	}
	//implement run method for this thread
	public void run()
	{
		if(valid)
		{
			role = input.nextLine();
			if(clientType.equalsIgnoreCase(playerString))
			{
				startPlayer();
				displayMessage("SERVER: You are "+role);
			}
			else
			{
				startObserver();
				updateObservers("SERVER: You are "+role);
			}
			while(true)
			{
				if(input.hasNextLine())
					processMessage(input.nextLine());
			}
			
		}
			
	}
	//create card buttons
	public void createCardButtons()
	{
		for(int j=0; j<myCards.size(); j++)
		{
			cardButs[j] = new JButton(myCards.get(j).getIcon());
			cardButs[j].setToolTipText(myCards.get(j).getTitle()+" of "+myCards.get(j).getFamilyName()+" family");
		}
	}
	//display the final window to the winner
	public void showWinnerMessage(String fName)
	{
		final JFrame winner = new JFrame();
		JPanel container = new JPanel(new BorderLayout());
		JPanel textPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
		JPanel picturePanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
		JPanel optionsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
		JButton rejoin = new JButton("Rejoin");
		JButton exit = new JButton("Exit");
		
		JLabel label = new JLabel("Congratulations, You Won!!");
	    label.setFont(new Font("Serif", Font.PLAIN, 36));
	    JLabel label2 = new JLabel("You have completed "+fName+"'s family");
	    label2.setFont(new Font("Serif", Font.PLAIN, 20));
	    
	    textPanel.add(label);
	    
	    Card family = new Card(fName,"icon","",200);
	    JLabel newLabel = new JLabel();
		newLabel.setIcon(family.getIcon());
		picturePanel.add(label2);
		picturePanel.add(newLabel);
	    
		optionsPanel.add(rejoin);
		optionsPanel.add(exit);
		
	    container.add(textPanel, BorderLayout.NORTH);
	    container.add(picturePanel, BorderLayout.CENTER);
	    container.add(optionsPanel, BorderLayout.SOUTH);
	    
	    winner.add(container);
	    
		winner.setSize(700,300);
		winner.setTitle("Winner!!");
		winner.setMinimumSize(new Dimension(600,300));
		winner.setLocation(200,300);
		winner.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		winner.setVisible(true);
		
		rejoin.addActionListener(new ActionListener()
	    {
	    	public void actionPerformed(ActionEvent e)
	    	{
	    		winner.dispose();
	    		new ClientApplication("127.0.0.1");
	    	}
	    });
		
		exit.addActionListener(new ActionListener()
	    {
	    	public void actionPerformed(ActionEvent e)
	    	{
	    		//close connection after game ends
	    		try
	    		{
	    			connection.close();
	    		}
	    		catch(IOException error)
	    		{
	    			error.printStackTrace();
	    		}
	    		System.exit(1);
	    	}
	    });
	}
	//update observer by appending the textarea
	public void updateObservers(final String message)
	{
		SwingUtilities.invokeLater(
				new Runnable()
				{
					public void run()
					{
						jtaObserver.append(message+"\n");
					}
				}
			);
	}
	//process messages received by the server
	public void processMessage(String message)
	{
		if(message.equalsIgnoreCase("Observer"))
		{
			String next = input.nextLine();
			updateObservers(next);
		}
		else if(message.equals("Sending the Cards"))
		{
			int k = 0; 
			while(k<9)
			{
				cardNum = Integer.parseInt(input.nextLine());
				for(int p=0;p<cards.size();p++)
				{	 
					if(cardNum == cards.get(p).getNum())
					{	
						addCardToPlayer(cards.get(p));
						cards.remove(p);
						break;
					}
				}
				k++;
			}
			createCardButtons();
			gameStarted = true;
			updatePanels();
		}
		else if(message.equalsIgnoreCase("SERVER: It's your turn"))
		{
			myTurn = true;
			displayMessage(message);
		}
		else if(message.equalsIgnoreCase("remove"))
		{
			int removeIndex = Integer.parseInt(input.nextLine());
			for(int i=0; i<myCards.size();i++)
			{
				if(myCards.get(i).getNum() == removeIndex)
				{
					myCards.remove(i);	
				}
			}
			createCardButtons();
			cardButs[myCards.size()] = null;
			updatePanels();
		}
		else if(message.equalsIgnoreCase("add"))
		{
			int addIndex = Integer.parseInt(input.nextLine());
			for(int i=0; i<cards.size();i++)
			{
				if(cards.get(i).getNum() == addIndex)
				{
					//insert the card in the correct order
					addCardToPlayer(cards.get(i));
					cards.remove(i);
					break;
				}
			}
			createCardButtons();
			updatePanels();
		}
		else if(message.equalsIgnoreCase("Winner"))
		{
			message = input.nextLine();
			String winnerName = input.nextLine();
			String fName = input.nextLine();
			if(winnerName.equalsIgnoreCase(role))
			{
				this.dispose();
				showWinnerMessage(fName);	
			}
			else
			{
				displayMessage(message);
				JOptionPane.showMessageDialog(null, message);
				//closing the connection after game ends
				try
				{
					connection.close();
				}
				catch(IOException e)
				{
					e.printStackTrace();
				}
				System.exit(1);
			}
		}
		else
		{
			myTurn = false;
			displayMessage(message);
		}
	}
	//update all the panels after a change is made
	public void updatePanels()
	{
		leftPanel.removeAll();
		leftPanel.add(statusPanel, BorderLayout.NORTH);
		leftPanel.add(getGamePanel(), BorderLayout.CENTER);
		leftPanel.repaint();
		leftPanel.revalidate();
		rightPanel.removeAll();
		rightPanel.add(getCardsPanel());
		rightPanel.repaint();
		rightPanel.revalidate();
		this.repaint();
		this.validate();
	}
	//display a particular message to the player
	public void displayMessage(final String message)
	{
		SwingUtilities.invokeLater(
				new Runnable()
				{
					public void run()
					{
						jta.append(message+"\n");
					}
				}
			);
	}
	//add a particular card to the player(in order)
	public void addCardToPlayer(Card newCard)
	{
		int arraySize = myCards.size();
		boolean added = false;
		if(arraySize == 0)
			myCards.add(newCard);
		else
		{
			for(int i=0;i<arraySize;i++)
			{
				if(myCards.get(i).getNum()>newCard.getNum())
				{
					//System.out.println(i);
					myCards.add(i,newCard);
					added = true;
					break;
				}
			}
			if(!added)
				myCards.add(newCard);
		}
	}
	//main method
	public static void main(String[] args)
	{
		new ClientApplication("127.0.0.1");
	}
}
