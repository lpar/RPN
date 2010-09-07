package com.ath0.rpn;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Stack;

public class CalculatorStack implements Serializable {

	/**
	 * Object version for serialization.
	 */
	private static final long serialVersionUID = 1L;
	
	// Number of characters to preallocate when converting a stack value into a string
	// or storing input from user.
	private static final int TYPICAL_LENGTH = 32;
	// 4x the above
	private static final int TYPICAL_LENGTH_X4 = 128;
	
	// How many digits of precision (decimal places) are used internally in calculations.
	private static final int INTERNAL_SCALE = 32;

	private final Stack<BigDecimal> stack;

	private int scale = 2;

	public CalculatorStack() {
		super();
		this.stack = new Stack<BigDecimal>();
	}

	public void push(String n) {
		BigDecimal x = new BigDecimal(n);
		this.stack.push(x);
	}
	
	public boolean isEmpty() {
		return this.stack.isEmpty();
	}
	
	/**
	 * Get the contents of the stack as a string
	 * @param levels the number of levels of stack to return
	 * @return
	 */
	public StringBuilder toString(int levels) {
		final StringBuilder result = new StringBuilder(TYPICAL_LENGTH_X4);
		int depth = this.stack.size();
		for (int i = 0; i < levels; i++) {
			if (i != 0) {
				result.append('\n');
			}
			int j = depth - levels + i;
	//		result.append(Integer.toString(i + 1));
	//		result.append(": ");
			if (j >= 0) {
				result.append(formatNumber(this.stack.get(j)));
			}
		}
		return result;
	}
	
	private String formatNumber(final BigDecimal x) {
		final StringBuilder result = new StringBuilder(TYPICAL_LENGTH);
		result.append(x.setScale(this.scale, RoundingMode.HALF_UP).toPlainString());
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
		int dp = result.indexOf(".");
		if (dp < 1) {
			dp = result.length();
		}
		int lowindex = 0;
		if (result.charAt(0) == '-') {
			lowindex = 1;
		}
		for (int i = dp - 3; i > lowindex; i -= 3) {
			result.insert(i, ',');
		}
		return result.toString();
	}
	
	// Changes sign of the input buffer.
	public void chs() {
		if (!this.stack.isEmpty()) {
			BigDecimal x = this.stack.pop();
			this.stack.push(x.negate());
		}
	}

	// Drop top element from stack.
	public void drop() {
		if (!this.stack.isEmpty()) {
			this.stack.pop();
		}
	}
	
	// Dupe top element on stack.
	public void dup() {
		if (!this.stack.isEmpty()) {
			BigDecimal x = this.stack.peek();
			this.stack.push(x);
		}
	}
	
	// Swap top two elements
	public void swap() {
		if (this.stack.size() > 1) {
			BigDecimal x = this.stack.pop();
			BigDecimal y = this.stack.pop();
			this.stack.push(x);
			this.stack.push(y);
		}
	}
	
	// Add top two elements.
	public void add() {
		if (this.stack.size() > 1) {
			BigDecimal x = this.stack.pop();
			BigDecimal y = this.stack.pop();
			BigDecimal r = y.add(x);
			this.stack.push(r);
		}
	}
	
	// Add top two elements.
	public void subtract() {
		if (this.stack.size() > 1) {
			BigDecimal x = this.stack.pop();
			BigDecimal y = this.stack.pop();
			BigDecimal r = y.subtract(x);
			this.stack.push(r);
		}
	}
	
	// Add top two elements.
	public void multiply() {
		if (this.stack.size() > 1) {
			BigDecimal x = this.stack.pop();
			BigDecimal y = this.stack.pop();
			BigDecimal r = y.multiply(x);
			this.stack.push(r);
		}
	}
	
	// Raise to power.
	// Returns error message, or null if no error.
	public String power() {
		String result = null;
		if (this.stack.size() > 1) {
			BigDecimal y = this.stack.pop();
			BigDecimal x = this.stack.pop();
			int yi = y.intValueExact();
			BigDecimal r = x.pow(yi);
			this.stack.push(r);
		}
		return result;
	}
	
	// Divide top two elements.
	// Returns error message, or null if no error.
	public String divide() {
		String result = null;
		if (this.stack.size() > 1) {
			BigDecimal x = this.stack.pop();
			BigDecimal y = this.stack.pop();
			// We use HALF_EVEN rounding because this statistically minimizes cumulative error
			// during repeated calculations.
			try {
				BigDecimal r = y.divide(x, INTERNAL_SCALE,
						RoundingMode.HALF_EVEN);
				this.stack.push(r);
			} catch (ArithmeticException e) {
				result = e.getMessage();
	//			Log.i("divide", "Error: " + e.getMessage());
			}
		}
		return result;
	}
	
	// Reciprocal of top element
	public void reciprocal() {
		if (!this.stack.isEmpty()) {
			BigDecimal x = this.stack.peek();
			BigDecimal one = new BigDecimal(1);
			BigDecimal y = one.divide(x, INTERNAL_SCALE, RoundingMode.HALF_EVEN);
			this.stack.push(y);
		}
	}
	
	public void setScale(final int newscale) {
		this.scale = newscale;
	}
	
	public void setScale() {
		if (!this.stack.isEmpty()) {
			BigDecimal x = this.stack.pop();
			int sc = x.intValue();
			if (sc < INTERNAL_SCALE) {
				setScale(sc);
			}
		}
	}

	public int getScale() {
		return this.scale;
	}
	
}
