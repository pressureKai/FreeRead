package com.kai.ui.bluetooth

import android.app.Activity
import android.bluetooth.BluetoothDevice
import android.content.Intent
import android.os.Looper
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import com.alibaba.android.arouter.facade.annotation.Route
import com.bumptech.glide.Glide
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.google.android.material.appbar.AppBarLayout
import com.kai.base.R
import com.kai.base.activity.BaseMvpActivity
import com.kai.base.mvp.base.BasePresenter
import com.kai.base.mvp.base.IView
import com.kai.common.extension.customToast
import com.kai.common.extension.getScreenHeight
import com.kai.common.utils.BlueToothUtils
import com.kai.common.utils.LogUtils
import com.kai.util.FileHelper
import com.kai.util.FileHelper.Companion.copy
import com.kai.util.PermissionHelper
import kotlinx.android.synthetic.main.activity_bluetooth.*
import kotlinx.android.synthetic.main.activity_bluetooth.back
import kotlinx.android.synthetic.main.activity_bluetooth.toolbar
import java.util.logging.Handler
import kotlin.math.abs

@Route(path = "/app/bluetooth")
class BluetoothActivity : BaseMvpActivity<IView, BasePresenter<IView>>() {
    private var bluetoothUtils: BlueToothUtils? = null
    private val bluetoothDevices: ArrayList<BluetoothDevice> = ArrayList()
    private val bluetoothConnectedDevices: ArrayList<BluetoothDevice> = ArrayList()
    override fun initView() {
        initImmersionBar(fitSystem = false, color = R.color.app_background, dark = true)
        bluetooth_animation.layoutParams.height = (getScreenHeight() / 5) * 2
        appBar.addOnOffsetChangedListener(AppBarLayout.OnOffsetChangedListener { _, verticalOffset ->

            val alpha = if (verticalOffset != 0) {
                abs(verticalOffset).toFloat() / (appBar.height - toolbar.height).toFloat()
            } else {
                0f
            }
            back_layout.alpha = alpha
            toolbar.alpha = alpha
        })
        back.setOnClickListener {
            finish()
        }


        connected_list.layoutManager = LinearLayoutManager(this)
        connected_list.adapter = BluetoothConnectedAdapter()

        scan_list.layoutManager = LinearLayoutManager(this)
        scan_list.adapter = BluetoothConnectedAdapter()


        loading_state.setOnClickListener {
            if (loading_state.text != "正在扫描") {
                refreshData()
            }
        }


        run.setOnClickListener {
            bluetoothUtils?.run()
        }

        connect.setOnClickListener {
            FileHelper.getAllSystemFile(this)
        }


        bluetoothUtils = BlueToothUtils(this)
        bluetoothUtils?.connectListener = object : BlueToothUtils.ConnectListener {
            override fun connectDeviceChange() {
                runOnUiThread {
                    (connected_list.adapter as BluetoothConnectedAdapter).notifyDataSetChanged()
                }
            }
        }
        refreshData()

        toolbar_title.text = resources.getString(R.string.bluetooth_transmission)
    }

    private fun refreshData() {
        connectBluetooth(object : BlueToothUtils.EnableBluetoothListener {
            override fun enableSuccess() {
                bluetoothConnectedDevices.clear()
                val queryingPairedDevices = bluetoothUtils!!.queryingPairedDevices()
                bluetoothConnectedDevices.addAll(queryingPairedDevices)
                (connected_list.adapter as BluetoothConnectedAdapter).setNewInstance(
                    bluetoothConnectedDevices
                )
                PermissionHelper.instance.requestPermission(
                    this@BluetoothActivity,
                    PermissionHelper.locationPermissions,
                    onCancelListener = {
                        customToast("权限不足")
                    }, onConfirmListener = {
                        bluetoothDevices.clear()
                        loading_state.text = "正在扫描"
                        loading_lottie.visibility = View.VISIBLE
                        bluetoothUtils!!.discoveringDevices(3000 * 4,
                            object : BlueToothUtils.DiscoveringDevicesListener {
                                override fun getDiscoveredDevice(device: BluetoothDevice?) {
                                    device?.let {
                                        if (it.name != null && it.address != null) {
                                            var isRepeat = false
                                            for (value in bluetoothDevices) {
                                                if (value.address == it.address) {
                                                    isRepeat = true
                                                    break
                                                }
                                            }
                                            if (!isRepeat) {
                                                bluetoothDevices.add(it)
                                            }
                                        }


                                        (scan_list.adapter as BluetoothConnectedAdapter).setNewInstance(
                                            bluetoothDevices
                                        )
                                        (scan_list.adapter as BluetoothConnectedAdapter).notifyDataSetChanged()
                                    }
                                }

                                override fun stopDiscoveredDevice() {
                                    runOnUiThread {
                                        loading_state.text = "扫描"
                                        loading_lottie.visibility = View.GONE
                                    }
                                }
                            })
                    })
            }
        })
    }


    private fun connectBluetooth(enableBluetoothListener: BlueToothUtils.EnableBluetoothListener) {
        val blueIsConnected = bluetoothUtils!!.blueIsConnected()
        if (!blueIsConnected) {
            bluetoothUtils!!.enableBluetooth(enableBluetoothListener)
        }
    }

    override fun setLayoutId(): Int {
        return R.layout.activity_bluetooth
    }


    override fun onDestroy() {
        bluetoothUtils!!.release()
        bluetoothUtils = null
        super.onDestroy()
    }

    inner class BluetoothConnectedAdapter :
        BaseQuickAdapter<BluetoothDevice, BaseViewHolder>(R.layout.item_bluetooth) {
        override fun convert(holder: BaseViewHolder, item: BluetoothDevice) {
            val name = holder.getView<TextView>(R.id.name)
            val state = holder.getView<TextView>(R.id.state)
            val layout = holder.getView<LinearLayout>(R.id.layout)
            name.text = item.name
            bluetoothUtils!!.getConnectInfo()?.let {
                if (it.size > 0) {
                    val iterator = it.iterator()
                    while (iterator.hasNext()) {
                        if (iterator.next().address == item.address) {
                            state.text = "已连接"
                            break
                        }
                    }
                } else {
                    state.text = ""
                }


            } ?: kotlin.run {
                state.text = ""
            }

            layout.setOnClickListener {
                if (state.text.toString().isEmpty()) {
                    bluetoothUtils?.let {
                        it.bleConnect(item)
                    }
                } else {
                    customToast("当前设备已连接")
                }


            }

        }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                FileHelper.requestFileCode -> {
                    data?.data?.let {
                        copy(
                            it,
                            this,
                            copySuccessListener = object : FileHelper.CopySuccessListener {
                                override fun copySuccess(path: String) {
                                    bluetoothUtils?.sendFile(path)
                                }
                            })
                    }
                }
            }
        }
    }
}