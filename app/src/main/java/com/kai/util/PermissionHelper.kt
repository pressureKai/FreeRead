package com.kai.util

import android.annotation.SuppressLint
import androidx.fragment.app.FragmentActivity
import com.tbruyelle.rxpermissions3.RxPermissions

/**
 * 权限管理工具类
 */
class PermissionHelper {
    companion object {
        val instance: PermissionHelper by lazy(mode = LazyThreadSafetyMode.SYNCHRONIZED) {
            PermissionHelper()
        }
        const val locationPermissions = android.Manifest.permission.ACCESS_FINE_LOCATION
        const val readStoragePermission = android.Manifest.permission.READ_EXTERNAL_STORAGE
        const val writeStoragePermission = android.Manifest.permission.WRITE_EXTERNAL_STORAGE
    }


    @SuppressLint("CheckResult")
    fun requestPermission(
        activity: FragmentActivity,
        permissions: String,
        onCancelListener: (() -> Unit)? = null,
        onConfirmListener: (() -> Unit)? = null
    ) {
        if (!activity.isDestroyed) {
            val rxPermissions = RxPermissions(activity)
            rxPermissions.requestEach(permissions).subscribe {
                when (it.granted) {
                    it.granted -> {
                        onConfirmListener?.invoke()
                    }
                    else -> {
                        onCancelListener?.invoke()
                    }
                }
            }
        }
    }

}