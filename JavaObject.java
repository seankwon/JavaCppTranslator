/* JavaObject.java
 *
 * This class encompasses the basic Java object type.
 * 
 */

import java.util.Hashtable;
import java.util.ArrayList;

public class JavaObject {
    private static JavaClass _obj;
    private static ArrayList<JavaMethod> temp;
    
    public JavaObject() {
        _obj = new JavaClass();
        temp = new ArrayList<JavaMethod>();
        setObjMethods();
    }

    public void setObjMethods() {
        _obj.name = "Object";

        Hashtable<String, String> h1 = new Hashtable<String, String>();
        h1.put("0", "Object");
        JavaMethod m1 = new JavaMethod("hashCode", "int32_t", h1, "public");
        m1.className = ("Object");

        Hashtable<String, String> h2 = new Hashtable<String, String>();
        h2.put("0", "Object");
        h2.put("1", "Object");
        JavaMethod m2 = new JavaMethod("equals", "bool", h2, "public");
        m2.className = ("Object");

        Hashtable<String, String> h3 = new Hashtable<String, String>();
        h3.put("0", "Object");
        JavaMethod m3 = new JavaMethod("getClass", "Class", h3, "public");
        m3.className = ("Object");

        Hashtable<String, String> h4 = new Hashtable<String, String>();
        h4.put("0", "Object");
        JavaMethod m4 = new JavaMethod("toString", "String", h4, "public");
        m4.className = ("Object");

        /*Hashtable<String, String> h5 = new Hashtable<String, String>();
        h5.put("0", "Class");
        JavaMethod m5 = new JavaMethod("getName", "String", h5, "public");
        m5.setClassName("Class");

        Hashtable<String, String> h6 = new Hashtable<String, String>();
        h6.put("0", "Class");
        JavaMethod m6 = new JavaMethod("getSuperClass", "Class", h6, "public");
        m6.setClassName("Class");

        Hashtable<String, String> h7 = new Hashtable<String, String>();
        h7.put("0", "Class");
        JavaMethod m7 = new JavaMethod("isPrimitive", "bool", h7, "public");
        m7.setClassName("Class");

        Hashtable<String, String> h8 = new Hashtable<String, String>();
        h8.put("0", "Class");
        JavaMethod m8 = new JavaMethod("isArray", "bool", h8, "public");
        m8.setClassName("Class");

        Hashtable<String, String> h9 = new Hashtable<String, String>();
        h9.put("0", "Class");
        JavaMethod m9 = new JavaMethod("getComponentType", "Class", h9, "public");
        m9.setClassName("Class");

        Hashtable<String, String> h10 = new Hashtable<String, String>();
        h10.put("0", "Class");
        h10.put("1", "Object");
        JavaMethod m10 = new JavaMethod("isInstance", "bool", h10, "public");
        m10.setClassName("Class");*/

        temp.add(m1); 
        temp.add(m2); 
        temp.add(m3); 
        temp.add(m4);
        /*temp.add(m5); 
        temp.add(m6); 
        temp.add(m7); 
        temp.add(m8);
        temp.add(m9); 
        temp.add(m10);*/
        _obj.methods = (temp);
    }

    public static JavaClass getObject() {
        return _obj;
    }
    
}
