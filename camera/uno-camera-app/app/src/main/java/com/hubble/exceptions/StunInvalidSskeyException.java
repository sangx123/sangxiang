/**
 *
 */
package com.hubble.exceptions;

import java.io.IOException;

/**
 * @author Nguyen
 */
public class StunInvalidSskeyException extends IOException {
  public StunInvalidSskeyException(String strMsg) {
    super(strMsg);
  }
}
