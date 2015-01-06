package lexer;

/**
 * @author GANESH R ZILPE
 * Description : This is Lexer class and it is used to find out what is exact word is. It is called from Gui.java.
 */

import java.util.Arrays;
import java.util.Collection;
import java.util.Vector;

public class Lexer {

	private String text;
	private Vector<Token> tokens; 
	/*private static final String[] KEYWORD = {"if", "else", "while", "switch", 
	"case", "return", "int", "float", "void", "char", "string", "boolean", 
	"true", "false", "print"}*/ //converting Keyword from String array to Vector for better searching and clean code 
	static Collection <String> KeywordCollection = Arrays.asList(new String [] {"if", "else", "while", "switch", 
			"case", "return", "int", "float", "void", "char", "string", "boolean", 
			"true", "false", "print", "default"});
	private static final Vector KEYWORD = new Vector(KeywordCollection);

	static Collection <String> AtoFCollection = Arrays.asList(new String[]{"A", "a", "B", "b", "C", "c", "D", "d", "E", "e", "F", "f"});
	static Collection <String> GtoZCollection = Arrays.asList(new String[]{"G", "g", "H", "h", "I", "i", "J", "j", "K", "k", "L", "l","M", "m", "N", "n", "O", "o", "P", "p", "Q", "q", "R", "r", "S", "s", "T", "t", "U", "u", "V", "v", "W", "w", "X", "x", "Y", "y", "Z", "z"});
	private static final Vector AtoF = new Vector(AtoFCollection);
	private static final Vector GtoZ = new Vector(GtoZCollection);
	//Following are the columns of State Table and hence defining integer constant for them
	private static final int ZERO      =  8;
	private static final int ONE       =  9;
	private static final int B         =  4;
	private static final int OTHER     =  17;
	private static final int DELIMITER =  18;
	private static final int ERROR     =  25;
	private static final int STOP      = -2;
	private static final int TWO_SEVEN = 10;
	private static final int EIGHT_NINE = 11;
	private static final int A_F = 7;
	private static final int G_Z = 12;
	private static final int X = 5;
	private static final int E = 6;
	private static final int SINGLE_QUOTE = 0;
	private static final int DOUBLE_QUOTE = 3;
	private static final int DOLLAR = 1;
	private static final int UNDERSCORE = 2;
	private static final int PERIOD = 13;
	private static final int WHITESPACE = 14;
	private static final int ESCAPECHAR = 15;
	private static final int PlusMinus = 16;

	private int singleQuoteCount = 0;
	private int doubleQuoteCount = 0;

	private boolean checkForDelimOper = true;
	//current string holds current word except current character  
	private String currentString;
	// current token is currently implemented for operator
	Token currentToken = null; 

