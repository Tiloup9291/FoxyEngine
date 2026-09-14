package engine.level;

public final class LevelParseException
extends Exception {
    private final String file;
    private final int line;

    public LevelParseException(String file, int line, String message) {
        super(file + ":" + line + ": " + message);
        this.file = file;
        this.line = line;
    }

    public String file() {
        return this.file;
    }

    public int line() {
        return this.line;
    }
}
