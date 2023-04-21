package edu.ufl.cise.plcsp23;

public class NumLitToken extends Token implements  INumLitToken{


    public NumLitToken(IToken.Kind kind, int pos, int length, int line, int column, char[] input) {
        super(kind, pos, length, line, column, input);
    }

    @Override
        public int getValue() {
            String number = new String(source, pos, length);
            return Integer.parseInt(number);
        }
    }
