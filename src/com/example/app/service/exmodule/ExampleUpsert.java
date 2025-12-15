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
 * Insert/update web service class.
 */
public class ExampleUpsert extends AbstractDbAccessWebService {

  /**
   * {@inheritDoc}
   */
  @Override
  public void doExecute(final Io io) throws Exception {
    // Header validation
    validateHeader(io);
    if (io.hasErrorMsg()) {
      // Exit processing on validation error
      return;
    }
    // Detail validation
    validateDetail(io);
    if (io.hasErrorMsg()) {
      // Exit processing on validation error
      return;
    }

    // Insert/update header
    upsertHead(io);
    if (io.hasErrorMsg()) {
      // Exit processing on optimistic locking error
      return;
    }
    // Delete and insert detail
    delInsDetail(io);
    // Set success message
    if (ValUtil.isBlank(io.getString("upd_ts"))) {
      // Set message for new registration
      io.putMsg(MsgType.INFO, "i0001", new String[] { io.getString("user_id") });
    } else {
      // Set message for update
      io.putMsg(MsgType.INFO, "i0002", new String[] { io.getString("user_id") });
    } 
  }

  /**
   * Validates header.
   * 
   * @param io argument and return value (request and response)
   * @throws Exception validation error
   */
  private void validateHeader(final Io io) throws Exception {

    // User ID check
    final String userId = io.getString("user_id");
    if (ValUtil.isBlank(userId)) {
      // Set not entered message
      io.putMsg(MsgType.ERROR,"ev001", new String[]{"User ID"}, "user_id");
    } else if (!ValUtil.isAlphabetNumber(userId)) {
      // Set non-alphanumeric message
      io.putMsg(MsgType.ERROR, "ev011", new String[] { "User ID" }, "user_id");
    } else if (!ValUtil.checkLength(userId, 4)) {
      // Set invalid length message
      io.putMsg(MsgType.ERROR, "ev021", new String[] { "User ID" , "4" }, "user_id");
    }

    // User name check
    final String userNm = io.getString("user_nm");
    if (ValUtil.isBlank(userNm)) {
      // Set not entered message
      io.putMsg(MsgType.ERROR, "ev001", new String[] { "User Name" }, "user_nm");
    } else if (!ValUtil.checkLength(userNm, 20)) {
      // Set invalid length message
      io.putMsg(MsgType.ERROR, "ev021", new String[] { "User Name", "20" }, "user_nm");
    }

    // Email check
    final String email = io.getString("email");
    if (!ValUtil.isBlank(email)) {
      if (!ValUtil.checkLength(email, 50)) {
        // Set invalid length message
        io.putMsg(MsgType.ERROR, "ev021", new String[] { "Email", "50" }, "email");
      }
    }

    // Income check
    final String incomeAm = io.getString("income_am");
    if (!ValUtil.isBlank(incomeAm) ) {
      if (!ValUtil.isNumber(incomeAm)) {
        // Set invalid number message
        io.putMsg(MsgType.ERROR, "ev012", new String[] { "Income" }, "income_am");
      } else if (!ValUtil.checkLengthNumber(incomeAm, 10, 0) ) {
        // Set invalid length message
        io.putMsg(MsgType.ERROR, "ev022", new String[] { "Income", "10" }, "income_am");
      }
    }

    // Birth date check
    final String birthDt = io.getString("birth_dt");
    if (!ValUtil.isBlank(birthDt)) {
      if (!ValUtil.isDate(birthDt)) {
        // Set invalid date message
        io.putMsg(MsgType.ERROR, "ev013", new String[] { "Birth Date" }, "birth_dt");
      }
    }
  }

