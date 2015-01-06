package parser;

import java.util.Stack;
import java.util.Vector;

import javax.swing.tree.DefaultMutableTreeNode;

import codegenerator.CodeGenerator;
import compiler.Gui;
import lexer.*;
import semanticAnalyzer.*;

/**
 * @author GANESH R ZILPE
 * Description : This is Parser class and it is used to parse the tokens and generate parsing tree for it. It is called from Gui.java.
 * Grammar for the compiler:
 * 	<PROGRAM> 		-> '{' <BODY> '}’
	<BODY> 			-> {<PRINT>';'|<ASSIGNMENT>';'|<VARIABLE>';’|<WHILE>|<IF>|<SWITCH>|<RETURN>';'}
	<ASSIGNMENT> 	-> identifier '=' <EXPRESSION>
	<VARIABLE> 		-> ('int'|'float'|'boolean'|'char’|'string'|'void')identifier
	<WHILE> 		-> 'while' '(' <EXPRESSION> ')' <PROGRAM>
	<IF> 			-> 'if' '(' <EXPRESSION> ')' <PROGRAM> ['else' <PROGRAM>]
	<RETURN> 		-> 'return'
	<PRINT> 		-> ’print’ ‘(‘ <EXPRESSION> ‘)’
	<EXPRESSION> 	-> <X> {'|' <X>}
	<X> 			-> <Y> {'&' <Y>}
	<Y> 			-> ['!'] <R>
	<R> 			-> <E> {('>'|'<'|'=='|'!=') <E>}
	<E> 			-> <A> {(’+'|'-’) <A>}
	<A> 			-> <B> {('*'|'/') <B>}
	<B> 			-> ['-'] <C>
	<C> 			-> integer | octal | hexadecimal | binary | true | false | string | char | float | identifier|'(' <EXPRESSION> ')'
	<SWITCH> 		-> 'switch' '(' identifier ')' '{' <CASES> [<DEFAULT>] '}'
	<CASES> 		-> ('case' (integer|octal|hexadecimal|binary) ':' <PROGRAM>)+
	<DEFAULT> 		-> 'default' ':' <PROGRAM>
 */
public class Parser {

	private static DefaultMutableTreeNode root;
	private static Vector<Token> tokens;
	private static int currentToken;
	private static Gui gui;

	static String tokenName = "";
	static String tokenWord = "";
	static int labelCounter = 0; //codegenerator


	public static DefaultMutableTreeNode run(Vector<Token> t, Gui guiTemp) 
	{
		tokens = t;
		gui = guiTemp;
		currentToken = 0;
		root = new DefaultMutableTreeNode("PROGRAM");

		SemanticAnalyzer.getSymbolTable().clear();
		CodeGenerator.clear(gui);
		labelCounter = 0;
		//
		rule_program(root);//rule_expression(root);
		//

		gui.writeSymbolTable(SemanticAnalyzer.getSymbolTable());
		CodeGenerator.writeCode(gui);

		return root;
	}

	public static void rule_program(DefaultMutableTreeNode parent) 
	{
		if (currentToken < tokens.size() && tokens.get(currentToken).getWord().equals("{"))
		{
			currentToken++;
		}
		else 
		{
			error(1);
			// ignore the tokens which are useless or not necessary for this rule
			while(currentToken < tokens.size())
			{
				tokenName = tokens.get(currentToken).getToken();
				tokenWord = tokens.get(currentToken).getWord();
				if(!(tokenWord.equals("print") || (tokenName.equals("IDENTIFIER")) || ((tokenName.equals("KEYWORD")) && (tokenWord.equals("int") || tokenWord.equals("float") || tokenWord.equals("boolean") || tokenWord.equals("char") || tokenWord.equals("string") || tokenWord.equals("void") || tokenWord.equals("while") || tokenWord.equals("if"))) || (tokenWord.equals("return")) || (tokenWord.equals("}")) || (tokenWord.equals("default")) ))
				{
					DefaultMutableTreeNode node;
					if(currentToken < tokens.size() && tokens.get(currentToken).getToken().equals("ERROR"))
					{
						node = new DefaultMutableTreeNode("error ("+tokens.get(currentToken).getWord()+")");
						parent.add(node);
					}

					currentToken++;
				}
				else 
					break;
			}
		}
		DefaultMutableTreeNode node;
		node = new DefaultMutableTreeNode("BODY");
		parent.add(node);
		rule_body(node); 

		if (currentToken < tokens.size() && tokens.get(currentToken).getWord().equals("}")) 
		{
			currentToken++;
			//code generator
			if(currentToken == tokens.size())
				CodeGenerator.addInstruction("OPR", "0", "0");
			//code generator
			return; //curentToken++;
		}
		else 
		{
			if(currentToken < tokens.size() && tokens.get(currentToken).getToken().equals("ERROR"))
			{
				node = new DefaultMutableTreeNode("error ("+tokens.get(currentToken).getWord()+")");
				parent.add(node);
			}
			error(2);
			return; //currentToken++;
		}

	}

