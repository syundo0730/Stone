package stone;

import java.io.IOException;
import java.io.LineNumberReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Lexer {
    public static String pat1 = "(//.*)";
    public static String pat2 = "([0-9]+)";
    public static String pat3 = "(\"(\\\\\"|\\\\\\\\|\\\\n|[^\"])*\")";
    public static String pat4 = "[A-Z_a-z]|[A-Z_a-z0-9]*|==|<=|>=|&&|\\|\\||\\p{Punct}";
    public static String regexPat = "\\s*(" + pat1 + "|" + pat2 + "|" + pat3 + "|" + pat4 + ")?";
    private Pattern pattern = Pattern.compile(regexPat);
    private ArrayList<Token> queue = new ArrayList<Token>();
    private boolean hasMore;
    private LineNumberReader reader;

    public Lexer(Reader reader) {
        this.hasMore = true;
        this.reader = new LineNumberReader(reader);
    }
    public Token read() throws ParseException {
        if (fillQueue(0)) {
            return queue.remove(0);
        } else {
            return Token.EOF;
        }
    }
    public Token peek(int i) throws ParseException {
        if (fillQueue(i)) {
            return this.queue.remove(i);
        } else {
            return Token.EOF;
        }
    }
    private boolean fillQueue(int i) throws ParseException {
        while (i >= this.queue.size()) {
            if (this.hasMore) {
                readLine();
            } else {
                return false;
            }
        }
        return true;
    }
    protected void readLine() throws ParseException {
        String line;
        try {
            line = this.reader.readLine();
        } catch (IOException e) {
            throw new ParseException(e);
        }
        if (line == null) {
            this.hasMore = false;
            return;
        }
        int lineNo = this.reader.getLineNumber();
        Matcher matcher = this.pattern.matcher(line);
        matcher.useTransparentBounds(true).useAnchoringBounds(false);
        int pos = 0;
        int endPos = line.length();
        while (pos < endPos) {
            matcher.region(pos, endPos);
            if (matcher.lookingAt()) {
                addToken(lineNo, matcher);
                pos = matcher.end();
            } else {
                throw new ParseException("bad token at line" + lineNo);
            }
        }
        this.queue.add(new IdToken(lineNo, Token.EOL));
    }
    protected void addToken(int lineNo, Matcher matcher) {
        String m = matcher.group(1);
        if (m == null) { // if no a space
            if (matcher.group(2) == null) { // if not a comment
                Token token;
                if (matcher.group(3) != null) {
                    token = new NumToken(lineNo, Integer.parseInt(m));
                } else if (matcher.group(4) != null) {
                    token = new StrToken(lineNo, toStringLiteral(m));
                } else {
                    token = new IdToken(lineNo, m);
                }
                this.queue.add(token);
            }
        }
    }
    protected String toStringLiteral(String str) {
        StringBuilder stringBuilder = new StringBuilder();
        int len = str.length();
        for (int i = 1; i < len; i++) {
            char c = str.charAt(i);
            if (c == '\\' && i + 1 < len) {
                int c2 = str.charAt(i + 1);
                if (c2 == '"' || c2 == '\\') {
                    c = str.charAt(++i);
                } else if (c2 == 'n') {
                    ++i;
                    c = '\n';
                }
            }
            stringBuilder.append(c);
        }
        return stringBuilder.toString();
    }
    protected static class NumToken extends Token {
        private int value;

        protected NumToken(int line, int value) {
            super(line);
            this.value = value;
        }
        public boolean isNumber() { return true; }
        public String getText() { return Integer.toString(this.value); }
        public int getNumber() { return this.value; }
    }
    protected static class IdToken extends Token {
        private String text;
        protected IdToken(int line, String id) {
            super(line);
            this.text = id;
        }
        public boolean isIdentifier() { return true; }
        public String getText() { return this.text; }
    }
    protected static class StrToken extends Token {
        private String literal;
        StrToken(int line, String str) {
            super(line);
            this.literal = str;
        }
        public boolean isString() { return true; }
        public String getText() { return this.literal; }
    }
}
