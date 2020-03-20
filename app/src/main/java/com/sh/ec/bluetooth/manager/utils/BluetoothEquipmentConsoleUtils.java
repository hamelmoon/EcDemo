package com.sh.ec.bluetooth.manager.utils;

import android.util.SparseArray;

import com.appdevice.domyos.DCBike;
import com.appdevice.domyos.DCCompletionBlock;
import com.appdevice.domyos.DCCompletionBlockWithError;
import com.appdevice.domyos.DCEllipticalTrainer;
import com.appdevice.domyos.DCEquipment;
import com.appdevice.domyos.DCRower;
import com.appdevice.domyos.DCTreadmill;
import com.appdevice.domyos.parameters.DCArithmeticDisplayZone1Parameter;
import com.appdevice.domyos.parameters.DCArithmeticDisplayZoneOtherParameter;
import com.appdevice.domyos.parameters.DCDisplayZoneParameters;
import com.appdevice.domyos.parameters.DCEquipmentDisplayZoneSecondAreaParameters;
import com.appdevice.domyos.parameters.DCStringDisplayZone1Parameter;
import com.appdevice.domyos.parameters.bike.DCBikeDisplayZoneParameters;
import com.appdevice.domyos.parameters.et.DCEllipticalTrainerDisplayZoneParameters;
import com.appdevice.domyos.parameters.treadmill.DCTreadmillDisplayZoneParameters;
import com.sh.ec.AppContext;
import com.sh.ec.R;
import com.sh.ec.bluetooth.manager.BluetoothManager;

/**
 * Created by mbouchagour on 19/06/2018.
 */
public final class BluetoothEquipmentConsoleUtils {

  /**
   * Private constructor because this is a class of utils method
   */
  private BluetoothEquipmentConsoleUtils() {
      //Do nothing
  }

  private static final int[] coreBikeConsoleNewDisplayIdList =
    AppContext.getInstance().getResources().getIntArray(R.array.consoleNewDisplayIdList);

  public static final SparseArray<Float> consoleNewDisplayIdVersionMap = new SparseArray<Float>() {
    {
      //TC
      put(84202, 2.4f);
      put(8396836, 3.2f);

      //BIKES
      put(8363074, 1.7f);
      put(8369237, 1.7f);
      put(8368167, 1.7f);
      put(8369244, 1.7f);
      put(8368168, 1.7f);
      put(8369246, 1.7f);
      put(8369247, 1.2f);
      put(8369248, 1.2f);
      put(2100001, 1.2f);
      put(2100002, 1.2f);
      put(2100003, 1.2f);
      put(2100004, 1.2f);

      //TC
      put(8389706, 2.2f);
      put(8389707, 2.2f);
      put(9000001, 3.0f);
      put(9000002, 3.0f);
      put(5400001, 1.6f);
      put(5400002, 1.6f);

      //ROW
      put(3100001, 1.1f);
      put(3100002, 1.1f);
    }
  };

  public static final SparseArray<Integer> consoleModelIdMap = new SparseArray<Integer>() {
    {
      //TC
      put(84202, 75);
      put(8396836,76);

      //BIKES
      put(8363074, 77);
      put(8369237, 78);
      put(8368167, 79);
      put(8369244, 80);
      put(8368168, 81);
      put(8369246, 82);
      put(8369247, 83);
      put(8369248, 84);
      put(2100001, 91);
      put(2100002, 92);
      put(2100003, 95);
      put(2100004, 96);

      //TC
      put(8389706, 85);
      put(8389707, 86);
      put(9000001, 87);
      put(9000002, 88);
      put(5400001, 89);
      put(5400002, 90);

      //ROW
      put(3100001, 93);
      put(3100002, 94);
    }
  };

  private static final int[] treadmillConsoleSpecificZoneIdList =
    AppContext.getInstance().getResources().getIntArray(R.array.treadmillSpecificZoneIdList);

  private static final DCUnit[] treadmillAvailableZones = new DCUnit[] {
    DCUnit.CURRENT_HEART_RATE, DCUnit.SLOPE_DEVICE, DCUnit.CURRENT_SPEED, DCUnit.DISTANCE,
    DCUnit.KCAL_BURNT
  };

  private static final DCUnit[] coreBikesAvailableZonesToInitialize = new DCUnit[] {
    DCUnit.CURRENT_HEART_RATE, DCUnit.KCAL_BURNT, DCUnit.DISTANCE, DCUnit.CURRENT_SPEED,
    DCUnit.CURRENT_ROTATION
  };

  private static final DCUnit[] rowerAvailableZones = new DCUnit[]{
    DCUnit.CURRENT_HEART_RATE, DCUnit.KCAL_BURNT, DCUnit.DISTANCE, DCUnit.TIME_PER_500M, DCUnit.TOTAL_STROKES, DCUnit.CURRENT_SPM
  };

  private static final int TREADMILL_BPM_ZONE_INDEX = 1;
  private static final int TREADMILL_INCLINE_PERCENTAGE_ZONE_INDEX = 4;
  private static final int TREADMILL_SPEED_ZONE_INDEX = 5;
  private static final int TREADMILL_BPM_SPECIFIC_ZONE_INDEX = 3;
  private static final int TREADMILL_KCAL_SPECIFIC_ZONE_INDEX = 2;
  private static final int TREADMILL_DISTANCE_SPECIFIC_ZONE_INDEX = 6;

  private static final int CORE_BIKE_BPM_ZONE_INDEX = 1;
  private static final int CORE_BIKE_RPM_ZONE_INDEX = 4;
  private static final int CORE_BIKE_RESISTANCE_ZONE_INDEX = 6;
  private static final int CORE_BIKE_SPECIFIC_BPM_ZONE_INDEX = 3;
  private static final int CORE_BIKE_SPECIFIC_DISTANCE_ZONE_INDEX = 7;
  private static final int CORE_BIKE_SPECIFIC_SPEED_ZONE_INDEX = 2;
  private static final int CORE_BIKE_SPECIFIC_CALORIES_ZONE_INDEX = 5;

  private static final int ROWER_BPM_ZONE_INDEX = 1;
  private static final int ROWER_SPM_ZONE_INDEX = 4;
  private static final int ROWER_RESISTANCE_ZONE_INDEX = 6;
  private static final int ROWER_CALORIES_ZONE_INDEX = 5;

