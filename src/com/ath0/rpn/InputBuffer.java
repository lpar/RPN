package com.ath0.rpn;

import java.io.Serializable;

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

	public InputBuffer(String value) {
		super();
		this.set(value);
	}
	
	public InputBuffer() {
		super();
	}
	
	/**
	 * Appends a given character to the buffer, if the result would be a valid 
   * real number. If '.' is appended to an empty buffer, a '0' is added first.
	 * @param c a digit or '.'
	 */
	public void append(char c) {
		switch (c) {
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
			this.buffer.append(c);
			break;
		}
	}
	
	/**
	 * Deletes the rightmost character in the buffer
	 */
	public void delete() {
		int x = this.buffer.length();
		if (x > 0) {
			this.buffer.setLength(x - 1);
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
		return this.buffer.length() == 0;
	}
	
	/**
	 * Sets the value of the buffer
	 * @param value the value, assumed to be a valid numeric
	 */
	public void set(String value) {
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
	
}
