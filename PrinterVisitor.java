/* PrinterVisitor.java
 * 
 * This class goes through the final C++ AST and prints out C++ code.
 *
 */

import java.io.PrintWriter;
import java.io.BufferedReader;
import java.io.FileReader;
import java.util.Iterator;
import java.util.Hashtable;
import java.util.Map;
import java.util.ArrayList;
import java.io.IOException;
import java.util.Hashtable;
import java.util.Enumeration;
import xtc.lang.JavaFiveParser;
import xtc.parser.ParseException;
import xtc.parser.Result;
import xtc.tree.GNode;
import xtc.tree.Node;
import xtc.type.Type;
import xtc.tree.Printer;
import xtc.tree.Visitor;


public class PrinterVisitor extends xtc.tree.Visitor {
    public MethodsWriter w;
    private ArrayList<JavaClass> classes;
    private JavaClass current_class;
    private JavaMethod current_method;
    private ArrayList<JavaMethod> current_class_methods;
    private ArrayList<JavaGlobalVariable> current_class_global_variables;
    private String methodCalled = "";
    private boolean isMainMethod = false;
    private boolean hasConstructor = false;
    private boolean isToString = false;
    private String current_object = "";
    private String OUTPUT_FILE_NAME = "main.cc";

    public PrinterVisitor(ArrayList<JavaClass> c) {
        classes = c;
        current_class = null;
        current_class_methods = null;
       
        w = new MethodsWriter(OUTPUT_FILE_NAME);
        current_class_global_variables = null;
    }
    
    public void visitConstructorDeclaration(GNode n){
        //DOES NOT HANDLE THE CASE WHERE THERE ARE MULTIPLE PARAMETERS! STILL NEEDS WORK!
        hasConstructor = true;
        w.print("__" + current_class.name + "::__" + current_class.name +"(" 
                + current_class.getCparam_string() + "):__vptr(&__vtable) ");
        for (Object o : n) {
            if (o instanceof Node){
                Node temp = (Node) o;
                if (temp != null){
                    if (temp.hasName("Block")){
                        dispatch((Node) o);
                    }
                }
            }
        }
        w.println("{}");
    }

    public void visitClassDeclaration(GNode n) {
        hasConstructor = false;
        current_class = findClass(n.getString(1));
        writeConstructor(current_class.name);
        visit(n);
        
        current_class = null;
        current_class_global_variables = null;
        current_method = null;
        current_class_methods = null;
    }   

    public void visitMethodDeclaration(GNode n) {
        if(!hasConstructor){
            w.println("__" + current_class.name + "::__" + current_class.name +"(" 
                      + current_class.getCparam_string() + "):__vptr(&__vtable){}");
            hasConstructor = true;
        }
        if(current_class == null) {
            current_class_methods = null;
            current_class_global_variables = null;
        } else {
            current_class_methods = current_class.methods;
            current_class_global_variables = current_class.globalVars;
        }
        if (current_class_methods != null){
            current_method = findMethod(n.getString(3));
            if (current_method.name.equals("main") && current_method.modifier.equals("public")&& current_method.type.equals("void")){
                isMainMethod = true;
                w.println("}}");
                w.writeLastFile();
                w.println("int main (){");
                visit(n);
                w.println("}");
                current_method = null;
            } else {
                w.print(current_method.type + " __" + current_class.name + "::" 
                                   + current_method.name + "(" + current_class.name + " __this");
                Iterator<Map.Entry<String, String>> it = current_method.params.entrySet().iterator();
                while (it.hasNext()) {
                    Map.Entry<String, String> entry = it.next();
                    w.print(", " + entry.getValue() + " " + entry.getKey());
                }
                w.println("){");

                visit(n);
                w.println("}");
                current_method = null;
            }
        } else {
            isMainMethod = true;
            w.println("}}");
            w.print("int main (){");
            visit(n);
            w.println("}");
            current_method = null;
        }
    }

    public void visitThisExpression(GNode n){
        w.print("__this");
        visit(n);
    }

    public void visitArguments(GNode n) {
        w.print(methodCalled);

            if (n.isEmpty()) {
                w.print("(" + current_object + ")");
                if(isToString)
                    w.print("->data");
                visit(n);
            } else {
                w.print("(" + current_object + ",");
                visit2(n, ",");
                w.print(")");
            }
    }

