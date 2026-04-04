package com.example.hanaparal.ui.common

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.Window
import com.example.hanaparal.R

class LoadingDialog(context: Context) {
    private val dialog: Dialog = Dialog(context)

    init {
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        // You'll need to create a layout file for this: R.layout.dialog_loading
        // For now, I'll comment this out so it doesn't cause compilation errors until the layout is created
        // dialog.setContentView(R.layout.dialog_loading)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.setCancelable(false)
    }

    fun show() {
        if (!dialog.isShowing) {
            dialog.show()
        }
    }

    fun dismiss() {
        if (dialog.isShowing) {
            dialog.dismiss()
        }
    }
}
