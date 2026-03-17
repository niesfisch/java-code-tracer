package de.marcelsauer.sample;

import org.springframework.stereotype.Component;

@Component
public class ClassH {

    private final ClassI classI;

    public ClassH(ClassI classI) {
        this.classI = classI;
    }

    public void methodH_9() {
        classI.methodI_10();
    }
}

