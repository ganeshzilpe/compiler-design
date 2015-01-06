package semanticAnalyzer;

import java.util.Hashtable;
import java.util.Stack;
import java.util.Vector;

import compiler.Gui;

/**
 * @author GANESH R ZILPE
 * Description : This is Semantic Analyzer class and it is used to check whether sentences are semantically correct or not. It is called from Parser.java.
 */

public class SemanticAnalyzer {

	private static final Hashtable<String, Vector<SymbolTableItem>> symbolTable = new Hashtable<String, Vector<SymbolTableItem>>();
	private static final Stack stack = new Stack();

	private static Gui gui;

	// create here a data structure for the cube of types
	private static int _INT = 0;
	private static int _FLOAT = 1;
	private static int _CHAR = 2;
	private static int _STRING = 3;
	private static int _BOOLEAN = 4;
	private static int _VOID = 5;
	private static int _ERROR = 6;
	private static int _ADD = 0;
	private static int _SUB = 1;
	private static int _MUL = 2;
	private static int _DIV = 3;
	private static int _ASSIGN = 4;
	private static int _AND = 5;
	private static int _OR = 6;
	private static int _NOT = 7;
	private static int _LESS_THAN = 8;
	private static int _GREAT_THAN = 9;
	private static int _LESS_EQUAL = 10;
	private static int _GREAT_EQUAL = 11;
	private static int _EQUAL_EQUAL = 12;
	private static int _NEGATION = 13;
	private static int  _NOT_EQUAL= 14;

