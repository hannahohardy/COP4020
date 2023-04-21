package edu.ufl.cise.plcsp23;


//<string_lit> ::= “ <string_character>* “
//<string_character> ::= <input_character>, but not “ or \ | <escape_sequence>

// Want to know position of the token and know that its on a certain line
// Require implicit end of lines
// Cant have newlines within the string
// getValue will return the literal value of the string. This does not include opening-closing quotes
// must process the escape sequences into ASCII values

public class StringLitToken extends Token implements IStringLitToken {

    //constructor
    public StringLitToken(Kind kind, int pos, int length, int line, int col, char[] source) {
        super(kind, pos, length, line, col, source);
    }

    //returns value of string with escape sequences converted to ascii chars
    @Override
    public String getValue() {
        String myString = new String(source, pos + 1, length - 2);
        String value = "";
        for (int i = 0; i < myString.length(); i++) {
            if (myString.charAt(i) == '\\') {
                i++;
                if (myString.charAt(i) == 'b') {
                    value += '\b';
                } else if (myString.charAt(i) == 't') {
                    value += '\t';
                } else if (myString.charAt(i) == 'n') {
                    value += '\n';
                } else if (myString.charAt(i) == 'r') {
                    value += '\r';
                } else if (myString.charAt(i) == '"') {
                    value += '\"';
                } else if (myString.charAt(i) == '\\') {
                    value += '\\';
                }
            } else {
                value += myString.charAt(i);
            }
        }
        return value;
    }

    // EOF StringLitToken.Java
}