	public static boolean rule_body(DefaultMutableTreeNode parent) 
	{
		boolean error = false;
		DefaultMutableTreeNode node;

		while (currentToken < tokens.size() && !(tokens.get(currentToken).getWord().equals("}"))) 
		{ 
			if(currentToken-1>=0 && tokens.get(currentToken).getLine()>tokens.get(currentToken-1).getLine())
				SemanticAnalyzer.getStack().clear();

			String tokenName = tokens.get(currentToken).getToken();
			String tokenWord = tokens.get(currentToken).getWord();
			if(tokenWord.equals("print"))
			{
				node = new DefaultMutableTreeNode("PRINT");
				parent.add(node);
				error = rule_print(node);

				if (currentToken < tokens.size() && tokens.get(currentToken).getWord().equals(";"))
				{
					node = new DefaultMutableTreeNode(";");
					parent.add(node);
					currentToken++;
				}
				else
				{
					if(currentToken < tokens.size() && tokens.get(currentToken).getToken().equals("ERROR"))
					{
						node = new DefaultMutableTreeNode("error ("+tokens.get(currentToken).getWord()+")");
						parent.add(node);
					}
					error(3);
					if(currentToken+1 < tokens.size() && tokens.get(currentToken).getLine()<tokens.get(currentToken+1).getLine())
						currentToken++;
					else
						continue;
				}
			}
			else if(tokenName.equals("IDENTIFIER"))
			{
				node = new DefaultMutableTreeNode("ASSIGNMENT");
				parent.add(node);
				rule_assignment(node);
				if (currentToken < tokens.size() && tokens.get(currentToken).getWord().equals(";")) 
				{
					node = new DefaultMutableTreeNode(";");
					parent.add(node);
					currentToken++;
				}
				else 
				{
					//DefaultMutableTreeNode node;
					if(currentToken < tokens.size() && tokens.get(currentToken).getToken().equals("ERROR"))
					{
						node = new DefaultMutableTreeNode("error ("+tokens.get(currentToken).getWord()+")");
						parent.add(node);
					}
					error(3);
					if(currentToken+1 < tokens.size() && tokens.get(currentToken).getLine()<tokens.get(currentToken+1).getLine())
						currentToken++;
					else
						continue;
				}
			}
			else if((tokenName.equals("KEYWORD")) && (tokenWord.equals("int") || tokenWord.equals("float") || tokenWord.equals("boolean") || tokenWord.equals("char") || tokenWord.equals("string") || tokenWord.equals("void")))
			{
				node = new DefaultMutableTreeNode("VARIABLE");
				parent.add(node);
				rule_variable(node);
				if (currentToken < tokens.size() && tokens.get(currentToken).getWord().equals(";")) 
				{
					node = new DefaultMutableTreeNode(";");
					parent.add(node);
					if(currentToken+1 < tokens.size() && tokens.get(currentToken).getLine()<tokens.get(currentToken+1).getLine())
						currentToken++;
					else
					{
						currentToken++;
						continue;
					}
				}
				else 
				{
					//DefaultMutableTreeNode node;
					if(currentToken < tokens.size() && tokens.get(currentToken).getToken().equals("ERROR"))
					{
						node = new DefaultMutableTreeNode("error ("+tokens.get(currentToken).getWord()+")");
						parent.add(node);
					}
					error(3);
					if(currentToken+1 < tokens.size() && tokens.get(currentToken).getLine()<tokens.get(currentToken+1).getLine())
						currentToken++;
					else
						continue;
				}
			}
			else if(tokenName.equals("KEYWORD") && tokenWord.equals("while"))
			{
				//DefaultMutableTreeNode node;
				node = new DefaultMutableTreeNode("WHILE");
				parent.add(node);
				rule_while(node);
			} 
			else if(tokenName.equals("KEYWORD") && tokenWord.equals("if"))
			{
				node = new DefaultMutableTreeNode("IF");
				parent.add(node);
				rule_if(node);
			} 
			else if(tokenWord.equals("switch"))
			{
				//DefaultMutableTreeNode node;
				node = new DefaultMutableTreeNode("SWITCH");
				parent.add(node);
				rule_switch(node);
			}
			else if(tokenWord.equals("return"))
			{
				//DefaultMutableTreeNode node;
				node = new DefaultMutableTreeNode("RETURN");
				parent.add(node);
				rule_return(node);
				if (currentToken < tokens.size() && tokens.get(currentToken).getWord().equals(";"))
				{
					node = new DefaultMutableTreeNode(";");
					parent.add(node);
					currentToken++;
				}
				else 
				{
					//DefaultMutableTreeNode node;
					if(currentToken < tokens.size() && tokens.get(currentToken).getToken().equals("ERROR"))
					{
						node = new DefaultMutableTreeNode("error ("+tokens.get(currentToken).getWord()+")");
						parent.add(node);
					}
					error(3);
					if(currentToken+1 < tokens.size() && tokens.get(currentToken).getLine()<tokens.get(currentToken+1).getLine())
						currentToken++;
					else
						continue;
				}
			}
			
			else
			{
				try
				{
					if(tokens.get(currentToken).getWord().equals("default") && (parent.getParent().getParent().toString().equals("CASE")|| parent.getParent().getParent().toString().equals("DEFAULT")))
						return false;
				}
				catch(Exception e)
				{

				}
				error(4);
				// ignore the tokens which are useless or not necessary for this rule
				while(currentToken < tokens.size())
				{
					tokenName = tokens.get(currentToken).getToken();
					tokenWord = tokens.get(currentToken).getWord();
					//DefaultMutableTreeNode node;
					if(currentToken < tokens.size() && tokens.get(currentToken).getToken().equals("ERROR"))
					{
						node = new DefaultMutableTreeNode("error ("+tokens.get(currentToken).getWord()+")");
						parent.add(node);
					}
					if(tokenWord.equals("print") || (tokenName.equals("IDENTIFIER")) || ((tokenName.equals("KEYWORD")) && (tokenWord.equals("int") || tokenWord.equals("float") || tokenWord.equals("boolean") || tokenWord.equals("char") || tokenWord.equals("string") || tokenWord.equals("void") || tokenWord.equals("while") || tokenWord.equals("if"))) || (tokenWord.equals("return")) || (tokenWord.equals("}")) || (tokenWord.equals("switch")))
					{
						break;
					}
					else
						currentToken++;
				}
			}


		}
		return true;
	}

	public static boolean rule_assignment(DefaultMutableTreeNode parent) 
	{
		DefaultMutableTreeNode node;

		String operator = null;

		node = new DefaultMutableTreeNode("identifier" + "(" + tokens.get(currentToken).getWord() + ")");
		//code generator
		String  id= tokens.get(currentToken).getWord();
		//code generator
		parent.add(node);

		if(SemanticAnalyzer.getSymbolTable().containsKey(tokens.get(currentToken).getWord()))
			SemanticAnalyzer.pushStack(SemanticAnalyzer.getSymbolTable().get(tokens.get(currentToken).getWord()).get(0).getType());
		else
			SemanticAnalyzer.error(gui, 4, tokens.get(currentToken).getLine(), tokens.get(currentToken).getWord());

		if(currentToken+1 < tokens.size() && tokens.get(currentToken).getLine()<tokens.get(currentToken+1).getLine())
		{
			error(5);
			error(9);
			return true;
		}
		currentToken++;

		if(currentToken < tokens.size() && tokens.get(currentToken).getWord().equals("="))
		{

			node = new DefaultMutableTreeNode("'='");
			parent.add(node);
			if(currentToken+1 < tokens.size() && tokens.get(currentToken).getLine()<tokens.get(currentToken+1).getLine())
			{
				error(9);
				return true;
			}
			operator = "=";
			currentToken++;
		}
		else
		{
			//DefaultMutableTreeNode node;

			error(5);
			// ignore the tokens which are useless or not necessary for this rule
			while(currentToken < tokens.size())
			{
				tokenName = tokens.get(currentToken).getToken();
				tokenWord = tokens.get(currentToken).getWord();

				if(currentToken < tokens.size() && tokens.get(currentToken).getToken().equals("ERROR"))
				{
					node = new DefaultMutableTreeNode("error ("+tokens.get(currentToken).getWord()+")");
					parent.add(node);
				}

				if(tokenName.equals("CHARACTER") || tokenName.equals("STRING") || tokenName.equals("IDENTIFIER") || tokenName.equals("FLOAT") || tokenName.equals("INTEGER") || tokenName.equals("HEXADECIMAL") || tokenName.equals("OCTAL") || tokenName.equals("BINARY") || tokenWord.equals("true") || tokenWord.equals("false") || tokenWord.equals("!") || tokenWord.equals("-") || tokenWord.equals(")") || tokenWord.equals(";"))
				{
					break;
				}
				if(currentToken+1 < tokens.size() && tokens.get(currentToken).getLine()<tokens.get(currentToken+1).getLine())
				{
					error(9);
					return true;
				}
				currentToken++;
			}
		}
		if(currentToken< tokens.size())
		{
			boolean isEqual = false;
			if(tokens.get(currentToken-1).getWord().equals("="))
				isEqual = true;
			node = new DefaultMutableTreeNode("EXPRESSION");
			parent.add(node);
			rule_expression(node);

			if(isEqual)
			{
				String type2 = SemanticAnalyzer.popStack(); 
				String type1 = SemanticAnalyzer.popStack();
				String result = SemanticAnalyzer.calculateCube(type1, type2, operator );
				if(result.equals("error"))
					SemanticAnalyzer.error(gui, 2, tokens.get(currentToken).getLine(), tokens.get(currentToken).getWord());
				SemanticAnalyzer.pushStack(result); 
				CodeGenerator.addInstruction("STO", id, "0");
			}
		}
		else 
			return false;
		return true;
	} 