	//cube[operator][type1][type2]
	static String [][][]cube = {
		{
			/*
			 *Operator = "+"
		   			 			int		float		char		string		boolean		void		error*/
			/*int*/	   {	"int",		"float",	"error",	"string",	"error",	"error",	"error" },
			/*float*/  {	"float",	"float",	"error",	"string",	"error",	"error",	"error" },
			/*char*/   {	"error",	"error",	"error",	"string",	"error",	"error",	"error" },
			/*string*/ {	"string",	"string",	"string",	"string",	"string",	"error",	"error" },
			/*boolean*/{	"error",	"error",	"error",	"string",	"error",	"error",	"error" },
			/*void*/   {	"error",	"error",	"error",	"error",	"error",	"error",	"error" },
			/*error*/  {	"error",	"error",	"error",	"error",	"error",	"error",	"error" },
		},
		{
			/*
			 *Operator = "-"
		   			 			int		float		char		string		boolean		void		error*/
			/*int*/	   {	"int",		"float",	"error",	"error",	"error",	"error",	"error" },
			/*float*/  {	"float",	"float",	"error",	"error",	"error",	"error",	"error" },
			/*char*/   {	"error",	"error",	"error",	"error",	"error",	"error",	"error" },
			/*string*/ {	"error",	"error",	"error",	"error",	"error",	"error",	"error" },
			/*boolean*/{	"error",	"error",	"error",	"error",	"error",	"error",	"error" },
			/*void*/   {	"error",	"error",	"error",	"error",	"error",	"error",	"error" },
			/*error*/  {	"error",	"error",	"error",	"error",	"error",	"error",	"error" },
		},
		{
			/*
			 *Operator = "*"=>_MUL
		   			 			int		float		char		string		boolean		void		error*/
			/*int*/	   {	"int",		"float",	"error",	"error",	"error",	"error",	"error" },
			/*float*/  {	"float",	"float",	"error",	"error",	"error",	"error",	"error" },
			/*char*/   {	"error",	"error",	"error",	"error",	"error",	"error",	"error" },
			/*string*/ {	"error",	"error",	"error",	"error",	"error",	"error",	"error" },
			/*boolean*/{	"error",	"error",	"error",	"error",	"error",	"error",	"error" },
			/*void*/   {	"error",	"error",	"error",	"error",	"error",	"error",	"error" },
			/*error*/  {	"error",	"error",	"error",	"error",	"error",	"error",	"error" },
		},
		{
			/*
			 *Operator = "/"=>_DIV
		   			 			int		float		char		string		boolean		void		error*/
			/*int*/	   {	"int",		"float",	"error",	"error",	"error",	"error",	"error" },
			/*float*/  {	"float",	"float",	"error",	"error",	"error",	"error",	"error" },
			/*char*/   {	"error",	"error",	"error",	"error",	"error",	"error",	"error" },
			/*string*/ {	"error",	"error",	"error",	"error",	"error",	"error",	"error" },
			/*boolean*/{	"error",	"error",	"error",	"error",	"error",	"error",	"error" },
			/*void*/   {	"error",	"error",	"error",	"error",	"error",	"error",	"error" },
			/*error*/  {	"error",	"error",	"error",	"error",	"error",	"error",	"error" },
		},
		{
			/*
			 *Operator = "="=>_ASSIGN
		   			 			int		float		char		string		boolean		void		error*/
			/*int*/	   {	"OK",		"error",	"error",	"error",	"error",	"error",	"error" },
			/*float*/  {	"OK",		"OK",		"error",	"error",	"error",	"error",	"error" },
			/*char*/   {	"error",	"error",	"OK",		"error",	"error",	"error",	"error" },
			/*string*/ {	"error",	"error",	"error",	"OK",		"error",	"error",	"error" },
			/*boolean*/{	"error",	"error",	"error",	"error",	"OK",		"error",	"error" },
			/*void*/   {	"error",	"error",	"error",	"error",	"error",	"OK",		"error" },
			/*error*/  {	"error",	"error",	"error",	"error",	"error",	"error",	"error" },
		},

		{
			/*
			 *Operator = "&"=>_AND
		   			 			int		float		char		string		boolean		void		error*/
			/*int*/	   {	"error",	"error",	"error",	"error",	"error",	"error",	"error" },
			/*float*/  {	"error",	"error",	"error",	"error",	"error",	"error",	"error" },
			/*char*/   {	"error",	"error",	"error",	"error",	"error",	"error",	"error" },
			/*string*/ {	"error",	"error",	"error",	"error",	"error",	"error",	"error" },
			/*boolean*/{	"error",	"error",	"error",	"error",	"boolean",	"error",	"error" },
			/*void*/   {	"error",	"error",	"error",	"error",	"error",	"error",	"error" },
			/*error*/  {	"error",	"error",	"error",	"error",	"error",	"error",	"error" },
		},

		{
			/*
			 *Operator = "|"=>_OR
		   			 			int		float		char		string		boolean		void		error*/
			/*int*/	   {	"error",	"error",	"error",	"error",	"error",	"error",	"error" },
			/*float*/  {	"error",	"error",	"error",	"error",	"error",	"error",	"error" },
			/*char*/   {	"error",	"error",	"error",	"error",	"error",	"error",	"error" },
			/*string*/ {	"error",	"error",	"error",	"error",	"error",	"error",	"error" },
			/*boolean*/{	"error",	"error",	"error",	"error",	"boolean",	"error",	"error" },
			/*void*/   {	"error",	"error",	"error",	"error",	"error",	"error",	"error" },
			/*error*/  {	"error",	"error",	"error",	"error",	"error",	"error",	"error" },
		},

		{
			/*
			 *Operator = "!"=>_NOT
		   			 			int		float		char		string		boolean		void		error*/
			/*int*/	   {	"error",	"error",	"error",	"error",	"boolean",	"error",	"error" }
		},

		{
			/*
			 *Operator = "<"=>_LESS_THAN
		   			 			int		float		char		string		boolean		void		error*/
			/*int*/	   {	"boolean",	"boolean",	"error",	"error",	"error",	"error",	"error" },
			/*float*/  {	"boolean",	"boolean",	"error",	"error",	"error",	"error",	"error" },
			/*char*/   {	"error",	"error",	"error",	"error",	"error",	"error",	"error" },
			/*string*/ {	"error",	"error",	"error",	"error",	"error",	"error",	"error" },
			/*boolean*/{	"error",	"error",	"error",	"error",	"boolean",	"error",	"error" },
			/*void*/   {	"error",	"error",	"error",	"error",	"error",	"error",	"error" },
			/*error*/  {	"error",	"error",	"error",	"error",	"error",	"error",	"error" },
		},

		{
			/*
			 *Operator = ">"=>_GREAT_THAN
		   			 			int		float		char		string		boolean		void		error*/
			/*int*/	   {	"boolean",	"boolean",	"error",	"error",	"error",	"error",	"error" },
			/*float*/  {	"boolean",	"boolean",	"error",	"error",	"error",	"error",	"error" },
			/*char*/   {	"error",	"error",	"error",	"error",	"error",	"error",	"error" },
			/*string*/ {	"error",	"error",	"error",	"error",	"error",	"error",	"error" },
			/*boolean*/{	"error",	"error",	"error",	"error",	"boolean",	"error",	"error" },
			/*void*/   {	"error",	"error",	"error",	"error",	"error",	"error",	"error" },
			/*error*/  {	"error",	"error",	"error",	"error",	"error",	"error",	"error" },
		},

		{
			/*
			 *Operator = "<="=>_LESS_EQUAL
		   			 			int		float		char		string		boolean		void		error*/
			/*int*/	   {	"boolean",	"boolean",	"error",	"error",	"error",	"error",	"error" },
			/*float*/  {	"boolean",	"boolean",	"error",	"error",	"error",	"error",	"error" },
			/*char*/   {	"error",	"error",	"error",	"error",	"error",	"error",	"error" },
			/*string*/ {	"error",	"error",	"error",	"error",	"error",	"error",	"error" },
			/*boolean*/{	"error",	"error",	"error",	"error",	"boolean",	"error",	"error" },
			/*void*/   {	"error",	"error",	"error",	"error",	"error",	"error",	"error" },
			/*error*/  {	"error",	"error",	"error",	"error",	"error",	"error",	"error" },
		},

		{
			/*
			 *Operator = ">="=>_GREAT_EQUAL
		   			 			int		float		char		string		boolean		void		error*/
			/*int*/	   {	"boolean",	"boolean",	"error",	"error",	"error",	"error",	"error" },
			/*float*/  {	"boolean",	"boolean",	"error",	"error",	"error",	"error",	"error" },
			/*char*/   {	"error",	"error",	"error",	"error",	"error",	"error",	"error" },
			/*string*/ {	"error",	"error",	"error",	"error",	"error",	"error",	"error" },
			/*boolean*/{	"error",	"error",	"error",	"error",	"boolean",	"error",	"error" },
			/*void*/   {	"error",	"error",	"error",	"error",	"error",	"error",	"error" },
			/*error*/  {	"error",	"error",	"error",	"error",	"error",	"error",	"error" },
		},

		{
			/*
			 *Operator = "=="=>_EQUAL_EQUAL
		   			 			int		float		char		string		boolean		void		error*/
			/*int*/	   {	"boolean",	"boolean",	"error",	"error",	"error",	"error",	"error" },
			/*float*/  {	"boolean",	"boolean",	"error",	"error",	"error",	"error",	"error" },
			/*char*/   {	"error",	"error",	"boolean",	"error",	"error",	"error",	"error" },
			/*string*/ {	"error",	"error",	"error",	"boolean",	"error",	"error",	"error" },
			/*boolean*/{	"error",	"error",	"error",	"error",	"boolean",	"error",	"error" },
			/*void*/   {	"error",	"error",	"error",	"error",	"error",	"error",	"error" },
			/*error*/  {	"error",	"error",	"error",	"error",	"error",	"error",	"error" },
		},

		{
			/*
			 *Operator = "-"=>_NEGATION
		   			int		float		char		string		boolean		void		error*/
			{	"int",		"int",		"error",	"error",	"error",	"error",	"error" }
		},
		
		{
			/*
			 *Operator = "!="=>_NOT_EQUAL
		   			 			int		float		char		string		boolean		void		error*/
			/*int*/	   {	"boolean",	"boolean",	"error",	"error",	"error",	"error",	"error" },
			/*float*/  {	"boolean",	"boolean",	"error",	"error",	"error",	"error",	"error" },
			/*char*/   {	"error",	"error",	"boolean",	"error",	"error",	"error",	"error" },
			/*string*/ {	"error",	"error",	"error",	"boolean",	"error",	"error",	"error" },
			/*boolean*/{	"error",	"error",	"error",	"error",	"boolean",	"error",	"error" },
			/*void*/   {	"error",	"error",	"error",	"error",	"error",	"error",	"error" },
			/*error*/  {	"error",	"error",	"error",	"error",	"error",	"error",	"error" },
		},


	};


