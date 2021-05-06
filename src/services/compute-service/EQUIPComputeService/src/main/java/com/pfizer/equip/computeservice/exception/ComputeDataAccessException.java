package com.pfizer.equip.computeservice.exception;

public class ComputeDataAccessException extends Exception
{

  /**
   * 
   */
  private static final long serialVersionUID = 1L;

  public ComputeDataAccessException()
  {
    super();
  }

  public ComputeDataAccessException(String message,
      Throwable cause)
  {
    super(message, cause);
  }

  public ComputeDataAccessException(String message)
  {
    super(message);
  }

  public ComputeDataAccessException(Throwable cause)
  {
    super(cause);
  }

}
