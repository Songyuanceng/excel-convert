/*
 * Copyright © 2015-2026 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * @since 0.0.1
 */

package com.yto;

import java.math.BigDecimal;
import java.text.DecimalFormat;

/**
 * @author jialiangli
 */
public class ZhiyunUtil {
    /**
     * 发送信息
     */
    public static final String SEND_SMS_MESSAGE_SENDER = "SEND_SMS_MESSAGE_SENDER";

    /**
     * 描述: 判断字符串是否为空
     *
     * @param name 字符串
     * @return boolean
     */
    public static boolean isEmpty(final String name) {
        return (null == name || name.length() == 0);
    }

    /**
     * 描述: 判断字符串是否为空
     *
     * @param name 字符串
     * @return boolean
     */
    public static boolean isNotEmpty(final String name) {
        return (null != name && name.length() > 0);
    }

    /**
     * 描述: 模糊查询时,过滤特殊字符
     *
     * @param string 字符串
     * @return string
     */
    public static String filterString(final String string) {
        return "%" + string.replaceAll("_", "\\\\_").replaceAll("%", "\\\\%").replaceAll("＿", "\\\\＿").trim() + "%";
    }

    /**
     * 描述: 模糊查询时,过滤特殊字符
     *
     * @param string 字符串
     * @return string
     */
    public static String filterStringRight(final String string) {
        return string.replaceAll("_", "\\\\_").replaceAll("%", "\\\\%").replaceAll("＿", "\\\\＿").trim() + "%";
    }

    /**
     * 描述: 模糊查询时,过滤特殊字符
     *
     * @param string 字符串
     * @return string
     */
    public static String filterStringLeft(final String string) {
        return "%" + string.replaceAll("_", "\\\\_").replaceAll("%", "\\\\%").replaceAll("＿", "\\\\＿").trim();
    }

    /**
     * @param obj1 第一个对象
     * @param obj2 第二个对象
     * @return boolean
     */
    public static boolean objCompator(final Object obj1, final Object obj2) {
        if (obj1 == null && obj2 != null) {
            return false;
        } else if (obj1 != null) {
            return obj1.equals(obj2);
        }
        return true;
    }

    /**
     * 格式化double类型数字，四舍五入保留两位小数
     *
     * @param number 数字
     * @return Double
     */
    public static Double formatDoubleNumber(final Double number) {
        BigDecimal b = new BigDecimal(number);
        return b.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
    }

    /**
     * 格式化double类型数字，保留两位小数
     *
     * @param number 数字
     * @return Double
     */
    public static Double formatDouble(final Double number) {
        DecimalFormat df = new DecimalFormat("######0.00");
        return Double.valueOf(df.format(number));
    }

    /**
     * 计算百分比
     *
     * @param x     单数
     * @param total 总数
     * @return string
     */
    public static String getPercent(final Double x, final Double total) {
        if (total == 0) {
            return "0.00%";
        } else {
            double tempresult = x / total;
            // ##.00%
            DecimalFormat df1 = new DecimalFormat("0.00%");
            // 百分比格式，后面不足2位的用0补齐
            return df1.format(tempresult);
        }
    }

    /**
     * @param num          数字
     * @param formatLength 格式化长度
     * @return string
     */
    public static String frontCompWithZore(final int num, final int formatLength) {
        return String.format("%0" + formatLength + "d", num);
    }
}
