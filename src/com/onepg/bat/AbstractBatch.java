package com.onepg.bat;

import com.onepg.util.IoItems;
import com.onepg.util.LogUtil;
import com.onepg.util.LogWriter;
import com.onepg.util.ValUtil;

/**
 * Batch processing base class.<br>
 * <ul>
 * <li>Provides common processing (log output, exception handling, etc.) for each batch processing.</li>
 * <li>Subclasses define specific batch processing by implementing the <code>doExecute</code> method.</li>
 * <li>When a subclass calls the <code>callMain</code> method of this class from the <code>main</code> method, the <code>doExecute</code> method is executed.</li>
 * <li>Arguments to the subclass <code>main</code> method are assumed to use only the first argument (<code>args[0]</code>) in URL parameter format and pass it to the <code>callMain</code> method.</li>
 * <li>Arguments in URL parameter format are converted to map format and passed to the <code>doExecute</code> method as the <code>IoItems</code> class.</li>
 * <li>In case of error within the subclass <code>doExecute</code> method, Exception is assumed to be thrown.</li>
 * <li>The return value to the caller of the <code>main</code> method (mainly batch or shell) on normal termination is 0.</li>
 * <li>The return value when Exception is thrown is 1.</li>
 * </ul>
 * <pre>
 * [Implementation Example]<code>public class ExampleBatch extends AbstractBatch {
 *    public static void main(String[] args) {
 *      final ExampleBatch batch = new ExampleBatch();
 *      batch.callMain(args);
 *    }
 * 
 *    @Override
 *    public void doExecute(final IoItems io) throws Exception {
 *      // Implement batch processing content
 *    }
 * }</code>
 * [Execution Example]<code>java com.example.ExampleBatch "param1=value1&param2=value2"</code>
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
   * @param args the arguments
   * @return the return value of the main method
   * @throws Exception exception error
   */
  protected abstract int doExecute(final IoItems args) throws Exception;

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
   * <li>The converted arguments are passed to the <code>doExecute</code> method as the <code>IoItems</code> class.</li>
   * <li>If the return value of the <code>doExecute</code> method is 0, the processing is considered to have terminated normally.</li>
   * <li>If the return value of the <code>doExecute</code> method is other than 0 or an exception error occurs, the processing is considered to have terminated abnormally.</li>
   * </ul>
   *
   * @param args the arguments
   */
  protected void callMain(final String[] args) {
    final IoItems argsMap = new IoItems();
    if (!ValUtil.isEmpty(argsMap)) {
      argsMap.putAllByUrlParam(args[0]);
    }
    if (this.logger.isDevelopMode()) {
      this.logger.develop(LogUtil.joinKeyVal("arguments", argsMap));
    }

    int status = 0;
    try {
      this.logger.begin();
      status = doExecute(argsMap);
    } catch (final Exception | Error e) {
      status = 1;
      this.logger.error(e, "An exception error occurred in batch processing. ");
    }
    this.logger.end(status);
    System.exit(status);
  }
}
