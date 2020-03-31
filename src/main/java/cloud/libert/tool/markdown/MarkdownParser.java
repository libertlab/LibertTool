package cloud.libert.tool.markdown;

import cloud.libert.tool.LibertToolException;
import cloud.libert.tool.core.ILineReader;

import java.io.*;

public class MarkdownParser implements ILineReader {
    public MdHeading title;
    public BufferedReader bufferedReader;
    private String path;

    public MarkdownParser(File file) throws LibertToolException {
        path = file.getAbsolutePath();
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            bufferedReader = br;
            parse();
        } catch (FileNotFoundException e) {
            throw new LibertToolException("File not found: " + path);
        } catch (IOException e) {
            throw new LibertToolException(e);
        }
    }

    private void parse() throws IOException {
//        title = MdHeading.parse(next(), this);
//        String line;
//        do {
//            line = next();
//
//        } while (line != null);
    }

    @Override
    public String next() throws IOException {
        String line = null;
        do {
            line = bufferedReader.readLine();
            if (line == null) {
                break;
            }
            line = line.trim();
        } while (line.length() == 0);
        return line;
    }

    @Override
    public String next(boolean trim) throws IOException {
        String line = bufferedReader.readLine();
        if (line == null) {
            return null;
        }
        return trim ? line.trim() : line;
    }
}
