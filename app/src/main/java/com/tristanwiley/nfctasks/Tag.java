package com.tristanwiley.nfctasks;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import java.util.List;

/**
 * an NFC tag.
 *
 * Created by Tristan on 2/20/2016.
 */
public class Tag {
    private String name;
    private Object thermostat_task;
    private Object weather_task;

    public Tag() {

    }

    public Tag(String name) {
        this.name = name;
    }

    public Tag(String name, Object thermostat_task, Object weather_task) {
        this.name = name;
        this.thermostat_task = thermostat_task;
        this.weather_task = weather_task;
    }

    public String getName() {
        return name;
    }

    public Object getThermostat_task() {
        return thermostat_task;
    }

    public Object getWeather_task() {
        return weather_task;
    }
}
