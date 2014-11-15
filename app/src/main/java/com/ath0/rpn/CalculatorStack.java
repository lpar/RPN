package com.ath0.rpn;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.util.Stack;

import android.util.Log;

/**
 * Model for RPN calculator. 
 * Implements a stack and a set of typical operations on it.
 * Some operations can cause arithmetic exceptions; in those cases, a String 
 * is used to return an error message, and null indicates no error. This
 * violation of good style allows the calling controller to handle operations 
 * uniformly with no knowledge of mathematics, and rely on the stack object's
 * operation method to supply the appropriate error message.
 */
public class CalculatorStack implements Serializable {

  /**
   * Object version for serialization.
   */
  private static final long serialVersionUID = 1L;

  // Number of characters to preallocate when converting a stack value into a 
  // string or storing input from user.
  private static final int TYPICAL_LENGTH = 32;
  // 4x the above
  private static final int TYPICAL_LENGTH_X4 = 128;

  // How many digits of precision (decimal places) are used internally in 
  // calculations.
  private static final int INTERNAL_SCALE = 32;

  private final Stack<BigDecimal> stack;

  // Initial scale is 2 decimal places, as that's the most useful for general 
  // everyday calculations.
  private int scale = 2;

  public CalculatorStack() {
    super();
    this.stack = new Stack<BigDecimal>();
  }

  /**
   * Pushes a value onto the stack.
   * @param number A valid decimal number, in a String. Usually taken from the 
   * InputBuffer.
   */
  public void push(final String number) {
    final BigDecimal newnum = new BigDecimal(number);
    this.stack.push(newnum);
  }

  /**
   * Returns whether the stack is empty.
   */
  public boolean isEmpty() {
    return this.stack.isEmpty();
  }

  /**
   * Gets the contents of the stack as a string.
   * @param levels the number of levels of stack to return
   * @return a text representation of the stack
   */
  public StringBuilder toString(final int levels) {
    final StringBuilder result = new StringBuilder(TYPICAL_LENGTH_X4);
    if (this.stack != null) {
      final int depth = this.stack.size();
      for (int i = 0; i < levels; i++) {
        if (i != 0) {
          result.append('\n');
        }
        final int idx = depth - levels + i;
        if (idx >= 0) {
          result.append(formatNumber(this.stack.get(idx)));
        }
      }
    }
    return result;
  }

  /**
   * Get value without thousands commas for unit tests, to avoid needing to
   * implement formatNumber there.
   */
  @Override
  public String toString() {
    return this.toString(1).toString().replaceAll(",", "");
  }

  /**
   * Formats a BigDecimal number to a fixed number of decimal places, and adds 
   * thousands commas.
   * @param number
   * @return
   */
  private String formatNumber(final BigDecimal number) {
    final StringBuilder result = new StringBuilder(TYPICAL_LENGTH);
    result.append(number.setScale(this.scale, 
        RoundingMode.HALF_UP).toPlainString());
    if (this.scale > 0) {
      if (result.indexOf(".") == -1) {
        result.append('.');
      }
      final int zerosAfterPoint = result.length() - result.indexOf(".") - 1;
      for (int i = zerosAfterPoint; i < this.scale; i++) {
        result.append('0');
      }
    }
    // Add commas
    int dot = result.indexOf(".");
    if (dot < 1) {
      dot = result.length();
    }
    int lowindex = 0;
    if (result.charAt(0) == '-') {
      lowindex = 1;
    }
    for (int i = dot - 3; i > lowindex; i -= 3) {
      result.insert(i, ',');
    }
    return result.toString();
  }

  /**
   * Changes the sign of the top number on the stack.
   */
  public void chs() {
    if (!this.stack.isEmpty()) {
      final BigDecimal topnum = this.stack.pop();
      this.stack.push(topnum.negate());
    }
  }

  /**
   * Drops the top element from the stack.
   */
  public void drop() {
    if (!this.stack.isEmpty()) {
      this.stack.pop();
    }
  }

  /**
   * Duplicates the top element on the stack.
   */
  public void dup() {
    if (!this.stack.isEmpty()) {
      final BigDecimal topnum = this.stack.peek();
      this.stack.push(topnum);
    }
  }

  /**
   * Swaps the top two elements on the stack.
   */
  public void swap() {
    if (this.stack.size() > 1) {
      final BigDecimal x = this.stack.pop();
      final BigDecimal y = this.stack.pop();
      this.stack.push(x);
      this.stack.push(y);
    }
  }

  /**
   * Adds together the top two elements on the stack, and replaces them with
   * the result.
   */
  public void add() {
    if (this.stack.size() > 1) {
      final BigDecimal x = this.stack.pop();
      final BigDecimal y = this.stack.pop();
      final BigDecimal r = y.add(x);
      this.stack.push(r);
    }
  }

  /**
   * Subtracts the top number on the stack from the number beneath it, and 
   * replaces them both with the result.
   */
  public void subtract() {
    if (this.stack.size() > 1) {
      BigDecimal x = this.stack.pop();
      BigDecimal y = this.stack.pop();
      BigDecimal r = y.subtract(x);
      this.stack.push(r);
    }
  }

  /**
   * Multiplies the top two numbers on the stack together, and replaces them 
   * with the result.
   */
  public void multiply() {
    if (this.stack.size() > 1) {
      BigDecimal x = this.stack.pop();
      BigDecimal y = this.stack.pop();
      BigDecimal r = y.multiply(x);
      this.stack.push(r);
    }
  }
  
