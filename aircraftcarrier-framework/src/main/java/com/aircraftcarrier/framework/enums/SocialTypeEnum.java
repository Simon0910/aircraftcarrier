package com.aircraftcarrier.framework.enums;

import com.aircraftcarrier.framework.tookit.MapUtil;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Map;

/**
 * 社交平台的类型枚举
 *
 * @author yudao
 */
@Getter
@AllArgsConstructor
public enum SocialTypeEnum implements IEnum<Integer> {

    /**
     * GitEE
     * 文档链接：https://gitee.com/api/v5/oauth_doc#/
     */
    GIT_EE(10, "GIT_EE"),
    /**
     * 钉钉
     * 文档链接：https://developers.dingtalk.com/document/app/obtain-identity-credentials
     */
    DING_TALK(20, "DING_TALK"),

    /**
     * 企业微信
     * 文档链接：https://xkcoding.com/2019/08/06/use-justauth-integration-wechat-enterprise.html
     */
    WECHAT_ENTERPRISE(30, "WECHAT_ENTERPRISE"),
    /**
     * 微信公众平台 - 移动端 H5
     * 文档链接：https://www.cnblogs.com/juewuzhe/p/11905461.html
     */
    WECHAT_MP(31, "WECHAT_MP"),
    /**
     * 微信开放平台 - 网站应用 PC 端扫码授权登录
     * 文档链接：https://justauth.wiki/guide/oauth/wechat_open/#_2-申请开发者资质认证
     */
    WECHAT_OPEN(32, "WECHAT_OPEN"),
    /**
     * 微信小程序
     * 文档链接：https://developers.weixin.qq.com/miniprogram/dev/framework/open-ability/login.html
     */
    WECHAT_MINI_PROGRAM(33, "WECHAT_MINI_PROGRAM"),
    ;

    /**
     * MAPPINGS
     */
    private static final Map<Integer, SocialTypeEnum> MAPPINGS = MapUtil.newHashMap(values().length);

    static {
        for (SocialTypeEnum value : values()) {
            MAPPINGS.put(value.getCode(), value);
        }
    }

    /**
     * 类型
     */
    private final Integer code;
    /**
     * 类型的标识
     */
    private final String desc;
}