	//states table
	private static final int[][] stateTable = { 
		/*		   {'}     {$}    {_}    {"}    {b}    {x}    {e}    {A-F}  {0}    {1}  {2-7}  {8-9}  {G-Z}   {.}  {" "}    {\}   {+-} {OTHER}{Delimiter}*/
		/*S0*/	{	 1,     6, 	   6, 	  3,     6, 	6,     6,     6,     7,     8,     8,     8,     6,    13, ERROR, ERROR, ERROR, ERROR, STOP}, 
		/*S1*/	{ERROR,     2, 	   2,     2,     2,     2,     2,     2,     2,     2,     2,     2, 	 2,     2,     2,    15,     2,     2,    2}, 
		/*S2*/	{	16,    23,    23,    23,    23,    23,    23,    23,    23,    23,    23,    23,    23,    23,    23,    22,    23,    23,   23},
		/*S3*/	{    3,     3,     3,     5,     3,     3,     3,     3,     3,     3,     3,     3,     3,     3,     3,     4,     3,     3,    3},
		/*S4*/	{    3,     3,     3,     3,     3,     3,     3,     3,     3,     3,     3,     3,     3,     3,     3,     3,     3,     3,    3},
		/*S5*/	{ STOP,  STOP,  STOP,  STOP,  STOP,  STOP,  STOP,  STOP,  STOP,  STOP,  STOP,  STOP,  STOP,  STOP,  STOP,  STOP,  STOP,  STOP, STOP},
		/*S6*/	{    6,     6,     6, ERROR,     6,     6,     6,     6,     6,     6,     6,     6,     6, ERROR, ERROR, ERROR, ERROR, ERROR, STOP},
		/*S7*/	{ERROR, ERROR, ERROR, ERROR,     9,    10,    19, ERROR,    17,    17,    17, ERROR, ERROR,    12, ERROR, ERROR, ERROR, ERROR, STOP},
		/*S8*/	{ERROR, ERROR, ERROR, ERROR, ERROR, ERROR,    19, ERROR,     8,     8,     8,     8, ERROR,    12, ERROR, ERROR, ERROR, ERROR, STOP},
		/*S9*/	{ERROR, ERROR, ERROR, ERROR, ERROR, ERROR, ERROR, ERROR,    14,    14, ERROR, ERROR, ERROR, ERROR, ERROR, ERROR, ERROR, ERROR, STOP},
		/*S10*/	{ERROR, ERROR, ERROR, ERROR,    11, ERROR,    11,    11,    11,    11,    11,    11, ERROR, ERROR, ERROR, ERROR, ERROR, ERROR, STOP},
		/*S11*/	{ERROR, ERROR, ERROR, ERROR,    11, ERROR,    11,    11,    11,    11,    11,    11, ERROR, ERROR, ERROR, ERROR, ERROR, ERROR, STOP},
		/*S12*/	{ERROR, ERROR, ERROR, ERROR, ERROR, ERROR,    19, ERROR,    18,    18,    18,    18, ERROR, ERROR, ERROR, ERROR, ERROR, ERROR, STOP},
		/*S13*/	{ERROR, ERROR, ERROR, ERROR, ERROR, ERROR, ERROR, ERROR,    18,    18,    18,    18, ERROR, ERROR, ERROR, ERROR, ERROR, ERROR, STOP},
		/*S14*/	{ERROR, ERROR, ERROR, ERROR, ERROR, ERROR, ERROR, ERROR,    14,    14, ERROR, ERROR, ERROR, ERROR, ERROR, ERROR, ERROR, ERROR, STOP},
		/*S15*/	{    2,     2, 	   2,     2,     2,     2,     2,     2,     2,     2,     2,     2, 	 2,     2,     2,     2,     2,     2,    2},
		/*S16*/	{ STOP,  STOP,  STOP,  STOP,  STOP,  STOP,  STOP,  STOP,  STOP,  STOP,  STOP,  STOP,  STOP,  STOP,  STOP,  STOP,  STOP,  STOP, STOP},
		/*S17*/	{ERROR, ERROR, ERROR, ERROR, ERROR, ERROR, ERROR, ERROR,    17,    17,    17, ERROR, ERROR, ERROR, ERROR, ERROR, ERROR, ERROR, STOP},
		/*S18*/	{ERROR, ERROR, ERROR, ERROR, ERROR, ERROR,    19, ERROR,    18,    18,    18,    18, ERROR, ERROR, ERROR, ERROR, ERROR, ERROR, STOP},
		/*S19*/	{ERROR, ERROR, ERROR, ERROR, ERROR, ERROR, ERROR, ERROR,    21,    21,    21,    21, ERROR, ERROR, ERROR, ERROR,    20, ERROR, STOP},
		/*S20*/	{ERROR, ERROR, ERROR, ERROR, ERROR, ERROR, ERROR, ERROR,    21,    21,    21,    21, ERROR, ERROR, ERROR, ERROR, ERROR, ERROR, STOP},
		/*S21*/	{ERROR, ERROR, ERROR, ERROR, ERROR, ERROR, ERROR, ERROR,    21,    21,    21,    21, ERROR, ERROR, ERROR, ERROR, ERROR, ERROR, STOP},
		/*S22*/	{	23,    23,    23,    23,    23,    23,    23,    23,    23,    23,    23,    23,    23,    23,    23,    23,    23,    23,   23},
		/*S23*/	{   24,    23,    23,    23,    23,    23,    23,    23,    23,    23,    23,    23,    23,    23,    23,    22,    23,    23,   23},
		/*S24*/	{ STOP,  STOP,  STOP,  STOP,  STOP,  STOP,  STOP,  STOP,  STOP,  STOP,  STOP,  STOP,  STOP,  STOP,  STOP,  STOP,  STOP,  STOP, STOP},
		/*SEr*/	{ERROR, ERROR, ERROR, ERROR, ERROR, ERROR, ERROR, ERROR, ERROR, ERROR, ERROR, ERROR, ERROR, ERROR, ERROR, ERROR, ERROR, ERROR, STOP}
	};

