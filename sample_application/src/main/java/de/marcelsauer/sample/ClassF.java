package de.marcelsauer.sample;

import org.springframework.stereotype.Component;

@Component
public class ClassF {

    private final ClassG classG;

    public ClassF(ClassG classG) {
        this.classG = classG;
    }

    public void methodF_7() {
        classG.methodG_8();
    }
}

