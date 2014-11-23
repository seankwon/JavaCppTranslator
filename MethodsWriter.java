import java.lang.StringBuilder;
import java.io.*;
public class MethodsWriter {
    private final String REF_FILE_ONE = "referenceFiles/objectMethods.ref";
    private final String REF_FILE_TWO = "referenceFiles/arrayMethods.ref";
    private final String OUTPUT_FILE = "main.cc";
    private String output;
    private PrintWriter writer;

    public MethodsWriter() {}
    public MethodsWriter(String o) {
        output = o;
        try {
            writer = new PrintWriter(OUTPUT_FILE);
        } catch(Exception e) {}
        writer.println(bigInput(REF_FILE_ONE));
    }

    public void print(String s) {writer.print(s);}
    public void println(String s) {writer.println(s);}
    public void close() {writer.close(); return;}

    public String bigInput(String input) {
        //add this to header final last
        StringBuilder output = new StringBuilder();
        String line = null;
        try {
            BufferedReader inFile = new BufferedReader(new FileReader(input));
            while ((line = inFile.readLine()) != null) {
                output.append(line + "\n");
            }
        } catch (IOException ioe) {
            ioe.printStackTrace() ;
        }

        return output.toString();
    }

    public void writeLastFile() {
        writer.println(bigInput(REF_FILE_TWO));
    }

}
