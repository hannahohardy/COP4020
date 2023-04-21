package edu.ufl.cise.plcsp23;

import java.util.Arrays;
import java.util.HashMap;

// Scanner Class!
public class Scanner implements IScanner {
    final String input;
    final char[] inputChars; // char arrary of the input, temrin w extra char 0 (EOF?)
    int pos; // position of the character in the input
    char ch; // next char
    int line = 1;
    int column = 1;
    int length;

    // Enumerations of the internal states
    private enum State { // BASICALLY THE CIRCLE
        START,
        IN_IDENT,
        IN_STRING_LIT,
        IN_NUM_LIT,
        HAVE_EQUAL,
        HAVE_LT,
        HAVE_AND,
        HAVE_OR,
        HAVE_EXP,
        HAVE_GT,
        IN_COMMENT
    }


    //Scanner constructor to initialize variables for scanner class
    public Scanner(String input) {
        this.input = input;
        inputChars = Arrays.copyOf(input.toCharArray(), input.length() + 1);
        pos = 0;
        ch = inputChars[pos]; //char at that pos

        System.out.println(inputChars);
        System.out.println(ch);
    }

    // Store reserved words in a hash map for easy lookup, Maps the string to the token kind
    public static HashMap<String, IToken.Kind> reservedWords;
    static {
        reservedWords = new HashMap<>();
        reservedWords.put("image", IToken.Kind.RES_image);
        reservedWords.put("pixel", IToken.Kind.RES_pixel);
        reservedWords.put("int", IToken.Kind.RES_int);
        reservedWords.put("string", IToken.Kind.RES_string);
        reservedWords.put("void", IToken.Kind.RES_void);
        reservedWords.put("nil", IToken.Kind.RES_nil);
        reservedWords.put("load", IToken.Kind.RES_load);
        reservedWords.put("display", IToken.Kind.RES_display);
        reservedWords.put("write", IToken.Kind.RES_write);
        reservedWords.put("x", IToken.Kind.RES_x);
        reservedWords.put("y", IToken.Kind.RES_y);
        reservedWords.put("a", IToken.Kind.RES_a);
        reservedWords.put("X", IToken.Kind.RES_X);
        reservedWords.put("Y", IToken.Kind.RES_Y);
        reservedWords.put("Z", IToken.Kind.RES_Z);
        reservedWords.put("x_cart", IToken.Kind.RES_x_cart);
        reservedWords.put("y_cart", IToken.Kind.RES_y_cart);
        reservedWords.put("a_polar", IToken.Kind.RES_a_polar);
        reservedWords.put("r_polar", IToken.Kind.RES_r_polar);
        reservedWords.put("rand", IToken.Kind.RES_rand);
        reservedWords.put("sin", IToken.Kind.RES_sin);
        reservedWords.put("cos", IToken.Kind.RES_cos);
        reservedWords.put("atan", IToken.Kind.RES_atan);
        reservedWords.put("if", IToken.Kind.RES_if);
        reservedWords.put("while", IToken.Kind.RES_while);
        reservedWords.put("red", IToken.Kind.RES_red);
        reservedWords.put("blu", IToken.Kind.RES_blu);
        reservedWords.put("grn", IToken.Kind.RES_grn);

    }

    // calls scanToken, return the next token
    @Override
    public Token next() throws LexicalException {
        return scanToken();
    }

    // Helper function to throw Lexical Exceptions
    private void error(String message) throws LexicalException {
        throw new LexicalException("Error at pos " + pos + ": " + message);
    }

    // Helper function to iterate to next character and increases position variables by 1
    protected void nextChar() {
        pos++;
        column++;
        ch = inputChars[pos];
    }

    // Helper functions to check char type
    private boolean isDigit(int ch) {
        return '0' <= ch && ch <= '9';
    }
    private boolean isLetter(int ch) {
        return ('A' <= ch && ch <= 'Z') || ('a' <= ch && ch <= 'z');
    }
    private boolean isIdentStart(int ch) {
        return isLetter(ch) || (ch == '_');
    }

