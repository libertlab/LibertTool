package cloud.libert.tool.markdown;

import cloud.libert.tool.core.Formats;
import cloud.libert.tool.core.ILineReader;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MdQuoteBlock implements IMarkdownMeta {
    private static final String PREFIX = "> ";
    private String startLine;
    private List<String> otherLines;

    public static MdQuoteBlock parse(String line, ILineReader reader) throws IOException {
        if (line.startsWith(PREFIX)) {
            MdQuoteBlock meta = new MdQuoteBlock();
            meta.otherLines = new ArrayList<>();
            meta.startLine = line;
            for (; ; ) {
                line = reader.next(true);
                if (line == null || "".equals(line)) {
                    break;
                }
                meta.otherLines.add(line);
            }
            return meta;
        } else {
            return null;
        }
    }

    @Override
    public void writeTo(BufferedWriter bw) throws IOException {
        bw.write(Formats.NL);
        bw.write(PREFIX);
        bw.write(startLine+ Formats.NL);
        for(String line : otherLines) {
            bw.write(line + Formats.NL);
        }
        bw.write(Formats.NL);
    }

    @Override
    public int getType() {
        return TYPE_QUOTE_BLOCK;
    }
}
