/**
 * Sign-in processing.
 * Performs LDAP authentication and navigates to the portal page on success.
 */
const signin = async function () {
  // Clear messages
  PageUtil.clearMsg();
  // Retrieve all page values
  const req = PageUtil.getValues();
  // Call sign-in web service
  const res = await HttpUtil.callJsonService('/signin', req);
  console.log('#signin res:', res);
  // Display messages
  PageUtil.setMsg(res);
  // Set response values
  PageUtil.setValues(res);
  // Check token
  if (!SessionUtil.hasToken()) {
    // Authentication failure
    return;
  }
  // Authentication success
  // Navigate to portal page
  HttpUtil.movePage('../index.html');
};