	public static boolean rule_variable(DefaultMutableTreeNode parent) 
	{
		DefaultMutableTreeNode node;

		if (currentToken < tokens.size()) 
		{
			node = new DefaultMutableTreeNode("keyword" + "(" + tokens.get(currentToken).getWord() + ")");
			parent.add(node);
			if(currentToken+1 < tokens.size() && tokens.get(currentToken).getLine()<tokens.get(currentToken+1).getLine())
			{
				error(6);
				currentToken++;
				return true;
			}
			currentToken++;
		}
		if (currentToken < tokens.size() && tokens.get(currentToken).getToken().equals("IDENTIFIER")) 
		{
			SemanticAnalyzer.checkVariable(gui, tokens.get(currentToken-1).getWord(), tokens.get(currentToken).getWord(), tokens.get(currentToken).getLine());
			//code generator
			CodeGenerator.addVariable(tokens.get(currentToken-1).getWord(), tokens.get(currentToken).getWord());
			//code generator

			node = new DefaultMutableTreeNode("identifier" + "(" + tokens.get(currentToken).getWord() + ")");
			parent.add(node);
			if(tokens.get(currentToken).getLine()<tokens.get(currentToken+1).getLine())
				return true;
			currentToken++;
		}
		else
		{
			//DefaultMutableTreeNode node;
			if(currentToken < tokens.size() && tokens.get(currentToken).getToken().equals("ERROR"))
			{
				node = new DefaultMutableTreeNode("error ("+tokens.get(currentToken).getWord()+")");
				parent.add(node);
			}
			error(6);
		}
		return true;
	}

	public static boolean rule_while(DefaultMutableTreeNode parent) 
	{
		boolean error = false;
		DefaultMutableTreeNode node;
		int whileConditionLine = 0; //code_generator
		Stack stackOfLabel = new Stack(); //code_generator
		node = new DefaultMutableTreeNode("while");
		parent.add(node);
		if(currentToken+1 < tokens.size() && tokens.get(currentToken).getLine()<tokens.get(currentToken+1).getLine())
			;
		else
			currentToken++;

		if (currentToken < tokens.size() && tokens.get(currentToken).getWord().equals("(")) 
		{
			whileConditionLine = CodeGenerator.getInstructions().size()+1; //code_generator
			node = new DefaultMutableTreeNode("(");
			parent.add(node);
			if(currentToken+1 < tokens.size() && tokens.get(currentToken).getLine()<tokens.get(currentToken+1).getLine())
				;
			else
				currentToken++;
		}
		else
		{
			error(8);// : expecting (
			// ignore the tokens which are useless or not necessary for this rule
			while(currentToken < tokens.size())
			{
				tokenName = tokens.get(currentToken).getToken();
				tokenWord = tokens.get(currentToken).getWord();

				if(tokenName.equals("CHARACTER") || tokenName.equals("STRING") || tokenName.equals("IDENTIFIER") || tokenName.equals("FLOAT") || tokenName.equals("INTEGER") || tokenName.equals("HEXADECIMAL") || tokenName.equals("OCTAL") || tokenName.equals("BINARY") || tokenWord.equals("true") || tokenWord.equals("false") || tokenWord.equals("!") || tokenWord.equals("-") || tokenWord.equals(")") || tokenWord.equals("}"))
				{
					break;
				}
				//DefaultMutableTreeNode node;
				if(currentToken < tokens.size() && tokens.get(currentToken).getToken().equals("ERROR"))
				{
					node = new DefaultMutableTreeNode("error ("+tokens.get(currentToken).getWord()+")");
					parent.add(node);
				}
				if(currentToken+1 < tokens.size() && tokens.get(currentToken).getLine()<tokens.get(currentToken+1).getLine())
				{
					break;
				}
				currentToken++;
			}
		}
		int sizeOfStack = SemanticAnalyzer.getStack().size();
		if(currentToken < tokens.size())
		{
			node = new DefaultMutableTreeNode("EXPRESSION");
			parent.add(node);
			rule_expression(node);
		}

		//before popping up values from stack, first check whether something adding or not during above rule_expression or not
		if(sizeOfStack != SemanticAnalyzer.getStack().size())
		{
			if(!SemanticAnalyzer.popStack().equals("boolean"))
				SemanticAnalyzer.error(gui, 3, tokens.get(currentToken).getLine(), tokens.get(currentToken).getWord());
		}

		if (currentToken < tokens.size() && tokens.get(currentToken).getWord().equals(")")) 
		{
			node = new DefaultMutableTreeNode(")");
			parent.add(node);
			//code_generator
			labelCounter++;
			CodeGenerator.addInstruction("JMC", "#e"+labelCounter, "false");
			stackOfLabel.push("e"+labelCounter);
			//code_generator
			currentToken++;
		}
		else
		{
			error(7);// : expecting )
			// ignore the tokens which are useless or not necessary for this rule
			while(currentToken < tokens.size())
			{
				tokenWord = tokens.get(currentToken).getWord();

				if(tokenWord.equals("{") || currentToken == tokens.size() || tokenWord.equals("}"))
				{
					break;
				}
				//DefaultMutableTreeNode node;
				if(currentToken < tokens.size() && tokens.get(currentToken).getToken().equals("ERROR"))
				{
					node = new DefaultMutableTreeNode("error ("+tokens.get(currentToken).getWord()+")");
					parent.add(node);
				}
				
				currentToken++;
			}
		}
		if(currentToken < tokens.size())
		{

			node = new DefaultMutableTreeNode("PROGRAM");
			parent.add(node);
			rule_program(node);
			//code_generator
			if(!stackOfLabel.isEmpty())
			{
				String label1 = (String) stackOfLabel.pop();
				CodeGenerator.addLabel(label1, CodeGenerator.getInstructions().size()+2);
			}
			labelCounter++;
			CodeGenerator.addInstruction("JMP", "#e"+labelCounter, "0");
			stackOfLabel.push("e"+labelCounter);
			if(!stackOfLabel.isEmpty())
			{
				String label1 = (String) stackOfLabel.pop();
				CodeGenerator.addLabel(label1, whileConditionLine);
			}
			//code_generator
		}

		return true;
	}

