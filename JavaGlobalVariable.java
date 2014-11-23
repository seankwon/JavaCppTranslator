/* JavaGlobalVariable.java
 * 
 * This method contruct holds all the necessary parts of a Java 
 * Global Variable for use by the JavaClass and later by the Visitor.
 *
 */

import java.util.Hashtable;
import xtc.tree.GNode;
import xtc.tree.Node;
import xtc.lang.JavaFiveParser;

public class JavaGlobalVariable {
	public String name;
    public String type;
    public String modifier;

    public JavaGlobalVariable() {}

    public JavaGlobalVariable(String n, String t, String m) {
    	name = n;
    	type = t;
    	modifier = m;
    }

    @Override public String toString() {
    	return modifier + " " + type + " " + name;
    }

    public boolean isEqualTo(JavaGlobalVariable variable) {
    	if ((name.equals(variable.name)) && (type.equals(variable.type)))
    		return true;
    	return false;
    }
}