  public static void initializeZoneInformations(Integer sportId, DCEquipment equipment,
                                                Integer equipmentId, float equipmentVersion, boolean started, float value,
                                                DCCompletionBlock completionBlock, DCCompletionBlockWithError errorBlock) {
    DCStringDisplayZone1Parameter goDisplay;
    if (started) {
      if (equipmentVersion > BluetoothManager.EQUIPMENT_FIRMWARE_1_5 || equipmentVersion >= consoleNewDisplayIdVersionMap.get(equipmentId)) {
        goDisplay = new DCStringDisplayZone1Parameter(6);//Display GO on console
      }else{
        goDisplay = new DCStringDisplayZone1Parameter(8);//Display GO on old console
      }
    } else {
      goDisplay = new DCStringDisplayZone1Parameter(8);//Display BLUETOOTH on console
    }
    switch (sportId) {
      case TypeConstants.TYPE_SPORT_TREADMILL:
        DCTreadmillDisplayZoneParameters treadmillDisplayZoneParameters =
            new DCTreadmillDisplayZoneParameters();
        for (DCUnit dcUnit : treadmillAvailableZones) {
          prepareTreadmillZoneDisplay(treadmillDisplayZoneParameters, equipmentId, dcUnit,0f);
        }
        treadmillDisplayZoneParameters.setDisplayZone1Parameter(goDisplay);
        sendDisplayParamsToTreadmill((DCTreadmill) equipment, treadmillDisplayZoneParameters,
            completionBlock, errorBlock);
        break;
      case TypeConstants.TYPE_SPORT_BIKE:
        DCBikeDisplayZoneParameters displayBikeZoneParameters =
            new DCBikeDisplayZoneParameters();
        displayBikeZoneParameters.setDisplayZone1Parameter(goDisplay);
        for (DCUnit dcUnit : coreBikesAvailableZonesToInitialize) {
          prepareBikeZoneDisplay(displayBikeZoneParameters, equipmentId, equipmentVersion, dcUnit, value);
        }
        sendDisplayParamsToBike((DCBike) equipment, displayBikeZoneParameters, completionBlock, errorBlock);
        if ((TypeConstants.contain(coreBikeConsoleNewDisplayIdList,
            equipmentId) || equipmentVersion >= consoleNewDisplayIdVersionMap.get(equipmentId))) {
          DCEquipmentDisplayZoneSecondAreaParameters dCEquipmentDisplayZoneSecondAreaParameters =
              new DCEquipmentDisplayZoneSecondAreaParameters();
          dCEquipmentDisplayZoneSecondAreaParameters.setDisplayZone7Parameter(
              new DCArithmeticDisplayZoneOtherParameter(DCArithmeticDisplayZoneOtherParameter.DCDisplayZoneOtherArithmeticTypeOneDecimalPlace,
                  value));
          if (BluetoothManager.isBluetoothPhoneEnabled()
              && equipment.getConnectionState() == DCEquipment.DCEquipmentConnectionStateConnected) {
            (equipment).setDisplayZonesSecondArea(dCEquipmentDisplayZoneSecondAreaParameters,
                completionBlock, errorBlock);
          }
        }
        break;
      case TypeConstants.TYPE_SPORT_ELLIPTIC:
        DCEllipticalTrainerDisplayZoneParameters displayZoneParameters =
            new DCEllipticalTrainerDisplayZoneParameters();
        displayZoneParameters.setDisplayZone1Parameter(goDisplay);
        for (DCUnit dcUnit : coreBikesAvailableZonesToInitialize) {
          prepareEllipticalDisplay(displayZoneParameters, equipmentId, equipmentVersion, dcUnit, value);
        }
        sendDisplayParamsToEllipticalBike((DCEllipticalTrainer) equipment, displayZoneParameters, completionBlock, errorBlock);
        if ((TypeConstants.contain(coreBikeConsoleNewDisplayIdList,
            equipmentId) || equipmentVersion >= consoleNewDisplayIdVersionMap.get(equipmentId))) {
          DCEquipmentDisplayZoneSecondAreaParameters dCEquipmentDisplayZoneSecondAreaParameters =
              new DCEquipmentDisplayZoneSecondAreaParameters();
          dCEquipmentDisplayZoneSecondAreaParameters.setDisplayZone7Parameter(
              new DCArithmeticDisplayZoneOtherParameter(DCArithmeticDisplayZoneOtherParameter.DCDisplayZoneOtherArithmeticTypeOneDecimalPlace,
                  value));
          if (BluetoothManager.isBluetoothPhoneEnabled()
              && equipment.getConnectionState() == DCEquipment.DCEquipmentConnectionStateConnected) {
            (equipment).setDisplayZonesSecondArea(dCEquipmentDisplayZoneSecondAreaParameters,
                completionBlock, errorBlock);
          }
        }
        break;
      case TypeConstants.TYPE_SPORT_ROWING_MACHINE:
        DCDisplayZoneParameters displayRowerZoneParameters =
            new DCDisplayZoneParameters();
        displayRowerZoneParameters.setDisplayZone1Parameter(goDisplay);
        for (DCUnit dcUnit : rowerAvailableZones) {
          prepareRowerZoneDisplay(displayRowerZoneParameters, dcUnit, 0);
        }
        sendDisplayParamsToRower((DCRower) equipment, displayRowerZoneParameters, completionBlock, errorBlock);
        break;
      default:
        break;
    }
  }

  public static void displayZoneInformation(Integer sportId, DCEquipment equipment,
                                            Integer equipmentId, float equipmentVersion, DCUnit zoneTriggered, boolean started, float value,
                                            DCCompletionBlock completionBlock, DCCompletionBlockWithError errorBlock) {
    switch (sportId) {
      case TypeConstants.TYPE_SPORT_TREADMILL:
        displayTreadmillZoneInformations((DCTreadmill) equipment, equipmentId, equipmentVersion,
          zoneTriggered, started, value, completionBlock, errorBlock);
        break;
      case TypeConstants.TYPE_SPORT_BIKE:
        displayBikeZoneInformations(sportId, equipment, equipmentId, equipmentVersion,
          zoneTriggered, started, value, completionBlock, errorBlock);
        break;
      case TypeConstants.TYPE_SPORT_ELLIPTIC:
        displayBikeZoneInformations(sportId, equipment, equipmentId, equipmentVersion,
          zoneTriggered, started, value, completionBlock, errorBlock);
        break;
      case TypeConstants.TYPE_SPORT_ROWING_MACHINE:
        displayRowerZoneInformations(sportId, equipment, equipmentId, equipmentVersion,
          zoneTriggered, started, value, completionBlock, errorBlock);
        break;
      default:
        break;
    }
  }