	public static boolean rule_if(DefaultMutableTreeNode parent) 
	{
		boolean error = false;
		Stack stackOfLabel = new Stack(); //code_generator
		DefaultMutableTreeNode node;
		node = new DefaultMutableTreeNode("if");
		parent.add(node);
		if(currentToken+1 < tokens.size() && tokens.get(currentToken).getLine()<tokens.get(currentToken+1).getLine())
			;
		else
			currentToken++;

		if (currentToken < tokens.size() && tokens.get(currentToken).getWord().equals("(")) 
		{
			node = new DefaultMutableTreeNode("(");
			parent.add(node);
			if(currentToken+1 < tokens.size() && tokens.get(currentToken).getLine()<tokens.get(currentToken+1).getLine())
				;
			else
				currentToken++;
		}
		else
		{
			error(8);// : expecting (
			// ignore the tokens which are useless or not necessary for this rule
			while(currentToken < tokens.size())
			{
				tokenName = tokens.get(currentToken).getToken();
				tokenWord = tokens.get(currentToken).getWord();

				if(tokenName.equals("CHARACTER") || tokenName.equals("STRING") || tokenName.equals("IDENTIFIER") || tokenName.equals("FLOAT") || tokenName.equals("INTEGER") || tokenName.equals("HEXADECIMAL") || tokenName.equals("OCTAL") || tokenName.equals("BINARY") || tokenWord.equals("true") || tokenWord.equals("false") || tokenWord.equals("!") || tokenWord.equals("-") || tokenWord.equals(")") || tokenWord.equals("}"))
				{
					break;
				}
				//DefaultMutableTreeNode node;
				if(currentToken < tokens.size() && tokens.get(currentToken).getToken().equals("ERROR"))
				{
					node = new DefaultMutableTreeNode("error ("+tokens.get(currentToken).getWord()+")");
					parent.add(node);
				}
				if(currentToken+1 < tokens.size() && tokens.get(currentToken).getLine()<tokens.get(currentToken+1).getLine())
				{
					break;
				}
				currentToken++;
			}
		}
		if(currentToken < tokens.size())
		{
			node = new DefaultMutableTreeNode("EXPRESSION");
			parent.add(node);
			rule_expression(node);
		}

		//node = new DefaultMutableTreeNode("EXPRESSION");
		//parent.add(node);
		//rule_expression(node);

		if(!SemanticAnalyzer.popStack().equals("boolean"))
			SemanticAnalyzer.error(gui, 3, tokens.get(currentToken).getLine(), tokens.get(currentToken).getWord()); 

		if (currentToken < tokens.size() && tokens.get(currentToken).getWord().equals(")")) 
		{
			node = new DefaultMutableTreeNode(")");
			parent.add(node);
			//code_generator
			labelCounter++;
			CodeGenerator.addInstruction("JMC", "#e"+labelCounter, "false");
			stackOfLabel.push("e"+labelCounter);
			//code_generator
			
			currentToken++;
		}
		else
		{

			error(7);// : expecting )
			// ignore the tokens which are useless or not necessary for this rule
			while(currentToken < tokens.size())
			{
				tokenWord = tokens.get(currentToken).getWord();

				if(tokenWord.equals("{") || currentToken == tokens.size() || tokenWord.equals("}"))
				{
					break;
				}
				//DefaultMutableTreeNode node;
				if(currentToken < tokens.size() && tokens.get(currentToken).getToken().equals("ERROR"))
				{
					node = new DefaultMutableTreeNode("error ("+tokens.get(currentToken).getWord()+")");
					parent.add(node);
				}
				//if(currentToken+1 < tokens.size() && tokens.get(currentToken).getLine()<tokens.get(currentToken+1).getLine())
				//break;
				currentToken++;
			}
		}
		if(currentToken < tokens.size())
		{
			boolean isElsePresent = false;
			node = new DefaultMutableTreeNode("PROGRAM");
			parent.add(node);

			rule_program(node);
			if(currentToken < tokens.size() && tokens.get(currentToken).getWord().equals("else"))
			{
				if(!stackOfLabel.isEmpty())
				{
					String label1 = (String) stackOfLabel.pop();
					CodeGenerator.addLabel(label1, CodeGenerator.getInstructions().size()+2);
				}
			}
			else
				if(!stackOfLabel.isEmpty())
				{
					String label1 = (String) stackOfLabel.pop();
					CodeGenerator.addLabel(label1, CodeGenerator.getInstructions().size()+1);
				}


			//code_generator
			if(currentToken < tokens.size() && tokens.get(currentToken).getWord().equals("else"))
			{
				//Code_generator
				isElsePresent = true;
				labelCounter++;
				CodeGenerator.addInstruction("JMP", "#e"+labelCounter, "0");
				stackOfLabel.push("e"+labelCounter);
				//Code_generator
				node = new DefaultMutableTreeNode("else");
				parent.add(node);
				//if(tokens.get(currentToken).getLine()<tokens.get(currentToken+1).getLine())
				//	return true;
				currentToken++;

				node = new DefaultMutableTreeNode("PROGRAM");
				parent.add(node);
				rule_program(node);
				if(!stackOfLabel.isEmpty())
				{
					String label1 = (String) stackOfLabel.pop();
					CodeGenerator.addLabel(label1, CodeGenerator.getInstructions().size()+1);
				}
			}
		}
		return true;
	}

	public static boolean rule_return(DefaultMutableTreeNode parent) 
	{
		DefaultMutableTreeNode node;
		node = new DefaultMutableTreeNode("return");
		parent.add(node);
		//codegenerator
		CodeGenerator.addInstruction("OPR", "1", "0");
		//codegenerator
		if(tokens.get(currentToken).getLine()<tokens.get(currentToken+1).getLine())
			return true;
		currentToken++;
		return true;
	}


	public static boolean rule_switch(DefaultMutableTreeNode parent) 
	{
		DefaultMutableTreeNode node;
		Stack stackOfLabelOfSwitch = new Stack(); //code_generator
		node = new DefaultMutableTreeNode("switch");
		parent.add(node);
		String id = "";
		if(currentToken+1 < tokens.size() && tokens.get(currentToken).getLine()<tokens.get(currentToken+1).getLine())
			;
		else
			currentToken++;
		if (currentToken < tokens.size() && tokens.get(currentToken).getWord().equals("(")) 
		{
			node = new DefaultMutableTreeNode("(");
			parent.add(node);
			if(currentToken+1 < tokens.size() && tokens.get(currentToken).getLine()<tokens.get(currentToken+1).getLine())
				;
			else
				currentToken++;
		}
		else
		{
			error(8);// : expecting (
			// ignore the tokens which are useless or not necessary for this rule
			while(currentToken < tokens.size())
			{
				tokenName = tokens.get(currentToken).getToken();
				tokenWord = tokens.get(currentToken).getWord();

				if(tokenName.equals("IDENTIFIER") ||  tokenWord.equals(")") || tokenWord.equals("{"))
				{
					break;
				}
				//DefaultMutableTreeNode node;
				if(currentToken < tokens.size() && tokens.get(currentToken).getToken().equals("ERROR"))
				{
					node = new DefaultMutableTreeNode("error ("+tokens.get(currentToken).getWord()+")");
					parent.add(node);
				}
				if(currentToken+1 < tokens.size() && tokens.get(currentToken).getLine()<tokens.get(currentToken+1).getLine())
				{
					break;
				}
				currentToken++;
			}
		}
		if(currentToken< tokens.size() && tokens.get(currentToken).getToken().equals("IDENTIFIER"))
		{
			id = tokens.get(currentToken).getWord();//code_generator
			node = new DefaultMutableTreeNode(tokens.get(currentToken).getWord());
			parent.add(node);
			if(currentToken+1 < tokens.size() && tokens.get(currentToken).getLine()<tokens.get(currentToken+1).getLine())
				;
			else
				currentToken++;
		}
		else
		{
			error(6);
		}

		if (currentToken < tokens.size() && tokens.get(currentToken).getWord().equals(")")) 
		{
			node = new DefaultMutableTreeNode(")");
			parent.add(node);
			currentToken++;
		}
		else
		{
			error(7);// : expecting )
			// ignore the tokens which are useless or not necessary for this rule
			while(currentToken < tokens.size())
			{
				tokenWord = tokens.get(currentToken).getWord();

				if(tokenWord.equals("{") || currentToken == tokens.size() || tokenWord.equals("}") || tokens.get(currentToken).getWord().equals("case") || tokens.get(currentToken).getWord().equals("default"))
				{
					break;
				}
				//DefaultMutableTreeNode node;
				if(currentToken < tokens.size() && tokens.get(currentToken).getToken().equals("ERROR"))
				{
					node = new DefaultMutableTreeNode("error ("+tokens.get(currentToken).getWord()+")");
					parent.add(node);
				}
				
				currentToken++;
			}
		}
		if (currentToken < tokens.size() && tokens.get(currentToken).getWord().equals("{"))
		{
			currentToken++;
		}
		else 
		{
			error(1);
			// ignore the tokens which are useless or not necessary for this rule
			while(currentToken < tokens.size())
			{
				tokenName = tokens.get(currentToken).getToken();
				tokenWord = tokens.get(currentToken).getWord();

				if( currentToken == tokens.size() || tokenWord.equals("}") || tokens.get(currentToken).getWord().equals("case") || tokens.get(currentToken).getWord().equals("default"))
				{
					break;
				}
				currentToken++;
			}
		}	
		if(currentToken < tokens.size() && tokens.get(currentToken).getWord().equals("case"))
		{

			node = new DefaultMutableTreeNode("CASE");
			parent.add(node);
			rule_case(node, id, stackOfLabelOfSwitch);

			while(currentToken < tokens.size() && tokens.get(currentToken).getWord().equals("case"))
				rule_case(parent, id, stackOfLabelOfSwitch);

		}
		else
		{
			error(12); //expected case
			// ignore the tokens which are useless or not necessary for this rule
			while(currentToken < tokens.size())
			{
				tokenName = tokens.get(currentToken).getToken();
				tokenWord = tokens.get(currentToken).getWord();

				if( currentToken == tokens.size() || tokenWord.equals("}") || tokens.get(currentToken).getWord().equals("case") || tokens.get(currentToken).getWord().equals("default"))
				{
					break;
				}
				currentToken++;
			}
		}

		if(currentToken < tokens.size() && tokens.get(currentToken).getWord().equals("default"))
		{
			node = new DefaultMutableTreeNode("DEFAULT");
			parent.add(node);
			rule_default(node);
		}

		if (currentToken < tokens.size() && tokens.get(currentToken).getWord().equals("}"))
		{
			while(!stackOfLabelOfSwitch.isEmpty())
			{
				String label1 = (String) stackOfLabelOfSwitch.pop();
				CodeGenerator.addLabel(label1, CodeGenerator.getInstructions().size()+1);
			}
			currentToken++;
		}
		else 
		{
			error(2);
		}

		return false;

	}

