package edu.ufl.cise.plcsp23;
public interface IScanner {
    /*
     * Return an IToken and advance the internal position so that
     * subsequent calls will return subsequent ITokens.
     *
     * @return
     * @throws LexicalException
    */
    public static IScanner makeScanner (String input) {
        return new Scanner(input);
    }

    IToken next() throws LexicalException;
}
