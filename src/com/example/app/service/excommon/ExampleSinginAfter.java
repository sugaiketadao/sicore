package com.example.app.service.excommon;

import com.onepg.util.Io;
import com.onepg.web.AbstractDbAccessWebService;

/**
 * Web service class executed after sign-in.
 */
public class ExampleSinginAfter extends AbstractDbAccessWebService {

  /**
   * {@inheritDoc}
   * <ul>
   * <li>Creates session information.</li>
   * </ul>
   */
  @Override
  public void doExecute(final Io io) throws Exception {
    if (io.hasErrorMsg()) {
      // Exit if an authentication error occurs.
      return;
    }
    // Save the sign-in ID to the session (as a usage example).
    io.session().put("signin_id", io.getString("signin_id"));
  }
}