	//constructor
	public Lexer(String text) {
		this.text = text;
	}

	//run
	public void run () {
		tokens = new Vector<Token>();
		String line;
		int counterOfLines= 1;
		// split lines
		do {
			int eolAt = text.indexOf('\n');
			if (eolAt >= 0) {
				line = text.substring(0,eolAt); 
				if (text.length()>0) text = text.substring(eolAt+1);  
			} else {
				line = text;
				text = "";
			}
			splitLine (counterOfLines, line);
			counterOfLines++;
		} while ( !text.equals("") );   
	}

	//split line
	private void splitLine(int row, String line) {
		int state = 0;
		int index = 0;
		char currentChar;
		String string="";
		currentString = "";
		checkForDelimOper = true;
		singleQuoteCount = 0;
		doubleQuoteCount = 0;

		if (line.equals("")) return;
		//DFA working
		int go; 
		do 
		{  
			currentChar = line.charAt(index);
			//logic for breaking the past word if " or ' occurs
			if(!currentString.isEmpty() && (currentChar =='\'' || currentChar == '"') && singleQuoteCount == 0 && doubleQuoteCount == 0)
			{
				break;
			}
			else
			{
				go = calculateNextState(state, currentChar);  
				if( go != STOP ) {
					string = string + currentChar;
					currentString = string;
					//to support '\' as error. This is special case with my logic
					if(state == 1 && go == ERROR)
					{
						index++;
						break;
					}
					state = go;
					//logic for breaking the word on completion of String and character i.e. on second occurrence of " and ' respectively
					if(state == 16 || state == 5 || state == 24)
					{
						index++;
						break;
					}
				}
				index++;        
			}

		} while (index < line.length() && go != STOP);

		if (state == 16) 
		{
			tokens.add(new Token(string, "CHARACTER", row));
		}
		else if (state == 5) 
		{
			tokens.add(new Token(string, "STRING", row));
		}
		else if (state == 6)
		{
			if(KEYWORD.contains(string))
				tokens.add(new Token(string, "KEYWORD", row));
			else
				tokens.add(new Token(string, "IDENTIFIER", row));
		}
		else if (state == 7) 
		{
			tokens.add(new Token(string, "INTEGER", row));
		}
		else if (state == 8) 
		{
			tokens.add(new Token(string, "INTEGER", row));
		}
		else if (state == 11) 
		{
			tokens.add(new Token(string, "HEXADECIMAL", row));
		}
		else if (state == 12 || state == 18 || state == 21) 
		{
			tokens.add(new Token(string, "FLOAT", row));
		}
		else if (state == 14) 
		{
			tokens.add(new Token(string, "BINARY", row));
		}
		else if (state == 17) 
		{
			tokens.add(new Token(string, "OCTAL", row));
		}
		else if(state == 25 || state == 1 || state == 2 || state ==3 || state == 4 || state == 9 || state == 10 
				|| state == 15 || state == 16 || state == 20 || state == 22 || state == 23 || state == 24)
		{
			tokens.add(new Token(string, "ERROR", row));
		}
		//If state is one of the following then String or Character don't end with their " and ' respectively. So, do not check for delimiter and operator as they are part of erroneous word 
		if(state == 22 || state == 23 || state == 3)
		{
			checkForDelimOper = false;
		}

		// current char
		if( isDelimiter(currentChar) && checkForDelimOper)
			tokens.add(new Token(currentChar+"", "DELIMITER", row));
		else if (isOperator(currentChar) && checkForDelimOper)
		{
			Token tempToken = new Token(currentChar+"", "OPERATOR", row); 
			//Logic for == and != operator
			if(!tokens.isEmpty())
				currentToken = tokens.lastElement();
			if(currentToken != null && (currentToken.getWord().equals("=") || currentToken.getWord().equals("!")) && tempToken.getWord().equals("="))
			{
				if(currentToken.getWord().equals("="))
					currentToken.setWord("==");
				else if(currentToken.getWord().equals("!"))
					currentToken.setWord("!=");
			}
			else
			{
				tokens.add(tempToken);
			}
		}
		// loop
		if (index < line.length()) 
			splitLine(row, line.substring(index));
	}

