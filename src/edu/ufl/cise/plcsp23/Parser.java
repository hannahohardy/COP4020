

        package edu.ufl.cise.plcsp23;
        import edu.ufl.cise.plcsp23.ast.*;
        import java.util.ArrayList;
        import java.util.Arrays;
        import java.util.List;

public class Parser extends AST  implements IParser {


    AST astObj;
    boolean InvalidStatementflag = false;

    boolean assignment = false;
    boolean CheckStringLit = false;
    String lexInput;
    List<Statement> stmList = new ArrayList<>();
    List<Declaration> declarationList = new ArrayList<>();

    ExpandedPixelExpr expandedPix;
    boolean expandPixelFlag = false;

    LValue lVal;
    ColorChannel chan_Select;
    PixelSelector pix;
    boolean decFlag = false;
    IToken.Kind preOp = null;

    Type LvalType;
    String inputParser;
    final char[] inputParserChars;
    IToken.Kind kind;
    IToken nextToken;
    int tempPos;
    boolean condFlag = false;
    boolean parenFlag = false;
    Expr rightE;
    Expr unaryExprLeft = null;

    ConditionalExpr conditionalE;
    Expr leftE;

    Expr leftBinaryExp = null;
    String lexTemp;
    Expr e = null;
    RandomExpr rnd;
    IdentExpr idnt;


    TypeChecker.SymbolTable symbTable = new TypeChecker.SymbolTable();
    Statement st = null;
    Ident prgIdent;
    ZExpr z;
    NumLitExpr numLit;
    StringLitExpr stringLit;
    UnaryExpr unary;
    BinaryExpr binary;
    Program prog = null;
    Block progBlock;
    List<NameDef> argList;



    NameDef nameDf;
    Expr gaurdE;
    static Declaration decl;
    Type typeVal;
    Expr trueCase;
    Expr falseCase;
    IScanner scanner;
    int currentPos = 0;
    IToken firstToken;


    public Parser(String inputParser) {
        super(null);

        this.inputParser = inputParser;
        inputParserChars = Arrays.copyOf(inputParser.toCharArray(), inputParser.length() + 1);
    }

    public Program program() throws PLCException {
        IToken lParenToken, rParenToken, lCurly, rCurly;
        kind = firstToken.getKind();
        if (kind == IToken.Kind.RES_pixel || kind == IToken.Kind.RES_string || kind == IToken.Kind.RES_int || kind == IToken.Kind.RES_void || kind == IToken.Kind.RES_image)
        {
            //check for program name
            typeVal = Type.getType(firstToken);
            nextToken = consume();
            if (nextToken.getKind() == IToken.Kind.IDENT)
            {
                prgIdent = new Ident(nextToken);
                //typCheck = new TypeChecker();


            }

            else
                throw new SyntaxException("Invalid Syntax missing ident in definition");
            //check for argList
            firstToken = nextToken;
            nextToken = consume();
            if (nextToken.getKind() == IToken.Kind.LPAREN) {
                lParenToken = nextToken;
                argList = ParamList();
                firstToken = nextToken;
                if (nextToken.getKind() == IToken.Kind.RPAREN)
                {
                    rParenToken = nextToken;
                    firstToken = nextToken;
                    nextToken = consume();
                    if (nextToken.getKind() == IToken.Kind.LCURLY)
                    {
                        progBlock =  block();
                        if (nextToken.getKind() == IToken.Kind.RCURLY || nextToken.getKind() == IToken.Kind.EOF)
                        {

                            prog = new Program(firstToken,typeVal,prgIdent,argList,progBlock);
                            if(nextToken.getKind() != IToken.Kind.EOF)
                            {
                                firstToken = nextToken;
                                nextToken = consume();
                                if(nextToken.getKind() != IToken.Kind.EOF && nextToken.getKind() != IToken.Kind.DOT)
                                {
                                    throw new SyntaxException("Syntax Error");
                                }
                            }
                        }
                    }
                    else
                    {
                        throw new SyntaxException("Invalid Syntax missing LPAREN in definition");
                    }

                }
            } else
                throw new SyntaxException("Invalid Syntax missing openParam in definition");


        }
        else
        {
            throw new SyntaxException("Invalid Syntax");
        }

        return prog;
    }

    public List<NameDef> ParamList() throws PLCException {
        List<NameDef> params = new ArrayList<>();
        NameDef param = null;
        firstToken = nextToken;
        nextToken = consume();

        while(nextToken.getKind() != IToken.Kind.RPAREN)
        {

            if(nextToken.getKind() != IToken.Kind.COMMA)
            {
                param = nameDef();
                params.add(param);
                firstToken = nextToken;
                nextToken = consume();
            }
            else
            {
                if(firstToken.getKind() == IToken.Kind.LPAREN && nextToken.getKind() == IToken.Kind.COMMA)
                {
                    throw  new SyntaxException("Invalid Syntax");
                }
                firstToken = nextToken;
                nextToken = consume();

            }


        }
        return params;
    }


