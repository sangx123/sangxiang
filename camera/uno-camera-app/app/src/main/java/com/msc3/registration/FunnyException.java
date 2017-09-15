package com.msc3.registration;

import java.io.IOException;

public class FunnyException extends IOException {
  public FunnyException(String string) {
    super(string);
  }
}
