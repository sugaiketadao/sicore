package com.example.app.service.exmodule;

import java.sql.Connection;

import com.onepg.db.SqlUtil;
import com.onepg.util.Io;
import com.onepg.util.IoItems;
import com.onepg.util.IoRows;
import com.onepg.util.LogUtil;
import com.onepg.util.ValUtil;
import com.onepg.util.Io.MsgType;
import com.onepg.web.AbstractDbAccessWebService;

/**
 * Upsert web service class.
 */
public class ExampleUpsert extends AbstractDbAccessWebService {

  /**
   * {@inheritDoc}
   */
  @Override
  public void doExecute(final Io io) throws Exception {
    // Validates the header
    validateHeader(io);
    if (io.hasErrorMsg()) {
      // Exits the process if a validation error occurs
      return;
    }
    // Validates the detail
    validateDetail(io);
    if (io.hasErrorMsg()) {
      // Exits the process if a validation error occurs
      return;
    }

    // Upserts the header
    upsertHead(io);
    if (io.hasErrorMsg()) {
      // Exits the process if an optimistic locking error occurs
      return;
    }
    // Deletes and inserts the detail
    delInsDetail(io);
    // Sets the success message
    if (ValUtil.isBlank(io.getString("upd_ts"))) {
      // Sets the message for new registration
      io.putMsg(MsgType.INFO, "i0001", new String[] { io.getString("user_id") });
    } else {
      // Sets the message for update
      io.putMsg(MsgType.INFO, "i0002", new String[] { io.getString("user_id") });
    } 
  }

  /**
   * Validates the header.
   * 
   * @param io argument and return value (request and response)
   * @throws Exception validation error
   */
  private void validateHeader(final Io io) throws Exception {

    // Checks the user ID
    final String userId = io.getString("user_id");
    if (ValUtil.isBlank(userId)) {
      // Sets the blank field message
      io.putMsg(MsgType.ERROR,"ev001", new String[]{"User ID"}, "user_id");
    } else if (!ValUtil.isAlphabetNumber(userId)) {
      // Sets the non-alphanumeric message
      io.putMsg(MsgType.ERROR, "ev011", new String[] { "User ID" }, "user_id");
    } else if (!ValUtil.checkLength(userId, 4)) {
      // Sets the invalid length message
      io.putMsg(MsgType.ERROR, "ev021", new String[] { "User ID" , "4" }, "user_id");
    }

    // Checks the user name
    final String userNm = io.getString("user_nm");
    if (ValUtil.isBlank(userNm)) {
      // Sets the blank field message
      io.putMsg(MsgType.ERROR, "ev001", new String[] { "User name" }, "user_nm");
    } else if (!ValUtil.checkLength(userNm, 20)) {
      // Sets the invalid length message
      io.putMsg(MsgType.ERROR, "ev021", new String[] { "User name", "20" }, "user_nm");
    }

    // Checks the Email
    final String email = io.getString("email");
    if (!ValUtil.isBlank(email)) {
      if (!ValUtil.checkLength(email, 50)) {
        // Sets the invalid length message
        io.putMsg(MsgType.ERROR, "ev021", new String[] { "Email", "50" }, "email");
      }
    }

    // Checks the income
    final String incomeAm = io.getString("income_am");
    if (!ValUtil.isBlank(incomeAm) ) {
      if (!ValUtil.isNumber(incomeAm)) {
        // Sets the numeric invalid message
        io.putMsg(MsgType.ERROR, "ev012", new String[] { "Income" }, "income_am");
      } else if (!ValUtil.checkLengthNumber(incomeAm, 10, 0) ) {
        // Sets the invalid length message
        io.putMsg(MsgType.ERROR, "ev022", new String[] { "Income", "10" }, "income_am");
      }
    }

    // Checks the birth date
    final String birthDt = io.getString("birth_dt");
    if (!ValUtil.isBlank(birthDt)) {
      if (!ValUtil.isDate(birthDt)) {
        // Sets the date invalid message
        io.putMsg(MsgType.ERROR, "ev013", new String[] { "Birth date" }, "birth_dt");
      }
    }
  }