    public void visitNewClassExpression(GNode n) {
        w.print("new " + "__" + n.getGeneric(2).getString(0));
        visit(n);
    }

    public void visitFieldDeclaration(GNode n){
        if(current_class!= null && current_method!=null){
            w.print(convertString(n.getGeneric(1).getGeneric(0).getString(0)) + " ");
            visit(n);
            w.println(";");
        }
    }

    public void visitDeclarator(GNode n) {
        if(n.getGeneric(2) == null)
            w.print(convertString(n.getString(0)));
        else{
            w.print(convertString(n.getString(0)) + " = ");
        }
        visit(n);
    }

    public void visitCharacterLiteral(GNode n){
        w.print(convertString(n.getString(0)));
        visit(n);
    }

    public void visitNullLiteral(GNode n){
        w.print(convertString(n.getString(0)));
        visit(n);
    }

    public void visitIntegerLiteral(GNode n){
        w.print(convertString(n.getString(0)));
        visit(n);
    }

    public void visitStringLiteral(GNode n) {
        w.print("new __String(" + (n.getString(0)) + ")");
        visit(n); 
    }

    public void visitFloatingPointLiteral(GNode n){
        w.print(convertString(n.getString(0)));
        visit(n);
    }

    public void visitPrimaryIdentifier(GNode n){
        //w.print(n.getString(0));
        boolean thisP = false;
        if (current_class_global_variables != null)
            for (JavaGlobalVariable g : current_class_global_variables) {
                if (g.name.equals(n.getString(0))) {
                    thisP = true; 
                } 
            }

        if (thisP) {
            w.print("__this->"+n.getString(0));
        } else {
            w.print(n.getString(0));
        }
        visit(n);
    }

    public void visitReturnStatement(GNode n){
        w.print("return ");
        visit(n);
        w.println(";");
    }

    public void visitExpression(GNode n) {
        String s = " " + convertString(n.getString(1)) + " ";
        visit2(n,s);
        //w.println(";");
    }

    public void visitExpressionStatement(GNode n){
        visit(n);
        w.println(";");
    }


    public void visitFormalParameters(GNode n) {
        int i = 0;
        boolean mainM = false;

        for (Object o : n) {
            i++;
            Node temp = (Node) o;
            if (temp.size() <= 5){
                if(isMainMethod && temp.getNode(1).getNode(0).getString(0).equals("String") && temp.getString(3).equals("args")){
                    w.println("using namespace java::lang;");
                    w.println("using namespace __rt;");
                    w.println("using namespace std;");
                    mainM = true;
                }
            }
        }
        visit(n);
    }

    public void visitCallExpression(GNode n) {
        if(n.getNode(0)!=null){
            if (n.getNode(0).hasName("SelectionExpression") && n.getNode(0).getNode(0).getString(0).equals("System")) {
                w.print("cout << ");
                //System.out.println(n.getNode(3).getNode(0).getName());
                if(n.getNode(3).getNode(0).hasName("StringLiteral"))
                    w.print(n.getNode(3).getNode(0).getString(0));
                else
                    visit(n.getNode(3));
                if (n.getString(2).equals("println")) {
                    w.print(" << std::endl");
                }
            } else {
                int i = 1;
                if(!n.getNode(0).hasName("SelectionExpression")){
                    current_object = n.getNode(0).getString(0);
                }
                methodCalled = "->__vptr->"+convertString( n.getString(2) );
                isToString = (n.getString(2).equals("toString"));
                visit(n);
                /*
                for (Object o : n) {
                    if (o instanceof Node){
                        Node temp = (Node) o;
                        if(temp != null){
                            if(!temp.hasName("PrimaryIdentifier")){
                                dispatch((Node) o);
                            }
                        }
                    }
                }*/

                methodCalled = "";
                //w.print(n.getString(2));
            }
        }
    }

    public void visitSelectionExpression(GNode n) {
        if (n.size() == 2) {
            current_object = n.getString(1);
            dispatch(n.getNode(0)); 
            w.print("->");

        } else {
            dispatch(n.getNode(0));
            w.print("::");
        }

        Object o = n.get(1);
        if ((o) instanceof String) 
            w.print((String) o);
        else
            dispatch((Node) o);
    }
    
    public void visitAdditiveExpression(GNode n){
        String s = " " + n.getString(1) + " ";
        visit2(n,s);
    }

