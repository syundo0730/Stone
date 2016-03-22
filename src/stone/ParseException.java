package stone;

import java.io.IOException;

public class ParseException extends Exception{
    public ParseException(Token token) {
        this("", token);
    }
    public ParseException(String message, Token token) {
        super("syntax error around " + location(token) + ". " + message);
    }
    private static String location(Token token) {
        if (token == Token.EOF) {
            return "the last line";
        } else {
            return "\"" + token.getText() + "\" at line " + token.getLineNumber();
        }
    }
    public ParseException(IOException e) {
        super(e);
    }
    public ParseException(String message) {
        super(message);
    }
}
