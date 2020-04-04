package cloud.libert.tool.core;

import java.io.IOException;

public interface ILineReader {
    String next() throws IOException;
    String next(boolean trim) throws IOException;
    default boolean passEmptyLine() {
        return false;
    }
}
