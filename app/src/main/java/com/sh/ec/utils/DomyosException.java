package com.sh.ec.utils;


import com.sh.ec.R;

/**
 * Created by salili on 29/05/2017.
 */
public class DomyosException extends Exception {
  public static final int CODE_404 = 404;
  public static final int CODE_500 = 500;
  private DomyosExceptionCode exceptionCode;

  /**
   * The enum Domyos exception code.
   */
  public enum DomyosExceptionCode {
    /**
     * No internet connexion domyos exception code.
     */
    //ExceptionName(titelID,MessageID)
    NoInternetConnexion(-1, R.string.noInternetConnection),//
    TimeOut(-1, R.string.error_connection_timed_out),//
    ERROR_404(CODE_404, R.string.error_wrong_request),//
    ERROR_500(CODE_500, R.string.error_server_side),//
    NETWORK_GENERIC_ERROR(-1, R.string.error_generic_network),//
    InternalError(-1, R.string.InternalError),//
    NOEquivalenceFound(-1, R.string.EquivalenceError),//
    EquipmentConnectionFailed(-1, R.string.error_equipment_connection_failed),//
    EquipmentConnectionRetryExceeded(-1, R.string.error_equipment_connection_stopped),//
    EquipmentGenericError(-1, R.string.error_equipment_generic),//
    BluetoothConnectionError(-1, R.string.error_bluetooth_connection),//
    BluetoothNotEnabledConnectionError(-1,R.string.bluetooth_not_activated),
    ProgramNoMoreAvailableError(-1, R.string.error_program_no_more_available);//

    private int idTitle;
    private int idMessage;

    DomyosExceptionCode(int idTitle, int idMessage) {
      this.idTitle = idTitle;
      this.idMessage = idMessage;
    }

    DomyosExceptionCode(int idMessage) {
      this.idTitle = -1;
      this.idMessage = idMessage;
    }

  /*  *//**
     * Gets title.
     *
     * @return the title
     *//*
    public String getTitle() {
      if (idTitle == -1) {
        return "";
      }
      return AndroidApplication.getContext().getString(idTitle);
    }
*/
    /**
     * Gets message.
     *
     * @return the message
     */
    /*public String getMessage() {
      if (idMessage == -1) return "";
      return AndroidApplication.getContext().getString(idMessage);
    }*/
  }

  /**
   * Constructs a new exception with {@code null} as its detail message.
   * The cause is not initialized, and may subsequently be initialized by a
   * call to {@link #initCause}.
   */
  public DomyosException() {
  }

  /**
   * Instantiates a new Domyos exception.
   *
   * @param exceptionCode the exception code
   */
  public DomyosException(DomyosExceptionCode exceptionCode) {
    this.exceptionCode = exceptionCode;
  }

  /**
   * Gets localized title.
   *
   * @return the localized title
   */
  /*public String getLocalizedTitle() {
    return this.exceptionCode.getTitle();
  }

  public String getLocalizedMessage() {
    return this.exceptionCode.getMessage();
  }
*/
  @Override public String getMessage() {
    return getLocalizedMessage();
  }

  public DomyosExceptionCode getExceptionCode() {
    return exceptionCode;
  }
}
