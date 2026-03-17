package de.marcelsauer.sample;

import org.springframework.stereotype.Component;

@Component
public class ClassI {

    private final ClassJ classJ;

    public ClassI(ClassJ classJ) {
        this.classJ = classJ;
    }

    public void methodI_10() {
        classJ.methodJ_11();
    }
}

