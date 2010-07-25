package com.ath0.rpn;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.HapticFeedbackConstants;
import android.view.KeyEvent;
import android.view.SoundEffectConstants;
import android.view.View;
import android.view.View.OnKeyListener;
import android.widget.HorizontalScrollView;
import android.widget.TextView;
import android.widget.Toast;

public class Main extends Activity implements OnKeyListener {
	
	private InputBuffer buffer;
	private CalculatorStack stack;
	private String error;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Eula.show(this);
        setContentView(R.layout.main);
    	Display display = getWindowManager().getDefaultDisplay(); 
    	int width = display.getWidth();
    	Log.i("onCreate",Integer.toString(width));
        final TextView t = (TextView) findViewById(R.id.Display);
        t.setMinWidth(width);
        t.setFocusable(true);
        t.setFocusableInTouchMode(true);
        t.requestFocus();
        t.setOnKeyListener(this);
        loadState();
        updateDisplay();
    }
    
    /**
     * Update the 4-level stack.
     */
    public void updateDisplay() {
    	StringBuilder text;
    	if (this.buffer.isEmpty() && this.error == null) {
    		if (this.stack.isEmpty()) {
    			// Display zero rather than a totally empty display
    			text = new StringBuilder("\n\n\n0");
    		} else {
    			text = this.stack.toString(4);
    		}
    	} else {
    		text = this.stack.toString(3);
    		text.append("\n");
    		if (this.error == null) {
    			text.append(this.buffer.get());
    		} else {
    			text.append(this.error);
    			this.error = null;
    		}
    	}
    	final TextView t = (TextView) findViewById(R.id.Display);
    	t.setText(text);
    	scrollToRight();
    }
    
    /**
     * Push edit buffer onto stack, if it's not blank.
     * Called before any arithmetic operation.
     */
    public void implicitPush() {
    	if (!this.buffer.isEmpty()) {
    		String x = this.buffer.get();
    		this.stack.push(x);
    		this.buffer.zap();
    	}
    }
     
    private void keyDelete() {
    	this.buffer.delete();
		this.updateDisplay();
    }
    
    private void keyEnter() {
    	if (!this.buffer.isEmpty()) {
			String x = this.buffer.get();
			this.stack.push(x);
			this.buffer.zap();
		} else {
			this.stack.dup();
		}
		this.updateDisplay();
    }
    
    private void keyOther(char c) {
    	switch (c) {
		case '+':
			implicitPush();
			this.stack.add();
			this.updateDisplay();
			break;
		case '-':
			implicitPush();
			this.stack.subtract();
			this.updateDisplay();
			break;
		case '*':
			implicitPush();
			this.stack.multiply();
			this.updateDisplay();
			break;
		case '/':
			implicitPush();
			this.error = this.stack.divide();
			this.updateDisplay();
			break;
		default:
			if ((c >= '0' && c <= '9') || c == '.') {
				this.buffer.append(c);
				this.updateDisplay();
			}
			break;
		}
    }
    
    private void scrollToRight() {
    	// Enqueue the scrolling to happen after next layout
        ((HorizontalScrollView) findViewById(R.id.Scroll)).post(new Runnable() {
            public void run() {
                ((HorizontalScrollView) findViewById(R.id.Scroll)).fullScroll(View.FOCUS_RIGHT);
            }

        });
    }
    
    /**
     * Click handler for on-screen buttons.
     * @param v
     */
    public void clickHandler(final View v) {
    	final String key = (String) v.getTag();
    	if ("sdp".equals(key)) {
    		implicitPush();
    		this.stack.setScale();
    		updateDisplay();
    	} else if ("bsp".equals(key)) {
    		keyDelete();
    	} else if ("chs".equals(key)) {
    		implicitPush();
    		this.stack.chs();
    		updateDisplay();
    	} else if ("enter".equals(key)) {
    		keyEnter();
    	} else {
    		final char c = key.charAt(0);
    		keyOther(c);
    	}
    	v.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS,HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING);
    	v.playSoundEffect(SoundEffectConstants.CLICK);
    }

    /**
     * Key handler for keyboard input.
     */
    public boolean onKey(final View v, final int code, final KeyEvent event) {
    	boolean result = false;
    	handler: {
    		if (event.getAction() == KeyEvent.ACTION_DOWN) {
    			// First, check for delete and enter
    			if (code == KeyEvent.KEYCODE_DEL) {
    				keyDelete();
    				result = true;
    				break handler;
    			}
    			if (code == KeyEvent.KEYCODE_ENTER) {
    				keyEnter();
    				result = true;
    				break handler;
    			}
    			// OK, must be a number or some other operation
    			final char c = (char) event.getUnicodeChar();
    			keyOther(c);
    			result = true;
    			break handler;
    		}
    	}
    	return result;
    }
    
    @Override
    public void onPause() {
    	super.onPause();
    	saveState();
    }
    
    /**
     * Save state to internal cache.
     */
    private void saveState() {
    	File dir = getCacheDir();
    	File data = new File(dir,"stack");
    	FileOutputStream fos = null;
    	ObjectOutputStream out = null;
    	try {
    		fos = new FileOutputStream(data);
    		out = new ObjectOutputStream(fos);
    		out.writeObject(this.stack);
    		out.writeObject(this.buffer);
    		out.close();
    	} catch (IOException ex) {
    		reportError("saveState","Unable to save stack: " + ex.getMessage());
    	}
    }
    
    /**
     * Load state from internal cache.
     */
    private void loadState() {
    	File dir = getCacheDir();
    	File data = new File(dir,"stack");
    	FileInputStream fis = null;
    	ObjectInputStream in = null;
    	try {
    		fis = new FileInputStream(data);
    		in = new ObjectInputStream(fis);
    		this.stack = (CalculatorStack) in.readObject();
    		this.buffer = (InputBuffer) in.readObject();
    		in.close();
    	} catch (FileNotFoundException ex) {
    		Log.i("loadState","No state file found, instantiating empty state"); 
    		this.buffer = new InputBuffer();
    		this.stack = new CalculatorStack();
    	} catch (IOException ex) {
    		reportError("loadState","Unable to load stack: " + ex.getMessage());
    	} catch (ClassNotFoundException ex) {
    		reportError("loadState","Unable to load stack: " + ex.getMessage());
    	}
    	if (this.buffer == null) {
      		this.buffer = new InputBuffer();
    	}
    	if (this.stack == null) {
       		this.stack = new CalculatorStack();
    	}
    }
    
    private void reportError(String thrower, String message) {
    	Context context = getApplicationContext();
    	int duration = Toast.LENGTH_LONG;
    	Toast toast = Toast.makeText(context, message, duration);
    	toast.show();
    	Log.e(thrower,message);
    }
}