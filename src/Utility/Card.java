package Utility;

import javax.swing.ImageIcon;
//class card for card objects
public class Card {

	String title, familyName, symbol;
	ImageIcon icon;
	int number;
	
	public Card(String title , String familyName, String symbol, int number)
	{
		this.title = title;
		this.familyName = familyName;
		this.symbol = symbol;
		this.number = number;
		icon = createImageIcon(("Images/"+title+"_"+familyName+".png"));
	}
	
	public String getTitle()
	{
		return title;
	}
	
	public String getFamilyName()
	{
		return familyName;
	}
	
	public String getSymbol()
	{
		return symbol;
	}
	
	public int getNum()
	{
		return number;
	}
	
	public ImageIcon getIcon()
	{
		return icon;
	}
	
	public ImageIcon createImageIcon(String path)
	{
		java.net.URL imgURL = getClass().getResource(path);
		if (imgURL != null) 
		{
			return new ImageIcon(imgURL);
		} 
		else 
		{
			System.err.println("Couldn't find file: " + path);
			return null;
		}
	}
	
	
}
