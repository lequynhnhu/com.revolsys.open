package com.revolsys.jtstest.testrunner;

/**
 * An Exception which indicates a problem during reflection
 *
 * @author Martin Davis
 * @version 1.7
 */
public class JTSTestReflectionException
extends Exception
{
  private static String createMessage(final String opName, final Object[] args) {
    String msg = "Could not find Geometry method: " + opName + "(";
    for (int j = 0; j < args.length; j++) {
      if (j > 0) {
        msg += ", ";
      }
      msg += args[j].getClass().getName();
    }
    msg += ")";
    return msg;
  }

  /**
   *
   */
  private static final long serialVersionUID = 1L;

  public JTSTestReflectionException(final String message) {
    super(message);
  }

  public JTSTestReflectionException(final String opName, final Object[] args) {
    super(createMessage(opName, args));
  }

}