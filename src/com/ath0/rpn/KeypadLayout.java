package com.ath0.rpn;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.Button;
import android.widget.RelativeLayout;

public class KeypadLayout extends RelativeLayout {

	public KeypadLayout(Context context) {
		super(context);
	}

	public KeypadLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public KeypadLayout(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	@Override
	protected void onMeasure (int widthMeasureSpec, int heightMeasureSpec) {
		// Get the size we are specified to fill
		int padwidth = MeasureSpec.getSize(widthMeasureSpec);
		int padheight = MeasureSpec.getSize(heightMeasureSpec);
		this.setMeasuredDimension(padwidth, padheight);
		// Work out the size of a key
		int keywidth = padwidth / 5;
		int keyheight = padheight / 4;
		// Run through all the child keys and set their size by adjusting their LayoutParams.
		// Equivalent to adjusting layout_height and layout_width in their XML, but at runtime.
		int count = this.getChildCount();
        for (int i=0; i<count; i++) {
           Button x = (Button) this.getChildAt(i);
           RelativeLayout.LayoutParams xp = (RelativeLayout.LayoutParams) x.getLayoutParams();
           xp.width = keywidth;
           if (x.getId() == R.id.enter) {
        	   xp.height = 2*keyheight;
           } else {
        	   xp.height = keyheight;
           }
           x.setLayoutParams(xp);
        }  
	//	Log.i("KeypadLayout", "Key size during onMeasure is " + Integer.toString(keywidth) + " x " + Integer.toString(keyheight));
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
	}
}
