
/**
 * Initialization.
 */
const init = async function () {
  // Clear messages
  PageUtil.clearMsg();
  // Retrieve parameters
  const params = HttpUtil.getUrlParams();
  console.log('#init params:', params);
  if (ValUtil.isBlank(params['user_id'])) {
    // Determine as new registration if key is not in parameters
    // New registration initialization
    initInsert();
    return;
  }
  // Update initialization
  await initUpdate(params);
};

/**
 * New registration initialization.
 */
const initInsert = function () {
  // Add and display 5 detail rows
  PageUtil.addRow('detail', new Array(5));
};

/**
 * Update initialization.
 */
const initUpdate = async function (params) {
  // Disable key field
  const codeElm = DomUtil.getByName('user_id');
  DomUtil.setEnable(codeElm, false);
  // Call data retrieval web service
  const res = await HttpUtil.callJsonService('/services/exmodule/ExampleLoad', params);
  console.log('#initUpdate res:', res);
  // Display messages
  PageUtil.setMsg(res);
  // Exit processing on error
  if (PageUtil.hasError(res)) {
    return;
  }
  // Set response
  PageUtil.setValues(res);
  if (ValUtil.isEmpty(res['detail'] )) {
    // Add and display 5 detail rows if detail rows are empty
    PageUtil.addRow('detail', new Array(5));
    return;
  }
};

/**
* Add row processing.
*/
const addRow = function () {
  PageUtil.addRow('detail');
};

/**
* Remove row processing.
*/
const removeRow = function () {
  PageUtil.removeRow('detail.chk', '1');
};

/**
 * Upsert processing.
 */
const upsert = async function () {
  // Clear messages
  PageUtil.clearMsg();
  // Retrieve values from entire page
  const req = PageUtil.getValues();
  // Call upsert web service
  const res = await HttpUtil.callJsonService('/services/exmodule/ExampleUpsert', req);
  console.log('#upsert res:', res);
  // Display messages
  PageUtil.setMsg(res);
};

/**
 * Delete processing.
 */
const del = async function () {
  // Clear messages
  PageUtil.clearMsg();
  // Retrieve values from entire page
  const req = PageUtil.getValues();
  // Call delete web service
  const res = await HttpUtil.callJsonService('/services/exmodule/ExampleDelete', req);
  console.log('#del res:', res);
  // Display messages
  PageUtil.setMsg(res);
};

/**
 * Cancel processing.
 */
const cancel = async function () {
  HttpUtil.movePage('listpage.html');
};

// Execute initialization
init();
