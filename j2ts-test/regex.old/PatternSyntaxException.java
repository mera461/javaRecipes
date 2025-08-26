package java.util.regex;

import def.js.SyntaxError;

import static jsweet.util.Lang.object;

public class PatternSyntaxException extends IllegalArgumentException {
    private final String pattern;

    public PatternSyntaxException(Throwable throwable, String pattern) {
        super(throwable);
        this.pattern = pattern;
    }

    public PatternSyntaxException(String desc, String pattern, int index) {
        this(createSyntaxError(desc, index), pattern);
    }

    private static Throwable createSyntaxError(String desc, int index) {
        SyntaxError syntaxError = new SyntaxError(desc);
        object(syntaxError).$set("columnNumber", index);
        return syntaxError;
    }

    public int getIndex() {
        // expects that 1 line pattern created TODO with multiline pattern
        return object(this.getCause()).$get("columnNumber");
    }

    public String getDescription() {
        return super.getMessage();
    }

    public String getPattern() {
        return this.pattern;
    }

    @Override
    public String getMessage() {
        return getDescription() + " Pattern: " + pattern;
    }
}