    public NameDef nameDef() throws PLCException {
        Dimension dim = null;
        Type typeval = null;
        if(nextToken.getKind() != IToken.Kind.RES_void && nextToken.getKind() != IToken.Kind.RES_image && nextToken.getKind() != IToken.Kind.RES_int && nextToken.getKind() != IToken.Kind.RES_pixel && nextToken.getKind() != IToken.Kind.RES_string && firstToken.getKind() != IToken.Kind.RES_while && firstToken.getKind() != IToken.Kind.RES_string &&nextToken.getKind() != IToken.Kind.COLON )
        {
            throw new SyntaxException("Error");
        }
        else
        {
            typeval = Type.getType(nextToken);
            firstToken = nextToken;
            nextToken = consume();
        }


        if(nextToken.getKind() == IToken.Kind.LSQUARE)
        {
            dim = dimension();
            firstToken = nextToken;
            nextToken = consume();
        }

        Ident identifier = new Ident(nextToken);
        NameDef param = new NameDef(nextToken,typeval,dim, identifier);
        return param;
    }

    public Dimension dimension() throws PLCException {
        Dimension dim = null;
        IToken currentToken = null;

        Expr e1= null;
        Expr e2=null;
        if(nextToken.getKind() == IToken.Kind.LSQUARE) {
            currentToken = firstToken;
            firstToken = nextToken;
            nextToken = consume();
            e1 = expr();
            while (nextToken.getKind() != IToken.Kind.RSQUARE)
            {
                if(nextToken.getKind() == IToken.Kind.COMMA)
                {
                    firstToken = nextToken;
                    nextToken = consume();
                }
                e2 = expr();
//               firstToken = nextToken;
//               nextToken = consume();

            }

            dim =new Dimension(currentToken,e1,e2);
        }
        return dim;
    }
    //block statement
    public Block block() throws PLCException {
        IToken currentToken = nextToken;
        List<Declaration> declarationList = new ArrayList<>();
        List<Statement> stmList = new ArrayList<>();
        firstToken = nextToken;
        nextToken = consume();

        while(nextToken.getKind() != IToken.Kind.RCURLY)
        {
            if(nextToken.getKind() == IToken.Kind.EOF)
            {
                break;
            }
            if(nextToken.getKind() != IToken.Kind.RES_write && nextToken.getKind() != IToken.Kind.RES_while &&nextToken.getKind()!= IToken.Kind.IDENT)
            {
                if(nextToken.getKind() == IToken.Kind.DOT)
                {
                    firstToken = nextToken;
                    nextToken = consume();
                    if(nextToken.getKind() == IToken.Kind.DOT)
                    {
                        throw new SyntaxException("Error Invalid Syntax");
                    }
                    continue;
                }
                else if(nextToken.getKind() == IToken.Kind.COLON && InvalidStatementflag != true)
                {
                    if(firstToken.getKind() == IToken.Kind.LCURLY)
                    {

                        firstToken = nextToken;
                        nextToken = consume();
                        if(nextToken.getKind() != IToken.Kind.IDENT && nextToken.getKind() != IToken.Kind.NUM_LIT)
                        {
                            InvalidStatementflag = false;
                        }
                        else
                        {
                            InvalidStatementflag = true;
                        }
                        stmList.add(statement());
                        firstToken = nextToken;
                        nextToken = consume();
                    }

                }
                else
                {
                    if(firstToken.getKind() != IToken.Kind.RES_while)
                    {
                        if(InvalidStatementflag != true)
                        {
                            decl = declar();
                            declarationList.add(decl);
                        }
                        else
                        {
                            firstToken = nextToken;
                            nextToken = consume();
                            stmList.add(statement());
                            firstToken = nextToken;
                            nextToken = consume();

                        }

                    }



                }

            }

            else
            {
                if(nextToken.getKind() != IToken.Kind.IDENT)
                {
                    firstToken = nextToken;
                    nextToken = consume();
                }

                stmList.add(statement());

            }


        }
        progBlock = new Block(currentToken,declarationList,stmList);
        return progBlock;
    }

