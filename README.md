# RPN

RPN is a slightly unusual four function calculator for Android.
It’s designed to replace the standard built-in calculator, and
features large buttons designed for easy touchscreen use.

It has three unusual features:

* It uses Reverse Polish Notation, as commonly found on HP calculators.
* It uses decimal arithmetic.
* It uses fixed point.
* It does not offer scientific functions, unit conversions, base
conversions, programmability, memory registers, or any other advanced
functionality, because I don’t need those things when I’m away from
my desk, and I wrote the program to “scratch my itch”. If you need
that stuff, there are at least half a dozen scientific calculators
available for free in the Android Market, including an HP48 emulator.

If you just want to install the calculator, you can do that easily; see 
[RPN on the Android Market](market://search?q=pname:com.ath0.rpn).

If you want to read more about why I wrote this calculator, and why it's the
way it is, [my web site has details](http://meta.ath0.com/software/rpn/).

Source code licensed under the GNU Public License, Version 3 or later.
Improvements are welcome.

# New in version 2.0

* Now requires Android 4 -- use 1.3.1 if you are stuck with an earlier OS version.
* Completely reworked Android 4 UI, with Roboto font.
* Now requires at least 320x480 resolution. As far as I know there aren't any Android 4 devices with a lower resolution than that; in fact, the SDK Android 4 image copes badly with a display that small.
* Now runs in portrait only. I found that orientation changes were more annoying than anything else, and there just isn't enough space for 4 rows of usable keys, plus an action bar, plus a number display with at least two lines.
* New square root button, as per user request.
* Single keypad (no shift), a benefit of going portrait mode only.
* Improved key labels for special functions.
* High resolution icon.
* Significant code cleanup thanks to Android 4 APIs.
