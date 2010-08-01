package com.ath0.rpn;

import android.app.Application;
import android.content.Context;
import android.text.ClipboardManager;
import android.util.AttributeSet;
import android.widget.EditText;

public class StackView extends EditText {

	public StackView(Context context) {
		super(context);
	}
	
    public StackView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
    }

	public StackView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public boolean onTextContextMenuItem(int id) {
    	if (id == android.R.id.copy) {
    		Context c = this.getContext();
    		ClipboardManager clipboard = (ClipboardManager) c.getSystemService(Context.CLIPBOARD_SERVICE);
    		String text = this.getText().toString();
    		int lastnl = text.lastIndexOf('\n');
    		String tocopy = text.substring(lastnl + 1);
    		clipboard.setText(tocopy);
    		return true;
    	}
    	return false;
    }

}
