package com.ath0.rpn;

import java.io.Serializable;

import android.util.Log;

/**
 * Implements the calculator's input buffer.
 */
public class InputBuffer implements Serializable {

  /**
   * Object version for serialization.
   */
  private static final long serialVersionUID = 1L;

  // A sensible initial capacity that should fit all everyday numbers.
  private static final int INITIAL_CAPACITY = 32;
  private final StringBuilder buffer = new StringBuilder(INITIAL_CAPACITY);

  public InputBuffer(final String value) {
    super();
    this.set(value);
  }

  public InputBuffer() {
    super();
  }

  /**
   * Appends a given character to the buffer, if the result would be a valid 
   * real number. If '.' is appended to an empty buffer, a '0' is added first.
   * @param ich a digit or '.'
   */
  public void append(final char ich) {
    switch (ich) {
    case '.':
      if (this.buffer.indexOf(".") == -1) {
        if (this.buffer.length() == 0) {
          this.buffer.append('0');
        }
        this.buffer.append('.');
      }
      break;
    case '0':
      if (!"0".equals(this.buffer)) {
        this.buffer.append('0');
      }
      break;
    case '1':
    case '2':
    case '3':
    case '4':
    case '5':
    case '6':
    case '7':
    case '8':
    case '9':
      this.buffer.append(ich);
      break;
    default:
      Log.e("append", "Ignoring character '" + ich + "'");
    }
  }

  /**
   * Deletes the rightmost character in the buffer
   */
  public void delete() {
    final int len = this.buffer.length();
    if (len > 0) {
      this.buffer.setLength(len - 1);
    }
  }

  /**
   * Zaps the buffer to be empty (no value, not even zero)
   */
  public void zap() {
    this.buffer.setLength(0);
  }

  /**
   * Returns whether the buffer is empty (no value, not even zero)
   * @return
   */
  public boolean isEmpty() {
    return this.buffer != null && this.buffer.length() == 0;
  }

  /**
   * Sets the value of the buffer
   * @param value the value, assumed to be a valid numeric
   */
  final public void set(final String value) {
    this.buffer.setLength(0);
    this.buffer.append(value);
  }

  /**
   * Gets the value of the buffer
   * @return the value
   */
  public String get() {
    return this.buffer.toString();
  }

  @Override
  public String toString() {
    return this.get();
  }

}
