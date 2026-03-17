package de.marcelsauer.sample;

import org.springframework.stereotype.Component;

@Component
public class ClassB {

    private final ClassC classC;

    public ClassB(ClassC classC) {
        this.classC = classC;
    }

    public void methodB_2() {
        methodB_3();
    }

    public void methodB_3() {
        classC.methodC_4();
    }
}