	public static boolean rule_case(DefaultMutableTreeNode parent, String id, Stack stackOfLabelOfSwitch) 
	{
		boolean error = false;

		DefaultMutableTreeNode node;

		//code generator
		if(id!= "")
		{
			CodeGenerator.addInstruction("LOD", id, "0");
		}
		//code generator

		node = new DefaultMutableTreeNode("case");
		parent.add(node);
		if(currentToken+1 < tokens.size() && tokens.get(currentToken).getLine()<tokens.get(currentToken+1).getLine())
			;
		else
			currentToken++;
		//return true;

		//check value after case is int/bin/hex/oct or not
		if (currentToken < tokens.size() && (tokens.get(currentToken).getToken().equals("INTEGER") || tokens.get(currentToken).getToken().equals("HEXADECIMAL") || tokens.get(currentToken).getToken().equals("BINARY") || tokens.get(currentToken).getToken().equals("OCTAL"))) 
		{
			node = new DefaultMutableTreeNode(tokens.get(currentToken).getWord());
			parent.add(node);

			//code generator
			CodeGenerator.addInstruction("LIT", tokens.get(currentToken).getWord(), "0"); //code generator
			//code generator
			if(currentToken+1 < tokens.size() && tokens.get(currentToken).getLine()<tokens.get(currentToken+1).getLine())
				;
			else
				currentToken++;
		}
		else
		{
			error(10);// : expecting int/bin/oct/hex
			// ignore the tokens which are useless or not necessary for this rule
			while(currentToken < tokens.size())
			{
				tokenName = tokens.get(currentToken).getToken();
				tokenWord = tokens.get(currentToken).getWord();

				if(tokenWord.equals("}") ||  tokenWord.equals(":") || tokenWord.equals("{"))
				{
					break;
				}
				//DefaultMutableTreeNode node;
				if(currentToken < tokens.size() && tokens.get(currentToken).getToken().equals("ERROR"))
				{
					node = new DefaultMutableTreeNode("error ("+tokens.get(currentToken).getWord()+")");
					parent.add(node);
				}
				if(currentToken+1 < tokens.size() && tokens.get(currentToken).getLine()<tokens.get(currentToken+1).getLine())
				{
					break;
				}
				currentToken++;
			}
		}
		if(currentToken< tokens.size() && tokens.get(currentToken).getWord().equals(":"))
		{
			node = new DefaultMutableTreeNode(tokens.get(currentToken).getWord());
			parent.add(node);

			//code generator
			CodeGenerator.addInstruction("OPR", "15", "0"); 
			//code generator

			if(currentToken+1< tokens.size() && tokens.get(currentToken+1).getWord().equals("{"))
				currentToken++;
		}
		else
		{
			error(11);
		}

		if(currentToken < tokens.size())
		{
			//code_generator
			labelCounter++;
			CodeGenerator.addInstruction("JMC", "#e"+labelCounter, "false");
			stackOfLabelOfSwitch.push("e"+labelCounter);
			//code_generator
			node = new DefaultMutableTreeNode("PROGRAM");
			parent.add(node);
			rule_program(node);

			//code_generator
			if(!stackOfLabelOfSwitch.isEmpty())
			{
				String label1 = (String) stackOfLabelOfSwitch.pop();
				if(currentToken < tokens.size() && (tokens.get(currentToken).getWord().equals("case") || tokens.get(currentToken).getWord().equals("default")))
					CodeGenerator.addLabel(label1, CodeGenerator.getInstructions().size()+2);
				else
					CodeGenerator.addLabel(label1, CodeGenerator.getInstructions().size()+1);
			}
			if(currentToken < tokens.size() && (tokens.get(currentToken).getWord().equals("case") || tokens.get(currentToken).getWord().equals("default")))
			{
				labelCounter++;
				CodeGenerator.addInstruction("JMP", "#e"+labelCounter, "0");
				stackOfLabelOfSwitch.push("e"+labelCounter);
			}

		}

		return true;
	}

	public static boolean rule_default(DefaultMutableTreeNode parent) 
	{
		boolean error = false;

		DefaultMutableTreeNode node;

		node = new DefaultMutableTreeNode("default");
		parent.add(node);
		if(currentToken+1 < tokens.size() && tokens.get(currentToken).getLine()<tokens.get(currentToken+1).getLine())
			;
		else
			currentToken++;
		//return true;

		if(currentToken< tokens.size() && tokens.get(currentToken).getWord().equals(":"))
		{
			node = new DefaultMutableTreeNode(tokens.get(currentToken).getWord());
			parent.add(node);
			if(currentToken+1< tokens.size() && tokens.get(currentToken+1).getWord().equals("{"))
				currentToken++;
		}
		else
		{
			error(11);
		}

		if(currentToken < tokens.size())
		{
			node = new DefaultMutableTreeNode("PROGRAM");
			parent.add(node);
			rule_program(node);
		}
		//if(currentToken< tokens.size() && (tokens.get(currentToken).getWord().equals(":")||tokens.get(currentToken).getWord().equals(":")) && tempCurrentToken == currentToken)
		//currentToken++;


		return true;
	}