  public static void displayMainMessage(Integer sportId, DCEquipment equipment, int equipmentId,
                                        float equipmentVersion, boolean started, DCCompletionBlock completionBlock,
                                        DCCompletionBlockWithError errorBlock) {
    DCStringDisplayZone1Parameter goDisplay;
    if (started) {
      if (equipmentVersion > BluetoothManager.EQUIPMENT_FIRMWARE_1_5 || equipmentVersion >= consoleNewDisplayIdVersionMap.get(equipmentId)) {
        goDisplay = new DCStringDisplayZone1Parameter(6);//Display GO on console
      }else{
        goDisplay = new DCStringDisplayZone1Parameter(8);//Display GO on old console
      }
    } else {
        goDisplay = new DCStringDisplayZone1Parameter(8);//Display BLUETOOTH on console
    }
    switch (sportId) {
      case TypeConstants.TYPE_SPORT_TREADMILL:
        DCTreadmillDisplayZoneParameters treadmillDisplayZoneParameters =
              new DCTreadmillDisplayZoneParameters();
        treadmillDisplayZoneParameters.setDisplayZone1Parameter(goDisplay);
        sendDisplayParamsToTreadmill((DCTreadmill) equipment, treadmillDisplayZoneParameters,
                completionBlock, errorBlock);
        break;
      case TypeConstants.TYPE_SPORT_BIKE:
        DCBikeDisplayZoneParameters displayBikeZoneParameters = new DCBikeDisplayZoneParameters();
        displayBikeZoneParameters.setDisplayZone1Parameter(goDisplay);
        sendDisplayParamsToBike((DCBike) equipment, displayBikeZoneParameters, completionBlock,
          errorBlock);
        break;
      case TypeConstants.TYPE_SPORT_ELLIPTIC:
        DCEllipticalTrainerDisplayZoneParameters displayElZoneParameters =
          new DCEllipticalTrainerDisplayZoneParameters();
        displayElZoneParameters.setDisplayZone1Parameter(goDisplay);
        sendDisplayParamsToEllipticalBike((DCEllipticalTrainer) equipment, displayElZoneParameters,
          completionBlock, errorBlock);
        break;

      case TypeConstants.TYPE_SPORT_ROWING_MACHINE:
        DCDisplayZoneParameters displayRowZoneParameters =
          new DCDisplayZoneParameters();
        displayRowZoneParameters.setDisplayZone1Parameter(goDisplay);
        sendDisplayParamsToRower((DCRower) equipment, displayRowZoneParameters,
          completionBlock, errorBlock);
        break;
      default:
        //empty
        break;
    }
  }

  public static void initConsoleDisplay(Integer sportId, DCEquipment equipment, int equipmentId,
                                        float equipmentVersion, boolean started, DCCompletionBlock completionBlock,
                                        DCCompletionBlockWithError errorBlock, boolean cleared, float distance, float kcal) {
    DCStringDisplayZone1Parameter goDisplay;
    if (started) {
      if (equipmentVersion > BluetoothManager.EQUIPMENT_FIRMWARE_1_5 || equipmentVersion >= consoleNewDisplayIdVersionMap.get(equipmentId)) {
        goDisplay = new DCStringDisplayZone1Parameter(6);//Display GO on console
      }else{
        goDisplay = new DCStringDisplayZone1Parameter(8);//Display GO on old console
      }
    } else {
      goDisplay = new DCStringDisplayZone1Parameter(8);//Display BLUETOOTH on console
    }
    switch (sportId) {
      case TypeConstants.TYPE_SPORT_TREADMILL:
        DCTreadmillDisplayZoneParameters treadmillDisplayZoneParameters =
            new DCTreadmillDisplayZoneParameters();
        for (DCUnit dcUnit : treadmillAvailableZones) {
          prepareTreadmillZoneDisplay(treadmillDisplayZoneParameters, equipmentId, dcUnit,0f);
        }
        treadmillDisplayZoneParameters.setDisplayZone1Parameter(goDisplay);
        sendDisplayParamsToTreadmill((DCTreadmill) equipment, treadmillDisplayZoneParameters,
            completionBlock, errorBlock);
        break;
      case TypeConstants.TYPE_SPORT_BIKE:
        DCBikeDisplayZoneParameters displayBikeZoneParameters =
            new DCBikeDisplayZoneParameters();

        displayBikeZoneParameters.setDisplayZone1Parameter(goDisplay);
        if(!cleared) {
          prepareBikeZoneDisplay(displayBikeZoneParameters, equipmentId, equipmentVersion, DCUnit.DISTANCE, distance);
          prepareBikeZoneDisplay(displayBikeZoneParameters, equipmentId, equipmentVersion, DCUnit.KCAL_BURNT, kcal);
        }
        sendDisplayParamsToBike((DCBike) equipment, displayBikeZoneParameters, completionBlock, errorBlock);
        if ((TypeConstants.contain(coreBikeConsoleNewDisplayIdList,
            equipmentId) || equipmentVersion >= consoleNewDisplayIdVersionMap.get(equipmentId))) {
          DCEquipmentDisplayZoneSecondAreaParameters dCEquipmentDisplayZoneSecondAreaParameters =
              new DCEquipmentDisplayZoneSecondAreaParameters();
          dCEquipmentDisplayZoneSecondAreaParameters.setDisplayZone7Parameter(
              new DCArithmeticDisplayZoneOtherParameter(DCArithmeticDisplayZoneOtherParameter.DCDisplayZoneOtherArithmeticTypeOneDecimalPlace,
                  distance));
          if (BluetoothManager.isBluetoothPhoneEnabled()
              && equipment.getConnectionState() == DCEquipment.DCEquipmentConnectionStateConnected) {
            (equipment).setDisplayZonesSecondArea(dCEquipmentDisplayZoneSecondAreaParameters,
                completionBlock, errorBlock);
          }
        }
        break;
      case TypeConstants.TYPE_SPORT_ELLIPTIC:
        DCEllipticalTrainerDisplayZoneParameters displayElZoneParameters =
            new DCEllipticalTrainerDisplayZoneParameters();
        displayElZoneParameters.setDisplayZone1Parameter(goDisplay);
        if(!cleared) {
          prepareEllipticalDisplay(displayElZoneParameters, equipmentId, equipmentVersion, DCUnit.DISTANCE, distance);
          prepareEllipticalDisplay(displayElZoneParameters, equipmentId, equipmentVersion, DCUnit.KCAL_BURNT, kcal);
        }
        sendDisplayParamsToEllipticalBike((DCEllipticalTrainer) equipment, displayElZoneParameters, completionBlock, errorBlock);

        if ((TypeConstants.contain(coreBikeConsoleNewDisplayIdList,
            equipmentId) || equipmentVersion >= consoleNewDisplayIdVersionMap.get(equipmentId))) {
          DCEquipmentDisplayZoneSecondAreaParameters dCEquipmentDisplayZoneSecondAreaParameters =
              new DCEquipmentDisplayZoneSecondAreaParameters();
          dCEquipmentDisplayZoneSecondAreaParameters.setDisplayZone7Parameter(
              new DCArithmeticDisplayZoneOtherParameter(DCArithmeticDisplayZoneOtherParameter.DCDisplayZoneOtherArithmeticTypeOneDecimalPlace,
                  distance));
          if (BluetoothManager.isBluetoothPhoneEnabled()
              && equipment.getConnectionState() == DCEquipment.DCEquipmentConnectionStateConnected) {
            (equipment).setDisplayZonesSecondArea(dCEquipmentDisplayZoneSecondAreaParameters,
                completionBlock, errorBlock);
          }
        }
        break;

      case TypeConstants.TYPE_SPORT_ROWING_MACHINE:
        DCDisplayZoneParameters displayRowerZoneParameters =
            new DCDisplayZoneParameters();

        displayRowerZoneParameters.setDisplayZone1Parameter(goDisplay);
        if(!cleared) {
          prepareRowerZoneDisplay(displayRowerZoneParameters, DCUnit.DISTANCE, distance);
          prepareRowerZoneDisplay(displayRowerZoneParameters, DCUnit.KCAL_BURNT, kcal);
        }
        sendDisplayParamsToRower((DCRower) equipment, displayRowerZoneParameters, completionBlock, errorBlock);
        break;
      default:
        //empty
        break;
    }
  }
  private static void prepareTreadmillZoneDisplay(DCTreadmillDisplayZoneParameters displayZoneParameters, Integer equipmentId, DCUnit zoneTriggered, float value) {
    int zoneIndex = -1;
    float valueToSend = value;
    switch (zoneTriggered) {
      case CURRENT_HEART_RATE:
        if (equipmentId != null && TypeConstants.contain(treadmillConsoleSpecificZoneIdList,
            equipmentId)) {
          zoneIndex = TREADMILL_BPM_SPECIFIC_ZONE_INDEX;
        } else {
          zoneIndex = TREADMILL_BPM_ZONE_INDEX;
        }
        break;
      case SLOPE_DEVICE:
        zoneIndex = TREADMILL_INCLINE_PERCENTAGE_ZONE_INDEX;
        break;
      case CURRENT_SPEED:
        zoneIndex = TREADMILL_SPEED_ZONE_INDEX;
        break;
      case DISTANCE:
        if (equipmentId != null && TypeConstants.contain(treadmillConsoleSpecificZoneIdList,
            equipmentId)) {
          zoneIndex = TREADMILL_DISTANCE_SPECIFIC_ZONE_INDEX;
        }
        break;
      case KCAL_BURNT:
        if (equipmentId != null && TypeConstants.contain(treadmillConsoleSpecificZoneIdList,
            equipmentId)) {
          zoneIndex = TREADMILL_KCAL_SPECIFIC_ZONE_INDEX;
        }
        valueToSend = value % 1000;
        break;
    }

    if (zoneIndex != -1) {
      prepareTreadmillZoneInformations(displayZoneParameters, zoneIndex, valueToSend);
    }
  }
  private static void displayTreadmillZoneInformations(DCTreadmill treadmill, Integer equipmentId,
                                                       float equipmentVersion, DCUnit zoneTriggered, boolean started, float value,
                                                       DCCompletionBlock completionBlock, DCCompletionBlockWithError errorBlock) {
    int zoneIndex = -1;
    float valueToSend = value;
    switch (zoneTriggered) {
      case CURRENT_HEART_RATE:
        if (equipmentId != null && TypeConstants.contain(treadmillConsoleSpecificZoneIdList,
          equipmentId)) {
          zoneIndex = TREADMILL_BPM_SPECIFIC_ZONE_INDEX;
        } else {
          zoneIndex = TREADMILL_BPM_ZONE_INDEX;
        }
        break;
      case SLOPE_DEVICE:
        zoneIndex = TREADMILL_INCLINE_PERCENTAGE_ZONE_INDEX;
        break;
      case CURRENT_SPEED:
        zoneIndex = TREADMILL_SPEED_ZONE_INDEX;
        break;
      case DISTANCE:
        if (equipmentId != null && TypeConstants.contain(treadmillConsoleSpecificZoneIdList,
          equipmentId)) {
          zoneIndex = TREADMILL_DISTANCE_SPECIFIC_ZONE_INDEX;
        }
        break;
      case KCAL_BURNT:
        if (equipmentId != null && TypeConstants.contain(treadmillConsoleSpecificZoneIdList,
          equipmentId)) {
          zoneIndex = TREADMILL_KCAL_SPECIFIC_ZONE_INDEX;
        }
        valueToSend = value % 1000;
        break;
    }

    if (zoneIndex != -1) {
      sendTreadmillZoneInformations(treadmill, zoneIndex, valueToSend, started, completionBlock,
        errorBlock);
    }
  }


