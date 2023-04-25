package edu.ufl.cise.plcsp23.ast;

import edu.ufl.cise.plcsp23.*;
import java.util.HashMap;

public class TypeChecker  implements ASTVisitor
{


    public TypeChecker()
    {
    }


    public static class SymbolTable {

        HashMap<String,Declaration> entr = new HashMap<>();

        public  boolean insert(String name, Declaration declaration) {
            return (entr.putIfAbsent(name,declaration) == null);
        }

        public Declaration lookup(String name) {
            return entr.get(name);
        }
    }
    SymbolTable symbolTable = new SymbolTable();





    @Override
    public Object visitAssignmentStatement(AssignmentStatement statementAssign, Object arg) throws PLCException
    {
        if (statementAssign.getLv() == null) {
        throw new TypeCheckException("Error: undefined identifier " + statementAssign);
    }
        Type lType = (Type) statementAssign.getLv().visit(this, null);
        Type eType = (Type) statementAssign.getE().visit(this, null);

        if (!lType.equals(eType)) {
            throw new TypeCheckException("AssignmentStatement type mismatch");
        }

        return null;
    }

    @Override
    public Object visitBinaryExpr(BinaryExpr binaryExpr, Object arg) throws PLCException
    {
        Type leftType = (Type) binaryExpr.getLeft().visit(this, arg);
        Type rightType = (Type) binaryExpr.getRight().visit(this, arg);
        if (!leftType.equals(rightType)) {
            throw new TypeCheckException("Types of left and right hand side of binary expression do not match");
        }
        return leftType;

    }

    @Override
    public Object visitBlock(Block block, Object arg) throws PLCException
    {
        int declSize = block.decList.size();
        Declaration decStatement;
        for(int i=0; i< declSize; i++)
        {
            if(symbolTable.lookup(block.decList.get(i).nameDef.ident.getName()) != null)
            {
                throw new TypeCheckException("Parameter already defined");
            }
            if(block.decList.get(i).initializer instanceof BinaryExpr)
            {
                visitBinaryExpr((BinaryExpr) block.decList.get(i).initializer,null);
            }

            if(block.decList.get(i).initializer instanceof IdentExpr)
            {
                visitIdentExpr((IdentExpr) block.decList.get(i).initializer,null);
            }
            decStatement = new Declaration( block.decList.get(i).firstToken,block.decList.get(i).nameDef,block.decList.get(i).initializer);
            symbolTable.insert(block.decList.get(i).nameDef.ident.getName(),decStatement);
        }
        int stmSize = block.statementList.size();
        Statement stm;


        for (int i = 0; i < stmSize; i++) {
            stm = block.statementList.get(i);
            if (stm instanceof AssignmentStatement) {
                visitAssignmentStatement((AssignmentStatement) stm, null);
            }
        }

        return null;
    }

    @Override
    public Object visitConditionalExpr(ConditionalExpr conditionalExpr, Object arg) throws PLCException {
        return null;
    }

    @Override
    public Object visitDeclaration(Declaration declaration, Object arg) throws PLCException {
        Type type = declaration.getNameDef().getType();
        Ident ident = declaration.getNameDef().getIdent();
        Expr expr = declaration.getInitializer();

        if (expr != null) {
            Type exprType = (Type) expr.visit(this, arg);

           // if (type == Type.IMAGE && exprType == Type.STRING) {

           // }
         if (type != exprType) {
                throw new PLCException("Type mismatch in declaration");
            }
        }

        if (symbolTable.lookup(ident.getName()) != null) {
            throw new PLCException("Variable already declared");
        }

        symbolTable.insert(ident.getName(), declaration);
        return null;
    }

    @Override
    public Object visitDimension(Dimension dimension, Object arg) throws PLCException {
        return null;
    }

    @Override
    public Object visitExpandedPixelExpr(ExpandedPixelExpr expandedPixelExpr, Object arg) throws PLCException {
        return null;
    }

