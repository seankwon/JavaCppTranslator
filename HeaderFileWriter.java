/* HeaderFileWriter.java
 * 
 * This class writes the header file for the translated C++ file
 *
 */
 
import java.util.ArrayList;
import java.util.Map;
import java.util.Iterator;
import java.util.Hashtable;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class HeaderFileWriter {
    private final String REF_FILE_ONE = "referenceFiles/objectStructs.ref";
    private final String REF_FILE_TWO = "referenceFiles/arrayStructs.ref";
    private ArrayList<JavaClass> classes;
    private ArrayList<JavaMethod> oldmethods;
    private ArrayList<String> structDefinitionStrings;
    private ArrayList<String> vtableStrings;
    private ArrayList<String> constructorStrings;
    private ArrayList<JavaMethod> temp;
    private JavaClass object;
    private JavaObject _obj;

    public HeaderFileWriter() {}
    public HeaderFileWriter(ArrayList<JavaClass> c) {
        classes = c;
        oldmethods = new ArrayList<JavaMethod>();
        for(JavaClass p : classes){
            for(JavaMethod m : p.methods){
                JavaMethod t = new JavaMethod();
                t.name = m.name;
                t.className = m.className;
                oldmethods.add(t);
            }
        }

        _obj = new JavaObject();
        object = _obj.getObject();
        structDefinitionStrings = new ArrayList<String>();
        vtableStrings = new ArrayList<String>();
        constructorStrings = new ArrayList<String>();
        temp = new ArrayList<JavaMethod>();
    }

    public ArrayList separateJavaGlobalVariables(JavaClass c) {
        //FIXME ArrayList<JavaGlobalVariable> separateJavaGlobalVariables....
        //Modify the following so that it works correctly
        ArrayList<JavaGlobalVariable> publicGV = new ArrayList<JavaGlobalVariable> ();
        ArrayList<JavaGlobalVariable> privateGV = new ArrayList<JavaGlobalVariable> ();
        ArrayList<JavaGlobalVariable> protectedGV = new ArrayList<JavaGlobalVariable> ();

        ArrayList<JavaGlobalVariable> vars = c.globalVars;
        for (JavaGlobalVariable gv : vars) {
            if (gv.modifier.equals("public")) {
                publicGV.add(gv);
            } else if (gv.modifier.equals("private")) {
                privateGV.add(gv);
            } else if (gv.modifier.equals("protected")) {
                protectedGV.add(gv);
            } else {
                publicGV.add(gv);
            }
        }

        ArrayList separatedGV = new ArrayList();
        //FIXME use this syntax always! also, for some reason, this gets the
        //wrong output. I'm very against unchecked data structures.
        //ArrayList<JavaGlobalVariable> separatedGV = new ArrayList<JavaGlobalVariable>();

        separatedGV.add(publicGV);
        separatedGV.add(privateGV);
        separatedGV.add(protectedGV);

        return separatedGV;
    }

    public ArrayList separateMethods(JavaClass c) {
        //FIXME ArrayList<JavaGlobalVariable>  separateMethods....
        //Modify the following so that it works correctly
        ArrayList<JavaMethod> publicC = new ArrayList<JavaMethod> ();
        ArrayList<JavaMethod> privateC = new ArrayList<JavaMethod> ();
        ArrayList<JavaMethod> protectedC = new ArrayList<JavaMethod> ();

        ArrayList<JavaMethod> methods = c.methods;

        for (JavaMethod jc : methods) {
            if (jc.modifier.equals("public")) {
                publicC.add(jc);
            } else if (jc.modifier.equals("private")) {
                privateC.add(jc);
            } else if (jc.modifier.equals("protected")) {
                protectedC.add(jc);
            } else {
                publicC.add(jc);
            }
        }

        ArrayList separatedC = new ArrayList();
        //FIXME use this syntax always! also, for some reason, this gets the
        //wrong output. I'm very against unchecked data structures.
        //ArrayList<JavaGlobalVariable> separatedGV = new ArrayList<JavaGlobalVariable>();

        separatedC.add(publicC);
        separatedC.add(privateC);
        separatedC.add(protectedC);

        return separatedC;
    }

    public void setStructDefinitionString(ArrayList<JavaClass> cl) {
        String tempStr = "";
        Iterator<Map.Entry<String, String>> it;
        int p = 0;
        for (JavaClass c : cl) {
            tempStr += "    struct " + "__" + c.name + " { \n";
            tempStr += "      __" + c.name + "_VT*" + " " + "__vptr;\n\n";
            tempStr += "      __" + c.name + "();\n";
            if (c.getCparam_string().length() != 0)
                tempStr += "      static " + c.name + " constructor("+ c.name +", "+ c.getCparam_string() + ");\n";
            else
                tempStr += "      static " + c.name + " constructor("+ c.name +");\n";


            ArrayList sgv = separateJavaGlobalVariables(c);
            ArrayList sm = separateMethods(c);
            for (int i = 0; i < 3; i++) {

                ArrayList<JavaGlobalVariable> currentGVList = (ArrayList<JavaGlobalVariable>) sgv.get(i);
                ArrayList<JavaMethod> currentMList = (ArrayList<JavaMethod>) sm.get(i);

                if ((currentGVList.size() == 0) && (currentMList.size() == 0)) {
                    break;
                }

                for (JavaGlobalVariable gv : currentGVList) {
                    tempStr += "            " + gv.type + " " + gv.name + ";\n";
                }

                for (JavaMethod m : currentMList) {
                    if(m.name.equals("main"))
                        continue;
                    tempStr += "            static " + m.type + " " +
                    m.name + "(";
                    if(checkOld(m)){
                        tempStr += m.className;
                        tempStr += (m.params.size() == 0) ? "" : ", ";
                        //tempStr += m.className + " __this, ";
                    }

                    tempStr += (m.params.size() == 0) ? ");\n" : "";
                    // iterate over params 
                    it = m.params.entrySet().iterator();
                    while (it.hasNext()) {
                        Map.Entry<String, String> entry = it.next();
                        tempStr += entry.getValue();
                        tempStr += (it.hasNext()) ? ", " : ");\n";
                    }
                }
            }

            tempStr += "\n";
            tempStr += "      static Class __class();\n\n";
            tempStr += "      static " + c.name + " init("+ c.name + ");\n\n";
            tempStr += "      static __" + c.name + "_VT __vtable;\n";
            tempStr += "    };\n\n\n";
            structDefinitionStrings.add(tempStr);
            tempStr = "";
        }
    }

    public void createVTables() {
        String tempStr = "";
        String consStr = "";
        Iterator<Map.Entry<String, String>> it;
        for (JavaClass c : classes) {
            tempStr += "    struct __" + c.name + "_VT {\n";
            tempStr += "      Class __isa;\n";
            tempStr += "      void (*__delete)(__" + c.name + "*);\n";
            consStr += "      __" + c.name + "_VT()\n";
            //FIXME this part needs a little cleanup
            consStr += "        : __isa(__" + c.name + "::__class()),\n";
            consStr += "        __delete(&__rt::__delete<__" + c.name + ">)";
            consStr += (c.methods.size() <= 0) ? " {\n" : ", \n";

            ////////////////////////////////////Needs work

            ArrayList sgv = separateJavaGlobalVariables(c);
            ArrayList sm = separateMethods(c);
            for (int i = 0; i < 3; i++) {

                ArrayList<JavaMethod> currentMList = (ArrayList<JavaMethod>) sm.get(i);

                if (currentMList.size() == 0) {
                    break;
                }

                for (JavaMethod m : currentMList) {
                    if(m.name.equals("main"))
                        continue;
                    tempStr += "      " + m.type + " (*" + m.name + ")(";
                    if(checkOld(m)){
                        tempStr += c.name; //m.className;
                        if(m.params.size() != 0)
                            tempStr += ", ";
                    }
                    tempStr += (m.params.size() == 0) ? ");\n" : "";
                    
                    consStr += "        " + m.name + "(";
                    if (m.className.equals(c.name)) { // if the method belongs to the current class ... just print it
                        consStr += "&__" + m.className + "::" + m.name + ")";
                    } else { // otherwise, we need to make sure it links properly

                        int paramSize = m.params.size();
                        String paramsString = "";

                        if (paramSize > 1) {
                            paramsString = ", ";
                            it = m.params.entrySet().iterator();
                            it.next(); // skip over the first element as that is already counted for here
                            while (it.hasNext()) {
                                Map.Entry<String, String> entry = it.next();
                                paramsString += entry.getValue();
                                paramsString += (it.hasNext()) ? ", " : "";
                            }
                        }

                        consStr += "(" + m.type + "(*)(" + c.name + paramsString + "))&__" + m.className + "::" + m.name + ")";
                    }

                    //fix final bracket here
                    consStr += (i == c.methods.size()-1) ? "{ " : ", \n";
                    
                    // iterate over params 
                    int paramsCounter = 0;
                    it = m.params.entrySet().iterator();
                    while (it.hasNext()) {
                        Map.Entry<String, String> entry = it.next();
                        if (m.className.equals(c.name)) { // if we are in a method that is owned by the current class, we just print it 
                            tempStr += entry.getValue();
                        } else { // otherwise, we must swap out the first arg with the current class name 
                            tempStr += (paramsCounter == 0) ? c.name : entry.getValue();
                        }
                        tempStr += (it.hasNext()) ? ", " : ");\n";
                        //System.out.println("String[" + paramsCounter + "]: " + tempStr);
                        paramsCounter++;
                    }
                }
            }

            vtableStrings.add(tempStr);
            tempStr = "";
            constructorStrings.add(consStr.substring(0,consStr.length()-3));
            consStr = "";
        }
    }

    public String vTableStr() {
        createVTables();
        String output = "";
        for (int i = 0; i < vtableStrings.size(); i++) {
            output += vtableStrings.get(i) + "\n";
            output += constructorStrings.get(i) + "\n";
            output += "      {}\n";
            output += "    };\n";
        }
        return output;
    }

    public void handleOverride(ArrayList<JavaClass> cl) {
        for (JavaClass c : cl) {
            // gather the class' methods and parents
            ArrayList<JavaMethod> methods = c.methods;
            ArrayList<JavaClass> parents = c.getParents();
            ArrayList<JavaGlobalVariable> globalVariables = c.globalVars;
            parents.add(object);
            // go through the parents of the current class
            for (int i = 0; i < parents.size(); i++) {
                                
                ArrayList<JavaMethod> currParentMethods = parents.get(i).methods;
                int curreParentMethodsSize = currParentMethods.size();
                // go through the methods of the current parent
                for (int k = 0; k < curreParentMethodsSize; k++) {
                    JavaMethod currentParentMethod = currParentMethods.get(k);

                    // if this method is new to the table, then add it. 
                    if (checkIfMethodIsNewInTable(currentParentMethod, methods)) { 
                        currentParentMethod.className = parents.get(i).name; // set the method's "owner"
                        methods.add(currentParentMethod);
                        //System.out.println(currentParentMethod.toString() + currentParentMethod.params.toString());
                    }
                }

                ArrayList<JavaGlobalVariable> currParentGlobalVariables = parents.get(i).globalVars;
                int currParentGlobalVariablesSize = currParentGlobalVariables.size();
                // go through the global variables of the current parent
                for (int k = 0; k < currParentGlobalVariablesSize; k++) {
                    JavaGlobalVariable currentParentGlobalVaraible = currParentGlobalVariables.get(k);

                    // if this variable is new to the table, then add it 
                    if (checkIfVariableIsNewInTable(currentParentGlobalVaraible, globalVariables)) { 
                        globalVariables.add(currentParentGlobalVaraible);
                    }
                }
            }

            c.methods = methods;
            c.globalVars = globalVariables;
        }
    }

    public boolean checkIfMethodIsNewInTable(JavaMethod checkMethod, ArrayList<JavaMethod> methods) {
        for (int i = 0; i < methods.size(); i++) {
            JavaMethod method = methods.get(i);
            if (checkMethod != null && checkMethod.name.equals(method.name)){
                return false;
            }
        }
        return true;
    }

    public boolean checkOld(JavaMethod checkMethod) {
        for (int i = 0; i < oldmethods.size(); i++) {
            JavaMethod method = oldmethods.get(i);
            if (checkMethod != null && checkMethod.name.equals(method.name) && checkMethod.className.equals(method.className)){
                return true;
            }
        }
        return false;
    }

    public boolean findMethod(String name){
        for(int j = 0; j < classes.size(); j++){
            ArrayList<JavaMethod> current_class_methods = classes.get(j).methods;
            for(int i = 0; i < current_class_methods.size(); i++){
                if(current_class_methods.get(i).name.equals(name))
                    return true;
            }
        }
        return false;
    }

    public boolean checkIfVariableIsNewInTable(JavaGlobalVariable checkVariable, ArrayList<JavaGlobalVariable> variables) {
        for (int i = 0; i < variables.size(); i++) {
            JavaGlobalVariable variable = variables.get(i);
            if (checkVariable != null && checkVariable.isEqualTo(variable))
                return false;
        }
        return true;
    }

    public String dataStructsString() {
        String output = "";
        for (String s : structDefinitionStrings) {
            output += (s + "\n");
        }
        return output;
    }

    public String fwdDeclarationsString() {
        String output = "";
        output += "namespace java {\n";
        output += "  namespace lang {\n";
        for (JavaClass c : classes) {
            output += "    struct __" + c.name + ";\n";
            output += "    struct __" + c.name + "_VT;\n\n";
        }

        output+="    struct __Object;\n";
        output+="    struct __Object_VT;\n\n";

        output+="    struct __String;\n";
        output+="    struct __String_VT;\n\n";

        output+="    struct __Class;\n";
        output+="    struct __Class_VT;\n\n";

        output+="    typedef __rt::Ptr<__Object> Object;\n";
        output+="    typedef __rt::Ptr<__Class> Class;\n";
        output+="    typedef __rt::Ptr<__String> String;\n";

        for (JavaClass c : classes) {
            output += "    typedef __rt::Ptr<__" + c.name + "> " + c.name + ";\n";
        }

        output += "  }\n";
        output += "}\n";

        return output;
    }

    public String headerOutput() {
        String output = "";
        output += "#pragma once\n#include <stdint.h>\n#include <string>\n";
        output += "#include \"ptr.h\"\n";
        output += fwdDeclarationsString();
        handleOverride(classes);
        setStructDefinitionString(classes);       
        output += writeBigFile(REF_FILE_ONE);
        output += writeBigFile(REF_FILE_TWO);
        output += "\n\nnamespace java {\n";
        output += "  namespace lang {\n";
        output += dataStructsString() + "\n";
        output += vTableStr(); 
        output += "  }\n";
        output += "}\n";
        return output;
    }

    public String writeBigFile(String path) {
        String output = "";
        String line = null;
        try {
            BufferedReader inFile = new BufferedReader(new FileReader(path));
            while ((line = inFile.readLine()) != null) {
                output += (line + "\n");
            }
        } catch (IOException ioe) {
            ioe.printStackTrace() ;
        }

        return output;
    }

    @Override public String toString() {
        String out = "";
        for (String s : structDefinitionStrings) out += s;
        return out;
    }
}
