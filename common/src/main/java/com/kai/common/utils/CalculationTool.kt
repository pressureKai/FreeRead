package com.kai.common.utils

import java.math.BigDecimal

/**
 * Created by 12457 on 2017/9/4.
 */
class CalculationTool {
    companion object{
        //默认除法运算精度
        private const val DEF_DIV_SCALE = 10

        /**
         * 提供精确的加法运算
         *
         * @param v1 被加数
         * @param v2 加数
         * @return 两个参数的和
         */
        fun add(v1: Double, v2: Double): Double {
            val b1 = BigDecimal(java.lang.Double.toString(v1))
            val b2 = BigDecimal(java.lang.Double.toString(v2))
            return b1.add(b2).toDouble()
        }

        /**
         * 提供精确的加法运算
         *
         * @param v1 被加数
         * @param v2 加数
         * @return 两个参数的和
         */
        fun add(v1: String?, v2: String?): BigDecimal {
            val b1 = BigDecimal(v1)
            val b2 = BigDecimal(v2)
            return b1.add(b2)
        }

        /**
         * 提供精确的加法运算
         *
         * @param v1    被加数
         * @param v2    加数
         * @param scale 保留scale 位小数
         * @return 两个参数的和
         */
        fun add(v1: String?, v2: String?, scale: Int): String {
            require(scale >= 0) { "The scale must be a positive integer or zero" }
            val b1 = BigDecimal(v1)
            val b2 = BigDecimal(v2)
            return b1.add(b2).setScale(scale, BigDecimal.ROUND_HALF_UP).toString()
        }

        /**
         * 提供精确的减法运算
         *
         * @param v1 被减数
         * @param v2 减数
         * @return 两个参数的差
         */
        fun sub(v1: Double, v2: Double): Double {
            val b1 = BigDecimal(java.lang.Double.toString(v1))
            val b2 = BigDecimal(java.lang.Double.toString(v2))
            return b1.subtract(b2).toDouble()
        }

        /**
         * 提供精确的减法运算。
         *
         * @param v1 被减数
         * @param v2 减数
         * @return 两个参数的差
         */
        fun sub(v1: String?, v2: String?): BigDecimal {
            val b1 = BigDecimal(v1)
            val b2 = BigDecimal(v2)
            return b1.subtract(b2)
        }

        /**
         * 提供精确的减法运算
         *
         * @param v1    被减数
         * @param v2    减数
         * @param scale 保留scale 位小数
         * @return 两个参数的差
         */
        fun sub(v1: String?, v2: String?, scale: Int): String {
            require(scale >= 0) { "The scale must be a positive integer or zero" }
            val b1 = BigDecimal(v1)
            val b2 = BigDecimal(v2)
            return b1.subtract(b2).setScale(scale, BigDecimal.ROUND_HALF_UP).toString()
        }

        /**
         * 提供精确的乘法运算
         *
         * @param v1 被乘数
         * @param v2 乘数
         * @return 两个参数的积
         */
        fun mul(v1: Double, v2: Double): Double {
            val b1 = BigDecimal(java.lang.Double.toString(v1))
            val b2 = BigDecimal(java.lang.Double.toString(v2))
            return b1.multiply(b2).toDouble()
        }

        /**
         * 提供精确的乘法运算
         *
         * @param v1 被乘数
         * @param v2 乘数
         * @return 两个参数的积
         */
        fun mul(v1: String?, v2: String?): BigDecimal {
            val b1 = BigDecimal(v1)
            val b2 = BigDecimal(v2)
            return b1.multiply(b2)
        }

        /**
         * 提供精确的乘法运算
         *
         * @param v1    被乘数
         * @param v2    乘数
         * @param scale 保留scale 位小数
         * @return 两个参数的积
         */
        fun mul(v1: Double, v2: Double, scale: Int): Double {
            val b1 = BigDecimal(java.lang.Double.toString(v1))
            val b2 = BigDecimal(java.lang.Double.toString(v2))
            return round(b1.multiply(b2).toDouble(), scale)
        }

        /**
         * 提供精确的乘法运算
         *
         * @param v1    被乘数
         * @param v2    乘数
         * @param scale 保留scale 位小数
         * @return 两个参数的积
         */
        fun mul(v1: String?, v2: String?, scale: Int): String {
            require(scale >= 0) { "The scale must be a positive integer or zero" }
            val b1 = BigDecimal(v1)
            val b2 = BigDecimal(v2)
            return b1.multiply(b2).setScale(scale, BigDecimal.ROUND_HALF_UP).toString()
        }
        /**
         * 提供（相对）精确的除法运算。当发生除不尽的情况时，由scale参数指
         * 定精度，以后的数字四舍五入
         *
         * @param v1    被除数
         * @param v2    除数
         * @param scale 表示表示需要精确到小数点以后几位。
         * @return 两个参数的商
         */
        /**
         * 提供（相对）精确的除法运算，当发生除不尽的情况时，精确到
         * 小数点以后10位，以后的数字四舍五入
         *
         * @param v1 被除数
         * @param v2 除数
         * @return 两个参数的商
         */
        fun div(v1: Double, v2: Double, scale: Int = DEF_DIV_SCALE): Double {
            require(scale >= 0) { "The scale must be a positive integer or zero" }
            val b1 = BigDecimal(java.lang.Double.toString(v1))
            val b2 = BigDecimal(java.lang.Double.toString(v2))
            return b1.divide(b2, scale, BigDecimal.ROUND_HALF_UP).toDouble()
        }

        /**
         * 提供（相对）精确的除法运算。当发生除不尽的情况时，由scale参数指
         * 定精度，以后的数字四舍五入
         *
         * @param v1    被除数
         * @param v2    除数
         * @param scale 表示需要精确到小数点以后几位
         * @return 两个参数的商
         */
        fun div(v1: String?, v2: String?, scale: Int): String {
            require(scale >= 0) { "The scale must be a positive integer or zero" }
            val b1 = BigDecimal(v1)
            val b2 = BigDecimal(v1)
            return b1.divide(b2, scale, BigDecimal.ROUND_HALF_UP).toString()
        }

        /**
         * 提供精确的小数位四舍五入处理
         *
         * @param v     需要四舍五入的数字
         * @param scale 小数点后保留几位
         * @return 四舍五入后的结果
         */
        fun round(v: Double, scale: Int): Double {
            require(scale >= 0) { "The scale must be a positive integer or zero" }
            val b = BigDecimal(java.lang.Double.toString(v))
            return b.setScale(scale, BigDecimal.ROUND_HALF_UP).toDouble()
        }

        /**
         * 提供精确的小数位四舍五入处理
         *
         * @param v     需要四舍五入的数字
         * @param scale 小数点后保留几位
         * @return 四舍五入后的结果
         */
        fun round(v: String?, scale: Int): String {
            require(scale >= 0) { "The scale must be a positive integer or zero" }
            val b = BigDecimal(v)
            return b.setScale(scale, BigDecimal.ROUND_HALF_UP).toString()
        }

        /**
         * 取余数
         *
         * @param v1    被除数
         * @param v2    除数
         * @param scale 小数点后保留几位
         * @return 余数
         */
        fun remainder(v1: String?, v2: String?, scale: Int): String {
            require(scale >= 0) { "The scale must be a positive integer or zero" }
            val b1 = BigDecimal(v1)
            val b2 = BigDecimal(v2)
            return b1.remainder(b2).setScale(scale, BigDecimal.ROUND_HALF_UP).toString()
        }

        /**
         * 取余数  BigDecimal
         *
         * @param v1    被除数
         * @param v2    除数
         * @param scale 小数点后保留几位
         * @return 余数
         */
        fun remainder(v1: BigDecimal, v2: BigDecimal?, scale: Int): BigDecimal {
            require(scale >= 0) { "The scale must be a positive integer or zero" }
            return v1.remainder(v2).setScale(scale, BigDecimal.ROUND_HALF_UP)
        }

        /**
         * 比较大小
         *
         * @param v1 被比较数
         * @param v2 比较数
         * @return 如果v1 大于v2 则 返回true 否则false
         */
        fun compare(v1: String?, v2: String?): Boolean {
            val b1 = BigDecimal(v1)
            val b2 = BigDecimal(v2)
            val bj = b1.compareTo(b2)
            val res: Boolean
            res = if (bj > 0) true else false
            return res
        }
    }

}