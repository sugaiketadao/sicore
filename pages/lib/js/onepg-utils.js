/**
 * Value manipulation utility class.
 * @class
 */
const ValUtil = /** @lends ValUtil */ {
  /**
   * <code>null</code> check.<br>
   * <ul>
   * <li>Checks whether the Object is <code>null</code>.</li>
   * </ul>
   *
   * @param {Object} obj the target object to check
   * @returns {boolean} <code>true</code> if <code>null</code>
   */
  isNull : function(obj) {
    // undefined
    if (obj === void 0) {
      return true;
    }
    // null
    if (obj === null) {
      return true;
    }
    return false;
  },

  /**
   * Blank check.<br>
   * <ul>
   * <li>Checks whether the string is spaces only, zero length, or <code>null</code>.</li>
   * </ul>
   *
   * @param {string} str the target string to check
   * @returns {boolean} <code>true</code> if blank
   */
  isBlank : function(str) {
    if (ValUtil.isNull(str)) {
      return true;
    }
    return String(str).trim().length === 0;
  },

  /**
   * <code>null</code> to blank replacement.<br>
   * <ul>
   * <li>Returns blank or replacement string if the value is <code>null</code>.</li>
   * </ul>
   *
   * @param {string} value the target value to check
   * @param {string} [rep] the replacement string (optional). Returns blank if omitted.
   * @returns {string} the blank or replacement string if <code>null</code>
   */
  nvl : function(value, rep) {
    if (ValUtil.isNull(rep)) {
      rep = '';
    }
    if (ValUtil.isNull(value)) {
      return rep;
    }
    return value;
  },

  /**
   * Blank replacement.<br>
   * <ul>
   * <li>Returns the replacement string if the value is blank.</li>
   * </ul>
   *
   * @param {string} value the target value to check
   * @param {string} rep the replacement string
   * @returns {string} the replacement string if blank
   */
  bvl : function(value, rep) {
    if (ValUtil.isBlank(value)) {
      return rep;
    }
    return value;
  },

  /**
   * Safe string substring.
   *
   * @param {string} value the target string
   * @param {number} [beginIndex] the start index (optional). Default is 0
   * @param {number} [endIndex] the end index (optional). Default is the string length
   * @returns {string} the extracted substring
   */
  substring: function(value, beginIndex, endIndex) {
    if (ValUtil.isNull(value)) {
      return '';
    }
    // Fill in default values
    beginIndex = beginIndex || 0;
    endIndex = endIndex || value.length;
    // Correct out-of-range values
    if (endIndex > value.length) {
      endIndex = value.length;
    }
    // Return empty string if start position is after end position or beyond string length
    if (beginIndex < 0 || beginIndex >= endIndex || beginIndex >= value.length) {
      return '';
    }
    return value.substring(beginIndex, endIndex);
  },

  /**
   * String comparison.<br>
   * <ul>
   * <li>Treats <code>null</code> as an empty string for comparison.</li>
   * </ul>
   *
   * @param {string} val1 the first value to compare
   * @param {string} val2 the second value to compare
   * @returns {boolean} <code>true</code> if equal
   */
  equals : function(val1, val2) {
    return ValUtil.nvl(val1) === ValUtil.nvl(val2) ;
  },

  /**
   * Object comparison.<br>
   * <ul>
   *   <li>Compares values of keys that exist in both objects, and excludes keys that exist in only one.</li>
   *   <li>Returns "not equal" if either target is not an object.</li>
   * </ul>
   *
   * @param {Object} obj1 the first object to compare
   * @param {Object} obj2 the second object to compare
   * @param {string} ignoreKeys the keys to ignore (multiple keys can be specified)
   * @returns {boolean} <code>true</code> if contents are equal
   */
  equalsObj : function(obj1, obj2, ignoreKeys) {
    if (!ValUtil.isObj(obj1)) {
      return false;
    }
    if (!ValUtil.isObj(obj2)) {
      return false;
    }
    const ignoreKeyAry = Array.prototype.slice.call(arguments, 2);
    for (const key in obj1){
      // Ignore if it's an excluded key
      if (ignoreKeyAry.indexOf(key) >= 0) {
        continue;
      }
      // Ignore if the key doesn't exist in obj2
      // Writing comparison value (<code>false</code>) to avoid confusion with loops
      if (key in obj2 === false) {
        continue;
      }

      const val1 = obj1[key];
      const val2 = obj2[key];

      const t = ValUtil.toType(val1);
      if (t === 'object') {
        if (!ValUtil.equalsObj(val1, val2, ignoreKeys)) {
          return false;
        }
        continue;
      }

      if (t === 'array') {
        // For arrays, process assuming each element is an object.
        if (val1.length !== val2.length) {
          return false;
        }
        for (let i = 0; i < val1.length; i++) {
          if (!ValUtil.equalsObj(val1[i], val2[i], ignoreKeys)) {
            return false;
          }
        }
        continue;
      }

      if (!ValUtil.equals(val1, val2)) {
        return false;
      }
    }
    return true;
  },

  /**
   * Empty check.<br>
   * <ul>
   *   <li>Returns <code>true</code> if <code>null</code> is passed.</li>
   *   <li>Returns the same result as isBlank() if a string is passed.</li>
   *   <li>Checks for zero length if an array is passed.</li>
   *   <li>Checks for zero length if an HTML element collection is passed.</li>
   *   <li>Checks for zero length of key array if an object is passed.</li>
   * </ul>
   *
   * @param {Object} obj the target object to check
   * @returns {boolean} <code>true</code> if empty
   */
  isEmpty : function(obj) {
    if (ValUtil.isNull(obj)) {
      return true;
    }
    const t = ValUtil.toType(obj);
    if (t === 'string') {
      return ValUtil.isBlank(obj);
    }
    if (t === 'array' || t === 'arguments' || t === 'nodelist' || t === 'htmlcollection') {
      return (obj.length === 0);
    }
    if (t === 'object') {
      return (Object.keys(obj).length === 0);
    }
    // 'number' and 'boolean' are not empty if determined
    return false;
  },
  
  /** @private Regular expression for positive integer check */
  _IS_NUM_UNSIGNED_INT: /^([1-9]\d*|0)$/,
  /** @private Regular expression for positive decimal check */
  _IS_NUM_UNSIGNED_FLOAT: /^([1-9]\d*|0)(\.\d+)?$/,
  /** @private Regular expression for integer check */
  _IS_NUM_INT: /^[-]?([1-9]\d*|0)$/,
  /** @private Regular expression for decimal check */
  _IS_NUM_FLOAT: /^[-]?([1-9]\d*|0)(\.\d+)?$/,
  /**
   * Numeric check.<br>
   * <ul>
   * <li>Checks whether the string is a valid number.</li>
   * </ul>
   *
   * @param {string} value the target value to check
   * @param {boolean} [minusNg] <code>true</code> to reject negative values (optional)
   * @param {boolean} [decNg] <code>true</code> to reject decimal values (optional)
   * @returns {boolean} <code>true</code> if valid
   */
  isNum : function(value, minusNg, decNg) {
    if (ValUtil.isNull(value)) {
      return false;
    }
    const t = ValUtil.toType(value);
    if (t !== 'number' && t !== 'string') {
      return false;
    }
    if (t === 'number') {
      if (isNaN(value)) {
        return false;
      }
      return true;
    }
    if (minusNg && decNg) {
      return ValUtil._IS_NUM_UNSIGNED_INT.test(value);
    } else if (minusNg) {
      return ValUtil._IS_NUM_UNSIGNED_FLOAT.test(value);
    } else if (decNg) {
      return ValUtil._IS_NUM_INT.test(value);
    }
    return ValUtil._IS_NUM_FLOAT.test(value);
  },

  /**
   * Valid date check.<br>
   * <ul>
   * <li>Checks whether the string is a valid date.</li>
   * </ul>
   *
   * @param {string} yyyymmdd the target value to check (YYYYMMDD)
   * @returns {boolean} <code>true</code> if valid
   */
  isDate : function(yyyymmdd) {
    if (!ValUtil.isNum(yyyymmdd)) {
      return false;
    }
    if (yyyymmdd.length !== 8) {
      return false;
    }
    const y = ~~yyyymmdd.substring(0, 4);
    const m = ~~yyyymmdd.substring(4, 6) - 1;
    const d = ~~yyyymmdd.substring(6, 8);
    if (m > 11 || d > 31) {
      return false;
    }
    try {
      const date = new Date(y, m, d);
      if (date.getFullYear() !== y || date.getMonth() !== m || date.getDate() !== d) {
        return false;
      }
    } catch(e) {
      return false;
    }
    return true;
  },

  /** @private Array of strings considered as boolean "true" */
  _TRUE_VALUES: ['1', 'true', 'yes', 'on'],

  /**
   * Boolean check.<br>
   * <ul>
   * <li>Checks whether the string is considered as boolean "true".</li>
   * <li>Performs the following evaluation:
   *   <ol>
   *     <li>"1", "true", "yes", "on" (all single-byte characters) are <code>true</code>.</li>
   *     <li><code>null</code>, blank, or any other values are <code>false</code>.</li>
   *     <li>Case-insensitive.</li>
   *     <li>Ignores leading and trailing spaces.</li>
   *     <li>Returns boolean values as is.</li>
   *   </ol>
   * </li>
   * </ul>
   *
   * @param {string|boolean} val the target value to check
   * @returns {boolean} <code>true</code> if considered as boolean "true"
   */
  isTrue: function(val) {
    if (ValUtil.isNull(val)) {
      return false;
    }
    if (ValUtil.toType(val) === 'boolean') {
      return val;
    }
    const lowVal = ('' + val).trim().toLowerCase();
    return (ValUtil._TRUE_VALUES.indexOf(lowVal) >= 0);
  },

  /**
   * String to Date object conversion.<br>
   * <ul>
   * <li>Converts a date string to a Date object.</li>
   * </ul>
   *
   * @param {string} yyyymmdd the target value to convert (YYYYMMDD)
   * @returns {Date} the Date object
   */
  toDate : function(yyyymmdd) {
    if (!ValUtil.isDate(yyyymmdd)) {
      return null;
    }
    const y = ~~yyyymmdd.substring(0, 4);
    const m = ~~yyyymmdd.substring(4, 6) - 1;
    const d = ~~yyyymmdd.substring(6, 8);
    const date = new Date(y, m, d);
    return date;
  },

  /**
   * Date object to string conversion.<br>
   * <ul>
   * <li>Converts a Date object to a date string.</li>
   * </ul>
   *
   * @param {Date} dateObj the target Date object to convert
   * @returns {string} the date string (YYYYMMDD)
   */
  dateTo : function(dateObj) {
    if (ValUtil.isNull(dateObj)) {
      return null;
    }
    return ValUtil._formatDate(dateObj, 'YYYYMMDD');
  },

  /**
   * Array check.
   *
   * @param {Object} obj the target object to check
   * @returns {boolean} <code>true</code> if array
   */
  isAry : function(obj) {
    if (ValUtil.isNull(obj)) {
      return false;
    }
    const t = ValUtil.toType(obj);
    return (t === 'array');
  },

  /**
   * Object check.
   *
   * @param {Object} obj the target object to check
   * @returns {boolean} <code>true</code> if object
   */
  isObj : function(obj) {
    if (ValUtil.isNull(obj)) {
      return false;
    }
    const t = ValUtil.toType(obj);
    return (t === 'object');
  },

  /**
   * Left padding.
   *
   * @param {string} value the target value to process
   * @param {string} pad the character to pad with
   * @param {number} len the length after padding
   * @returns {string} the left-padded string
   */
  lpad : function(value, pad, len) {
    const pads = pad.repeat(len);
    return (pads + value).slice(len * -1);
  },

  /**
   * Right padding.
   *
   * @param {string} value the target value to process
   * @param {string} pad the character to pad with
   * @param {number} len the length after padding
   * @returns {string} the right-padded string
   */
  rpad : function(value, pad, len) {
    const pads = pad.repeat(len);
    return (value + pads).substring(0, len);
  },


  /**
   * Gets object type.<br>
   * <ul>
   * <li>Use this when detailed type checking is needed, since typeof returns 'object' for both <code>null</code> and arrays.</li>
   * </ul>
   *
   * @param {Object} obj the object
   * @returns {string} the type string: 'undefined', 'null', 'boolean', 'number', 'string', 'array', 'object', etc.
   */
  toType : function(obj) {
    return Object.prototype.toString.call(obj).slice(8, -1).toLowerCase();
  },

  /**
   * @private
   * Date object format conversion.
   *
   * @param {Date} dateObj the Date object
   * @param {string} formatter the format string
   * @returns {string} the formatted string
   */
  _formatDate : function(dateObj, formatter) {
    formatter = formatter.replace(/YYYY/g, dateObj.getFullYear());
    formatter = formatter.replace(/MM/g, ('0' + (dateObj.getMonth() + 1)).slice(-2));
    formatter = formatter.replace(/DD/g, ('0' + dateObj.getDate()).slice(-2));
    formatter = formatter.replace(/HH/g, ('0' + dateObj.getHours()).slice(-2));
    formatter = formatter.replace(/MI/g, ('0' + dateObj.getMinutes()).slice(-2));
    formatter = formatter.replace(/SS/g, ('0' + dateObj.getSeconds()).slice(-2));
    formatter = formatter.replace(/MS/g, ('00' + dateObj.getMilliseconds()).slice(-3));
    return formatter;
  },
};

/**
 * Value format class.<br>
 * <ul>
 *   <li>Handles value formatting for page display.</li>
 *   <li>Mainly executed from PageUtil using bracket notation, and rarely executed directly from module-specific processing.</li>
 *   <li>Requires corresponding unformat processing.</li>
 * </ul>
 * @class
 */