	// calculate state
	private int calculateNextState(int state, char currentChar) 
	{
		if((currentChar == '+' || currentChar == '-') && state == 19)/* To support Float of kind 2e2, 2e+2, 2e-2 which is having + and - or nothing after e. This has to be put ahead of isOperator() call which is later followed this else if */
		{
			return stateTable[state][PlusMinus];
		}
		else if (isSpace(currentChar)  || isDelimiter(currentChar)  || 
				isOperator(currentChar) /*|| isQuotationMark(currentChar)*/ ) // this is commented otherwise string and character would not be supported
			return stateTable[state][DELIMITER];

		if (currentChar == ' ')
			return stateTable [state][WHITESPACE];
		else if (currentChar == '\'')
		{
			singleQuoteCount++;
			return stateTable [state][SINGLE_QUOTE];
		}
		else if(currentChar == '\\')
			return stateTable[state][ESCAPECHAR];
		else if (currentChar == '$')
			return stateTable [state][DOLLAR];    
		else if (currentChar == '_')
			return stateTable [state][UNDERSCORE];
		else if (currentChar == '"')
		{
			doubleQuoteCount++;
			return stateTable [state][DOUBLE_QUOTE];
		}
		else if (currentChar == 'b' || currentChar == 'B')
			return stateTable [state][B];
		else if (currentChar == 'x' || currentChar == 'X')
			return stateTable [state][X];
		else if (currentChar == 'e' || currentChar == 'E')
			return stateTable [state][E];
		else if (AtoF.contains(currentChar+""))
			return stateTable [state][A_F];
		else if (GtoZ.contains(currentChar+""))
			return stateTable [state][G_Z];
		else if (currentChar == '.')
			return stateTable [state][PERIOD];
		else if (currentChar == '0')
			return stateTable [state][ZERO];
		else if (currentChar == '1')
			return stateTable [state][ONE];
		else if (currentChar == '2' || currentChar == '3' || currentChar == '4'|| currentChar == '5'|| currentChar == '6'|| currentChar == '7')
			return stateTable [state][TWO_SEVEN];
		else if (currentChar == '8' || currentChar =='9')
			return stateTable [state][EIGHT_NINE];
		return stateTable [state][OTHER];
	}

	// isDelimiter
	private boolean isDelimiter(char c) {
		char [] delimiters = {':', ';', '}','{', '[',']','(',')',','}; //Added new line character as a delimiter
		for (int x=0; x<delimiters.length; x++) {
			if (c == delimiters[x]) return true;      
		}
		return false;
	}

	// isOperator
	private boolean isOperator(char o) {
		// == and != should be handled in splitLine
		char [] operators = {'+', '-', '*','/','<','>','=','!','&','|'};
		for (int x=0; x<operators.length; x++) {
			if (o == operators[x]) return true;      
		}
		return false;
	}

	// isQuotationMark
	private boolean isQuotationMark(char o) {
		char [] quote = {'"', '\''};
		for (int x=0; x<quote.length; x++) {
			if (o == quote[x]) return true;      
		}
		return false;
	}

	// isSpace
	private boolean isSpace(char o) {
		return o == ' ' || o == '\t';
	}

	// getTokens
	public Vector<Token> getTokens() {
		return tokens;
	}
}
