package integration.package1;

/**
 * Created by msauer on 6/26/15.
 */
public class A implements InterfaceA {

    private final B b = new B();

    public void a() {
        b.b();
    }
}
