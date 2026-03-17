package de.marcelsauer.sample;

import org.springframework.stereotype.Component;

@Component
public class ClassA {

    private final ClassB classB;

    public ClassA(ClassB classB) {
        this.classB = classB;
    }

    public void methodA_1() {
        classB.methodB_2();
    }
}