  /**
   * Validates the detail.
   * 
   * @param io argument and return value (request and response)
   * @throws Exception validation error
   */
  private void validateDetail(final Io io) throws Exception {
    if (!io.containsKeyRows("detail")) {
      // Skips the detail check if the detail does not exist
      return;
    }

    final IoRows detail = io.getRows("detail");

    // Loops through detail rows (uses index instead of enhanced for loop)
    for (int rowIdx = 0; rowIdx < detail.size(); rowIdx++) {
      final IoItems row = detail.get(rowIdx);
      // Checks the pet name
      final String petNm = row.getString("pet_nm");
      if (ValUtil.isBlank(petNm)) {
        // Sets the blank field message
        io.putMsg(MsgType.ERROR, "ev001", new String[] { "Pet name" }, "pet_nm", "detail", rowIdx);
      } else if (!ValUtil.checkLength(petNm, 10)) {
        // Sets the invalid length message
        io.putMsg(MsgType.ERROR, "ev021", new String[] { "Pet name", "10" }, "pet_nm", "detail", rowIdx);
      }

      // Checks the weight
      final String weightKg = row.getString("weight_kg");
      if (!ValUtil.isBlank(weightKg)) {
        if (!ValUtil.isNumber(weightKg)) {
          // Sets the numeric invalid message
          io.putMsg(MsgType.ERROR, "ev012", new String[] { "Weight" }, "weight_kg", "detail", rowIdx);
        } else if (!ValUtil.checkLengthNumber(weightKg, 3, 1)) {
          // Sets the invalid length message
          io.putMsg(MsgType.ERROR, "ev023", new String[] { "Weight", String.valueOf(3 - 1),  "1" }, "weight_kg", "detail", rowIdx);
        }
      }

      // Checks the birth date
      final String birthDt = row.getString("birth_dt");
      if (!ValUtil.isBlank(birthDt)) {
        if (!ValUtil.isDate(birthDt)) {
          // Sets the date invalid message
          io.putMsg(MsgType.ERROR, "ev013", new String[] { "Birth date" }, "birth_dt", "detail", rowIdx);
        }
      }
    }
  }

  /**
   * Upserts the header.
   * 
   * @param io argument and return value (request and response)
   */
  private void upsertHead(final Io io) {
    if (ValUtil.isBlank(io.getString("upd_ts"))) {
      // Inserts one new row into the database if the update timestamp is blank
      if (!SqlUtil.insertOne(getDbConn(), "t_user", io, "upd_ts")) {
        io.putMsg(MsgType.ERROR, "e0001", new String[] { io.getString("user_id") }, "user_id");
        super.logger.info("Unique constraint violation occurred during header insert. " + LogUtil.joinKeyVal("user_id", io.getString("user_id")));
      }
      return;
    }

    // Updates one row in the database
    if (!SqlUtil.updateOne(getDbConn(), "t_user", io, new String[]{"user_id"}, "upd_ts")) {
      io.putMsg(MsgType.ERROR, "e0002", new String[] { io.getString("user_id") }, "user_id");
    }
  }

  /**
   * Deletes and inserts the detail.
   * 
   * @param io argument and return value (request and response)
   */
  private void delInsDetail(final Io io) {
    final Connection conn = getDbConn();
    // Deletes multiple rows from the database
    final int delCnt = SqlUtil.delete(conn, "t_user_pet", io, new String[] { "user_id" });

    if (!io.containsKeyRows("detail")) {
      // Skips the detail insertion if the detail does not exist
      if (super.logger.isDevelopMode()) {
        super.logger.develop(LogUtil.joinKeyVal("deleted count", delCnt, "inserted count", 0));
      }
      return;
    }

    // Inserts new detail rows
    final IoRows detail = io.getRows("detail");
    final String userId = io.getString("user_id");
    int dno = 0;
    for (final IoItems row : detail) {
      dno++;
      // Sets the key value
      row.put("user_id", userId);
      row.put("pet_no", dno);
      // Inserts one row into the database
      SqlUtil.insertOne(conn, "t_user_pet", row);
    }
    if (super.logger.isDevelopMode()) {
      super.logger.develop(LogUtil.joinKeyVal("deleted count", delCnt, "inserted count", dno));
    }
  }
}