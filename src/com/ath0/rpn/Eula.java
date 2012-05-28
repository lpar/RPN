/*
 * Copyright (C) 2008 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.ath0.rpn;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStreamReader;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.util.Log;

/**
 * Displays an EULA ("End User License Agreement") that the user has to accept 
 * before using the application. Your application should call 
 * {@link Eula#show(android.app.Activity)} in the onCreate() method of the 
 * first activity. If the user accepts the EULA, it will never be shown again. 
 * If the user refuses, {@link android.app.Activity#finish()} is invoked
 * on your activity.
 */
class Eula {
  private static final String ASSET_EULA = "EULA.txt";
  private static final String PREFERENCE_EULA_ACCEPTED = "eula.accepted";
  private static final String PREFERENCES_EULA = "eula";

  /**
   * callback to let the activity know when the user has accepted the EULA.
   */
  static interface OnEulaAgreedTo {

    /**
     * Called when the user has accepted the eula and the dialog closes.
     */
    void onEulaAgreedTo();
  }

  /**
   * Displays the EULA if necessary. This method should be called from the onCreate()
   * method of your main Activity.
   *
   * @param activity The Activity to finish if the user rejects the EULA.
   * @return Whether the user has agreed already.
   */
  public static boolean show(final Activity activity) {
    boolean result = true;
    final SharedPreferences preferences = activity.getSharedPreferences(PREFERENCES_EULA,
        Context.MODE_PRIVATE);
    if (!preferences.getBoolean(PREFERENCE_EULA_ACCEPTED, false)) {
      final AlertDialog.Builder builder = new AlertDialog.Builder(activity);
      builder.setTitle(R.string.eula_title);
      builder.setCancelable(true);
      builder.setPositiveButton(R.string.eula_accept, new DialogInterface.OnClickListener() {
        @Override
        @SuppressWarnings("synthetic-access")
        public void onClick(final DialogInterface dialog, final int which) {
          accept(preferences);
          if (activity instanceof OnEulaAgreedTo) {
            ((OnEulaAgreedTo) activity).onEulaAgreedTo();
          }
        }
      });
      builder.setNegativeButton(R.string.eula_refuse, new DialogInterface.OnClickListener() {
        @Override
        @SuppressWarnings("synthetic-access")
        public void onClick(final DialogInterface dialog, final int which) {
          refuse(activity);
        }
      });
      builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
        @Override
        @SuppressWarnings("synthetic-access")
        public void onCancel(final DialogInterface dialog) {
          refuse(activity);
        }
      });
      builder.setMessage(readEula(activity));
      builder.create().show();
      result = false;
    }
    return result;
  }

  private static void accept(final SharedPreferences preferences) {
    preferences.edit().putBoolean(PREFERENCE_EULA_ACCEPTED, true).commit();
  }

  private static void refuse(final Activity activity) {
    activity.finish();
  }

  private static CharSequence readEula(final Activity activity) {
    CharSequence result = "";
    BufferedReader txt = null;
    try {
      txt = new BufferedReader(new 
          InputStreamReader(activity.getAssets().open(ASSET_EULA)));
      String line;
      final StringBuilder buffer = new StringBuilder();
      while ((line = txt.readLine()) != null) { 
        buffer.append(line).append('\n'); 
      }
      result = buffer;
    } catch (IOException e) {
      // Do nothing
    } finally {
      closeStream(txt);
    }
    return result;
  }

  /**
   * Closes the specified stream.
   *
   * @param stream The stream to close.
   */
  private static void closeStream(final Closeable stream) {
    if (stream != null) {
      try {
        stream.close();
      } catch (IOException e) {
        Log.e("closeStream", e.getMessage());
      }
    }
  }
}
