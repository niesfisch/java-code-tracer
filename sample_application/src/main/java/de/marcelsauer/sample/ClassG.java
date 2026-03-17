package de.marcelsauer.sample;

import org.springframework.stereotype.Component;

@Component
public class ClassG {

    private final ClassH classH;

    public ClassG(ClassH classH) {
        this.classH = classH;
    }

    public void methodG_8() {
        classH.methodH_9();
    }
}

