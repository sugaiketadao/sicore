package com.onepg.util;

/**
 * Break exception class.<br>
 * <ul>
 * <li>An exception class used to abort processing.</li>
 * <li>Throwing this class interrupts stack trace output. (This is handled by the <code>LogUtil.getStackTrace()</code> method.)</li>
 * <li>Use this when logging has already been performed and you do not want the error propagation destination to output trace logs again.</li>
 * </ul>
 */
public final class BreakException extends RuntimeException {

  /**
   * Constructor.
   */
  public BreakException() {
    super();
  }

}
