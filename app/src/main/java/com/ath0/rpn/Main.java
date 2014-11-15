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
import android.view.ContextMenu;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnKeyListener;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Controller for RPN calculator.
 */
public class Main extends Activity implements OnKeyListener {

  private InputBuffer buffer;
  private CalculatorStack stack;
  private String error;
  private int screenlines;

  /**
   * Typical onCreate for an Android app. Shows an EULA, mostly for the
   * disclaimers on liability if the calculator should give an incorrect value.
   */
  @Override
  public void onCreate(final Bundle savedInstanceState) {
    Log.d("Main", "onCreate");
    super.onCreate(savedInstanceState);
    Eula.show(this);
    setContentView(R.layout.main);
    loadState();
  }

  /**
   * Accesses the FrameLayout containing the calculator display and the 
   * TextView within it, and calculates the number of lines of text that can 
   * be shown given the font size.
   * Catching onWindowFocusChanged is the best way to get access to the final
   * sizing of one or more widgets.
   */
  @Override
  public void onWindowFocusChanged(final boolean hasFocus) {
    super.onWindowFocusChanged(hasFocus);
    // At this point we are guaranteed to have been laid out on screen
    final TextView disp = (TextView) findViewById(R.id.Display);
    final FrameLayout hsv = (FrameLayout) findViewById(R.id.TopFrame);
    this.screenlines = 1 + Math.round((float) hsv.getHeight() / 
        (float) disp.getLineHeight());
    Log.d("onWindowFocusChanged", "Frame height = " + 
        Integer.toString(hsv.getHeight()));
    Log.d("onWindowFocusChanged", "Line height = " + 
        Integer.toString(disp.getLineHeight()));
    Log.d("onWindowFocusChanged", "Therefore number of lines = " + 
        Integer.toString(this.screenlines));
    // With that done, we can update the display.
    updateDisplay();
  }

  /**
   * Catches attempts to create an options menu, and inflates our custom
   * options menu.
   */
  @Override
  public boolean onCreateOptionsMenu(final Menu menu) {
    Log.d("onCreateOptionsMenu", "Options menu inflated");
    final MenuInflater inflater = getMenuInflater();
    inflater.inflate(R.menu.main, menu);
    // Set the display to have a context menu. This will cause our
    // onCreateContextMenu method to be called when the display is long pressed
    final TextView disp = (TextView) findViewById(R.id.Display);
    registerForContextMenu(disp);
    return true;
  }