    //    public List<Statement> statementList() throws PLCException
//    {
//        List<Statement> stmList = new ArrayList<>();
//        stmList.add(statement());
//        return stmList;
//    }
    public Statement statement() throws PLCException {
        boolean pixSelect = false;
        boolean chan = false;
        IToken primaryToken = null;
        Expr ex = null;
        if(firstToken.getKind() == IToken.Kind.RES_write)
        {
            ex = expr();
            WriteStatement wrt = new WriteStatement(firstToken, ex);
            return wrt;
        }
        else if(firstToken.getKind() == IToken.Kind.RES_while)
        {

            ex = expr();
            progBlock = block();
            WhileStatement whilst = new WhileStatement(firstToken,ex,progBlock);
            return whilst;

        }

        else
        {
            if(InvalidStatementflag == true)
            {
                Expr invalidExpr = new IdentExpr(nextToken);
                st = new AssignmentStatement(firstToken,lVal,invalidExpr);
                return st;

            }
            if(firstToken.getKind() == IToken.Kind.COLON)
            {
                ex = expr();
                st = new ReturnStatement(firstToken,ex);
                return st;

            }
            Expr e1= null;
            Expr e2=null;
            // left side
            primaryToken = nextToken;
            ex = expr();
            if(nextToken.getKind() == IToken.Kind.LSQUARE)
            {
                pixSelect = true;
                pix = Pix();
                firstToken = nextToken;
                nextToken = consume();
            }

            if(nextToken.getKind() == IToken.Kind.COLON)
            {
                chan = true;
                firstToken = nextToken;
                nextToken = consume();
                chan_Select = chan_Select();
                if(chan_Select == null)
                {
                    chan = false;
                }

            }

            if(pixSelect == true || chan == true)
            {
                ex = new UnaryExprPostfix(nextToken,ex,pix,chan_Select);
            }

            lVal = new LValue(primaryToken,new Ident(primaryToken),pix,chan_Select);
            firstToken = nextToken;
            nextToken = consume();
            //rValue

            primaryToken = nextToken;
//            e = expr();
            if(nextToken.getKind() == IToken.Kind.LSQUARE)
            {
                pixSelect = true;
                if(firstToken.getKind() == IToken.Kind.ASSIGN)
                {
                    expandPixelFlag = true;
                    pixSelect = false;
                    expandedPix = ExtendedPix();
                }
                else
                {
                    pixSelect = true;
                    expandPixelFlag = false;
                    pix = Pix();
                }

                firstToken = nextToken;
                nextToken = consume();
            }

            if(nextToken.getKind() == IToken.Kind.COLON)
            {
                chan = true;
                firstToken = nextToken;
                nextToken = consume();
                chan_Select = chan_Select();

            }
            if(pixSelect == true || chan == true || expandPixelFlag == true)
            {
                if(pixSelect == true)
                {
                    ex = new UnaryExprPostfix(primaryToken,new IdentExpr(primaryToken),pix,chan_Select);
                }
                else
                {
                    ex = expandedPix;
                }


            }
            st = new AssignmentStatement(primaryToken,lVal,ex);
        }
        return st;
    }
    public PixelSelector Pix() throws PLCException {
        IToken currentToken;
        Expr e1= null;
        Expr e2=null;
        if(nextToken.getKind() == IToken.Kind.LSQUARE) {
            currentToken = firstToken;
            firstToken = nextToken;
            nextToken = consume();
            e1 = expr();
            while (nextToken.getKind() != IToken.Kind.RSQUARE)
            {
                if(nextToken.getKind() == IToken.Kind.COMMA)
                {
                    firstToken = nextToken;
                    nextToken = consume();
                }
                e2 = expr();
//               firstToken = nextToken;
//               nextToken = consume();

            }

            pix =new PixelSelector(currentToken,e1,e2);
        }
        return pix;
    }
    public ExpandedPixelExpr ExtendedPix() throws PLCException
    {
        IToken currentToken;
        Expr e1= null;
        Expr e2_Org=null;
        Expr e2 =null;
        if(nextToken.getKind() == IToken.Kind.LSQUARE) {
            currentToken = firstToken;
            firstToken = nextToken;
            nextToken = consume();
            e1 = expr();
            while (nextToken.getKind() != IToken.Kind.RSQUARE)
            {
                if(e2_Org != null)
                {
                    e2 = e2_Org;
                }
                if(nextToken.getKind() == IToken.Kind.COMMA)
                {
                    firstToken = nextToken;
                    nextToken = consume();
                }
                e2_Org = expr();
//               firstToken = nextToken;
//               nextToken = consume();

            }

            expandedPix =new ExpandedPixelExpr(currentToken,e1,e2,e2_Org);
        }
        return expandedPix;
    }

    public ColorChannel chan_Select() throws TypeCheckException {
        if(nextToken.getKind() == IToken.Kind.RES_grn || nextToken.getKind() == IToken.Kind.RES_blu|| nextToken.getKind() == IToken.Kind.RES_red)
        {
            chan_Select = ColorChannel.getColor(nextToken);
        }

        return chan_Select;
    }