    public void visitMultiplicativeExpression(GNode n){
        String s = " " + n.getString(1) + " ";
        visit2(n, s);
    }

    public void visitLogicalAndExpression(GNode n){
        int i = 0;
        for (Object o : n) {
            i++;
            if (o instanceof Node) dispatch((Node) o);
            if (i < n.size()) w.print(" && ");
        }
    }

    public void visitLogicalOrExpression(GNode n){
        int i = 0;
        for (Object o : n) {
            i++;
            if (o instanceof Node) dispatch((Node) o);
            if (i < n.size()) w.print(" || ");
        }
    }

    public void visitLogicalNegationExpression(GNode n){
        w.print("!");
        visit(n);
    }

    public void visitRelationalExpression(GNode n){
        String s = " " + n.getString(1) + " ";
        visit2(n, s);
    }

    public void visitEqualityExpression(GNode n){
        visit2(n, " == ");
    }

    public void visitConditionalStatement(GNode n){
        w.print("if(");
        int i = 0;

        for (Object o : n) {
            Node temp = (Node) o;
            if (temp != null){
                if (temp.hasName("Block")){
                    if(i == 0)
                        w.println("){");
                    else
                        w.println("else{");
                    i++;
                }
            }
            if (o instanceof Node) dispatch((Node) o);
            if (temp != null){
                if (temp.hasName("Block"))
                    w.println("}");
            }
        }
    }


    // What if for statement does not instantiate the variable inside the loop? (NEED TO ADD)
    public void visitBasicForControl(GNode n){
        int i = 0;
        for (Object o : n) {
            i++;
            Node temp = (Node) o;
            if (o instanceof Node) dispatch((Node) o);
            if(temp!=null){
                if (temp.hasName("Type")) w.print(temp.getGeneric(0).getString(0) + " ");
                if ((temp.hasName("RelationalExpression") || temp.hasName("Declarators") 
                    || temp.hasName("ExpressionList")) && i<n.size()) w.print(" ; ");
            }
        }
    }
    public void visitForStatement(GNode n){
        int i = 0;
        w.print("for(");
        for (Object o : n) {
            i++;
            Node temp = (Node) o;
            if (temp != null){
                if (temp.hasName("Block")){
                    w.println("){");
                }
            }
            if (o instanceof Node) dispatch((Node) o);
            if (temp != null){
                if (temp.hasName("Block"))
                    w.println("}");
            }
        }
    }
    
    public void visitPostfixExpression(GNode n){
        visit(n);
        w.print(convertString(n.getString(1)));        
    }

    public void close(){
        w.close();
        return;
    }

    public void pln(GNode n) {
        w.println(n.toString());
    }

    public JavaClass findClass(String n){
        for(int i=0; i<classes.size(); i++){
            if(classes.get(i).name.equals(n))
                return classes.get(i);
        }
        return null;
    }

    public JavaMethod findMethod(String n){
        for(int i=0; i < current_class_methods.size(); i++){
            if(current_class_methods.get(i).name.equals(n))
                return current_class_methods.get(i);
        }
        return null;
    }

    public void visit2(GNode n, String s){
        int i = 1;
        for (Object o : n) {
            i++;
            if (o instanceof Node) dispatch((Node) o);
            if (i < n.size()) w.print(s);
        }
    }
    public void visit(Node n) {
        for (Object o : n) {
            // visit the nearest instance of a node 
            if (o instanceof Node) dispatch((Node) o);
        }
    }

    public void writeConstructor(String n) {
        w.println("__" + n + "_VT " + "__" + n + "::__vtable;");
        w.println("Class __" + n + "::__class() {");
        w.println("   static Class k = " + "new __Class(__rt::literal(\"java.lang." + 
                  n + ".A\"), (Class)__rt::null());");
        w.println("   return k;");
        w.println("}");
    }

    public String convertString(String str) {
        if (str.equals("String")) {
            return ("__String*");
        } else if (str.equals("long")) {
              return ("signed int65_t");
          } else if (str.equals("int")) {
              return ("int32_t");
          } else if (str.equals("short")) {
              return ("signed int16_t");
          } else if (str.equals("boolean")) {
              return ("bool");
          } else if (str.equals("final")) {
              return ("const");
          } else {
              return str; 
          }
      }
}
