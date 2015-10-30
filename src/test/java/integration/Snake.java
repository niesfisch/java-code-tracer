package integration;

import de.marcelsauer.profiler.config.FileUtils;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

import java.util.List;
import java.util.Set;

/**
 * Created by msauer on 7/9/15.
 */
public class Snake {

    public static void main(String[] args) throws Exception {
        Yaml yaml = new Yaml(new Constructor(Invoice.class));
        //Invoice invoice = (Invoice) yaml.load(Util.getLocalResource("de/marcelsauer/profiler/integration/example2_27.yaml"));
        yaml = new Yaml(new Constructor(Config.class));
        Config config = (Config) yaml.load(FileUtils.getLocalResource("de/marcelsauer/profiler/integration/config.yaml"));

        System.out.println(config);

//        Person billTo = invoice.billTo;
//        System.out.println(billTo.family);
//        System.out.println(invoice.classes);

//        yaml = new Yaml();
//        String output = yaml.dump(invoice);
//        System.out.println(output);
    }
}

class Config {
    public Package packages;
    public Recorder recorder;

    @Override
    public String toString() {
        return "Config{" +
            "classes=" + packages +
            ", recorder=" + recorder +
            '}';
    }
}

class Recorder {
    public Set<String> interfaces;
    public Set<String> superClasses;
    public Set<String> classLevelAnnotations;
    public Set<String> methodLevelAnnotations;


    @Override
    public String toString() {
        return "Recorder{" +
            "interfaces=" + interfaces +
            ", superClasses=" + superClasses +
            ", classLevelAnnotations=" + classLevelAnnotations +
            ", methodLevelAnnotations=" + methodLevelAnnotations +
            '}';
    }
}

class Package {
    public Set<String> included;
    public Set<String> excluded;

    @Override
    public String toString() {
        return "Package{" +
            "included=" + included +
            ", excluded=" + excluded +
            '}';
    }
}

class Address {
    public String lines;
    public String city;
    public String state;
    public String postal;
}

class Person {
    public String given;
    public String family;
    public Address address;
}

class Invoice {
    public Set<String> packages;
    public Integer invoice; // invoice
    public String date; // date
    public Person billTo;// bill-to
    public Person shipTo;// ship-to
    public List<Product> product;
    public Float tax;
    public Float total;
    public String comments;

}

class Product {
    public String sku;
    public Integer quantity;
    public String description;
    public Float price;

    @Override
    public String toString() {
        return "Product: " + sku;
    }
}
