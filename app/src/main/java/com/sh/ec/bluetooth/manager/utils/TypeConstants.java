package com.sh.ec.bluetooth.manager.utils;

import com.appdevice.domyos.DCBike;
import com.appdevice.domyos.DCEllipticalTrainer;
import com.appdevice.domyos.DCEquipment;
import com.appdevice.domyos.DCRower;
import com.appdevice.domyos.DCTreadmill;
import com.sh.ec.AppContext;
import com.sh.ec.R;

/**
 * All data type constants
 *
 * Created by mbouchagour on 18/04/2017.
 */
public class TypeConstants {

  /// Strings for Stats
  public static final String WEEKLY_INTERVAL = "weekly";
  public static final String MONTHLY_INTERVAL = "monthly";
  public static final String ANNUALLY_INTERVAL = "annually";

  // For conversion
  public static final int METER_KILOMETER_CONVERSION_INT = 1000;
  public static final int MINUTES_TO_SECONDS_CONVERSION = 60;
  public static final int TYPE_DISTANCE = 5;  // in meter
  public static final int TYPE_CALORIES = 23;  // in kilo-calories
  public static final int TYPE_DURATION = 24;  // in second
  public static final int TYPE_FREE = -1;  // in second
  private static final float METER_KILOMETER_CONVERSION = 1000f;
  private static final float IMPERIAL_COEFFICIENT = 1.609344f;

  //Sport Ids
  public static final int TYPE_SPORT_BIKE = 110;
  public static final int TYPE_SPORT_TREADMILL = 395;
  public static final int TYPE_SPORT_ELLIPTIC = 397;
  public static final int TYPE_SPORT_ROWING_MACHINE = 398;

  //Users ids
  public static final int HEIGHT_ID = 27; //Height in cm
  public static final int WEIGHT_ID = 22; //Weight in kg

  //Profile value
  public static final int MIN_WEIGHT = 15;
  public static final int AVERAGE_WEIGHT = 75;
  public static final int MAX_WEIGHT = 350;
  public static final int MIN_HEIGHT = 50;
  public static final int AVERAGE_HEIGHT = 180;
  public static final int MAX_HEIGHT = 250;

  //For HistoryService Values
  public static final int DEFAULT_HISTORY_MONTH_SEARCH_RANGE = 12;//default number of month loaded
  public static final int DEFAULT_HISTORY_MONTH_UPDATE = 6;//number of month to search more history
  public static final int DEFAULT_HISTORY_ITEM_COUNT_PER_PAGE = 12;
  // number of item to display per page
  public static final int DEFAULT_HISTORY_REMOTE_REFRESH_INDEX = 2;
  public static final String OLDEST_HISTORY_POSSIBLE_DATE = "201706";
  //list page number used to request history

  //For Stats Values
  public static final int TYPE_STATS_HEART_RATE = 1; // bpm
  public static final int TYPE_STATS_MAX_HEART_RATE = 3; // bpm
  public static final int TYPE_STATS_AVERAGE_HEART_RATE = 4; // bpm
  public static final int TYPE_STATS_SPEED = 6; // m/h
  public static final int TYPE_STATS_MAX_SPEED = 7; // m/h
  public static final int TYPE_STATS_AVERAGE_SPEED = 9; // m/h
  public static final int TYPE_STATS_AVERAGE_ELEVATION = 17; // m
  public static final int TYPE_STATS_ROTATION = 100; // rpm
  public static final int TYPE_STATS_MAX_ROTATION = 101; // rpm
  public static final int TYPE_STATS_AVERAGE_ROTATION = 103; // rpm
  public static final int TYPE_STATS_SLOPE = 121; // deg
  public static final int TYPE_STATS_MAX_SLOPE = 204; // deg
  public static final int TYPE_STATS_AVERAGE_SLOPE = 205; // deg
  public static final int TYPE_STATS_RESISTANCE = 177;
  public static final int TYPE_STATS_MAX_RESISTANCE = 206;
  public static final int TYPE_STATS_AVERAGE_RESISTANCE = 207;
  public static final int TYPE_STATS_PACE = -120;
  public static final int TYPE_STATS_AVERAGE_SPM = 214; // SPM
  public static final int TYPE_STATS_MAX_SPM = 213; // SPM
  public static final int TYPE_STATS_TIME_PER_500M = 211;// s
  public static final int TYPE_STATS_AVERAGE_TIME_PER_500M = 216;// s
  public static final int TYPE_STATS_TOTAL_STROKES = 215;
  public static final int TYPE_STATS_CURRENT_SPM = 212;// SPM
  /// ACTIVITIES TAGS
  public static final String ACTIVITY_CREATED_TAG = "DOMYOS_CONNECTED_V2";
  public static final String ACTIVITY_PROGRAM_TAG = "DCV2_PROGRAM";
  public static final String ACTIVITY_PROGRAM_PREFIX_TAG = "DCV2_PROGRAM::";
  public static final String ACTIVITY_FREERUN_TAG = "DCV2_FREERUN";
  public static final String ACTIVITY_FREERUN_CALORIE_TAG = "DCV2_FREERUN_TARGET_KCAL";
  public static final String ACTIVITY_FREERUN_DISTANCE_TAG = "DCV2_FREERUN_TARGET_KM";
  public static final String ACTIVITY_FREERUN_DURATION_TAG = "DCV2_FREERUN_TARGET_TIME";