const FrmUtil = /** @lends FrmUtil */ {

  /**
   * Uppercase conversion.
   * @param {string} value the target value to process
   * @returns {string} the uppercased string
   */
  upper: function(value) {
    if (ValUtil.isBlank(value)) {
      return value;
    }
    return value.toUpperCase();
  },

  /**
   * Number (comma formatting).<br>
   * Returns non-numeric values without formatting.
   * @param {string} value the target value to process
   * @returns {string} the comma-formatted string
   */
  num: function(value) {
    if (ValUtil.isBlank(value)) {
      return value;
    }
    const unVal = UnFrmUtil.num(value);
    if (!ValUtil.isNum(unVal)) {
      return value;
    }
    // Separate decimal part
    const vals = unVal.trim().split('.');
    // Format integer part with commas
    vals[0] = vals[0].replace(/(\d)(?=(\d\d\d)+(?!\d))/g, '$1,');
    return vals.join('.');
  },

  /**
   * Date (YYYY/MM/DD format).<br>
   * Returns invalid dates without formatting.
   * @param {string} value the target value to process
   * @returns {string} the date string in YYYY/MM/DD format
   */
  ymd: function(value) {
    if (ValUtil.isBlank(value)) {
      return value;
    }
    const unVal = UnFrmUtil.ymd(value);
    if (!ValUtil.isDate(unVal)) {
      return unVal;
    }
    const unValTrim = unVal.trim();
    return unValTrim.substring(0, 4) + '/' + unValTrim.substring(4, 6) + '/' + unValTrim.substring(6, 8);
  },

  /**
   * Time (HH:MI:SS format).<br>
   * Returns values other than 6-digit numbers without formatting.
   * @param {string} value the target value to process
   * @returns {string} the time string in HH:MI:SS format
   */
  hms: function(value) {
    if (ValUtil.isBlank(value)) {
      return value;
    }
    const unVal = UnFrmUtil.hms(value);
    const unValTrim = unVal.trim();
    if (unValTrim.length !== 6 || !ValUtil.isNum(unValTrim)) {
      return unValTrim;
    }
    return unValTrim.substring(0, 2) + ':' + unValTrim.substring(2, 4) + ':' + unValTrim.substring(4, 6);
  },
};

/**
 * Value unformat class.<br>
 * <ul>
 *   <li>Handles value unformatting for request creation.</li>
 *   <li>Mainly executed from PageUtil using bracket notation, and rarely executed directly from module-specific processing.</li>
 *   <li>Requires corresponding format processing.</li>
 * </ul>
 * @class
 */
