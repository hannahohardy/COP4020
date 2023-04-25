package edu.ufl.cise.plcsp23;
import edu.ufl.cise.plcsp23.ast.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Parser extends AST  implements IParser {
    IScanner scanner;
    int currentPos = 0;
    IToken firstToken;
    boolean declarBoo = false;
    boolean invalidStmt = false;
    boolean expandpixelBoo = false;
    boolean conditionBoo = false;
    boolean parentBoo = false;

    boolean stringLitBoo = false;
    String inputScan;

    ExpandedPixelExpr pixelExpr;

    LValue lVal;
    ColorChannel chanSelect;
    PixelSelector pix;

    IToken.Kind pOp = null;

    String inputParser;
    final char[] inputParCh;
    IToken.Kind kind;
    IToken nextToken;
    Expr leftExpr;

    Expr rightExpr;

    ConditionalExpr conditionalE;

    Expr leftBinaryExp = null;
    String lexTemp;
    Expr e = null;
    RandomExpr random;
    IdentExpr idnt;
    Statement st = null;
    Ident prgIdent;
    ZExpr z;
    NumLitExpr numLit;
    StringLitExpr stringLit;
    UnaryExpr unary;
    BinaryExpr binary;
    Program prog = null;
    Block pBlock;
    List<NameDef> argList;
    NameDef nameDf;
    Expr trueCase;
    Expr falseCase;
    Expr gaurdE;
    static Declaration decl;
    Type typeVal;


    //  TypeChecker.SymbolTable symbTable = new TypeChecker.SymbolTable();


    public Parser(String inputParser) {
        super(null);
        this.inputParser = inputParser;
        inputParCh = Arrays.copyOf(inputParser.toCharArray(), 1 + inputParser.length());
    }


    public IToken consume() throws SyntaxException, LexicalException {

        while(inputParCh[currentPos] == ' ' || inputParCh[currentPos] == '\n' || inputParCh[currentPos] == '\t')
        {
            currentPos++;
        }

        if(firstToken.getSourceLocation() != null && firstToken.getKind() != IToken.Kind.EOF)
        {

            currentPos =  currentPos + ((Token) firstToken).getLength();
        }
        else
        {
            currentPos = currentPos+1;
        }
        if(nextToken!= null)
        {
            if(nextToken.getKind() == IToken.Kind.OR || nextToken.getKind() == IToken.Kind.AND || nextToken.getKind() == IToken.Kind.EXP || nextToken.getKind()== IToken.Kind.LE || nextToken.getKind()== IToken.Kind.GE || nextToken.getKind()== IToken.Kind.EXP || nextToken.getKind()== IToken.Kind.EQ)
                currentPos = currentPos+1;
        }
        lexTemp = inputParser.substring(currentPos,inputParser.length());
        scanner = CompilerComponentFactory.makeScanner(lexTemp);
        IToken token;
        token = scanner.next();
        kind = token.getKind();
        return token;

    }

    public Program program() throws PLCException {
        kind = firstToken.getKind();
        if (kind == IToken.Kind.RES_string || kind == IToken.Kind.RES_pixel || kind == IToken.Kind.RES_int || kind == IToken.Kind.RES_image || kind == IToken.Kind.RES_void) {
            typeVal = Type.getType(firstToken);
            nextToken = consume();
            if (IToken.Kind.IDENT == nextToken.getKind() )
                prgIdent = new Ident(nextToken);
            firstToken = nextToken;
            nextToken = consume();
            if (IToken.Kind.LPAREN == nextToken.getKind()) {
                argList = ParamList();
                firstToken = nextToken;
                if (IToken.Kind.RPAREN == nextToken.getKind()) {
                    firstToken = nextToken;
                    nextToken = consume();
                    if (IToken.Kind.LCURLY == nextToken.getKind()) {
                        pBlock =  block();
                        if (nextToken.getKind() == IToken.Kind.EOF || nextToken.getKind() == IToken.Kind.RCURLY) {
                            prog = new Program(firstToken,typeVal,prgIdent,argList,pBlock);
                            if(nextToken.getKind() != IToken.Kind.EOF) {
                                firstToken = nextToken;
                                nextToken = consume();
                            }
                        }
                    }
                }
            }
        }
        return prog;
    }
//public Program program() throws PLCException {
//    if (!isExpectedType(firstToken)) {
//        return null;
//    }
//
//    typeVal = Type.getType(firstToken);
//    nextToken = consume();
//
//    if (!isIdentToken(nextToken)) {
//        return null;
//    }
//
//    prgIdent = new Ident(nextToken);
//    nextToken = consume();
//
//    if (!isLParenToken(nextToken)) {
//        return null;
//    }
//
//    argList = ParamList();
//    nextToken = consume();
//
//    if (!isRParenToken(nextToken)) {
//        return null;
//    }
//
//    nextToken = consume();
//
//    if (!isLCurlyToken(nextToken)) {
//        return null;
//    }
//
//    pBlock = block();
//    nextToken = consume();
//
//    if (!isEOForRCurlyToken(nextToken)) {
//        return null;
//    }
//
//    prog = new Program(firstToken, typeVal, prgIdent, argList, pBlock);
//
//    if (nextToken.getKind() != IToken.Kind.EOF) {
//        nextToken = consume();
//    }
//
//    return prog;
//}
//
//    private boolean isExpectedType(IToken token) {
//        IToken.Kind kind = token.getKind();
//        return kind == IToken.Kind.RES_string ||
//                kind == IToken.Kind.RES_pixel ||
//                kind == IToken.Kind.RES_int ||
//                kind == IToken.Kind.RES_image ||
//                kind == IToken.Kind.RES_void;
//    }
//
//    private boolean isIdentToken(IToken token) {
//        return token.getKind() == IToken.Kind.IDENT;
//    }
//
//    private boolean isLParenToken(IToken token) {
//        return token.getKind() == IToken.Kind.LPAREN;
//    }
//
//    private boolean isRParenToken(IToken token) {
//        return token.getKind() == IToken.Kind.RPAREN;
//    }
//
//    private boolean isLCurlyToken(IToken token) {
//        return token.getKind() == IToken.Kind.LCURLY;
//    }
//
//    private boolean isEOForRCurlyToken(IToken token) {
//        IToken.Kind kind = token.getKind();
//        return kind == IToken.Kind.EOF || kind == IToken.Kind.RCURLY;
//    }
//

    public List<NameDef> ParamList() throws PLCException {
        NameDef parameter;
        List<NameDef> parameters = new ArrayList<>();
        firstToken = nextToken;
        nextToken = consume();
        while(nextToken.getKind() != IToken.Kind.RPAREN) {
            if(nextToken.getKind() != IToken.Kind.COMMA) {
                parameter = nameDef();
                parameters.add(parameter);
                firstToken = nextToken;
                nextToken = consume();
            } else{
                firstToken = nextToken;
                nextToken = consume();
            }
        }
        return parameters;
    }


    public NameDef nameDef() throws PLCException {
        Dimension dimension = null;
        Type type = null;
        IToken.Kind ntK=  nextToken.getKind();
        if(ntK != IToken.Kind.RES_void && ntK != IToken.Kind.RES_image && ntK != IToken.Kind.RES_int && ntK != IToken.Kind.RES_pixel &&ntK != IToken.Kind.RES_string && firstToken.getKind() != IToken.Kind.RES_while && firstToken.getKind() != IToken.Kind.RES_string &&ntK != IToken.Kind.COLON )
            throw new SyntaxException("Syntax Error Found");
        // t14()
        else {
            type = Type.getType(nextToken);
            firstToken = nextToken;
            nextToken = consume();
        }

        if(IToken.Kind.LSQUARE == nextToken.getKind()) {
            dimension = dimension();
            firstToken = nextToken;
            nextToken = consume();
        }

        return new NameDef(nextToken,type,dimension, new Ident(nextToken));
    }

    public Dimension dimension() throws PLCException {
        Expr expr1 = null, expr2 = null;
       // Dimension dimension = null;
        IToken curr = null;
        IToken.Kind ntK=  nextToken.getKind();

        if(IToken.Kind.LSQUARE == ntK) {
            curr = firstToken;
            firstToken = nextToken;
            nextToken = consume();
            expr1 = expr();
            while (nextToken.getKind() != IToken.Kind.RSQUARE) {
                if(nextToken.getKind() == IToken.Kind.COMMA) {
                    firstToken = nextToken;
                    nextToken = consume();
                }
                expr2 = expr();
            }
        }
        return new Dimension(curr,expr1,expr2);
    }

    public Block block() throws PLCException {
        List<Declaration> declarationList = new ArrayList<>();
        List<Statement> stmList = new ArrayList<>();
        IToken currentToken = nextToken;
        firstToken = nextToken;
        nextToken = consume();

        while(nextToken.getKind() != IToken.Kind.RCURLY) {
            if(nextToken.getKind() == IToken.Kind.EOF)
                break;
            if(nextToken.getKind() != IToken.Kind.RES_write && nextToken.getKind() != IToken.Kind.RES_while && nextToken.getKind()!= IToken.Kind.IDENT) {
                if(nextToken.getKind() == IToken.Kind.DOT) {
                    firstToken = nextToken;
                    nextToken = consume();
                    if(nextToken.getKind() == IToken.Kind.DOT)
                        throw new SyntaxException("Error Invalid Syntax");
                }
                else if(IToken.Kind.COLON == nextToken.getKind() && !invalidStmt) {
                    if(firstToken.getKind() == IToken.Kind.LCURLY) {
                        firstToken = nextToken;
                        nextToken = consume();
                        invalidStmt = nextToken.getKind() == IToken.Kind.IDENT || nextToken.getKind() == IToken.Kind.NUM_LIT;
                        stmList.add(statement());
                        firstToken = nextToken;
                        nextToken = consume();
                    }
                }
                else {
                    if(firstToken.getKind() != IToken.Kind.RES_while) {
                        if(!invalidStmt) {
                            decl = declar();
                            declarationList.add(decl);
                        }
                        else {
                            firstToken = nextToken;
                            nextToken = consume();
                            stmList.add(statement());
                            firstToken = nextToken;
                            nextToken = consume();
                        }
                    }
                }
            }
            else {
                if(IToken.Kind.IDENT != nextToken.getKind()) {
                    firstToken = nextToken;
                    nextToken = consume();
                }
                stmList.add(statement());
            }
        }
        return new Block(currentToken,declarationList,stmList);
    }

    public Statement statement() throws PLCException {
        boolean sel = false;
        boolean bool = false;
        IToken primToke = null;
        Expr expr = null;
        if(firstToken.getKind() == IToken.Kind.RES_write) {
            expr = expr();
            return new WriteStatement(firstToken, expr);
        }
        else if(firstToken.getKind() == IToken.Kind.RES_while) {
            expr = expr();
            pBlock = block();
            return new WhileStatement(firstToken,expr,pBlock);
        }
        else {
            if(invalidStmt) {
                Expr invalidExpr = new IdentExpr(nextToken);
                st = new AssignmentStatement(firstToken,lVal,invalidExpr);
                return st;

            }
            if(firstToken.getKind() == IToken.Kind.COLON) {
                expr = expr();
                return new ReturnStatement(firstToken,expr);
            }
            primToke = nextToken;
            expr = expr();
            if(nextToken.getKind() == IToken.Kind.LSQUARE) {
                sel = true;
                pix = Pix();
                firstToken = nextToken;
                nextToken = consume();
            }
            if(nextToken.getKind() == IToken.Kind.COLON) {
                bool = true;
                firstToken = nextToken;
                nextToken = consume();
                chanSelect = chanSelect();
                if(chanSelect == null)
                    bool = false;
            }

            if(sel || bool)
                expr = new UnaryExprPostfix(nextToken,expr,pix,chanSelect);

            lVal = new LValue(primToke,new Ident(primToke),pix,chanSelect);
            firstToken = nextToken;
            nextToken = consume();
            primToke = nextToken;

            if(nextToken.getKind() == IToken.Kind.LSQUARE) {
                if(firstToken.getKind() == IToken.Kind.ASSIGN) {
                    expandpixelBoo = true;
                    sel = false;
                    pixelExpr = ExtendedPix();
                }
                else {
                    sel = true;
                    expandpixelBoo = false;
                    pix = Pix();
                }
                firstToken = nextToken;
                nextToken = consume();
            }
            if(nextToken.getKind() == IToken.Kind.COLON) {
                bool = true;
                firstToken = nextToken;
                nextToken = consume();
                chanSelect = chanSelect();

            }
            if(sel || bool || expandpixelBoo) {
                if(sel)
                    expr = new UnaryExprPostfix(primToke,new IdentExpr(primToke),pix,chanSelect);
                else
                    expr = pixelExpr;
            }
            st = new AssignmentStatement(primToke,lVal,expr);
        }
        return st;
    }

    public ColorChannel chanSelect() throws TypeCheckException {
        if(nextToken.getKind() == IToken.Kind.RES_grn || nextToken.getKind() == IToken.Kind.RES_blu|| nextToken.getKind() == IToken.Kind.RES_red)
        {
            chanSelect = ColorChannel.getColor(nextToken);
        }

        return chanSelect;
    }

    public PixelSelector Pix() throws PLCException {
        IToken currentToken = null;
        Expr eTemp = null;
        Expr eTemp2 = null;
        if(nextToken.getKind() == IToken.Kind.LSQUARE) {
            currentToken = firstToken;
            firstToken = nextToken;
            nextToken = consume();
            eTemp = expr();
            while (nextToken.getKind() != IToken.Kind.RSQUARE) {
                if(nextToken.getKind() == IToken.Kind.COMMA) {
                    firstToken = nextToken;
                    nextToken = consume();
                }
                eTemp2 = expr();
            }
        }
        return new PixelSelector(currentToken,eTemp,eTemp2);
    }
    public ExpandedPixelExpr ExtendedPix() throws PLCException
    {
        IToken currentToken;
        Expr eTemp = null;
        Expr preE2 =null;
        Expr eTemp2 =null;
        if(nextToken.getKind() == IToken.Kind.LSQUARE) {
            currentToken = firstToken;
            firstToken = nextToken;
            nextToken = consume();
            eTemp = expr();
            while (nextToken.getKind() != IToken.Kind.RSQUARE)
            {
                if(preE2 != null)
                {
                    eTemp2 = preE2;
                }
                if(nextToken.getKind() == IToken.Kind.COMMA)
                {
                    firstToken = nextToken;
                    nextToken = consume();
                }
                preE2 = expr();
            }
            pixelExpr =new ExpandedPixelExpr(currentToken,eTemp,eTemp2,preE2);
        }
        return pixelExpr;
    }


    public Declaration declar() throws PLCException {
        boolean sel = false;
        boolean boolC = false;
//        boolean ExpandedPixel = false;
  //      IToken primaryToken = null;
        Expr ex = null;
        Dimension dim =null;
        nameDf = nameDef();
        IToken currentToken = nextToken;
        firstToken = nextToken;
        nextToken = consume();
        if(nextToken.getKind() != IToken.Kind.ASSIGN)
        {
            decl = new Declaration(currentToken,nameDf,ex);
            declarBoo = true;
        }
        else
        {
            firstToken = nextToken;
            nextToken = consume();
            ex = expr();
            if(nextToken.getKind() == IToken.Kind.LSQUARE)
            {
                sel = true;
                pix = Pix();
                firstToken = nextToken;
                nextToken = consume();
            }

            if(nextToken.getKind() == IToken.Kind.COLON)
            {
                if(firstToken.getKind() == IToken.Kind.DOT)
                {
                    boolC = false;
                    invalidStmt = true;
                }
                else
                {
                    boolC = true;
                    firstToken = nextToken;
                    nextToken = consume();
                    chanSelect = chanSelect();
                }
            }
            if(sel == true || boolC == true )
            {
                ex = new UnaryExprPostfix(nextToken,ex,pix,chanSelect);
                firstToken = nextToken;
                nextToken = consume();
            }
            decl = new Declaration(currentToken,nameDf,ex);

        }
        return decl;
    }

    public Expr expr() throws PLCException
    {
        kind = firstToken.getKind();
        if(kind != IToken.Kind.RES_if)
        {
            leftExpr = orExpr();
        }
        else
        {
            leftExpr =  conditionalExpr();
        }

        return leftExpr;
    }
    public Expr primaryExpr() throws PLCException {
        IToken  currentToken;

        if(nextToken == null)
        {
            currentToken = firstToken;
        }

        else
        {
            currentToken = nextToken;
            if(conditionBoo != true && parentBoo != true &&currentToken.getKind() != IToken.Kind.EOF && currentToken.getKind() != IToken.Kind.STRING_LIT)
            {
                firstToken = currentToken;
                nextToken = consume();
            }

        }


        if(currentToken.getKind() == IToken.Kind.NUM_LIT)
        {
            numLit = new NumLitExpr(currentToken);
            if(nextToken == null)
            {
                firstToken = nextToken;
                nextToken = consume();
            }
            IToken.Kind eofKind = null;
            if(nextToken != null)
            {
                eofKind = nextToken.getKind();
            }
            if(eofKind != IToken.Kind.EOF && eofKind != IToken.Kind.RSQUARE && eofKind != IToken.Kind.PLUS)
            {
                firstToken = nextToken;
                nextToken = consume();
            }

            if(nextToken.getKind() == IToken.Kind.EOF || parentBoo == true)
                return numLit;
            else if(nextToken.getKind() == IToken.Kind.PLUS || nextToken.getKind() == IToken.Kind.MINUS|| nextToken.getKind() == IToken.Kind.DIV|| nextToken.getKind() == IToken.Kind.TIMES|| nextToken.getKind() == IToken.Kind.MOD|| nextToken.getKind() == IToken.Kind.EXP)
            {
                if(leftBinaryExp == null)
                    leftBinaryExp = numLit;
                else
                {
                    binary = new BinaryExpr(currentToken,leftBinaryExp,pOp,numLit);
                    leftBinaryExp = binary;
                }
                pOp = nextToken.getKind();
                nextToken = consume();
                if(nextToken.getKind() == IToken.Kind.PLUS || nextToken.getKind() == IToken.Kind.MINUS|| nextToken.getKind() == IToken.Kind.DIV|| nextToken.getKind() == IToken.Kind.TIMES|| nextToken.getKind() == IToken.Kind.MOD)
                    throw new SyntaxException("Invalid Op");
                else
                {

                    rightExpr = expr();
                    binary = new BinaryExpr(currentToken,leftBinaryExp,pOp,rightExpr);
                    return binary;
                }


            }
            else if(nextToken.getKind() == IToken.Kind.BITOR || nextToken.getKind() == IToken.Kind.OR || nextToken.getKind() == IToken.Kind.BITAND || nextToken.getKind() == IToken.Kind.AND)
            {
                leftBinaryExp = numLit;
                IToken.Kind op = nextToken.getKind();
                nextToken = consume();
                if(nextToken.getKind() == IToken.Kind.PLUS || nextToken.getKind() == IToken.Kind.MINUS|| nextToken.getKind() == IToken.Kind.DIV|| nextToken.getKind() == IToken.Kind.TIMES|| nextToken.getKind() == IToken.Kind.MOD)
                    throw new SyntaxException("Invalid Op");
                else
                {
                    rightExpr = expr();
                    binary = new BinaryExpr(currentToken,leftBinaryExp,op,rightExpr);
                    return binary;
                }

            }
            else if(nextToken.getKind() == IToken.Kind.DOT)
            {
                return numLit;
            }
            else if(nextToken.getKind() == IToken.Kind.GE || nextToken.getKind() == IToken.Kind.GT || nextToken.getKind() == IToken.Kind.LE || nextToken.getKind() == IToken.Kind.LT || nextToken.getKind() == IToken.Kind.EQ || nextToken.getKind() == IToken.Kind.ASSIGN)
            {
                leftBinaryExp = numLit;
                IToken.Kind op = nextToken.getKind();

                nextToken = consume();
                if(nextToken.getKind() == IToken.Kind.PLUS || nextToken.getKind() == IToken.Kind.MINUS|| nextToken.getKind() == IToken.Kind.DIV|| nextToken.getKind() == IToken.Kind.TIMES|| nextToken.getKind() == IToken.Kind.MOD)
                    throw new SyntaxException("Invalid Op");
                else
                {
                    rightExpr = expr();
                    binary = new BinaryExpr(currentToken,leftBinaryExp,op,rightExpr);
                    return binary;
                }

            }
            return numLit;
        }
        else if(currentToken.getKind() == IToken.Kind.RES_x ||currentToken.getKind() == IToken.Kind.RES_y || currentToken.getKind() == IToken.Kind.RES_r || currentToken.getKind() == IToken.Kind.RES_a )
        {
            Expr preDec =  new PredeclaredVarExpr(currentToken);
            if(inputParCh[currentPos+1] == '+' || inputParCh[currentPos+1] == '-'|| inputParCh[currentPos+1] == '*' || inputParCh[currentPos+1] == '/')
            {
                firstToken = nextToken;
                nextToken = consume();
            }
            if(nextToken.getKind() == IToken.Kind.PLUS || nextToken.getKind() == IToken.Kind.MINUS|| nextToken.getKind() == IToken.Kind.DIV|| nextToken.getKind() == IToken.Kind.TIMES|| nextToken.getKind() == IToken.Kind.MOD|| nextToken.getKind() == IToken.Kind.EXP)
            {
                leftBinaryExp = preDec;
                IToken.Kind op = nextToken.getKind();
                nextToken = consume();
                if(nextToken.getKind() == IToken.Kind.PLUS || nextToken.getKind() == IToken.Kind.MINUS|| nextToken.getKind() == IToken.Kind.DIV|| nextToken.getKind() == IToken.Kind.TIMES|| nextToken.getKind() == IToken.Kind.MOD)
                    throw new SyntaxException("Invalid Op");
                else
                {
                    rightExpr = expr();
                    binary = new BinaryExpr(currentToken,leftBinaryExp,op,rightExpr);
                    return binary;
                }



            }
            return preDec;
        }

        else if(currentToken.getKind() == IToken.Kind.STRING_LIT)
        {
            stringLit = new StringLitExpr(currentToken);
            currentPos++;
            if(inputParCh[currentPos] == '"')
            {
                currentPos++;
                while (inputParCh[currentPos] != '"' && inputParCh[currentPos] != '\n')
                {
                    currentPos++;
                }
            }

            stringLitBoo = true;
            nextToken = consume();
            if(nextToken.getKind() == IToken.Kind.PLUS || nextToken.getKind() == IToken.Kind.MINUS|| nextToken.getKind() == IToken.Kind.DIV|| nextToken.getKind() == IToken.Kind.TIMES|| nextToken.getKind() == IToken.Kind.MOD|| nextToken.getKind() == IToken.Kind.EXP)
            {
                leftBinaryExp = stringLit;
                IToken.Kind op = nextToken.getKind();

                nextToken = consume();
                if(nextToken.getKind() == IToken.Kind.PLUS || nextToken.getKind() == IToken.Kind.MINUS|| nextToken.getKind() == IToken.Kind.DIV|| nextToken.getKind() == IToken.Kind.TIMES|| nextToken.getKind() == IToken.Kind.MOD)
                    throw new SyntaxException("Invalid Op");
                else
                {
                    rightExpr = expr();
                    binary = new BinaryExpr(currentToken,leftBinaryExp,op,rightExpr);
                    return binary;
                }
            }
            else
            {
                return stringLit;
            }

        }

        else if(currentToken.getKind() == IToken.Kind.RES_rand)
        {
            random = new RandomExpr(currentToken);
            return random;
        }
        else if(currentToken.getKind() == IToken.Kind.RES_Z)
        {//z block
            z = new ZExpr(currentToken);
            IToken.Kind eofKind = null;
            if(nextToken != null)
            {
                eofKind = nextToken.getKind();
            }
            if(eofKind != IToken.Kind.EOF && nextToken.getKind() != IToken.Kind.PLUS && nextToken.getKind() != IToken.Kind.MINUS && nextToken.getKind() != IToken.Kind.DIV && nextToken.getKind() != IToken.Kind.TIMES && nextToken.getKind() != IToken.Kind.MOD && nextToken.getKind() != IToken.Kind.DOT)
            {
                firstToken = nextToken;
                nextToken = consume();
            }

            if(nextToken.getKind() == IToken.Kind.QUESTION || nextToken.getKind() == IToken.Kind.EOF)
            {
                return z;
            }

           IToken.Kind ntK=  nextToken.getKind();
            if(ntK == IToken.Kind.PLUS || ntK == IToken.Kind.MINUS|| ntK == IToken.Kind.DIV|| ntK == IToken.Kind.TIMES||ntK == IToken.Kind.MOD || ntK == IToken.Kind.EXP)
            {
                leftBinaryExp = z;
                IToken.Kind op = nextToken.getKind();
                firstToken = nextToken;
                nextToken = consume();
                if(nextToken.getKind() == IToken.Kind.PLUS || nextToken.getKind() == IToken.Kind.MINUS|| nextToken.getKind() == IToken.Kind.DIV|| nextToken.getKind() == IToken.Kind.TIMES|| nextToken.getKind() == IToken.Kind.MOD)
                    throw new SyntaxException("Invalid Op");
                else
                {
                    rightExpr = expr();
                    binary = new BinaryExpr(currentToken,leftBinaryExp,op,rightExpr);
                    return binary;
                }



            }
            else if(nextToken.getKind() == IToken.Kind.BITOR || nextToken.getKind() == IToken.Kind.OR || nextToken.getKind() == IToken.Kind.BITAND || nextToken.getKind() == IToken.Kind.AND)
            {
                leftBinaryExp = z;
                IToken.Kind op = nextToken.getKind();

                nextToken = consume();
                if(nextToken.getKind() == IToken.Kind.PLUS || nextToken.getKind() == IToken.Kind.MINUS|| nextToken.getKind() == IToken.Kind.DIV|| nextToken.getKind() == IToken.Kind.TIMES|| nextToken.getKind() == IToken.Kind.MOD)
                    throw new SyntaxException("Invalid Op");
                else
                {
                    rightExpr = expr();
                    binary = new BinaryExpr(currentToken,leftBinaryExp,op,rightExpr);
                    return binary;
                }

            }

            else if(nextToken.getKind() == IToken.Kind.GE || nextToken.getKind() == IToken.Kind.GT || nextToken.getKind() == IToken.Kind.LE || nextToken.getKind() == IToken.Kind.LT|| nextToken.getKind() == IToken.Kind.EQ)
            {
                leftBinaryExp = z;
                IToken.Kind op = nextToken.getKind();

                nextToken = consume();
                if(nextToken.getKind() == IToken.Kind.PLUS || nextToken.getKind() == IToken.Kind.MINUS|| nextToken.getKind() == IToken.Kind.DIV|| nextToken.getKind() == IToken.Kind.TIMES|| nextToken.getKind() == IToken.Kind.MOD)
                    throw new SyntaxException("Invalid Op");
                else
                {
                    rightExpr = expr();
                    binary = new BinaryExpr(currentToken,leftBinaryExp,op,rightExpr);
                    return binary;
                }

            }
            return z;
        }

        else if(currentToken.getKind() == IToken.Kind.IDENT)
        {

            idnt = new IdentExpr(currentToken);
            IToken.Kind eofKind = null;
            if(nextToken != null)
            {
                eofKind = nextToken.getKind();
            }
            if(eofKind != IToken.Kind.EOF && eofKind != IToken.Kind.LCURLY && eofKind != IToken.Kind.LSQUARE)
            {
                firstToken = nextToken;
                nextToken = consume();
            }

            if(nextToken.getKind() == IToken.Kind.QUESTION || nextToken.getKind() == IToken.Kind.EOF  )
            {
                return idnt;
            }
            if(nextToken.getKind() == IToken.Kind.PLUS || nextToken.getKind() == IToken.Kind.MINUS|| nextToken.getKind() == IToken.Kind.DIV|| nextToken.getKind() == IToken.Kind.TIMES|| nextToken.getKind() == IToken.Kind.MOD|| nextToken.getKind() == IToken.Kind.EXP)
            {
                leftBinaryExp = idnt;
                IToken.Kind op = nextToken.getKind();
                nextToken = consume();

                if(op== IToken.Kind.PLUS ||op == IToken.Kind.MINUS|| op == IToken.Kind.DIV|| op == IToken.Kind.TIMES||op == IToken.Kind.MOD)
                    throw new SyntaxException("Invalid op");
                else
                {
                    rightExpr = expr();
                    binary = new BinaryExpr(currentToken,leftBinaryExp,op,rightExpr);
                    return binary;
                }
            }
            else if(nextToken.getKind() == IToken.Kind.BITOR || nextToken.getKind() == IToken.Kind.OR || nextToken.getKind() == IToken.Kind.BITAND || nextToken.getKind() == IToken.Kind.AND)
            {
                leftBinaryExp = idnt;
                IToken.Kind op = nextToken.getKind();

                nextToken = consume();
                if(nextToken.getKind() == IToken.Kind.PLUS || nextToken.getKind() == IToken.Kind.MINUS|| nextToken.getKind() == IToken.Kind.DIV|| nextToken.getKind() == IToken.Kind.TIMES|| nextToken.getKind() == IToken.Kind.MOD)
                    throw new SyntaxException("Invalid Op");
                else
                {
                    rightExpr = expr();
                    binary = new BinaryExpr(currentToken,leftBinaryExp,op,rightExpr);
                    return binary;
                }

            }
            else if(nextToken.getKind() == IToken.Kind.GE || nextToken.getKind() == IToken.Kind.GT || nextToken.getKind() == IToken.Kind.LE || nextToken.getKind() == IToken.Kind.LT|| nextToken.getKind() == IToken.Kind.EQ)
            {
                leftBinaryExp = idnt;
                IToken.Kind op = nextToken.getKind();

                nextToken = consume();
                if(nextToken.getKind() == IToken.Kind.PLUS || nextToken.getKind() == IToken.Kind.MINUS|| nextToken.getKind() == IToken.Kind.DIV|| nextToken.getKind() == IToken.Kind.TIMES|| nextToken.getKind() == IToken.Kind.MOD)
                    throw new SyntaxException("Invalid Op");
                else
                {
                    rightExpr = expr();
                    binary = new BinaryExpr(currentToken,leftBinaryExp,op,rightExpr);
                    return binary;
                }

            }
            return idnt;
        }


        else if((currentToken.getKind() == IToken.Kind.LPAREN || currentToken.getKind() == IToken.Kind.RPAREN))
        {
            parentBoo = true;
            if(currentToken.getKind() == IToken.Kind.RPAREN)
            {
                return e;
            }
            else
            {
                nextToken = consume();
                firstToken = nextToken;
                if(nextToken.getKind() == IToken.Kind.BANG || nextToken.getKind() == IToken.Kind.RES_sin ||nextToken.getKind()== IToken.Kind.RES_cos||nextToken.getKind() == IToken.Kind.RES_atan||nextToken.getKind() == IToken.Kind.MINUS )
                {
                    e = unaryExpr();
                }
                else
                    e =expr();
            }
        }

        else
        { //t12()
            throw new SyntaxException("Unable to parse given expression");
        }



        return e;
    }

    public Expr unaryExpr() throws PLCException {

        kind = nextToken.getKind();
        if(kind == IToken.Kind.BANG || kind == IToken.Kind.RES_sin ||kind == IToken.Kind.RES_cos||kind == IToken.Kind.RES_atan||kind == IToken.Kind.MINUS )
        {
            IToken  currentToken = firstToken;
            firstToken = nextToken;
            nextToken =  consume();
            kind = nextToken.getKind();
            while(kind == IToken.Kind.BANG ||  kind == IToken.Kind.RES_sin ||kind == IToken.Kind.RES_cos||kind == IToken.Kind.RES_atan||kind == IToken.Kind.MINUS )
            {
                e = unaryExtension(currentToken);
            }
            e = primaryExpr();
            unary = new UnaryExpr(currentToken,currentToken.getKind(),e);
            return unary;

        }
        else
            e =  primaryExpr();
        return e;
    }

    private Expr unaryExtension(IToken currentTok) throws PLCException
    {

        IToken.Kind op = nextToken.getKind();


        rightExpr = expr();
        unary = new UnaryExpr(currentTok,op,rightExpr);
        return unary;

    }


    public Expr multiplicativeExpr() throws PLCException {

        kind = firstToken.getKind();
        leftExpr = unaryExpr();

        while(kind == IToken.Kind.TIMES ||  kind == IToken.Kind.DIV ||  kind == IToken.Kind.MOD)
        {
            consume();
            rightExpr = unaryExpr();
        }
        return leftExpr;


    }
    public Expr additiveExpr() throws PLCException {
        kind = firstToken.getKind();
        leftExpr = multiplicativeExpr();

        while(kind == IToken.Kind.PLUS ||  kind == IToken.Kind.MINUS)
        {
            consume();
            rightExpr = multiplicativeExpr();
        }
        return leftExpr;
    }
    public Expr powerExpr() throws PLCException {

        kind = firstToken.getKind();
        leftExpr = additiveExpr();

        while(kind == IToken.Kind.EXP)
        {
            consume();
            rightExpr = additiveExpr();
        }
        return leftExpr;
    }
    public Expr comparisonExpr() throws PLCException
    {
        kind = firstToken.getKind();
        leftExpr =powerExpr();

        while(kind == IToken.Kind.LT || kind == IToken.Kind.GT || kind == IToken.Kind.GE || kind == IToken.Kind.LE )
        {
            consume();
            rightExpr =powerExpr();
        }

        return leftExpr;
    }
    public Expr andExpr() throws PLCException
    {
        kind = firstToken.getKind();
        leftExpr =comparisonExpr();

        while(kind == IToken.Kind.BITAND ||kind == IToken.Kind.AND )
        {
            consume();
            rightExpr =comparisonExpr();
        }

        return leftExpr;
    }

    public Expr orExpr() throws PLCException
    {
        kind = firstToken.getKind();
        leftExpr =andExpr();

        while(kind == IToken.Kind.BITOR|| kind == IToken.Kind.OR)
        {
            consume();
            rightExpr =andExpr();
        }

        return leftExpr;
    }
    public Expr conditionalExpr() throws PLCException {
        conditionBoo = true;
        kind = firstToken.getKind();
        IToken currentToken = firstToken;
        if(kind == IToken.Kind.RES_if)
        {
            nextToken =  consume();
            gaurdE = primaryExpr();

            firstToken = nextToken;
            nextToken = consume();
            trueCase = primaryExpr();

            firstToken = nextToken;
            nextToken = consume();
            falseCase = primaryExpr();
            conditionalE = new ConditionalExpr(currentToken,gaurdE,trueCase,falseCase);

        }
        else
            throw new SyntaxException("Error");
        return conditionalE;
    }



    @Override
    public AST parse() throws PLCException,SyntaxException
    {

        inputScan = new String(inputParser);
        if(inputParser == "")
        {
            throw new SyntaxException("Empty Prog");
        }
        else
        {
            inputScan = inputParser.substring(currentPos,inputParser.length());
            scanner = CompilerComponentFactory.makeScanner(inputScan);
            firstToken = scanner.next();
            prog = program();
            return prog;
        }

    }

    @Override
    public Object visit(ASTVisitor v, Object arg) throws PLCException
    {
        return null;
    }
}



