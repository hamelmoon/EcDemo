package com.sh.ec.entity;

public class DeviceData {

    String name;
    String add;

    public DeviceData(String name, String add) {
        this.name = name;
        this.add = add;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAdd() {
        return add;
    }

    public void setAdd(String add) {
        this.add = add;
    }

    @Override
    public String toString() {
        return "DeviceData{" +
                "name='" + name + '\'' +
                ", add='" + add + '\'' +
                '}';
    }
}