  private static void displayBikeZoneInformations(Integer sportId, DCEquipment equipment,
                                                  Integer equipmentId, float equipmentVersion, DCUnit zoneTriggered, boolean started, float value,
                                                  DCCompletionBlock completionBlock, DCCompletionBlockWithError errorBlock) {
    int zoneIndex = -1;
    float valueToSend = value;
    switch (zoneTriggered) {
      case CURRENT_HEART_RATE:
        if (equipmentId != null && (TypeConstants.contain(coreBikeConsoleNewDisplayIdList,
          equipmentId) || equipmentVersion >= consoleNewDisplayIdVersionMap.get(equipmentId))) {
          zoneIndex = CORE_BIKE_SPECIFIC_BPM_ZONE_INDEX;
        } else {
          zoneIndex = CORE_BIKE_BPM_ZONE_INDEX;
        }
        break;
      case RESISTANCE:
        zoneIndex = CORE_BIKE_RESISTANCE_ZONE_INDEX;
        break;
      case CURRENT_ROTATION:
        zoneIndex = CORE_BIKE_RPM_ZONE_INDEX;
        break;
      case CURRENT_SPEED:
        if (equipmentId != null && (TypeConstants.contain(coreBikeConsoleNewDisplayIdList,
          equipmentId) || equipmentVersion >= consoleNewDisplayIdVersionMap.get(equipmentId))) {
          zoneIndex = CORE_BIKE_SPECIFIC_SPEED_ZONE_INDEX;
        }
        break;
      case DISTANCE:
        if (equipmentId != null && (TypeConstants.contain(coreBikeConsoleNewDisplayIdList,
          equipmentId) || equipmentVersion >= consoleNewDisplayIdVersionMap.get(equipmentId))) {
          zoneIndex = CORE_BIKE_SPECIFIC_DISTANCE_ZONE_INDEX;
        }
        break;
      case KCAL_BURNT:
        if (equipmentId != null && (TypeConstants.contain(coreBikeConsoleNewDisplayIdList,
          equipmentId) || equipmentVersion >= consoleNewDisplayIdVersionMap.get(equipmentId))) {
          zoneIndex = CORE_BIKE_SPECIFIC_CALORIES_ZONE_INDEX;
        }
        valueToSend = value % 1000;
        break;
    }

    if (zoneIndex != -1) {
      if (sportId == TypeConstants.TYPE_SPORT_BIKE) {
        sendBikeZoneInformations((DCBike) equipment, zoneIndex, valueToSend, started,
          completionBlock, errorBlock);
      } else {
        sendEllipticalZoneInformations((DCEllipticalTrainer) equipment, zoneIndex, valueToSend,
          started, completionBlock, errorBlock);
      }
    }
  }
  private static void prepareEllipticalDisplay(DCEllipticalTrainerDisplayZoneParameters displayZoneParameters, Integer equipmentId, float equipmentVersion, DCUnit zoneTriggered, float value) {
    int zoneIndex = -1;
    float valueToSend = value;
    switch (zoneTriggered) {
      case CURRENT_HEART_RATE:
        if (equipmentId != null && (TypeConstants.contain(coreBikeConsoleNewDisplayIdList,
            equipmentId) || equipmentVersion >= consoleNewDisplayIdVersionMap.get(equipmentId))) {
          zoneIndex = CORE_BIKE_SPECIFIC_BPM_ZONE_INDEX;
        } else {
          zoneIndex = CORE_BIKE_BPM_ZONE_INDEX;
        }
        break;
      case RESISTANCE:
        zoneIndex = CORE_BIKE_RESISTANCE_ZONE_INDEX;
        break;
      case CURRENT_ROTATION:
        zoneIndex = CORE_BIKE_RPM_ZONE_INDEX;
        break;
      case CURRENT_SPEED:
        if (equipmentId != null && (TypeConstants.contain(coreBikeConsoleNewDisplayIdList,
            equipmentId) || equipmentVersion >= consoleNewDisplayIdVersionMap.get(equipmentId))) {
          zoneIndex = CORE_BIKE_SPECIFIC_SPEED_ZONE_INDEX;
        }
        break;
      case DISTANCE:
        if (equipmentId != null && (TypeConstants.contain(coreBikeConsoleNewDisplayIdList,
            equipmentId) || equipmentVersion >= consoleNewDisplayIdVersionMap.get(equipmentId))) {
          zoneIndex = CORE_BIKE_SPECIFIC_DISTANCE_ZONE_INDEX;
        }
        break;
      case KCAL_BURNT:
        if (equipmentId != null && (TypeConstants.contain(coreBikeConsoleNewDisplayIdList,
            equipmentId) || equipmentVersion >= consoleNewDisplayIdVersionMap.get(equipmentId))) {
          zoneIndex = CORE_BIKE_SPECIFIC_CALORIES_ZONE_INDEX;
        }
        valueToSend = value % 1000;
        break;
    }

    if (zoneIndex != -1) {
      prepareEllipticalZoneInformations(displayZoneParameters, zoneIndex, valueToSend);
    }
  }
  private static DCBikeDisplayZoneParameters prepareBikeZoneDisplay(DCBikeDisplayZoneParameters displayZoneParameters, Integer equipmentId, float equipmentVersion, DCUnit zoneTriggered, float value) {
    int zoneIndex = -1;
    float valueToSend = value;
    switch (zoneTriggered) {
      case CURRENT_HEART_RATE:
        if (equipmentId != null && (TypeConstants.contain(coreBikeConsoleNewDisplayIdList,
            equipmentId) || equipmentVersion >= consoleNewDisplayIdVersionMap.get(equipmentId))) {
          zoneIndex = CORE_BIKE_SPECIFIC_BPM_ZONE_INDEX;
        } else {
          zoneIndex = CORE_BIKE_BPM_ZONE_INDEX;
        }
        break;
      case RESISTANCE:
        zoneIndex = CORE_BIKE_RESISTANCE_ZONE_INDEX;
        break;
      case CURRENT_ROTATION:
        zoneIndex = CORE_BIKE_RPM_ZONE_INDEX;
        break;
      case CURRENT_SPEED:
        if (equipmentId != null && (TypeConstants.contain(coreBikeConsoleNewDisplayIdList,
            equipmentId) || equipmentVersion >= consoleNewDisplayIdVersionMap.get(equipmentId))) {
          zoneIndex = CORE_BIKE_SPECIFIC_SPEED_ZONE_INDEX;
        }
        break;
      case DISTANCE:
        if (equipmentId != null && (TypeConstants.contain(coreBikeConsoleNewDisplayIdList,
            equipmentId) || equipmentVersion >= consoleNewDisplayIdVersionMap.get(equipmentId))) {
          zoneIndex = CORE_BIKE_SPECIFIC_DISTANCE_ZONE_INDEX;
        }
        break;
      case KCAL_BURNT:
        if (equipmentId != null && (TypeConstants.contain(coreBikeConsoleNewDisplayIdList,
            equipmentId) || equipmentVersion >= consoleNewDisplayIdVersionMap.get(equipmentId))) {
          zoneIndex = CORE_BIKE_SPECIFIC_CALORIES_ZONE_INDEX;
        }
        valueToSend = value % 1000;
        break;
    }

    if (zoneIndex != -1) {
        return prepareBikeZoneInformations(displayZoneParameters, zoneIndex, valueToSend);
    }
    
    return null;
  }


