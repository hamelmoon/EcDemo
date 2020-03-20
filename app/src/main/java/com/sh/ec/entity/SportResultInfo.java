package com.sh.ec.entity;

/**
 * 运动后的数据
 */
public class SportResultInfo  {

    private int machineType;
    private String machineName;
    private float distance;
    private float calorie;
    private float avg_speed;
    private float avg_pace;
    private long pace;
    private int heart_rate;
    private int heart_rate_max;
    private float incline_max;
    private long time;
    private int rpm;
    private int resistance;
    private float speed;

    public float getSpeed() {
        return speed;
    }

    public void setSpeed(float speed) {
        this.speed = speed;
    }

    public int getRpm() {
        
        return rpm;
    }
    public void setRpm(int rpm) {
        this.rpm = rpm;
    }

    public int getResistance() {
        return resistance;
    }

    public void setResistance(int resistance) {
        this.resistance = resistance;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public int getMachineType() {
        return machineType;
    }

    public void setMachineType(int machineType) {
        this.machineType = machineType;
    }

    public String getMachineName() {
        return machineName;
    }

    public void setMachineName(String machineName) {
        this.machineName = machineName;
    }

    public float getDistance() {
        return distance;
    }

    public void setDistance(float distance) {
        this.distance = distance;
    }

    public float getCalorie() {
        return calorie;
    }

    public void setCalorie(float calorie) {
        this.calorie = calorie;
    }

    public float getAvg_speed() {
        return avg_speed;
    }

    public void setAvg_speed(float avg_speed) {
        this.avg_speed = avg_speed;
    }

    public float getAvg_pace() {
        return avg_pace;
    }

    public void setAvg_pace(float avg_pace) {
        this.avg_pace = avg_pace;
    }

    public long getPace() {
        return pace;
    }

    public void setPace(long pace) {
        this.pace = pace;
    }

    public int getHeart_rate() {
        return heart_rate;
    }

    public void setHeart_rate(int heart_rate) {
        this.heart_rate = heart_rate;
    }

    public int getHeart_rate_max() {
        return heart_rate_max;
    }

    public void setHeart_rate_max(int heart_rate_max) {
        this.heart_rate_max = heart_rate_max;
    }

    public float getIncline_max() {
        return incline_max;
    }

    public void setIncline_max(float incline_max) {
        this.incline_max = incline_max;
    }


}
