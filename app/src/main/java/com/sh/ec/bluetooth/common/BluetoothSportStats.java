package com.sh.ec.bluetooth.common;

/**
 * BluetoothSportStats model in domain layer
 *
 * Created by mbouchagour on 18/04/2017.
 */
public class BluetoothSportStats {
  private int rpm = 0;
  private float resistance = 0;
  private int analogHeartRate = 0;
  private float inclinePercentage = 0;
  private float speedKmPerHour = 0;
  private float currentSessionCumulativeKM = 0;
  private float sessionAverageSpeedChanged = 0;
  private float kcalPerHour = 0;
  private int currentCumulativeCount = 0;
  private int timePer500M = 0;

  public int getTimePer500M() {
    return timePer500M;
  }

  public void setTimePer500M(int timePer500M) {
    this.timePer500M = timePer500M;
  }

  public int getCurrentCumulativeCount() {
    return currentCumulativeCount;
  }

  public void setCurrentCumulativeCount(int currentCumulativeCount) {
    this.currentCumulativeCount = currentCumulativeCount;
  }

  public float getKcalPerHour() {
    return kcalPerHour;
  }

  public void setKcalPerHour(float kcalPerHour) {
    this.kcalPerHour = kcalPerHour;
  }

  public BluetoothSportStats() {
    //empty constructor
  }

  public int getRpm() {
    return rpm;
  }

  public void setRpm(int rpm) {
    this.rpm = rpm;
  }

  public float getResistance() {
    return resistance;
  }

  public void setResistance(float resistance) {
    this.resistance = resistance;
  }

  public int getAnalogHeartRate() {
    return analogHeartRate;
  }

  public void setAnalogHeartRate(int analogHeartRate) {
    this.analogHeartRate = analogHeartRate;
  }

  public float getInclinePercentage() {
    return inclinePercentage;
  }

  public void setInclinePercentage(float inclinePercentage) {
    this.inclinePercentage = inclinePercentage;
  }

  public float getSpeedKmPerHour() {
    return speedKmPerHour;
  }

  public void setSpeedKmPerHour(float speedKmPerHour) {
    this.speedKmPerHour = speedKmPerHour;
  }

  public float getCurrentSessionCumulativeKM() {
    return currentSessionCumulativeKM;
  }

  public void setCurrentSessionCumulativeKM(float currentSessionCumulativeKM) {
    this.currentSessionCumulativeKM = currentSessionCumulativeKM;
  }

  public float getSessionAverageSpeedChanged() {
    return sessionAverageSpeedChanged;
  }

  public void setSessionAverageSpeedChanged(float sessionAverageSpeedChanged) {
    this.sessionAverageSpeedChanged = sessionAverageSpeedChanged;
  }
}
