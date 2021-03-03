package com.kai.base.application

import com.tencent.tinker.loader.app.TinkerApplication
import com.tencent.tinker.loader.shareutil.ShareConstants

/**
 *
 * @ProjectName:    My Application
 * @Description:     java类作用描述
 * @Author:         pressureKai
 * @UpdateDate:     2021/1/8 10:42
 */
class TinkerAppLike :TinkerApplication(
    ShareConstants.TINKER_ENABLE_ALL,
    "com.kai.common.application.BaseApplication",
    "com.tencent.tinker.loader.TinkerLoader",
    false
){
}