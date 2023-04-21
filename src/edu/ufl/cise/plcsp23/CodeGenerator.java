package edu.ufl.cise.plcsp23;

import edu.ufl.cise.plcsp23.ast.*;

import java.util.List;

public class CodeGenerator implements ASTVisitor {
    private String packageName;
    public CodeGenerator(String packageName) {
        this.packageName = packageName;

    }
    @Override
    public Object visitProgram(Program program, Object arg) throws PLCException {
        StringBuilder code = new StringBuilder();

        // Add import statements
        code.append("import java.io.*;\n");
        code.append("import java.util.*;\n");
        code.append("import edu.ufl.cise.plcsp23.ConsoleIO;\n\n");

        // Add class definition
        code.append("public class ").append(program.getIdent().getName()).append(" {\n\n");

        // Add method signature
        String returnType = program.getType().toString(); // Modify this line to get the Java type corresponding to Type
        code.append("  public static ").append(returnType).append(" apply(");

        // Add method parameters
        List<NameDef> paramList = program.getParamList();
        for (NameDef nameDef : paramList) {
            // Get the Java type corresponding to Type
            String javaType = nameDef.getType().toString();
            String paramName = nameDef.getIdent().getName();

            code.append(javaType).append(" ").append(paramName).append(", ");
        }
        // Remove trailing comma and space
        if (!((List<?>) paramList).isEmpty()) {
            code.setLength(code.length() - 2);
        }
        code.append(") {\n");

        // Add method body
        code.append(program.getBlock().visit(this, arg));

        // Close method and class
        code.append("  }\n");
        code.append("}\n");

        return code.toString();
    }



    @Override
    public Object visitAssignmentStatement(AssignmentStatement statementAssign, Object arg) throws PLCException {
     return null;
    }

    @Override
    public Object visitBinaryExpr(BinaryExpr binaryExpr, Object arg) throws PLCException {
        return null;
    }

    @Override
    public Object visitBlock(Block block, Object arg) throws PLCException {

        return null;
    }

    @Override
    public Object visitConditionalExpr(ConditionalExpr conditionalExpr, Object arg) throws PLCException {
        return null;
    }

    @Override
    public Object visitDeclaration(Declaration declaration, Object arg) throws PLCException {
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
    public Object visitIdent(Ident ident, Object arg) throws PLCException {
        return null;
    }

    @Override
    public Object visitIdentExpr(IdentExpr identExpr, Object arg) throws PLCException {
        return null;
    }

    @Override
    public Object visitLValue(LValue lValue, Object arg) throws PLCException {
        return null;
    }

    @Override
    public Object visitNameDef(NameDef nameDef, Object arg) throws PLCException {
        Type type = nameDef.getType();
        String typeName = type.toString();
        Ident ident = nameDef.getIdent();
        String identName = ident.getName();

        return typeName + " " + identName;  }

    @Override
    public Object visitNumLitExpr(NumLitExpr numLitExpr, Object arg) throws PLCException {
        return null;
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
    public Object visitRandomExpr(RandomExpr randomExpr, Object arg) throws PLCException {
        return null;
    }

    @Override
    public Object visitReturnStatement(ReturnStatement returnStatement, Object arg) throws PLCException {
        return null;
    }

    @Override
    public Object visitStringLitExpr(StringLitExpr stringLitExpr, Object arg) throws PLCException {
        return null;
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
