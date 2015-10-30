package de.marcelsauer.profiler.config;

import java.util.HashSet;
import java.util.Set;

/**
 * yaml bean
 *
 * @author msauer
 */
public class Recorder {
    public Set<String> interfaces = new HashSet<String>();
    public Set<String> superClasses = new HashSet<String>();
    public Set<String> classLevelAnnotations = new HashSet<String>();
    public Set<String> methodLevelAnnotations = new HashSet<String>();


    @Override
    public String toString() {
        return "Recorder{" +
            "interfaces=" + interfaces +
            ", superClasses=" + superClasses +
            ", classLevelAnnotations=" + classLevelAnnotations +
            ", methodLevelAnnotations=" + methodLevelAnnotations +
            '}';
    }
}
