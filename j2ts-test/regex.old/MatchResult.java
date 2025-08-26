package java.util.regex;

public interface MatchResult {
    int start();

    int start(int i);

    int end();

    int end(int i);

    String group();

    String group(int i);

    int groupCount();
}