  /**
   * Updates the N-level stack display on screen.
   */
  public void updateDisplay() {
    final TextView disp = (TextView) findViewById(R.id.Display);
    StringBuilder text;
    if (this.buffer.isEmpty() && this.error == null) {
      if (this.stack.isEmpty()) {
        // Display zero rather than a totally empty display
        text = new StringBuilder();
        for (int i = 1; i < this.screenlines - 1; i++) {
          text.append('\n');
        }
        text.append('0');
        final int scale = this.stack.getScale();
        if (scale > 0) {
          text.append('.');
          for (int i = 0; i < scale; i++) {
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
    disp.setLines(this.screenlines);
    disp.setText(text);
    scrollToRight();
  }

  /**
   * Pushes the edit buffer onto the stack, if it's not blank.
   * Called before any arithmetic operation.
   */
  public void implicitPush() {
    if (!this.buffer.isEmpty()) {
      final String num = this.buffer.get();
      this.stack.push(num);
      this.buffer.zap();
    }
  }

  /**
   * If the input buffer has some contents, deletes the rightmost character 
   * from the input buffer. Otherwise when the input buffer is empty, drops
   * the top element from the stack.
   */
  private void keyDelete() {
    if (this.buffer.isEmpty()) {
      this.stack.drop();
    } else {
      this.buffer.delete();	
    }
    this.updateDisplay();
  }

  /**
   * Handles enter key operations. If there's something in the input buffer,
   * that's pushed to the stack. Otherwise, the top item on the stack is
   * duplicated. (This perhaps unexpected behavior duplicates the observed 
   * behavior of HP Voyager series calculators.)
   */
  private void keyEnter() {
    if (this.buffer.isEmpty()) {
      this.stack.dup();
    } else {
      final String num = this.buffer.get();
      this.stack.push(num);
      this.buffer.zap();
    }
    this.updateDisplay();
  }

  /**
   * Handles keyboard operations which can be represented by simple single
   * character labels -- the digits, / + - * and .
   * @param c the char on the key; a digit, '/' '+' '-' '*' or '.'.
   * @return true if the keystroke was handled.
   */
  private boolean keyOther(final char c) {
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

  /**
   * Scrolls the number view over to the right. Generally called after any kind
   * of buffer update, so that the update is actually displayed. Uses a queued
   * thread for technical reasons.
   */
  private void scrollToRight() {
    // Enqueue the scrolling to happen after next layout
    ((HorizontalScrollView) findViewById(R.id.Scroll)).post(new Runnable() {
      @Override
      public void run() {
        ((HorizontalScrollView) findViewById(R.id.Scroll)).fullScroll(View.FOCUS_RIGHT);
      }
    });
  }

  /**
   * Handles all the on-screen buttons by examining their tag values from the
   * UI declaration XML. Passes operations on to the various keyFoo methods.
   * @param v the View representing the button pressed
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
      this.error = this.stack.sqrt();
      updateDisplay();
    } else if ("enter".equals(key)) {
      keyEnter();
    } else {
      final char c = key.charAt(0);
      keyOther(c);
    }
  }

  /**
   * Handles device keyboard input, in case the phone has a keyboard.
   */
  @Override
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

  /**
   * Catches app pause lifecycle events and saves state.
   */
  @Override
  public void onPause() {
    super.onPause();
    saveState();
  }

  /**
   * Saves state to internal device cache.
   */
  private void saveState() {
    final File dir = getCacheDir();
    final File data = new File(dir,"stack");
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
   * Loads state from internal device cache.
   */
  private void loadState() {
    final File dir = getCacheDir();
    final File data = new File(dir,"stack");
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

  /**
   * Reports an error to the user (via toast) as well as logging it.
   * @param thrower the method which encountered the error 
   * @param message the error message
   */
  private void reportError(final String thrower, final String message) {
    Context context = getApplicationContext();
    int duration = Toast.LENGTH_LONG;
    Toast toast = Toast.makeText(context, message, duration);
    toast.show();
    Log.e(thrower,message);
  }

  /**
   * Implements a clipboard copy operation. Copies the top stack item to the
   * clipboard.
   * @return
   */
  private boolean copy() {
    Context ctx = this.getBaseContext();
    ClipboardManager clipboard = 
        (ClipboardManager) ctx.getSystemService(Context.CLIPBOARD_SERVICE);
    final TextView t = (TextView) findViewById(R.id.Display);
    String text = t.getText().toString();
    int lastnl = text.lastIndexOf('\n');
    String tocopy = text.substring(lastnl + 1);
    Log.d("copy", "Putting " + tocopy + " on clipboard");
    ClipData clip = ClipData.newPlainText("RPN calculator value", tocopy);
    clipboard.setPrimaryClip(clip);
    return true;
  }

  /**
   * Implements a clipboard paste. If the clipboard contains some plain text,
   * it is treated as keyboard input.
   * @return
   */
  private boolean paste() {
    Context ctx = this.getBaseContext();
    ClipboardManager clipboard = 
        (ClipboardManager) ctx.getSystemService(Context.CLIPBOARD_SERVICE);
    ClipData.Item item = clipboard.getPrimaryClip().getItemAt(0);
    CharSequence text = item.getText();
    Log.d("paste", "Asked to paste " + text.toString());
    // Dispatch as keypresses to self
    for (int i = 0; i < text.length(); i++) {
      char c = text.charAt(i);
      keyOther(c);
    }
    return true;
  }
  
  /**
   * Prepares the context menu shown by the display. It behaves just like the
   * options menu.
   */
  @Override
  public void onCreateContextMenu(final ContextMenu menu, final View v, 
      final ContextMenu.ContextMenuInfo menuInfo) {
    // Context menu is created each time.
    // Inflate menu into it.
    final MenuInflater inflater = getMenuInflater();
    inflater.inflate(R.menu.main, menu);
    // Now prepare it like an options menu.
    onPrepareOptionsMenu(menu);
    return;
  }
  
  /**
   * Prepares the options menu. Checks if the clipboard has text on it, and
   * enables or disables the paste operation accordingly.
   */
  @Override
  public boolean onPrepareOptionsMenu(final Menu menu) {
    Context ctx = this.getBaseContext();
    MenuItem pasteitem = menu.findItem(android.R.id.paste);
    // Now check the clipboard and set the menu entry states
    ClipboardManager clipboard = 
        (ClipboardManager) ctx.getSystemService(Context.CLIPBOARD_SERVICE);
    if (clipboard.hasPrimaryClip()) {
      if (clipboard.getPrimaryClipDescription().hasMimeType(
          ClipDescription.MIMETYPE_TEXT_PLAIN)) {
        Log.d("setMenuStateForClipboard", "Clipboard is OK");
        pasteitem.setEnabled(true);
      } else {
        Log.d("setMenuStateForClipboard", "Clipboard has no plain text");
        pasteitem.setEnabled(false);
      }
    } else {
      Log.d("setMenuStateForClipboard", "Clipboard is empty");
      pasteitem.setEnabled(false);
    }
    return true;
  }

  /**
   * Handles an options menu selection, either copy or paste.
   */
  @Override
  public boolean onOptionsItemSelected(final MenuItem item) {
    boolean result;
    switch (item.getItemId()) {
    case android.R.id.copy:
      result = this.copy();
      break;
    case android.R.id.paste:
      result = this.paste();
      break;
    default:
      result = super.onOptionsItemSelected(item);
    }
    return result;
  }
  
  /**
   * Handle context menu selection just like options menu selection.
   */
  @Override
  public boolean onContextItemSelected (final MenuItem item) {
    return onOptionsItemSelected(item);
  }
}
