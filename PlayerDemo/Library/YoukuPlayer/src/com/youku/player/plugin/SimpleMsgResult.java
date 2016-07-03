package com.youku.player.plugin;


/**
 * Class SimpleMsgResult
 */
public class SimpleMsgResult {

  //
  // Fields
  //

  private boolean succ;
  private String msg;
  
  //
  // Constructors
  //
  public SimpleMsgResult () { };
  
  //
  // Methods
  //


  //
  // Accessor methods
  //

  /**
   * Set the value of succ
   * @param newVar the new value of succ
   */
  public void setSucc ( boolean newVar ) {
    succ = newVar;
  }

  /**
   * Get the value of succ
   * @return the value of succ
   */
  public boolean getSucc ( ) {
    return succ;
  }

  /**
   * Set the value of msg
   * @param newVar the new value of msg
   */
  public void setMsg ( String newVar ) {
    msg = newVar;
  }

  /**
   * Get the value of msg
   * @return the value of msg
   */
  public String getMsg ( ) {
    return msg;
  }

  //
  // Other methods
  //

}
