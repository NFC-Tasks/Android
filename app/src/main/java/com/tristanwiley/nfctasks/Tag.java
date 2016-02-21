package com.tristanwiley.nfctasks;

import java.util.List;

/**
 * Created by Tristan on 2/20/2016.
 */
public class Tag {
    String name;
    List<Operation> operations;

    public Tag(String name, List<Operation> operations) {
        this.name = name;
        this.operations = operations;
    }

    public String getName() {
        return name;
    }

    public List<Operation> getOperations() {
        return operations;
    }

    public void addOperation(Operation operation) {
        operations.add(operation);
    }
}
