package com.sh.ec.entity;


public class EquipmentInfo {
    private int maxSpeed;
    private int maxResistance;
    private int maxInclinePercentage;
    private int minResistance;
    private int minInclinePercentage;
    private String serialNumber;
    private int modelId = -1;
    private float firmwareVersion;

    public String getSerialNumber() {
        return serialNumber;
    }

    public void setSerialNumber(String serialNumber) {
        this.serialNumber = serialNumber;
    }

    public int getModelId() {
        return modelId;
    }

    public void setModelId(int modelId) {
        this.modelId = modelId;
    }

    public float getFirmwareVersion() {
        return firmwareVersion;
    }

    public void setFirmwareVersion(float firmwareVersion) {
        this.firmwareVersion = firmwareVersion;
    }

    public int getMaxSpeed() {
        return maxSpeed;
    }

    public void setMaxSpeed(int maxSpeed) {
        this.maxSpeed = maxSpeed;
    }

    public int getMaxResistance() {
        return maxResistance;
    }

    public void setMaxResistance(int maxResistance) {
        this.maxResistance = maxResistance;
    }

    public int getMinResistance() {
        return minResistance;
    }

    public void setMinResistance(int minResistance) {
        this.minResistance = minResistance;
    }

    public int getMinInclinePercentage() {
        return minInclinePercentage;
    }

    public void setMinInclinePercentage(int minInclinePercentage) {
        this.minInclinePercentage = minInclinePercentage;
    }

    public int getMaxInclinePercentage() {
        return maxInclinePercentage;
    }

    public void setMaxInclinePercentage(int maxInclinePercentage) {
        this.maxInclinePercentage = maxInclinePercentage;
    }
}
