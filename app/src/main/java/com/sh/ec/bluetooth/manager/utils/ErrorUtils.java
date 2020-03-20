package com.sh.ec.bluetooth.manager.utils;

import com.appdevice.domyos.DCEquipment;
import com.sh.ec.utils.DomyosException;

import java.net.SocketTimeoutException;

import static com.sh.ec.bluetooth.manager.BluetoothManager.isBluetoothPhoneEnabled;


/**
 * Util class to handle errors
 *
 * Created by mbouchagour on 12/12/2017.
 */
public class ErrorUtils {

  public static DomyosException mapNetworkErrors(Throwable throwable) {
  /*  if (throwable instanceof HttpException) {
      HttpException error = (HttpException) throwable;
    *//*  Integer code = error.response().code();
      switch (code) {
        case CODE_404:
          return new DomyosException(DomyosException.DomyosExceptionCode.ERROR_404);
        case CODE_500:
          return new DomyosException(DomyosException.DomyosExceptionCode.ERROR_500);
        default:
          return new DomyosException(DomyosException.DomyosExceptionCode.NETWORK_GENERIC_ERROR);
      }*//*
    }
    else*/ if (throwable instanceof SocketTimeoutException) {
      return new DomyosException(DomyosException.DomyosExceptionCode.TimeOut);
    } else if (throwable instanceof DomyosException) {
      return (DomyosException) throwable;
    } else {
      return new DomyosException(DomyosException.DomyosExceptionCode.NETWORK_GENERIC_ERROR);
    }
  }

  public static DomyosException mapBluetoothErrors(int errorCode) {
    if (isBluetoothPhoneEnabled()) {
      switch (errorCode) {
        case DCEquipment.DCErrorCodeNotConnected:
          return new DomyosException(DomyosException.DomyosExceptionCode.EquipmentConnectionFailed);
        case DCEquipment.DCErrorCodeRetryCountExceeded:
          return new DomyosException(
            DomyosException.DomyosExceptionCode.EquipmentConnectionRetryExceeded);
        default:
          return new DomyosException(DomyosException.DomyosExceptionCode.EquipmentGenericError);
      }
    } else {
      return new DomyosException(
        DomyosException.DomyosExceptionCode.BluetoothNotEnabledConnectionError);
    }
  }
}
