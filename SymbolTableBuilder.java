
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
import java.util.List;

import xtc.Constants;

import xtc.lang.JavaEntities;

import xtc.tree.GNode;
import xtc.tree.Node;
import xtc.tree.Visitor;
import xtc.tree.Attribute;
import xtc.tree.Printer;
import xtc.util.SymbolTable;
import xtc.util.Runtime;
import xtc.type.*;

public class SymbolTableBuilder extends Visitor {
    final private SymbolTable table;
    final private Runtime runtime;
    public MethodsWriter w;
    private ArrayList<JavaClass> classes;
    private Hashtable<String, String> allVars = new Hashtable<String, String>();
    private JavaClass current_class;
    private JavaMethod current_method;
    private ArrayList<JavaMethod> current_class_methods;
    private ArrayList<JavaGlobalVariable> current_class_global_variables;
    private String methodCalled = "";
    private boolean isMainMethod = false;
    private boolean hasConstructor = false;
    private boolean isString = false;
    private boolean notDoneClassVars = false;
    private boolean fromNewClassExpression = false;
    private boolean fromArgs = false;
    private boolean inLoop = false;
    private StringBuilder constructorCode;
    private String currentObject = "";
    private String OUTPUT_FILE_NAME = "main.cc";
    private String nullCheck = "";
    private String pop  = "";
    private String testClass;
    private String cn;

    public SymbolTableBuilder(ArrayList<JavaClass> classes, final Runtime runtime, final SymbolTable table) {
        current_class = null;
        current_class_methods = null;
        this.classes = classes;

        w = new MethodsWriter(OUTPUT_FILE_NAME);
        current_class_global_variables = null;
        this.runtime = runtime;
        this.table = table;
    }
      
    public void visitCompilationUnit(GNode n) {
        dispatch(n.getNode(0));

        table.enter(JavaEntities.fileNameToScopeName(n.getLocation().file));
        table.mark(n);
    
        for (int i = 1; i < n.size(); i++) {
            GNode child = n.getGeneric(i);
            dispatch(child);
        }
    
        table.setScope(table.root());
    }

    public void visitClassDeclaration(GNode n) {
        hasConstructor = false;
        String className = n.getString(1);
        current_class = findClass(className);
        writeVTable(current_class.name);
        table.enter(className);
        table.mark(n);
        if (current_class.globalVars.size() <= 0) {
            beginInit(current_class.name);
            endInit();
        }
        visit(n);
        checkConstructors();
        current_class = null;
        current_class_global_variables = null;
        current_method = null;
        current_class_methods = null;
        table.exit();
    }

    public void visitMethodDeclaration(GNode n) {
        System.out.println(n);
        testClass = findTestClass();
        String methodName = JavaEntities.methodSymbolFromAst(n);
        table.enter(methodName);
        table.mark(n);
        if (findClass(n.getString(3)) == null) {
            if (current_class == null) {
                current_class_methods = null;
                current_class_global_variables = null;
            } else {
                current_class_methods = current_class.methods;
                current_class_global_variables = current_class.globalVars;
            }
            if (current_class_methods != null){
                current_method = findMethod(n.getString(3));
                writeMainMethod();
                visit(n);
                w.println("}");
                current_method = null;
            } else {
                isMainMethod = true;
                checkConstructors();
                w.println("}}");
                w.print("int main (int argc, char* argv[]){");
                visit(n);
                w.println("}");
                current_method = null;
            }
        } else {
            hasConstructor = true;
            writeConstructor(current_class);
            w.constructorWrite = true;
            w.println(current_class.getParentsConstructors());
            visit(n);
            current_class.constructorBlock = w.constructorBlock.toString();
            w.resetConstructor();
            w.println("return __this;");
            w.println("}");
        }
        table.exit();
    }

    public void visitArguments(GNode n) {
        fromArgs = true;
        if (!fromNewClassExpression) {
            w.print(methodCalled);
        }
        if (n.isEmpty()) {
            w.print("(" + currentObject + ")");
            visit(n);
        } else if (n.size() == 1) {
            w.print("("+currentObject+",");
            visit(n);
            w.print(")");
        } else {
            w.print("(" + currentObject + ",");
            visit3(n, ",");
            w.print(")");
        }
        fromArgs = false;
    }

    public void visitNewClassExpression(GNode n) {
        String classN = n.getGeneric(2).getString(0);
        //System.out.println(n.getGeneric(3).size());
        if (!fromArgs) {
            w.print("__"+classN+"::constructor");
            currentObject = "new __"+classN+"()";
        } else {
            currentObject = "new __"+classN+"()";
            fromNewClassExpression = true;
        }
        visit(n);
        fromNewClassExpression = false;
    }

    public void visitThisExpression(GNode n){
        w.print("__this");
        visit(n);
    }

