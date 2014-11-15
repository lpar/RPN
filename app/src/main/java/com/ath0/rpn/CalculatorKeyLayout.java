package com.ath0.rpn;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.HapticFeedbackConstants;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.GridLayout;

/**
 * Implements a calculator keyboard using GridLayout.
 * Sizes the calculator keys to be of equal width and fill the screen 
 * horizontally; then makes all but the top row of keys square. Also sets the 
 * keys to custom fonts.
 */
public class CalculatorKeyLayout extends GridLayout implements OnTouchListener {

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
	 * Catch the measurement of the keyboard, and use the width we are given to
	 * fix up the size of the buttons. This used to be in the onSizeChanged event,
	 * but that caused visual glitching after the speed improvements in
	 * Android 4.1 started to allow the phone to draw the unresized buttons before
	 * the resize code had a chance to run. 
	 */
	@Override
  protected void onMeasure (final int widthMeasureSpec, final int heightMeasureSpec) {
	  int width = MeasureSpec.getSize(widthMeasureSpec);
	  int height = MeasureSpec.getSize(heightMeasureSpec);
	  Log.d("onMeasure", "width = " + width);
	  Log.d("onMeasure", "height = " + height);
	  int mode = MeasureSpec.getMode(heightMeasureSpec);
	  if (mode == MeasureSpec.AT_MOST) {
	    Log.d("onMeasure", "mode = AT_MOST");
	  } else if (mode == MeasureSpec.EXACTLY) {
	    Log.d("onMeasure", "mode = EXACTLY");
	  } else {
	    Log.d("onMeasure", "mode = UNSPECIFIED");
	  }
	  resizeKeys(width);
	  super.onMeasure(widthMeasureSpec, heightMeasureSpec);
	}
	
	private void resizeKeys(final int keyboardWidth) {
	    final int keyw = keyboardWidth / 5;
	    final int keyh = keyw;
	    Log.d("resizeKeys", "width = " + Integer.toString(keyboardWidth));
	    Log.d("resizeKeys", "kh = " + Integer.toString(keyh));
	    Log.d("resizeKeys", "kw = " + Integer.toString(keyw));
	    // Work out row padding before we resize all the buttons
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
	      key.setOnTouchListener(this);
	      final int kid = key.getId();
	      // Enter key is the classic double-height key.
	      if (kid == R.id.enter) {
	        key.setHeight(keyh * 2);
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
	        // If the user sets font size to "Huge" in system preferences, the
	        // word "SWAP" becomes too wide to fit on a button; so we manually
	        // downsize the two top right buttons to size "Large" in that case.
	        if (kid == R.id.swap || kid == R.id.drop) {
	          float size = key.getTextSize();
	          if (size > 32.0) {
	            key.setTextSize(TypedValue.COMPLEX_UNIT_PX, 32.0f);
	          }
	        }
	      }
	    }
	}

  @Override
  public boolean onTouch(final View v, final MotionEvent event) {
    if (event.getActionMasked() == MotionEvent.ACTION_DOWN) {
      v.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY,
          HapticFeedbackConstants.FLAG_IGNORE_VIEW_SETTING);
    }
    return false;
  }
}