  private static void displayRowerZoneInformations(Integer sportId, DCEquipment equipment,
                                                   Integer equipmentId, float equipmentVersion, DCUnit zoneTriggered, boolean started, float value,
                                                   DCCompletionBlock completionBlock, DCCompletionBlockWithError errorBlock) {
    int zoneIndex = -1;
    float valueToSend = value;
    switch (zoneTriggered) {
      case CURRENT_HEART_RATE:
          zoneIndex = ROWER_BPM_ZONE_INDEX;
        break;
      case RESISTANCE:
        zoneIndex = ROWER_RESISTANCE_ZONE_INDEX;
        break;
      case CURRENT_SPM:
        zoneIndex = ROWER_SPM_ZONE_INDEX;
        break;
      case KCAL_BURNT:
        zoneIndex = ROWER_CALORIES_ZONE_INDEX;
        valueToSend = value % 1000;
        break;
    }

    if (zoneIndex != -1) {
        sendRowerZoneInformations((DCRower) equipment, zoneIndex, valueToSend, started,
          completionBlock, errorBlock);
    }
  }


  private static DCDisplayZoneParameters prepareRowerZoneDisplay(DCDisplayZoneParameters dcDisplayZoneParameters, DCUnit zoneTriggered, float value) {
    int zoneIndex = -1;
    float valueToSend = value;
    switch (zoneTriggered) {
      case CURRENT_HEART_RATE:
        zoneIndex = ROWER_BPM_ZONE_INDEX;
        break;
      case RESISTANCE:
        zoneIndex = ROWER_RESISTANCE_ZONE_INDEX;
        break;
      case CURRENT_SPM:
        zoneIndex = ROWER_SPM_ZONE_INDEX;
        break;
      case KCAL_BURNT:
        zoneIndex = ROWER_CALORIES_ZONE_INDEX;
        valueToSend = value % 1000;
        break;
    }

    if (zoneIndex != -1) {
      return prepareRowerZoneInformations(dcDisplayZoneParameters, zoneIndex, valueToSend);
    }
    
    return null;
  }


  /**
   * Display the value given in the indicated zone as an integer, besides if the zone is the main display
   * switch between 'GO' and 'BLUETOOTH' depending on the session done (started or not)
   */
  private static void sendTreadmillZoneInformations(DCTreadmill treadmill, int zoneIndex,
                                                    float value, boolean started, DCCompletionBlock completionBlock,
                                                    DCCompletionBlockWithError errorBlock) {

    DCArithmeticDisplayZoneOtherParameter arithmeticDisplayZoneOtherParameter =
      new DCArithmeticDisplayZoneOtherParameter(
        DCArithmeticDisplayZoneOtherParameter.DCDisplayZoneOtherArithmeticTypeOneDecimalPlace,
        value);
    DCArithmeticDisplayZoneOtherParameter specificParam = new DCArithmeticDisplayZoneOtherParameter(
      DCArithmeticDisplayZoneOtherParameter.DCDisplayZone1ArithmeticTypeInteger, value);

    DCTreadmillDisplayZoneParameters displayZoneParameters = new DCTreadmillDisplayZoneParameters();
    switch (zoneIndex) {
      case 1:
        if (value != 0) {
          displayZoneParameters = new DCTreadmillDisplayZoneParameters();
          DCArithmeticDisplayZone1Parameter arithmeticDisplayZone1Parameter =
            new DCArithmeticDisplayZone1Parameter(
              DCArithmeticDisplayZone1Parameter.DCDisplayZone1ArithmeticTypeInteger, value);
          displayZoneParameters.setDisplayZone1Parameter(arithmeticDisplayZone1Parameter);
        }

        break;
      case 2:
        displayZoneParameters = new DCTreadmillDisplayZoneParameters();
        displayZoneParameters.setDisplayZone2Parameter(specificParam);
        break;
      case 3:
        displayZoneParameters = new DCTreadmillDisplayZoneParameters();
        displayZoneParameters.setDisplayZone3Parameter(specificParam);
        break;
      case 4:
        displayZoneParameters = new DCTreadmillDisplayZoneParameters();
        displayZoneParameters.setDisplayZone4Parameter(arithmeticDisplayZoneOtherParameter);
        break;
      case 5:
        displayZoneParameters.setDisplayZone5Parameter(arithmeticDisplayZoneOtherParameter);
        break;
      case 6:
        displayZoneParameters.setDisplayZone6Parameter(arithmeticDisplayZoneOtherParameter);
        break;
    }
    sendDisplayParamsToTreadmill(treadmill, displayZoneParameters, completionBlock, errorBlock);
  }