  /// PROGRAMS TYPE
  public static final int HEALTH_PROGRAM_TYPE = 0;
  public static final int CALORIE_PROGRAM_TYPE = 1;
  public static final int ENDURANCE_PROGRAM_TYPE = 2;
  public static final int SPLIT_PROGRAM_TYPE = 3;
  public static final int LIMITED_PROGRAM_TYPE = 4;


  /// SPORT MAX POSSIBLE VALUES
  public static final int BIKE_MAX_SPEED_VALUE = 35;//km/h
  public static final int TREADMILL_MAX_SPEED_VALUE = 25;//km/h
  public static final int ELLIPTICAL_MAX_SPEED_VALUE = 12;//km/h
  public static final int ROWER_MAX_SPEED_VALUE = 600;//s
  public static final int DEFAULT_MAX_SPEED_VALUE = 35;//km/h


  /**
   * Return equipment value depending on DCEquipment
   */
  public static int getEquipmentType(DCEquipment equipment) {
    if (equipment instanceof DCBike) {
      return TYPE_SPORT_BIKE;
    } else if (equipment instanceof DCTreadmill) {
      return TYPE_SPORT_TREADMILL;
    } else if (equipment instanceof DCEllipticalTrainer) {
      return TYPE_SPORT_ELLIPTIC;
    } else if (equipment instanceof DCRower) {
      return TYPE_SPORT_ROWING_MACHINE;
    }
    return -1;
  }

  /**
   * Return the string unit from type
   */
  public static String transformTypeObjectiveToStringResources(int type, boolean isMetricUnit) {
    switch (type) {
      case TYPE_DISTANCE:
        return returnDistanceUnit(isMetricUnit);
      case TYPE_CALORIES:
        return AppContext.getInstance().getString(R.string.calorie_qianka_txt);
      case TYPE_DURATION:
        return AppContext.getInstance().getString(R.string.minute_txt);
      default:
        return "";
    }
  }

/*  *//**
   * Return the drawable from type
   *//*
  public static Drawable returnImageIdFromType(int type) {
    switch (type) {
      case TYPE_DISTANCE:
        return ContextCompat.getDrawable(AndroidApplication.getContext(), R.drawable.ic_kilometer);
      case TYPE_CALORIES:
        return ContextCompat.getDrawable(AndroidApplication.getContext(), R.drawable.ic_calorie);
      case TYPE_DURATION:
        return ContextCompat.getDrawable(AndroidApplication.getContext(), R.drawable.ic_time);
      default:
        return null;
    }
  }

  *//**
   * Return the icon in function of the params
   *//*
  public static int returnImageIntIdFromType(int categoryProgram) {
    switch (categoryProgram) {
      case TYPE_DISTANCE:
        return R.drawable.ic_kilometer;
      case TYPE_CALORIES:
        return R.drawable.ic_calorie;
      case TYPE_DURATION:
        return R.drawable.ic_time;
      default:
        return R.drawable.ic_time;//default value if an error occur
    }
  }*/

  /**
   * Method to convert data (ex : distance in meter into kilometer/miles)
   */
  public static int convertData(int type, int value, boolean isMetricUnit) {
    switch (type) {
      case TYPE_STATS_MAX_SPEED:
      case TYPE_STATS_AVERAGE_SPEED:
      case TYPE_DISTANCE:
        float result = value / METER_KILOMETER_CONVERSION;

        if (!isMetricUnit) {
          result = result / IMPERIAL_COEFFICIENT;
        }
        return Math.round(result);
      default:
        return 0;
    }
  }

