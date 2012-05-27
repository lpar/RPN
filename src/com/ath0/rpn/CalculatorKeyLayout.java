package com.ath0.rpn;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.Button;
import android.widget.GridLayout;

public class CalculatorKeyLayout extends GridLayout {

	private Context mycontext;
	
	public CalculatorKeyLayout(Context context) {
		super(context);
		this.mycontext = context;
	}
	
	public CalculatorKeyLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
		this.mycontext = context;
	}
	
	public CalculatorKeyLayout(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		this.mycontext = context;
	}

	@Override
	public void onSizeChanged(int w, int h, int oldw, int oldh) {
		if (w == 0 && h == 0) {
            return;
        }
		int kw = w / 5;
		int kh = kw;
/*		System.out.println("height = " + Integer.toString(h));
		System.out.println("width = " + Integer.toString(w));
		System.out.println("kh = " + Integer.toString(kh));
		System.out.println("kw = " + Integer.toString(kw)); */
		// Work out row padding before we resize all the buttons
		Button enter = (Button) findViewById(R.id.enter);
		Button del = (Button) findViewById(R.id.bsp);
		int padding = enter.getTop() - del.getTop();
	/*	System.out.println("Padding seems to be" + Integer.toString(padding)); */
		// Set some keys to custom font so they show up correctly
		AssetManager assets = this.mycontext.getAssets();
		Typeface rpnfont = Typeface.createFromAsset(assets, "fonts/RPN.TTF");
		Typeface roboto = Typeface.createFromAsset(assets, "fonts/Roboto-Light.ttf");
		// Resize everything except enter
		for(int i = 0; i < getChildCount(); i++) {
		    Button key = (Button) getChildAt(i);
		    int k = key.getId();
		    if (k == R.id.enter) {
		    	key.setHeight(kh * 2 + padding);
		    } else if (k != R.id.sqrt && k != R.id.power && k != R.id.swap && k != R.id.drop && k != R.id.recip) {
		    	key.setHeight(kh);
		    }
		    key.setWidth(kw);
		    // System.out.println("Set width and height for key #" + Integer.toString(i));
		    if (k == R.id.bsp || k == R.id.recip || k == R.id.power || k == R.id.sqrt) {
		    	key.setTypeface(rpnfont);
		    } else {
		    	key.setTypeface(roboto);
		    }
		}
		
		
	}

}