const UnFrmUtil = /** @lends UnFrmUtil */ {

  /**
   * Uppercase conversion unformat.
   * @param {string} value the target value to process
   * @returns {string} the unprocessed string
   */
  upper: function(value) {
    // No processing
    return value;
  },

  /**
   * Number unformat (removes commas).
   * @param {string} value the target value to process
   * @returns {string} the string with commas removed
   */
  num: function(value) {
    if (ValUtil.isBlank(value)) {
      return value;
    }
    const unVal = ('' + value).trim().replace(/,/g, '');
    return unVal;
  },

  /**
   * Date unformat (removes slashes).
   * @param {string} value the target value to process
   * @returns {string} the string with slashes removed
   */
  ymd: function(value) {
    if (ValUtil.isBlank(value)) {
      return value;
    }
    const unVal = value.trim().replace(/\//g, '');
    return unVal;
  },

  /**
   * Time unformat (removes colons).
   * @param {string} value the target value to process
   * @returns {string} the string with colons removed
   */
  hms: function(value) {
    if (ValUtil.isBlank(value)) {
      return value;
    }
    const unVal = value.trim().replace(/:/g, '');
    return unVal;
  },
};

/**
 * HTTP manipulation utility class.
 *
 * @class
 */
const HttpUtil = /** @lends HttpUtil */ {
  /**
   * Object to URL parameter conversion.<br>
   * <ul>
   * <li><pre>[Example]
   *      If <code>params = {p1: 'aaa', p2: 'bbb'}</code>,
   *      converts to <code>p1=aaa&p2=bbb</code>.</pre></li>
   * </ul>
   *
   * @param {Object.<string,string>} obj the object
   * @returns {string} the URL parameter string
   */
  convUrlParam: function(obj) {
    if (!ValUtil.isObj(obj)) {
      return '';
    }
    const ret = [];
    for (const key in obj) {
      const val = obj[key];
      ret.push(key + '=' + encodeURIComponent(ValUtil.nvl(val)));
    }
    return ret.join('&');
  },

  /**
   * Gets URL parameters.<br>
   * <ul>
   *   <li>Gets everything after the ? in the URL as an object.</li>
   *   <li>[Example] For "a=01&b=02", returns <code>{a:'01', b:'02'}</code>.</li>
   *   <li>Parameters are removed after retrieval.</li>
   * </ul>
   * @returns {Object} the URL parameter object
   */
  getUrlParams: function() {
    const ret = {};
    let params = location.search;
    if (params.length === 0) {
      return ret;
    }
    // Remove leading ?
    params = params.substring(1);
    const paramsAry = params.split('&');
    for (const param of paramsAry) {
      if (ValUtil.isBlank(param)) {
        continue;
      }
      const eqPos = param.indexOf('=');
      if (eqPos < 0) {
        ret[param] = '';
        continue;
      }
      const key = param.substring(0, eqPos);
      const val = param.substring(eqPos + 1);
      ret[key] = decodeURIComponent(ValUtil.nvl(val));
    }

    // Remove URL parameters
    const all = location.toString();
    const search = location.search;
    const rep = all.substring(0, all.length - search.length);
    history.replaceState(null, null, rep);

    return ret;
  },

  /**
   * Page navigation.<br>
   * <ul>
   * <li>Navigates to the specified URL. (Retrieves HTML file)</li>
   * <li>If parameters are specified, appends them after the ? in the URL.</li>
   * <li><pre>[Example]
   *      If <code>url = 'editpage.html', params = {user_id: 'U001', upd_ts: '20251231T245959001000'}</code>,
   *      accesses <code>editpage.html?user_id=U001&upd_ts=20251231T245959001000</code>.</pre></li>
   * </ul>
   *
   * @param {string} url the destination URL
   * @param {Object.<string, string>|string} [params] the parameters (strings are also allowed) (optional)
   */
  movePage : function(url, params) {
    let loc = '';
    loc += ValUtil.nvl(url);
    if (!ValUtil.isEmpty(params)) {
      loc += '?';
      if (ValUtil.isObj(params)) {
        loc += HttpUtil.convUrlParam(params);
      } else {
        loc += params;
      }
    }
    // Use replace to prevent going back with the back button (returns to the first opened page)
    location.replace(loc);
  },

  /**
   * Executes JSON web service (async/await compatible).<br>
   * <ul>
   * <li>Sends a JSON request to the specified URL with the <code>POST</code> method and receives a JSON response.</li>
   * <li>Request and response are exchanged as objects.</li>
   * </ul>
   * 
   * @param {string} url the destination URL
   * @param {Object} [req] the request object (optional)
   * @param {Object.<string, string>} [addHeader] the additional HTTP headers (optional)
   * @returns {Object} the response object
   */
  callJsonService : async function(url, req, addHeader) {
    req = req || {};
    if (!ValUtil.isObj(req)) {
      // Request data must be an object
      throw new Error('HttpUtil#callJsonService: Request must be an object. ');
    }
    // Merge headers
    const header = Object.assign(addHeader || {}, { 'Content-Type': 'application/json' });

    return new Promise(function(resolve, reject) {
      const xhr = new XMLHttpRequest();
      xhr.open('POST', url, true);
      for (const key in header) {
        const val = header[key];
        xhr.setRequestHeader(key, val);
      }
      // Don't auto-parse JSON for verification
      xhr.responseType = 'text';

      // Communication complete event
      xhr.onload = function() {
        if (200 <= xhr.status && xhr.status < 300) {
          let res = null;
          try {
            // Manually parse JSON
            res = JSON.parse(xhr.response);
            resolve(res);
          } catch (e) {
            reject(new Error(`Json parse error. \n${e.name}\n : ${e.message}`));
          }
        } else {
          reject(new Error(`HTTP status ${xhr.status}. `));
        }
      };
      
      // Network error event
      xhr.timeout = 60000;
      xhr.ontimeout = function(e) {
        reject(new Error('Timeout. '));
      };
      xhr.onerror = function(e) {
        reject(new Error('Network error. '));
      };

      // Send
      xhr.send(JSON.stringify(req));
    });
  },
};

/**
 * HTML element manipulation utility class.
 *
 * @class
 */
const DomUtil = /** @lends DomUtil */ {

  /** @private Alternative attribute name for <code>name</code> attribute of non-form input elements */
  _ORG_ATTR_NAME: 'data-name',

  /** @private Row index attribute when converted to object */
  _ORG_ATTR_OBJ_ROW_INDEX: 'data-obj-row-idx',
  /** @private Value when checkbox is OFF */
  _ORG_ATTR_CHECK_OFF_VALUE: 'data-check-off-value',
  /** @private Value format type */
  _ORG_ATTR_VALUE_FORMAT_TYPE: 'data-value-format-type',

  /** @private Backup of <code>display</code> style */
  _ORG_ATTR_STYLE_DISPLAY_BACKUP: 'data-style-display-backup',
  /** @private Backup of <code>visibility</code> style */
  _ORG_ATTR_STYLE_VISIBILITY_BACKUP: 'data-style-visibility-backup',

  /**
   * Element existence check.
   * @param {Element|NodeList} elms the target elements to check
   * @returns {boolean} <code>true</code> if HTML elements are retrieved
   */
  isExists: function(elms) {
    if (ValUtil.isNull(elms)) {
      return false;
    }
    if (DomUtil._isNodeList(elms) || DomUtil._isHtmlCollection(elms)) {
      return (elms.length > 0);
    }
    if (DomUtil._isHtmlElement(elms)) {
      return true;
    }
    if (ValUtil.isAry(elms) && !ValUtil.isEmpty(elms) && DomUtil._isHtmlElement(elms[0])) {
      return true;
    }
    return false;
  },

  /**
   * @private
   * NodeList check.
   *
   * @param {Object} elm the target object to check
   * @returns {boolean} <code>true</code> if <code>NodeList</code>
   */
  _isNodeList: function(elm) {
    if (ValUtil.isNull(elm)) {
      return false;
    }
    const t = ValUtil.toType(elm);
    return (t === 'nodelist');
  },

  /**
   * @private
   * HTMLCollection check.
   *
   * @param {Object} elm the target object to check
   * @returns {boolean} <code>true</code> if <code>HTMLCollection</code>
   */
  _isHtmlCollection: function(elm) {
    if (ValUtil.isNull(elm)) {
      return false;
    }
    const t = ValUtil.toType(elm);
    return (t === 'htmlcollection');
  },

  /**
   * @private
   * HTMLElement check.
   *
   * @param {Object} elm the target object to check
   * @returns {boolean} <code>true</code> if <code>HTMLElement</code>
   */
  _isHtmlElement: function(elm) {
    if (ValUtil.isNull(elm)) {
      return false;
    }
    const t = ValUtil.toType(elm);
    return (t.startsWith('html') && t.endsWith('element'));
  },

  /**
   * Checks whether an element is visible.<br>
   * <ul>
   * <li>Considers elements with <code>display:none</code> or <code>visibility:hidden</code> as not visible.</li>
   * <li>Also checks the visibility state of parent elements.</li>
   * </ul>
   * @param {Element} elm the target element to check
   * @returns {boolean} <code>true</code> if visible
   */
  isVisible: function(elm) {
    if (!DomUtil.isExists(elm)) {
      return false;
    }

    // Check the element and its ancestor elements
    let curElm = elm;
    while (DomUtil.isExists(curElm) && curElm !== document.body) {
      // Do not consider hidden fields as invisible
      if (curElm.tagName.toLowerCase() === 'input' && ValUtil.nvl(curElm.getAttribute('type')).toLowerCase() === 'hidden') {
        // Move to parent element
        curElm = curElm.parentElement;
        continue;
      }

      // Get merged styles with getComputedStyle
      const style = window.getComputedStyle(curElm);
      // Consider invisible if display: none or visibility: hidden
      if (style.display === 'none' || style.visibility === 'hidden') {
        return false;
      }
      // Move to parent element
      curElm = curElm.parentElement;
    }
    return true;
  },

  /**
   * @private
   * Gets the first element from NodeList or array.<br>
   * <ul>
   * <li>Returns the first element from <code>NodeList</code> or array.</li>
   * <li>Returns as is if the argument is not a <code>NodeList</code> or array.</li>
   * <li>Returns <code>null</code> if the argument is invalid.</li>
   * </ul>
   * @param {NodeList|Element} elm the target HTML element
   * @returns {Element|null} the first element
   */
  _getListFirst: function(elm) {
    if (!DomUtil.isExists(elm)) {
      return null;
    }
    if (DomUtil._isNodeList(elm) && elm.length > 0) {
      return elm[0];
    }
    if (ValUtil.isAry(elm) && elm.length > 0) {
      return elm[0];
    }
    return elm;
  },

  /**
   * @private
   * Converts NodeList to array.<br>
   * <ul>
   * <li>Enables usage of array methods like <code>forEach</code>.</li>
   * </ul>
   * @param {NodeList} list the <code>NodeList</code>
   * @returns {Array<Element>} the HTML element array
   */
  _listToAry: function(list) {
    if (!DomUtil.isExists(list)) {
      return [];
    }
    if (!DomUtil._isNodeList(list) && !DomUtil._isHtmlCollection(list)) {
      return [list];
    }

    const ret = [];
    for (const node of list) {
      if (node.nodeType === Node.ELEMENT_NODE) {
        ret.push(node);
      }
    }
    return ret;
  },

  /**
   * ID selector (gets the first element).<br>
   * <ul>
   * <li>Returns <code>null</code> if the argument is invalid or element cannot be found.</li>
   * </ul>
   * @param {string} id the <code>id</code> attribute
   * @param {Object} [outerElm] the search scope element (optional)
   * @returns {Element|null} the retrieved HTML element
   */
  getById : function(id, outerElm) {
    if (ValUtil.isBlank(id)) {
      return null;
    }
    if (DomUtil.isExists(outerElm)) {
      const oElm = DomUtil._getListFirst(outerElm);
      if (!DomUtil.isExists(oElm)) {
        return null;
      }
      const selector = '#' + id;
      const retElm = oElm.querySelector(selector);
      // querySelector() returns null if not found
      return retElm;
    }
    const retElm = document.getElementById(id);
    // getElementById() returns null if not found
    return retElm;
  },

  /**
   * Selector (gets the first element).<br>
   * <ul>
   * <li>Returns <code>null</code> if the argument is invalid or element cannot be found.</li>
   * </ul>
   * @param {string} selector the selector string
   * @param {Object} [outerElm] the search scope element (optional)
   * @returns {Element|null} the retrieved HTML element
   */
  getSelector: function(selector, outerElm) {
    if (ValUtil.isBlank(selector)) {
      return null;
    }
    if (DomUtil.isExists(outerElm)) {
      const oElm = DomUtil._getListFirst(outerElm);
      if (!DomUtil.isExists(oElm)) {
        return null;
      }
      const retElm = oElm.querySelector(selector);
      // querySelector() returns null if not found
      return retElm;
    }
    const retElm = document.querySelector(selector);
    // querySelector() returns null if not found
    return retElm;
  },

  /**
   * Name selector (gets the first element).<br>
   * <ul>
   * <li>Returns <code>null</code> if the argument is invalid or element cannot be found.</li>
   * </ul>
   * @param {string} name the <code>name</code> attribute
   * @param {Object} [outerElm] the search scope element (optional)
   * @returns {Element|null} the retrieved HTML element
   */
  getByName: function(name, outerElm) {
    if (ValUtil.isBlank(name)) {
      return null;
    }
    const selector = `[name="${name}"]`;
    const retElm = DomUtil.getSelector(selector, outerElm);
    return retElm;
  },

  /**
   * @private
   * Name and value selector (gets the first element).<br>
   * <ul>
   * <li>For radio buttons.</li>
   * <li>Returns <code>null</code> if the argument is invalid or element cannot be found.</li>
   * </ul>
   * @param {string} name the <code>name</code> attribute
   * @param {string} value the <code>value</code> attribute
   * @param {Object} [outerElm] the search scope element (optional)
   * @returns {Element|null} the retrieved HTML element
   */
  _getByNameAndValue: function(name, value, outerElm) {
    if (ValUtil.isBlank(name) || ValUtil.isBlank(value)) {
      return null;
    }
    const selector = `[name="${name}"][value="${value}"]`;
    const retElm = DomUtil.getSelector(selector, outerElm);
    return retElm;
  },

  /**
   * data-name selector (gets the first element).<br>
   * <ul>
   * <li>Gets elements other than form input elements using an alternative attribute name for the <code>name</code> attribute.</li>
   * <li>Returns <code>null</code> if the argument is invalid or element cannot be found.</li>
   * </ul>
   * @param {string} name the <code>data-name</code> attribute
   * @param {Object} [outerElm] the search scope element (optional)
   * @returns {Element|null} the retrieved HTML element
   */
  getByDataName: function(name, outerElm) {
    if (ValUtil.isBlank(name)) {
      return null;
    }
    const selector = `[${DomUtil._ORG_ATTR_NAME}="${name}"]`;
    const retElm = DomUtil.getSelector(selector, outerElm);
    return retElm;
  },

  /**
   * @private
   * Tag selector (gets the first element).<br>
   * <ul>
   * <li>Returns <code>null</code> if the argument is invalid or element cannot be found.</li>
   * </ul>
   * @param {string} tag the HTML tag name
   * @param {Object} [outerElm] the search scope element (optional)
   * @returns {Element|null} the retrieved HTML element
   */
  _getByTag: function(tag, outerElm) {
    if (ValUtil.isBlank(tag)) {
      return null;
    }
    const selector = tag;
    const retElm = DomUtil.getSelector(selector, outerElm);
    return retElm;
  },

  /**
   * Selector (gets multiple elements).<br>
   * <ul>
   * <li>Returns an array of HTML elements.</li>
   * <li>Returns an empty array if no elements are found.</li>
   * <li>Returns <code>null</code> if the argument is invalid.</li>
   * </ul>
   * @param {string} selector the selector string
   * @param {Object} [outerElm] the search scope element (optional)
   * @returns {Array<Element>|null} the multiple HTML element array 
   */
  getsSelector: function(selector, outerElm) {
    if (ValUtil.isBlank(selector)) {
      return null;
    }
    if (DomUtil.isExists(outerElm)) {
      const oElm = DomUtil._getListFirst(outerElm);
      if (!DomUtil.isExists(oElm)) {
        return null;
      }
      const retElms = oElm.querySelectorAll(selector);
      // querySelectorAll() returns an empty NodeList if not found
      return DomUtil._listToAry(retElms);
    }
    const retElms = document.querySelectorAll(selector);
    // querySelectorAll() returns an empty NodeList if not found
    return DomUtil._listToAry(retElms);
  },

  /**
   * @private
   * Class selector (gets multiple elements).<br>
   * <ul>
   * <li>Returns an array of HTML elements.</li>
   * <li>Returns an empty array if no elements are found.</li>
   * <li>Returns <code>null</code> if the argument is invalid.</li>
   * </ul>
   * @param {string} cls the <code>class</code> attribute
   * @param {Object} [outerElm] the search scope element (optional)
   * @returns {Array<Element>|null} the multiple HTML element array 
   */
  _getsByClass: function(cls, outerElm) {
    if (ValUtil.isBlank(cls)) {
      return null;
    }
    const selector = '.' + cls;
    const retElms = DomUtil.getsSelector(selector, outerElm);
    return retElms;
  },

  /**
   * @private
   * Ancestor element ID selector (gets the first element).<br>
   * <ul>
   * <li>Searches ancestor elements from the specified element and returns the closest element matching the id.</li>
   * <li>Returns <code>null</code> if the argument is invalid or element cannot be found.</li>
   * </ul>
   * @param {Element} baseElm the base element
   * @param {string} id the <code>id</code> attribute
   * @returns {Element|null} the retrieved HTML element
   */
  _getParentById: function(baseElm, id) {
    if (ValUtil.isBlank(id)) {
      return null;
    }
    if (!DomUtil.isExists(baseElm)) {
      return null;
    }
    const selector = '#' + id;
    const retElm = baseElm.closest(selector);
    if (!DomUtil.isExists(retElm)) {
      return null;
    }
    return retElm;
  },

  /**
   * Ancestor element tag selector (gets the first element).<br>
   * <ul>
   * <li>Searches ancestor elements from the base element and returns the closest element matching the HTML tag.</li>
   * <li>Used to get the row element containing the pressed button.</li>
   * <li>Returns <code>null</code> if the argument is invalid or element cannot be found.</li>
   * </ul>
   * @param {Element} baseElm the base element
   * @param {string} tag the HTML tag name
   * @returns {Element|null} the retrieved HTML element
   */
  getParentByTag: function(baseElm, tag) {
    if (ValUtil.isBlank(tag)) {
      return null;
    }
    if (!DomUtil.isExists(baseElm)) {
      return null;
    }
    const retElm = baseElm.closest(tag);
    if (!DomUtil.isExists(retElm)) {
      return null;
    }
    return retElm;
  },

  /**
   * @private
   * Gets all direct child elements.<br>
   * <ul>
   * <li>Gets all child elements directly under the specified element (excluding text nodes).</li>
   * <li>Returns an array of HTML elements.</li>
   * <li>Returns an empty array if there are no child elements.</li>
   * <li>Returns <code>null</code> if the argument is invalid.</li>
   * </ul>
   * @param {Element} parentElm the parent element
   * @returns {Array<Element>|null} the child element array
   */
  _getAllChildren: function(parentElm) {
    if (!DomUtil.isExists(parentElm)) {
      return null;
    }
    // Use the children property (text nodes are not included)
    return DomUtil._listToAry(parentElm.children);
  },

  /**
   * Gets element value.<br>
   * <ul>
   * <li>Gets the <code>value</code> attribute value of <code>&lt;input&gt;</code>, <code>&lt;select&gt;</code>, <code>&lt;textarea&gt;</code>.</li>
   * <li>If the <code>data-value-format-type</code> attribute that defines the value format type is set on the target element, unformats the value using the corresponding method in <code>UnFrmUtil</code>.</li>
   * <li>If the target element is a text box or text area, removes tab characters, trailing spaces, and newline codes.</li>
   * <li>For checkboxes, returns the <code>value</code> attribute when checked, and returns the <code>data-check-off-value</code> attribute value when unchecked.</li>
   * <li>Returns <code>null</code> if the argument is invalid.</li>
   * </ul>
   * @param {Element} elm the target element
   * @returns {string|null} the value (unformatted)
   */
  getVal: function(elm) {
    if (!DomUtil.isExists(elm)) {
      return null;
    }
    const val = PageUtil._getElmUnFormatVal(elm);
    return val;
  },

  /**
   * Sets element value.<br>
   * <ul>
   * <li>Sets the <code>value</code> attribute value of <code>&lt;input&gt;</code>, <code>&lt;select&gt;</code>, <code>&lt;textarea&gt;</code>.</li>
   * <li>If the <code>data-value-format-type</code> attribute that defines the value format type is set on the target element, formats the value using the corresponding method in <code>FrmUtil</code>.</li>
   * <li>Returns <code>false</code> if the argument is invalid.</li>
   * </ul>
   * @param {Element} elm the target element
   * @param {string} value the value to set (before formatting)
   * @returns {boolean} <code>true</code> on successful setting
   */
  setVal: function(elm, value) {
    if (!DomUtil.isExists(elm)) {
      return false;
    }
    PageUtil._setElmFormatVal(elm, value);
    return true;
  },

  /**
   * Gets element text.<br>
   * <ul>
   * <li>Gets the <code>textContent</code> of the element.</li>
   * <li>If the <code>data-value-format-type</code> attribute that defines the value format type is set on the target element, unformats the value using the corresponding method in <code>UnFrmUtil</code>.</li>
   * <li>Returns <code>null</code> if the argument is invalid.</li>
   * </ul>
   * @param {Element} elm the target element
   * @returns {string|null} the text (unformatted)
   */
  getTxt: function(elm) {
    if (!DomUtil.isExists(elm)) {
      return null;
    }
    const val = PageUtil._getElmUnFormatVal(elm);
    return val;
  },

  /**
   * Sets element text.<br>
   * <ul>
   * <li>Sets the <code>textContent</code> of the element.</li>
   * <li>If the <code>data-value-format-type</code> attribute that defines the value format type is set on the target element, formats the value using the corresponding method in <code>FrmUtil</code>.</li>
   * <li>Returns <code>false</code> if the argument is invalid.</li>
   * </ul>
   * @param {Element} elm the target element
   * @param {string} text the text to set (before formatting)
   * @returns {boolean} <code>true</code> on successful setting
   */
  setTxt: function(elm, text) {
    if (!DomUtil.isExists(elm)) {
      return false;
    }
    PageUtil._setElmFormatVal(elm, text);
    return true;
  },

  /**
   * Toggles element enable state.<br>
   * <ul>
   * <li>Toggles the <code>disabled</code> attribute of the element.</li>
   * <li>Returns <code>false</code> if the argument is invalid.</li>
   * </ul>
   * @param {Element} elm the target element
   * @param {boolean|string} isEnable <code>true</code> to enable
   * @returns {boolean} <code>true</code> on successful toggle
   */
  setEnable: function(elm, isEnable) {
    if (!DomUtil.isExists(elm)) {
      return false;
    }
    isEnable = ValUtil.isTrue(isEnable);
    const oldEnable = !DomUtil.hasAttr(elm, 'disabled');
    if (oldEnable === isEnable) {
      // No change
      return false;
    }
    if (isEnable) {
      // Enable element
      DomUtil.removeAttr(elm, 'disabled');
    } else {
      // Disable element
      DomUtil.setAttr(elm, 'disabled', 'disabled');
    }
    return true;
  },

  /**
   * Toggles element visibility.<br>
   * <ul>
   * <li>Toggles the <code>display</code> style or <code>visibility</code> style of the element.</li>
   * <li>If keeping element space (preserving layout), toggles the <code>visibility</code> style.</li>
   * <li>Returns <code>false</code> if the argument is invalid.</li>
   * </ul>
   * @param {Element} elm the target element
   * @param {boolean|string} isShow <code>true</code> to show
   * @param {boolean} keepLayout <code>true</code> to keep element space
   * @returns {boolean} <code>true</code> on successful toggle
   */
  setVisible: function(elm, isShow, keepLayout) {
    if (!DomUtil.isExists(elm)) {
      return false;
    }
    isShow = ValUtil.isTrue(isShow);
    keepLayout = ValUtil.isTrue(keepLayout);
    
    if (keepLayout) {
      // Toggle with visibility style
      return DomUtil._setVisibilityStyle(elm, isShow);
    }

    // Toggle with display style
    return DomUtil._setDisplayStyle(elm, isShow);
  },

  /**
   * @private
   * Sets element visibility style.
   * @param {Element} elm the target element
   * @param {boolean} isShow <code>true</code> to show
   * @returns {boolean} <code>true</code> on successful toggle
   */
  _setVisibilityStyle: function(elm, isShow) {
    // Toggle with visibility style
    if (ValUtil.isTrue(isShow)) {
      if (elm.style.visibility !== 'hidden') {
        return false;
      }
      if (DomUtil.hasAttr(elm, DomUtil._ORG_ATTR_STYLE_VISIBILITY_BACKUP)) {
        // Restore backed-up visibility style
        elm.style.visibility = DomUtil.getAttr(elm, DomUtil._ORG_ATTR_STYLE_VISIBILITY_BACKUP);
      } else {
        elm.style.visibility = '';
      }
    } else {
      if (elm.style.visibility === 'hidden') {
        return false;
      }
      if (!ValUtil.isBlank(elm.style.visibility)) {
        // Back up visibility style
        DomUtil.setAttr(elm, DomUtil._ORG_ATTR_STYLE_VISIBILITY_BACKUP, elm.style.visibility);
      }
      elm.style.visibility = 'hidden';
    }
    return true;
  },

  /**
   * @private
   * Sets element display style.
   * @param {Element} elm the target element
   * @param {boolean} isShow <code>true</code> to show
   * @returns {boolean} <code>true</code> on successful toggle
   */  
  _setDisplayStyle: function(elm, isShow) {
    // Toggle with display style
    if (ValUtil.isTrue(isShow)) {
      if (elm.style.display !== 'none') {
        return false;
      }
      if (DomUtil.hasAttr(elm, DomUtil._ORG_ATTR_STYLE_DISPLAY_BACKUP)) {
        // Restore backed-up display style
        elm.style.display = DomUtil.getAttr(elm, DomUtil._ORG_ATTR_STYLE_DISPLAY_BACKUP);
      } else {
        elm.style.display = '';
      }
    } else {
      if (elm.style.display === 'none') {
        return false;
      }
      if (!ValUtil.isBlank(elm.style.display)) {
        // Back up display style
        DomUtil.setAttr(elm, DomUtil._ORG_ATTR_STYLE_DISPLAY_BACKUP, elm.style.display);
      }
      elm.style.display = 'none';
    }
    return true;
  },

  /**
   * Gets element attribute.<br>
   * <ul>
   * <li>Gets the specified attribute value.</li>
   * <li>Returns <code>null</code> if the argument is invalid.</li>
   * <li>Uses dataset API for data-* attributes.</li>
   * </ul>
   * @param {Element} elm the target element
   * @param {string} attrName the attribute name
   * @returns {string|number|null} the attribute value
   */
  getAttr: function(elm, attrName) {
    if (!DomUtil.isExists(elm) || ValUtil.isBlank(attrName)) {
      return null;
    }

    // Get using dataset API for data-* attributes
    if (attrName.startsWith('data-')) {
      const datasetKey = DomUtil._convDataAttrToDatasetKey(attrName);
      return elm.dataset[datasetKey];
    }

    return elm.getAttribute(attrName);
  },

  /**
   * Sets element attribute.<br>
   * <ul>
   * <li>Sets the specified attribute value.</li>
   * <li>Returns <code>false</code> if the argument is invalid.</li>
   * <li>Uses dataset API for data-* attributes.</li>
   * </ul>
   * @param {Element} elm the target element
   * @param {string} attrName the attribute name
   * @param {string} val the value to set
   * @returns {boolean} <code>true</code> on successful setting
   */
  setAttr: function(elm, attrName, val) {
    if (!DomUtil.isExists(elm) || ValUtil.isBlank(attrName)) {
      return false;
    }
    const value = ValUtil.nvl(val);

    // Set using dataset API for data-* attributes
    if (attrName.startsWith('data-')) {
      const datasetKey = DomUtil._convDataAttrToDatasetKey(attrName);
      elm.dataset[datasetKey] = value;
      return true;
    }

    elm.setAttribute(attrName, value);
    return true;
  },

  /**
   * Checks element attribute existence.<br>
   * <ul>
   * <li>Uses dataset API for data-* attributes.</li>
   * </ul>
   * @param {Element} elm the target element
   * @param {string} attrName the attribute name
   * @returns {boolean} <code>true</code> if exists
   */
  hasAttr: function(elm, attrName) {
    if (!DomUtil.isExists(elm) || ValUtil.isBlank(attrName)) {
      return false;
    }

    // Get using dataset API for data-* attributes
    if (attrName.startsWith('data-')) {
      const datasetKey = DomUtil._convDataAttrToDatasetKey(attrName);
      // Check attribute existence
      if (ValUtil.isNull(elm.dataset[datasetKey])) {
        return false;
      }
      return true;
    }

    return elm.hasAttribute(attrName);
  },

  /**
   * Removes element attribute.<br>
   * <ul>
   * <li>Removes the specified attribute.</li>
   * <li>Returns <code>false</code> if the argument is invalid.</li>
   * <li>Uses dataset API for data-* attributes.</li>
   * </ul>
   * @param {Element} elm the target element
   * @param {string} attrName the attribute name
   * @returns {boolean} <code>true</code> on successful removal
   */
  removeAttr: function(elm, attrName) {
    if (!DomUtil.isExists(elm) || ValUtil.isBlank(attrName)) {
      return false;
    }

    // Remove using dataset API for data-* attributes
    if (attrName.startsWith('data-')) {
      const datasetKey = DomUtil._convDataAttrToDatasetKey(attrName);
      // Check attribute existence
      if (ValUtil.isNull(elm.dataset[datasetKey])) {
        return false;
      }
      delete elm.dataset[datasetKey];
      return true;
    }

    // Check attribute existence
    if (!elm.hasAttribute(attrName)) {
      return false;
    }
    elm.removeAttribute(attrName);
    return true;
  },

  /**
   * @private
   * Converts data-* attribute name to dataset key name.<br>
   * <ul>
   * <li>[Example] <code>'data-obj-row-idx'</code> → <code>'objRowIdx'</code></li>
   * <li>[Example] <code>'data-check-off-value'</code> → <code>'checkOffValue'</code></li>
   * </ul>
   * @param {string} attrName the data-* attribute name
   * @returns {string} the dataset key name
   */
  _convDataAttrToDatasetKey: function(attrName) {
      if (attrName.indexOf('data-') !== 0) {
        return attrName;
    }
    // Remove 'data-'
    const datasetKey = attrName.substring(5);

    // Convert hyphens to camelCase
    return datasetKey.replace(/-([a-z])/g, function(match, letter) {
      return letter.toUpperCase();
    });
  },

  /**
   * Adds CSS class.<br>
   * <ul>
   * <li>Adds a CSS class to the element.</li>
   * <li>Returns <code>false</code> if the argument is invalid.</li>
   * </ul>
   * @param {Element} elm the target element
   * @param {string} cls the class name
   * @returns {boolean} <code>true</code> on successful addition
   */
  addClass: function(elm, cls) {
    if (!DomUtil.isExists(elm) || ValUtil.isBlank(cls)) {
      return false;
    }
    elm.classList.add(cls);
    return true;
  },

  /**
   * Removes CSS class.<br>
   * <ul>
   * <li>Removes a CSS class from the element.</li>
   * <li>Returns <code>false</code> if the argument is invalid.</li>
   * <li>Returns <code>false</code> if the class to remove doesn't exist.</li>
   * </ul>
   * @param {Element} elm the target element
   * @param {string} cls the class name
   * @returns {boolean} <code>true</code> on successful removal
   */
  removeClass: function(elm, cls) {
    if (!DomUtil.isExists(elm) || ValUtil.isBlank(cls) || !DomUtil.hasClass(elm, cls)) {
      return false;
    }
    elm.classList.remove(cls);
    return true;
  },

  /**
   * Checks CSS class existence.<br>
   * <ul>
   * <li>Checks whether the element has the specified CSS class.</li>
   * </ul>
   * @param {Element} elm the target element
   * @param {string} cls the class name
   * @returns {boolean} <code>true</code> if the element has the class
   */
  hasClass: function(elm, cls) {
    if (!DomUtil.isExists(elm) || ValUtil.isBlank(cls)) {
      return false;
    }
    return elm.classList.contains(cls);
  },
};

/**
 * Page manipulation utility class.<br>
 * <ul>
   *   <li>Performs operations on the entire page or specified areas of the page.</li>
   *   <li>Performs operations such as displaying messages, checking for errors, clearing forms, enabling/disabling forms, and showing/hiding forms.</li>
   *   <li>Both lists (repeating rows) and details (repeating rows) are collectively called "lists". Sections explaining lists apply to both.</li>
 * </ul>
 * @class
 */
const PageUtil = /** @lends PageUtil */ {

  /** @private <code>id</code> attribute name of message display area element and message array key in response data (specified in Io.java). */
  _ITEMID_MSG: '_msg',
  /** @private Key that becomes <code>true</code> if there are error messages (specified in Io.java). */
  _ITEMID_HAS_ERR: '_has_err',
  /** @private Attribute name for backing up <code>title</code> attribute. */
  _ORG_ATTR_TITLE_BACKUP: 'data-title-backup',
  /** @private <code>name</code> attribute name for list section radio buttons when converted to object. */
  _ORG_ATTR_DETAIL_RADIO_OBJ_NAME: 'data-radio-obj-name',

  /**
   * Displays messages.<br>
   * <ul>
   *   <li>Displays messages from response data.</li>
   *   <li>The key in response data is <code>'_msg'</code>.</li>
   *   <li>Elements with <code>id</code> attribute <code>'_msg'</code> become message display area elements.</li>
   *   <li>If multiple message display area elements exist, sets to the first element.</li>
   *   <li>To control display, execute <code>PageUtil#clearMsg()</code> in page initialization processing to hide.</li>
   *   <li>If no messages exist in response data, clears the text of the message display area element. (<code>PageUtil#clearMsg()</code> is executed)</li>
   * </ul>
   * @param {Object} res the response data
   */
  setMsg: function(res) {
    if (!ValUtil.isObj(res)) {
      throw new Error('PageUtil#setMsg: Argument response is invalid. ');
    }
    const msgs = res[PageUtil._ITEMID_MSG];
    if (ValUtil.isEmpty(msgs)) {
      PageUtil.clearMsg();
      return;
    }
    // Display messages in message display area
    const msgElm = DomUtil.getById(PageUtil._ITEMID_MSG);
    if (!DomUtil.isExists(msgElm)) {
      throw new Error('PageUtil#setMsg: Message element not found. ');
    }
    let msgHtml = '<ul>';
    for (const msg of msgs) {
      if (!ValUtil.isObj(msg)) {
        throw new Error('PageUtil#setMsg: Message is invalid. ');
      }
      let cls = '';
      if (msg['type'] === 'INFO') {
        cls = 'info-msg';
      } else if (msg['type'] === 'WARN') {
        cls = 'warn-msg';
      } else {
        cls = 'err-msg';
      }
      msgHtml += (`<li class="${cls}">${msg['text']}</li>`);
    }
    msgHtml += '</ul>';
    msgElm.innerHTML = msgHtml;
    DomUtil.setVisible(msgElm, true, false);    
    // Scroll to message area
    msgElm.scrollIntoView(true);

    // Highlight fields and set message to <code>title</code> attribute
    for (const msg of msgs) {
      const itemName = msg['item'];
      const rowIdx = msg['row'];
      if (!ValUtil.isBlank(itemName)) {
        let elm;
        if (!ValUtil.isBlank(rowIdx)) {
          // If row index value is specified, get considering <code>data-obj-row-idx</code> attribute
          elm = DomUtil.getSelector(`[name="${itemName}"][${DomUtil._ORG_ATTR_OBJ_ROW_INDEX}="${rowIdx}"]`);
        } else {
          elm = DomUtil.getByName(itemName);
        }
        if (DomUtil.isExists(elm)) {
          let cls = '';
          if (msg['type'] === 'INFO') {
            cls = 'info-item';
          } else if (msg['type'] === 'WARN') {
            cls = 'warn-item';
          } else {
            cls = 'err-item';
          }
          // Add CSS class
          DomUtil.addClass(elm, cls);
          // Back up <code>title</code> attribute and set message
          if (!ValUtil.isBlank(elm.title)) {
            DomUtil.setAttr(elm, PageUtil._ORG_ATTR_TITLE_BACKUP, elm.title);
          }
          elm.title = msg['text'];
        }
      }
    }
  },

  /**
   * Checks for error existence.<br>
   * <ul>
   *   <li>Checks for error existence from response data.</li>
   *   <li>The key in response data is <code>'_has_err'</code>.</li>
   * </ul>
   * @param {Object} res the response data
   * @returns {boolean} <code>true</code> if errors exist
   */
  hasError: function (res) {
    if (!ValUtil.isObj(res)) {
      throw new Error('PageUtil#hasErr: Argument response is invalid. ');
    }
    const hasErr = res[PageUtil._ITEMID_HAS_ERR];
    return ValUtil.isTrue(hasErr);
  },

  /**
   * Clears messages.<br>
   * <ul>
   *   <li>Clears the text of the message display area element.</li>
   * </ul>
   */
  clearMsg: function() {
    const msgElm = DomUtil.getById(PageUtil._ITEMID_MSG);
    if (!DomUtil.isExists(msgElm)) {
      throw new Error('PageUtil#clearMsg: Message element not found. ');
    }
    msgElm.innerHTML = '<ul></ul>';
    DomUtil.setVisible(msgElm, false, false);
    
    // Remove field highlight and restore <code>title</code> attribute
    const elms = DomUtil.getsSelector('.info-item, .warn-item, .err-item');
    for (const elm of elms) {
      // Remove CSS classes 
      DomUtil.removeClass(elm, 'info-item');
      DomUtil.removeClass(elm, 'warn-item');
      DomUtil.removeClass(elm, 'err-item');
      // Restore <code>title</code> attribute
      if (DomUtil.hasAttr(elm, PageUtil._ORG_ATTR_TITLE_BACKUP)) {
        elm.title = DomUtil.getAttr(elm, PageUtil._ORG_ATTR_TITLE_BACKUP);
      } else {
        elm.title = '';
      }
    }
  },

  /**
   * Gets page data.<br>
   * <ul>
   *   <li>Gets values of data-sending HTML elements (<code>&lt;input&gt;</code>, <code>&lt;select&gt;</code>, <code>&lt;textarea&gt;</code>) on the page as an associative array.</li>
   *   <li>HTML elements to be retrieved have the <code>name</code> attribute set, and the <code>name</code> attribute becomes the key of the associative array.</li>
   *   <li>If the argument retrieval range element is omitted, <code>&lt;main&gt;</code> is the retrieval range, and if <code>&lt;main&gt;</code> does not exist, <code>document.body</code> is the retrieval range.</li>
   *   <li>Mainly retrieves as request data to web services.</li>
   *   <li>The following differs from normal <code>&lt;form&gt;</code> POST submission.
   *   <ul>
   *     <li>Includes disabled items.</li>
   *     <li>Does not include style-hidden items (<code>display:none</code> or <code>visibility:hidden</code>).</li>
   *   </ul></li>
   *   <li>If the <code>data-value-format-type</code> attribute defining the value format type is set, gets the unformatted value with the corresponding <code>UnFrmUtil</code> method.</li>
   *   <li>For text boxes and text areas, gets values with tab characters, trailing blanks, and line breaks removed.</li>
   *   <li>For checkboxes, returns the value of the <code>value</code> attribute when checked, and gets the value of the <code>data-check-off-value</code> attribute when unchecked.</li>
   *   <li>List part (repeated part) data becomes an array and is stored in the associative array with one key.</li>
   *   <li><pre>[Example] <code>&lt;input name="user_id" value="U001"&gt;
   *      &lt;input name="birth_dt" value="2025/02/10"&gt;
   *      &lt;table&gt;...omitted...&lt;tbody id="detail"&gt;
   *         &lt;tr&gt;&lt;td&gt;&lt;input name="detail.pet_no" value="1"&gt;&lt;/td&gt;
   *             &lt;td&gt;&lt;input name="detail.weight_kg" value="8.9"&gt;&lt;/td&gt;&lt;/tr&gt;
   *         &lt;tr&gt;&lt;td&gt;&lt;input name="detail.pet_no" value="2"&gt;&lt;/td&gt;
   *             &lt;td&gt;&lt;input name="detail.weight_kg" value="12.1"&gt;&lt;/td&gt;&lt;/tr&gt;
   *       &lt;/tbody&gt;&lt;/table&gt;</code> is
   *       retrieved as <code>{ user_id:'U001', birth_dt:'20250210', detail:[{pet_no:'1', weight_kg:'8.9'}, {pet_no:'2', weight_kg:'12.1'}] }</code>.</pre></li>
   *   <li>Elements of list parts must follow the rules below.
   *   <ul> 
   *     <li>Elements within a row (hereinafter called row-internal elements) have the <code>name</code> attribute separated by <code>"."</code> and set in the format <code>tableId.itemName</code>. Note that <code>name</code> attributes separated by <code>"."</code> should only be used for row-internal elements.</li>
   *     <li>An element (hereinafter called table element) with the <code>id</code> attribute set to <code>tableId</code>, the part before the <code>"."</code> separator, must exist as the parent or grandparent element of row-internal elements.<br>
   *         In most cases, the table element is <code>&lt;tbody&gt;</code> or <code>&lt;table&gt;</code>.</li>
   *     <li>The child element directly under the table element must be the top-level element of the repeated part (hereinafter called row element).<br>
   *         In most cases, the row element is <code>&lt;tr&gt;</code>.</li>
   *     <li><pre>[NG Example 1] Table element does not exist. (Neither <code>&lt;table&gt;</code> nor <code>&lt;tbody&gt;</code> has the <code>id</code> attribute)
   *       <code>&lt;table&gt;...omitted...&lt;tbody&gt;
   *         &lt;tr&gt;&lt;td&gt;&lt;input name="detail.pet_nm"&gt;&lt;/td&gt;&lt;/tr&gt;
   *         &lt;tr&gt;&lt;td&gt;&lt;input name="detail.pet_nm"&gt;&lt;/td&gt;&lt;/tr&gt;
   *       &lt;/tbody&gt;&lt;/table&gt;</code></pre></li>
   *     <li><pre>[NG Example 2] Row element does not exist directly under table element. (The <code>id</code> attribute is assigned to <code>&lt;table&gt;</code>, but <code>&lt;tbody&gt;</code> is in between)
   *       <code>&lt;table id="detail"&gt;...omitted...&lt;tbody&gt;
   *         &lt;tr&gt;&lt;td&gt;&lt;input name="detail.pet_nm"&gt;&lt;/td&gt;&lt;/tr&gt;
   *         &lt;tr&gt;&lt;td&gt;&lt;input name="detail.pet_nm"&gt;&lt;/td&gt;&lt;/tr&gt;
   *       &lt;/tbody&gt;&lt;/table&gt;</code></pre></li>
   *     <li><pre>[OK Example 1] When the <code>id</code> attribute is assigned to <code>&lt;tbody&gt;</code>, <code>&lt;tbody&gt;</code> becomes the table element.
   *       <code>&lt;table&gt;...omitted...&lt;tbody id="detail"&gt;
   *         &lt;tr&gt;&lt;td&gt;&lt;input name="detail.pet_nm"&gt;&lt;/td&gt;&lt;/tr&gt;
   *         &lt;tr&gt;&lt;td&gt;&lt;input name="detail.pet_nm"&gt;&lt;/td&gt;&lt;/tr&gt;
   *       &lt;/tbody&gt;&lt;/table&gt;</code></pre></li>
   *     <li><pre>[OK Example 2] When the <code>id</code> attribute is assigned to <code>&lt;table&gt;</code>, <code>&lt;table&gt;</code> becomes the table element. (Example using multiple <code>&lt;tbody&gt;</code>)
   *       <code>&lt;table id="detail"&gt;...omitted...
   *         &lt;tbody&gt;&lt;tr&gt;&lt;td&gt;&lt;input name="detail.pet_nm"&gt;&lt;/td&gt;&lt;/tr&gt;&lt;/tbody&gt;
   *         &lt;tbody&gt;&lt;tr&gt;&lt;td&gt;&lt;input name="detail.pet_nm"&gt;&lt;/td&gt;&lt;/tr&gt;&lt;/tbody&gt;
   *       &lt;/table&gt;</code></pre></li>
   *   </ul></li>
   *   <li>Row index values are stored in row-internal elements as the <code>data-obj-row-idx</code> attribute, and they are converted to arrays based on that index.</li>
   *   <li>For radio buttons within rows, removes the [row index] at the end of the <code>name</code> attribute to use as the return value key. (See <code>PageUtil#setValue</code>)</li>
   * </ul>
   * @param {Object} [outerElm] Retrieval range element (optional)
   * @returns {Object} Page data associative array (unformatted)
   */
  getValues: function(outerElm) {
    outerElm = outerElm || DomUtil._getByTag('main') || document.body;
    if (!DomUtil.isExists(outerElm)) {
      throw new Error('PageUtil#getValues: Argument element is invalid. ');
    }

    // Adds row index
    PageUtil._setRowIndex(outerElm);
    // Gets target elements
    const targetElms = DomUtil.getsSelector('input[name],select[name],textarea[name]', outerElm);

    const jsonData = {};
    const listObj = {};
    for (const elm of targetElms) {
      if (!DomUtil.isVisible(elm)) {
        // Ignores hidden elements
        continue;
      }
      if (PageUtil._isRadioOff(elm)) {
        // Ignores unchecked radio buttons
        continue;
      }
      const name = elm.getAttribute('name');
      const listNameSepPos = name.indexOf('.');
      if (listNameSepPos > 0 && DomUtil.hasAttr(elm, DomUtil._ORG_ATTR_OBJ_ROW_INDEX)) {
        // List conversion
        // Stores row arrays in a map temporarily
        const listId = name.substring(0, listNameSepPos);
        let colName = name.substring(listNameSepPos + 1);
        const nameIndexWrapPos = colName.indexOf('[');
        if (nameIndexWrapPos > 1) {
          // For radio buttons within rows, removes the [n] from the <code>name</code> attribute
          colName = colName.substring(0, nameIndexWrapPos);
        }
        const rowIdx = ~~DomUtil.getAttr(elm, DomUtil._ORG_ATTR_OBJ_ROW_INDEX);
        if (ValUtil.isNull(listObj[listId])) {
          // Creates a new array if not in the map
          listObj[listId] = [];
        }
        const list = listObj[listId];
        let row = list[rowIdx];
        if (ValUtil.isNull(row)) {
          // Creates a new row object if not in the array
          row = {};
          list[rowIdx] = row;
        }
        row[colName] = PageUtil._getElmUnFormatVal(elm);
      } else {
        if (jsonData[name]) {
          throw new Error(`PageUtil#getValues: Name attribute is duplicated. name = ${name}`);
        }
        jsonData[name] = PageUtil._getElmUnFormatVal(elm);
      }
    }

    for (const listId in listObj) {
      if (jsonData[listId]) {
        throw new Error(`PageUtil#getValues: Name attribute is duplicated. listId = ${listId}`);
      }
      jsonData[listId] = listObj[listId];
    }
    return jsonData;
  },


  /**
   * Gets row data.<br>
   * <ul>
   *   <li>Gets page data of one row in a list as an associative array.</li>
   *   <li>Data retrieval rules are the same as <code>PageUtil#getValues</code>.</li>
   *   <li>The part before <code>"."</code> in the <code>name</code> attribute (= the <code>id</code> attribute of the table element) is removed to become the key of the associative array.</li>
   *   <li><pre>[Example] When the second row of <code>&lt;table&gt;...omitted...&lt;tbody id="detail"&gt;
   *         &lt;tr&gt;&lt;td&gt;&lt;input name="detail.pet_no" value="1"&gt;&lt;/td&gt;
   *             &lt;td&gt;&lt;input name="detail.weight_kg" value="8.9"&gt;&lt;/td&gt;&lt;/tr&gt;
   *         &lt;tr&gt;&lt;td&gt;&lt;input name="detail.pet_no" value="2"&gt;&lt;/td&gt;
   *             &lt;td&gt;&lt;input name="detail.weight_kg" value="12.1"&gt;&lt;/td&gt;&lt;/tr&gt;
   *       &lt;/tbody&gt;&lt;/table&gt;</code> is specified as an argument,
   *       <code>{ pet_no:'2', weight_kg:'12.1' }</code> is retrieved.</pre></li>
   *   <li>For radio buttons within rows, removes the [row index] at the end of the <code>name</code> attribute to use as the return value key. (See <code>PageUtil#setValues</code>)</li>
   * </ul>
   * @param {Element} rowElm Row element (typically <code>&lt;tr&gt;</code>)
   * @returns {Object} Row data associative array (unformatted)
   */
  getRowValues: function(rowElm) {
    if (!DomUtil.isExists(rowElm)) {
      throw new Error('PageUtil#getRowValues: Argument element is invalid. ');
    }

    // Gets target elements
    const targetElms = DomUtil.getsSelector('input[name],select[name],textarea[name]', rowElm);

    const jsonData = {};
    for (const elm of targetElms) {
      if (!DomUtil.isVisible(elm)) {
        // Ignores hidden elements
        continue;
      }
      if (PageUtil._isRadioOff(elm)) {
        // Ignores unchecked radio buttons
        continue;
      }
      let colName = elm.getAttribute('name');
      const listNameSepPos = colName.indexOf('.');
      if (listNameSepPos > 0) {
        colName = colName.substring(listNameSepPos + 1);
        const nameIndexWrapPos = colName.indexOf('[');
        if (nameIndexWrapPos > 1) {
          // For radio buttons within rows, removes [n] from the <code>name</code> attribute
          colName = colName.substring(0, nameIndexWrapPos);
        }
      }
      if (jsonData[colName]) {
        throw new Error(`PageUtil#getRowValues: Name attribute is duplicated. name = ${colName}`);
      }
      jsonData[colName] = PageUtil._getElmUnFormatVal(elm);
    }
    return jsonData;
  },

  /**
   * Gets row data.<br>
   * <ul>
   *   <li>Traverses parent elements from the argument base element to get the row element (typically <code>&lt;tr&gt;</code>), and gets the page data of that row element as an associative array.</li>
   *   <li>Data retrieval rules are the same as <code>PageUtil#getRowValues</code>.</li>
   * </ul> 
   * @param {Element} baseElm Base element
   * @param {string} [rowTag] Tag name of row element (optional) Defaults to 'tr' if omitted
   * @returns {Object} Row data associative array (unformatted)
   */
  getRowValuesByInnerElm: function(baseElm, rowTag) {
    rowTag = rowTag || 'tr';
    if (!DomUtil.isExists(baseElm)) {
      throw new Error('PageUtil#getRowValuesByInnerElm: Argument element is invalid. ');
    }
    const rowElm = DomUtil.getParentByTag(baseElm, rowTag);
    if (!DomUtil.isExists(rowElm)) {
      throw new Error(`Row element not found. rowTag = ${rowTag}, baseElm = ${baseElm.name}`);
    }
    return PageUtil.getRowValues(rowElm);
  },

  /**
   * Sets page data.<br>
   * <ul>
   *   <li>Sets values from an associative array to HTML elements on the page.</li>
   *   <li>HTML elements to be set must have the <code>name</code> attribute or <code>data-name</code> attribute, and the key of the associative array becomes the value destination <code>name</code> attribute or <code>data-name</code> attribute.</li>
   *   <li>For elements other than input form elements (which originally do not have the <code>name</code> attribute) such as <code>&lt;span&gt;</code> or <code>&lt;td&gt;</code>, sets the <code>data-name</code> attribute.</li>
   *   <li>For HTML elements that do not have the <code>value</code> attribute such as <code>&lt;span&gt;</code> or <code>&lt;td&gt;</code>, sets to <code>textContent</code>.</li>
   *   <li>If the argument set range element is omitted, <code>&lt;main&gt;</code> is the set range, and if <code>&lt;main&gt;</code> does not exist, <code>document.body</code> is the set range.</li>
   *   <li>Mainly sets response data from web services.</li>
   *   <li>Keys of the associative array starting with underscore are values used by this framework and cannot be used from outside this class, so they are ignored in this method.</li>
   *   <li>If the <code>data-value-format-type</code> attribute defining the value format type is set, sets the value formatted with the corresponding <code>FrmUtil</code> method.</li>
   *   <li>List part data is assumed to be stored as an array in one key within the associative array.</li>
   *   <li><pre>[Example] <code>{ user_id:'U001', birth_dt:'20250210', list:[{pet_no:'1', weight_kg:'8.9'}, {pet_no:'2', weight_kg:'12.1'}] }</code> is
   *     set as <code>&lt;input name="user_id" value="U001"&gt;
   *     &lt;input name="birth_dt" value="2025/02/10"&gt;
   *     &lt;table&gt;...omitted...&lt;tbody id="detail"&gt;
   *       &lt;tr&gt;&lt;td data-name="detail.pet_no"&gt;1&lt;/td&gt;
   *           &lt;td&gt;&lt;input name="detail.weight_kg" value="8.9"&gt;&lt;/td&gt;&lt;/tr&gt;
   *       &lt;tr&gt;&lt;td data-name="detail.pet_no"&gt;2&lt;/td&gt;
   *           &lt;td&gt;&lt;input name="detail.weight_kg" value="12.1"&gt;&lt;/td&gt;&lt;/tr&gt;
   *     &lt;/tbody&gt;&lt;/table&gt;</code>.</pre></li>
   *   <li>Rules for table elements, row elements, and row-internal elements within lists are the same as <code>PageUtil#getValues</code>.</li>
   *   <li>To link and display the number of arrays in the associative array with the number of rows in the list, dynamically generates elements from the template row element (hereinafter called template row element) and sets values.</li>
   *   <li>The template row element is placed at the child element (top) of the table element, enclosed in <code>&lt;script&gt;</code>.</li>
   *   <li><pre>[Example] In the case of the above example, place the template row element as follows.
   *     <code>&lt;table&gt;...omitted...&lt;tbody id="detail"&gt;
   *       &lt;script type="text/html"&gt;&lt;tr&gt;&lt;td data-name="detail.pet_no"&gt;&lt;/td&gt;
   *                                    &lt;td&gt;&lt;input name="detail.weight_kg"&gt;&lt;/td&gt;&lt;/tr&gt;
   *       &lt;/script&gt;
   *     &lt;/tbody&gt;&lt;/table&gt;</code></pre></li>
   *   <li>Radio buttons within template row elements have [row index] appended to the end of the <code>name</code> attribute to group them per row.</li>
   * </ul>
   * @param {Object} obj Associative array data
   * @param {Element} [outerElm] Set range element (optional) If omitted, <code>document.body</code> is the target
   */
  setValues: function(obj, outerElm) {
    if (ValUtil.isNull(obj) || typeof (obj) !== 'object') {
      throw new Error('PageUtil#setValues: Argument is invalid. ');
    }

    outerElm = outerElm || DomUtil._getByTag('main') || document.body;
    if (!DomUtil.isExists(outerElm)) {
      throw new Error('PageUtil#setValues: Argument element is invalid. ');
    }

    for (const name in obj) {
      if (typeof (name) !== 'string') {
        continue;
      }
      // Keys starting with underscore are values used by the framework and cannot be used from outside this class, so this method ignores them
      if (name.indexOf('_') === 0) {
        continue;
      }

      let val = obj[name];

      // For arrays, sets to list part
      if (ValUtil.isAry(val)) {
        PageUtil._setRowValues(name, val, outerElm);
        continue;
      }

      if (val != null) {
        const valto = typeof (val);
        if (valto !== 'string' && valto !== 'number' && valto !== 'boolean') {
          // Skips if not a primitive type
          continue;
        }
      }
      val = ValUtil.nvl(val);
      PageUtil._getElmToSetElmFormatVal(name, val, outerElm);
    }
  },

  /**
   * @private
   * Adds list rows (multiple rows).<br>
   * <ul>
   *   <li>Assumes template row element exists directly under the argument list parent element, generates row elements for the array data of the argument associative array, sets values, and adds them to the list parent element.</li>
   * </ul>
   * @param {string} listId <code>id</code> attribute of table element (parent element) ([Example] <code>'detail'</code>)
   * @param {Element} listElm Table element (parent element)
   * @param {Array<Object>} objAry Row data array (each element is an associative array representing one row of data)
   */
  _addRows: function(listId, listElm, objAry) {
    // Gets template row
    const templateScript = DomUtil._getByTag('script', listElm);
    if (!DomUtil.isExists(templateScript)) {
      console.warn(`PageUtil#_addRows: Template script not found in list. id=${listId}`);
      return;
    }
    const tempHtml = templateScript.innerHTML.trim();
    const tempHtmls = PageUtil._splitHtmlTagsOuterInner(tempHtml);
    if (ValUtil.isEmpty(tempHtmls)) {
      console.warn(`PageUtil#_addRows: Template script is invalid HTML. id=${listId}`);
      return;
    }
    const outerHtmlBegin = tempHtmls[0];
    const innerHtml = tempHtmls[1];

    // Extracts element name and attributes from opening tag
    const rowElmInfo = PageUtil._parseHtmlOpenTag(outerHtmlBegin);
    if (ValUtil.isEmpty(rowElmInfo)) {
      console.error(`PageUtil#_addRows: Failed to parse row tag. openTag=${outerHtmlBegin}`);
      return;
    }
    const rowElmTag = rowElmInfo[0];
    const rowElmAttrs = rowElmInfo[1];

    // Row index for radio buttons
    let radioRowIdx = -1;
    const oldRowElms = DomUtil.getsSelector(`${rowElmTag}`, listElm);
    // Gets current max row index
    if (DomUtil.isExists(oldRowElms)) {
      // Gets radio buttons within each row element that have [row index] at the end of the <code>name</code> attribute
      for (const oldRowElm of oldRowElms) {
        const radioElm = DomUtil.getSelector('input[type="radio"][name*="["][name$="]"]', oldRowElm);
        if (DomUtil.isExists(radioElm)) {
          const name = DomUtil.getAttr(radioElm, 'name');
          const idx = ~~name.substring(name.lastIndexOf('[') + 1, name.length - 1);
          if (idx > radioRowIdx) {
            radioRowIdx = idx;
          }
        }
      }
    }

    // Generates rows for data
    for (const obj of objAry) {
      // Generates row element from template
      const rowElm = document.createElement(rowElmTag);
      // Sets row element attributes
      for (const attrName in rowElmAttrs) {
        DomUtil.setAttr(rowElm, attrName, rowElmAttrs[attrName]);
      }
      // Sets row-internal elements
      rowElm.innerHTML = innerHtml;

      // Sets values to row-internal elements
      if (ValUtil.isObj(obj)) {
        for (const colName in obj) {
          const val = ValUtil.nvl(obj[colName]);
          const name = listId + '.' + colName;
          PageUtil._getElmToSetElmFormatVal(name, val, rowElm);
        }
      }
      // For radio buttons within row elements, appends [row index] to the end of the <code>name</code> attribute to group them per row.
      // The original <code>name</code> attribute is stored in the <code>data-radio-obj-name</code> attribute
      radioRowIdx++;
      const radioElms = DomUtil.getsSelector('input[type="radio"][name]', rowElm);
      for (const radioElm of radioElms) {
        const name = radioElm.getAttribute('name');
        const rotName = name + `[${radioRowIdx}]`;
        DomUtil.setAttr(radioElm, 'name', rotName);
        DomUtil.setAttr(radioElm, PageUtil._ORG_ATTR_DETAIL_RADIO_OBJ_NAME, name);
      }

      // Adds row element to table element
      listElm.appendChild(rowElm);
    }
  },

  /**
   * Adds list row.<br>
   * <ul>
   *   <li>Generates and adds a row element with the template row element of the table element and sets default values.</li>
   *   <li>For template rows, see <code>PageUtil#setValues</code>.</li>
   *   <li><pre>[Example] In the case of the template row below.
   *     <code>&lt;table&gt;...omitted...&lt;tbody id="detail"&gt;
   *       &lt;script type="text/html"&gt;&lt;tr&gt;&lt;td data-name="detail.pet_no"&gt;&lt;/td&gt;
   *                                    &lt;td&gt;&lt;input name="detail.weight_kg"&gt;&lt;/td&gt;&lt;/tr&gt;
   *       &lt;/script&gt;
   *     &lt;/tbody&gt;&lt;/table&gt;</code>
   *     Keys of default values are as follows, excluding the table element <code>id</code> attribute (detail.).
   *    <code>{ pet_no:'1', weight_kg:'8.9' }</code>
   *    If an array of associative arrays is passed, multiple rows are added.
   *    <code>[ { pet_no:'1', weight_kg:'8.9' }, { pet_no:'2', weight_kg:'12.1' } ]</code></pre>
   *   </li>
   *   <li>If default values are omitted, adds one empty row element.</li>
   *   <li>To add multiple empty rows, pass <code>new Array(n)</code>.</li>
   * </ul>
   * @param {string} listId <code>id</code> attribute of table element (parent element) ([Example] <code>'detail'</code>)
   * @param {Object|Array<Object>} [obj] Default value associative array when adding row (optional)
   */
  addRow: function(listId, obj) {
    obj = obj || {};
    if (ValUtil.isBlank(listId)) {
      throw new Error('PageUtil#addRow: Argument listId is invalid. ');
    }
    const listElm = DomUtil.getById(listId);
    if (!DomUtil.isExists(listElm)) {
      console.warn(`PageUtil#addRow: List element not found. id=${listId}`);
      return;
    }
    if (ValUtil.isAry(obj)) {
      PageUtil._addRows(listId, listElm, obj);
    } else {
      PageUtil._addRows(listId, listElm, [obj]);
    }
  },

  /**
   * Removes list row.<br>
   * <ul>
   *   <li>Removes the row element (typically <code>&lt;tr&gt;</code>) that contains an element with the specified <code>name</code> attribute and <code>value</code> attribute.</li>
   * </ul>
   * @param {string} searchElmName <code>name</code> attribute of search target element ([Example] For checkboxes <code>'detail.chk'</code>) <code>data-name</code> attribute is not allowed
   * @param {string} searchElmVal Value of search target element ([Example] <code>'1'</code>)
   * @param {string} [rowTag] Tag name of row element (optional) Defaults to <code>'tr'</code> if omitted
   * @returns {boolean} <code>true</code> on successful removal
   */
  removeRow: function(searchElmName, searchElmVal, rowTag) {
    rowTag = rowTag || 'tr';
    if (ValUtil.isBlank(searchElmName) || ValUtil.isBlank(searchElmVal)) {
      throw new Error(`PageUtil#removeRow: Argument is invalid. name=${searchElmName} value=${searchElmVal}`);
    }
    const searchElms = DomUtil.getsSelector(`[name="${searchElmName}"][value="${searchElmVal}"]`);
    if (searchElms.length <= 0) {
      console.warn(`PageUtil#removeRow: Element not found. searchElmName=${searchElmName} searchElmVal=${searchElmVal}`);
      return false;
    }
    let found = false;
    for (const elm of searchElms) {
      if (PageUtil._isCheckType(elm) && !elm.checked) {
        // Ignores unchecked checkboxes and radio buttons
        continue;
      }
      const rowElm = DomUtil.getParentByTag(elm, rowTag);
      if (!DomUtil.isExists(rowElm)) {
        console.warn(`PageUtil#removeRow: Row element not found. rowTag=${rowTag} searchElmName=${searchElmName} searchElmVal=${searchElmVal}`);
        continue;
      }
      rowElm.parentNode.removeChild(rowElm);
      found = true;
    }
    if (!found) {
      console.warn(`PageUtil#removeRow: Element not found. (non checked) rowTag=${rowTag} searchElmName=${searchElmName} searchElmVal=${searchElmVal}`);
      return false;
    }
    return true;
  },

  /**
   * Deletes all rows.<br>
   * <ul>
   *   <li>Removes all row elements except the template row.</li>
   * </ul>
   * @param {string} listId <code>id</code> attribute of table element (parent element) ([Example] <code>'detail'</code>)
   */
  clearRows: function (listId) {
    if (ValUtil.isBlank(listId)) {
      throw new Error('PageUtil#clearRows: Argument listId is invalid. ');
    }
    const listElm = DomUtil.getById(listId);
    if (!DomUtil.isExists(listElm)) {
      console.warn(`PageUtil#clearRows: List element not found. id=${listId}`);
      return;
    }
    PageUtil._removeAllRows(listElm);
  },

  /**
   * @private
   * Deletes all rows.<br>
   * <ul>
   *   <li>Removes all row elements except the template row.</li>
   * </ul>
   * @param {Element} listElm Table element (parent element)
   */
  _removeAllRows: function(listElm) {
    // Removes all existing rows (except template row)
    const oldRowElms = DomUtil._getAllChildren(listElm);
    for (const rowElm of oldRowElms) {
      if (rowElm.tagName.toLowerCase() === 'script') {
        continue;
      }
      listElm.removeChild(rowElm);
    }
  },

  /**
   * @private
   * Adds index to cell elements with custom attribute.<br>
   * <ul>
   *   <li>Targets cell elements (elements with <code>name</code> attribute containing <code>'."</code>) that are data submission elements (<code>&lt;input&gt;</code>, <code>&lt;select&gt;</code>, <code>&lt;textarea&gt;</code>).</li>
   *   <li>Adds index as <code>data-obj-row-idx</code> attribute.</li>
   *   <li>The added index is used when converting to associative array in <code>PageUtil#getValues</code>.</li>
   *   <li>The added index also serves as a marker for displaying response values when returned from web service.</li>
   *   <li>The added index starts from zero and increments for each row element.</li>
   *   <li>Ignores rows that have no cell elements in their child or descendant elements. (Does not increment)</li>
   *   <li>See JSDoc of <code>PageUtil#getValues</code> for details.</li>
   * </ul>
   * @param {Object} outerElm Index addition range element
   */
  _setRowIndex : function(outerElm) {
    if (ValUtil.isNull(outerElm)) {
      throw new Error('PageUtil#_setRowIndex: Target element required.');
    }
    // Gets cell elements
    const rowInElms = DomUtil.getsSelector('input[name*="."],select[name*="."],textarea[name*="."]', outerElm);
    // Finds list elements from page
    const listObj = {};
    for (const elm of rowInElms) {
      const name = elm.getAttribute('name');
      const listId = name.substring(0, name.indexOf('.'));
      if (listObj[listId]) {
        // Skips if already exists in map
        continue;
      }
      const listElm = DomUtil._getParentById(elm, listId);
      if (!DomUtil.isExists(listElm)) {
        throw new Error(`PageUtil#_setRowIndex: List parent element not found. id=#${listId} `);
      }
      listObj[listId] = listElm;
    }

    // Loop for each list
    for (const listId in listObj) {
      const listElm = listObj[listId];
      const rowElms = DomUtil._getAllChildren(listElm);
      // Row loop
      let i = -1;
      for (const rowElm of rowElms) {
        if (rowElm.tagName.toLowerCase() === 'script') {
          continue;
        }
        const colElms = DomUtil.getsSelector(`[name^="${listId}."]`, rowElm);
        if (colElms.length <= 0) {
          continue;
        }
        i++;
        // Cell element loop
        for (const colElm of colElms) {
          // Adds index to cell element
          DomUtil.setAttr(colElm, DomUtil._ORG_ATTR_OBJ_ROW_INDEX, i);
        }
      }
    }
  },

  /**
   * @private
   * Gets element (by <code>name</code> attribute or <code>data-name</code> attribute).<br>
   * <ul>
   *   <li>If the element cannot be retrieved by <code>name</code> attribute, retrieves it by <code>data-name</code> attribute.</li>
   * </ul>
   * @param {string} name 
   * @param {Element} outerElm 
   * @returns Element
   */
  _getElmBynNameOrDataName: function(name, outerElm) {
    let elm = DomUtil.getByName(name, outerElm);
    if (!DomUtil.isExists(elm)) {
      elm = DomUtil.getByDataName(name, outerElm);
    }
    return elm;
  },

  /**
   * @private
   * Sets row part data.
   * 
   * @param {string} listId <code>id</code> attribute of table element (parent element) ([Example] <code>'detail'</code>)
   * @param {Array<Object>} objAry Array of associative arrays (one associative array represents one row of data)
   * @param {Element} outerElm Set range element
   */
  _setRowValues: function(listId, objAry, outerElm) {
    let listElm;
    if (DomUtil.getAttr(outerElm, 'id') === listId) {
      listElm = outerElm;
    } else {
      listElm = DomUtil.getById(listId, outerElm);
    }
    if (!DomUtil.isExists(listElm)) {
      console.warn(`PageUtil#_setRowValues: List element not found. id=${listId}`);
      return;
    }
    // Removes existing rows (except template row)
    PageUtil._removeAllRows(listElm);
    // Adds rows
    PageUtil._addRows(listId, listElm, objAry);
  },
  
  /**
   * @private
   * Gets unformatted element value.<br>
   * <ul>
   * <li>For elements that do not have <code>value</code> attribute such as labels, returns the value of textContent.</li>
   * <li>See JSDoc of <code>PageUtil#getValues</code> for details.</li>
   * </ul>
   */
  _getElmUnFormatVal: function(elm) {
    if (PageUtil._isCheckType(elm)) {
      // For checkbox or radio button
      // For radio button, the checked one is passed
      if (elm.checked) {
        return ValUtil.nvl(elm.value);
      } else {
        // When OFF, gets the value of custom attribute
        return ValUtil.nvl(DomUtil.getAttr(elm, DomUtil._ORG_ATTR_CHECK_OFF_VALUE));
      }
    }
    let val = '';
    if (PageUtil._hasValueProp(elm)) {
      const orgval = ValUtil.nvl(elm.value);
      if (PageUtil._isTextType(elm)) {
        val = PageUtil._convPostVal(orgval);
      } else if (PageUtil._isTextArea(elm)) {
        val = PageUtil._convPostVal(orgval, true);
      } else {
        val = orgval;
      }
      if (val !== orgval) {
        // Returns to page if the value changed due to formatting
        PageUtil._setElmFormatVal(elm, val);
      }
    } else {
      // Processing for labels, etc.
      val = ValUtil.nvl(elm.textContent);
    }
    // Unformat
    const fmtType = DomUtil.getAttr(elm, DomUtil._ORG_ATTR_VALUE_FORMAT_TYPE);
    if (!ValUtil.isNull(fmtType) && !ValUtil.isNull(UnFrmUtil[fmtType])) {
      val = UnFrmUtil[fmtType](val);
    }
    return val;
  },

  /**
   * @private
   * Gets element and sets value.<br>
   * <ul>
   * <li>For radio buttons, selects the appropriate element and sets the value.</li>
   * </ul>
   * @param {string} name Value of <code>name</code> attribute or <code>data-name</code> attribute
   * @param {string} val Value to set
   * @param {Element} outerElm Set range element
   * @returns {boolean} <code>true</code> on successful set, <code>false</code> on failure
   */
  _getElmToSetElmFormatVal: function(name, val, outerElm) {
    let elm = PageUtil._getElmBynNameOrDataName(name, outerElm);
    if (!DomUtil.isExists(elm)) {
      console.warn(`PageUtil#_getElmToSetElmFormatVal: Element not found. name=${name}`);
      return false;
    }
    if (PageUtil._isRadioNotVal(elm, val)) {
      // If a radio button element whose value is not the specified value is passed (the first element with the same name was not the specified value), replaces it with the element of the specified value
      elm = DomUtil._getByNameAndValue(name, val, outerElm);
      if (!DomUtil.isExists(elm)) {
        console.warn(`PageUtil#_getElmToSetElmFormatVal: Element not found. name=${name} value=${val}`);
        return false;
      }
    }
    PageUtil._setElmFormatVal(elm, val);
    return true;
  },

  /**
   * @private
   * Sets element value.
   */
  _setElmFormatVal: function(elm, val) {
    val = ValUtil.nvl(val);
    if (PageUtil._isCheckType(elm)) {
      // For checkbox or radio button
      // For radio button, the element selected by <code>value</code> attribute is passed, so it is always checked ON
      elm.checked = (('' + val) === elm.value);
      return;
    }
    // Format
    const fmtType = DomUtil.getAttr(elm, DomUtil._ORG_ATTR_VALUE_FORMAT_TYPE);
    if (!ValUtil.isNull(fmtType) && !ValUtil.isNull(FrmUtil[fmtType])) {
      val = FrmUtil[fmtType](val);
    }
    if (PageUtil._hasValueProp(elm)) {
      elm.value = val;
    } else {
      elm.textContent = val;
    }
  },

  /**
   * @private
   * Determines if the HTML element has value.
   */
  _hasValueProp: function(elm) {
    const tag = elm.tagName.toLowerCase();
    return (tag === 'input' || tag === 'select' || tag === 'textarea');
  },

  /**
   * @private
   * Determines if the element is a checkbox or radio button.
   */
  _isCheckType: function(elm) {
    const tag = elm.tagName.toLowerCase();
    if (tag === 'input') {
      const type = ValUtil.nvl(elm.getAttribute('type')).toLowerCase();
      return (type === 'checkbox' || type === 'radio');
    }
    return false;
  },

  /**
   * @private
   * Determines if the element is a radio button and is not checked.
   */
  _isRadioOff: function(elm) {
    const tag = elm.tagName.toLowerCase();
    if (tag === 'input') {
      const type = ValUtil.nvl(elm.getAttribute('type')).toLowerCase();
      return (type === 'radio' && !elm.checked);
    }
    return false;
  },

  /**
   * @private
   * Determines if the element is a radio button and the value is not the specified value.
   */
  _isRadioNotVal: function(elm, val) {
    const tag = elm.tagName.toLowerCase();
    if (tag === 'input') {
      const type = ValUtil.nvl(elm.getAttribute('type')).toLowerCase();
      return (type === 'radio' && elm.value !== val);
    }
    return false;
  },

  /**
   * @private
   * Determines if the element is a text input element (including hidden fields).
   */
  _isTextType: function(elm) {
    const tag = elm.tagName.toLowerCase();
    if (tag === 'input') {
      const type = ValUtil.nvl(elm.getAttribute('type')).toLowerCase();
      return (type === 'text' || type === 'hidden');
    }
    return false;
  },

  /**
   * @private
   * Determines if the element is a textarea.
   */
  _isTextArea: function(elm) {
    const tag = elm.tagName.toLowerCase();
    return (tag === 'textarea');
  },

  /**
   * @private
   * Converts characters for web service transmission.<br>
   * <ul>
   * <li>Processing in web service is high load, so adjusts each field on the client side.</li>
   * <li>Removes tab characters and trailing blanks.</li>
   * <li>Either removes newline characters or unifies them to LF.</li>
   * </ul>
   * 
   * @param {string} val Processing value
   * @param {boolean} [isRetIgnore] <code>true</code> to keep newlines (optional)
   * @returns {string} Converted value
   */
  _convPostVal: function(val, isRetIgnore) {
    // Removes tab characters
    const txt = ValUtil.nvl(val).replace(/\t/g, ' ');
    if (isRetIgnore) {
      // Keeps newline characters (unified to LF), removes trailing blanks
      return txt.replace(/\r?\n/g, '\n').replace(/ +$/, '');
    }
    // Removes newline characters (replaced with single-byte blank), removes trailing blanks
    return txt.replace(/\r?\n/g, ' ').replace(/ +$/, '');
  },

  /** 
   * @private
   * Splits HTML into the first HTML tag, its closing tag, and other inner tags.<br>
   * <ul>
   *   <li>Finds the first &gt; and the last &lt; that are not surrounded by double quotes or single quotes and splits them.</li>
   * </ul>
   * @param {string} html HTML string
   * @returns {Array<string>} [First tag, inner tags, closing tag of first tag] (returns an array of length zero if not found)
   */
  _splitHtmlTagsOuterInner: function(html) {
    html = ValUtil.nvl(html).trim();
    if (ValUtil.isBlank(html)) {
      return [];
    }

    let outerBeginEnd = -1;
    let outerEndStart = -1;

    let inDq;
    let inSq;
    let i;

    i = 0
    inDq = false;
    inSq = false;
    while (i < html.length) {
      const char = html[i];
      if (!inSq && char === '"') {
        // Double quote processing (assuming not inside single quotes)
        inDq = !inDq;
        i++;
        continue
      }
      if (!inDq && char === "'") {
        // Single quote processing (assuming not inside double quotes)
        inSq = !inSq;
        i++;
        continue
      }
      if (!inDq && !inSq && char === '>') {
        // Found > outside quotes
        outerBeginEnd = i;
        break;
      }
      i++;
    }
    if (outerBeginEnd < 0) {
      // If the first tag is not found
      return [];
    }

    i = html.length - 1;
    while (i >= 0) {
      const char = html[i];
      // Assumes no quotes inside closing tag
      if (char === '<') {
        // Found <
        outerEndStart = i;
        break;
      }
      i--;
    }
    if (outerEndStart < 0 || outerEndStart <= outerBeginEnd) {
      // If the closing tag of the first tag is not found
      return [];
    }

    const tags = [html.substring(0, outerBeginEnd + 1), html.substring(outerBeginEnd + 1, outerEndStart), html.substring(outerEndStart)];
    return tags;
  },

  /**
   * @private
   * Parses opening tag.<br>
   * <ul>
   *   <li>Extracts tag name and attributes from the opening tag.</li>
   *   <li>Assumes blank-separated elements within HTML tag, attributes are separated by <code>=</code> or no separator such as <code>readonly</code>.</li>
   *   <li><pre>[Example] <code>&lt;tr class="row" style="color:black" hidden&gt;</code>
   *      returns <code>['tr', {class: 'row', style: 'color:black', hidden: 'hidden'}]</code>.</pre></li>
   * </ul>
   * @param {string} htmlTag Opening tag string
   * @returns {Array<string, Object>|null} [Tag name, attribute associative array] (returns <code>null</code> if parsing fails)
   */
  _parseHtmlOpenTag: function(htmlTag) {
    if (ValUtil.isBlank(htmlTag)) {
      return null;
    }
    htmlTag = ValUtil.nvl(htmlTag).trim();
    // Removes < and > and splits by blank
    htmlTag = htmlTag.substring(1, htmlTag.length - 1).trim();
    const tags = htmlTag.split(' ');

    // Tag name is up to the first blank
    const tagName = tags[0].toLowerCase();
    // Attribute values
    const attrs = {};

    // Parses the part other than tag name as attributes
    for (const tag of tags.slice(1)) {
      const att = tag.trim();
      if (ValUtil.isBlank(att)) {
        continue;
      }

      const eqPos = att.indexOf('=');
      if (eqPos < 0) {
        // Valueless attributes such as readonly
        attrs[att] = att;
        continue;
      }

      // Format of attribute name=value
      const attrName = att.substring(0, eqPos);
      let attrVal = att.substring(eqPos + 1).trim();

      // Removes quotes
      if ((attrVal.startsWith('"') && attrVal.endsWith('"')) ||
        (attrVal.startsWith("'") && attrVal.endsWith("'"))) {
        attrVal = attrVal.substring(1, attrVal.length - 1);
      }

      attrs[attrName] = attrVal;
    }

    return [tagName, attrs];
  },
};


/**
 * Session storage utility class.<br>
 * <ul>
 *   <li>Stores and retrieves associative arrays in browser's session storage with the following units + keys.</li>
 *   <ul>
 *     <li>Page unit (HTML file unit of URL, data retention within one page)</li>
 *     <li>Module unit (module directory unit of URL, data sharing between pages)</li>
 *     <li>System unit (data sharing across entire system)</li>
 *   </ul>
 *   <li>Assumes non-critical processing and does not throw exceptions in principle.</li>
 * </ul>
 * @class
 */
const StorageUtil = /** @lends StorageUtil */ {

  /** @private Page unit key prefix */
  _KEY_PREFIX_PAGE: '@page',
  /** @private Module unit key prefix */
  _KEY_PREFIX_MODULE: '@module',
  /** @private System-wide key prefix */
  _KEY_PREFIX_SYSTEM: '@system',

  /** @private Root directory name */
  _ROOT_DIR_NAME: '[root]',

  /**
   * Gets page unit data (HTML file unit of URL). <br>
   * <ul>
   *   <li>Retrieves an associative array from browser's session storage by page unit + key.</li>
   * </ul>
   * @param {string} key Retrieval key
   * @param {Object} [notExistsValue] Return value when not exists (optional)
   * @returns {Object|null} Retrieved data
   */
  getPageObj: function(key, notExistsValue) {
    if (!StorageUtil._argsValidateObjGet('getPageObj', key, notExistsValue)) {
      return null;
    }
    const pageKey = StorageUtil._createPageKey(key);
    const obj = StorageUtil._getObj(pageKey, notExistsValue);
    return obj;
  },

  /**
   * Gets module unit data (module directory unit of URL). <br>
   * <ul>
   *   <li>Retrieves an associative array from browser's session storage by module unit + key.</li>
   * </ul>
   * @param {string} key Retrieval key
   * @param {Object} [notExistsValue] Return value when not exists (optional)
   * @returns {Object|null} Retrieved data
   */
  getModuleObj: function(key, notExistsValue) {
    if (!StorageUtil._argsValidateObjGet('getModuleObj', key, notExistsValue)) {
      return null;
    }
    const mdlKey = StorageUtil._createModuleKey(key);
    const obj = StorageUtil._getObj(mdlKey, notExistsValue);
    return obj;
  },

  /**
   * Gets system unit data.<br>
   * <ul>
   *   <li>Retrieves an associative array from browser's session storage by key.</li>
   * </ul>
   * @param {string} key Retrieval key
   * @param {Object} [notExistsValue] Return value when not exists (optional)
   * @returns {Object|null} Retrieved data
   */
  getSystemObj: function(key, notExistsValue) {
    if (!StorageUtil._argsValidateObjGet('getSystemObj', key, notExistsValue)) {
      return null;
    }
    const sysKey = StorageUtil._createSystemKey(key);
    const obj = StorageUtil._getObj(sysKey, notExistsValue);
    return obj;
  },

  /**
   * Stores page unit data (HTML file unit of URL).<br>
   * <ul>
   *   <li>Stores an associative array in browser's session storage by page unit + key.</li>
   * </ul>
   * @param {string} key Storage key
   * @param {Object} obj Storage data
   * @returns {boolean} <code>true</code> on successful storage
   */
  setPageObj: function(key, obj) {
    if (!StorageUtil._argsValidateObjSet('setPageObj', key, obj)) {
      return false;
    }
    const pageKey = StorageUtil._createPageKey(key);
    return StorageUtil._setObj(pageKey, obj);
  },

  /**
   * Stores module unit data (module directory unit of URL).<br>
   * <ul>
   *   <li>Stores an associative array in browser's session storage by module unit + key.</li>
   * </ul>
   * @param {string} key Storage key
   * @param {Object} obj Storage data
   * @returns {boolean} <code>true</code> on successful storage
   */
  setModuleObj: function(key, obj) {
    if (!StorageUtil._argsValidateObjSet('setModuleObj', key, obj)) {
      return false;
    }
    const mdlKey = StorageUtil._createModuleKey(key);
    return StorageUtil._setObj(mdlKey, obj);
  },

  /**
   * Stores system unit data.<br>
   * <ul>
   *   <li>Stores an associative array in browser's session storage by key.</li>
   * </ul>
   * @param {string} key Storage key
   * @param {Object} obj Storage data
   * @returns {boolean} <code>true</code> on successful storage
   */
  setSystemObj: function(key, obj) {
    if (!StorageUtil._argsValidateObjSet('setSystemObj', key, obj)) {
      return false;
    }
    const sysKey = StorageUtil._createSystemKey(key);
    return StorageUtil._setObj(sysKey, obj);
  },

  /**
   * Removes page unit data.
   * @param {string} key Key
   * @returns {boolean} <code>true</code> on successful removal
   */
  removePage: function(key) {
    if (ValUtil.isBlank(key)) {
      console.error('StorageUtil#removePage: key is required.');
      return false;
    }
    const pageKey = StorageUtil._createPageKey(key);
    return StorageUtil._remove(pageKey);
  },

  /**
   * Removes module unit data.
   * @param {string} key Key
   * @returns {boolean} <code>true</code> on successful removal
   */
  removeModule: function(key) {
    if (ValUtil.isBlank(key)) {
      console.error('StorageUtil#removeModule: key is required.');
      return false;
    }
    const mdlKey = StorageUtil._createModuleKey(key);
    return StorageUtil._remove(mdlKey);
  },

  /**
   * Removes system unit data.
   * @param {string} key Removal key
   * @returns {boolean} <code>true</code> on successful removal
   */
  removeSystem: function(key) {
    if (ValUtil.isBlank(key)) {
      console.error('StorageUtil#removeSystem: key is required.');
      return false;
    }
    const sysKey = StorageUtil._KEY_PREFIX_SYSTEM + key;
    return StorageUtil._remove(sysKey);
  },

  /**
   * Clears all data.<br>
   * <ul>
   *   <li>Removes all data stored by this utility.</li>
   *   <li>Use for emergency or debugging purposes.</li>
   * </ul>
   * @returns {boolean} <code>true</code> on successful clear
   */
  clearAllData: function() {
    const systemResult = StorageUtil.clearSystem();
    const moduleResult = StorageUtil.clearModule();
    const pageResult = StorageUtil.clearPage();
    return systemResult && moduleResult && pageResult;
  },

  /**
   * @private
   * Validates arguments for retrieval.
   * @param {string} methodName Retrieval method name
   * @param {string} key Retrieval key
   * @param {Object} [notExistsValue] Return value when not exists (optional)
   * @returns {boolean} <code>false</code> on error
   */
  _argsValidateObjGet: function(methodName, key, notExistsValue) {
    if (ValUtil.isBlank(key)) {
      console.error(`StorageUtil#${methodName}: key is required.`);
      return false;
    }
    if (!ValUtil.isEmpty(notExistsValue) && !ValUtil.isObj(notExistsValue)) {
      console.error(`StorageUtil#${methodName}: notExistsValue must be an object.`);
      return false;
    }
    return true;
  },

  /**
   * @private
   * Validates arguments for storage.
   * @param {string} methodName Retrieval method name
   * @param {string} key Retrieval key
   * @param {Object} obj Storage data
   * @returns {boolean} <code>false</code> on error
   */
  _argsValidateObjSet: function(methodName, key, obj) {
    if (ValUtil.isBlank(key)) {
      console.error(`StorageUtil#${methodName}: key is required.`);
      return false;
    }
    if (!ValUtil.isObj(obj)) {
      console.error(`StorageUtil#${methodName}: store data must be an object.`);
      return false;
    }
    return true;
  },

  /**
   * @private
   * Retrieves data.
   * @param {string} key Key
   * @param {Object} [notExistsValue] Return value when not exists (optional)
   * @returns {Object|null} Retrieved data
   */
  _getObj: function(key, notExistsValue) {
    try {
      const json = sessionStorage.getItem(key);
      if (ValUtil.isNull(json)) {
        if (!ValUtil.isNull(notExistsValue)) {
          return notExistsValue;
        } else {
          return null;
        }
      }
      return JSON.parse(json);
    } catch (e) {
      console.error(`StorageUtil#_getObj: Failed to parse JSON data. key=${key}`, e);
      // Removes corrupted data
      try {
        sessionStorage.removeItem(key);
      } catch (removeError) {
        console.error(`StorageUtil#_getObj: Failed to remove corrupted data. key=${key}`, removeError);
      }
      return null;
    }
  },

  /**
   * @private
   * Stores data.
   * @param {string} key Key
   * @param {Object} obj Storage data
   * @returns {boolean} <code>true</code> on successful storage
   */
  _setObj: function(key, obj) {
    try {
      const json = JSON.stringify(obj);
      sessionStorage.setItem(key, json);
      return true;
    } catch (e) {
      if (e.name === 'QuotaExceededError') {
        console.error(`StorageUtil#_setData: Storage quota exceeded. key=${key}`, e);
      } else {
        console.error(`StorageUtil#_setData: Failed to save data. key=${key}`, e);
      }
      return false;
    }
  },

  /**
   * @private
   * Removes data.
   * @param {string} key Key
   * @returns {boolean} <code>true</code> on successful removal
   */
  _remove: function(key) {
    try {
      sessionStorage.removeItem(key);
      return true;
    } catch (e) {
      console.warn(`StorageUtil#_remove: Failed to remove data. key=${key}`, e);
      return false;
    }
  },

  /**
   * @private
   * Generates page unit key.
   * @param {string} key Key
   * @returns {string} Page unit key
   */
  _createPageKey: function(key) {
    return StorageUtil._createPageKeyPrefixByLocation() + key;
  },

  /**
   * @private
   * Generates page unit key prefix for current page.
   * @returns {string} Page unit key prefix
   */
  _createPageKeyPrefixByLocation: function() {
    // Gets page name from location
    const paths = location.pathname.split('/');
    // Gets page name
    let pageName = paths.pop();
    if (ValUtil.isBlank(pageName)) {
      pageName = 'index';
    } else {
      // Removes extension
      const dotPos = pageName.lastIndexOf('.');
      if (dotPos > 0) {
        pageName = pageName.substring(0, dotPos);
      }
    }
    // Gets directory name
    let mdlName = paths.pop();
    if (ValUtil.isBlank(mdlName)) {
      // For root directory
      mdlName = StorageUtil._ROOT_DIR_NAME;
    }
    return `${StorageUtil._KEY_PREFIX_PAGE}/${mdlName}/${pageName}/`;
  },

  /**
   * @private
   * Generates module unit key.
   * @param {string} key Key
   * @returns {string} Module unit key
   */
  _createModuleKey: function(key) {
    return StorageUtil._createModuleKeyPrefixByLocation() + key;
  },

  /**
   * @private
   * Generates module unit key prefix for current module.
   * @returns {string} Module unit key prefix
   */
  _createModuleKeyPrefixByLocation: function() {
    // Gets module name from location
    const paths = location.pathname.split('/');
    // Removes file name
    paths.pop();
    // Gets directory name
    let mdlName = paths.pop();
    if (ValUtil.isBlank(mdlName)) {
      // For root directory
      mdlName = StorageUtil._ROOT_DIR_NAME;
    }
    return `${StorageUtil._KEY_PREFIX_MODULE}/${mdlName}/`;
  },

  /**
   * @private
   * Generates system unit key.
   * @param {string} key Key
   * @returns {string} System unit key
   */
  _createSystemKey: function(key) {
    return `${StorageUtil._KEY_PREFIX_SYSTEM}/${key}`;
  },

  /**
   * Clears all page unit data.<br>
   * <ul>
   *   <li>Removes all data stored in the current page.</li>
   * </ul>
   * @returns {boolean} <code>true</code> on successful clear
   */
  clearPage: function() {
    const prefix = StorageUtil._createPageKeyPrefixByLocation();
    return StorageUtil._clear(prefix);
  },

  /**
   * Clears all module unit data.<br>
   * <ul>
   *   <li>Removes all data stored in the current module.</li>
   * </ul>
   * @returns {boolean} <code>true</code> on successful clear
   */
  clearModule: function() {
    const prefix = StorageUtil._createModuleKeyPrefixByLocation();
    return StorageUtil._clear(prefix);
  },

  /**
   * Clears system unit data.<br>
   * <ul>
   *   <li>Removes all data stored as system-wide shared data.</li>
   * </ul>
   * @returns {boolean} <code>true</code> on successful clear
   */
  clearSystem: function() {
    const prefix = StorageUtil._KEY_PREFIX_SYSTEM;
    return StorageUtil._clear(prefix);
  },

  /**
   * @private
   * Clears all data with specified prefix.
   * @param {string} prefix Prefix
   * @returns {boolean} <code>true</code> on successful clear
   */
  _clear: function(prefix) {
    let count = 0;
    try {
      // Checks all keys in sessionStorage
      for (let i = (sessionStorage.length - 1); i >= 0; i--) {
        const key = sessionStorage.key(i);
        if (!ValUtil.isNull(key) && key.startsWith(prefix)) {
          sessionStorage.removeItem(key);
          count++;
        }
      }
      console.info(`StorageUtil#_clear: Cleared ${count} items.`);
      return true;
    } catch (e) {
      console.error('StorageUtil#_clear: Failed to clear data.', e);
      return false;
    }
  },

  /**
   * @private
   * For debugging: Displays all stored data.<br>
   * <ul>
   *   <li>Displays all currently stored data to the console.</li>
   *   <li>Use only for development and debugging purposes.</li>
   * </ul>
   */
  _debugAllData: function() {
    console.group('StorageUtil#_debugAllData: All Data');
    
    try {
      const sysObj = {};
      const mdlObj = {};
      const pageObj = {};
      const otherObj = {};
      
      for (let i = 0; i < sessionStorage.length; i++) {
        const key = sessionStorage.key(i);
        if (ValUtil.isNull(key)) {
          continue;
        }
        const value = sessionStorage.getItem(key);
        if (key.startsWith(StorageUtil._KEY_PREFIX_SYSTEM)) {
          const orgKey = key.substring(StorageUtil._KEY_PREFIX_SYSTEM.length);
          sysObj[orgKey] = value;
        } else if (key.startsWith(StorageUtil._KEY_PREFIX_MODULE)) {
          const orgKey = key.substring(StorageUtil._KEY_PREFIX_MODULE.length);
          mdlObj[orgKey] = value;
        } else if (key.startsWith(StorageUtil._KEY_PREFIX_PAGE)) {
          const orgKey = key.substring(StorageUtil._KEY_PREFIX_PAGE.length);
          pageObj[orgKey] = value;
        } else {
          otherObj[key] = value;
        }
      }
      console.log(StorageUtil._KEY_PREFIX_SYSTEM + ':', sysObj);
      console.log(StorageUtil._KEY_PREFIX_MODULE + ':', mdlObj);
      console.log(StorageUtil._KEY_PREFIX_PAGE + ':', pageObj);
      console.log('Other:', otherObj);
    } catch (e) {
      console.error('StorageUtil#_debugAllData: Failed to show data.', e);
    }
    
    console.groupEnd();
  }
};

