package com.sh.ec.bluetooth.manager.utils;


public enum DCUnit {
  CURRENT_HEART_RATE("BPM", TypeConstants.TYPE_STATS_HEART_RATE),  //
  MAX_HEART_RATE("BPM", TypeConstants.TYPE_STATS_MAX_HEART_RATE),  //
  AVERAGE_HEART_RATE("BPM", TypeConstants.TYPE_STATS_AVERAGE_HEART_RATE),  //
  DISTANCE("m", TypeConstants.TYPE_DISTANCE),  //
  CURRENT_SPEED("m/h", TypeConstants.TYPE_STATS_SPEED), //
  MAX_SPEED("m/h", TypeConstants.TYPE_STATS_MAX_SPEED),  //
  AVERAGE_SPEED("m/h", TypeConstants.TYPE_STATS_AVERAGE_SPEED),  //
  ELEVATION("m", TypeConstants.TYPE_STATS_AVERAGE_ELEVATION),  //
  WEIGHT("kg", TypeConstants.WEIGHT_ID), //
  KCAL_BURNT("Kcal", TypeConstants.TYPE_CALORIES),  //
  DURATION("s", TypeConstants.TYPE_DURATION),  //
  HEIGHT("cm", TypeConstants.HEIGHT_ID),  //
  CURRENT_ROTATION("RPM", TypeConstants.TYPE_STATS_ROTATION),  //
  MAX_ROTATION("RPM", TypeConstants.TYPE_STATS_MAX_ROTATION),  //
  AVERAGE_ROTATION("RPM", TypeConstants.TYPE_STATS_AVERAGE_ROTATION),  //
  SLOPE_DEVICE("Deg", TypeConstants.TYPE_STATS_SLOPE),  //
  MAX_SLOPE("Deg", TypeConstants.TYPE_STATS_MAX_SLOPE), //
  AVERAGE_SLOPE("Deg", TypeConstants.TYPE_STATS_AVERAGE_SLOPE),  //
  RESISTANCE("%", TypeConstants.TYPE_STATS_RESISTANCE), //
  MAX_RESISTANCE("%", TypeConstants.TYPE_STATS_MAX_RESISTANCE), //
  AVERAGE_RESISTANCE("%", TypeConstants.TYPE_STATS_AVERAGE_RESISTANCE),  //
  TIME_PER_500M("s", TypeConstants.TYPE_STATS_TIME_PER_500M),//
  AVERAGE_TIME_PER_500M("s", TypeConstants.TYPE_STATS_AVERAGE_TIME_PER_500M),//
  TOTAL_STROKES("", TypeConstants.TYPE_STATS_TOTAL_STROKES),//
  CURRENT_SPM("SPM", TypeConstants.TYPE_STATS_CURRENT_SPM),//
  AVERAGE_SPM("SPM", TypeConstants.TYPE_STATS_AVERAGE_SPM),//
  MAX_SPM("SPM", TypeConstants.TYPE_STATS_MAX_SPM),//
  UNKNOWN("?", -1);

  private final String unitString;
  private final int unitId;

  DCUnit(String unitString, int unitId) {
    this.unitId = unitId;
    this.unitString = unitString;
  }

  public String getUnitString() {
    return unitString;
  }

  public int getUnitId() {
    return unitId;
  }

  public static DCUnit getValueForUnitId(int unitId) {
    for (DCUnit value : DCUnit.values()) {
      if (value.unitId == unitId) {
        return value;
      }
    }
    return UNKNOWN;
  }
}
