package org.finos.symphony.toolkit.workflow.fixture;

import java.util.List;

public class Person {
    private List<String> names;
    private List<Address> addresses;

    public Person() {
    }

    public Person(List<String> names, List<Address> addresses) {
        this.names = names;
        this.addresses = addresses;
    }

    public Person(List<String> names) {
        this.names = names;
    }

    public List<String> getNames() {
        return names;
    }

    public void setNames(List<String> names) {
        this.names = names;
    }

    public List<Address> getAddresses() {
        return addresses;
    }

    public void setAddresses(List<Address> addresses) {
        this.addresses = addresses;
    }
}