	public static boolean rule_print(DefaultMutableTreeNode parent) 
	{
		boolean error = false;

		DefaultMutableTreeNode node;

		node = new DefaultMutableTreeNode("print");
		parent.add(node);
		currentToken++;


		if (currentToken < tokens.size() && tokens.get(currentToken).getWord().equals("(")) 
		{
			node = new DefaultMutableTreeNode("(");
			parent.add(node);
			if(tokens.get(currentToken).getLine()<tokens.get(currentToken+1).getLine())
				return true;
			currentToken++;
		}
		else
		{
			error(8);// : expecting (
			// ignore the tokens which are useless or not necessary for this rule
			while(currentToken < tokens.size())
			{
				tokenName = tokens.get(currentToken).getToken();
				tokenWord = tokens.get(currentToken).getWord();

				//DefaultMutableTreeNode node;
				if(tokenName.equals("CHARACTER") || tokenName.equals("STRING") || tokenName.equals("IDENTIFIER") || tokenName.equals("FLOAT") || tokenName.equals("INTEGER") || tokenName.equals("HEXADECIMAL") || tokenName.equals("OCTAL") || tokenName.equals("BINARY") || tokenWord.equals("true") || tokenWord.equals("false") || tokenWord.equals("!") || tokenWord.equals("-") || tokenWord.equals(")"))
				{
					break;
				}
				if(currentToken < tokens.size() && tokens.get(currentToken).getToken().equals("ERROR"))
				{
					node = new DefaultMutableTreeNode("error ("+tokens.get(currentToken).getWord()+")");
					parent.add(node);
				}
				if(currentToken+1 < tokens.size() && tokens.get(currentToken).getLine()<tokens.get(currentToken+1).getLine())
				{
					currentToken++;
					return true;
				}
				currentToken++;
			}
		}
		if(currentToken < tokens.size())
		{
			node = new DefaultMutableTreeNode("EXPRESSION");
			parent.add(node);
			rule_expression(node);
		}

		if (currentToken < tokens.size() && tokens.get(currentToken).getWord().equals(")")) 
		{
			node = new DefaultMutableTreeNode(")");
			parent.add(node);
			//codegenerator
			CodeGenerator.addInstruction("OPR", "21", "0");
			//codegenerator
			if ( tokens.get(currentToken).getLine() < tokens.get(currentToken+1).getLine() ) 
			{
				return true;
			}
			currentToken++;
			error = true;
		}
		else
		{
			//DefaultMutableTreeNode node;
			if(currentToken < tokens.size() && tokens.get(currentToken).getToken().equals("ERROR"))
			{
				node = new DefaultMutableTreeNode("error ("+tokens.get(currentToken).getWord()+")");
				parent.add(node);
			}
			error(7);// : expecting )
		}

		return true;
	}

	private static boolean rule_expression(DefaultMutableTreeNode parent) 
	{
		boolean error;
		DefaultMutableTreeNode node;
		node = new DefaultMutableTreeNode("X");
		parent.add(node);
		String operator = null;
		rule_X(node);
		while (currentToken < tokens.size() && tokens.get(currentToken).getWord().equals("|") ) 
		{
			node = new DefaultMutableTreeNode("|");
			parent.add(node);
			if(tokens.get(currentToken).getLine()<tokens.get(currentToken+1).getLine())
				return true;
			currentToken++;
			operator = "|";
			node = new DefaultMutableTreeNode("X");
			parent.add(node);
			rule_X(node);

			String type2 = SemanticAnalyzer.popStack(); 
			String type1 = SemanticAnalyzer.popStack();
			String result = SemanticAnalyzer.calculateCube(type1, type2, operator ); 
			SemanticAnalyzer.pushStack(result); 
			//codegenerator
			CodeGenerator.addInstruction("OPR", "8", "0");
			//codegenerator
		}
		return true;
	}


	private static boolean rule_X(DefaultMutableTreeNode parent) 
	{
		boolean error;
		DefaultMutableTreeNode node;
		node = new DefaultMutableTreeNode("Y");
		parent.add(node);
		String operator = null;
		rule_Y(node);
		while (currentToken < tokens.size() && tokens.get(currentToken).getWord().equals("&") ) 
		{
			node = new DefaultMutableTreeNode("&");
			parent.add(node);
			if(tokens.get(currentToken).getLine()<tokens.get(currentToken+1).getLine())
				return true;
			currentToken++;
			operator = "&";
			node = new DefaultMutableTreeNode("Y");
			parent.add(node);
			rule_Y(node);

			String type2 = SemanticAnalyzer.popStack(); 
			String type1 = SemanticAnalyzer.popStack();
			String result = SemanticAnalyzer.calculateCube(type1, type2, operator );
			SemanticAnalyzer.pushStack(result); 
			//codegenerator
			CodeGenerator.addInstruction("OPR", "9", "0");
			//codegenerator
		}
		return true;
	}

	private static boolean rule_Y(DefaultMutableTreeNode parent) 
	{
		boolean error;
		DefaultMutableTreeNode node = null;
		boolean isOperatorUsed = false;


		if (currentToken < tokens.size() && tokens.get(currentToken).getWord().equals("!"))
		{
			node = new DefaultMutableTreeNode("!");
			parent.add(node);
			if(tokens.get(currentToken).getLine()<tokens.get(currentToken+1).getLine())
				return true;
			currentToken++;
			isOperatorUsed = true;
		}
		node = new DefaultMutableTreeNode("R");
		parent.add(node);
		rule_R(node);

		if(isOperatorUsed)
		{
			String x = SemanticAnalyzer.popStack(); 
			String result = SemanticAnalyzer.calculateCube(x, "!" ); 
			SemanticAnalyzer.pushStack(result); 
			//codegenerator
			CodeGenerator.addInstruction("OPR", "10", "0");
			//codegenerator
		}
		return true;
	}

	private static boolean rule_R(DefaultMutableTreeNode parent) 
	{
		boolean error;
		String operator = null; 
		DefaultMutableTreeNode node = new DefaultMutableTreeNode("E");
		parent.add(node);
		rule_E(node);
		while (currentToken < tokens.size() && tokens.get(currentToken).getWord().equals("!=") || currentToken < tokens.size() && tokens.get(currentToken).getWord().equals("==") || currentToken < tokens.size() && tokens.get(currentToken).getWord().equals("<") ||currentToken < tokens.size() && tokens.get(currentToken).getWord().equals(">") ) {
			if (currentToken < tokens.size() && tokens.get(currentToken).getWord().equals("!=")) {
				node = new DefaultMutableTreeNode("!=");
				parent.add(node);
				if(tokens.get(currentToken).getLine()<tokens.get(currentToken+1).getLine())
					return true;
				currentToken++;
				operator = "!=";
				node = new DefaultMutableTreeNode("E");
				parent.add(node);

				rule_E(node);
				//codegenerator
				CodeGenerator.addInstruction("OPR", "16", "0");
				//codegenerator

			} else if (currentToken < tokens.size() && tokens.get(currentToken).getWord().equals("==")) {
				node = new DefaultMutableTreeNode("==");
				parent.add(node);
				if(tokens.get(currentToken).getLine()<tokens.get(currentToken+1).getLine())
					return true;
				currentToken++;
				operator = "==";
				node = new DefaultMutableTreeNode("E");
				parent.add(node);
				rule_E(node);
				//codegenerator
				CodeGenerator.addInstruction("OPR", "15", "0");
				//codegenerator
			}
			else if (currentToken < tokens.size() && tokens.get(currentToken).getWord().equals("<")) {
				node = new DefaultMutableTreeNode("<");
				parent.add(node);
				if(tokens.get(currentToken).getLine()<tokens.get(currentToken+1).getLine())
					return true;
				currentToken++;
				operator = "<";
				node = new DefaultMutableTreeNode("E");
				parent.add(node);
				rule_E(node);
				//codegenerator
				CodeGenerator.addInstruction("OPR", "12", "0");
				//codegenerator
			}
			else if (currentToken < tokens.size() && tokens.get(currentToken).getWord().equals(">")) {
				node = new DefaultMutableTreeNode(">");
				parent.add(node);
				if(tokens.get(currentToken).getLine()<tokens.get(currentToken+1).getLine())
					return true;
				currentToken++;
				operator = ">";
				node = new DefaultMutableTreeNode("E");
				parent.add(node);
				rule_E(node);
				//codegenerator
				CodeGenerator.addInstruction("OPR", "11", "0");
				//codegenerator
			}

			String type2 = SemanticAnalyzer.popStack(); 
			String type1 = SemanticAnalyzer.popStack();
			String result = SemanticAnalyzer.calculateCube(type1, type2, operator );
			SemanticAnalyzer.pushStack(result); 
		}
		return true;
	}

