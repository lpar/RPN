import static org.junit.Assert.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Random;

import org.junit.Test;


public class CalculatorStackTest {

	// Decimal places to test.
	// Should be less than CalculatorStack.INTERNAL_PRECISION.
	private final int MAX_TEST_PRECISION = 20;
	
	// Digits of precision to test.
	// Must be greater than MAX_TEST_PRECISION.
	private final int MAX_DIGITS = 40; 
	
	// Number of rounds of testing for each numeric operation.
	private final int TEST_ROUNDS = 100;
	
	@Test
	public void testToString() {
		CalculatorStack s = new CalculatorStack();
		s.setInputBuffer("30.00");
		s.enter();
		s.setInputBuffer("1.15");
		s.add();
		s.setScale(4);
		assertEquals("Incorrect padding of decimals", "31.1500", s.toString());
		s.setScale(0);
		assertEquals("Incorrect zero scaling", "31", s.toString());
	}

	@Test
	public void testEnterAndDrop() {
		CalculatorStack s = new CalculatorStack();
		s.setInputBuffer("0078704");
		s.enter();
		assertEquals("Incorrect string to decimal on enter", "78704", s.toString());
		s.enter();
		assertEquals("Double enter destroyed value", "78704", s.toString());
		s.append('4');
		s.append('2');
		s.enter();
		assertEquals("Second entered value failed", "42", s.toString());
		s.drop();
		assertEquals("Drop failed", "78704", s.toString());
		s.drop();
		assertEquals("Double drop failed to empty", "0", s.toString());
		s.drop();
		assertEquals("Drop on empty stack failed", "0", s.toString());
	}

	@Test
	public void testAppend() {
		CalculatorStack s = new CalculatorStack();
		s.append('1');
		s.append('2');
		s.append('3');
		assertEquals("Incorrect buffer append", "123", s.toString());
		s.append('.');
		s.append('5');
		assertEquals("Incorrect decimals append", "123.5", s.toString());
		s.append('6');
		s.append('7');
		s.append('.');
		s.append('8');
		assertEquals("Incorrect double decimal append", "123.5678", s.toString());
	}

	@Test
	public void testBackspace() {
		CalculatorStack s = new CalculatorStack();
		s.backspace();
		assertEquals("Incorrect backspace on new stack", "0", s.toString());
		s.append('2');
		s.backspace();
		assertEquals("Incorrect backspace on 1-char input", "0", s.toString());
		s.backspace();
		s.setInputBuffer("123.567");
		s.backspace();
		assertEquals("Incorrect backspace", "123.56", s.toString());
		s.backspace();
		s.backspace();
		s.backspace();
		s.append('0');
		s.append('9');
		assertEquals("Incorrect backspace over decimal", "12309", s.toString());
		s.append('.');
		s.append('8');
		assertEquals("Incorrect post-decimal-deletion decimal append", "12309.8", s.toString());
	}
	
	@Test
	public void testChs() {
		CalculatorStack s = new CalculatorStack();
		s.chs();
		assertEquals("Incorrect CHS on clean stack", "0", s.toString());
		s.append('0');
		s.append('0');
		s.chs();
		assertEquals("Incorrect CHS on zero", "00", s.toString());
		s.append('3');
		s.chs();
		assertEquals("Incorrect CHS on non-zero with leading zeros", "-003", s.toString());
		s.clear();
		s.append('5');
		s.chs();
		assertEquals("Incorrect CHS on non-zero", "-5", s.toString());
		s.chs();
		assertEquals("Incorrect CHS on negative", "5", s.toString());
	}

	private String randomNumber() {
		StringBuilder s = new StringBuilder(MAX_DIGITS);
		Random r = new Random();
		if (r.nextBoolean()) {
			s.append('-');
		}
		int leftdigits = MAX_DIGITS - MAX_TEST_PRECISION;
		for (int i = 0; i < leftdigits; i++) {
			s.append(Integer.toString(r.nextInt(10)));
		}
		s.append('.');
		for (int i = 0; i < MAX_TEST_PRECISION; i++) {
			s.append(Integer.toString(r.nextInt(10)));
		} 
		return s.toString();
	}
	
	@Test
	public void testAdd() {
		for (int j = 0; j < TEST_ROUNDS; j++) {
			// Pick a random test display scale
			Double testscale = new Double(MAX_TEST_PRECISION * Math.random());
			int scale = testscale.intValue();
			CalculatorStack s = new CalculatorStack();
			String a = randomNumber();
			String b = randomNumber();
			BigDecimal da = new BigDecimal(a);
			BigDecimal db = new BigDecimal(b);
			BigDecimal dr = da.add(db);
			BigDecimal xv = dr.setScale(scale, RoundingMode.HALF_UP);
			System.out.println(a + " + " + b + " = " + xv.toPlainString()
					+ " at scale " + Integer.toString(scale));
			s.setInputBuffer(a);
			s.enter();
			s.setInputBuffer(b);
			s.add();
			s.setScale(scale);
			assertEquals("Incorrect addition", xv.toPlainString(), s.toString());
		}
	}
	
	@Test
	public void testDivide() {
		for (int j = 0; j < TEST_ROUNDS; j++) {
			// Pick a random test display scale
			Double testscale = new Double(MAX_TEST_PRECISION * Math.random());
			int scale = testscale.intValue();
			CalculatorStack s = new CalculatorStack();
			String a = randomNumber();
			String b = randomNumber();
			BigDecimal da = new BigDecimal(a);
			BigDecimal db = new BigDecimal(b);
			BigDecimal dr = da.divide(db, MAX_TEST_PRECISION + 1, RoundingMode.HALF_EVEN);
			BigDecimal xv = dr.setScale(scale, RoundingMode.HALF_UP);
			System.out.println(a + " / " + b + " = " + xv.toPlainString()
					+ " at scale " + Integer.toString(scale));
			s.setInputBuffer(a);
			s.enter();
			s.setInputBuffer(b);
			s.divide();
			s.setScale(scale);
			assertEquals("Incorrect division", xv.toPlainString(), s.toString());
		}
	}

	@Test
	public void testMultiply() {
		for (int j = 0; j < TEST_ROUNDS; j++) {
			// Pick a random test display scale
			Double testscale = new Double(MAX_TEST_PRECISION * Math.random());
			int scale = testscale.intValue();
			CalculatorStack s = new CalculatorStack();
			String a = randomNumber();
			String b = randomNumber();
			BigDecimal da = new BigDecimal(a);
			BigDecimal db = new BigDecimal(b);
			BigDecimal dr = da.multiply(db);
			BigDecimal xv = dr.setScale(scale, RoundingMode.HALF_UP);
			System.out.println(a + " * " + b + " = " + xv.toPlainString()
					+ " at scale " + Integer.toString(scale));
			s.setInputBuffer(a);
			s.enter();
			s.setInputBuffer(b);
			s.multiply();
			s.setScale(scale);
			assertEquals("Incorrect multiplication", xv.toPlainString(), s.toString());
		}
	}

	
}
