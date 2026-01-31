package com.onepg.web;

/**
 * StandaloneHttpServer startup class.
 * @hidden
 */
public final class StandaloneServerStarter {
  /**
   * Main processing.
   * @param args Command line arguments
   */
  public static void main(final String[] args) {
     new Thread(() -> StandaloneServer.main(args)).start();
  }
}