	private static boolean rule_E(DefaultMutableTreeNode parent) 
	{
		boolean error;
		DefaultMutableTreeNode node;
		node = new DefaultMutableTreeNode("A");
		parent.add(node);
		String operator = null; 
		rule_A(node);
		while (currentToken < tokens.size() && tokens.get(currentToken).getWord().equals("+") || currentToken < tokens.size() && tokens.get(currentToken).getWord().equals("-")) {
			if (currentToken < tokens.size() && tokens.get(currentToken).getWord().equals("+")) {
				node = new DefaultMutableTreeNode("+");
				parent.add(node);
				if(tokens.get(currentToken).getLine()<tokens.get(currentToken+1).getLine())
					return true;
				currentToken++;
				operator = "+";
				node = new DefaultMutableTreeNode("A");
				parent.add(node);
				rule_A(node);
				//codegenerator
				CodeGenerator.addInstruction("OPR", "2", "0");
				//codegenerator
			} else if (currentToken < tokens.size() && tokens.get(currentToken).getWord().equals("-")) {
				node = new DefaultMutableTreeNode("-");
				parent.add(node);
				if(tokens.get(currentToken).getLine()<tokens.get(currentToken+1).getLine())
					return true;
				currentToken++;
				operator = "-";
				node = new DefaultMutableTreeNode("A");
				parent.add(node);
				rule_A(node);
				//codegenerator
				CodeGenerator.addInstruction("OPR", "3", "0");
				//codegenerator
			}
			String type2 = SemanticAnalyzer.popStack(); 
			String type1 = SemanticAnalyzer.popStack();
			String result = SemanticAnalyzer.calculateCube(type1, type2, operator ); 
			SemanticAnalyzer.pushStack(result); 
		}
		return true;
	}

	private static boolean rule_A(DefaultMutableTreeNode parent) 
	{
		boolean error;
		DefaultMutableTreeNode node = new DefaultMutableTreeNode("B");
		parent.add(node);
		String operator = null;
		rule_B(node);
		while (currentToken < tokens.size() && tokens.get(currentToken).getWord().equals("*") || currentToken < tokens.size() && tokens.get(currentToken).getWord().equals("/")) 
		{
			if (currentToken < tokens.size() && tokens.get(currentToken).getWord().equals("*")) {
				node = new DefaultMutableTreeNode("*");
				parent.add(node);
				if(tokens.get(currentToken).getLine()<tokens.get(currentToken+1).getLine())
					return true;
				currentToken++;
				operator = "*";
				node = new DefaultMutableTreeNode("B");
				parent.add(node);

				rule_B(node);
				//codegenerator
				CodeGenerator.addInstruction("OPR", "4", "0");
				//codegenerator

			} else if (currentToken < tokens.size() && tokens.get(currentToken).getWord().equals("/")) {
				node = new DefaultMutableTreeNode("/");
				parent.add(node);
				if(tokens.get(currentToken).getLine()<tokens.get(currentToken+1).getLine())
					return true;
				currentToken++;
				operator = "/";
				node = new DefaultMutableTreeNode("B");
				parent.add(node);
				rule_B(node);
				//codegenerator
				CodeGenerator.addInstruction("OPR", "5", "0");
				//codegenerator
			}

			String type2 = SemanticAnalyzer.popStack(); 
			String type1 = SemanticAnalyzer.popStack();
			String result = SemanticAnalyzer.calculateCube(type1, type2, operator ); 
			SemanticAnalyzer.pushStack(result); 
		}
		return true;
	}

	private static boolean rule_B(DefaultMutableTreeNode parent) 
	{
		boolean error;
		DefaultMutableTreeNode node;

		boolean isOperatorUsed = false;

		if (currentToken < tokens.size() && tokens.get(currentToken).getWord().equals("-")) {
			node = new DefaultMutableTreeNode("-");
			parent.add(node);
			if(tokens.get(currentToken).getLine()<tokens.get(currentToken+1).getLine())
				return true;
			isOperatorUsed  = true;
			currentToken++;
			//codegenerator
			CodeGenerator.addInstruction("LIT", "0", "0");
			//codegenerator
		}
		node = new DefaultMutableTreeNode("C");
		parent.add(node);
		rule_C(node);
		if(isOperatorUsed)
		{
			String x = SemanticAnalyzer.popStack(); 
			String result = SemanticAnalyzer.calculateCube(x, "-" ); 
			SemanticAnalyzer.pushStack(result); 
			//codegenerator
			CodeGenerator.addInstruction("OPR", "3", "0");
			//codegenerator

		}
		return true;
	}

