/* JavaClass.java
 * 
 * The contruct of a Java class that holds variables such as the name, 
 * methods, parent, and implementaion for use by the Vistor
 *
 */
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Enumeration;
import xtc.tree.GNode;
import xtc.tree.Node;
import xtc.lang.JavaFiveParser;

public class JavaClass {
    public String name;
    public ArrayList<JavaMethod> methods; // methods stored inside the Java Class
    public JavaClass parent; // the parent of the current Java Class
    public String parentName; // name of the parent
    public GNode implementation; // a structure of the implementation of the Java Class's code
    public ArrayList<JavaGlobalVariable> globalVars; // a list of global variables inside this java class
    public Hashtable<String, String> Cparam;
    public String constructorBlock;

    /* constructors */
    public JavaClass() {
        globalVars = new ArrayList<JavaGlobalVariable>();
        constructorBlock = "";
    }
    
    public JavaClass(String n, ArrayList<JavaMethod> m, GNode i) {
        name = n;
        methods = m;
        implementation = i; 
        globalVars = new ArrayList<JavaGlobalVariable>();
    }

    public ArrayList<JavaClass> getParents() {
        JavaClass curr = parent;
        ArrayList<JavaClass> parents = new ArrayList<JavaClass>();
        while (curr != null) {
            parents.add(curr);
            curr = curr.parent;
        }
        return parents;
    }

    public void setParentFromList(ArrayList<JavaClass> cl) {
        // iterates through a list of set classes
        for (JavaClass c : cl) {
            if (c.name.equals(parentName)) {
                parent = c;
                break;
            } else {
                parent = null; 
            }
        }
    }

    public JavaClass getSuperClass() {
        JavaClass curr = parent;
        while (curr.parent != null) 
            curr = curr.parent;
        return curr;
    }

    public boolean isParent() {
        // recursively check if a certain class is a parnet of this Class
        JavaClass curr = parent; 
        while (curr.parent != null) {
            if (curr.parent.name.equals(name))
                return true;
            else 
                curr = curr.parent;
        }
        return false; 
    }

    @Override public String toString() {
        String methodStr = "";
        for (JavaMethod m : methods)
            methodStr += m.toString();

        return "Name: " + name + "\n" +
            "  Methods: \n" + methods + 
            "  Parent: " + ((parent == null) ? "none" : parent.name);
    }

    public String getCparam_string(){
        Enumeration d = Cparam.keys();
        String ret = "";
        while(d.hasMoreElements()) {
            String t = (String) d.nextElement();
            ret = ret + Cparam.get(t) + " " + t +",";
        }
        if(ret.length() == 0)
            return ret;
        return ret.substring(0, ret.length()-1);
    }
}
