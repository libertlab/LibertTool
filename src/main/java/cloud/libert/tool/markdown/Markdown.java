package cloud.libert.tool.markdown;

import cloud.libert.tool.LibertToolException;
import cloud.libert.tool.core.ILineReader;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class Markdown implements ILineReader {
    public MdHeading title;
    public BufferedReader bufferedReader;
    private String path;
    public List<IMdMeta> list;

    private boolean passEmptyLine = false;
    private boolean currentEmpty = false;
    private String lastLine = "";

    public Markdown(File file) throws LibertToolException {
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

    public static void main(String[] args) throws IOException {
        File f = new File("D:\\test\\LibertTool\\test.md");
        try {
            Markdown mp = new Markdown(f);
            System.out.println("end");
            mp.writeTo("D:/test/LibertTool/test_out.md");
            System.out.println(mp.toHtml());
        } catch (LibertToolException e) {
            e.printStackTrace();
        }
    }

    public void writeTo(String path) throws IOException {
        File f = new File(path);
        if(!f.exists()) {
            f.createNewFile();
        }
        try(BufferedWriter bw = new BufferedWriter(new FileWriter(f))) {
            if(title!=null) {
                title.writeTo(bw);
            }
            for(IMdMeta meta : list) {
                meta.writeTo(bw);
            }
        }
    }

    public String toHtml() {
        StringBuilder sb = new StringBuilder(1024);
        if(title!=null) {
            sb.append(title.toHtml());
        }
        for(IMdMeta meta : list) {
            sb.append(meta.toHtml());
        }
        return sb.toString();
    }

    private void parse() throws IOException {
        title = MdHeading.parse(next());
        list = new ArrayList<>();
        IMdMeta meta = null;
        String line = next();
        while (line != null) {
            if(line.startsWith("> ")) {
                passEmptyLine = false;
                meta = MdQuoteBlock.parse(line, this);
                line = lastLine;
                if(meta!=null) {
                    list.add(meta);
                }
                continue;
            } else if(line.startsWith("| ")) {
                meta = MdTable.parse(line, this);
                line = lastLine;
                if(meta!=null) {
                    list.add(meta);
                }
                continue;
            } else if(line.startsWith("```")) {
                passEmptyLine = false;
                meta = MdCodeBlock.parse(line, this);
            } else if((meta = MdHeading.parse(line)) != null) {

            } else {
                meta = new MdParagraph(line);
            }
            if(meta!=null) {
                list.add(meta);
            }
            line = next();
        }
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
            if(line.length()==0) {
                passEmptyLine = true;
                currentEmpty = true;
            } else {
                passEmptyLine = currentEmpty;
                currentEmpty = false;
            }
        } while (line.length() == 0);
        lastLine = line;
        return line;
    }

    public String next(boolean trim) throws IOException {
        String line = null;
        String trimed = null;
        do {
            line = bufferedReader.readLine();
            if(line == null) {
                break;
            }
            trimed = line.trim();
            if(line.length()==0) {
                passEmptyLine = true;
                currentEmpty = true;
            } else {
                passEmptyLine = currentEmpty;
                currentEmpty = false;
            }
        } while(trimed.length() == 0);
        lastLine = trim ? trimed : line;
        return lastLine;
    }

    @Override
    public boolean passEmptyLine() {
        return passEmptyLine;
    }

}
