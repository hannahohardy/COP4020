package edu.ufl.cise.plcsp23;

import java.util.List;
import java.util.ArrayList;

import edu.ufl.cise.plcsp23.ast.*;

public class CompilerComponentFactory {
    public static IScanner makeScanner(String input) {
        return new Scanner(input);
    }

    public static IParser makeParser(String input) throws LexicalException {
        return new Parser(input);
    }

    public static ASTVisitor makeTypeChecker() {
        return new TypeChecker();
    }

public static ASTVisitor makeCodeGenerator(String packageName){
        return new CodeGenerator(packageName);
}

}