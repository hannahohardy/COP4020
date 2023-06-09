/*Copyright 2023 by Beverly A Sanders
 * 
 * This code is provided for solely for use of students in COP4020 Programming Language Concepts at the 
 * University of Florida during the spring semester 2023 as part of the course project.  
 * 
 * No other use is authorized. 
 * 
 * This code may not be posted on a public web site either during or after the course.  
 */

package edu.ufl.cise.plcsp23.ast;
import edu.ufl.cise.plcsp23.IStringLitToken;
import edu.ufl.cise.plcsp23.IToken;
import edu.ufl.cise.plcsp23.PLCException;

public class StringLitExpr extends Expr {
	
	//Constructor
	public StringLitExpr(IToken firstToken) {
		super(firstToken);
	}

	//Visit method
	@Override
	public Object visit(ASTVisitor v, Object arg) throws PLCException {
		return v.visitStringLitExpr(this,arg);
	}

	//Gets the token and string value -- use later to isolate your spaces from tokens
	public String getValue() {
		return ((IStringLitToken)firstToken).getValue();
	}

	@Override
	public String toString() {
		return "StringLitExpr [firstToken=" + firstToken + "]";
	}
}
