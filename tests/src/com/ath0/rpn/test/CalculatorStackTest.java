package com.ath0.rpn.test;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Random;

import junit.framework.TestCase;
import android.util.Log;

import com.ath0.rpn.CalculatorStack;

public class CalculatorStackTest extends TestCase {

  // Decimal places to test.
  // Should be less than CalculatorStack.INTERNAL_PRECISION.
  private final int MAX_TEST_PRECISION = 20;

  // Digits of precision to test.
  // Must be greater than MAX_TEST_PRECISION.
  private final int MAX_DIGITS = 40; 

  // Number of rounds of testing for each numeric operation.
  private final int TEST_ROUNDS = 100;

  public static void testToString() {
    CalculatorStack s = new CalculatorStack();
    s.push("30.00");
    s.push("1.15");
    s.add();
    s.setScale(4);
    assertEquals("Incorrect padding of decimals", "31.1500", s.toString());
    s.setScale(0);
    assertEquals("Incorrect zero scaling", "31", s.toString());
  }

  public static void testPushAndDrop() {
    CalculatorStack s = new CalculatorStack();
    s.setScale(2);
    s.push("0078704");
    Log.d("testEnterAndDrop", "stack = " + s.toString());
    assertEquals("Incorrect string to decimal with leading zeros", "78704.00", s.toString());
    s.push("42");
    assertEquals("Second entered value failed", "42.00", s.toString());
    s.drop();
    assertEquals("Drop failed", "78704.00", s.toString());
    s.drop();
    assertEquals("Double drop failed to empty", "", s.toString());
    s.drop();
    assertEquals("Drop on empty stack failed", "", s.toString());
  }

  public static void testChs() {
    CalculatorStack s = new CalculatorStack();
    s.setScale(2);
    s.chs();
    assertEquals("Incorrect CHS on clean stack", "", s.toString());
    s.push("00");
    s.chs();
    Log.d("testChs", "stack = " + s.toString());
    assertEquals("Incorrect CHS on zero", "0.00", s.toString());
    s.push("003");
    s.chs();
    assertEquals("Incorrect CHS on non-zero with leading zeros", "-3.00", s.toString());
    s.push("5");
    s.chs();
    assertEquals("Incorrect CHS on non-zero", "-5.00", s.toString());
    s.chs();
    assertEquals("Incorrect CHS on negative", "5.00", s.toString());
  }

  private String randomNumber() {
    StringBuilder s = new StringBuilder(this.MAX_DIGITS);
    Random r = new Random();
    if (r.nextBoolean()) {
      s.append('-');
    }
    int leftdigits = this.MAX_DIGITS - this.MAX_TEST_PRECISION;
    for (int i = 0; i < leftdigits; i++) {
      s.append(Integer.toString(r.nextInt(10)));
    }
    s.append('.');
    for (int i = 0; i < this.MAX_TEST_PRECISION; i++) {
      s.append(Integer.toString(r.nextInt(10)));
    } 
    return s.toString();
  }

  public void testAdd() {
    for (int j = 0; j < this.TEST_ROUNDS; j++) {
      // Pick a random test display scale
      Double testscale = Double.valueOf(this.MAX_TEST_PRECISION * Math.random());
      int scale = testscale.intValue();
      CalculatorStack s = new CalculatorStack();
      String a = randomNumber();
      String b = randomNumber();
      BigDecimal da = new BigDecimal(a);
      BigDecimal db = new BigDecimal(b);
      BigDecimal dr = da.add(db);
      BigDecimal xv = dr.setScale(scale, RoundingMode.HALF_UP);
      Log.d("testAdd", a + " + " + b + " = " + xv.toPlainString()
          + " at scale " + Integer.toString(scale));
      s.push(a);
      s.push(b);
      s.add();
      s.setScale(scale);
      assertEquals("Incorrect addition", xv.toPlainString(), s.toString());
    }
  }

  public void testDivide() {
    for (int j = 0; j < this.TEST_ROUNDS; j++) {
      // Pick a random test display scale
      Double testscale = Double.valueOf(this.MAX_TEST_PRECISION * Math.random());
      int scale = testscale.intValue();
      CalculatorStack s = new CalculatorStack();
      String a = randomNumber();
      String b = randomNumber();
      BigDecimal da = new BigDecimal(a);
      BigDecimal db = new BigDecimal(b);
      BigDecimal dr = da.divide(db, this.MAX_TEST_PRECISION + 1, RoundingMode.HALF_EVEN);
      BigDecimal xv = dr.setScale(scale, RoundingMode.HALF_UP);
      Log.d("testDivide", a + " / " + b + " = " + xv.toPlainString()
          + " at scale " + Integer.toString(scale));
      s.push(a);
      s.push(b);
      s.divide();
      s.setScale(scale);
      assertEquals("Incorrect division", xv.toPlainString(), s.toString());
    }
  }

  public void testMultiply() {
    for (int j = 0; j < this.TEST_ROUNDS; j++) {
      // Pick a random test display scale
      Double testscale = Double.valueOf(this.MAX_TEST_PRECISION * Math.random());
      int scale = testscale.intValue();
      CalculatorStack s = new CalculatorStack();
      String a = randomNumber();
      String b = randomNumber();
      BigDecimal da = new BigDecimal(a);
      BigDecimal db = new BigDecimal(b);
      BigDecimal dr = da.multiply(db);
      BigDecimal xv = dr.setScale(scale, RoundingMode.HALF_UP);
      Log.d("testMultiply", a + " * " + b + " = " + xv.toPlainString()
          + " at scale " + Integer.toString(scale));
      s.push(a);
      s.push(b);
      s.multiply();
      s.setScale(scale);
      assertEquals("Incorrect multiplication", xv.toPlainString(), s.toString());
    }
  }

}
