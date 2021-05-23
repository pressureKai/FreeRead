/**
 * Copyright 2016 JustWayward Team
 *
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.kai.wifitransfer

import androidx.fragment.app.FragmentActivity
import java.io.IOException

/**
 * Wifi传书 服务端
 *
 * @author yuyh.
 * @date 2016/10/10.
 */
object ServerRunner {
    private var server: SimpleFileServer? = null
    var serverIsRunning = false

    /**
     * 启动wifi传书服务
     */
    fun startServer(): SimpleFileServer? {
        server = SimpleFileServer.instance
        try {
            if (!serverIsRunning) {
                server!!.start()
                serverIsRunning = true
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return server
    }

    fun stopServer() {
        if (server != null) {
            server!!.stop()
            serverIsRunning = false
        }
    }
}