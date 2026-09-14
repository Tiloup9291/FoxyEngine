package engine.assets;

import java.nio.file.Path;

public class ObjParseException
extends Exception {
    private final Path file;
    private final int line;

    public ObjParseException(Path file, int line, String message) {
        super(ObjParseException.format(file, line, message));
        this.file = file;
        this.line = line;
    }

    public ObjParseException(Path file, int line, String message, Throwable cause) {
        super(ObjParseException.format(file, line, message), cause);
        this.file = file;
        this.line = line;
    }

    private static String format(Path file, int line, String message) {
        return (Comparable)(file != null ? file : "<memory>") + ":" + line + ": " + message;
    }

    public Path file() {
        return this.file;
    }

    public int line() {
        return this.line;
    }
}
