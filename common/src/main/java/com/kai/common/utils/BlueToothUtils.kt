package com.kai.common.utils

import android.bluetooth.*
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Build
import androidx.annotation.RequiresApi
import java.io.*
import java.lang.reflect.InvocationTargetException
import java.lang.reflect.Method
import java.util.*
import kotlin.collections.ArrayList


/**
 * Created by leo on 2018/3/21.
 * email:fanrunqi@qq.com
 */
@RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
class BlueToothUtils(var ctx: Context) {
    val TAG = "BluetoothUtil"

    /**
     * step1----------------------------------------------------------------------------------------
     * Get the BluetoothAdapter
     */
    private var mEBTReceiverRegisterFlag: Boolean? = null
    private var devices: ArrayList<BluetoothDevice> = ArrayList()
    var ebtListener: EnableBluetoothListener? = null

    /**
     * 写入字符串,不限长度
     */
    var info: String? = null
    var writeDataList: MutableList<ByteArray> = ArrayList()

    /**
     * 读写成功回调
     */
    var dataListener: InteractiveDataListener? = null
    var groupId = 0

    var connectListener: ConnectListener? = null


    public interface ConnectListener {
        fun connectDeviceChange()
    }

    private val mEBTReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action === BluetoothAdapter.ACTION_STATE_CHANGED) {
                when (intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, 0)) {
                    BluetoothAdapter.STATE_TURNING_ON -> LogUtils.e(TAG, "STATE_TURNING_ON")
                    BluetoothAdapter.STATE_ON -> {
                        LogUtils.e(TAG, "STATE_ON")
                        ebtListener!!.enableSuccess()
                    }
                    BluetoothAdapter.STATE_TURNING_OFF -> LogUtils.e(TAG, "STATE_TURNING_OFF")
                    BluetoothAdapter.STATE_OFF -> LogUtils.e(TAG, "STATE_OFF")
                }
            }
        }
    }


    private val bluetoothAdapter: BluetoothAdapter?
        get() {
            if (mBluetoothAdapter == null) {
                mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
            }
            return mBluetoothAdapter
        }


    private val mDDReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (BluetoothDevice.ACTION_FOUND == intent.action) {
                val device =
                        intent.getParcelableExtra<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE)
                ddListener!!.getDiscoveredDevice(device)
            }
        }
    }
    var ddListener: DiscoveringDevicesListener? = null


    private val mEDReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action === BluetoothAdapter.ACTION_SCAN_MODE_CHANGED) {
                when (intent.getIntExtra(BluetoothAdapter.EXTRA_SCAN_MODE, 0)) {
                    BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE -> LogUtils.e(
                            TAG,
                            "SCAN_MODE_CONNECTABLE_DISCOVERABLE"
                    )
                    BluetoothAdapter.SCAN_MODE_CONNECTABLE -> LogUtils.e(
                            TAG,
                            "SCAN_MODE_CONNECTABLE"
                    )
                    BluetoothAdapter.SCAN_MODE_NONE -> LogUtils.e(TAG, "SCAN_MODE_NONE")
                }
            }
        }
    }
    var bluetoothGattCharacteristicUuid: String? = null


    //判断蓝牙是否打开
    fun blueIsEnabled(): Boolean {
        return mBluetoothAdapter!!.isEnabled
    }

    /**---------------------------------------------------------------------------------------------
     * step2  打开蓝牙
     */
    fun enableBluetooth(listener: EnableBluetoothListener) {
        //判断是否支持蓝牙4.0
        if (!ctx.packageManager.hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            return
        }
        ebtListener = listener
        if (!mBluetoothAdapter!!.isEnabled.also { mEBTReceiverRegisterFlag = it }) {
            //打开蓝牙
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            ctx.startActivity(enableBtIntent)
            //蓝牙状态广播
            val filter = IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED)
            ctx.registerReceiver(mEBTReceiver, filter)
        } else {
            listener.enableSuccess()
        }
    }


    interface EnableBluetoothListener {
        fun enableSuccess()
    }

    /**---------------------------------------------------------------------------------------------
     * step3
     * @return 获取已经配对设备
     */
    fun queryingPairedDevices(): Set<BluetoothDevice> {
        return mBluetoothAdapter!!.bondedDevices
    }

    /**---------------------------------------------------------------------------------------------
     * step4 获取发现的设备
     * @param listener 回调接口
     * @param scanTime  扫描时间
     */
    fun discoveringDevices(scanTime: Long, listener: DiscoveringDevicesListener?) {
        val isStartSuccess = mBluetoothAdapter?.startDiscovery()

        isStartSuccess?.let {
            if (!it) {
                ddListener?.stopDiscoveredDevice()
                return
            }
            val filter = IntentFilter(BluetoothDevice.ACTION_FOUND)
            ctx.registerReceiver(mDDReceiver, filter)
            ddListener = listener
            Timer().schedule(object : TimerTask() {
                override fun run() {
                    mBluetoothAdapter!!.cancelDiscovery()
                    ddListener?.stopDiscoveredDevice()
                }
            }, scanTime)
        }

    }


    interface DiscoveringDevicesListener {
        fun getDiscoveredDevice(device: BluetoothDevice?)
        fun stopDiscoveredDevice()
    }

    /**---------------------------------------------------------------------------------------------
     * 使蓝牙变为可发现状态
     */
    fun enablingDiscoverability(duration: Int) {
        val discoverableIntent = Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE)
        discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, duration)
        ctx.startActivity(discoverableIntent)
        //蓝牙可发现状态广播
        val filter = IntentFilter(BluetoothAdapter.ACTION_SCAN_MODE_CHANGED)
        ctx.registerReceiver(mEDReceiver, filter)
    }


    fun bleConnect(bd: BluetoothDevice?, bluetoothGattCharacteristicUuid: String? = "") {
        if (null == bd) {
            return
        }
        if (createBond(bd)) {
            if (mBluetoothGatt != null) {
                mBluetoothGatt!!.disconnect()
                mBluetoothGatt!!.close()
            }
            this.bluetoothGattCharacteristicUuid = bluetoothGattCharacteristicUuid
            mBluetoothAdapter!!.cancelDiscovery()
            ddListener?.stopDiscoveredDevice()
            try {
                val remoteDevice = mBluetoothAdapter!!.getRemoteDevice(bd.address)
                mBluetoothGatt = remoteDevice.connectGatt(ctx, false, mGattCallback)
                clearCache()
            } catch (e: Exception) {
                LogUtils.e(TAG, "connect gatt  error is $e")
            }
        }
    }

    private fun removeDevice(bluetoothDevice: BluetoothDevice) {
        var beRemoveIndex = -1
        for ((index, value) in devices.withIndex()) {
            if (value.address == bluetoothDevice.address) {
                beRemoveIndex = index
            }
        }
        if (beRemoveIndex != -1) {
            devices.removeAt(beRemoveIndex)
            tempBluetoothGattCharacteristic = null
            connectListener?.connectDeviceChange()
        }

    }

    private val mGattCallback: BluetoothGattCallback = object : BluetoothGattCallback() {
        //连接状态改变的回调
        override fun onConnectionStateChange(
                gatt: BluetoothGatt, status: Int,
                newState: Int
        ) {

            when (newState) {
                BluetoothProfile.STATE_CONNECTED -> {
                    // 连接成功后启动服务发现
                    LogUtils.e(TAG, "搜索服务")
                    mBluetoothGatt!!.discoverServices()
                }
                BluetoothProfile.STATE_CONNECTING -> {
                    // 连接中

                }
                BluetoothProfile.STATE_DISCONNECTED -> {
                    // 断开
                    removeDevice(gatt.device)
                }
                BluetoothProfile.STATE_DISCONNECTING -> {
                    // 断开中
                }
            }
        }

        //发现服务的回调
        override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                val serviceList = mBluetoothGatt!!.services
                for (bs in serviceList) {
                    for (ch in bs.characteristics) {
                        if (bluetoothGattCharacteristicUuid!!.isNotEmpty()) {
                            if (ch.uuid.toString() == bluetoothGattCharacteristicUuid) {
                                tempBluetoothGattCharacteristic = ch
                                LogUtils.e(TAG, "连接成功")
                                devices.add(gatt.device)
                                connectListener?.connectDeviceChange()
                            }
                        } else {
                            if (tempBluetoothGattCharacteristic == null) {
                                tempBluetoothGattCharacteristic = ch
                                LogUtils.e(TAG, "连接成功")
                                devices.add(gatt.device)
                                connectListener?.connectDeviceChange()
                            }
                        }

                    }
                }
            } else {
                LogUtils.e(TAG, "服务发现失败，错误码为:$status")
            }
        }

        //写操作的回调
        override fun onCharacteristicWrite(
                gatt: BluetoothGatt,
                characteristic: BluetoothGattCharacteristic,
                status: Int
        ) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                LogUtils.e(TAG, "写入成功" + characteristic.value)
                groupWrite()
            }
        }

        //读操作的回调
        override fun onCharacteristicRead(
                gatt: BluetoothGatt,
                characteristic: BluetoothGattCharacteristic,
                status: Int
        ) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                LogUtils.e(TAG, "读取成功" + characteristic.value)
                dataListener!!.readSuccess(characteristic.value)
            }
        }

        //数据返回的回调（此处接收BLE设备返回数据）
        override fun onCharacteristicChanged(
                gatt: BluetoothGatt,
                characteristic: BluetoothGattCharacteristic
        ) {
            LogUtils.e(TAG, "设备返回数据" + characteristic.value)
            dataListener!!.replyData(characteristic.value)
        }
    }


    interface InteractiveDataListener {
        fun writeSuccess(data: String?)
        fun readSuccess(data: ByteArray?)
        fun replyData(data: ByteArray?)
    }

    fun AddInteractiveDataListener(dataListener: InteractiveDataListener?) {
        this.dataListener = dataListener
    }


    fun blueStringWrite(info: String, encode: String?) {
        this.info = info
        if (null == tempBluetoothGattCharacteristic) {
            LogUtils.e(TAG, "请先连接蓝牙设备")
            return
        }
        val data = string2bytes(info, encode)
        val temp = ByteArray(20)
        for (i in data!!.indices) {
            if ((i + 1) % 20 == 0) {
                writeDataList.add(temp)
            }
            temp[i % 20] = data[i]
        }
        val remainder = data.size % 20
        if (remainder > 0) {
            val lastArray = ByteArray(remainder)
            for (j in 0 until remainder) {
                lastArray[j] = temp[j]
            }
            writeDataList.add(lastArray)
        }
        //循环写数据
        groupWrite()
    }


    private fun groupWrite() {
        if (groupId > writeDataList.size - 1) {
            dataListener!!.writeSuccess(info)
            //初始化
            writeDataList.clear()
            info = null
            groupId = 0
            return
        }
        bleBytesWrite(writeDataList[groupId])
        groupId++
    }

    /**
     * ble写数据(ble传输过程每次最大只能传输20个字节)
     * 可以请求更改MTU方法为：mBluetoothGatt.requestMtu(100);
     */
    private fun bleBytesWrite(data: ByteArray?) {
        mBluetoothGatt!!.setCharacteristicNotification(
                tempBluetoothGattCharacteristic,
                true
        ) //设置该特征具有Notification功能
        tempBluetoothGattCharacteristic!!.value = data //将指令放置进特征中
        tempBluetoothGattCharacteristic!!.writeType =
                BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE //设置回复形式
        mBluetoothGatt!!.writeCharacteristic(tempBluetoothGattCharacteristic) //开始写数据
    }

    /**
     * ble读数据
     */
    fun bleBytesRead() {
        mBluetoothGatt!!.readCharacteristic(tempBluetoothGattCharacteristic)
    }

    /**
     * 字符串转化成byte[]数组
     * @param source
     * @return
     */
    private fun string2bytes(source: String, encode: String?): ByteArray? {
        var bytes: ByteArray? = null
        bytes = try {
            source.toByteArray(charset(encode!!))
        } catch (e: UnsupportedEncodingException) {
            e.printStackTrace()
            return null
        }
        return bytes
    }

    /**
     * 判断是否已经连接设备
     * @return
     */
    fun blueIsConnected(): Boolean {
        return mBluetoothAdapter!!.isEnabled && tempBluetoothGattCharacteristic != null
    }


    fun getConnectInfo(): ArrayList<BluetoothDevice>? {
        if (!blueIsConnected()) return null
        return devices
    }

    /**
     * ----------------------------------------------------------------------------------------------
     * 关闭蓝牙
     */
    fun closeBlueTooth() {
        if (mBluetoothAdapter != null) {
            mBluetoothAdapter!!.disable()
        }
    }

    /**
     * ----------------------------------------------------------------------------------------------
     * 释放资源和关闭广播
     */
    fun release() {
        try {
//            if (mBluetoothAdapter != null) {
//                mBluetoothAdapter!!.disable()
//            }
            devices.clear()
            if (!mEBTReceiverRegisterFlag!!) {
                ctx.unregisterReceiver(mEBTReceiver)
            }
            ctx.unregisterReceiver(mDDReceiver)
            ctx.unregisterReceiver(mEDReceiver)
        } catch (e: Exception) {
            LogUtils.e(TAG, "error is $e")
        }

    }

    companion object {
        var mBluetoothAdapter: BluetoothAdapter? = null

        /**
         * ----------------------------------------------------------------------------------------------
         * step5 连接ble设备，并监听读写操作回调
         */
        var mBluetoothGatt: BluetoothGatt? = null
        var tempBluetoothGattCharacteristic: BluetoothGattCharacteristic? = null
    }

    //绑定
    private fun createBond(bleDevice: BluetoothDevice): Boolean {
        var result = false
        try {
            val createBond: Method = bleDevice.javaClass.getMethod("createBond")
            val invoke = createBond.invoke(bleDevice) as Boolean
            result = invoke
        } catch (e: NoSuchMethodException) {
            e.printStackTrace()
        } catch (e: InvocationTargetException) {
            e.printStackTrace()
        } catch (e: IllegalAccessException) {
            e.printStackTrace()
        }
        return result
    }

    //与设备解除配对
    fun removeBond(bleDevice: BluetoothDevice): Boolean {
        var result = false
        try {
            val removeBond: Method = bleDevice.javaClass.getMethod("removeBond")
            val returnValue = removeBond.invoke(bleDevice) as Boolean
            result = returnValue
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
        return result
    }


    private fun clearCache(): Boolean {
        try {
            mBluetoothGatt?.let {
                val localMethod: Method = it.javaClass.getMethod("refresh")
                if (localMethod != null) {
                    return localMethod.invoke(mBluetoothGatt) as Boolean
                }
            } ?: kotlin.run {
                return false
            }
        } catch (localException: java.lang.Exception) {
            LogUtils.e("BluetoothUtils", "clear cache error is $localException")
        }
        return false
    }


    fun run() {
        LogUtils.e(TAG, "start run")
        Thread {
            var socket: BluetoothSocket?
            mSocket = bluetoothAdapter?.listenUsingInsecureRfcommWithServiceRecord("transformationFileServer", BLE_SERVICE_UUID)
            try {
                while (running) {

                    mSocket?.let {
                        socket = it.accept()
                        socket?.let { socket ->
                            //关闭连接，只保留一个连接
                            mSocket?.close()
                            loopRead(socket)
//                            val inputStream = socket.inputStream
//                            val outputStream = socket.outputStream
//                            var read: Int
//                            val byteArray = ByteArray(1024) { 0 }
//                            while (socket.isConnected) {
//                                read = inputStream.read(byteArray)
//                                if (read == -1) break
//                                val byte = ByteArray(read) { readByte -> byteArray[readByte] }
//                                LogUtils.e(TAG, "receive byte ${byte}")
//                                break
//                            }
//
//                            outputStream.close()
//                            socket.close()
                        }

                    }
                }
            } catch (e: java.lang.Exception) {
                LogUtils.e(TAG, "error is $e")
            }
        }.start()

    }

    fun connectBleDeviceSocket(device: BluetoothDevice, content: String) {
        LogUtils.e(TAG, "start connect")
        sSocket?.let {
            try {
                it.close()
            } catch (e: java.lang.Exception) {
                LogUtils.e(TAG, "close sSocket error is $e")
            }
            sSocket = null
        }

        var outputStream: OutputStream? = null
        try {
            sSocket = device.createRfcommSocketToServiceRecord(BLE_SERVICE_UUID)
            sSocket?.let {
                it.connect()
                outputStream = it.outputStream
                LogUtils.e(TAG, "write byteArray ${content.toByteArray()}")
                outputStream?.write(content.toByteArray())
            }
        } catch (e: java.lang.Exception) {
            LogUtils.e(TAG, "connect sSocket error is $e")
        } finally {
            try {
                outputStream?.close()

            } catch (e: java.lang.Exception) {
                LogUtils.e(TAG, "close outputStream error is $e")
            }

            try {
                sSocket?.close()
            } catch (e: java.lang.Exception) {
                LogUtils.e(TAG, "close sSocket error is $e")
            }
        }
    }

    var isRead = true
    private fun loopRead(socket: BluetoothSocket) {
        try {
            if (!socket.isConnected) {
                socket.connect()
            }
            val inputStream = DataInputStream(socket.inputStream)
            isRead = true
            while (isRead) {

                val filePath = "${ctx.getExternalFilesDir("Bluetooth")!!.absolutePath}${File.separator}document${File.separator}"

                val file = File(filePath)
                file.mkdir()

                val fileName = inputStream.readUTF()
                val fileLength = inputStream.readLong()

                LogUtils.e(TAG, "正在接收文件 $fileName")

                var len: Long = 0
                var r: Int = 0
                val b = ByteArray(4 * 1024)

                val fileOutputStream = FileOutputStream(filePath + fileName)


                while (`inputStream`.read(b).also { r = it } !== -1) {
                    fileOutputStream.write(b, 0, r)
                    len += r
                    if (len >= fileLength) break
                }
                LogUtils.e(TAG, "文件接收成功 ${filePath + fileName}")

            }
        } catch (e: java.lang.Exception) {
            socket.close()
        }
    }

    fun sendFile(filePath: String) {
        if (isSending || filePath.isEmpty()) {
            return
        }
        sSocket?.let {
            try {
                it.close()
            } catch (e: java.lang.Exception) {
                LogUtils.e(TAG, "close sSocket error is $e")
            }
            sSocket = null
        }
        val first = devices.first()
        first?.let{ device ->
            sSocket = device.createRfcommSocketToServiceRecord(BLE_SERVICE_UUID)

            Thread {
                isSending = true
                try {
                    sSocket?.let { socket ->
                        if(!socket.isConnected){
                            socket.connect()
                        }
                        val `in` = FileInputStream(filePath)
                        val file = File(filePath)
                        mDataOutputStream = DataOutputStream(socket.outputStream)
                        mDataOutputStream?.writeUTF(file.name)
                        mDataOutputStream?.writeLong(file.length())
                        var r: Int
                        val b = ByteArray(4 * 1024)
                        while (`in`.read(b).also { r = it } != -1) {
                            mDataOutputStream?.write(b, 0, r)
                        }
                        LogUtils.e(TAG,"send file success $filePath")
                    }


                } catch (e: java.lang.Exception) {
                    LogUtils.e(TAG,"send file error is $e  and path is $filePath")
                    close()
                }

                isSending = false
            }.start()
        }


    }


    fun close(){
        isRead = false
        mSocket?.close()
        sSocket?.close()
    }

    fun sendMessage(str: String) {

    }

    var mSocket: BluetoothServerSocket? = null
    var sSocket: BluetoothSocket? = null
    var running: Boolean = true
    var isSending: Boolean = false
    var mDataOutputStream: DataOutputStream? = null
    var mDataInputStream: DataInputStream? = null
    val BLE_SERVICE_UUID = UUID.fromString("00001802-0000-1000-8000-00805f9b34fb")
    val BLE_WRITE_UUID = UUID.fromString("00002a02-0000-1000-8000-00805f9b34fb")

    init {
        bluetoothAdapter
    }
}