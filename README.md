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
Improvements are welcome. A few ideas:

* Extra functions on the alternate keyboard, such as square root. This will
  require implementing suitable algorithms in decimal arithmetic.

* Tablet support via the fragment API. I don't have an Android tablet yet, but
  plan to get one some time in the medium future.

* Nicer buttons.

