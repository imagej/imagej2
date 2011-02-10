package ijx.macro;
import ijx.IJ;

import java.util.Hashtable;

/** An object of this type is a tokenized macro file and the associated symbol table. */
public class Program implements MacroConstants {

	private int maxSymbols = 800; // will be increased as needed
	private int maxProgramSize = 2000;  // well be increased as needed
	private int pc = -1;
	
	int stLoc = -1;
	int symTabLoc;
	Symbol[] table = new Symbol[maxSymbols];
    static Symbol[] systemTable;
	int[] code = new int[maxProgramSize];
	int[] lineNumbers = new int[maxProgramSize];
	Variable[] globals;
	boolean hasVars, hasFunctions;
	int macroCount;
    Hashtable menus;
    // run keyboard shortcut macros on event dispatch thread?
	boolean queueCommands; 
	Hashtable extensionRegistry;
	
	
	public Program() {
		if (systemTable!=null) {
			stLoc = systemTable.length - 1;
			for (int i=0; i<=stLoc; i++)
			table[i] = systemTable[i];
		} else {
			//IJ.log("make table");
			addKeywords();
			addFunctions();
			addNumericFunctions();
			addStringFunctions();
			addArrayFunctions();
			systemTable = new Symbol[stLoc+1];
			for (int i=0; i<=stLoc; i++)
				systemTable[i] = table[i];
			IJ.register(Program.class);
		}
	}
	
	public int[] getCode() {
		return code;
	}
	
	public Symbol[] getSymbolTable() {
		return table;
	}
	
	void addKeywords() {
		for (int i=0; i<keywords.length; i++)
			addSymbol(new Symbol(keywordIDs[i], keywords[i]));
	}

	void addFunctions() {
		for (int i=0; i<functions.length; i++)
			addSymbol(new Symbol(functionIDs[i], functions[i]));
	}

	void addNumericFunctions() {
		for (int i=0; i<numericFunctions.length; i++)
			addSymbol(new Symbol(numericFunctionIDs[i], numericFunctions[i]));
	}
	
	void addStringFunctions() {
		for (int i=0; i<stringFunctions.length; i++)
			addSymbol(new Symbol(stringFunctionIDs[i], stringFunctions[i]));
	}

	void addArrayFunctions() {
		for (int i=0; i<arrayFunctions.length; i++)
			addSymbol(new Symbol(arrayFunctionIDs[i], arrayFunctions[i]));
	}

	void addSymbol(Symbol sym) {
		stLoc++;
		if (stLoc==table.length) {
			Symbol[] tmp = new Symbol[maxSymbols*2];
			System.arraycopy(table, 0, tmp, 0, maxSymbols);
			table = tmp;
			maxSymbols *= 2;
		}
		table[stLoc] = sym;
	}
	
	void addToken(int tok, int lineNumber) {//n__
		pc++;
		if (pc==code.length) {
			int[] tmp = new int[maxProgramSize*2];
			System.arraycopy(code, 0, tmp, 0, maxProgramSize);
			code = tmp;

            tmp = new int[maxProgramSize*2];  //n__
			System.arraycopy(lineNumbers, 0, tmp, 0, maxProgramSize);
			lineNumbers = tmp;

			maxProgramSize *= 2;
        }
		code[pc] = tok;
        lineNumbers[pc] = lineNumber; //n__
	}

	/** Looks up a word in the symbol table. Returns null if the word is not found. */
	Symbol lookupWord(String str) {
        //IJ.log("lookupWord: "+str);
		Symbol symbol;
		String symStr;
		for (int i=0; i<=stLoc; i++) {
			symbol = table[i];
			if (symbol.type!=STRING_CONSTANT && str.equals(symbol.str)) {
				symTabLoc = i;
				return symbol;
			}
		}
		return null;
	}

	void saveGlobals(Interpreter interp) {
		//IJ.log("saveGlobals: "+interp.topOfStack);
		if (interp.topOfStack==-1)
			return;
		int n = interp.topOfStack+1;
		globals = new Variable[n];
		for (int i=0; i<n; i++)
			globals[i] = interp.stack[i];
	}
	
	public void dumpSymbolTable() {
		IJ.log("");
		IJ.log("Symbol Table");
		for (int i=0; i<=maxSymbols; i++) {
			Symbol symbol = table[i];
			if (symbol==null)
				break;
			IJ.log(i+" "+symbol);
		}
	}

	public void dumpProgram() {
		IJ.log("");
		IJ.log("Tokenized Program");
		String str;
		int token, address;
		for (int i=0; i<=pc; i++) 
			IJ.log(i+"	 "+lineNumbers[i]+"   "+(code[i]&TOK_MASK)+"   "+decodeToken(code[i]));
	}
	
	public Variable[] getGlobals() {
		return globals;
	}

	public boolean hasVars() {
		return hasVars;
	}

	public int macroCount() {
		return macroCount;
	}

	public String decodeToken(int token) {
		return decodeToken(token&TOK_MASK, token>>TOK_SHIFT);
	}

	String decodeToken(int token, int address) {
		String str;
		switch (token) {
			case WORD:
			case PREDEFINED_FUNCTION:
			case NUMERIC_FUNCTION:
			case STRING_FUNCTION:
			case ARRAY_FUNCTION:
			case USER_FUNCTION:
				str = table[address].str;
				break;
			case STRING_CONSTANT:
				str = "\""+table[address].str+"\"";
				break;
			case NUMBER:
				double v = table[address].value;
				if ((int)v==v)
					str = IJ.d2s(v,0);
				else
					str = ""+v;
				break;
			case EOF:
				str = "EOF";
				break;
			default:
				if (token<32) {
					switch (token) {
					case PLUS_PLUS:
						str="++";
						break;
					case MINUS_MINUS:
						str="--";
						break;
					case PLUS_EQUAL:
						str="+=";
						break;
					case MINUS_EQUAL:
						str="-=";
						break;
					case MUL_EQUAL:
						str="*=";
						break;
					case DIV_EQUAL:
						str="/=";
						break;
					case LOGICAL_AND:
						str="&&";
						break;
					case LOGICAL_OR:
						str="||";
						break;
					case EQ:
						str="==";
						break;
					case NEQ:
						str="!=";
						break;
					case GT:
						str=">";
						break;
					case GTE:
						str=">=";
						break;
					case LT:
						str="<";
						break;
					case LTE:
						str="<=";
						break;
					default:
						str="";
						break;
					}
				} else if (token>=200) {
					str = table[address].str;
				} else {
					char s[] = new char[1];
					s[0] = (char)token;
					str = new String(s);
				}
				break;
		}
		return str;
	}
    
    public Hashtable getMenus() {
        return menus;
    }

	// Returns 'true' if this macro program contains the specified word. */
	public boolean hasWord(String word) {
		int token, tokenAddress;
		for (int i=0; i<code.length; i++) {
			token = code[i];
			if (token<=127) continue;
			if (token==EOF) return false;
			tokenAddress = token>>TOK_SHIFT;
			String str = table[tokenAddress].str;
			if (str!=null && str.equals(word)) return true;
		}
		return false;
	}
	
} // Program
