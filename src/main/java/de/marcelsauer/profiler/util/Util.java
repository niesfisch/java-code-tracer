package de.marcelsauer.profiler.util;

import javassist.CtMethod;
import javassist.bytecode.AccessFlag;

/**
 * @author msauer
 */
public class Util {
    public static String toJVM(String clazz) {
        return clazz.replace(".", "/");
    }

    public static String fromJVM(String clazz) {
        return clazz.replace("/", ".");
    }

    public static boolean isPublic(CtMethod declaredMethod) {
        return AccessFlag.isPublic(declaredMethod.getModifiers());
    }

    public static String packageName(String clazz) {
        if (clazz.contains("/")) {
            return Util.fromJVM(clazz).substring(0, clazz.lastIndexOf("/"));
        } else {
            return clazz.substring(0, clazz.lastIndexOf("."));
        }
    }

}
