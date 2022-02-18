package com.aircraftcarrier.marketing.store.common;

import lombok.Getter;
import lombok.Setter;

/**
 * @author lzp
 */
@Getter
@Setter
public class LoginUserInfo {
    private Long userId;
    private String userName;
    private String currentSellerNo;
    private String currentSellerName;
}