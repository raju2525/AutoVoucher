package com.example.autovoucher

import android.accessibilityservice.AccessibilityService
import android.os.Bundle

import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import android.widget.Toast
import kotlinx.coroutines.*


class AutoVoucherService : AccessibilityService() {

    private var job: Job? = null
    private var processing = false
    private val appliedVouchers = mutableSetOf<String>()

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        val pkg = event?.packageName?.toString() ?: return
        if (!pkg.contains("cinepolis", ignoreCase = true)) return

        val enabled = Storage.loadServiceEnabled(applicationContext)
        if (!enabled) {
            if (processing) {
                stopProcessing()
            }
            return
        }

        if (processing) return

        startProcessing()
    }
    private fun formatCardNumber(input: String): String {
        val digitsOnly = input.filter { it.isDigit() }
        val sb = StringBuilder()
        for (i in digitsOnly.indices) {
            sb.append(digitsOnly[i])
            if ((i + 1) % 4 == 0 && i != digitsOnly.lastIndex) {
                sb.append(" ")
            }
        }
        return sb.toString()
    }

    private fun startProcessing() {
        processing = true
        job = CoroutineScope(Dispatchers.Default).launch {
            val ctx = applicationContext
            val vouchers = Storage.loadVouchers(ctx)
            val delaySeconds = Storage.loadDelay(ctx).coerceAtLeast(1)

            for (voucher in vouchers) {
                if (!isActive || !Storage.loadServiceEnabled(ctx)) break
                if (appliedVouchers.contains(voucher.id)) continue

                var filled = false
                var attemptCount = 0

                while (!filled && isActive && Storage.loadServiceEnabled(ctx)) {
                    attemptCount++
                    val root = rootInActiveWindow
                    if (root == null) {
                        delay(500)
                        continue
                    }

                    val idNode = findNodeByContentDesc(root, "flb_enter_16_digit_gift_card_number")
                    val pinNode = findNodeByContentDesc(root, "flb_enter_6_digit_pin")
                    val applyNode = findNodeByText(root, "apply")

                    if (idNode != null && pinNode != null && applyNode != null) {
                        // Format card number and pin
                        val formattedCard = formatCardNumber(voucher.id)
                        val pin = voucher.pin.filter { it.isDigit() } // no spaces

                        setText(idNode, formattedCard)
                        delay(150)
                        setText(pinNode, pin)
                        delay(150)

                        applyNode.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                        appliedVouchers.add(voucher.id)
                        filled = true

                        delay(delaySeconds * 1000L)
                    } else {
                        delay(500)
                    }

                    if (attemptCount > 50) break
                }
            }

            withContext(Dispatchers.Main) {
                if (!isActive || !Storage.loadServiceEnabled(ctx)) {
                    Toast.makeText(ctx, "Voucher processing stopped", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(ctx, "Voucher processing finished", Toast.LENGTH_SHORT).show()
                }
                processing = false
            }
        }
    }


    private fun findNodeByContentDesc(root: AccessibilityNodeInfo, desc: String): AccessibilityNodeInfo? {
        val list = ArrayList<AccessibilityNodeInfo>()
        collectNodes(root, list)
        return list.firstOrNull { it.contentDescription?.toString()?.equals(desc, ignoreCase = true) == true }
    }

    private fun collectNodes(node: AccessibilityNodeInfo?, list: MutableList<AccessibilityNodeInfo>) {
        if (node == null) return
        list.add(node)
        for (i in 0 until node.childCount) {
            collectNodes(node.getChild(i), list)
        }
    }

    private fun findNodeByText(root: AccessibilityNodeInfo, text: String): AccessibilityNodeInfo? {
        val nodes = root.findAccessibilityNodeInfosByText(text)
        return nodes?.firstOrNull()
    }

    private fun setText(node: AccessibilityNodeInfo, value: String) {
        val args = Bundle()
        args.putCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, value)
        node.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, args)
    }

    override fun onInterrupt() {
        stopProcessing()
    }

    override fun onUnbind(intent: android.content.Intent?): Boolean {
        stopProcessing()
        return super.onUnbind(intent)
    }

    private fun stopProcessing() {
        job?.cancel()
        processing = false
    }
}