  /**
   * Validates detail.
   * 
   * @param io argument and return value (request and response)
   * @throws Exception validation error
   */
  private void validateDetail(final Io io) throws Exception {
    if (!io.containsKeyRows("detail")) {
      // Skip detail check if detail does not exist
      return;
    }

    final IoRows detail = io.getRows("detail");

    // Detail row loop (do not use enhanced for statement because index is needed)
    for (int rowIdx = 0; rowIdx < detail.size(); rowIdx++) {
      final IoItems row = detail.get(rowIdx);
      // Pet name check
      final String petNm = row.getString("pet_nm");
      if (ValUtil.isBlank(petNm)) {
        // Set not entered message
        io.putMsg(MsgType.ERROR, "ev001", new String[] { "Pet Name" }, "pet_nm", "detail", rowIdx);
      } else if (!ValUtil.checkLength(petNm, 10)) {
        // Set invalid length message
        io.putMsg(MsgType.ERROR, "ev021", new String[] { "Pet Name", "10" }, "pet_nm", "detail", rowIdx);
      }

      // Weight check
      final String weightKg = row.getString("weight_kg");
      if (!ValUtil.isBlank(weightKg)) {
        if (!ValUtil.isNumber(weightKg)) {
          // Set invalid number message
          io.putMsg(MsgType.ERROR, "ev012", new String[] { "Weight" }, "weight_kg", "detail", rowIdx);
        } else if (!ValUtil.checkLengthNumber(weightKg, 3, 1)) {
          // Set invalid length message
          io.putMsg(MsgType.ERROR, "ev023", new String[] { "Weight", String.valueOf(3 - 1),  "1" }, "weight_kg", "detail", rowIdx);
        }
      }

      // Birth date check
      final String birthDt = row.getString("birth_dt");
      if (!ValUtil.isBlank(birthDt)) {
        if (!ValUtil.isDate(birthDt)) {
          // Set invalid date message
          io.putMsg(MsgType.ERROR, "ev013", new String[] { "Birth Date" }, "birth_dt", "detail", rowIdx);
        }
      }
    }
  }

  /**
   * Inserts or updates header.
   * 
   * @param io argument and return value (request and response)
   */
  private void upsertHead(final Io io) {
    if (ValUtil.isBlank(io.getString("upd_ts"))) {
      // Insert one new record to database if update timestamp is blank
      if (!SqlUtil.insertOne(getDbConn(), "t_user", io, "upd_ts")) {
        io.putMsg(MsgType.ERROR, "e0001", new String[] { io.getString("user_id") }, "user_id");
        super.logger.info("Unique constraint violation occurred during header insert. " + LogUtil.joinKeyVal("user_id", io.getString("user_id")));
      }
      return;
    }

    // Update one record in database
    if (!SqlUtil.updateOne(getDbConn(), "t_user", io, new String[]{"user_id"}, "upd_ts")) {
      io.putMsg(MsgType.ERROR, "e0002", new String[] { io.getString("user_id") }, "user_id");
    }
  }

  /**
   * Deletes and inserts detail.
   * 
   * @param io argument and return value (request and response)
   */
  private void delInsDetail(final Io io) {
    final Connection conn = getDbConn();
    // Delete multiple records from database
    final int delCnt = SqlUtil.delete(conn, "t_user_pet", io, new String[] { "user_id" });

    if (!io.containsKeyRows("detail")) {
      // Skip detail insert if detail does not exist
      if (logger.isDevelopMode()) {
        logger.develop(LogUtil.joinKeyVal("deleted count", delCnt, "inserted count", 0));
      }
      return;
    }

    // Insert new detail records
    final IoRows detail = io.getRows("detail");
    final String userId = io.getString("user_id");
    int dno = 0;
    for (final IoItems row : detail) {
      dno++;
      // Set key values
      row.put("user_id", userId);
      row.put("pet_no", dno);
      // Insert one record to database
      SqlUtil.insertOne(conn, "t_user_pet", row);
    }
    if (logger.isDevelopMode()) {
      logger.develop(LogUtil.joinKeyVal("deleted count", delCnt, "inserted count", dno));
    }
  }
}