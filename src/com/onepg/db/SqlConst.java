package com.onepg.db;

import com.onepg.util.AbstractIoTypeMap;
import com.onepg.util.LogUtil;
import com.onepg.util.ValUtil;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 固定SQL.<br>
 * <ul>
 * <li>SQL文字列とバインド項目定義（項目名と型）を格納します。</li>
 * <li>おもにバッチ処理での使用を想定しており、クラス変数（定数）として宣言します。</li>
 * <li>本クラスを使用することでプリペアードステートメントのキャッシュが可能になるため、Webサービス処理での性能改善目的の使用も想定しています。</li>
 * <li>SQL実行時には、バインド値を持つパラメーターをこのSQLと共に渡します。</li>
 * <li>同じ項目名を同じ型で複数回バインド可能です。</li>
 * <li>バインド項目が無い場合は、そのまま使用します。</li>
 * <li>プリペアードステートメントのキャッシュを使用して実行するには <code>SqlUtil.executeOneCache</code> または <code>SqlUtil.executeCache</code> を使用します。</li>
 * </ul>
 * <pre>
 * ［SQL宣言例 バインド項目有り］
 * <code>SqlConst SQL_INS_PET = SqlConst.begin()
 *     .addQuery("INSERT INTO t_pet ( ")
 *     .addQuery("  pet_no ")
 *     .addQuery(", pet_nm ")
 *     .addQuery(", birth_dt ")
 *     .addQuery(", ins_ts ")
 *     .addQuery(", upd_ts ")
 *     .addQuery(" ) VALUES ( ")
 *     .addQuery("  ? ", "pet_no", BindType.BigDecimal)
 *     .addQuery(", ? ", "pet_nm", BindType.String)
 *     .addQuery(", ? ", "birth_dt", BindType.Date)
 *     .addQuery(", ? ", "now_ts", BindType.Timestamp)
 *     .addQuery(", ? ", "now_ts", BindType.Timestamp)
 *     .addQuery(" ) ")
 *     .end();</code>
 * ［SQL実行例 バインド項目有り］
 * <code>SqlUtil.executeOne(conn, SQL_INS_PET.bind(io));</code>
 * ［SQL宣言例 バインド項目無し］
 * <code>SqlConst SQL_SEL_USER = SqlConst.begin()
 *     .addQuery("SELECT ")
 *     .addQuery("  u.user_id ")
 *     .addQuery(", u.user_nm ")
 *     .addQuery(", u.email ")
 *     .addQuery(", u.birth_dt ")
 *     .addQuery(" FROM t_user u ")
 *     .addQuery(" ORDER BY u.user_id ")
 *     .end();</code>
 * ［SQL実行例 バインド項目無し］
 * <code>SqlResultSet rSet = SqlUtil.select(getDbConn(), SQL_SEL_USER);</code>
 * ［SQL実行例 プリペアードステートメントキャッシュ実行］
 * <code>SqlUtil.executeOneCache(conn, SQL_INS_PET.bind(io));</code>
 * </pre>
 */
public final class SqlConst extends SqlBean {

  /**
   * バインド型.<br>
   * <ul>
   * <li>SQLにバインドする際の型を示します。</li>
   * <li>数値の型は <code>BigDecimal</code> に統一されます。</li>
   * </ul>
   */
  public enum BindType {
    STRING, BIGDECIMAL, DATE, TIMESTAMP
  }

  /** 
   * バインド項目名リスト.<br>
   * <ul>
   * <li>バインド項目名の順序を保持するためのリスト。</li>
   * <li>同じ項目名が複数回バインドされる場合も、その順序で複数回格納されます。</li>
   * </ul>
   */
  private final List<String> bindItemNames;
  /** バインド項目定義マップ&lt;項目名、型&gt;. */
  private final Map<String, BindType> bindItems;
  
  /**
   * コンストラクタ.
   * 
   * @param query SQL文字列
   * @param bindItemNames バインド項目名リスト
   * @param bindItems バインド項目定義マップ&lt;項目名、型&gt;
   */
  SqlConst(final String query, final List<String> bindItemNames, final Map<String, BindType> bindItems) {
    super(query);
    this.bindItemNames = bindItemNames;
    this.bindItems = bindItems;
  }
  
  /**
   * 固定SQLビルダーインスタンス生成.
   * 
   * @return 固定SQLビルダーインスタンス
   */
  public static SqlConstBuilder begin() {
    return new SqlConstBuilder();
  }
  
