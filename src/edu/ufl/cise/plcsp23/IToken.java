package edu.ufl.cise.plcsp23;
public interface IToken {
    /* Represents the location in the source code */
    public record SourceLocation(int line, int column){}

    public static enum Kind {
        IDENT,
        NUM_LIT,
        STRING_LIT,
        RES_image,
        RES_pixel,
        RES_int,
        RES_string,
        RES_void,
        RES_nil,
        RES_load,
        RES_display,
        RES_write,
        RES_x,
        RES_y,
        RES_a,
        RES_r,
        RES_X,
        RES_Y,
        RES_Z,
        RES_x_cart,
        RES_y_cart,
        RES_a_polar,
        RES_r_polar,
        RES_rand,
        RES_sin,
        RES_cos,
        RES_atan,
        RES_if,
        RES_while,
        RES_red,
        RES_blu,
        RES_grn,
        DOT, // .
        COMMA, // ,
        QUESTION, // ?
        COLON, // :
        LPAREN, // (
        RPAREN, // )
        LT, // <
        GT, // >
        LSQUARE, // [
        RSQUARE, // ]
        LCURLY, // {
        RCURLY, // }
        ASSIGN, // =
        EQ, // ==
        EXCHANGE, // <->
        LE, // <=
        GE, // >=
        BANG, // !
        BITAND, // &
        AND, // &&
        BITOR, // |
        OR, // ||
        PLUS, // +
        MINUS, // -
        TIMES, // *
        EXP, // **
        DIV, // /
        MOD, // %
        EOF,
        ERROR   //may be useful
    }


    // Returns a location to the source record containing the line and column of this token
    // Both counts start numbering at 1
    // Return line number and column of this token
    public SourceLocation getSourceLocation();

    //Contains the kind of the token, returns kind
    public Kind getKind();

    // Contains reference to input array. Returns a char array containing characters of the token
    public String getTokenString();

    public int getLength();

    public String toString();

    // EOF IToken.Java
}