  /**
   * Display the value given in the indicated zone as an integer, besides if the zone is the main display
   * switch between 'GO' and 'BLUETOOTH' depending on the session done (started or not)
   */
  private static void prepareTreadmillZoneInformations(DCTreadmillDisplayZoneParameters displayZoneParameters, int zoneIndex,
                                                       float value) {
    if(displayZoneParameters!=null){
      DCArithmeticDisplayZoneOtherParameter arithmeticDisplayZoneOtherParameter =
          new DCArithmeticDisplayZoneOtherParameter(
              DCArithmeticDisplayZoneOtherParameter.DCDisplayZoneOtherArithmeticTypeOneDecimalPlace,
              value);
      DCArithmeticDisplayZoneOtherParameter specificParam = new DCArithmeticDisplayZoneOtherParameter(
          DCArithmeticDisplayZoneOtherParameter.DCDisplayZone1ArithmeticTypeInteger, value);
  
      switch (zoneIndex) {
        case 1:
          if (value != 0) {
            displayZoneParameters = new DCTreadmillDisplayZoneParameters();
            DCArithmeticDisplayZone1Parameter arithmeticDisplayZone1Parameter =
                new DCArithmeticDisplayZone1Parameter(
                    DCArithmeticDisplayZone1Parameter.DCDisplayZone1ArithmeticTypeInteger, value);
            displayZoneParameters.setDisplayZone1Parameter(arithmeticDisplayZone1Parameter);
          }
  
          break;
        case 2:
          displayZoneParameters = new DCTreadmillDisplayZoneParameters();
          displayZoneParameters.setDisplayZone2Parameter(specificParam);
          break;
        case 3:
          displayZoneParameters = new DCTreadmillDisplayZoneParameters();
          displayZoneParameters.setDisplayZone3Parameter(specificParam);
          break;
        case 4:
          displayZoneParameters = new DCTreadmillDisplayZoneParameters();
          displayZoneParameters.setDisplayZone4Parameter(arithmeticDisplayZoneOtherParameter);
          break;
        case 5:
          displayZoneParameters.setDisplayZone5Parameter(arithmeticDisplayZoneOtherParameter);
          break;
        case 6:
          displayZoneParameters.setDisplayZone6Parameter(arithmeticDisplayZoneOtherParameter);
          break;
      }
    }
  }

  /**
   * Display the value given in the indicated zone as an integer, besides if the zone is the main display
   * switch between 'GO' and 'BLUETOOTH' depending on the session done (started or not)
   */
  private static void sendBikeZoneInformations(DCBike bike, int zoneIndex, float value,
                                               boolean started, DCCompletionBlock completionBlock, DCCompletionBlockWithError errorBlock) {

    DCArithmeticDisplayZoneOtherParameter arithmeticDisplayZoneOtherParameter =
      new DCArithmeticDisplayZoneOtherParameter(
        DCArithmeticDisplayZone1Parameter.DCDisplayZone1ArithmeticTypeInteger, value);
    DCBikeDisplayZoneParameters displayZoneParameters = new DCBikeDisplayZoneParameters();
    switch (zoneIndex) {
      case 1://FIXME mettre les case dans methode privé
        if (value != 0) {
          DCArithmeticDisplayZone1Parameter arithmeticDisplayZone1Parameter =
            new DCArithmeticDisplayZone1Parameter(
              DCArithmeticDisplayZone1Parameter.DCDisplayZone1ArithmeticTypeInteger, value);
          displayZoneParameters.setDisplayZone1Parameter(arithmeticDisplayZone1Parameter);
        }
        break;
      case 2:
        DCArithmeticDisplayZoneOtherParameter speedValue =
          new DCArithmeticDisplayZoneOtherParameter(
            DCArithmeticDisplayZoneOtherParameter.DCDisplayZoneOtherArithmeticTypeOneDecimalPlace,
            value);

        displayZoneParameters.setDisplayZone2Parameter(speedValue);
        break;
      case 3:
        displayZoneParameters.setDisplayZone3Parameter(arithmeticDisplayZoneOtherParameter);
        break;
      case 4:
        displayZoneParameters.setDisplayZone4Parameter(arithmeticDisplayZoneOtherParameter);
        break;
      case 5:
        displayZoneParameters.setDisplayZone5Parameter(arithmeticDisplayZoneOtherParameter);
        break;
      case 6:
        displayZoneParameters.setDisplayZone6Parameter(arithmeticDisplayZoneOtherParameter);
        break;
      case 7:
        DCEquipmentDisplayZoneSecondAreaParameters dCEquipmentDisplayZoneSecondAreaParameters =
          new DCEquipmentDisplayZoneSecondAreaParameters();
        dCEquipmentDisplayZoneSecondAreaParameters.setDisplayZone7Parameter(
          new DCArithmeticDisplayZoneOtherParameter(
            DCArithmeticDisplayZoneOtherParameter.DCDisplayZoneOtherArithmeticTypeOneDecimalPlace,
            value));
        if(BluetoothManager.isBluetoothPhoneEnabled() && bike.getConnectionState() == DCEquipment.DCEquipmentConnectionStateConnected) {
          bike.setDisplayZonesSecondArea(dCEquipmentDisplayZoneSecondAreaParameters, completionBlock,
              errorBlock);
        }
        break;
    }
    sendDisplayParamsToBike(bike, displayZoneParameters, completionBlock, errorBlock);
  }