	public static Hashtable<String, Vector<SymbolTableItem>> getSymbolTable() {
		return symbolTable;
	}

	public static void checkVariable(Gui gui1, String type, String id, int line) 
	{
		gui = gui1;

		// A. search the id in the symbol table
		if(!symbolTable.containsKey(id))
		{
			// B. if !exist then insert: type, scope=global, value={0, false, "", '')
			Vector v = new Vector();
			v.add(new SymbolTableItem(type,"global", ""));
			symbolTable.put(id, v);
		}
		// C. else error: variable id is already defined
		else
			error(gui1, 1, line, id);
		
	}

	public static void pushStack(String type) 
	{
		// push type in the stack
		stack.push(type);
	}

	public static String popStack() 
	{
		String result="";
		// pop a value from the stack
		if(!stack.isEmpty())
		{
			Object ob = stack.pop();
			return result=ob.toString()/*=stack.pop().toString()*/;
		}
		return result;
	}


	public static String calculateCube(String type, String operator) 
	{
		String result="";
		// unary operator ( - and !)
		int type1 = -1;
		int op=-1;
		if(type.equals("int")) type1 = _INT;
		else if(type.equals("float")) type1 = _FLOAT;
		else if(type.equals("char")) type1 = _CHAR;
		else if(type.equals("string")) type1 = _STRING;
		else if(type.equals("boolean")) type1 = _BOOLEAN;
		else if(type.equals("void")) type1 = _VOID;
		else if(type.equals("error")) type1 = _ERROR;
		else return result = "error";

		if(operator.equals("-")) op=_NEGATION;
		else if(operator.equals("!")) op=_NOT;
		else return result = "error";
		result = cube[op][0][type1];
		return result;
	}

