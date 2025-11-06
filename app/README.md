AutoVoucher Android project
==========================

What this is
-------------
Kotlin Android app that uses AccessibilityService to auto-fill gift voucher card & PIN fields in a target app (example: Cinepolis).
The app stores vouchers and a configurable delay. The Accessibility service reads stored vouchers and acts only when the 'Start' button is enabled in the app.

Important steps to use
----------------------
1. Import this project into Android Studio (File > Open).
2. Build & install on your device (minSdk 21).
3. Open the app, add voucher(s) (Card number and PIN).
4. Set delay using the slider.
5. Tap 'Open Accessibility Settings' and enable "AutoVoucher" service.
6. Press 'Start' in the app to allow the service to act.
7. Open the target app (Cinepolis) and go to Gift Card screen. The service will detect the target package and try to fill fields.

Tuning to target app
--------------------
To make the service find fields reliably you must inspect the target app:
- On your PC, use `adb`:
  1) Connect device via USB with USB debugging enabled.
  2) Run: adb shell uiautomator dump /sdcard/window_dump.xml
  3) Run: adb pull /sdcard/window_dump.xml
  4) Open the file and search for resource-id or hint text of the card & pin fields and the apply button.
- Update AutoVoucherService.kt: replace "Card Number", "PIN", "Apply" and/or view IDs like 'com.cinepolis:id/gv_card_number' with the actual values.

Troubleshooting
---------------
- If ACTION_SET_TEXT fails, try focusing the node and pasting via clipboard (requires extra code).
- Some OEMs restrict Accessibility actions. Test and iterate.
- Do not use this to violate any app's terms of service.

Legal & ethical
---------------
Use for personal automation only. Do not attempt to bypass security or automate abusive behaviour.
