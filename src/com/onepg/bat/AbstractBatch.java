package com.onepg.bat;

import com.onepg.util.IoItems;
import com.onepg.util.LogTxtHandler;
import com.onepg.util.LogUtil;
import com.onepg.util.LogWriter;
import com.onepg.util.ValUtil;

/**
 * Base class for batch processing.<br>
 * <ul>
 * <li>Provides common processing (logging, exception handling, etc.) for each batch.</li>
 * <li>Subclasses define specific batch processing by implementing the <code>doExecute</code> method.</li>
 * <li>When a subclass calls the <code>callMain</code> method of this class from the <code>main</code> method, the <code>doExecute</code> method is executed.</li>
 * <li>Arguments to the subclass <code>main</code> method are assumed to be passed as-is to the <code>callMain</code> method in URL parameter format.</li>
 * <li>Arguments in URL parameter format are converted to map format and passed to the <code>doExecute</code> method as an <code>IoItems</code> instance.</li>
 * <li>When an error occurs in the subclass <code>doExecute</code> method, it is assumed to throw an Exception.</li>
 * <li>The return value of the <code>callMain</code> method on successful completion is 0.</li>
 * <li>The return value of the <code>callMain</code> method when an Exception is thrown in the <code>doExecute</code> method is 1.</li>
 * </ul>
 * <pre>
 * [Implementation Example] <code>public class ExampleBatch extends AbstractBatch {
 *    public static void main(String[] args) {
 *      System.exit((new ExampleBatch()).callMain(args));
 *    }
 * 
 *    @Override
 *    public void doExecute(final IoItems io) throws Exception {
 *      // Implement batch processing content
 *    }
 * }</code>
 * [Execution Example] <code>java com.example.ExampleBatch "param1=value1&param2=value2"</code>
 * </pre> 
 */
public abstract class AbstractBatch {

  /** Trace code. */
  protected final String traceCode;
  /** Log writer. */
  protected final LogWriter logger;

  /**
   * Main processing.<br>
   * <ul>
   * <li>Subclasses implement specific batch processing.</li>
   * </ul>
   *
   * @param args arguments
   * @throws Exception exception error
   */
  protected abstract void doExecute(final IoItems args) throws Exception;

  /**
   * Constructor.
   */
  public AbstractBatch() {
    this.traceCode = ValUtil.getSequenceCode();
    this.logger = LogUtil.newLogWriter(getClass(), this.traceCode);
  }

  /**
   * Invokes main processing.<br>
   * <ul>
   * <li>Converts arguments from URL parameter format to map format, executes log start processing, and then calls the <code>doExecute</code> method.</li>
   * <li>Accepts multiple arguments as an array to handle length limitations per command line argument.</li>
   * <li>The converted arguments are passed to the <code>doExecute</code> method as an <code>IoItems</code> instance.</li>
   * <li>If an Exception is thrown in the <code>doExecute</code> method, the processing is considered to have terminated abnormally.</li>
   * </ul>
   *
   * @param args arguments
   * @return 0 on successful completion, 1 on abnormal termination
   */
  protected int callMain(final String[] args) {
    final IoItems argsMap = new IoItems();
    argsMap.putAllByBatParam(args);
    if (this.logger.isDevelopMode()) {
      this.logger.develop(LogUtil.joinKeyVal("arguments", argsMap));
    }

    int status = 0;
    try {
      this.logger.begin();
      doExecute(argsMap);
    } catch (final Exception | Error e) {
      status = 1;
      this.logger.error(e, "An exception error occurred in batch processing. ");
    }
    this.logger.end(status);

    // Close resources
    closeResources();

    return status;
  }

  /**
   * Closes resources.<br>
   * <ul>
   * <li>Closes the log text file.</li>
   * </ul>
   */
  private final void closeResources() {
    try {
      // Close log text file
      LogTxtHandler.closeAll();
    } catch (final Exception | Error e) {
      LogUtil.stdout(e, "An exception error occurred in log text file close.");
    }
  }
}

