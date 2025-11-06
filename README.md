Here’s a clearer and more professional rewrite of your text — concise, logically structured, and easy to follow while keeping all the technical details intact:

---

### Overview

This code is an **Android automation service** designed to automatically handle voucher entry tasks.

#### What It Does

1. Detects when you open the **Target App** app.
2. Identifies the on-screen fields for **“Gift Card Number”** and **“PIN.”**
3. Automatically fills in your saved voucher details.
4. Clicks the **“Apply”** button for you.
5. Repeats this process for every stored voucher, one by one.

---

### Adapting It for Another App

You can easily modify this service to work with other apps such as **Amazon**, **Flipkart**, etc.
To do so, you only need to update a few specific lines in the source file:

**File to Modify:** `AutoVoucherService.kt`

#### 1. Change the App to Monitor

```kotlin
if (!pkg.contains("cinepolis", ignoreCase = true)) return
```

**Update:** Replace `"cinepolis"` with the package name of the new app,
for example: `"com.amazon.android"`

---

#### 2. Update the Field and Button Identifiers

```kotlin
val idNode = findNodeByContentDesc(root, "flb_enter_16_digit_gift_card_number")
val pinNode = findNodeByContentDesc(root, "flb_enter_6_digit_pin")
val applyNode = findNodeByText(root, "apply")
```

**Update each line as follows:**

* Replace `"flb_enter_16_digit_gift_card_number"` with the new app’s **gift card input field ID**.
* Replace `"flb_enter_6_digit_pin"` with the **PIN field ID** used in the target app.
* Replace `"apply"` with the text displayed on the app’s **redeem or submit button** (e.g., `"Redeem"`, `"Add to Balance"`).

---

### Summary

By updating these **four strings**—the package name, gift card field ID, PIN field ID, and button text—you can redirect the automation service to work seamlessly with any other app.
