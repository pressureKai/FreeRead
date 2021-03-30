package com.kai.learn.rxjava3.activity

/**
 *  个人预估占比分成
 *     规模
 *        0-20  ->  demo
 *       20-99  -> 1,3
 *       99-up -> 1,2,3
 * 1. java基础 -> 设计模式 -  3
 * 2. 数据结构 -> 算法  - 2
 * 3. android基础 -> kotlin  - 3
 * 4. 第三方库(Rxjava,glide,oKHttp,retrofit) - 1
 */
import com.kai.commonmvvm.activity.BaseMvvmActivity
import kotlin.properties.ReadWriteProperty

class RxjavaMainActivity  : BaseMvvmActivity(){


    init{

       // ReadWriteProperty
//        List<String>()
    }
    /**
     *  java 基础
     *   常用的数据结构(名词解释,同类或相似数据结构效率对比,一句话概括)
     *   主要分为3大类
     *   1.collection(数据集合) 2.map(图 - 键值对映射)
     *
     *   list 与 map 都继承了 Collection 接口
     *   Collection 继承了 Iterator 迭代器(一个提供数据迭代功能的只读接口)接口
     *
     *   collection
     *
     *
     *
     *
     *
     *
     * 设计模式
     *    委托模式 - 组合优于继承  https://blog.csdn.net/wangpeifeng669/article/details/26403119
     *    综上概述：
              组合通常优于继承
             1.考虑使用多态，可以用继承。
             2.考虑复用父类方法，而且父类很少改动，可以用继承。
             其他情况请慎重使用继承。
     *     Java 中委托模式的实现 https://blog.csdn.net/wangpeifeng669/article/details/26271925
     *    委托方内部持有一个承包方对象将事件委托给承包方,真正做处理的还是承包方（没有继承接口的约束）
     *      1. 承包方并不要求继承同一个接口作为公共方法的约束
     *
     *      //承包方
     *      class RealPrint(){
     *           fun print(){
     *              System.out.print("realPrint print something")
     *           }
     *      }
     *
     *      //委托方
     *      class print(){
     *          val print = RealPrint()
     *          fun main(){
     *               print.print()
     *          }
     *      }
     *
     *
     *     Kotlin 中委托模式的实现 https://www.jianshu.com/p/bdf3bdfa15ce
     *
     *
     */

}