  /**
   * Prepare the value to display given in the indicated zone as an integer, besides if the zone is the main display
   * switch between 'GO' and 'BLUETOOTH' depending on the session done (started or not)
   */
  private static DCBikeDisplayZoneParameters prepareBikeZoneInformations(DCBikeDisplayZoneParameters displayZoneParameters, int zoneIndex, float value) {
    if(displayZoneParameters != null) {
      DCArithmeticDisplayZoneOtherParameter arithmeticDisplayZoneOtherParameter =
          new DCArithmeticDisplayZoneOtherParameter(DCArithmeticDisplayZone1Parameter.DCDisplayZone1ArithmeticTypeInteger, value);
      switch (zoneIndex) {
        case 1://FIXME mettre les case dans methode privé
          if (value != 0) {
            DCArithmeticDisplayZone1Parameter arithmeticDisplayZone1Parameter =
                new DCArithmeticDisplayZone1Parameter(DCArithmeticDisplayZone1Parameter.DCDisplayZone1ArithmeticTypeInteger, value);
            displayZoneParameters.setDisplayZone1Parameter(arithmeticDisplayZone1Parameter);
          }
          break;
        case 2:
          DCArithmeticDisplayZoneOtherParameter speedValue =
              new DCArithmeticDisplayZoneOtherParameter(DCArithmeticDisplayZoneOtherParameter.DCDisplayZoneOtherArithmeticTypeOneDecimalPlace,
                  value);

          displayZoneParameters.setDisplayZone2Parameter(speedValue);
          break;
        case 3:
          displayZoneParameters.setDisplayZone3Parameter(arithmeticDisplayZoneOtherParameter);
          break;
        case 4:
          displayZoneParameters.setDisplayZone4Parameter(arithmeticDisplayZoneOtherParameter);
          break;
        case 5:
          displayZoneParameters.setDisplayZone5Parameter(arithmeticDisplayZoneOtherParameter);
          break;
        case 6:
          displayZoneParameters.setDisplayZone6Parameter(arithmeticDisplayZoneOtherParameter);
          break;
      }
      return displayZoneParameters;
    }
    return null;
  }

  /**
   * Prepare the value to display given in the indicated zone as an integer, besides if the zone is the main display
   * switch between 'GO' and 'BLUETOOTH' depending on the session done (started or not)
   */
  private static void prepareEllipticalZoneInformations(DCEllipticalTrainerDisplayZoneParameters displayZoneParameters, int zoneIndex, float value) {
    if(displayZoneParameters!=null) {
      DCArithmeticDisplayZoneOtherParameter arithmeticDisplayZoneOtherParameter =
          new DCArithmeticDisplayZoneOtherParameter(DCArithmeticDisplayZone1Parameter.DCDisplayZone1ArithmeticTypeInteger, value);

      switch (zoneIndex) {
        case 1:
          if (value != 0) {
            DCArithmeticDisplayZone1Parameter arithmeticDisplayZone1Parameter =
                new DCArithmeticDisplayZone1Parameter(DCArithmeticDisplayZone1Parameter.DCDisplayZone1ArithmeticTypeInteger, value);
            displayZoneParameters.setDisplayZone1Parameter(arithmeticDisplayZone1Parameter);
          }

          break;
        case 2:
          DCArithmeticDisplayZoneOtherParameter speedValue =
              new DCArithmeticDisplayZoneOtherParameter(DCArithmeticDisplayZoneOtherParameter.DCDisplayZoneOtherArithmeticTypeOneDecimalPlace,
                  value);
          displayZoneParameters.setDisplayZone2Parameter(speedValue);
          break;
        case 3:
          displayZoneParameters.setDisplayZone3Parameter(arithmeticDisplayZoneOtherParameter);
          break;
        case 4:
          displayZoneParameters.setDisplayZone4Parameter(arithmeticDisplayZoneOtherParameter);
          break;
        case 5:
          displayZoneParameters.setDisplayZone5Parameter(arithmeticDisplayZoneOtherParameter);
          break;
        case 6:
          displayZoneParameters.setDisplayZone6Parameter(arithmeticDisplayZoneOtherParameter);
          break;
      }
    }
  }

  /**
   * Display the value given in the indicated zone as an integer, besides if the zone is the main display
   * switch between 'GO' and 'BLUETOOTH' depending on the session done (started or not)
   */
  private static void sendRowerZoneInformations(DCRower rower, int zoneIndex, float value,
                                                boolean started, DCCompletionBlock completionBlock, DCCompletionBlockWithError errorBlock) {

    DCArithmeticDisplayZoneOtherParameter arithmeticDisplayZoneOtherParameter =
      new DCArithmeticDisplayZoneOtherParameter(
        DCArithmeticDisplayZone1Parameter.DCDisplayZone1ArithmeticTypeInteger, value);
    DCDisplayZoneParameters displayZoneParameters = new DCDisplayZoneParameters();
    switch (zoneIndex) {
      case 1:
        if (value != 0) {
          DCArithmeticDisplayZone1Parameter arithmeticDisplayZone1Parameter =
            new DCArithmeticDisplayZone1Parameter(
              DCArithmeticDisplayZone1Parameter.DCDisplayZone1ArithmeticTypeInteger, value);
          displayZoneParameters.setDisplayZone1Parameter(arithmeticDisplayZone1Parameter);
        }
        break;
      case 2:
        DCArithmeticDisplayZoneOtherParameter speedValue =
          new DCArithmeticDisplayZoneOtherParameter(
            DCArithmeticDisplayZoneOtherParameter.DCDisplayZoneOtherArithmeticTypeOneDecimalPlace,
            value);

        displayZoneParameters.setDisplayZone2Parameter(speedValue);
        break;
      case 3:
        displayZoneParameters.setDisplayZone3Parameter(arithmeticDisplayZoneOtherParameter);
        break;
      case 4:
        displayZoneParameters.setDisplayZone4Parameter(arithmeticDisplayZoneOtherParameter);
        break;
      case 5:
        displayZoneParameters.setDisplayZone5Parameter(arithmeticDisplayZoneOtherParameter);
        break;
      case 6:
        displayZoneParameters.setDisplayZone6Parameter(arithmeticDisplayZoneOtherParameter);
        break;
      case 7:
        DCEquipmentDisplayZoneSecondAreaParameters dCEquipmentDisplayZoneSecondAreaParameters =
          new DCEquipmentDisplayZoneSecondAreaParameters();
        dCEquipmentDisplayZoneSecondAreaParameters.setDisplayZone7Parameter(
          new DCArithmeticDisplayZoneOtherParameter(
            DCArithmeticDisplayZoneOtherParameter.DCDisplayZoneOtherArithmeticTypeOneDecimalPlace,
            value));
        if(BluetoothManager.isBluetoothPhoneEnabled() && rower.getConnectionState() == DCEquipment.DCEquipmentConnectionStateConnected) {
          rower.setDisplayZonesSecondArea(dCEquipmentDisplayZoneSecondAreaParameters,
              completionBlock, errorBlock);
        }
        break;
    }
    sendDisplayParamsToRower(rower, displayZoneParameters, completionBlock, errorBlock);
  }

