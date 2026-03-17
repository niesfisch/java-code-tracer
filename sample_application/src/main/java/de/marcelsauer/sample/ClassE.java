package de.marcelsauer.sample;

import org.springframework.stereotype.Component;

@Component
public class ClassE {

    private final ClassF classF;

    public ClassE(ClassF classF) {
        this.classF = classF;
    }

    public void methodE_6() {
        classF.methodF_7();
    }
}