    public Declaration declar() throws PLCException {
        boolean pixSelect = false;
        boolean chan = false;
        boolean ExpandedPixel = false;
        IToken primaryToken = null;
        Expr ex = null;
        Dimension dim =null;
        nameDf = nameDef();
        IToken currentToken = nextToken;
        firstToken = nextToken;
        nextToken = consume();
        if(nextToken.getKind() != IToken.Kind.ASSIGN)
        {
            decl = new Declaration(currentToken,nameDf,ex);
            decFlag = true;
        }
        else
        {
            firstToken = nextToken;
            nextToken = consume();
            primaryToken = nextToken;
            ex = expr();
            if(nextToken.getKind() == IToken.Kind.LSQUARE)
            {
                pixSelect = true;
                pix = Pix();
                firstToken = nextToken;
                nextToken = consume();
            }

            if(nextToken.getKind() == IToken.Kind.COLON)
            {
                if(firstToken.getKind() == IToken.Kind.DOT)
                {
                    chan = false;
                    InvalidStatementflag = true;
                }
                else
                {
                    chan = true;
                    firstToken = nextToken;
                    nextToken = consume();
                    chan_Select = chan_Select();
                }
            }
            if(pixSelect == true || chan == true )
            {
                ex = new UnaryExprPostfix(nextToken,ex,pix,chan_Select);
                firstToken = nextToken;
                nextToken = consume();
            }
            decl = new Declaration(currentToken,nameDf,ex);

        }
        return decl;
    }
    /*
     **Function for expression productions
     * <expr> ::= <conditional_expr> | <or_expr>
     */
    public Expr expr() throws PLCException
    {
        kind = firstToken.getKind();
        if(kind != IToken.Kind.RES_if)
        {
            leftE = orExpr();
        }
        else
        {
            leftE =  conditionalExpr();
        }

        return leftE;
    }
    public IToken consume() throws SyntaxException, LexicalException {

        while(inputParserChars[currentPos] == ' ' || inputParserChars[currentPos] == '\n' || inputParserChars[currentPos] == '\t')
        {
            currentPos++;
        }

        if(firstToken.getSourceLocation() != null && firstToken.getKind() != IToken.Kind.EOF)
        {

            //  currentPos =  currentPos+firstToken.getSourceLocation().column();
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
//       currentPos = currentPos+ token.getTokenString().length();
        return token;

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
            if(condFlag != true && parenFlag != true &&currentToken.getKind() != IToken.Kind.EOF && currentToken.getKind() != IToken.Kind.STRING_LIT)
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

            if(nextToken.getKind() == IToken.Kind.EOF || parenFlag == true)
                return numLit;
                //arithmetic
            else if(nextToken.getKind() == IToken.Kind.PLUS || nextToken.getKind() == IToken.Kind.MINUS|| nextToken.getKind() == IToken.Kind.DIV|| nextToken.getKind() == IToken.Kind.TIMES|| nextToken.getKind() == IToken.Kind.MOD|| nextToken.getKind() == IToken.Kind.EXP)
            {
                if(leftBinaryExp == null)
                    leftBinaryExp = numLit;
                else
                {
                    binary = new BinaryExpr(currentToken,leftBinaryExp,preOp,numLit);
                    leftBinaryExp = binary;
                }
                preOp = nextToken.getKind();
                nextToken = consume();
                if(nextToken.getKind() == IToken.Kind.PLUS || nextToken.getKind() == IToken.Kind.MINUS|| nextToken.getKind() == IToken.Kind.DIV|| nextToken.getKind() == IToken.Kind.TIMES|| nextToken.getKind() == IToken.Kind.MOD)
                    throw new SyntaxException("Invalid Op");
                else
                {

                    rightE = expr();
                    binary = new BinaryExpr(currentToken,leftBinaryExp,preOp,rightE);
                    return binary;
                }


            }
            //Logical
            else if(nextToken.getKind() == IToken.Kind.BITOR || nextToken.getKind() == IToken.Kind.OR || nextToken.getKind() == IToken.Kind.BITAND || nextToken.getKind() == IToken.Kind.AND)
            {
                leftBinaryExp = numLit;
                IToken.Kind op = nextToken.getKind();
                nextToken = consume();
                if(nextToken.getKind() == IToken.Kind.PLUS || nextToken.getKind() == IToken.Kind.MINUS|| nextToken.getKind() == IToken.Kind.DIV|| nextToken.getKind() == IToken.Kind.TIMES|| nextToken.getKind() == IToken.Kind.MOD)
                    throw new SyntaxException("Invalid Op");
                else
                {
                    rightE = expr();
                    binary = new BinaryExpr(currentToken,leftBinaryExp,op,rightE);
                    return binary;
                }

            }
            //DOT
            else if(nextToken.getKind() == IToken.Kind.DOT)
            {
                return numLit;
            }
            //relational
            else if(nextToken.getKind() == IToken.Kind.GE || nextToken.getKind() == IToken.Kind.GT || nextToken.getKind() == IToken.Kind.LE || nextToken.getKind() == IToken.Kind.LT || nextToken.getKind() == IToken.Kind.EQ || nextToken.getKind() == IToken.Kind.ASSIGN)
            {
                leftBinaryExp = numLit;
                IToken.Kind op = nextToken.getKind();

                nextToken = consume();
                if(nextToken.getKind() == IToken.Kind.PLUS || nextToken.getKind() == IToken.Kind.MINUS|| nextToken.getKind() == IToken.Kind.DIV|| nextToken.getKind() == IToken.Kind.TIMES|| nextToken.getKind() == IToken.Kind.MOD)
                    throw new SyntaxException("Invalid Op");
                else
                {
                    rightE = expr();
                    binary = new BinaryExpr(currentToken,leftBinaryExp,op,rightE);
                    return binary;
                }

            }
            return numLit;
        }
        else if(currentToken.getKind() == IToken.Kind.RES_x ||currentToken.getKind() == IToken.Kind.RES_y || currentToken.getKind() == IToken.Kind.RES_r || currentToken.getKind() == IToken.Kind.RES_a )
        {
            Expr preDec =  new PredeclaredVarExpr(currentToken);
            if(inputParserChars[currentPos+1] == '+' || inputParserChars[currentPos+1] == '-'|| inputParserChars[currentPos+1] == '*' || inputParserChars[currentPos+1] == '/')
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
                    rightE = expr();
                    binary = new BinaryExpr(currentToken,leftBinaryExp,op,rightE);
                    return binary;
                }



            }
            return preDec;
        }

        else if(currentToken.getKind() == IToken.Kind.STRING_LIT)
        {
            stringLit = new StringLitExpr(currentToken);
            currentPos++;
            if(inputParserChars[currentPos] == '"')
            {
                currentPos++;
                while (inputParserChars[currentPos] != '"' && inputParserChars[currentPos] != '\n')
                {
                    currentPos++;
                }
            }

            CheckStringLit = true;
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
                    rightE = expr();
                    binary = new BinaryExpr(currentToken,leftBinaryExp,op,rightE);
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
            rnd = new RandomExpr(currentToken);
            return rnd;
        }
        // Z block starts
        else if(currentToken.getKind() == IToken.Kind.RES_Z)
        {
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

            //binary arithmetic operation
            if(nextToken.getKind() == IToken.Kind.PLUS || nextToken.getKind() == IToken.Kind.MINUS|| nextToken.getKind() == IToken.Kind.DIV|| nextToken.getKind() == IToken.Kind.TIMES|| nextToken.getKind() == IToken.Kind.MOD || nextToken.getKind() == IToken.Kind.EXP)
            {
                leftBinaryExp = z;
                IToken.Kind op = nextToken.getKind();
                firstToken = nextToken;
                nextToken = consume();
                if(nextToken.getKind() == IToken.Kind.PLUS || nextToken.getKind() == IToken.Kind.MINUS|| nextToken.getKind() == IToken.Kind.DIV|| nextToken.getKind() == IToken.Kind.TIMES|| nextToken.getKind() == IToken.Kind.MOD)
                    throw new SyntaxException("Invalid Op");
                else
                {
                    rightE = expr();
                    binary = new BinaryExpr(currentToken,leftBinaryExp,op,rightE);
                    return binary;
                }



            }
            //LOGICAL
            else if(nextToken.getKind() == IToken.Kind.BITOR || nextToken.getKind() == IToken.Kind.OR || nextToken.getKind() == IToken.Kind.BITAND || nextToken.getKind() == IToken.Kind.AND)
            {
                leftBinaryExp = z;
                IToken.Kind op = nextToken.getKind();

                nextToken = consume();
                if(nextToken.getKind() == IToken.Kind.PLUS || nextToken.getKind() == IToken.Kind.MINUS|| nextToken.getKind() == IToken.Kind.DIV|| nextToken.getKind() == IToken.Kind.TIMES|| nextToken.getKind() == IToken.Kind.MOD)
                    throw new SyntaxException("Invalid Op");
                else
                {
                    rightE = expr();
                    binary = new BinaryExpr(currentToken,leftBinaryExp,op,rightE);
                    return binary;
                }

            }

            //relational operator
            else if(nextToken.getKind() == IToken.Kind.GE || nextToken.getKind() == IToken.Kind.GT || nextToken.getKind() == IToken.Kind.LE || nextToken.getKind() == IToken.Kind.LT|| nextToken.getKind() == IToken.Kind.EQ)
            {
                leftBinaryExp = z;
                IToken.Kind op = nextToken.getKind();

                nextToken = consume();
                if(nextToken.getKind() == IToken.Kind.PLUS || nextToken.getKind() == IToken.Kind.MINUS|| nextToken.getKind() == IToken.Kind.DIV|| nextToken.getKind() == IToken.Kind.TIMES|| nextToken.getKind() == IToken.Kind.MOD)
                    throw new SyntaxException("Invalid Op");
                else
                {
                    rightE = expr();
                    binary = new BinaryExpr(currentToken,leftBinaryExp,op,rightE);
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



            //binary arithmetic operation
            if(nextToken.getKind() == IToken.Kind.PLUS || nextToken.getKind() == IToken.Kind.MINUS|| nextToken.getKind() == IToken.Kind.DIV|| nextToken.getKind() == IToken.Kind.TIMES|| nextToken.getKind() == IToken.Kind.MOD|| nextToken.getKind() == IToken.Kind.EXP)
            {
                leftBinaryExp = idnt;
                IToken.Kind op = nextToken.getKind();
                nextToken = consume();
                if(nextToken.getKind() == IToken.Kind.PLUS || nextToken.getKind() == IToken.Kind.MINUS|| nextToken.getKind() == IToken.Kind.DIV|| nextToken.getKind() == IToken.Kind.TIMES|| nextToken.getKind() == IToken.Kind.MOD)
                    throw new SyntaxException("Invalid Op");
                else
                {
                    rightE = expr();
                    binary = new BinaryExpr(currentToken,leftBinaryExp,op,rightE);
                    return binary;
                }



            }
            //Logical Op
            else if(nextToken.getKind() == IToken.Kind.BITOR || nextToken.getKind() == IToken.Kind.OR || nextToken.getKind() == IToken.Kind.BITAND || nextToken.getKind() == IToken.Kind.AND)
            {
                leftBinaryExp = idnt;
                IToken.Kind op = nextToken.getKind();

                nextToken = consume();
                if(nextToken.getKind() == IToken.Kind.PLUS || nextToken.getKind() == IToken.Kind.MINUS|| nextToken.getKind() == IToken.Kind.DIV|| nextToken.getKind() == IToken.Kind.TIMES|| nextToken.getKind() == IToken.Kind.MOD)
                    throw new SyntaxException("Invalid Op");
                else
                {
                    rightE = expr();
                    binary = new BinaryExpr(currentToken,leftBinaryExp,op,rightE);
                    return binary;
                }

            }
            //relational
            else if(nextToken.getKind() == IToken.Kind.GE || nextToken.getKind() == IToken.Kind.GT || nextToken.getKind() == IToken.Kind.LE || nextToken.getKind() == IToken.Kind.LT|| nextToken.getKind() == IToken.Kind.EQ)
            {
                leftBinaryExp = idnt;
                IToken.Kind op = nextToken.getKind();

                nextToken = consume();
                if(nextToken.getKind() == IToken.Kind.PLUS || nextToken.getKind() == IToken.Kind.MINUS|| nextToken.getKind() == IToken.Kind.DIV|| nextToken.getKind() == IToken.Kind.TIMES|| nextToken.getKind() == IToken.Kind.MOD)
                    throw new SyntaxException("Invalid Op");
                else
                {
                    rightE = expr();
                    binary = new BinaryExpr(currentToken,leftBinaryExp,op,rightE);
                    return binary;
                }

            }
            return idnt;
        }


        else if((currentToken.getKind() == IToken.Kind.LPAREN || currentToken.getKind() == IToken.Kind.RPAREN))
        {
            parenFlag = true;
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
        {
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


        rightE = expr();
        unary = new UnaryExpr(currentTok,op,rightE);
        return unary;

    }


    public Expr multiplicativeExpr() throws PLCException {
//        leftE = null;
//        rightE = null;
        kind = firstToken.getKind();
        leftE = unaryExpr();

        while(kind == IToken.Kind.TIMES ||  kind == IToken.Kind.DIV ||  kind == IToken.Kind.MOD)
        {
            consume();
            rightE = unaryExpr();
            // leftE =
        }
        return leftE;


    }
    public Expr additiveExpr() throws PLCException {
        kind = firstToken.getKind();
        leftE = multiplicativeExpr();

        while(kind == IToken.Kind.PLUS ||  kind == IToken.Kind.MINUS)
        {
            consume();
            rightE = multiplicativeExpr();
        }
        return leftE;
    }
    public Expr powerExpr() throws PLCException {
//        leftE = null;
//        rightE = null;
        kind = firstToken.getKind();
        leftE = additiveExpr();

        while(kind == IToken.Kind.EXP)
        {
            consume();
            rightE = additiveExpr();
            // leftE =
        }
        return leftE;
    }
    public Expr comparisonExpr() throws PLCException
    {
        kind = firstToken.getKind();
        leftE =powerExpr();

        while(kind == IToken.Kind.LT || kind == IToken.Kind.GT || kind == IToken.Kind.GE || kind == IToken.Kind.LE )
        {
            consume();
            rightE =powerExpr();
        }

        return leftE;
    }
    public Expr andExpr() throws PLCException
    {
        kind = firstToken.getKind();
        leftE =comparisonExpr();

        while(kind == IToken.Kind.AND ||  kind == IToken.Kind.BITAND)
        {
            consume();
            rightE =comparisonExpr();
        }

        return leftE;
    }

    public Expr orExpr() throws PLCException
    {
        kind = firstToken.getKind();
        leftE =andExpr();

        while(kind == IToken.Kind.OR ||  kind == IToken.Kind.BITOR)
        {
            consume();
            rightE =andExpr();
        }

        return leftE;
    }
    public Expr conditionalExpr() throws PLCException {
        condFlag = true;
        kind = firstToken.getKind();
        IToken currentToken = firstToken;
        if(kind == IToken.Kind.RES_if)
        {
            nextToken =  consume();
            gaurdE = primaryExpr();
//            if(nextToken.getKind() != IToken.Kind.QUESTION)
//            {
//                 firstToken = nextToken;
//                 nextToken = consume();
//
//            }
            firstToken = nextToken;
            nextToken = consume();
            trueCase = primaryExpr();
//            while(nextToken.getKind() != IToken.Kind.QUESTION)
//            {
//                firstToken = nextToken;
//                nextToken = consume();
//
//            }
            firstToken = nextToken;
            nextToken = consume();
            falseCase = primaryExpr();
            //  kind = nextToken.getKind();
            conditionalE = new ConditionalExpr(currentToken,gaurdE,trueCase,falseCase);

        }
        else
            throw new SyntaxException("Error");
        return conditionalE;
    }



    @Override
    public AST parse() throws PLCException,SyntaxException
    {

        lexInput = new String(inputParser);
        if(inputParser == "")
        {
            throw new SyntaxException("Empty Prog");
        }
        else
        {
            lexInput = inputParser.substring(currentPos,inputParser.length());
            scanner = CompilerComponentFactory.makeScanner(lexInput);
            firstToken = scanner.next();
            // e = expr();
            prog = program();
//          Object obj =  typCheck.visitProgram(prog,decl);
//          TypeChecker typeChecker = new TypeChecker();
//          symbTable.insert(prog.toString(),decl);


            return prog;
            // return e;
        }

    }

//    public static AST getInstance()
//    {
//        if()
//    }

    @Override
    public Object visit(ASTVisitor v, Object arg) throws PLCException
    {
//       typCheck = new TypeChecker();
//       typCheck.addEntry(v.toString(),decl);
        return null;
    }
}






//package edu.ufl.cise.plcsp23;
//
//import edu.ufl.cise.plcsp23.ast.*;
//import java.util.List;
//import edu.ufl.cise.plcsp23.IToken.Kind;
//import static edu.ufl.cise.plcsp23.IToken.Kind.*;
//
////Parser class to implement the IParser.java interface
//public class Parser implements IParser {
//
//    private int current;                //Holds current position
//    IToken t;   //Always holds the current token
//
//    private List<Token> tokens;   //List of tokens
//
//
//    private IScanner scanner;
//
//    //Parser Constructor -- FIX TOKEN LIST
//    public Parser(List<Token> tokens) {
//        this.tokens = tokens;
//        this.current = 0;
//        this.t = peek();
//    }
//
//    //Function to parse AST and throws PLCException if not valid AST -- NEEDS WORK
//
//    public AST parse() throws PLCException {
//    this.tokens = tokens;
//       this.current = 0;// token
//        this.t = peek(); // this t is what is returning [BANG , MINUS]
//        return expression();
//    }
//
//    private static class ParseError extends SyntaxException {
//        public ParseError(String message) {
//            super(message);
//        }
//    }
//
//
//    private static void report(int line, String where, String message)
//    {System.err.println("[line " + line + "] Error" + where + ": " + message);}
//    //< lox-error
////> Parsing Expressions token-error
//
//
//
//    //Function to help with errors in consume -- From TB needs work (IDK WHAT LOX IS)
//
//    //Function to print errors -- From TB needs work (IDK WHAT REPORT IS)
//    private ParseError error(Token token, String message) {
//        if (token.kind == IToken.Kind.EOF) {
//            report(token.line, " at end", message);
//        } else {
//            report(token.line, " at '" + token.getTokenString() + "'", message);
//        }
//        return new ParseError(message);
//    }
//
////    Function to parse single expression and return it
////    returns an error if not a valid expression
////    Expr parse() {
////        try {
////            return expression();
////        } catch (ParseError error) {
////            return null;
////        }
////    }
//
//    //Returns true if the input kind is the same as the current token, false otherwise
//    protected boolean isKind(Kind kind) {
//        return t.getKind() == kind;
//    }
//    //Returns true if the input kinds of the token match the current token kind, false otherwise
//    protected boolean isKind(Kind... kinds) {
//        for (Kind k : kinds) {
//            if (k == t.getKind())
//                return true;
//        }
//        return false;
//    }
//
//    //Checks if we’ve run out of tokens to parse
//    private boolean isAtEnd() {
//        return peek().kind == IToken.Kind.EOF;
//    }
//
//    private Token consume(IToken.Kind kind, String message) throws SyntaxException {
//        if (check(kind)) {return advance();}
//    throw new SyntaxException(message);
//    }
//    //Get current token we have yet to consume
//    private Token peek() {
//        return tokens.get(current);
//    }
//
//    //Returns the most recently consumed token
//    private Token previous() {
//        return tokens.get(current - 1);
//    }
//
//    //Consumes the current token and returns it,
//    //similar to how our scanner’s corresponding method crawled through characters.
//    private Token advance() {
//        if (!isAtEnd()) current++;
//        return previous();
//    }
//
//    //It checks to see if the next token is of the expected kind.
//    //If so, it consumes the token.
//    //If some other token is there, then we’ve hit an error
//
//
//    //returns true if the current token is of the given kind.
//    //Unlike match(), it never consumes the token, it only looks at it.
//    private boolean check(IToken.Kind kind) {
//        if (isAtEnd()) return false;
//        return peek().kind == kind;
//    }
//
//    //Checks to see if the current token has any of the given kinds.
//    //If so, it consumes the token and returns true.
//    //Otherwise, it returns false and leaves the current token alone.
//    private boolean match(IToken.Kind... tokenKind) {
//        for (IToken.Kind kind : tokenKind) {
//            if (check(kind)) {
//                advance();
//                return true;
//            }
//        }
//        return false;
//    }
//
//    //NEED TO FIX ERROR FUNCTION TO FIX CONSUME FUNCTION TO FIX EXPR
//    //Expression Expr: <expr> ::= <conditional_expr> | <or_expr>
//    private Expr expression() throws SyntaxException {
//        return conditional();
////        IToken firstToken = t;  //Save current token
////        Expr e = null;
////        if(isKind(RES_if)){
////            e = conditional();
////        } else if (isKind(OR)||isKind(BITOR)) {
////            e = or();
////        }
////        throw new ParseError("Unexpected token in expression");
////        return e;
//    }
//
////REVISE FUNCTIONS BELOW FROM TB------------------------------------------------------------------------------------
//
//    //Conditional Expr: <conditional_expr> ::= if <expr> ? <expr> ? <expr>
//    private Expr conditional() throws SyntaxException {
//        Expr expr = or();
//        if (match(RES_if)){
//            Expr cond = expression();
//            consume(QUESTION, "Expected '?' after condition");
//            Expr thenExpr = expression();
//            consume(COLON, "Expected ':' after 'then expression");
//            Expr elseExpr = expression();
//            expr = new ConditionalExpr(expr.firstToken, expr, thenExpr, elseExpr);
//        }
//
////        IToken firstToken = t;
////        return null;
//        return expr;
//
//    }
//
//    //Or Expr: <or_expr> ::= <and_expr> ( ( | | || ) <and_expr>)*
//    private Expr or() {
//        Expr left = and();
//
//        while (match(OR,BITOR)) {
//            Token operator = previous();
//            Expr right = and();
//
//            left = new BinaryExpr(or().firstToken, left, operator.kind, right);
//        }
//        return left;
//    }
//
//    //And Expr: <and_expr> ::= <comparison_expr> ( ( & | && ) <comparison_expr>)*
//    private Expr and(){
//        Expr left = comparison();
//
//        while(match(AND,BITAND)){
//            Token operator = previous();
//            Expr right = comparison();
//            left = new BinaryExpr(left.firstToken, left, operator.kind, right);
//        }
//
//        return left;
//    }
//
//    //Comparison Expr: <comparison_expr> ::= <power_expr> ( (< | > | == | <= | >=) <power_expr>)*
//    private Expr comparison() {
//        Expr left = power();
//
//        while (match(GT, GE, LT, LE, EQ)) {
//            Token operator = previous();
//            Expr right = power();
//            left = new BinaryExpr(left.firstToken, left, operator.kind, right);
//        }
//        return left;
//    }
//
//    //Power Expr: <power_expr> ::= <additive_expr> ** <power_expr> | <additive_expr>
//    private Expr power() {
//        Expr left = additive();
//
//        while(match(EXP)){
//            Token operator = previous();
//            Expr right = unary();
//            left = new BinaryExpr(left.firstToken, left, operator.kind, right);
//        }
//
//        return left;
//    }
//
//    //In tb they called this function term()
//    //Additive expr: <additive_expr> ::= <multiplicative_expr> ( ( + | - ) <multiplicative_expr> )*
//    private Expr additive() {
//        Expr left = multiplicative();
//
//        while (match(PLUS, MINUS)) {
//            Token operator = previous();
//            Expr right = multiplicative();
//            left = new BinaryExpr(left.firstToken, left, operator.kind, right);
//        }
//        return left;
//    }
//
//    //In tb: called factor()
//    //Multiplicative Expr: <multiplicative_expr> ::= <unary_expr> (( * | / | % ) <unary_expr>)*
//    private Expr multiplicative() {
//        Expr left = unary();
//
//        while (match(TIMES, DIV, MOD)) {
//            Token operator = previous();
//            Expr right = unary();
//            left = new BinaryExpr(left.firstToken, left, operator.kind, right);
//        }
//        return left;
//    }
//
//    //Unary Expr: <unary_expr> ::= ( ! | - | sin | cos | atan) <unary_expr> | <primary_expr>
//    private Expr unary() {
//        if (match( BANG, MINUS)) {
//            Token operator = previous();
//            Expr right = unary();
//            return new UnaryExpr(unary().firstToken, operator.kind, right);
//        }
//        return primary();
//    }
//
//    //Primary Expr: <primary_expr> ::= STRING_LIT | NUM_LIT | IDENT | ( <expr> ) | Z | rand
//    private Expr primary() {
//        return null;
//        // where is this {check token and
//    }
//}
