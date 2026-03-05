package com.onepg.web;

import com.onepg.util.AbstractIoTypeMap;
import com.onepg.util.Io;
import java.util.ArrayList;
import com.onepg.util.LogUtil;
import com.onepg.util.ValUtil;
import com.onepg.util.Io.MsgType;

import javax.naming.AuthenticationException;
import javax.naming.Context;
import javax.naming.NamingException;
import javax.naming.directory.InitialDirContext;
import java.util.Hashtable;

/**
 * Sign-in service class.
 * @hidden
 */
final class SigninService extends AbstractWebService {

  /** IO key - sign-in ID */
  private static final String IOKEY_SIGNIN_ID = "signin_id";
  /** IO key - sign-in password */
  private static final String IOKEY_SIGNIN_PW = "signin_pw";
  /** IO key - sign-in debug mode flag */
  private static final String IOKEY_SIGNIN_DEBUG = "signin_debug";
  /** Session key - JWT */
  private static final String SSKEY_JWT = "token";
  
  /**
   * {@inheritDoc}
   * <ul>
   *   <li>Retrieves LDAP connection information and JWT settings from <code>web.properties</code>.</li>
   *   <li>Clears session data.</li>
   *   <li>Retrieves the sign-in ID and password from the request and performs LDAP authentication.</li>
   *   <li>Issues a JWT and returns it in the response on successful authentication.</li>
   *   <li>Sets the JWT to blank and returns an error message on authentication failure.</li>
   *   <li>On authentication failure, outputs error information only when the sign-in debug mode flag is enabled ("1").</li>
   *   <li>Does not always output error information because authentication failure due to incorrect passwords is expected, but outputs the stack trace in debug mode because failure due to misconfiguration is also expected.</li>
   *   <li>Does not perform JWT validation (because this is called from an unauthenticated state).</li>
   * </ul>
   */
  @Override
  public void doExecute(final Io io) throws Exception {
    // Clear session data (the #clear() method is unavailable due to AbstractIoTypeMap constraints)
    final AbstractIoTypeMap session = io.session();
    for (final String key : new ArrayList<>(session.keySet())) {
      session.remove(key);
    }
    // Sign-in credentials
    final String id = io.getString(IOKEY_SIGNIN_ID);
    final String pw = io.remove(IOKEY_SIGNIN_PW);

    // LDAP authentication
    final Hashtable<String, String> param = new Hashtable<>();
    param.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
    param.put(Context.PROVIDER_URL, ServerUtil.LDAP_URL);
    param.put(Context.SECURITY_AUTHENTICATION, "simple");
    param.put(Context.SECURITY_PRINCIPAL, ServerUtil.LDAP_USER_DN_FMT.formatted(id));
    param.put(Context.SECURITY_CREDENTIALS, pw);

    try {
      // No exception is thrown on successful authentication, so only close processing is performed here
      (new InitialDirContext(param)).close();
    } catch (final NamingException e) {
      // Output authentication failure as an info log
      if (e instanceof AuthenticationException) {
        super.logger.info("LDAP authentication failed. " + LogUtil.joinKeyVal("id", id));
      } else {
        super.logger.error("LDAP server error during authentication. " + LogUtil.joinKeyVal("id", id));
      }
      if (ValUtil.isTrue(io.getStringNullableOrDefault(IOKEY_SIGNIN_DEBUG, ValUtil.OFF))) {
        // Output error information only when the sign-in debug mode flag is enabled ("1")
        super.logger.error(e, "LDAP authentication error in debug mode. ");
      }
      // Set the JWT to blank and return an error message on authentication failure
      io.session().put(SSKEY_JWT, ValUtil.BLANK);
      io.putMsg(MsgType.ERROR, "es001");
      return;
    }

    // Issue JWT
    final String token = JwtUtil.createToken(id);
    io.session().put(SSKEY_JWT, token);
  }
}
