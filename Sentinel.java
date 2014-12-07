/* Sentinel.java
 *
 * The main class in the translator structure. This class opens the file, 
 * parses it into a Java AST and allows the Visitor class to parse through 
 * and translate out to C++
 *
 */

import java.io.PrintWriter;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.io.BufferedWriter;
import java.io.OutputStreamWriter;
import java.io.FileOutputStream;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.ArrayList;

import xtc.lang.JavaFiveParser;
import xtc.lang.JavaEntities;
import xtc.lang.JavaExternalAnalyzer;
import xtc.lang.JavaAstSimplifier;
import xtc.parser.ParseException;
import xtc.parser.Result;
import xtc.tree.GNode;
import xtc.tree.Node;
import xtc.type.Type;
import xtc.tree.Printer;
import xtc.tree.Visitor;
import xtc.util.SymbolTable;

public class Sentinel extends xtc.util.Tool {
    private CustomVisitor visitor;
    private HeaderFileWriter hWriter;
    private ArrayList<JavaClass> classes;
    private String fileName;

    public Sentinel() {
        // initialize Sentinel Class
        visitor = new CustomVisitor();
        classes = new ArrayList<JavaClass>();
    }

    public ArrayList<JavaClass> getClasses() {
        // return the list of classes in the file 
        return classes; 
    }

    public File locate(String name) throws IOException {
        // find the file and ensure that it is not too long to store 
        File file = super.locate(name);
        if (Integer.MAX_VALUE < file.length()) {
            throw new IllegalArgumentException(file + ": file too large");
        }
        return file;
    }

    public Node parse(Reader in, File file) throws IOException, ParseException {
        // Parse the file and return the AST node tree (root node)
        fileName = file.getName();
        fileName = fileName.substring(0, fileName.length() - 5);
        JavaFiveParser parser = new JavaFiveParser(in, file.toString(), (int)file.length());
        Result result = parser.pCompilationUnit(0);
        return (Node)parser.value(result);
    }

    public interface ICommand {
        public void run();
    }

    public String getName() {
        return "The master visitor class";
    }

    public void process(Node node) {
        //System.out.println(node.toString());
        visitor.dispatch(node);
        classes.addAll(visitor.getClasses());
        
        //iterate through all classes to set parents of all classes
        for (JavaClass c : classes)
            c.setParentFromList(classes);

        hWriter = new HeaderFileWriter(classes);

        //System.out.println(hWriter.headerOutput());

        BufferedWriter writer = null;
        try {
            writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("main" + ".h"), "utf-8"));
            writer.write(hWriter.headerOutput());
        } catch (IOException ioe) {
            System.out.println("IOException\n");
            ioe.printStackTrace() ;
        } finally {
            try {
                writer.close();
            } catch (Exception ex) {   }
        }

        SymbolTable table = new SymbolTable();

        // do some simplifications on the AST
        new JavaAstSimplifier().dispatch(node);
        
        // construct the symbol table
        SymbolTableBuilder svisitor = new SymbolTableBuilder(classes, runtime, table);
        svisitor.dispatch(node);
        svisitor.close();
        table.current().dump(runtime.console());
        runtime.console().flush();
        /*
         * XXX PLACE ALL CUSTOM CLASS DECLARATIONS HERE!
         * "classes" is an arraylist of JavaClass
         * All JavaClass have JavaMethod classes
         * Manipulate the classes variable!
        pvisitor = new PrinterVisitor(classes);
        pvisitor.dispatch(node);
        pvisitor.close();
         *
         */
    }

    public static void main(String[] args) {
        //Run the visitor with the java file as an argument 
        Sentinel s = new Sentinel();
        s.run(args);
    }
}
