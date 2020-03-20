package com.sh.ec.bluetooth.common;

import com.sh.ec.entity.PauseCauseEnum;

import java.io.Serializable;


public class EquipmentPauseState implements Serializable{

  private boolean pause;
  private PauseCauseEnum pauseReason;

  public EquipmentPauseState(boolean pause, PauseCauseEnum pauseReason) {
    this.pause = pause;
    this.pauseReason = pauseReason;
  }

  /**
   *  *@return Retourne la valeur de pause
   *  
   */
  public boolean isPause() {
    return pause;
  }

  /**
   *  *Modifie la valeur de pause
   *  
   */
  public void setPause(boolean pause) {
    this.pause = pause;
  }

  /**
   *  *@return Retourne la valeur de pauseReason
   *  
   */
  public PauseCauseEnum getPauseReason() {
    return pauseReason;
  }

  /**
   *  *Modifie la valeur de pauseReason
   *  
   */
  public void setPauseReason(PauseCauseEnum pauseReason) {
    this.pauseReason = pauseReason;
  }
}
