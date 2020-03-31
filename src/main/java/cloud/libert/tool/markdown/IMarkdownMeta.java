package cloud.libert.tool.markdown;

import java.io.BufferedWriter;
import java.io.IOException;

public interface IMarkdownMeta {
    public static final int TYPE_HEADING = 1;
    public static final int TYPE_QUOTE_BLOCK = 7;
    public void writeTo(BufferedWriter bw) throws IOException;
    public int getType();
}