  /**
   * Takes the top item on the stack, and uses its integer value as the power
   * for raising the number beneath it.
   * e.g. before:  X Y  after: X^Y   before: 2 3  after: 8
   * @return an error message, or null if there is no error
   */
  // Returns error message, or null if no error.
  public String power() {
    String result = null;
    if (this.stack.size() > 1) {
      BigDecimal y = this.stack.pop();
      BigDecimal x = this.stack.pop();
      try {
        BigDecimal r;
        try {
          // Try an exact approach first
          int yi = y.intValueExact();
          Log.d("power", "Computed power exactly");
          r = x.pow(yi);
        } catch (ArithmeticException ex) {
          // If we can't compute it exactly, compute an approximate value
          r = approxPow(x,y);
          Log.d("power", "Computed power approximately");
        }
        this.stack.push(r);
      } catch (RuntimeException e) {
        result = e.getMessage();
      }
    }
    return result;
  }

  /**
   * Uses the top number on the stack to divide the number beneath it.
   * Replaces both with the result of the division.
   * e.g. before: x y  after: x/y   before:  4 2  after: 2
   * @return an error message, or null if there is no error
   */
  public String divide() {
    String result = null;
    if (this.stack.size() > 1) {
      BigDecimal x = this.stack.pop();
      BigDecimal y = this.stack.pop();
      // We use HALF_EVEN rounding because this statistically minimizes 
      // cumulative error during repeated calculations.
      try {
        BigDecimal r = y.divide(x, INTERNAL_SCALE,
            RoundingMode.HALF_EVEN);
        this.stack.push(r);
      } catch (ArithmeticException e) {
        result = e.getMessage();
      }
    }
    return result;
  }

  /**
   * Computes the reciprocal of the top element on the stack, and replaces it
   * with the result.
   * @return an error message, or null if there is no error
   */
  public String reciprocal() {
    String result = null;
    if (!this.stack.isEmpty()) {
      BigDecimal x = this.stack.pop();
      try {
        BigDecimal y = BigDecimal.ONE.divide(x, INTERNAL_SCALE, 
            RoundingMode.HALF_EVEN);
        this.stack.push(y);
      } catch (ArithmeticException e) {
        result = e.getMessage();
      }
    }
    return result;
  }

  /**
   * Sets the display scale, in decimal places.
   * Computation is always performed to the INTERNAL_SCALE.
   * @param newscale new scale value
   */
  public void setScale(final int newscale) {
    this.scale = newscale;
  }

  /**
   * Sets the display scale to the integer value of the top element on the
   * stack, as long as that value is less than the INTERNAL_SCALE.
   */
  public void setScale() {
    if (!this.stack.isEmpty()) {
      BigDecimal x = this.stack.pop();
      int sc = x.intValue();
      if (sc < INTERNAL_SCALE) {
        setScale(sc);
      }
    }
  }

  /**
   * Gets the current display scale.
   * @return
   */
  public int getScale() {
    return this.scale;
  }

  /**
   * Computes the square root of the value on the top of the stack, and
   * replaces that value with the result.
   */
  public String sqrt() {
    String result = null;

    if (!this.stack.isEmpty()) {
      try {
        BigDecimal x = sqrt(this.stack.pop(), INTERNAL_SCALE);
        this.stack.push(x);
      } catch (RuntimeException e) {
        result = e.getMessage();
      }
    }
    return result;
  }

  /**
   * Computes the square root of x to a given scale, x >= 0.
   * Use Newton's algorithm.
   * Taken from "Java Number Cruncher: The Java Programmer's Guide to 
   * Numerical Computing" (Ronald Mak, 2003) http://goo.gl/CXpi2
   * @param x the value of x
   * @param scale the desired scale of the result
   * @return the result value
   */
  private static BigDecimal sqrt(final BigDecimal x, final int scale)
  {
    // Check that x >= 0.
    if (x.signum() < 0) {
      throw new IllegalArgumentException("x < 0");
    }
    if (x.signum() == 0) {
      return BigDecimal.ZERO;
    }

    // n = x*(10^(2*scale))
    BigInteger n = x.movePointRight(scale << 1).toBigInteger();

    // The first approximation is the upper half of n.
    int bits = (n.bitLength() + 1) >> 1;
    BigInteger ix = n.shiftRight(bits);
    BigInteger ixPrev;

    // Loop until the approximations converge
    // (two successive approximations are equal after rounding).
    do {
      ixPrev = ix;

      // x = (x + n/x)/2
      ix = ix.add(n.divide(ix)).shiftRight(1);

      Thread.yield();
    } while (ix.compareTo(ixPrev) != 0);

    return new BigDecimal(ix, scale);
  }

  /**
   * Compute the power x^y to a the given scale, using doubles.
   * Loses some precision, but means y can have non integer values.
   */
  private static BigDecimal approxPow(final BigDecimal x, final BigDecimal y)
  {
    double d;

    // Check that |y| >= 1 for negative x.
    if (x.signum() < 0 && y.abs().doubleValue() < 1.0) {
      throw new IllegalArgumentException("|n| < 1");
    }
    // Check that y is positive or 0 for x = 0.
    else if (x.signum() == 0 && y.signum() < 0) {
      throw new IllegalArgumentException("n < 0");
    }

    d = Math.pow(x.doubleValue(), y.doubleValue());
    return new BigDecimal(d);
  }

}
