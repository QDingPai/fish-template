package com.qdp.fish.template.utils;

public class MyStringUtils {

    // 私有构造方法，防止实例化
    private MyStringUtils() {
        // 私有构造方法
    }

    /**
     * 判断字符串是否为空或null。
     *
     * @param str 要检查的字符串
     * @return 如果字符串为空或null，返回 true；否则返回 false
     */
    public static boolean isNullOrEmpty(String str) {
        return str == null || str.isEmpty();
    }

    /**
     * 将字符串按指定分隔符拆分成数组。
     *
     * @param str       要拆分的字符串
     * @param delimiter 分隔符
     * @return 拆分后的字符串数组
     */
    public static String[] splitString(String str, String delimiter) {
        if (isNullOrEmpty(str)) {
            return new String[0];
        }
        return str.split(delimiter);
    }

    /**
     * 将字符串反转。
     *
     * @param str 要反转的字符串
     * @return 反转后的字符串
     */
    public static String reverseString(String str) {
        if (isNullOrEmpty(str)) {
            return str;
        }
        return new StringBuilder(str).reverse().toString();
    }

    // 其他通用的字符串操作方法可以继续添加...

}