  /**
   * Display the value given in the indicated zone as an integer, besides if the zone is the main display
   * switch between 'GO' and 'BLUETOOTH' depending on the session done (started or not)
   */
  private static DCDisplayZoneParameters prepareRowerZoneInformations(DCDisplayZoneParameters displayZoneParameters, int zoneIndex, float value) {
    if(displayZoneParameters!=null) {
      DCArithmeticDisplayZoneOtherParameter arithmeticDisplayZoneOtherParameter =
          new DCArithmeticDisplayZoneOtherParameter(DCArithmeticDisplayZone1Parameter.DCDisplayZone1ArithmeticTypeInteger, value);
      switch (zoneIndex) {
        case 1:
          if (value != 0) {
            DCArithmeticDisplayZone1Parameter arithmeticDisplayZone1Parameter =
                new DCArithmeticDisplayZone1Parameter(DCArithmeticDisplayZone1Parameter.DCDisplayZone1ArithmeticTypeInteger, value);
            displayZoneParameters.setDisplayZone1Parameter(arithmeticDisplayZone1Parameter);
          }
          break;
        case 2:
          DCArithmeticDisplayZoneOtherParameter speedValue =
              new DCArithmeticDisplayZoneOtherParameter(DCArithmeticDisplayZoneOtherParameter.DCDisplayZoneOtherArithmeticTypeOneDecimalPlace,
                  value);

          displayZoneParameters.setDisplayZone2Parameter(speedValue);
          break;
        case 3:
          displayZoneParameters.setDisplayZone3Parameter(arithmeticDisplayZoneOtherParameter);
          break;
        case 4:
          displayZoneParameters.setDisplayZone4Parameter(arithmeticDisplayZoneOtherParameter);
          break;
        case 5:
          displayZoneParameters.setDisplayZone5Parameter(arithmeticDisplayZoneOtherParameter);
          break;
        case 6:
          displayZoneParameters.setDisplayZone6Parameter(arithmeticDisplayZoneOtherParameter);
          break;
      }
      return displayZoneParameters;
    }
    return null;
  }


  /**
   * Display the value given in the indicated zone as an integer, besides if the zone is the main display
   * switch between 'GO' and 'BLUETOOTH' depending on the session done (started or not)
   */
  private static void sendEllipticalZoneInformations(DCEllipticalTrainer ellipticalTrainer,
                                                     int zoneIndex, float value, boolean started, DCCompletionBlock completionBlock,
                                                     DCCompletionBlockWithError errorBlock) {

    DCArithmeticDisplayZoneOtherParameter arithmeticDisplayZoneOtherParameter =
      new DCArithmeticDisplayZoneOtherParameter(
        DCArithmeticDisplayZone1Parameter.DCDisplayZone1ArithmeticTypeInteger, value);
    DCEllipticalTrainerDisplayZoneParameters displayZoneParameters =
      new DCEllipticalTrainerDisplayZoneParameters();
    switch (zoneIndex) {
      case 1:
        if (value != 0) {
          displayZoneParameters = new DCEllipticalTrainerDisplayZoneParameters();
          DCArithmeticDisplayZone1Parameter arithmeticDisplayZone1Parameter =
            new DCArithmeticDisplayZone1Parameter(
              DCArithmeticDisplayZone1Parameter.DCDisplayZone1ArithmeticTypeInteger, value);
          displayZoneParameters.setDisplayZone1Parameter(arithmeticDisplayZone1Parameter);
        }

        break;
      case 2:
        DCArithmeticDisplayZoneOtherParameter speedValue =
          new DCArithmeticDisplayZoneOtherParameter(
            DCArithmeticDisplayZoneOtherParameter.DCDisplayZoneOtherArithmeticTypeOneDecimalPlace,
            value);
        displayZoneParameters.setDisplayZone2Parameter(speedValue);
        break;
      case 3:
        displayZoneParameters.setDisplayZone3Parameter(arithmeticDisplayZoneOtherParameter);
        break;
      case 4:
        displayZoneParameters.setDisplayZone4Parameter(arithmeticDisplayZoneOtherParameter);
        break;
      case 5:
        displayZoneParameters.setDisplayZone5Parameter(arithmeticDisplayZoneOtherParameter);
        break;
      case 6:
        displayZoneParameters.setDisplayZone6Parameter(arithmeticDisplayZoneOtherParameter);
        break;
      case 7:
        DCEquipmentDisplayZoneSecondAreaParameters dCEquipmentDisplayZoneSecondAreaParameters =
          new DCEquipmentDisplayZoneSecondAreaParameters();
        dCEquipmentDisplayZoneSecondAreaParameters.setDisplayZone7Parameter(
          new DCArithmeticDisplayZoneOtherParameter(
            DCArithmeticDisplayZoneOtherParameter.DCDisplayZoneOtherArithmeticTypeOneDecimalPlace,
            value));
        if(BluetoothManager.isBluetoothPhoneEnabled() && ellipticalTrainer.getConnectionState() == DCEquipment.DCEquipmentConnectionStateConnected) {
          ellipticalTrainer.setDisplayZonesSecondArea(dCEquipmentDisplayZoneSecondAreaParameters,
              completionBlock, errorBlock);
        }
        break;
    }
    sendDisplayParamsToEllipticalBike(ellipticalTrainer, displayZoneParameters, completionBlock,
      errorBlock);
  }

  private static void sendDisplayParamsToTreadmill(DCTreadmill treadmill,
                                                   DCTreadmillDisplayZoneParameters displayZoneParameters,
                                                   DCCompletionBlock genericCompletionBlock, DCCompletionBlockWithError genericErrorBlock) {
    if(BluetoothManager.isBluetoothPhoneEnabled() && treadmill.getConnectionState() == DCEquipment.DCEquipmentConnectionStateConnected) {
    treadmill.setDisplayZones(displayZoneParameters, genericCompletionBlock, genericErrorBlock);
  }
  }

  private static void sendDisplayParamsToBike(DCBike bike,
                                              DCBikeDisplayZoneParameters displayZoneParameters, DCCompletionBlock genericCompletionBlock,
                                              DCCompletionBlockWithError genericErrorBlock) {
    if(BluetoothManager.isBluetoothPhoneEnabled() && bike.getConnectionState() == DCEquipment.DCEquipmentConnectionStateConnected) {
      bike.setDisplayZones(displayZoneParameters, genericCompletionBlock, genericErrorBlock);
    }
  }

  private static void sendDisplayParamsToRower(DCRower rower,
                                               DCDisplayZoneParameters displayZoneParameters, DCCompletionBlock genericCompletionBlock,
                                               DCCompletionBlockWithError genericErrorBlock) {
    if(BluetoothManager.isBluetoothPhoneEnabled() && rower.getConnectionState() == DCEquipment.DCEquipmentConnectionStateConnected) {
      rower.setDisplayZones(displayZoneParameters, genericCompletionBlock, genericErrorBlock);
    }
  }


  private static void sendDisplayParamsToEllipticalBike(DCEllipticalTrainer ellipticalTrainer,
                                                        DCEllipticalTrainerDisplayZoneParameters displayZoneParameters,
                                                        DCCompletionBlock genericCompletionBlock, DCCompletionBlockWithError genericErrorBlock) {
    if(BluetoothManager.isBluetoothPhoneEnabled() && ellipticalTrainer.getConnectionState() == DCEquipment.DCEquipmentConnectionStateConnected) {
    ellipticalTrainer.setDisplayZones(displayZoneParameters, genericCompletionBlock,
      genericErrorBlock);
  }
}
}