    public void visitBlock(GNode n) {
        table.enter(table.freshName("block"));
        table.mark(n);
        visit(n);
        table.exit();
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

    public void visitDeclarator(GNode n) {
        if(n.getGeneric(2) == null)
            w.print(convertString(n.getString(0)));
        else{
            w.print(convertString(n.getString(0)) + " = ");
            if(n.getNode(2) != null && n.getNode(2).hasName("NewClassExpression"))
                nullCheck = n.getString(0);
        }
        visit(n);
    }

    public void visitNewArrayExpression(GNode n){
        w.print("new __rt::Array<" + cn + ">(");
        visit(n);
        w.print(")");
    } 

    public void visitForStatement(GNode n) {
        inLoop = true;
        int i = 0;
        w.print("for(");
        table.enter(table.freshName("forStatement"));
        table.mark(n);
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
        table.exit();
        inLoop = false;
    }

    public void visitWhileStatement(GNode n) {
        inLoop = true;
        int i = 0;
        w.print("while(");
        table.enter(table.freshName("whileStatement"));
        table.mark(n);
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
        table.exit();
        inLoop = false;
    }
      
    public void visit(GNode n) {
        for (Object o : n) {
            if (o instanceof Node) dispatch((Node) o);
        }
    }

    public final List<Type> visitFieldDeclaration(final GNode n) {
        notDoneClassVars = (current_class != null && current_method !=null);
        if(notDoneClassVars) {
            //checks if field declaration is an array
            if (checkIfArray(n)){
                printArray(n);
               // System.out.println("diap:\n" + n.toString());
                //System.out.println(n.getNode(2).toString());
               // visit(n.getNode(2));
             //   return null;
            }
            else    
                w.print(convertString(n.getGeneric(1).getGeneric(0).getString(0)) + " ");
            visit(n);
            w.println(";");
            if (nullCheck.length() != 0)
                w.println("__rt::checkNotNull("+nullCheck+");");
            nullCheck = "";
        } else {
            beginInit(current_class.name);
            w.print("__this->");
            visit(n);
            nullCheck = "";
            w.println(";");
            endInit();
        }
        @SuppressWarnings("unchecked")
            final List<Attribute> modifiers = (List<Attribute>) dispatch(n.getNode(0));
        Type type = (Type) dispatch(n.getNode(1));
        return processDeclarators(modifiers, type, n.getGeneric(2));
    }
      
    /** Visit a Modifiers = Modifier*. */
    public final List<Attribute> visitModifiers(final GNode n) {
        final List<Attribute> result = new ArrayList<Attribute>();
        for (int i = 0; i < n.size(); i++) {
            final String name = n.getGeneric(i).getString(0);
            final Attribute modifier = JavaEntities.nameToModifier(name);
            if (null == modifier)
                runtime.error("unexpected modifier " + name, n);
            else if (result.contains(modifier))
                runtime.error("duplicate modifier " + name, n);
            else
                result.add(modifier);
        }
        return result;
    }

    public final List<Type> processDeclarators(final List<Attribute> modifiers,
                                               final Type type, final GNode declarators) {
        final List<Type> result = new ArrayList<Type>();
        boolean isLocal = JavaEntities.isScopeLocal(table.current().getQualifiedName());
        GNode checkNode = declarators.getGeneric(0).getGeneric(2);
        for (final Object i : declarators) {
            GNode declNode = (GNode) i;
            String name = declNode.getString(0);
            Type dimType = JavaEntities.typeWithDimensions(type, 
                                                           countDimensions(declNode.getGeneric(1)));
            Type entity = isLocal ? VariableT.newLocal(dimType, name) : 
                VariableT.newField(dimType, name);
            if (checkNode != null) {
                if (checkNode.hasName("NewClassExpression"))
                    allVars.put(name, name);
                else if (checkNode.hasName("PrimaryIdentifier")) {
                    if (allVars.containsKey(checkNode.getString(0)))
                        allVars.put(name, allVars.get(checkNode.getString(0)));
                }
            }

            for (Attribute mod : modifiers)
                entity.addAttribute(mod);
            if (null == table.current().lookupLocally(name)) {
                result.add(entity);
                table.current().define(name, entity);
                //entity.scope(table.current().getQualifiedName());
            }
        }
        return result;
    }
    public void visitPrimaryIdentifier(GNode n){
        //w.print(n.getString(0));
        boolean scope = false;
        boolean thisP = false;
        String var = (allVars.containsKey(n.getString(0))) ? allVars.get(n.getString(0)) :
            n.getString(0);
        if (table.current().lookup(var) != null) 
            scope = isLocalOrParam(table.current().lookup(var).toString());
        //look into current Symbol table using the name of the variable to check if it is a local variable. 
        if(!var.equals("test") && !scope) {     //this returns true if the variable is not a local variable
            if (isGlobalFromTest(var))
                w.print("test->"+var);
            else if (isGlobalVar(var))
                if(!inLoop)
                    w.print("__this->"+var);
                else
                    w.print(var);
            else
                w.print(var);
        } else {     //else the variable is a local variable
            w.print(var);
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

    public void visitSubscriptExpression(GNode n) {
        pop = "(*" + n.getNode(0).getString(0) + ")[" + n.getNode(1).getString(0) + "]";
        w.print("(*" + n.getNode(0).getString(0) + ")[" + n.getNode(1).getString(0) + "]");
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
                    w.println(testClass + " test = __" + testClass + "::constructor(new __" + testClass + "());");
                    w.println("__rt::Ptr<__rt::Array<String>, __rt::array_policy> args = new __rt::Array<String>(argc - 1);");

                    w.println(" for (int32_t i = 1; i < argc; i++) {" );
                    w.println("(*args)[i - 1] = __rt::literal(argv[i]);\n}");
                    mainM = true;
                }
            }
        }
        visit(n);
    }

