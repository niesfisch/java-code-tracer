package de.marcelsauer.sample;

import org.springframework.stereotype.Component;

@Component
public class ClassC {

    private final ClassD classD;

    public ClassC(ClassD classD) {
        this.classD = classD;
    }

    public void methodC_4() {
        classD.methodD_5();
    }
}

