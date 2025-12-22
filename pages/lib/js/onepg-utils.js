/**
 * Value operation utility class.
 * @class
 */
const ValUtil = /** @lends ValUtil */ {
  /**
   * <code>null</code> check.<br>
   * <ul>
   * <li>Checks if the Object is <code>null</code>.</li>
   * </ul>
   *
   * @param {Object} obj Target to check
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
   * <li>Checks if the string is spaces only, zero length, or <code>null</code>.</li>
   * </ul>
   *
   * @param {string} str Target to check
   * @returns {boolean} <code>true</code> if blank
   */
  isBlank : function(str) {
    if (ValUtil.isNull(str)) {
      return true;
    }
    return String(str).trim().length === 0;
  },

  /**
   * <code>null</code> blank replacement.<br>
   * <ul>
   * <li>Returns blank or replacement string if the string is <code>null</code>.</li>
   * </ul>
   *
   * @param {string} value Target to check
   * @param {string} [rep] Replacement string (optional). Returns blank if omitted.
   * @returns {string} Blank or replacement string if <code>null</code>
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
   * <li>Returns replacement string if the string is blank.</li>
   * </ul>
   *
   * @param {string} value Target to check
   * @param {string} rep Replacement string
   * @returns {string} Replacement string if blank
   */
  bvl : function(value, rep) {
    if (ValUtil.isBlank(value)) {
      return rep;
    }
    return value;
  },

  /**
   * Safe string extraction.
   *
   * @param {string} value Target string
   * @param {number} [beginIndex] Start index (optional). Default is 0.
   * @param {number} [endIndex] End index (optional). Default is string length.
   * @returns {string} Extracted string
   */
  substring: function(value, beginIndex, endIndex) {
    if (ValUtil.isNull(value)) {
      return '';
    }
    // Complement default values
    beginIndex = beginIndex || 0;
    endIndex = endIndex || value.length;
    // Out of range correction
    if (endIndex > value.length) {
      endIndex = value.length;
    }
    // Return empty string if start position is at or after end position, or if start position is at or after string length
    if (beginIndex < 0 || beginIndex >= endIndex || beginIndex >= value.length) {
      return '';
    }
    return value.substring(beginIndex, endIndex);
  },

  /**
   * String comparison.<br>
   * <ul>
   * <li>Compares <code>null</code> as empty string.</li>
   * </ul>
   *
   * @param {string} val1 Comparison target 1
   * @param {string} val2 Comparison target 2
   * @returns {boolean} <code>true</code> if equal
   */
  equals : function(val1, val2) {
    return ValUtil.nvl(val1) === ValUtil.nvl(val2) ;
  },

  /**
   * Associative array comparison.<br>
   * <ul>
   *   <li>Compares values of keys that exist in both targets. Keys that exist in only one are excluded from comparison.</li>
   *   <li>Determines "not equal" if either target is not an associative array.</li>
   * </ul>
   *
   * @param {Object} obj1 Comparison target 1
   * @param {Object} obj2 Comparison target 2
   * @param {string} ignoreKeys Keys to exclude from comparison (multiple allowed)
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
      // Ignore if excluded from comparison
      if (ignoreKeyAry.indexOf(key) >= 0) {
        continue;
      }
      // Ignore if key does not exist in comparison target 2
      // Explicitly writing comparison value (<code>false</code>) to avoid confusion with loop
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
        // For arrays, assumes each element is an associative array.
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
   *   <li>Returns same result as isBlank() if a string is passed.</li>
   *   <li>Checks if length is zero when an array is passed.</li>
   *   <li>Checks if length is zero when HTML element collection result is passed.</li>
   *   <li>Checks if key array length is zero when an associative array is passed.</li>
   * </ul>
   *
   * @param {Object} obj Target to check
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
    // 'number' 'boolean' are not empty if identified
    return false;
  },
  
  /** @private Unsigned integer check regex */
  _IS_NUM_UNSIGNED_INT: /^([1-9]\d*|0)$/,
  /** @private Unsigned decimal check regex */
  _IS_NUM_UNSIGNED_FLOAT: /^([1-9]\d*|0)(\.\d+)?$/,
  /** @private Integer check regex */
  _IS_NUM_INT: /^[-]?([1-9]\d*|0)$/,
  /** @private Decimal check regex */
  _IS_NUM_FLOAT: /^[-]?([1-9]\d*|0)(\.\d+)?$/,
  /**
   * Numeric check.<br>
   * <ul>
   * <li>Checks if the string is valid as a number.</li>
   * </ul>
   *
   * @param {string} value Target to check
   * @param {boolean} [minusNg] <code>true</code> to disallow negative values (optional)
   * @param {boolean} [decNg] <code>true</code> to disallow decimals (optional)
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
   * <li>Checks if the string is valid as a date.</li>
   * </ul>
   *
   * @param {string} yyyymmdd Target to check (YYYYMMDD)
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
   * <li>Checks if the string is a value considered as boolean "true".</li>
   * <li>Performs the following evaluation:
   *   <ol>
   *     <li>"1", "true", "yes", "on" (all half-width) are <code>true</code>.</li>
   *     <li><code>null</code> or blank is <code>false</code>; anything else not listed above is <code>false</code>.</li>
   *     <li>Case insensitive.</li>
   *     <li>Leading and trailing half-width blanks are ignored.</li>
   *     <li>Boolean values are returned as is.</li>
   *   </ol>
   * </li>
   * </ul>
   *
   * @param {string|boolean} val Target to check
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
   * @param {string} yyyymmdd Target to convert (YYYYMMDD)
   * @returns {Date} Date object
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
   * @param {Date} dateObj Target to convert
   * @returns {string} Date string (YYYYMMDD)
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
   * @param {Object} obj Target to check
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
   * Associative array check.
   *
   * @param {Object} obj Target to check
   * @returns {boolean} <code>true</code> if associative array
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
   * @param {string} value Target
   * @param {string} pad Padding character
   * @param {number} len Length after padding
   * @returns {string} String after left padding
   */
  lpad : function(value, pad, len) {
    const pads = pad.repeat(len);
    return (pads + value).slice(len * -1);
  },

  /**
   * Right padding.
   *
   * @param {string} value Target
   * @param {string} pad Padding character
   * @param {number} len Length after padding
   * @returns {string} String after right padding
   */
  rpad : function(value, pad, len) {
    const pads = pad.repeat(len);
    return (value + pads).substring(0, len);
  },


  /**
   * Object type retrieval.<br>
   * <ul>
   * <li>Use when detailed type checking is needed since typeof returns 'object' for both <code>null</code> and arrays.</li>
   * </ul>
   *
   * @param {Object} obj Object
   * @returns {string} Type string: 'undefined', 'null', 'boolean', 'number', 'string', 'array', 'object', etc.
   */
  toType : function(obj) {
    return Object.prototype.toString.call(obj).slice(8, -1).toLowerCase();
  },

  /**
   * @private
   * Date object format conversion.
   *
   * @param {Date} dateObj Date object
   * @param {string} formatter Format string
   * @returns {string} Formatted string
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
 * Value formatting class.<br>
 * <ul>
 *   <li>Handles value formatting when displaying on pages.</li>
 *   <li>Mainly called from PageUtil using bracket notation; rarely called directly from feature-specific processing.</li>
 *   <li>Requires corresponding unformat processing.</li>
 * </ul>
 * @class
 */
const FrmUtil = /** @lends FrmUtil */ {

  /**
   * Uppercase conversion.
   * @param {string} value Target
   * @returns {string} String after uppercase conversion
   */
  upper: function(value) {
    if (ValUtil.isBlank(value)) {
      return value;
    }
    return value.toUpperCase();
  },

  /**
   * Number (comma formatting).<br>
   * Returns unformatted if not a number.
   * @param {string} value Target
   * @returns {string} String after comma formatting
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
   * Returns unformatted if not a valid date.
   * @param {string} value Target
   * @returns {string} Date string in YYYY/MM/DD format
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
   * Return without formatting if not 6-digit number.
   * @param {string} value Processing target
   * @returns {string} Time string in HH:MI:SS format
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
 * Value unformatting class.<br>
 * <ul>
 *   <li>Handles value unformatting when creating requests.</li>
 *   <li>Mainly called from PageUtil using bracket notation; rarely called directly from feature-specific processing.</li>
 *   <li>Requires corresponding format processing.</li>
 * </ul>
 * @class
 */
const UnFrmUtil = /** @lends UnFrmUtil */ {

  /**
   * Uppercase conversion unformat.
   * @param {string} value Target
   * @returns {string} Unprocessed string
   */
  upper: function(value) {
    // No processing
    return value;
  },

  /**
   * Number unformat (comma removal).
   * @param {string} value Target
   * @returns {string} String after comma removal
   */
  num: function(value) {
    if (ValUtil.isBlank(value)) {
      return value;
    }
    const unVal = ('' + value).trim().replace(/,/g, '');
    return unVal;
  },

  /**
   * Date unformat (slash removal).
   * @param {string} value Target
   * @returns {string} String after slash removal
   */
  ymd: function(value) {
    if (ValUtil.isBlank(value)) {
      return value;
    }
    const unVal = value.trim().replace(/\//g, '');
    return unVal;
  },

  /**
   * Time unformat (colon removal).
   * @param {string} value Target
   * @returns {string} String after colon removal
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
 * HTTP operation utility class.
 *
 * @class
 */
const HttpUtil = /** @lends HttpUtil */ {
  /**
   * Convert associative array to URL parameters.<br>
   * <ul>
   * <li><pre>[Example]
   *      For <code>params = {p1: 'aaa', p2: 'bbb'}</code>,
   *      convert to <code>p1=aaa&p2=bbb</code>.</pre></li>
   * </ul>
   *
   * @param {Object.<string,string>} obj Associative array
   * @returns {string} URL parameter string
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
   * URL parameter retrieval.<br>
   * <ul>
   *   <li>Retrieves the portion after ? in the URL as an associative array.</li>
   *   <li>[Example] For "a=01&b=02", returns <code>{a:'01', b:'02'}</code>.</li>
   *   <li>Parameters are removed after retrieval.</li>
   * </ul>
   * @returns {Object} URL parameter associative array
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
   * <li>Navigates to the specified URL (retrieves HTML file).</li>
   * <li>Appends parameters to the URL after ? if specified.</li>
   * <li><pre>[Example]
 *      When <code>url = 'editpage.html', params = {user_id: 'U001', upd_ts: '20251231T245959001000'}</code>,
 *      accesses <code>editpage.html?user_id=U001&upd_ts=20251231T245959001000</code>.</pre></li>
   * </ul>
   *
   * @param {string} url Navigation destination URL
   * @param {Object.<string, string>|string} [params] Parameters (string also allowed) (optional)
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
    // Use replace to prevent going back (go back to the first page opened)
    location.replace(loc);
  },  /**
   * JSON Web service execution (async/await compatible).<br>
   * <ul>
   * <li>Sends a JSON request via POST method to the specified URL and receives a JSON response.</li>
   * <li>Request/response is handled as associative arrays.</li>
   * </ul>
   * 
   * @param {string} url Destination URL
   * @param {Object} [req] Request associative array (optional)
   * @param {Object.<string, string>} [addHeader] Additional HTTP headers (optional)
   * @returns {Object} Response associative array
   */
  callJsonService : async function(url, req, addHeader) {
    req = req || {};
    if (!ValUtil.isObj(req)) {
      // Request data must be an associative array
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
      // Disable automatic JSON parsing for verification
      xhr.responseType = 'text';

      // Communication complete event
      xhr.onload = function() {
        if (200 <= xhr.status && xhr.status < 300) {
          let res = null;
          try {
            // Manual JSON parsing
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
 * HTML element operation utility class.
 *
 * @class
 */
const DomUtil = /** @lends DomUtil */ {

  /** @private Alternative attribute name for <code>name</code> attribute for non-form input elements */
  _ORG_ATTR_NAME: 'data-name',

  /** @private Row index attribute when converted to associative array */
  _ORG_ATTR_OBJ_ROW_INDEX: 'data-obj-row-idx',
  /** @private Value when checkbox is OFF */
  _ORG_ATTR_CHECK_OFF_VALUE: 'data-check-off-value',
  /** @private Value format type */
  _ORG_ATTR_VALUE_FORMAT_TYPE: 'data-value-format-type',

  /** @private Backup for <code>display</code> style */
  _ORG_ATTR_STYLE_DISPLAY_BACKUP: 'data-style-display-backup',
  /** @private Backup for <code>visibility</code> style */
  _ORG_ATTR_STYLE_VISIBILITY_BACKUP: 'data-style-visibility-backup',

  /**
   * Element existence check.
   * @param {Element|NodeList} elms Target element(s) to check
   * @returns {boolean} <code>true</code> if HTML element(s) retrieved
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
   * @param {Object} elm Target to check
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
   * @param {Object} elm Target to check
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
   * @param {Object} elm Target to check
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
   * Check if element is visible.<br>
   * <ul>
   * <li>Determines hidden if <code>display:none</code> or <code>visibility:hidden</code>.</li>
   * <li>Also checks visibility of parent elements.</li>
   * </ul>
   * @param {Element} elm Target element to check
   * @returns {boolean} <code>true</code> if visible
   */
  isVisible: function(elm) {
    if (!DomUtil.isExists(elm)) {
      return false;
    }

    // Check the element and its ancestor elements
    let curElm = elm;
    while (DomUtil.isExists(curElm) && curElm !== document.body) {
      // Hidden items are not considered invisible
      if (curElm.tagName.toLowerCase() === 'input' && ValUtil.nvl(curElm.getAttribute('type')).toLowerCase() === 'hidden') {
        // Move to parent element
        curElm = curElm.parentElement;
        continue;
      }

      // Get merged style using getComputedStyle
      const style = window.getComputedStyle(curElm);
      // Invisible if display: none or visibility: hidden
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
   * Get first element from NodeList or array.<br>
   * <ul>
   * <li>Returns the first element from <code>NodeList</code> or array.</li>
   * <li>Returns as-is if argument is not <code>NodeList</code> or array.</li>
   * <li>Returns <code>null</code> if argument is invalid.</li>
   * </ul>
   * @param {NodeList|Element} elm Target HTML element
   * @returns {Element|null} First element
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
   * Convert NodeList to array.<br>
   * <ul>
   * <li>Enables use of array methods like <code>forEach</code>.</li>
   * </ul>
   * @param {NodeList} list <code>NodeList</code>
   * @returns {Array<Element>} HTML element array
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
   * ID selector (get first element).<br>
   * <ul>
   * <li>Returns <code>null</code> if argument is invalid or element not found.</li>
   * </ul>
   * @param {string} id <code>id</code> attribute
   * @param {Object} [outerElm] Search scope element (optional)
   * @returns {Element|null} Retrieved HTML element
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
   * Selector (get first element).<br>
   * <ul>
   * <li>Returns <code>null</code> if argument is invalid or element not found.</li>
   * </ul>
   * @param {string} selector Selector string
   * @param {Object} [outerElm] Search scope element (optional)
   * @returns {Element|null} Retrieved HTML element
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
   * Name selector (get first element).<br>
   * <ul>
   * <li>Returns <code>null</code> if argument is invalid or element not found.</li>
   * </ul>
   * @param {string} name <code>name</code> attribute
   * @param {Object} [outerElm] Search scope element (optional)
   * @returns {Element|null} Retrieved HTML element
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
   * Name and value selector (get first element).<br>
   * <ul>
   * <li>For radio buttons.</li>
   * <li>Returns <code>null</code> if argument is invalid or element not found.</li>
   * </ul>
   * @param {string} name <code>name</code> attribute
   * @param {string} value <code>value</code> attribute
   * @param {Object} [outerElm] Search scope element (optional)
   * @returns {Element|null} Retrieved HTML element
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
   * Data-name selector (get first element).<br>
   * <ul>
   * <li>Gets elements other than form input elements using an alternative attribute name for the <code>name</code> attribute.</li>
   * <li>Returns <code>null</code> if argument is invalid or element not found.</li>
   * </ul>
   * @param {string} name <code>data-name</code> attribute
   * @param {Object} [outerElm] Search scope element (optional)
   * @returns {Element|null} Retrieved HTML element
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
   * Tag selector (get first element).<br>
   * <ul>
   * <li>Returns <code>null</code> if argument is invalid or element not found.</li>
   * </ul>
   * @param {string} tag HTML tag name
   * @param {Object} [outerElm] Search scope element (optional)
   * @returns {Element|null} Retrieved HTML element
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
   * Selector (get multiple elements).<br>
   * <ul>
   * <li>Returns an array of HTML elements.</li>
   * <li>Returns zero-length array if not found.</li>
   * <li>Returns <code>null</code> if argument is invalid.</li>
   * </ul>
   * @param {string} selector Selector string
   * @param {Object} [outerElm] Search scope element (optional)
   * @returns {Array<Element>|null} Multiple HTML element array 
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
      // querySelectorAll() returns zero-length NodeList if not found
      return DomUtil._listToAry(retElms);
    }
    const retElms = document.querySelectorAll(selector);
    // querySelectorAll() returns zero-length NodeList if not found
    return DomUtil._listToAry(retElms);
  },

  /**
   * @private
   * Class selector (get multiple elements).<br>
   * <ul>
   * <li>Returns an array of HTML elements.</li>
   * <li>Returns zero-length array if not found.</li>
   * <li>Returns <code>null</code> if argument is invalid.</li>
   * </ul>
   * @param {string} cls <code>class</code> attribute
   * @param {Object} [outerElm] Search scope element (optional)
   * @returns {Array<Element>|null} Multiple HTML element array 
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
   * Ancestor element ID selector (get first element).<br>
   * <ul>
   * <li>Searches ancestor elements from the specified element and returns the nearest element matching the id.</li>
   * <li>Returns <code>null</code> if argument is invalid or element not found.</li>
   * </ul>
   * @param {Element} baseElm Specified element
   * @param {string} id <code>id</code> attribute
   * @returns {Element|null} Retrieved HTML element
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
   * Ancestor element tag selector (get first element).<br>
   * <ul>
   * <li>Searches ancestor elements from the base element and returns the nearest element matching the HTML tag.</li>
   * <li>Used to retrieve the row element containing the clicked button, etc.</li>
   * <li>Returns <code>null</code> if argument is invalid or element not found.</li>
   * </ul>
   * @param {Element} baseElm Base element
   * @param {string} tag HTML tag name
   * @returns {Element|null} Retrieved HTML element
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
   * Get all direct child elements.<br>
   * <ul>
   * <li>Retrieves all child elements directly under the specified element (excludes text nodes).</li>
   * <li>Returns an array of HTML elements.</li>
   * <li>Returns zero-length array if no child elements.</li>
   * <li>Returns <code>null</code> if argument is invalid.</li>
   * </ul>
   * @param {Element} parentElm Parent element
   * @returns {Array<Element>|null} Child element array
   */
  _getAllChildren: function(parentElm) {
    if (!DomUtil.isExists(parentElm)) {
      return null;
    }
    // Use children property (text nodes not included)
    return DomUtil._listToAry(parentElm.children);
  },

  /**
   * Get element value.<br>
   * <ul>
   * <li>Retrieves <code>value</code> attribute of <code>&lt;input&gt;</code>, <code>&lt;select&gt;</code>, <code>&lt;textarea&gt;</code>.</li>
   * <li>If the target element has the <code>data-value-format-type</code> attribute that defines the value format type, retrieves the unformatted value using the corresponding method of <code>UnFrmUtil</code>.</li>
   * <li>If the target element is a text box or text area, retrieves the value with tab characters, trailing blanks, and line break codes removed.</li>
   * <li>If the target element is a checkbox, returns the value of the <code>value</code> attribute when checked, and retrieves the value of the <code>data-check-off-value</code> attribute when unchecked.</li>
   * <li>Returns <code>null</code> if argument is invalid.</li>
   * </ul>
   * @param {Element} elm Target element
   * @returns {string|null} Value
   */
  getVal: function(elm) {
    if (!DomUtil.isExists(elm)) {
      return null;
    }
    const val = PageUtil._getElmUnFormatVal(elm);
    return val;
  },

  /**
   * Set element value.<br>
   * <ul>
   * <li>Sets <code>value</code> attribute of <code>&lt;input&gt;</code>, <code>&lt;select&gt;</code>, <code>&lt;textarea&gt;</code>.</li>
   * <li>If the target element has the <code>data-value-format-type</code> attribute that defines the value format type, sets the formatted value using the corresponding method of <code>FrmUtil</code>.</li>
   * <li>Returns <code>false</code> if argument is invalid.</li>
   * </ul>
   * @param {Element} elm Target element
   * @param {string} value Value to set
   * @returns {boolean} <code>true</code> on success
   */
  setVal: function(elm, value) {
    if (!DomUtil.isExists(elm)) {
      return false;
    }
    PageUtil._setElmFormatVal(elm, value);
    return true;
  },

  /**
   * Get element text.<br>
   * <ul>
   * <li>Retrieves the element's <code>textContent</code>.</li>
   * <li>If the target element has the <code>data-value-format-type</code> attribute that defines the value format type, retrieves the unformatted value using the corresponding method of <code>UnFrmUtil</code>.</li>
   * <li>Returns <code>null</code> if argument is invalid.</li>
   * </ul>
   * @param {Element} elm Target element
   * @returns {string|null} Text
   */
  getTxt: function(elm) {
    if (!DomUtil.isExists(elm)) {
      return null;
    }
    const val = PageUtil._getElmUnFormatVal(elm);
    return val;
  },

  /**
   * Set element text.<br>
   * <ul>
   * <li>Sets the element's <code>textContent</code>.</li>
   * <li>If the target element has the <code>data-value-format-type</code> attribute that defines the value format type, sets the formatted value using the corresponding method of <code>FrmUtil</code>.</li>
   * <li>Returns <code>false</code> if argument is invalid.</li>
   * </ul>
   * @param {Element} elm Target element
   * @param {string} text Text to set
   * @returns {boolean} <code>true</code> on success
   */
  setTxt: function(elm, text) {
    if (!DomUtil.isExists(elm)) {
      return false;
    }
    PageUtil._setElmFormatVal(elm, text);
    return true;
  },

  /**
   * Toggle element enabled state.<br>
   * <ul>
   * <li>Toggles the element's <code>disabled</code> attribute.</li>
   * <li>Returns <code>false</code> if argument is invalid.</li>
   * </ul>
   * @param {Element} elm Target element
   * @param {boolean|string} isEnable <code>true</code> to enable
   * @returns {boolean} <code>true</code> on success
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
   * Toggle element visibility.<br>
   * <ul>
   * <li>Toggles the element's <code>display</code> style or <code>visibility</code> style.</li>
   * <li>Use <code>visibility</code> style to preserve element space (maintain layout).</li>
   * <li>Returns <code>false</code> if argument is invalid.</li>
   * </ul>
   * @param {Element} elm Target element
   * @param {boolean|string} isShow <code>true</code> to show
   * @param {boolean} keepLayout <code>true</code> to preserve element space
   * @returns {boolean} <code>true</code> on success
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
   * Set element visibility style.
   * @param {Element} elm Target element
   * @param {boolean} isShow <code>true</code> to show
   * @returns {boolean} <code>true</code> on success
   */
  _setVisibilityStyle: function(elm, isShow) {
    // Toggle with visibility style
    if (ValUtil.isTrue(isShow)) {
      if (elm.style.visibility !== 'hidden') {
        return false;
      }
      if (DomUtil.hasAttr(elm, DomUtil._ORG_ATTR_STYLE_VISIBILITY_BACKUP)) {
        // Restore backed up visibility style
        elm.style.visibility = DomUtil.getAttr(elm, DomUtil._ORG_ATTR_STYLE_VISIBILITY_BACKUP);
      } else {
        elm.style.visibility = '';
      }
    } else {
      if (elm.style.visibility === 'hidden') {
        return false;
      }
      if (!ValUtil.isBlank(elm.style.visibility)) {
        // Backup visibility style
        DomUtil.setAttr(elm, DomUtil._ORG_ATTR_STYLE_VISIBILITY_BACKUP, elm.style.visibility);
      }
      elm.style.visibility = 'hidden';
    }
    return true;
  },

  /**
   * @private
   * Set element display style.
   * @param {Element} elm Target element
   * @param {boolean} isShow <code>true</code> to show
   * @returns {boolean} <code>true</code> on success
   */  
  _setDisplayStyle: function(elm, isShow) {
    // Toggle with display style
    if (ValUtil.isTrue(isShow)) {
      if (elm.style.display !== 'none') {
        return false;
      }
      if (DomUtil.hasAttr(elm, DomUtil._ORG_ATTR_STYLE_DISPLAY_BACKUP)) {
        // Restore backed up display style
        elm.style.display = DomUtil.getAttr(elm, DomUtil._ORG_ATTR_STYLE_DISPLAY_BACKUP);
      } else {
        elm.style.display = '';
      }
    } else {
      if (elm.style.display === 'none') {
        return false;
      }
      if (!ValUtil.isBlank(elm.style.display)) {
        // Backup display style
        DomUtil.setAttr(elm, DomUtil._ORG_ATTR_STYLE_DISPLAY_BACKUP, elm.style.display);
      }
      elm.style.display = 'none';
    }
    return true;
  },

  /**
   * Get element attribute.<br>
   * <ul>
   * <li>Retrieves the value of the specified attribute.</li>
   * <li>Returns <code>null</code> if argument is invalid.</li>
   * <li>Uses dataset API for data-* attributes.</li>
   * </ul>
   * @param {Element} elm Target element
   * @param {string} attrName Attribute name
   * @returns {string|number|null} Attribute value
   */
  getAttr: function(elm, attrName) {
    if (!DomUtil.isExists(elm) || ValUtil.isBlank(attrName)) {
      return null;
    }

    // Use dataset API for data-* attributes
    if (attrName.startsWith('data-')) {
      const datasetKey = DomUtil._convDataAttrToDatasetKey(attrName);
      return elm.dataset[datasetKey];
    }

    return elm.getAttribute(attrName);
  },

  /**
   * Set element attribute.<br>
   * <ul>
   * <li>Sets the value of the specified attribute.</li>
   * <li>Returns <code>false</code> if argument is invalid.</li>
   * <li>Uses dataset API for data-* attributes.</li>
   * </ul>
   * @param {Element} elm Target element
   * @param {string} attrName Attribute name
   * @param {string} val Value to set
   * @returns {boolean} <code>true</code> on success
   */
  setAttr: function(elm, attrName, val) {
    if (!DomUtil.isExists(elm) || ValUtil.isBlank(attrName)) {
      return false;
    }
    const value = ValUtil.nvl(val);

    // Use dataset API for data-* attributes
    if (attrName.startsWith('data-')) {
      const datasetKey = DomUtil._convDataAttrToDatasetKey(attrName);
      elm.dataset[datasetKey] = value;
      return true;
    }

    elm.setAttribute(attrName, value);
    return true;
  },

  /**
   * Check element attribute existence.<br>
   * <ul>
   * <li>Uses dataset API for data-* attributes.</li>
   * </ul>
   * @param {Element} elm Target element
   * @param {string} attrName Attribute name
   * @returns {boolean} <code>true</code> if exists
   */
  hasAttr: function(elm, attrName) {
    if (!DomUtil.isExists(elm) || ValUtil.isBlank(attrName)) {
      return false;
    }

    // Use dataset API for data-* attributes
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
   * Remove element attribute.<br>
   * <ul>
   * <li>Removes the specified attribute.</li>
   * <li>Returns <code>false</code> if argument is invalid.</li>
   * <li>Uses dataset API for data-* attributes.</li>
   * </ul>
   * @param {Element} elm Target element
   * @param {string} attrName Attribute name
   * @returns {boolean} <code>true</code> on success
   */
  removeAttr: function(elm, attrName) {
    if (!DomUtil.isExists(elm) || ValUtil.isBlank(attrName)) {
      return false;
    }

    // Use dataset API for data-* attributes
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
   * Convert data-* attribute name to dataset key name.<br>
   * <ul>
   * <li>[Example] <code>'data-obj-row-idx'</code> -> <code>'objRowIndex'</code></li>
   * <li>[Example] <code>'data-check-off-value'</code> -> <code>'checkOffValue'</code></li>
   * </ul>
   * @param {string} attrName data-* attribute name
   * @returns {string} Dataset key name
   */
  _convDataAttrToDatasetKey: function(attrName) {
      if (attrName.indexOf('data-') !== 0) {
        return attrName;
    }
    // Remove 'data-'
    const datasetKey = attrName.substring(5);

    // Convert hyphen to camelCase
    return datasetKey.replace(/-([a-z])/g, function(match, letter) {
      return letter.toUpperCase();
    });
  },

  /**
   * Add CSS class.<br>
   * <ul>
   * <li>Adds a CSS class to the element.</li>
   * <li>Returns <code>false</code> if argument is invalid.</li>
   * </ul>
   * @param {Element} elm Target element
   * @param {string} cls Class name
   * @returns {boolean} <code>true</code> on success
   */
  addClass: function(elm, cls) {
    if (!DomUtil.isExists(elm) || ValUtil.isBlank(cls)) {
      return false;
    }
    elm.classList.add(cls);
    return true;
  },

  /**
   * Remove CSS class.<br>
   * <ul>
   * <li>Removes a CSS class from the element.</li>
   * <li>Returns <code>false</code> if argument is invalid.</li>
   * <li>Returns <code>false</code> if target class does not exist.</li>
   * </ul>
   * @param {Element} elm Target element
   * @param {string} cls Class name
   * @returns {boolean} <code>true</code> on success
   */
  removeClass: function(elm, cls) {
    if (!DomUtil.isExists(elm) || ValUtil.isBlank(cls) || !DomUtil.hasClass(elm, cls)) {
      return false;
    }
    elm.classList.remove(cls);
    return true;
  },

  /**
   * Check CSS class existence.<br>
   * <ul>
   * <li>Checks if the element has the specified CSS class.</li>
   * </ul>
   * @param {Element} elm Target element
   * @param {string} cls Class name
   * @returns {boolean} <code>true</code> if has class
   */
  hasClass: function(elm, cls) {
    if (!DomUtil.isExists(elm) || ValUtil.isBlank(cls)) {
      return false;
    }
    return elm.classList.contains(cls);
  },
};

/**
 * Page operation utility class.<br>
 * <ul>
 *   <li>Performs operations targeting the entire page or specific areas of the page.</li>
 *   <li>Performs operations such as message display, error existence check, form clear, form enable/disable, and form show/hide.</li>
 *   <li>"List" refers to both grids (repeating rows) and details (repeating rows). Explanations about lists apply to both.</li>
 * </ul>
 * @class
 */
const PageUtil = /** @lends PageUtil */ {

  /** @private The <code>id</code> attribute name of message display area element and key for message array in response data (specified in Io.java). */
  _ITEMID_MSG: '_msg',
  /** @private Key that becomes <code>true</code> if error message exists (specified in Io.java). */
  _ITEMID_HAS_ERR: '_has_err',
  /** @private Attribute name for <code>title</code> attribute backup. */
  _ORG_ATTR_TITLE_BACKUP: 'data-title-backup',
  /** @private <code>name</code> attribute name for associative array conversion of list radio buttons. */
  _ORG_ATTR_DETAIL_RADIO_OBJ_NAME: 'data-radio-obj-name',

  /**
   * Display message.<br>
   * <ul>
   *   <li>Display message from response data.</li>
   *   <li>The key for response data is <code>'_msg'</code>.</li>
   *   <li>Element with <code>id</code> attribute <code>'_msg'</code> is treated as message display area element.</li>
   *   <li>If multiple message display area elements exist, set to the first element.</li>
   *   <li>To control visibility, execute <code>PageUtil#clearMsg()</code> in page initialization to hide.</li>
   *   <li>If no message exists in response data, clear the text of message display area element (<code>PageUtil#clearMsg()</code> is executed).</li>
   * </ul>
   * @param {Object} res Response data
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
    // Display message in message display area
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

    // Highlight item and set message to <code>title</code> attribute
    for (const msg of msgs) {
      const itemName = msg['item'];
      const rowIdx = msg['row'];
      if (!ValUtil.isBlank(itemName)) {
        let elm;
        if (!ValUtil.isBlank(rowIdx)) {
          // If row index value is specified, also consider <code>data-obj-row-idx</code> attribute when getting
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
          // Backup <code>title</code> attribute and set message
          if (!ValUtil.isBlank(elm.title)) {
            DomUtil.setAttr(elm, PageUtil._ORG_ATTR_TITLE_BACKUP, elm.title);
          }
          elm.title = msg['text'];
        }
      }
    }
  },

  /**
   * Check error existence.<br>
   * <ul>
   *   <li>Check for error existence from response data.</li>
   *   <li>The key for response data is <code>'_has_err'</code>.</li>
   * </ul>
   * @param {Object} res Response data
   * @returns {boolean} <code>true</code> if error exists
   */
  hasError: function (res) {
    if (!ValUtil.isObj(res)) {
      throw new Error('PageUtil#hasErr: Argument response is invalid. ');
    }
    const hasErr = res[PageUtil._ITEMID_HAS_ERR];
    return ValUtil.isTrue(hasErr);
  },

  /**
   * Clear message.<br>
   * <ul>
   *   <li>Clear the text of message display area element.</li>
   * </ul>
   */
  clearMsg: function() {
    const msgElm = DomUtil.getById(PageUtil._ITEMID_MSG);
    if (!DomUtil.isExists(msgElm)) {
      throw new Error('PageUtil#clearMsg: Message element not found. ');
    }
    msgElm.innerHTML = '<ul></ul>';
    DomUtil.setVisible(msgElm, false, false);
    
    // Clear item highlight and restore <code>title</code> attribute
    const elms = DomUtil.getsSelector('.info-item, .warn-item, .err-item');
    for (const elm of elms) {
      // Remove CSS class 
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
   * Get page data.<br>
   * <ul>
   *   <li>Get values of data-sending HTML elements (<code>&lt;input&gt;</code>, <code>&lt;select&gt;</code>, <code>&lt;textarea&gt;</code>) on the page as an associative array.</li>
   *   <li>Target HTML elements are those with <code>name</code> attribute set, and <code>name</code> attribute becomes the key of the associative array.</li>
   *   <li>If scope element argument is omitted, <code>&lt;main&gt;</code> is used as scope; if <code>&lt;main&gt;</code> does not exist, <code>document.body</code> is used as scope.</li>
   *   <li>Mainly used to get request data for Web services.</li>
   *   <li>The following differ from normal <code>&lt;form&gt;</code> POST submission:
   *   <ul>
   *     <li>Disabled items are included.</li>
   *     <li>Style-hidden items (<code>display:none</code> or <code>visibility:hidden</code>) are not included.</li>
   *   </ul></li>
   *   <li>If <code>data-value-format-type</code> attribute defining value format type is set, get unformatted value using corresponding <code>UnFrmUtil</code> method.</li>
   *   <li>For text boxes and text areas, get value with tab characters and trailing blank line breaks removed.</li>
   *   <li>For checkboxes, return <code>value</code> attribute value when checked, and get <code>data-check-off-value</code> attribute value when not checked.</li>
   *   <li>List section (repeating section) data becomes an array and is stored in associative array with one key.</li>
   *   <li><pre>[Example] <code>&lt;input name="user_id" value="U001"&gt;
   *      &lt;input name="birth_dt" value="2025/02/10"&gt;
   *      &lt;table&gt;...omitted...&lt;tbody id="detail"&gt;
   *         &lt;tr&gt;&lt;td&gt;&lt;input name="detail.pet_no" value="1"&gt;&lt;/td&gt;
   *             &lt;td&gt;&lt;input name="detail.weight_kg" value="8.9"&gt;&lt;/td&gt;&lt;/tr&gt;
   *         &lt;tr&gt;&lt;td&gt;&lt;input name="detail.pet_no" value="2"&gt;&lt;/td&gt;
   *             &lt;td&gt;&lt;input name="detail.weight_kg" value="12.1"&gt;&lt;/td&gt;&lt;/tr&gt;
   *       &lt;/tbody&gt;&lt;/table&gt;</code> is
   *       retrieved as <code>{ user_id:'U001', birth_dt:'20250210', detail:[{pet_no:'1', weight_kg:'8.9'}, {pet_no:'2', weight_kg:'12.1'}] }</code>.</pre></li>
   *   <li>List section elements should follow the rules below:
   *   <ul> 
   *     <li>Elements within a row (hereinafter called row-inner elements) have <code>name</code> attribute in <code>tableId.itemName</code> format with <code>"."</code> separator. Note that <code>name</code> attribute with <code>"."</code> separator should only be used for row-inner elements.</li>
   *     <li>Row-inner elements must have a parent/grandparent element (hereinafter called table element) with <code>id</code> attribute equal to the part before <code>"."</code> separator (<code>tableId</code>).<br>
   *         In most cases, table element is <code>&lt;tbody&gt;</code> or <code>&lt;table&gt;</code>.</li>
   *     <li>Direct children of table element become the topmost element of repeating section (hereinafter called row element).<br>
   *         In most cases, row element is <code>&lt;tr&gt;</code>.</li>
   *     <li><pre>[NG Example 1] Table element does not exist. (Neither <code>&lt;table&gt;</code> nor <code>&lt;tbody&gt;</code> has <code>id</code> attribute)
   *       <code>&lt;table&gt;...omitted...&lt;tbody&gt;
   *         &lt;tr&gt;&lt;td&gt;&lt;input name="detail.pet_nm"&gt;&lt;/td&gt;&lt;/tr&gt;
   *         &lt;tr&gt;&lt;td&gt;&lt;input name="detail.pet_nm"&gt;&lt;/td&gt;&lt;/tr&gt;
   *       &lt;/tbody&gt;&lt;/table&gt;</code></pre></li>
   *     <li><pre>[NG Example 2] Row element does not exist directly under table element. (<code>&lt;table&gt;</code> has <code>id</code> attribute but <code>&lt;tbody&gt;</code> is in between)
   *       <code>&lt;table id="detail"&gt;...omitted...&lt;tbody&gt;
   *         &lt;tr&gt;&lt;td&gt;&lt;input name="detail.pet_nm"&gt;&lt;/td&gt;&lt;/tr&gt;
   *         &lt;tr&gt;&lt;td&gt;&lt;input name="detail.pet_nm"&gt;&lt;/td&gt;&lt;/tr&gt;
   *       &lt;/tbody&gt;&lt;/table&gt;</code></pre></li>
   *     <li><pre>[OK Example 1] If <code>&lt;tbody&gt;</code> has <code>id</code> attribute, <code>&lt;tbody&gt;</code> becomes table element.
   *       <code>&lt;table&gt;...omitted...&lt;tbody id="detail"&gt;
   *         &lt;tr&gt;&lt;td&gt;&lt;input name="detail.pet_nm"&gt;&lt;/td&gt;&lt;/tr&gt;
   *         &lt;tr&gt;&lt;td&gt;&lt;input name="detail.pet_nm"&gt;&lt;/td&gt;&lt;/tr&gt;
   *       &lt;/tbody&gt;&lt;/table&gt;</code></pre></li>
   *     <li><pre>[OK Example 2] If <code>&lt;table&gt;</code> has <code>id</code> attribute, <code>&lt;table&gt;</code> becomes table element. (Example using multiple <code>&lt;tbody&gt;</code>)
   *       <code>&lt;table id="detail"&gt;...omitted...
   *         &lt;tbody&gt;&lt;tr&gt;&lt;td&gt;&lt;input name="detail.pet_nm"&gt;&lt;/td&gt;&lt;/tr&gt;&lt;/tbody&gt;
   *         &lt;tbody&gt;&lt;tr&gt;&lt;td&gt;&lt;input name="detail.pet_nm"&gt;&lt;/td&gt;&lt;/tr&gt;&lt;/tbody&gt;
   *       &lt;/table&gt;</code></pre></li>
   *   </ul></li>
   *   <li>Row-inner elements store row index value in <code>data-obj-row-idx</code> attribute, and are converted to array based on that index.</li>
   *   <li>Radio buttons within rows have [row index] removed from end of <code>name</code> attribute for return value key. (See <code>PageUtil#setValue</code>)</li>
   * </ul>
   * @param {Object} [outerElm] Scope element (optional)
   * @returns {Object} Page data associative array
   */
  getValues: function(outerElm) {
    outerElm = outerElm || DomUtil._getByTag('main') || document.body;
    if (!DomUtil.isExists(outerElm)) {
      throw new Error('PageUtil#getValues: Argument element is invalid. ');
    }

    // Add row index
    PageUtil._setRowIndex(outerElm);
    // Get target elements
    const targetElms = DomUtil.getsSelector('input[name],select[name],textarea[name]', outerElm);

    const jsonData = {};
    const listObj = {};
    for (const elm of targetElms) {
      if (!DomUtil.isVisible(elm)) {
        // Ignore hidden elements
        continue;
      }
      if (PageUtil._isRadioOff(elm)) {
        // Ignore unchecked radio buttons
        continue;
      }
      const name = elm.getAttribute('name');
      const listNameSepPos = name.indexOf('.');
      if (listNameSepPos > 0 && DomUtil.hasAttr(elm, DomUtil._ORG_ATTR_OBJ_ROW_INDEX)) {
        // List conversion
        // Temporarily store row-by-row arrays in a map
        const listId = name.substring(0, listNameSepPos);
        let colName = name.substring(listNameSepPos + 1);
        const nameIndexWrapPos = colName.indexOf('[');
        if (nameIndexWrapPos > 1) {
          // For radio buttons within rows, remove [n] from <code>name</code> attribute
          colName = colName.substring(0, nameIndexWrapPos);
        }
        const rowIdx = ~~DomUtil.getAttr(elm, DomUtil._ORG_ATTR_OBJ_ROW_INDEX);
        if (ValUtil.isNull(listObj[listId])) {
          // If not in map, create new array
          listObj[listId] = [];
        }
        const list = listObj[listId];
        let row = list[rowIdx];
        if (ValUtil.isNull(row)) {
          // If not in array, create new row object
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
   * Get row data.<br>
   * <ul>
   *   <li>Get one row of page data within list as associative array.</li>
   *   <li>Data retrieval rules are same as <code>PageUtil#getValues</code>.</li>
   *   <li>Part before <code>"."</code> in <code>name</code> attribute (= table element's <code>id</code> attribute) is removed to create associative array key.</li>
   *   <li><pre>[Example] For <code>&lt;table&gt;...omitted...&lt;tbody id="detail"&gt;
   *         &lt;tr&gt;&lt;td&gt;&lt;input name="detail.pet_no" value="1"&gt;&lt;/td&gt;
   *             &lt;td&gt;&lt;input name="detail.weight_kg" value="8.9"&gt;&lt;/td&gt;&lt;/tr&gt;
   *         &lt;tr&gt;&lt;td&gt;&lt;input name="detail.pet_no" value="2"&gt;&lt;/td&gt;
   *             &lt;td&gt;&lt;input name="detail.weight_kg" value="12.1"&gt;&lt;/td&gt;&lt;/tr&gt;
   *       &lt;/tbody&gt;&lt;/table&gt;</code> when specifying 2nd row as argument,
   *       <code>{ pet_no:'2', weight_kg:'12.1' }</code> is retrieved.</pre></li>
   *   <li>For radio buttons within row, [row index] at end of <code>name</code> attribute is removed to create return value key. (See <code>PageUtil#setValues</code>)</li>
   * </ul>
   * @param {Element} rowElm Row element (typically <code>&lt;tr&gt;</code>)
   * @returns {Object} Row data associative array
   */
  getRowValues: function(rowElm) {
    if (!DomUtil.isExists(rowElm)) {
      throw new Error('PageUtil#getRowValues: Argument element is invalid. ');
    }

    // Get target elements
    const targetElms = DomUtil.getsSelector('input[name],select[name],textarea[name]', rowElm);

    const jsonData = {};
    for (const elm of targetElms) {
      if (!DomUtil.isVisible(elm)) {
        // Ignore hidden elements
        continue;
      }
      if (PageUtil._isRadioOff(elm)) {
        // Ignore unchecked radio buttons
        continue;
      }
      let colName = elm.getAttribute('name');
      const listNameSepPos = colName.indexOf('.');
      if (listNameSepPos > 0) {
        colName = colName.substring(listNameSepPos + 1);
        const nameIndexWrapPos = colName.indexOf('[');
        if (nameIndexWrapPos > 1) {
          // For radio buttons within a row, remove [n] from <code>name</code> attribute
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
   * Get row data.<br>
   * <ul>
   *   <li>Traverse parent elements from base element argument to get row element (mostly <code>&lt;tr&gt;</code>), and get page data of that row element as an associative array.</li>
   *   <li>Data retrieval rules are same as <code>PageUtil#getRowValues</code>.</li>
   * </ul> 
   * @param {Element} baseElm Base element
   * @param {string} [rowTag] Row element tag name (optional) defaults to 'tr' if omitted
   * @returns {Object} Row data associative array
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
   * Set page data.<br>
   * <ul>
   *   <li>Set associative array values to HTML elements on the page.</li>
   *   <li>Target HTML elements are those with <code>name</code> attribute or <code>data-name</code> attribute set, and associative array key becomes the <code>name</code> attribute or <code>data-name</code> attribute of target element.</li>
   *   <li>To set values to non-input form elements (that do not originally have <code>name</code> attribute) like <code>&lt;span&gt;</code> or <code>&lt;td&gt;</code>, set <code>data-name</code> attribute.</li>
   *   <li>For HTML elements without <code>value</code> attribute like <code>&lt;span&gt;</code> or <code>&lt;td&gt;</code>, set to <code>textContent</code>.</li>
   *   <li>If scope element argument is omitted, <code>&lt;main&gt;</code> is used as scope; if <code>&lt;main&gt;</code> does not exist, <code>document.body</code> is used as scope.</li>
   *   <li>Mainly used to set response data from Web services.</li>
   *   <li>Values with keys starting with underscore are used by this framework and cannot be used outside this class, so they are ignored by this method.</li>
   *   <li>If <code>data-value-format-type</code> attribute defining value format type is set, set formatted value using corresponding <code>FrmUtil</code> method.</li>
   *   <li>List section data is assumed to be stored as an array in one key of the associative array.</li>
   *   <li><pre>[Example] <code>{ user_id:'U001', birth_dt:'20250210', list:[{pet_no:'1', weight_kg:'8.9'}, {pet_no:'2', weight_kg:'12.1'}] }</code> is
   *     set as <code>&lt;input name="user_id" value="U001"&gt;
   *     &lt;input name="birth_dt" value="2025/02/10"&gt;
   *     &lt;table&gt;...omitted...&lt;tbody id="detail"&gt;
   *       &lt;tr&gt;&lt;td data-name="detail.pet_no"&gt;1&lt;/td&gt;
   *           &lt;td&gt;&lt;input name="detail.weight_kg" value="8.9"&gt;&lt;/td&gt;&lt;/tr&gt;
   *       &lt;tr&gt;&lt;td data-name="detail.pet_no"&gt;2&lt;/td&gt;
   *           &lt;td&gt;&lt;input name="detail.weight_kg" value="12.1"&gt;&lt;/td&gt;&lt;/tr&gt;
   *     &lt;/tbody&gt;&lt;/table&gt;</code>.</pre></li>
   *   <li>Rules for table element, row element, and row-inner elements in list are same as <code>PageUtil#getValues</code>.</li>
   *   <li>To synchronize number of arrays in associative array with number of list rows for display, dynamically generate elements from template row element (hereinafter called template row element) and set values.</li>
   *   <li>Template row element is placed as child element (first) of table element wrapped in <code>&lt;script&gt;</code>.</li>
   *   <li><pre>[Example] For above example, place template row element as follows:
   *     <code>&lt;table&gt;...omitted...&lt;tbody id="detail"&gt;
   *       &lt;script type="text/html"&gt;&lt;tr&gt;&lt;td data-name="detail.pet_no"&gt;&lt;/td&gt;
   *                                    &lt;td&gt;&lt;input name="detail.weight_kg"&gt;&lt;/td&gt;&lt;/tr&gt;
   *       &lt;/script&gt;
   *     &lt;/tbody&gt;&lt;/table&gt;</code></pre></li>
   *   <li>Radio buttons within template row element have [row index] appended to end of <code>name</code> attribute to group by row.</li>
   * </ul>
   * @param {Object} obj Associative array data
   * @param {Element} [outerElm] Scope element (optional, defaults to <code>document.body</code> if omitted)
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
      // Values starting with underscore are framework-reserved, not accessible outside this class, ignored by this method
      if (name.indexOf('_') === 0) {
        continue;
      }

      let val = obj[name];

      // Set to list section if array
      if (ValUtil.isAry(val)) {
        PageUtil._setRowValues(name, val, outerElm);
        continue;
      }

      if (val != null) {
        const valto = typeof (val);
        if (valto !== 'string' && valto !== 'number' && valto !== 'boolean') {
          // Skip non-primitive types
          continue;
        }
      }
      val = ValUtil.nvl(val);
      PageUtil._getElmToSetElmFormatVal(name, val, outerElm);
    }
  },

  /**
   * @private
   * Add list rows (multiple rows).<br>
   * <ul>
   *   <li>Assumes template row element exists directly under list parent element argument, generates row elements for array data in associative array argument, sets values, and appends to list parent element.</li>
   * </ul>
   * @param {string} listId <code>id</code> attribute of table element (parent element) ([Example] <code>'detail'</code>)
   * @param {Element} listElm Table element (parent element)
   * @param {Array<Object>} objAry Row data array (each element is an object representing one row of data)
   */
  _addRows: function(listId, listElm, objAry) {
    // Get template row
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

    // Extract element name and attributes from opening tag
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
    // Get current maximum row index
    if (DomUtil.isExists(oldRowElms)) {
      // Get radio buttons within each row element that have [row index] at end of <code>name</code> attribute
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

    // Generate rows for data
    for (const obj of objAry) {
      // Generate row element from template
      const rowElm = document.createElement(rowElmTag);
      // Set row element attributes
      for (const attrName in rowElmAttrs) {
        DomUtil.setAttr(rowElm, attrName, rowElmAttrs[attrName]);
      }
      // Set inner elements
      rowElm.innerHTML = innerHtml;

      // Set values to inner elements
      if (ValUtil.isObj(obj)) {
        for (const colName in obj) {
          const val = ValUtil.nvl(obj[colName]);
          const name = listId + '.' + colName;
          PageUtil._getElmToSetElmFormatVal(name, val, rowElm);
        }
      }
      // Append [row index] to end of <code>name</code> attribute for radio buttons within row to group by row.
      // Original <code>name</code> attribute is stored in <code>data-radio-obj-name</code> attribute
      radioRowIdx++;
      const radioElms = DomUtil.getsSelector('input[type="radio"][name]', rowElm);
      for (const radioElm of radioElms) {
        const name = radioElm.getAttribute('name');
        const rotName = name + `[${radioRowIdx}]`;
        DomUtil.setAttr(radioElm, 'name', rotName);
        DomUtil.setAttr(radioElm, PageUtil._ORG_ATTR_DETAIL_RADIO_OBJ_NAME, name);
      }

      // Append row element to table element
      listElm.appendChild(rowElm);
    }
  },

  /**
   * Add list row.<br>
   * <ul>
   *   <li>Generate and add row element using template row element of table element and set default values.</li>
   *   <li>See <code>PageUtil#setValues</code> for template row.</li>
   *   <li><pre>[Example] For following template row:
   *     <code>&lt;table&gt;...omitted...&lt;tbody id="detail"&gt;
   *       &lt;script type="text/html"&gt;&lt;tr&gt;&lt;td data-name="detail.pet_no"&gt;&lt;/td&gt;
   *                                    &lt;td&gt;&lt;input name="detail.weight_kg"&gt;&lt;/td&gt;&lt;/tr&gt;
   *       &lt;/script&gt;
   *     &lt;/tbody&gt;&lt;/table&gt;</code>
   *     Default value key excludes table element <code>id</code> attribute (detail.) as follows:
   *    <code>{ pet_no:'1', weight_kg:'8.9' }</code>
   *    Multiple rows are added when passing array of associative arrays:
   *    <code>[ { pet_no:'1', weight_kg:'8.9' }, { pet_no:'2', weight_kg:'12.1' } ]</code></pre>
   *   </li>
   *   <li>If default value is omitted, add one empty row element.</li>
   *   <li>To add multiple empty rows, pass <code>new Array(n)</code>.</li>
   * </ul>
   * @param {string} listId <code>id</code> attribute of table element (parent element) ([Example] <code>'detail'</code>)
   * @param {Object|Array<Object>} [obj] Default value associative array for row addition (optional)
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
   * Remove list row.<br>
   * <ul>
   *   <li>Remove row element (mostly <code>&lt;tr&gt;</code>) containing element with specified <code>name</code> attribute and <code>value</code> attribute.</li>
   * </ul>
   * @param {string} searchElmName <code>name</code> attribute of search target element ([Example] <code>'detail.chk'</code> for checkbox) <code>data-name</code> attribute not allowed
   * @param {string} searchElmVal Value of search target element ([Example] <code>'1'</code>)
   * @param {string} [rowTag] Row element tag name (optional) defaults to <code>'tr'</code> if omitted
   * @returns {boolean} <code>true</code> on successful deletion
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
        // Ignore unchecked checkboxes and radio buttons
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
   * Clear all rows.<br>
   * <ul>
   *   <li>Remove all row elements except template row.</li>
   * </ul>
   * @param {string} listId <code>id</code> attribute of table element (parent element) ([Example] <code>'detail'</code>)
   */
  clearRows: function (listId) {
    if (ValUtil.isBlank(listId)) {
      throw new Error('PageUtil#addRow: Argument listId is invalid. ');
    }
    const listElm = DomUtil.getById(listId);
    if (!DomUtil.isExists(listElm)) {
      console.warn(`PageUtil#addRow: List element not found. id=${listId}`);
      return;
    }
    PageUtil._removeAllRows(listElm);
  },

  /**
   * @private
   * Clear all rows.<br>
   * <ul>
   *   <li>Remove all row elements except template row.</li>
   * </ul>
   * @param {Element} listElm Table element (parent element)
   */
  _removeAllRows: function(listElm) {
    // Delete all existing rows (except template row)
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
   * Add index to row-inner elements using custom attribute.<br>
   * <ul>
   *   <li>Target row-inner elements (elements with <code>name</code> attribute containing <code>".'"</code> separator) that are data-sending elements (<code>&lt;input&gt;</code>, <code>&lt;select&gt;</code>, <code>&lt;textarea&gt;</code>).</li>
   *   <li>Add index as <code>data-obj-row-idx</code> attribute.</li>
   *   <li>Added index is used when converting to associative array in <code>PageUtil#getValues</code>.</li>
   *   <li>Added index also serves as marker for response value display returned from Web service.</li>
   *   <li>Index starts from zero and increments for each row element.</li>
   *   <li>If row element has no row-inner elements in its child/descendant elements, that row is ignored. (No increment)</li>
   *   <li>See JSDoc of <code>PageUtil#getValues</code> for details.</li>
   * </ul>
   * @param {Object} outerElm Index addition scope element
   */
  _setRowIndex : function(outerElm) {
    if (ValUtil.isNull(outerElm)) {
      throw new Error('PageUtil#_setRowIndex: Target element required.');
    }
    // Get row-inner elements
    const rowInElms = DomUtil.getsSelector('input[name*="."],select[name*="."],textarea[name*="."]', outerElm);
    // Find list elements from page
    const listObj = {};
    for (const elm of rowInElms) {
      const name = elm.getAttribute('name');
      const listId = name.substring(0, name.indexOf('.'));
      if (listObj[listId]) {
        // Skip if already exists in map
        continue;
      }
      const listElm = DomUtil._getParentById(elm, listId);
      if (!DomUtil.isExists(listElm)) {
        throw new Error(`PageUtil#_setRowIndex: List parent element not found. id=#${listId} `);
      }
      listObj[listId] = listElm;
    }

    // Loop by list
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
        // Row-inner elements loop
        for (const colElm of colElms) {
          // Add index to row-inner element
          DomUtil.setAttr(colElm, DomUtil._ORG_ATTR_OBJ_ROW_INDEX, i);
        }
      }
    }
  },

  /**
   * @private
   * Get element (by <code>name</code> attribute or <code>data-name</code> attribute).<br>
   * <ul>
   *   <li>If element cannot be obtained by <code>name</code> attribute, get by <code>data-name</code> attribute.</li>
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
   * Set row section data.
   * 
   * @param {string} listId <code>id</code> attribute of table element (parent element) ([Example] <code>'detail'</code>)
   * @param {Array<Object>} objAry Array data of associative arrays (one associative array is one row of data)
   * @param {Element} outerElm Scope element
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
    // Delete existing rows (except template row)
    PageUtil._removeAllRows(listElm);
    // Add rows
    PageUtil._addRows(listId, listElm, objAry);
  },
  
  /**
   * @private
   * Get unformatted element value.<br>
   * <ul>
   * <li>For elements without <code>value</code> attribute like labels, return textContent value.</li>
   * <li>See JSDoc of <code>PageUtil#getValues</code> for details.</li>
   * </ul>
   */
  _getElmUnFormatVal: function(elm) {
    if (PageUtil._isCheckType(elm)) {
      // Checkbox or radio button case
      // For radio button, passed element has checked ON.
      if (elm.checked) {
        return ValUtil.nvl(elm.value);
      } else {
        // For OFF, get custom attribute value
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
        // Restore to page if value changed by formatting.
        PageUtil._setElmFormatVal(elm, val);
      }
    } else {
      // Processing for labels etc.
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
   * Get element and set value.<br>
   * <ul>
   * <li>For radio buttons, select appropriate element and set value.</li>
   * </ul>
   * @param {string} name Value of <code>name</code> attribute or <code>data-name</code> attribute
   * @param {string} val Value to set
   * @param {Element} outerElm Scope element
   * @returns {boolean} <code>true</code> if set succeeded, <code>false</code> if failed
   */
  _getElmToSetElmFormatVal: function(name, val, outerElm) {
    let elm = PageUtil._getElmBynNameOrDataName(name, outerElm);
    if (!DomUtil.isExists(elm)) {
      console.warn(`PageUtil#_getElmToSetElmFormatVal: Element not found. name=${name}`);
      return false;
    }
    if (PageUtil._isRadioNotVal(elm, val)) {
      // If radio button element with value not matching specified value is passed (first element with same name did not have specified value), replace with element of specified value
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
   * Set element value.
   */
  _setElmFormatVal: function(elm, val) {
    val = ValUtil.nvl(val);
    if (PageUtil._isCheckType(elm)) {
      // Checkbox or radio button case
      // For radio button, element selected by <code>value</code> attribute is passed, so it will always be checked ON.
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
   * Determine if HTML element has value.
   */
  _hasValueProp: function(elm) {
    const tag = elm.tagName.toLowerCase();
    return (tag === 'input' || tag === 'select' || tag === 'textarea');
  },

  /**
   * @private
   * Determine if checkbox or radio button.
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
   * Determine if radio button and checked OFF.
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
   * Determine if radio button and value is not specified value.
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
   * Determine if text input element (including hidden).
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
   * Determine if text area.
   */
  _isTextArea: function(elm) {
    const tag = elm.tagName.toLowerCase();
    return (tag === 'textarea');
  },

  /**
   * @private
   * Web service submission character conversion.<br>
   * <ul>
   * <li>Processing on Web service is high load, so adjust each item on client side.</li>
   * <li>Remove tab characters and trailing blanks.</li>
   * <li>Also remove line breaks or unify to LF.</li>
   * </ul>
   * 
   * @param {string} val Processing value
   * @param {boolean} [isRetIgnore] <code>true</code> to keep line breaks (optional)
   * @returns {string} Converted value
   */
  _convPostVal: function(val, isRetIgnore) {
    // Remove tab characters
    const txt = ValUtil.nvl(val).replace(/\t/g, ' ');
    if (isRetIgnore) {
      // Keep line breaks (unified to LF), remove trailing blanks
      return txt.replace(/\r?\n/g, '\n').replace(/ +$/, '');
    }
    // Remove line breaks (replace with single-byte blank), remove trailing blanks
    return txt.replace(/\r?\n/g, ' ').replace(/ +$/, '');
  },

  /** 
   * @private
   * Split HTML tag into first HTML tag, its closing tag, and other inner tags.<br>
   * <ul>
   *   <li>Find first &gt; and last &lt; not enclosed in double or single quotes and split.</li>
   * </ul>
   * @param {string} html HTML string
   * @returns {Array<string>} [first tag, inner tag, first tag's closing tag] (zero-length array if not found)
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
      // First tag not found
      return [];
    }

    i = html.length - 1;
    while (i >= 0) {
      const char = html[i];
      // Assuming no quotes inside closing tag
      if (char === '<') {
        // Found <
        outerEndStart = i;
        break;
      }
      i--;
    }
    if (outerEndStart < 0 || outerEndStart <= outerBeginEnd) {
      // Closing tag of first tag not found
      return [];
    }

    const tags = [html.substring(0, outerBeginEnd + 1), html.substring(outerBeginEnd + 1, outerEndStart), html.substring(outerEndStart)];
    return tags;
  },

  /**
   * @private
   * Parse opening tag.<br>
   * <ul>
   *   <li>Extract tag name and attributes from opening tag.</li>
   *   <li>Assumes HTML tag is blank-separated, attributes are <code>=</code>-separated or without separator like <code>readonly</code>.</li>
   *   <li><pre>[Example] <code>&lt;tr class="row" style="color:black" hidden&gt;</code> returns
   *      <code>['tr', {class: 'row', style: 'color:black', hidden: 'hidden'}]</code>.</pre></li>
   * </ul>
   * @param {string} htmlTag Opening tag string
   * @returns {Array<string, Object>|null} [tag name, attributes associative array] (returns <code>null</code> if cannot parse)
   */
  _parseHtmlOpenTag: function(htmlTag) {
    if (ValUtil.isBlank(htmlTag)) {
      return null;
    }
    htmlTag = ValUtil.nvl(htmlTag).trim();
    // Remove < and > and split by blank
    htmlTag = htmlTag.substring(1, htmlTag.length - 1).trim();
    const tags = htmlTag.split(' ');

    // Tag name is up to first blank
    const tagName = tags[0].toLowerCase();
    // Attribute values
    const attrs = {};

    // Parse parts other than tag name as attributes
    for (const tag of tags.slice(1)) {
      const att = tag.trim();
      if (ValUtil.isBlank(att)) {
        continue;
      }

      const eqPos = att.indexOf('=');
      if (eqPos < 0) {
        // Valueless attribute like readonly
        attrs[att] = att;
        continue;
      }

      // Format: attribute-name=value
      const attrName = att.substring(0, eqPos);
      let attrVal = att.substring(eqPos + 1).trim();

      // Remove quotes
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
 *   <li>Store and retrieve associative arrays in browser session storage by the following scope + key:</li>
 *   <ul>
 *     <li>Page scope (per URL HTML file, data persists within one page)</li>
 *     <li>Module scope (per URL module directory, data shared across pages)</li>
 *     <li>System scope (data shared across entire system)</li>
 *   </ul>
 *   <li>Assumes non-critical processing, so generally does not throw exception errors.</li>
 * </ul>
 * @class
 */
const StorageUtil = /** @lends StorageUtil */ {

  /** @private Page scope key prefix */
  _KEY_PREFIX_PAGE: '@page',
  /** @private Module scope key prefix */
  _KEY_PREFIX_MODULE: '@module',
  /** @private System common key prefix */
  _KEY_PREFIX_SYSTEM: '@system',

  /** @private Root directory name */
  _ROOT_DIR_NAME: '[root]',

  /**
   * Get page scope (per URL HTML file) data. <br>
   * <ul>
   *   <li>Retrieve associative array from browser session storage by page scope + key.</li>
   * </ul>
   * @param {string} key Retrieval key
   * @param {Object} [notExistsValue] Value to return when not exists (optional)
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
   * Get module scope (per URL module directory) data. <br>
   * <ul>
   *   <li>Retrieve associative array from browser session storage by module scope + key.</li>
   * </ul>
   * @param {string} key Retrieval key
   * @param {Object} [notExistsValue] Value to return when not exists (optional)
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
   * Get system scope data.<br>
   * <ul>
   *   <li>Retrieve associative array from browser session storage by key.</li>
   * </ul>
   * @param {string} key Retrieval key
   * @param {Object} [notExistsValue] Value to return when not exists (optional)
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
   * Store page scope data (per URL HTML file).<br>
   * <ul>
   *   <li>Store associative array in browser session storage by page scope + key.</li>
   * </ul>
   * @param {string} key Storage key
   * @param {Object} obj Data to store
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
   * Store module scope data (per URL module directory).<br>
   * <ul>
   *   <li>Store associative array in browser session storage by module scope + key.</li>
   * </ul>
   * @param {string} key Storage key
   * @param {Object} obj Data to store
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
   * Store system scope data.<br>
   * <ul>
   *   <li>Store associative array in browser session storage by key.</li>
   * </ul>
   * @param {string} key Storage key
   * @param {Object} obj Data to store
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
   * Remove page scope data.
   * @param {string} key Key
   * @returns {boolean} <code>true</code> on successful deletion
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
   * Remove module scope data.
   * @param {string} key Key
   * @returns {boolean} <code>true</code> on successful deletion
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
   * Remove system scope data.
   * @param {string} key Deletion key
   * @returns {boolean} <code>true</code> on successful deletion
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
   * Clear all data.<br>
   * <ul>
   *   <li>Remove all data stored by this utility.</li>
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
   * Validate arguments for retrieval.
   * @param {string} methodName Retrieval method name
   * @param {string} key Retrieval key
   * @param {Object} [notExistsValue] Value to return when not exists (optional)
   * @returns {boolean} <code>false</code> if error
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
   * Validate arguments for storage.
   * @param {string} methodName Retrieval method name
   * @param {string} key Retrieval key
   * @param {Object} obj Data to store
   * @returns {boolean} <code>false</code> if error
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
   * Get data.
   * @param {string} key Key
   * @param {Object} [notExistsValue] Value to return when not exists (optional)
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
      // Delete corrupted data
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
   * Store data.
   * @param {string} key Key
   * @param {Object} obj Data to store
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
   * Remove data.
   * @param {string} key Key
   * @returns {boolean} <code>true</code> on successful deletion
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
   * Generate page scope key.
   * @param {string} key Key
   * @returns {string} Page scope key
   */
  _createPageKey: function(key) {
    return StorageUtil._createPageKeyPrefixByLocation() + key;
  },

  /**
   * @private
   * Generate page scope key prefix for current page.
   * @returns {string} Page scope key prefix
   */
  _createPageKeyPrefixByLocation: function() {
    // Get page name from location
    const paths = location.pathname.split('/');
    // Get page name
    let pageName = paths.pop();
    if (ValUtil.isBlank(pageName)) {
      pageName = 'index';
    } else {
      // Remove extension
      const dotPos = pageName.lastIndexOf('.');
      if (dotPos > 0) {
        pageName = pageName.substring(0, dotPos);
      }
    }
    // Get directory name
    let mdlName = paths.pop();
    if (ValUtil.isBlank(mdlName)) {
      // Root directory case
      mdlName = StorageUtil._ROOT_DIR_NAME;
    }
    return `${StorageUtil._KEY_PREFIX_PAGE}/${mdlName}/${pageName}/`;
  },

  /**
   * @private
   * Generate module scope key.
   * @param {string} key Key
   * @returns {string} Module scope key
   */
  _createModuleKey: function(key) {
    return StorageUtil._createModuleKeyPrefixByLocation() + key;
  },

  /**
   * @private
   * Generate module scope key prefix for current module.
   * @returns {string} Module scope key prefix
   */
  _createModuleKeyPrefixByLocation: function() {
    // Get module name from location
    const paths = location.pathname.split('/');
    // Remove file name
    paths.pop();
    // Get directory name
    let mdlName = paths.pop();
    if (ValUtil.isBlank(mdlName)) {
      // Root directory case
      mdlName = StorageUtil._ROOT_DIR_NAME;
    }
    return `${StorageUtil._KEY_PREFIX_MODULE}/${mdlName}/`;
  },

  /**
   * @private
   * Generate system scope key.
   * @param {string} key Key
   * @returns {string} System scope key
   */
  _createSystemKey: function(key) {
    return `${StorageUtil._KEY_PREFIX_SYSTEM}/${key}`;
  },

  /**
   * Clear all page scope data.<br>
   * <ul>
   *   <li>Remove all data stored for current page.</li>
   * </ul>
   * @returns {boolean} <code>true</code> on successful clear
   */
  clearPage: function() {
    const prefix = StorageUtil._createPageKeyPrefixByLocation();
    return StorageUtil._clear(prefix);
  },

  /**
   * Clear all module scope data.<br>
   * <ul>
   *   <li>Remove all data stored for current module.</li>
   * </ul>
   * @returns {boolean} <code>true</code> on successful clear
   */
  clearModule: function() {
    const prefix = StorageUtil._createModuleKeyPrefixByLocation();
    return StorageUtil._clear(prefix);
  },

  /**
   * Clear system scope data.<br>
   * <ul>
   *   <li>Remove all data stored for system sharing.</li>
   * </ul>
   * @returns {boolean} <code>true</code> on successful clear
   */
  clearSystem: function() {
    const prefix = StorageUtil._KEY_PREFIX_SYSTEM;
    return StorageUtil._clear(prefix);
  },

  /**
   * @private
   * Clear all data with specified prefix.
   * @param {string} prefix Prefix
   * @returns {boolean} <code>true</code> on successful clear
   */
  _clear: function(prefix) {
    let count = 0;
    try {
      // Check all keys in sessionStorage
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
   * For debugging: Display all stored data.<br>
   * <ul>
   *   <li>Display all currently stored data to console.</li>
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

