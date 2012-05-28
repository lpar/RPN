package com.ath0.rpn;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.Button;
import android.widget.GridLayout;

/**
 * Implements a calculator keyboard using GridLayout.
 * Sizes the calculator keys to be of equal width and fill the screen 
 * horizontally; then makes all but the top row of keys square. Also sets the 
 * keys to custom fonts.
 */
public class CalculatorKeyLayout extends GridLayout {

	private Context mycontext;

	// We make all the constructors store the context, as we need it later on 
	// to load fonts.
	public CalculatorKeyLayout(Context context) {
		super(context);
		this.mycontext = context;
	}

	public CalculatorKeyLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
		this.mycontext = context;
	}

	public CalculatorKeyLayout(Context context, AttributeSet attrs, 
	    int defStyle) {
		super(context, attrs, defStyle);
		this.mycontext = context;
	}

	/**
	 * Catches onSizeChanged events and uses the new size of the layout to 
	 * compute the key sizes.
	 */
	@Override
	public void onSizeChanged(int w, int h, int oldw, int oldh) {
		if (w == 0 && h == 0) {
			return;
		}
		Log.i("CalculatorKeyLayout", "RPN.onSizeChanged");
		int kw = w / 5;
		int kh = kw;
		Log.d("CalculatorKeyLayout", "height = " + Integer.toString(h));
		Log.d("CalculatorKeyLayout", "width = " + Integer.toString(w));
		Log.d("CalculatorKeyLayout", "kh = " + Integer.toString(kh));
		Log.d("CalculatorKeyLayout", "kw = " + Integer.toString(kw));
		// Work out row padding before we resize all the buttons
		Button enter = (Button) findViewById(R.id.enter);
		Button del = (Button) findViewById(R.id.bsp);
		int padding = enter.getTop() - del.getTop();
		Log.d("CalculatorKeyLayout", "Padding seems to be" + 
		    Integer.toString(padding));
		// Set keys to custom fonts
		AssetManager assets = this.mycontext.getAssets();
		// The RPN font is a subset created from Symbola, as found at
		// http://users.teilar.gr/~g1951d/
		// It's used to provide the Unicode characters required for the square 
		// root, reciprocal, delete and raise-to-power keys.
		Typeface rpnfont = Typeface.createFromAsset(assets, "fonts/RPN.TTF");
		// Roboto, of course, is Google's new font for Android 4 apps.
		Typeface roboto = Typeface.createFromAsset(assets, "fonts/Roboto-Light.ttf");
		// Now run through all the buttons, resizing them and applying the fonts.
		for(int i = 0; i < getChildCount(); i++) {
			Button key = (Button) getChildAt(i);
			int k = key.getId();
			// Enter key is the classic double-height key.
			if (k == R.id.enter) {
				key.setHeight(kh * 2 + padding);
			} else if (k != R.id.sqrt && k != R.id.power && k != R.id.swap && 
			    k != R.id.drop && k != R.id.recip) {
				key.setHeight(kh);
			}
			key.setWidth(kw);
			if (k == R.id.bsp || k == R.id.recip || k == R.id.power || 
			    k == R.id.sqrt) {
				key.setTypeface(rpnfont);
			} else {
				key.setTypeface(roboto);
			}
		}	
	}
}
