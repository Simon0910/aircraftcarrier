package com.aircraftcarrier.framework.concurrent;

/**
 * 类注释内容
 *
 * @author zhipengliu
 * @date 2022/10/15
 * @since 1.0
 */
public class MessageUtil {

    public static void message(String msg, Object... value) {
        msg = msg.replaceAll("\\{\\}", "%s") + ". ";
        System.out.println(String.format(msg, value));
    }

}
