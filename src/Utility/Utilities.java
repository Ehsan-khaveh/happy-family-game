package Utility;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.StringTokenizer;

import javax.swing.ImageIcon;
//class utilities for shared functions and objects between clientApplication and serverApplication
public class Utilities {
	
	static FileInputStream inputStream;
	static String familyNamesFile="src/Utility/familyNames.txt", strLine, familyName, career;
	static DataInputStream inFamily;
	static BufferedReader brFamily;
	static StringTokenizer tokens;
	static ArrayList<Card> cards = new ArrayList<Card>();
	
	public static ArrayList<Card> getCards()
	{
		
		try
		{
			inputStream = new FileInputStream(familyNamesFile);
			inFamily = new DataInputStream(inputStream);
			brFamily = new BufferedReader(new InputStreamReader(inFamily));
			int p=0;
			while ((strLine = brFamily.readLine()) != null) {
				tokens = new StringTokenizer(strLine);
				familyName = tokens.nextToken();
				career = tokens.nextToken();
				
				cards.add(new Card("Father", familyName, career, ++p));
				cards.add(new Card("Mother", familyName, career, ++p));
				cards.add(new Card("Grandfather", familyName, career, ++p));
				cards.add(new Card("Grandmother", familyName, career, ++p));
				cards.add(new Card("Son", familyName, career, ++p));
				cards.add(new Card("Daughter", familyName, career, ++p));
			}
			
		}catch(FileNotFoundException e)
		{
			System.out.println("File not found");
		}
		catch(IOException e)
		{
			System.out.println("Problem ooccured in reading from the file");
		}
		return cards;
	}
	
	protected ImageIcon createImageIcon(String path)
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