  /**
   * バインド値セット.<br>
   * <ul>
   * <li>格納しているSQL文字列と、引数として受け取ったパラメーター値マップを SQL Bean にセットして返します。</li>
   * <li>パラメーター値マップからバインド項目名リストとバインド項目定義マップをもとにバインド値リストを作成して SQL Bean にセットします。</li>
   * <li>バインド値リストの要素は <code>Object</code> とし、各項目値はバインド項目定義に沿った型で格納されます。</li>
   * <li>パラメーター値マップにバインド項目名が存在しなければ実行時エラーとなります。</li>
   * </ul>
   * 
   * @param params パラメーター値マップ
   * @return SQL Bean
   */
  public SqlBean bind(final AbstractIoTypeMap params) {
    if (ValUtil.isNull(params)) {
      throw new RuntimeException("Parameter map must not be null.");
    }
    if (ValUtil.isEmpty(this.bindItemNames)) {
      throw new RuntimeException("Bind item list is empty. Use the fixed SQL directly.");
    }
    
    final List<Object> bindValues = new ArrayList<>();
    for (final String itemName : this.bindItemNames) {
      if (!params.containsKey(itemName)) {
        throw new RuntimeException("Parameter value not found for bind item. "
                                + LogUtil.joinKeyVal("itemName", itemName));
      }
      final BindType bindType = this.bindItems.get(itemName);
      if (BindType.STRING == bindType) {
        final String paramValue = params.getString(itemName);
        bindValues.add(paramValue);
      } else if (BindType.BIGDECIMAL == bindType) {
        final BigDecimal paramValue = params.getBigDecimal(itemName);
        bindValues.add(paramValue);
      } else if (BindType.DATE == bindType) {
        final java.sql.Date paramValue = params.getSqlDateNullable(itemName);
        bindValues.add(paramValue);
      } else if (BindType.TIMESTAMP == bindType) {
        final java.sql.Timestamp paramValue = params.getSqlTimestampNullable(itemName);
        bindValues.add(paramValue);
      }
    }
    return new SqlBean(super.id, super.query, bindValues);
  }

  /**
   * 固定SQLビルダー.
   * <ul>
   * <li>固定SQL（<code>SqlConst</code>）を組み立てるビルダークラスです。</li>
   * <li>SQL の組み立てとバインド項目定義（項目名と型）を同時に行えるメソッドを持ちます。</li>
   * <li><code>addQuery</code> メソッドは自インスタンスを返すのでメソッドチェーンで使えます。</li>
   * </ul>
   */
  public static final class SqlConstBuilder {

    /** SQL文字列. */
    private final StringBuilder query = new StringBuilder();

    /** 
     * バインド項目名リスト.<br>
     * <ul>
     * <li>バインド項目名の順序を保持するためのリスト。</li>
     * <li>同じ項目名が複数回バインドされる場合も、その順序で複数回格納されます。</li>
     * </ul>
     */
    private final List<String> bindItemNames = new ArrayList<>();
    /** バインド項目定義マップ&lt;項目名、型&gt;. */
    private final Map<String, BindType> bindItems = new LinkedHashMap<>();
      
    /**
     * コンストラクタ.
     */
    SqlConstBuilder() {
      // 処理なし
    }

    /**
     * 固定SQL返却.
     *
     * @return 固定SQL
     */
    public SqlConst end() {
      return new SqlConst(this.query.toString(), this.bindItemNames, this.bindItems);
    }

    /**
     * SQL追加.<br>
     * <ul>
     * <li>２文字以上のブランクを1文字ブランクに置き換えて追加します。</li>
     * </ul>
     * 
     * @param sql SQL
     * @return 自インスタンス
     */
    public SqlConstBuilder addQuery(final String sql) {
      // SQL追加
      SqlUtil.appendQuery(this.query, sql); 
      return this;
    }

    /**
     * SQL＆バインド項目定義（項目名と型）追加.<br>
     * <ul>
     * <li>SQL文字列内にバインドプレースホルダー <code>?</code> を１つだけ含む必要があります。</li>
     * <li>バインド項目名は <code>Io</code> オブジェクトキーとして有効な値である必要があります。（<code>AbstractIoTypeMap</code> のキールール）</li>
     * </ul>
     *
     * @param sql      SQL
     * @param itemName バインド項目名
     * @param bindType バインド型
     * @return 自インスタンス
     */
    public SqlConstBuilder addQuery(final String sql, final String itemName, final BindType bindType) {
      // バインド項目名チェック
      ValUtil.validateIoKey(itemName);

      if (ValUtil.isBlank(sql)) {
        throw new RuntimeException("SQL must not be blank.");
      }
      // SQL文字列内の ? の数チェック
      final int placeholderCount = sql.length() - sql.replace("?", "").length();
      if (placeholderCount != 1) {
        throw new RuntimeException("SQL must contain exactly one bind placeholder '?'. "
            + LogUtil.joinKeyVal("sql", sql, "placeholderCount", placeholderCount));
      }
      // 既存バインド項目型チェック
      if (this.bindItems.containsKey(itemName) && this.bindItems.get(itemName) != bindType) {
        throw new RuntimeException("Bind item already exists with different type. "
                                + LogUtil.joinKeyVal("itemName", itemName,
                                                    "existingType", this.bindItems.get(itemName),
                                                    "newType", bindType));
      }
    
      // SQL追加
      SqlUtil.appendQuery(this.query, sql); 
      // バインド項目名リストとバインド項目定義マップに追加
      this.bindItemNames.add(itemName);
      this.bindItems.put(itemName, bindType);

      return this;
    }
    
    /**
     * 最終SQL文字列削除.
     * <ul>
     * <li>SQL文字列の最終文字（１文字）を削除します。</li>
     * </ul>
     * 
     * @return 自インスタンス
     */
    public SqlConstBuilder delLastChar() {
      ValUtil.deleteLastChar(this.query);
      return this;
    }
    
    /**
     * 最終SQL文字列削除.
     * <ul>
     * <li>SQL文字列の最後から指定文字数を削除します。</li>
     * </ul>
     * 
     * @param deleteCharCount 削除文字数
     * @return 自インスタンス
     */
    public SqlConstBuilder delLastChar(final int deleteCharCount) {
      ValUtil.deleteLastChar(this.query, deleteCharCount);
      return this;
    }
  }
}