	public static String calculateCube(String type1, String type2, String operator) {
		String result="";
		// binary operator
		int tp1 = -1;
		int tp2 = -1;
		int op=-1;
		if(type1.equals("int")) tp1 = _INT;
		else if(type1.equals("float")) tp1 = _FLOAT;
		else if(type1.equals("char")) tp1 = _CHAR;
		else if(type1.equals("string")) tp1 = _STRING;
		else if(type1.equals("boolean")) tp1 = _BOOLEAN;
		else if(type1.equals("void")) tp1 = _VOID;
		else if(type1.equals("error")) tp1 = _ERROR;
		else return result="error";

		if(type2.equals("int")) tp2 = _INT;
		else if(type2.equals("float")) tp2 = _FLOAT;
		else if(type2.equals("char")) tp2 = _CHAR;
		else if(type2.equals("string")) tp2 = _STRING;
		else if(type2.equals("boolean")) tp2 = _BOOLEAN;
		else if(type2.equals("void")) tp2 = _VOID;
		else if(type2.equals("error")) tp2 = _ERROR;
		else return result="error";

		if(operator.equals("+")) op=_ADD;
		else if(operator.equals("-")) op=_SUB;
		else if(operator.equals("*")) op=_MUL;
		else if(operator.equals("/")) op=_DIV;
		else if(operator.equals("=")) op=_ASSIGN;
		else if(operator.equals("&")) op=_AND;
		else if(operator.equals("|")) op=_OR;
		else if(operator.equals("<")) op=_LESS_THAN;
		else if(operator.equals(">")) op=_GREAT_THAN;
		else if(operator.equals("<=")) op=_LESS_EQUAL;
		else if(operator.equals(">=")) op=_GREAT_THAN;
		else if(operator.equals("==")) op=_EQUAL_EQUAL;
		else if(operator.equals("!=")) op=_NOT_EQUAL;
		else return result="error";

		result = cube[op][tp1][tp2];

		return result;
	}

	public static void error(Gui gui, int err, int n, String info) 
	{
		switch (err) {
		case 1: 
			gui.writeConsole("Line" + n + ": variable <"+ info +"> is already defined"); 
			break;
		case 2: 
			gui.writeConsole("Line" + n + ": incompatible types: type mismatch"); 
			break;
		case 3: 
			gui.writeConsole("Line" + n + ": incompatible types: expected boolean");
			break;
		case 4: 
			gui.writeConsole("Line" + n + ": variable <"+ info + "> not found");
			break;

		}
	}
	public static Stack getStack()
	{
		return stack;
	}

}
