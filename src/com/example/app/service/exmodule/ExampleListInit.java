package com.example.app.service.exmodule;

import com.onepg.db.SqlUtil;
import com.onepg.util.Io;
import com.onepg.web.AbstractDbAccessWebService;

/**
 * List initialization web service class.
 */
public class ExampleListInit extends AbstractDbAccessWebService {

  /**
   * {@inheritDoc}
   */
  @Override
  public void doExecute(final Io io) throws Exception {
    final String today = SqlUtil.getToday(getDbConn());
    // Set initial values
    io.put("birth_dt", today);
  }
}
 