    @Override
    public Object visitIdent(Ident ident, Object arg) throws PLCException
    {
        String name = ident.getName();
        Declaration dec = symbolTable.lookup(name);
        if(dec == null)
        {
            throw  new SyntaxException("Error Undidentified ident " + name);
        }
        Type type = dec.nameDef.getType();
        return type;
    }

    @Override
    public Object visitIdentExpr(IdentExpr identExpr, Object arg) throws PLCException
    {
        Declaration dec = symbolTable.lookup(identExpr.getName());
        if (dec == null) {
            throw new TypeCheckException("Identifier " + identExpr.getName() + " not defined");
        }
        return dec.initializer;

    }

    @Override
    public Object visitLValue(LValue lValue, Object arg) throws PLCException {
        return null;
    }

    @Override
    public Object visitNameDef(NameDef nameDef, Object arg) throws PLCException {
        return null;
    }

    @Override
    public Object visitNumLitExpr(NumLitExpr numLitExpr, Object arg) throws PLCException
    {
        return Type.INT;

    }

    @Override
    public Object visitPixelFuncExpr(PixelFuncExpr pixelFuncExpr, Object arg) throws PLCException {
        return null;
    }

    @Override
    public Object visitPixelSelector(PixelSelector pixelSelector, Object arg) throws PLCException {
        return null;
    }

    @Override
    public Object visitPredeclaredVarExpr(PredeclaredVarExpr predeclaredVarExpr, Object arg) throws PLCException {
        return null;
    }

    @Override
    public Object visitProgram(Program program, Object arg) throws PLCException {
        Declaration decParam;
        Declaration dec = symbolTable.lookup(program.ident.getName());
        NameDef nameDf = new NameDef(program.firstToken ,program.type,null,program.getIdent());
        if(dec != null)
        {
            throw new SyntaxException("Program name already defined");
        }
        dec = new Declaration(program.firstToken,nameDf,null);
        symbolTable.insert(program.ident.getName(), null);
        int plLength =program.paramList.size();
        for(int i=0; i<plLength; i++)
        {
            Declaration  decLookup = symbolTable.lookup(program.paramList.get(i).ident.getName());
            if(decLookup != null)
            {
                throw new TypeCheckException("Parameter already defined");
            }
            if(program.paramList.get(i).type != Type.INT &&program.paramList.get(i).type != Type.STRING && program.paramList.get(i).type != Type.IMAGE&& program.paramList.get(i).type != Type.PIXEL )
            {
                throw new TypeCheckException("Incorrect type for parameter");
            }
            decParam = new Declaration( program.paramList.get(i).firstToken,program.paramList.get(i),null);
            symbolTable.insert(program.paramList.get(i).ident.getName(),decParam);
        }
        if(program.block.decList.size() != 0 || program.block.statementList.size() !=0)
        {
            visitBlock(program.block,null);
        }
        return symbolTable;
    }

    @Override
    public Object visitRandomExpr(RandomExpr randomExpr, Object arg) throws PLCException {
        return null;
    }

    @Override
    public Object visitReturnStatement(ReturnStatement returnStatement, Object arg) throws PLCException {
      return null;
    }

    @Override
    public Object visitStringLitExpr(StringLitExpr stringLitExpr, Object arg) throws PLCException
    {
        return Type.STRING;
    }

    @Override
    public Object visitUnaryExpr(UnaryExpr unaryExpr, Object arg) throws PLCException {
        return null;
    }

    @Override
    public Object visitUnaryExprPostFix(UnaryExprPostfix unaryExprPostfix, Object arg) throws PLCException {
        return null;
    }

    @Override
    public Object visitWhileStatement(WhileStatement whileStatement, Object arg) throws PLCException {
        return null;
    }

    @Override
    public Object visitWriteStatement(WriteStatement statementWrite, Object arg) throws PLCException {
        return null;
    }

    @Override
    public Object visitZExpr(ZExpr zExpr, Object arg) throws PLCException {
        return null;
    }
}
