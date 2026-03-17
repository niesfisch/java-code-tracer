package de.marcelsauer.sample;

import org.springframework.stereotype.Component;

@Component
public class ClassD {

    private final ClassE classE;

    public ClassD(ClassE classE) {
        this.classE = classE;
    }

    public void methodD_5() {
        classE.methodE_6();
    }
}

