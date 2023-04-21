package edu.ufl.cise.plcsp23;

//  EXTEND ITOKEN FOR THINGS THAT HAVE VALUES IN TOKEN: NUM_LIT method, STRING_LIT method
public class Token implements IToken {

    final IToken.Kind kind; //type
    final int pos;
    final int length;
    final int line;
    final int col;
    final char[] source; //text of the token

    public record SourceLocation(int line, int column){}

    public Token(IToken.Kind kind, int pos, int length, int line, int column, char[] source){
        super();
        this.kind = kind;
        this.pos = pos;
        this.length = length;
        this.source = source;
        this.line = line;
        this.col = column - length;
    }

    @Override
    public IToken.SourceLocation getSourceLocation() {
        return new IToken.SourceLocation(line, col);
    }

    @Override
    public IToken.Kind getKind() {
        return kind;
    }
    public int getLength(){
        return this.length;
    }

    @Override
    public String getTokenString() {
        return new String(source, pos, length);
    }
    @Override
    public String toString(){
        return kind + "=" + getTokenString() + " located at" + pos + "," + length;
    }

    // EOF Token.Java
}
