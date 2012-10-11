package com.ath0.rpn.test;

import junit.framework.TestCase;

import com.ath0.rpn.InputBuffer;

public class InputBufferTest extends TestCase {
  
  public static void testAppend() {
    InputBuffer ib = new InputBuffer();
    ib.append('1');
    ib.append('2');
    ib.append('3');
    assertEquals("Incorrect buffer append", "123", ib.toString());
    ib.append('.');
    ib.append('5');
    assertEquals("Incorrect decimals append", "123.5", ib.toString());
    ib.append('6');
    ib.append('7');
    ib.append('.');
    ib.append('8');
    assertEquals("Incorrect double decimal append", "123.5678", ib.toString());
  }

  public static void testDelete() {
    InputBuffer ib = new InputBuffer();
    ib.delete();
    assertEquals("Incorrect backspace on new buffer", "", ib.toString());
    ib.append('2');
    ib.delete();
    assertEquals("Incorrect backspace on 1-char input", "", ib.toString());
    ib.delete();
    ib.set("123.567");
    ib.delete();
    assertEquals("Incorrect backspace", "123.56", ib.toString());
    ib.delete();
    ib.delete();
    ib.delete();
    ib.append('0');
    ib.append('9');
    assertEquals("Incorrect backspace over decimal", "12309", ib.toString());
    ib.append('.');
    ib.append('8');
    assertEquals("Incorrect post-decimal-deletion decimal append", "12309.8", ib.toString());
  }

}
