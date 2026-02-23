
/**
 * Initialization.
 */
const init = async function () {
  // Clear messages
  PageUtil.clearMsg();
  // Call list initialization web service
  const res = await HttpUtil.callJsonService('/services/exmodule/ExampleListInit');
  // Retrieve previous search conditions
  const old = StorageUtil.getPageObj('searchConditions');
  // Merge response with previous search conditions
  Object.assign(res, old);
  // Set response to search conditions area
  PageUtil.setValues(res, DomUtil.getById('searchConditionsArea'));
};

/**
 * Search button processing.
 */
const search = async function () {
  // Clear messages
  PageUtil.clearMsg();
  // Clear search results area
  PageUtil.clearRows('list');
  // Retrieve values from search conditions area
  const req = PageUtil.getValues(DomUtil.getById('searchConditionsArea'));
  // Save current search conditions to browser storage
  StorageUtil.setPageObj('searchConditions', req); 
  // Call list search web service
  const res = await HttpUtil.callJsonService('/services/exmodule/ExampleListSearch', req);
  // Display messages
  PageUtil.setMsg(res);
  // Exit processing on error
  if (PageUtil.hasError(res)) {
    return;
  }
  // Set response to search results area
  PageUtil.setValues(res, DomUtil.getById('searchResultsArea'));
};

/**
 * Edit button processing.
 */
const editMove = async function (btnElm) {
  // Retrieve data from the row containing the button
  const req = PageUtil.getRowValuesByInnerElm(btnElm);
  // Navigate to edit page with row data as parameters
  HttpUtil.movePage('editpage.html', req);
};

/**
 * New button processing.
 */
const create = async function () {
  // Navigate to edit page without parameters (new registration)
  HttpUtil.movePage('editpage.html');
};

// Execute initialization
init();

