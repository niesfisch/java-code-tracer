package integration.package1;

/**
 * Created by msauer on 6/26/15.
 */
class SubClass extends SuperClass implements SubInterface {

    private ClassWithInnerClasses classWithInnerClasses = new ClassWithInnerClasses();

    void subclassMethod() {
        subinterfaceMethod();
        classWithInnerClasses.classWithInnerClasses();
    }

    public void subinterfaceMethod() {
        superinterfaceMethod();
    }

    public void superinterfaceMethod() {

    }
}
