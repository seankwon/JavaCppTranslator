/* JavaMethod.java
 * 
 * This method contruct holds all the necessary parts of a JavaMethod
 * for use by the JavaClass and later by the Visitor.
 *
 */

import java.util.Hashtable;
import xtc.tree.GNode;
import xtc.tree.Node;
import xtc.lang.JavaFiveParser;

public class JavaMethod {
    public Hashtable<String, String> params;
    /*
     * XXX Output of params:
     * [name] : [type]
     *
     * Example :
     * {
     *  "foo" : "String"
     *  "bar" : "int"
     * }
     */
    public String name;
    public String type;
    public String className;
    public String modifier;
    public GNode implementation;

    public JavaMethod() {
    
    }

    public boolean isEqualTo(JavaMethod method) {
        if ((name.equals(method.name))
            && (type.equals(method.type)))
             {
            return true;
        } else {
            return false;
        }
    }

    public JavaMethod(String n, String t, Hashtable<String, String> p, String m) {
        params = p; 
        type = t;
        name = n;
        modifier = m;
    }
    
    public JavaMethod(String n, String t, Hashtable<String, String> p, GNode i) {
        params = p; 
        type = t;
        name = n;
        implementation = i;
    }

    @Override
    public String toString() {
        return "\t" + modifier + " " + type + " " + name + "\n";
    }
}
