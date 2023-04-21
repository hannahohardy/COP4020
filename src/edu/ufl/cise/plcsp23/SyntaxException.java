package edu.ufl.cise.plcsp23;

//The exception to be thrown for errors discovered during parsing
@SuppressWarnings("serial")
public class SyntaxException extends PLCException {

	public SyntaxException(String message) {
		super(message);
	}

}
