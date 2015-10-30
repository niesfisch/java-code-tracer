package integration.package1;

/**
 * Created by msauer on 6/26/15.
 */
class ClassWithInnerClasses {


    void classWithInnerClasses() {
        new Runnable() {
            public void run() {

            }
        }.run();

        new StaticInnerClass().staticInnerClassMethod();
        new NonStaticInnerClass().nonStaticInnerClassMethod();
    }

    static class StaticInnerClass {
        public void staticInnerClassMethod() {

        }
    }

    class NonStaticInnerClass {
        public void nonStaticInnerClassMethod() {

        }
    }
}