    //DFA
    // ScanToken function returns the type of token from the string
    private Token scanToken() throws LexicalException {
        State state = State.START;
        int tokenStart = -1;        // position of first char in token set to -1 for error detection

        // reading if char is valid, terminates when token eof is returned
        while (true) {
            switch (state) {
                case START -> {
                    tokenStart = pos;
                    switch (ch) {
                        //EOF
                        case 0 -> {
                            return new Token(IToken.Kind.EOF, tokenStart, 0, line, column, inputChars);
                        }
                        //whitespace -- changed to account for newlines and escape sequences below
                        case ' ', '\r', '\t', '\f' -> nextChar();
                        //newline
                        case '\n' -> {
                            nextChar();
                            line++;
                            column = 1;
                        }
                        //<OP> OR <SEPERATOR> (ALL THE SINGLE)
                        case '.' -> {
                            nextChar();
                            return new Token(IToken.Kind.DOT, tokenStart, 1, line, column, inputChars);
                        }
                        case ',' -> {
                            nextChar();
                            return new Token(IToken.Kind.COMMA, tokenStart, 1, line, column, inputChars);
                        }
                        case '?' -> {
                            nextChar();
                            return new Token(IToken.Kind.QUESTION, tokenStart, 1, line, column, inputChars);
                        }
                        case ':' -> {
                            nextChar();
                            return new Token(IToken.Kind.COLON, tokenStart, 1, line, column, inputChars);
                        }
                        case '(' -> {
                            nextChar();
                            return new Token(IToken.Kind.LPAREN, tokenStart, 1, line, column, inputChars);
                        }
                        case ')' -> {
                            nextChar();
                            return new Token(IToken.Kind.RPAREN, tokenStart, 1, line, column, inputChars);
                        }
                        case '<' -> {
                            state = State.HAVE_LT;
                            nextChar();
                        }
                        case '>' -> {
                            state = State.HAVE_GT;
                            nextChar();
                        }
                        case '[' -> {
                            nextChar();
                            return new Token(IToken.Kind.LSQUARE, tokenStart, 1, line, column, inputChars);
                        }
                        case ']' -> {
                            nextChar();
                            return new Token(IToken.Kind.RSQUARE, tokenStart, 1, line, column, inputChars);
                        }
                        case '{' -> {
                            nextChar();
                            return new Token(IToken.Kind.LCURLY, tokenStart, 1, line, column, inputChars);
                        }
                        case '}' -> {
                            nextChar();
                            return new Token(IToken.Kind.RCURLY, tokenStart, 1, line, column, inputChars);
                        }
                        case '=' -> {
                            state = State.HAVE_EQUAL;
                            nextChar();
                        }
                        case '!' -> {
                            nextChar();
                            return new Token(IToken.Kind.BANG, tokenStart, 1, line, column, inputChars);
                        }
                        case '&' -> {
                            state = edu.ufl.cise.plcsp23.Scanner.State.HAVE_AND;
                            nextChar();
                        }
                        case '|' -> {
                            state = State.HAVE_OR;
                            nextChar();
                        }
                        case '+' -> {
                            nextChar();
                            return new Token(IToken.Kind.PLUS, tokenStart, 1, line, column, inputChars);
                        }
                        case '-' -> {
                            nextChar();
                            return new Token(IToken.Kind.MINUS, tokenStart, 1, line, column, inputChars);
                        }
                        case '*' -> {
                            state = State.HAVE_EXP;
                            nextChar();
                        }
                        case '/' -> {
                            nextChar();
                            return new Token(IToken.Kind.DIV, tokenStart, 1, line, column, inputChars);
                        }
                        case '%' -> {
                            nextChar();
                            return new Token(IToken.Kind.MOD, tokenStart, 1, line, column, inputChars);
                        }
                        //comments -- necessary for passing test cases for literals
                        case '~' -> {
                            state = State.IN_COMMENT;
                            nextChar();
                        }
                        //string literals
                        case '\"' -> {
                            nextChar();
                            state = State.IN_STRING_LIT;
                        }
                        //numlit 0
                        case '0' -> {
                            nextChar();
                            return new NumLitToken(IToken.Kind.NUM_LIT, tokenStart, 1, line, column, inputChars);
                        }
                        //digits 1-9
                        case '1', '2', '3', '4', '5', '6', '7', '8', '9' -> {
                            state = State.IN_NUM_LIT;
                        }
                        // Handles idents and reserved words
                        default -> {
                            if (isLetter(ch)) {
                                state = State.IN_IDENT;
                                nextChar();
                            } else if (isIdentStart(ch)) {
                                state = State.IN_IDENT;
                                nextChar();
                            } else error("illegal char with ascii value: " + (int) ch);
                        }
                    }
                }
                //==
                case HAVE_EQUAL -> {
                    if (ch == '=') { // "==" checking the next input
                        state = state.START;
                        nextChar();
                        return new Token(IToken.Kind.EQ, tokenStart, 2, line, column, inputChars);
                    } else {
                        state = state.START;
                        nextChar();
                        return new Token(IToken.Kind.ASSIGN, tokenStart, 2, line, column, inputChars);

                    }

                }
                //inside comment and escapes when it is a \n -- Needed to pass string literals test
                case IN_COMMENT -> {
                    if (ch != '\n') {
                        nextChar();
                    } else {
                        line++;
                        nextChar();
                        column = 1;
                        state = state.START;
                    }
                }
                // <= , <->,
                case HAVE_LT -> {
                    if (ch == '=') {
                        state = state.START;
                        nextChar();
                        return new Token(IToken.Kind.LE, tokenStart, 2, line, column, inputChars);
                    }
                    if (ch == '-') {
                        nextChar();
                        if (ch == '>') {
                            state = state.START;
                            nextChar();
                            return new Token(IToken.Kind.EXCHANGE, tokenStart, 3, line, column, inputChars);
                        } else {
                            throw new LexicalException("illegal exchange"); //DETECTS ILLEGAL EXCHANGE (test case)
                        }
                    } else {
                        state = state.START;
                        return new Token(IToken.Kind.LT, tokenStart, 1, line, column, inputChars);
                    }
                }
                //>=
                case HAVE_GT -> {
                    if (ch == '=') {
                        nextChar();
                        state = state.START;
                        return new Token(IToken.Kind.GE, tokenStart, 2, line, column, inputChars);
                    } else {

                        state = state.START;

                        return new Token(IToken.Kind.GT, tokenStart, 1, line, column, inputChars);

                    }
                }

                //&&
                case HAVE_AND -> {
                    if (ch == '&') {
                        state = state.START;
                        nextChar();
                        return new Token(IToken.Kind.AND, tokenStart, 2, line, column, inputChars);

                    } else {
                        state = state.START;
                        nextChar();
                        return new Token(IToken.Kind.BITAND, tokenStart, 1, line, column, inputChars);
                    }
                }
                //||
                case HAVE_OR -> {
                    if (ch == '|') {
                        state = state.START;
                        nextChar();
                        return new Token(IToken.Kind.OR, tokenStart, 2, line, column, inputChars);

                    } else {
                        return new Token(IToken.Kind.BITOR, tokenStart, 2, line, column, inputChars);
                    }
                }
                //**
                case HAVE_EXP -> {
                    if (ch == '*') {
                        state = state.START;
                        nextChar();
                        return new Token(IToken.Kind.EXP, tokenStart, 2, line, column, inputChars);

                    } else {
                        state = state.START;
                        nextChar();
                        return new Token(IToken.Kind.TIMES, tokenStart, 1, line, column, inputChars);
                    }
                }
                case IN_IDENT -> {
                    if (isIdentStart(ch)) {
                        nextChar();
                    } else if (isDigit(ch)) {
                        nextChar();
                    } else {
                        length = pos-tokenStart;
                        String text = input.substring(tokenStart, tokenStart+length);
                        IToken.Kind kind = reservedWords.get(text);
                        if (kind == null) {
                            kind = IToken.Kind.IDENT;
                        }

                        return new Token(kind, tokenStart, length, line, column, inputChars);
                    }
                }
                case IN_STRING_LIT -> {
                    if (ch == '\\') {
                        nextChar();
                        if (ch != 'r' && ch != 't' && ch != 'f' && ch != '\\' && ch != '"' && ch != 'n') {
                            throw new LexicalException("illegal escape sequence");
                        } else {
                            nextChar();
                        }
                    } else if (ch == '\n') {    //Throws lexical exception for illegal escape sequence
                        throw new LexicalException("illegal escape sequence");
                    } else if (ch != '"') {
                        nextChar();
                    } else {
                        length = pos-tokenStart+1;
                        nextChar();
                        return new StringLitToken(IToken.Kind.STRING_LIT, tokenStart, length, line, column, inputChars);
                    }
                }
                case IN_NUM_LIT -> {
                    if (isDigit(ch)) {
                        nextChar();
                    } else {
                        length = pos-tokenStart;
                        String number = new String(inputChars, tokenStart, length);
                        try {
                            Integer.parseInt(number);
                            return new NumLitToken(IToken.Kind.NUM_LIT, tokenStart, length, line, column, inputChars);
                        } catch (Exception e) {
                            throw new LexicalException("num lit not in range");
                        }
                    }
                }
                //check commit changes
                default -> {
                    throw new UnsupportedOperationException("Bug in the Scanner");
                }
            }
        }

    }
}