    public void visitCallExpression(GNode n) {
        JavaMethod method;
        if (n.getNode(0) == null) {
            //case when the test class is actually being called
            int i = 1;
            currentObject = "test";
            GNode temp = GNode.create("PrimaryIdentifier", "test");
            n.set(0, temp);
            method = findMethodWithinMain(n.getString(2));
            methodCalled = "->__vptr->"+convertString( n.getString(2) );
            visit(n);
            methodCalled = "";
            method = null;
        } else if (n.getNode(0).hasName("SelectionExpression") && 
                n.getNode(0).getNode(0).getString(0).equals("System")) {
            //Case when method is trying to System.out.println()
            w.print("cout << "); 
            if(n.getNode(3).getNode(0).hasName("StringLiteral")) {
                w.print(n.getNode(3).getNode(0).getString(0));
            } else {
                visit(n.getNode(3));
            }
            if (n.getString(2).equals("println")) {
                w.print(" << std::endl");
            }
        } else if (n.getNode(0).hasName("SubscriptExpression") || n.getNode(0).hasName("CastExpression")) {
            System.out.println("asdf\n" + n.toString());
            int once = 0;
            for (Object o : n) {
                if (o instanceof Node){
                    Node p = (Node) o;
                    if(p.hasName("Arguments")){
                        w.print("->__vptr->" + n.getString(2));
                    }
                    dispatch((Node) o);
                }
            }
        }
        else {
            int i = 1;
            //System.out.println(n.getNode(0).toString());
            if(!n.getNode(0).hasName("SelectionExpression")){
                currentObject = n.getNode(0).getString(0);
            }            
            method = findMethodWithinMain(n.getString(2));
            methodCalled = "->__vptr->"+convertString( n.getString(2) );
            // set the following to true when program is trying to print a
            // string or a method with a string return type
            visit(n);

            methodCalled = "";
            method = null;
        }
    }

