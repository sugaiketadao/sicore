package com.onepg.util;

/**
 * Break exception class.<br>
 * <ul>
 * <li>An exception class used to abort processing.</li>
 * <li>Throwing this class interrupts the stack trace output. (This is handled by the <code>LogUtil.getStackTrace()</code> method.)</li>
 * <li>Use this when logging has already been done and you do not want trace logs output again at the error propagation destination.</li>
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