	private static boolean rule_C(DefaultMutableTreeNode parent) 
	{
		boolean error = false;
		DefaultMutableTreeNode node;
		if (currentToken < tokens.size() && tokens.get(currentToken).getToken().equals("INTEGER")) 
		{
			node = new DefaultMutableTreeNode("integer" + "(" + tokens.get(currentToken).getWord() + ")");
			parent.add(node);
			//codegenerator
			CodeGenerator.addInstruction("LIT", tokens.get(currentToken).getWord(), "0");
			//codegenerator
			if(currentToken+1< tokens.size() && tokens.get(currentToken).getLine()<tokens.get(currentToken+1).getLine())
				return true;

			SemanticAnalyzer.pushStack("int");

			currentToken++;
		}
		else if (currentToken < tokens.size() && tokens.get(currentToken).getToken().equals("HEXADECIMAL")) 
		{
			node = new DefaultMutableTreeNode("hexadecimal" + "(" + tokens.get(currentToken).getWord() + ")");
			parent.add(node);
			//codegenerator
			CodeGenerator.addInstruction("LIT", tokens.get(currentToken).getWord(), "0");
			//codegenerator
			if(currentToken+1< tokens.size() && tokens.get(currentToken).getLine()<tokens.get(currentToken+1).getLine())
				return true;
			SemanticAnalyzer.pushStack("int");
			currentToken++;
		}
		else if (currentToken < tokens.size() && tokens.get(currentToken).getToken().equals("OCTAL")) 
		{
			node = new DefaultMutableTreeNode("octal" + "(" + tokens.get(currentToken).getWord() + ")");
			parent.add(node);
			//codegenerator
			CodeGenerator.addInstruction("LIT", tokens.get(currentToken).getWord(), "0");
			//codegenerator
			if(currentToken+1< tokens.size() && tokens.get(currentToken).getLine()<tokens.get(currentToken+1).getLine())
				return true;
			SemanticAnalyzer.pushStack("int");
			currentToken++;
		}
		else if (currentToken < tokens.size() && tokens.get(currentToken).getToken().equals("BINARY")) 
		{
			node = new DefaultMutableTreeNode("binary" + "(" + tokens.get(currentToken).getWord() + ")");
			parent.add(node);
			//codegenerator
			CodeGenerator.addInstruction("LIT", tokens.get(currentToken).getWord(), "0");
			//codegenerator
			if(currentToken+1< tokens.size() && tokens.get(currentToken).getLine()<tokens.get(currentToken+1).getLine())
				return true;
			SemanticAnalyzer.pushStack("int");
			currentToken++;
		}
		else if (currentToken < tokens.size() && tokens.get(currentToken).getWord().equals("true")) 
		{
			node = new DefaultMutableTreeNode("true");
			parent.add(node);
			//codegenerator
			CodeGenerator.addInstruction("LIT", tokens.get(currentToken).getWord(), "0");
			//codegenerator
			if(currentToken+1< tokens.size() && tokens.get(currentToken).getLine()<tokens.get(currentToken+1).getLine())
				return true;
			SemanticAnalyzer.pushStack("boolean");
			currentToken++;
		}
		else if (currentToken < tokens.size() && tokens.get(currentToken).getWord().equals("false")) 
		{
			node = new DefaultMutableTreeNode("false");
			parent.add(node);
			//codegenerator
			CodeGenerator.addInstruction("LIT", tokens.get(currentToken).getWord(), "0");
			//codegenerator
			if(currentToken+1< tokens.size() && tokens.get(currentToken).getLine()<tokens.get(currentToken+1).getLine())
				return true;
			SemanticAnalyzer.pushStack( "boolean");
			currentToken++;
		}
		else if (currentToken < tokens.size() && tokens.get(currentToken).getToken().equals("CHARACTER")) 
		{
			node = new DefaultMutableTreeNode("character" + "(" + tokens.get(currentToken).getWord() + ")");
			parent.add(node);
			//codegenerator
			CodeGenerator.addInstruction("LIT", tokens.get(currentToken).getWord(), "0");
			//codegenerator
			if(currentToken+1< tokens.size() && tokens.get(currentToken).getLine()<tokens.get(currentToken+1).getLine())
				return true;
			SemanticAnalyzer.pushStack("char");
			currentToken++;
		}
		else if (currentToken < tokens.size() && tokens.get(currentToken).getToken().equals("STRING")) 
		{
			node = new DefaultMutableTreeNode("string" + "(" + tokens.get(currentToken).getWord() + ")");
			parent.add(node);
			//codegenerator
			CodeGenerator.addInstruction("LIT", tokens.get(currentToken).getWord(), "0");
			//codegenerator
			if(currentToken+1< tokens.size() && tokens.get(currentToken).getLine()<tokens.get(currentToken+1).getLine())
				return true;
			SemanticAnalyzer.pushStack("string");
			currentToken++;
		}else if (currentToken < tokens.size() && tokens.get(currentToken).getToken().equals("FLOAT")) 
		{
			node = new DefaultMutableTreeNode("float" + "(" + tokens.get(currentToken).getWord() + ")");
			parent.add(node);
			//codegenerator
			CodeGenerator.addInstruction("LIT", tokens.get(currentToken).getWord(), "0");
			//codegenerator
			if(currentToken+1< tokens.size() && tokens.get(currentToken).getLine()<tokens.get(currentToken+1).getLine())
				return true;
			SemanticAnalyzer.pushStack("float");
			currentToken++;
		}
		else if (currentToken < tokens.size() && tokens.get(currentToken).getToken().equals("IDENTIFIER")) 
		{
			node = new DefaultMutableTreeNode("identifier" + "(" + tokens.get(currentToken).getWord() + ")");
			parent.add(node);
			//codegenerator
			CodeGenerator.addInstruction("LOD", tokens.get(currentToken).getWord(), "0");
			//codegenerator
			if(currentToken+1< tokens.size() && tokens.get(currentToken).getLine()<tokens.get(currentToken+1).getLine())
				return true;
			if(SemanticAnalyzer.getSymbolTable().containsKey(tokens.get(currentToken).getWord()))
				SemanticAnalyzer.pushStack( SemanticAnalyzer.getSymbolTable().get(tokens.get(currentToken).getWord()).get(0).getType());
			else
				SemanticAnalyzer.error(gui, 4, tokens.get(currentToken).getLine(), tokens.get(currentToken).getWord());

			currentToken++;
		} else if (currentToken < tokens.size() && tokens.get(currentToken).getWord().equals("(")) 
		{
			node = new DefaultMutableTreeNode("(");
			parent.add(node);
			if(currentToken+1< tokens.size() && tokens.get(currentToken).getLine()<tokens.get(currentToken+1).getLine())
			{
				error(9);
				return true;
			}
			currentToken++;
			//
			node = new DefaultMutableTreeNode("expression");
			parent.add(node);
			rule_expression(node);
			//
			if(currentToken < tokens.size() && (tokens.get(currentToken).getWord().equals(")")))//I have added the condition
			{
				node = new DefaultMutableTreeNode(")");
				parent.add(node);
				if(currentToken+1< tokens.size() && tokens.get(currentToken).getLine()<tokens.get(currentToken+1).getLine())
					return true;
				currentToken++;      
			}
			else
			{
				error(7);// : expecting )
			}
		}
		else 
		{
			if(currentToken < tokens.size() && tokens.get(currentToken).getToken().equals("ERROR"))
			{
				node = new DefaultMutableTreeNode("error ("+tokens.get(currentToken).getWord()+")");
				parent.add(node);
			}
			error(9);
			if(currentToken+1< tokens.size() && tokens.get(currentToken).getLine()<tokens.get(currentToken+1).getLine())
				return true;
			//currentToken++;
		}
		return true;
	}

	public static void error(int err) 
	{
		int n=0;
		if(currentToken< tokens.size())
			n = tokens.get(currentToken).getLine();
		else
			if(currentToken-1<tokens.size())
				n = tokens.get(currentToken-1).getLine();

		switch (err) 
		{
		case 1: gui.writeConsole("Line" + n + ": expected {");
		break;
		case 2: gui.writeConsole("Line" + n + ": expected }");
		break;
		case 3: gui.writeConsole("Line" + n + ": expected ;");
		break;
		case 4:
			gui.writeConsole("Line" +n+": expected identifier or keyword");
			break;
		case 5:
			gui.writeConsole("Line" +n+": expected =");
			break;
		case 6:
			gui.writeConsole("Line" +n+": expected identifier");
			break;
		case 7:
			gui.writeConsole("Line" +n+": expected )");
			break;
		case 8:
			gui.writeConsole("Line" +n+": expected (");
			break;
		case 9:
			gui.writeConsole("Line" +n+": expected value, identifier, (");
			break;
		case 10:
			gui.writeConsole("Line" +n+": expected Integer/Binary/Hexadecimal/Octal value");
			break;
		case 11:
			gui.writeConsole("Line" +n+": expected :");
			break;
		case 12:
			gui.writeConsole("Line" +n+": expected case");
			break;
		}

	}

}