  public static float convertData(int type, float value, boolean isMetricUnit) {
    switch (type) {
      case TYPE_STATS_MAX_SPEED:
      case TYPE_STATS_AVERAGE_SPEED:
      case TYPE_DISTANCE:
        float result = value / METER_KILOMETER_CONVERSION;

        if (!isMetricUnit) {
          result = result / IMPERIAL_COEFFICIENT;
        }
        return result;
      default:
        return 0.0f;
    }
  }

  /**
   * Method to return the distance unit (from imperial or metric system)
   */
  public static String returnDistanceUnit(boolean isMetricUnit) {
    return isMetricUnit ? AppContext.getInstance().getString(R.string.kilometer_txt)
      :  AppContext.getInstance().getString(R.string.distance_imperial_unit_lowercase);
  }

  /**
   * Method to convert data (ex : distance in meter into kilometer/miles)
   */
  public static float convertDataFloat(int type, float value, boolean isMetricUnit) {
    switch (type) {
      case TYPE_STATS_MAX_SPEED:
      case TYPE_STATS_AVERAGE_SPEED:
      case TYPE_DISTANCE:
        float result = value / METER_KILOMETER_CONVERSION;

        if (!isMetricUnit) {
          result = result / IMPERIAL_COEFFICIENT;
        }
        return result;
      default:
        return 0f;
    }
  }

  /**
   * Convert kilometers in meters
   */
  public static float convertInMeters(float kilometers) {
    return kilometers * METER_KILOMETER_CONVERSION;
  }

  /**
   * Convert meters in kilometers
   */
  public static float convertInKm(float meters) {
    return meters / METER_KILOMETER_CONVERSION;
  }

  /**
   * Convert km/h to mph
   */
  public static float convertKilometerPerHourToMilePerHour(float kilometerToHour) {
    return kilometerToHour / IMPERIAL_COEFFICIENT;
  }

  /**
   * Convert miles to kilometers
   */
  public static float convertMileToKilometer(float miles) {
    return miles * IMPERIAL_COEFFICIENT;
  }

  /**
   * Returns the number of page corresponding to the given history entry number
   */
  public static int getHistoryPageNumber(int historyCount) {
    int lowerApprox = (int) Math.floor(historyCount / (double) DEFAULT_HISTORY_ITEM_COUNT_PER_PAGE);
    int error = (int) Math.ceil((historyCount - lowerApprox * DEFAULT_HISTORY_ITEM_COUNT_PER_PAGE)
      / ((double) DEFAULT_HISTORY_ITEM_COUNT_PER_PAGE));
    return lowerApprox + error;
  }

  /*
   * Return the icon in function of the params
  public static Drawable getImageFromCategoryProgram(int categoryProgram) {
    switch (categoryProgram) {
      case HEALTH_PROGRAM_TYPE:
        return ContextCompat.getDrawable(AndroidApplication.getContext(),
          R.drawable.ic_program_health);
      case CALORIE_PROGRAM_TYPE:
        return ContextCompat.getDrawable(AndroidApplication.getContext(),
          R.drawable.ic_program_calorie_burn);
      case ENDURANCE_PROGRAM_TYPE:
        return ContextCompat.getDrawable(AndroidApplication.getContext(),
          R.drawable.ic_program_endurance);
      case SPLIT_PROGRAM_TYPE:
        return ContextCompat.getDrawable(AndroidApplication.getContext(),
          R.drawable.ic_program_split);
      case LIMITED_PROGRAM_TYPE:
        return ContextCompat.getDrawable(AndroidApplication.getContext(),
          R.drawable.ic_program_limited);
      default:
        return null;
    }
  }

  /**
   * Return the icon in function of the params
   */
/*  public static int getImageIntFromCategoryProgram(int categoryProgram) {
    switch (categoryProgram) {
      case HEALTH_PROGRAM_TYPE:
        return R.drawable.ic_program_health;
      case CALORIE_PROGRAM_TYPE:
        return R.drawable.ic_program_calorie_burn;
      case ENDURANCE_PROGRAM_TYPE:
        return R.drawable.ic_program_endurance;
      case SPLIT_PROGRAM_TYPE:
        return R.drawable.ic_program_split;
      case LIMITED_PROGRAM_TYPE:
        return R.drawable.ic_program_limited;
      default:
        return -1;
    }
  }*/

  public static boolean contain(int[] array, int value) {
    for (int currentValue : array) {
      if (value == currentValue) {
        return true;
      }
    }
    return false;
  }
}
