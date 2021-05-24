package com.kai.util

import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.airbnb.lottie.LottieAnimationView
import com.kai.base.R
import com.kai.common.application.BaseApplication
import com.kai.common.extension.getScreenHeight
import com.kai.common.extension.getScreenWidth


class DialogHelper {
    private var mLoadingDialog: AlertDialog? = null
    private var mRemindDialog: AlertDialog? = null

    companion object {
        var instance: DialogHelper? = null
            get() {
                synchronized(DialogHelper::class.java) {
                    if (field == null) {
                        field = DialogHelper()
                    }
                }
                return field
            }
    }


    fun showLoadingDialog(remind: String = "", activity: AppCompatActivity) {
        var remindString = remind
        if (remindString.isEmpty()) {
            remindString = BaseApplication.getContext()!!.resources.getString(R.string.loading)
        }
        mLoadingDialog = initLoadingDialog(activity, remind)
        mLoadingDialog?.let {
            it.window?.let { window ->
                window.findViewById<TextView>(R.id.remind).text = remindString
            }
            if (!it.isShowing) {
                it.show()
            }
        }
    }


    fun showRemindDialog(
        activity: AppCompatActivity,
        remind: String?="",
        cancel: String ="",
        confirm: String?="",
        remindDialogClickListener: RemindDialogClickListener){

        var cancelString = cancel
        if (cancelString!!.isEmpty()) {
            cancelString = BaseApplication.getContext()!!.resources.getString(R.string.cancel)
        }
        var confirmString = confirm
        if (confirmString!!.isEmpty()) {
            confirmString = BaseApplication.getContext()!!.resources.getString(R.string.confirm)
        }

        mRemindDialog = initRemindDialog(activity,remind!!,confirmString,cancelString,remindDialogClickListener)
        mLoadingDialog?.let {
            if (!it.isShowing) {
                it.show()
            }
        }
    }



    fun hintRemindDialog(){
        mRemindDialog?.let {
            if (it.isShowing) {
                Handler(Looper.getMainLooper()).postDelayed({
                    it.hide()
                    mRemindDialog = null
                }, 10)
            }
        }
    }

    fun hintLoadingDialog() {
        mLoadingDialog?.let {
            if (it.isShowing) {
                Handler(Looper.getMainLooper()).postDelayed({
                    it.hide()
                    mLoadingDialog = null
                }, 500)
            }
        }
    }

    private fun initLoadingDialog(activity: AppCompatActivity, remind: String): AlertDialog? {
        val width = activity.getScreenWidth() / 2
        val height = activity.getScreenWidth() / 2
        val builder =
            AlertDialog.Builder(activity)
        val alertDialog = builder.create()
        alertDialog.setCanceledOnTouchOutside(false)
        val inflate = View.inflate(activity, R.layout.dialog_loading, null)
        val animationView = inflate.findViewById<LottieAnimationView>(R.id.loading_animation)
        val marginLayoutParams = animationView.layoutParams as ViewGroup.MarginLayoutParams
        marginLayoutParams.width = width / 2
        marginLayoutParams.height = width / 2
        marginLayoutParams.topMargin = width / 10
        animationView.layoutParams = marginLayoutParams
        inflate.findViewById<TextView>(R.id.remind).text = remind
        if (alertDialog.window != null) {
            alertDialog.window!!.setLayout(width, height)
        }
        alertDialog.show()
        alertDialog.setContentView(inflate)
        val window = alertDialog.window
        window?.setGravity(Gravity.CENTER)
        window?.let {
            val lp = window.attributes
            lp.width = width
            lp.height = height
            window.attributes = lp
        }

        return alertDialog
    }



    private fun initRemindDialog(activity:AppCompatActivity,
                                 remind:String,
                                 confirm:String,
                                 cancel:String,
                                 remindDialogClickListener: RemindDialogClickListener):AlertDialog{
        val width =  (activity.getScreenWidth() / 4 ) * 3
        val height = (width / 4 ) * 3
        val builder =
            AlertDialog.Builder(activity)
        val alertDialog = builder.create()
        alertDialog.setCanceledOnTouchOutside(false)
        val inflate = View.inflate(activity, R.layout.dialog_layout_remind, null)
        val content = inflate.findViewById<TextView>(R.id.content)
        val cancelTextView = inflate.findViewById<TextView>(R.id.cancel)
        val confirmTextView = inflate.findViewById<TextView>(R.id.confirm)


        content.text = remind
        cancelTextView.text = cancel
        confirmTextView.text = confirm

        cancelTextView.setOnClickListener {
            remindDialogClickListener.onRemindDialogClickListener(false)
        }

        confirmTextView.setOnClickListener {
            remindDialogClickListener.onRemindDialogClickListener(true)
        }
        if (alertDialog.window != null) {
            alertDialog.window!!.setLayout(width, height)
        }
        alertDialog.show()
        alertDialog.setContentView(inflate)
        val window = alertDialog.window
        window?.setGravity(Gravity.CENTER)
        window?.let {
            val lp = window.attributes
            lp.width = width
            lp.height = height
            window.attributes = lp
        }

        return alertDialog
    }


    public interface RemindDialogClickListener{
        fun onRemindDialogClickListener(positive:Boolean)
    }
}