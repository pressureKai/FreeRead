# FreeRead
Android 免费小说阅读器
####  基于网络爬虫实现的小说阅读器，实现了全局夜间/夜间模式切换，全局字体切换，书籍章节预加载，字体/主题/亮度设置，模拟翻页效果，Wi-Fi传书以及实现基于本地数据库实现的书籍浏览历史，模拟账号注册登陆，书籍收藏，书籍添加书架等功能

# 项目介绍  
  1.项目阅读器参考[任阅](https://github.com/JustWayward/BookReader) 
    并做以下改动
    1).源代码重构为使用kotlin语言实现
    2).书籍的缓存由原作者的ACache类改为Room数据库实现
    3).更改数据成更切合爬虫数据的实体类
  2.数据源爬虫参考[CrawlerForReader](https://github.com/smuyyh/CrawlerForReader)  
    1).源代码重构为使用kotlin语言实现
    2).更改数据回调方式（原作者使用接口回调更改为使用Rxjava进行回调）
  3.使用Kotlin语言编写
  4.使用Room数据库存储数据
  5.使用[ImmersionBar](https://github.com/gyf-dev/ImmersionBar) 实现沉浸式状态栏
  6.使用[今日头条屏幕适配](https://github.com/JessYanCoding/AndroidAutoSize) 方案
  7.日间模式/夜间模式切换使用 [Android-skin-support](https://github.com/ximsfei/Android-skin-support)
  8.全局字体切换使用[calligraphy](https://github.com/chrisjenx/Calligraphy)
  9.集成bugly奔溃上报，以及应用内更新
    

## LICENSE

```
Copyright 2021 PressureKai Team, All right reserved.
Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```


