/* CustomVisitor.java
 * 
 * This class is used to go through the AST by visiting each 
 *
 */

import java.util.ArrayList;
import java.util.Hashtable;
import xtc.lang.JavaFiveParser;
import xtc.parser.ParseException;
import xtc.parser.Result;
import xtc.tree.GNode;
import xtc.tree.Node;
import xtc.type.Type;
import xtc.tree.Printer;
import xtc.tree.Visitor;

public class CustomVisitor extends xtc.tree.Visitor {
    private ArrayList<JavaClass> classes;
    private ArrayList<JavaMethod> methods;
    private Hashtable<String, String> paramsList;
    private Hashtable<String, String> cparamsList;
    private ArrayList<JavaGlobalVariable> globalVars;
    private boolean isClassScope = false;
    private boolean isConstructor = false;
    private String class_name;

    public CustomVisitor() {
        classes = new ArrayList<JavaClass>();
        methods = new ArrayList<JavaMethod>();
        paramsList = new Hashtable<String, String>();
        cparamsList = new Hashtable<String, String>();
        globalVars = new ArrayList<JavaGlobalVariable>();
    }

    public ArrayList<JavaClass> getClasses() {
        return classes;    
    }

    public ArrayList<JavaGlobalVariable> getGlobalVars() {
        return globalVars;
    }

    public void visitClassDeclaration(GNode n) {
        isClassScope = true;
        //Create new class
        JavaClass c = new JavaClass();
        // create arraylist to create deepcopying, in order for methods to not
        // be garbage collected
        ArrayList<JavaMethod> tempMethods = new ArrayList<JavaMethod>();
        ArrayList<JavaGlobalVariable> tempGlobalVars = new ArrayList<JavaGlobalVariable>();
        Hashtable<String, String> p = new Hashtable<String, String>();

        // set class name
        class_name = n.getString(1);
        c.name = n.getString(1);
        // set class Gnode 
        c.implementation = n;
        // get parent name of class
        if (n.getGeneric(3) != null)
            c.parentName = (n.getGeneric(3).getGeneric(0).getGeneric(0).getString(0));
        visit(n);
        p.putAll(cparamsList);
        c.Cparam = p;
        // set class name of all methods
        for (JavaMethod m : methods)
            m.className = (c.name);

        // perform deep copying
        tempMethods.addAll(methods);
        tempGlobalVars.addAll(globalVars);
        // set methods of class
        c.methods = (tempMethods);
        c.globalVars = (tempGlobalVars);
        // add class to list of classes
        classes.add(c);
        // clear methods in order add methods to another class
        methods.clear();
        cparamsList.clear();
        globalVars.clear();

    }
    public void visitConstructorDeclaration(GNode n){
        isConstructor = true;
        visit(n);
        isConstructor = false;
    }

    public void pln(GNode n) {
        System.out.println(n.toString());
    }

    @SuppressWarnings("unchecked")
    public void visitMethodDeclaration(GNode n) {
        isClassScope = false;
        // create new objects to initialize
        JavaMethod m = new JavaMethod();
        Hashtable p = new Hashtable<String, String>();

        // add method name
        m.name = (n.getString(3));
        // add method modifier
        m.modifier = (n.getGeneric(0).getGeneric(0).getString(0));
        // add implementation
        m.implementation = (n);
        // add method return type
        
        // conditional for void objects, may need more change 
        if (n.getGeneric(2).getName() == "VoidType")
            m.type = ("void");
        else
            m.type = (convertString(n.getGeneric(2).getGeneric(0).getString(0)));

        // visit the body of the method
        visit(n);
        // set params of method

        p.putAll(paramsList);
        m.params = (p);
        // add method to list of methods
        methods.add(m);
        // reset method when done with one method
        paramsList.clear();
    }

    public void visitFormalParameter(GNode n) {
        //Grab params
        if(isConstructor){
            cparamsList.put(n.getString(3), convertString(n.getGeneric(1).getGeneric(0).getString(0)));
        }
        else
            paramsList.put(n.getString(3), convertString(n.getGeneric(1).getGeneric(0).getString(0)));
        visit(n);
    }

    public void visitFieldDeclaration(GNode n) {
        if (isClassScope) {
            JavaGlobalVariable globalVariable = new JavaGlobalVariable();
            globalVariable.name = (n.getGeneric(2).getGeneric(0).getString(0));
            globalVariable.type = (n.getGeneric(1).getGeneric(0).getString(0));
            try {
                globalVariable.modifier = (n.getGeneric(0).getGeneric(0).getString(0));
            } catch (IndexOutOfBoundsException e) {
                globalVariable.modifier = ("public");
            }

            globalVars.add(globalVariable);
        }
        visit(n);
    }

    public void visit(Node n) {
        for (Object o : n) {
            // visit the nearest instance of a node 
            if (o instanceof Node) dispatch((Node) o);
        }
    }

    public String convertString(String str) {
        if (str.equals("String")) {
            return ("String");
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
