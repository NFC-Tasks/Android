package com.tristanwiley.nfctasks;

import java.util.List;

/**
 * Created by Tristan on 2/20/2016.
 */
public class Operation {
    String type;
    List<String> dataForTask;

    public Operation(String type, List<String> dataForTask) {
        this.type = type;
        this.dataForTask = dataForTask;
    }

    public String getType() {
        return type;
    }

    public List<String> getDataForTask() {
        return dataForTask;
    }

    public void addTask(String task){
        dataForTask.add(task);
    }
}
