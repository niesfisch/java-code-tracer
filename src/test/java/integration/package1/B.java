package integration.package1;

import integration.package2.Collaborator1;
import integration.package2.Collaborator2;

/**
 * Created by msauer on 6/26/15.
 */
public class B {

    private final SubClass subClass = new SubClass();
    private final Collaborator1 collaborator1 = new Collaborator1();
    private final Collaborator2 collaborator2 = new Collaborator2();

    public void b() {
        subClass.subclassMethod();
        collaborator1.collaborator1Method();
        collaborator2.collaborator2Method();

    }
}
