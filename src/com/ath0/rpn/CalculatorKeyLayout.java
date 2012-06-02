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

	private final Context mycontext;

	// We make all the constructors store the context, as we need it later on 
	// to load fonts.
	public CalculatorKeyLayout(final Context context) {
		super(context);
		this.mycontext = context;
	}

	public CalculatorKeyLayout(final Context context, final AttributeSet attrs) {
		super(context, attrs);
		this.mycontext = context;
	}

	public CalculatorKeyLayout(final Context context, final AttributeSet attrs, 
	    final int defStyle) {
		super(context, attrs, defStyle);
		this.mycontext = context;
	}

	/**
	 * Catches onSizeChanged events and uses the new size of the layout to 
	 * compute the key sizes.
	 */
	@Override
	public void onSizeChanged(final int neww, final int newh, final int oldw, final int oldh) {
		if (neww == 0 && newh == 0) {
			return;
		}
		Log.i("CalculatorKeyLayout", "RPN.onSizeChanged");
		final int keyw = neww / 5;
		final int keyh = keyw;
		Log.d("CalculatorKeyLayout", "height = " + Integer.toString(newh));
		Log.d("CalculatorKeyLayout", "width = " + Integer.toString(neww));
		Log.d("CalculatorKeyLayout", "kh = " + Integer.toString(keyh));
		Log.d("CalculatorKeyLayout", "kw = " + Integer.toString(keyw));
		// Work out row padding before we resize all the buttons
		final Button enter = (Button) findViewById(R.id.enter);
		final Button del = (Button) findViewById(R.id.bsp);
		final int padding = enter.getTop() - del.getTop();
		Log.d("CalculatorKeyLayout", "Padding seems to be " + 
		    Integer.toString(padding));
		// Set keys to custom fonts
		final AssetManager assets = this.mycontext.getAssets();
		// The RPN font is a subset created from Symbola, as found at
		// http://users.teilar.gr/~g1951d/
		// It's used to provide the Unicode characters required for the square 
		// root, reciprocal, delete and raise-to-power keys.
		final Typeface rpnfont = Typeface.createFromAsset(assets, "fonts/RPN.TTF");
		// Roboto, of course, is Google's new font for Android 4 apps.
		final Typeface roboto = 
		    Typeface.createFromAsset(assets, "fonts/Roboto-Light.ttf");
		// Now run through all the buttons, resizing them and applying the fonts.
		for(int i = 0; i < getChildCount(); i++) {
			final Button key = (Button) getChildAt(i);
			final int kid = key.getId();
			// Enter key is the classic double-height key.
			if (kid == R.id.enter) {
				key.setHeight(keyh * 2 + padding);
			} else if (kid != R.id.sqrt && kid != R.id.power && kid != R.id.swap && 
			    kid != R.id.drop && kid != R.id.recip) {
				key.setHeight(keyh);
			}
			key.setWidth(keyw);
			if (kid == R.id.bsp || kid == R.id.recip || kid == R.id.power || 
			    kid == R.id.sqrt) {
				key.setTypeface(rpnfont);
			} else {
				key.setTypeface(roboto);
			}
		}	
	}
}
