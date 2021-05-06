package com.pfizer.equip.services.input.dataframe;

public class KVP {
    private String name;
    private String value;

    public KVP(String name, String value) {
        this.name = name;
        this.value = value;
    }

    public KVP() {}

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
