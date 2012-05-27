package com.ath0.rpn;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipDescription;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.SoundEffectConstants;
import android.view.View;
import android.view.View.OnKeyListener;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.TextView;
import android.widget.Toast;

public class Main extends Activity implements OnKeyListener {
	
	private InputBuffer buffer;
	private CalculatorStack stack;
	private String error;
	private Menu optionsmenu;
	private int screenlines;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Eula.show(this);
        setContentView(R.layout.main);
        // Request that the main view be laid out
  //  	Log.i("onCreate",Integer.toString(width));
   /*     final TextView t = (TextView) findViewById(R.id.Display);
    	Display display = getWindowManager().getDefaultDisplay(); 
    	int width = display.getWidth();
        t.setMinWidth(width);
        t.setFocusable(true);
        t.setFocusableInTouchMode(true);
        t.requestFocus();
        t.setOnKeyListener(this); */
        loadState();
    }
    
    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
    	super.onWindowFocusChanged(hasFocus);
        // At this point we are guaranteed to have been laid out on screen
    	final TextView t = (TextView) findViewById(R.id.Display);
    	final FrameLayout hsv = (FrameLayout) findViewById(R.id.TopFrame);
    	this.screenlines = 1 + Math.round((float) hsv.getHeight() / (float) t.getLineHeight());
    	System.out.println("Frame height = " + Integer.toString(hsv.getHeight()));
    	System.out.println("Line height = " + Integer.toString(t.getLineHeight()));
    	System.out.println("Lines = " + Integer.toString(this.screenlines));
    	updateDisplay();
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
    	this.optionsmenu = menu;
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);
        return true;
    }
       
    /**
     * Update the N-level stack.
     */
    public void updateDisplay() {
    	final TextView t = (TextView) findViewById(R.id.Display);
    	StringBuilder text;
    	if (this.buffer.isEmpty() && this.error == null) {
    		if (this.stack.isEmpty()) {
    			// Display zero rather than a totally empty display
    			text = new StringBuilder();
    			for (int i = 1; i < this.screenlines - 1; i++) {
    				text.append('\n');
    			}
    			text.append('0');
    			int sc = this.stack.getScale();
    			if (sc > 0) {
    				text.append('.');
    				for (int i = 0; i < sc; i++) {
    					text.append('0');
    				}
    			}
    		} else {
    			text = this.stack.toString(this.screenlines);
    		}
    	} else {
    		text = this.stack.toString(this.screenlines - 1);
    		text.append("\n");
    		if (this.error == null) {
    			text.append(this.buffer.get());
    		} else {
    			text.append(this.error);
    			this.error = null;
    		}
    	}
    	t.setLines(this.screenlines);
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
    	if (this.buffer.isEmpty()) {
    		this.stack.drop();
    	} else {
    		this.buffer.delete();	
    	}
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
    
    private boolean keyOther(char c) {
    	boolean handled = false;
    	switch (c) {
		case '+':
			implicitPush();
			this.stack.add();
			this.updateDisplay();
			handled = true;
			break;
		case '-':
			implicitPush();
			this.stack.subtract();
			this.updateDisplay();
			handled = true;
			break;
		case '*':
			implicitPush();
			this.stack.multiply();
			this.updateDisplay();
			handled = true;
			break;
		case '/':
			implicitPush();
			this.error = this.stack.divide();
			this.updateDisplay();
			handled = true;
			break;
		default:
			if ((c >= '0' && c <= '9') || c == '.') {
				this.buffer.append(c);
				this.updateDisplay();
				handled = true;
			}
		}
    	return handled;
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
    	} else if ("drop".equals(key)) {
    		implicitPush();
    		this.stack.drop();
    		updateDisplay();
    	} else if ("swap".equals(key)) {
    		implicitPush();
    		this.stack.swap();
    		updateDisplay();
    	} else if ("pow".equals(key)) {
    		implicitPush();
    		this.error = this.stack.power();
    		updateDisplay();
    	} else if ("1/x".equals(key)) {
    		implicitPush();
    		this.error = this.stack.reciprocal();
    		updateDisplay();
    	} else if ("bsp".equals(key)) {
    		keyDelete();
    	} else if ("chs".equals(key)) {
    		implicitPush();
    		this.stack.chs();
    		updateDisplay();
    	} else if ("sqrt".equals(key)) {
    		implicitPush();
    		this.stack.sqrt();
    		updateDisplay();
    	} else if ("enter".equals(key)) {
    		keyEnter();
    	} else {
    		final char c = key.charAt(0);
    		keyOther(c);
    	}
    //	v.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP,HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING);
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
    			result = keyOther(c);
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
    	//	Log.i("loadState","No state file found, instantiating empty state"); 
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
    
    private boolean copy() {
    	Context c = this.getBaseContext();
		ClipboardManager clipboard = (ClipboardManager) c.getSystemService(Context.CLIPBOARD_SERVICE);
		final TextView t = (TextView) findViewById(R.id.Display);
		String text = t.getText().toString();
		int lastnl = text.lastIndexOf('\n');
		String tocopy = text.substring(lastnl + 1);
		System.out.println("Putting " + tocopy + " on clipboard");
		ClipData clip = ClipData.newPlainText("RPN calculator value", tocopy);
		clipboard.setPrimaryClip(clip);
		return true;
    }
    
    private boolean paste() {
    	Context ctx = this.getBaseContext();
    	ClipboardManager clipboard = (ClipboardManager) ctx.getSystemService(Context.CLIPBOARD_SERVICE);
    	ClipData.Item item = clipboard.getPrimaryClip().getItemAt(0);
    	CharSequence text = item.getText();
    	System.out.println("Asked to paste " + text.toString());
    	// Dispatch as keypresses to self
    	for (int i = 0; i < text.length(); i++) {
    		char c = text.charAt(i);
    		keyOther(c);
    	}
    	return true;
    }
    
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
    	System.out.println("Preparing options menu");
    	Context c = this.getBaseContext();
    	ClipboardManager clipboard = (ClipboardManager) c.getSystemService(Context.CLIPBOARD_SERVICE);
    	MenuItem pasteitem = this.optionsmenu.findItem(android.R.id.paste);
    	if (!(clipboard.hasPrimaryClip())) {
    		System.out.println("Clipboard is empty");
    		pasteitem.setEnabled(false);
    	} else if (!(clipboard.getPrimaryClipDescription().hasMimeType(ClipDescription.MIMETYPE_TEXT_PLAIN))) {
    		System.out.println("Clipboard has no plain text");
    		pasteitem.setEnabled(false);
    	} else {
    		System.out.println("Clipboard is OK");
    		pasteitem.setEnabled(true);
    	}
    	return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
    	System.out.println("Menu item selected!");
    	switch (item.getItemId()) {
    	case android.R.id.copy:
    		return this.copy();
    	case android.R.id.paste:
    		return this.paste();
    	default:
    		return super.onOptionsItemSelected(item);
    	}
    }
}