    public void visitSelectionExpression(GNode n) {
        if (n.size() == 2) {
            if (n.getNode(0).hasName("PrimaryIdentifier")) {
                currentObject = n.getNode(0).getString(0) + "->"+n.getString(1);
            } else {
                currentObject = n.getString(1);
            }
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
    
    public void visitCastExpression(GNode n){
        w.print("((" + n.getNode(0).getNode(0).getString(0) + ")");
        visit(n);
        w.print(")");
        currentObject = "(" + n.getNode(0).getNode(0).getString(0) + ")" + pop;
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

    public void close(){
        w.close();
        return;
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
    
    public void visitPostfixExpression(GNode n){
        visit(n);
        w.print(convertString(n.getString(1)));        
    }

    public final Type visitFormalParameter(final GNode n) {
        assert null == n.get(4) : "must run JavaAstSimplifier first";
        String id = n.getString(3);
        Type dispatched = (Type) dispatch(n.getNode(1));
        Type result = VariableT.newParam(dispatched, id);
        if (n.getGeneric(0).size() != 0)
            result.addAttribute(JavaEntities.nameToModifier("final"));
        if (null == table.current().lookupLocally(id)) {
            table.current().define(id, result);
            result.scope(table.current().getQualifiedName());
        } else
            runtime.error("duplicate parameter declaration " + id, n);
        assert JavaEntities.isParameterT(result);
        return result;
    }

    public final Type visitPrimitiveType(final GNode n) {
        final Type result = JavaEntities.nameToBaseType(n.getString(0));
        return result;
    }

    public void visitArrayInitializer(GNode n) {
        visit(n);
    }

    public final Type visitType(final GNode n) {
        final boolean composite = n.getGeneric(0).hasName("QualifiedIdentifier");
        final Object dispatched0 = dispatch(n.getNode(0));
        assert dispatched0 != null;
        final Type componentT = composite ? new AliasT((String) dispatched0) : (Type) dispatched0;
        final int dimensions = countDimensions(n.getGeneric(1));
        final Type result = JavaEntities.typeWithDimensions(componentT, dimensions);
        return result;
    }

    public static int countDimensions(final GNode dimNode) {
        return null == dimNode ? 0 : dimNode.size();
    }

    public final Type visitVoidType(final GNode n) {
        return JavaEntities.nameToBaseType("void");
    }

    public final String visitQualifiedIdentifier(final GNode n) {
        final StringBuffer b = new StringBuffer();
        for (int i = 0; i < n.size(); i++) {
            if (b.length() > 0)
                b.append(Constants.QUALIFIER);
            b.append(n.getString(i));
        }
        return b.toString();
    }

    public JavaClass findClass(String n){
        for(int i=0; i<classes.size(); i++){
            if(classes.get(i).name.equals(n))
                return classes.get(i);
        }
        return null;
    }

    public JavaMethod findMethodWithinMain(String n) {
        for (int i = 0; i < classes.size(); i++) {
            for (int j = 0; j < classes.get(i).methods.size(); j++)  {
                if (classes.get(i).methods.get(j).name.equals(n)) 
                    return classes.get(i).methods.get(j);
            }
        }
        return null;
    }

    public void writeMainMethod() {
        if (current_method.name.equals("main") && current_method.modifier.equals("public")&& current_method.type.equals("void")){
            isMainMethod = true;
            checkConstructors();
            w.println("}}");
            w.println("int main (int argc, char* argv[]){");
        } else {
            w.print(current_method.type + " __" + current_class.name + "::" 
                               + current_method.name + "(" + current_class.name + " __this");
            Iterator<Map.Entry<String, String>> it = current_method.params.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry<String, String> entry = it.next();
                w.print(", " + entry.getValue() + " " + entry.getKey());
            }
            w.println("){");
        }
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

    public void visit3(GNode n, String s){
        int i = 1;
        for (Object o : n) {
            i++;
            if (o instanceof Node) dispatch((Node) o);
            if (i <= n.size()) w.print(s);
        }
    }

    public void visit(Node n) {
        for (Object o : n) {
            // visit the nearest instance of a node 
            if (o instanceof Node) dispatch((Node) o);
        }
    }

    public void writeVTable(String n) {
        w.println("__" + n + "_VT " + "__" + n + "::__vtable;");
        w.println("Class __" + n + "::__class() {");
        w.println("   static Class k = " + "new __Class(__rt::literal(\"" + 
                  n + "\"), (Class)__rt::null());");
        w.println("   return k;");
        w.println("}");
    }

    public void beginInit(String n) {
        w.println("__" + n + "::__" + n +"():__vptr(&__vtable){}");
        w.println(n+" __"+n+"::init("+n+" __this) {\n") ;
        w.println("  __Object::init(__this);\n") ;
    }

    public void endInit() {
        w.println("  return __this;\n") ;
        w.println("}\n\n") ;
    }

    public void writeConstructor(JavaClass c) {
        w.print(c.name + " __" + c.name + "::constructor(");
        if (c.getCparam_string().length() != 0)
            w.print(c.name + " __this, " + c.getCparam_string() + ") ");
        else
            w.print(c.name + " __this) ");
        w.println("{");
        w.println("__this = __" + current_class.name + "::init(__this);");
    }

    public boolean isLocalOrParam(String s) {
        String c = s.substring(0, s.indexOf("("));
        return (c.equals("param") || c.equals("local"));
    }

    public String findTestClass() {
        for (JavaClass c : classes) {
            if (c.name.contains("Test")) 
                return c.name;
        } 
        return "";
    }

    public void printArray(GNode n) {
        cn = n.getGeneric(1).getGeneric(0).getString(0);
        w.print("__rt::Ptr<__rt::Array<" + cn + ">,__rt::array_policy> ");
    }

    public boolean checkIfArray(GNode n) {
        return (n.getNode(1) != null &&
                n.getNode(1).getNode(1) != null &&
                n.getNode(1).getNode(1).hasName("Dimensions"));
    }

    public boolean isGlobalFromTest(String s) {
        for (JavaClass cl : classes) {
            for (JavaGlobalVariable g : cl.globalVars)  {
                if (s.equals(g.name) && cl.name.contains("Test")) return true;
            }
        }
        return false;
    }

    public boolean isGlobalVar(String s) {
        for (JavaClass cl : classes) {
            for (JavaGlobalVariable g : cl.globalVars)  {
                if (s.equals(g.name)) return true;
            }
        }
        return false;
    }

    public void checkConstructors() {
        if (!hasConstructor) {
            writeConstructor(current_class);
            w.println("return __this;");
            w.println("}");
            hasConstructor = true;
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
          } else if (str.equals("byte")){
              return "unsigned char";
          } else {
              return str; 
          }
      }

}
