package com.onepg.util;

/**
 * Separate thread processing base class.
 * @hidden
 */
abstract class AbstractSeparateThread implements Runnable {

  /** Log writer. */
  protected LogWriter logger = null;
  /** Running flag. */
  private volatile boolean running = false;
  /** Thread instance. */
  private Thread thread = null;

  /**
   * Main processing.
   */
  protected abstract void doExecute();

  /**
   * Constructor.
   */
  AbstractSeparateThread() {
    super();
  }

  @Override
  public final void run() {
    this.running = true;
    this.logger = LogUtil.newLogWriter(this.getClass(), this.thread.getName());
    this.logger.begin();
    try {
      doExecute();
    } catch (Exception | Error e) {
      this.logger.error(e, "An exception error occurred during separate thread execution. ");
    } finally {
      this.running = false;
      this.logger.end();
    }
  }

  /**
   * Executes thread.
   */
  public synchronized void execute() {
    if (isRunning()) {
        throw new RuntimeException("The thread is already running. ");
    }
    
    try {
      this.thread = new Thread(this, getClass().getSimpleName() + "-" + ValUtil.getSequenceCode());
      this.thread.setDaemon(false); // Explicitly set as non-daemon thread
      this.thread.start();
    } catch (IllegalArgumentException | SecurityException e) {
        throw new RuntimeException("An exception error occurred during thread creation. ", e);
    }
  }

  /**
   * Gets running flag.
   * <ul>
   * <li>Checks both running flag and thread alive status</li>
   * <li>May return <code>false</code> immediately after thread start or just before termination</li>
   * </ul>
   *
   * @return <code>true</code> if running
   */
  public boolean isRunning() {
    return this.running && this.thread != null && this.thread.isAlive();
  }
}
