package com.aircraftcarrier.framework.data;

import com.aircraftcarrier.framework.enums.YnValueEnum;

import java.time.LocalDateTime;

/**
 * 填充公共属性
 *
 * @author lzp
 * @version 1.0
 * @date 2021-03-01
 */
public class FillUtil {

    public static <T> BaseDO<T> fillByCreate(LocalDateTime createTime, String createUser, BaseDO<T> baseDO) {
        baseDO.setUpdateTime(createTime);
        baseDO.setCreateTime(createTime);
        baseDO.setUpdateUser(createUser);
        baseDO.setCreateUser(createUser);
        baseDO.setYn(YnValueEnum.Y.getCode());
        return baseDO;
    }

    public static <T> BaseDO<T> fillByUpdate(LocalDateTime updateTime, String updateUser, BaseDO<T> baseDO) {
        baseDO.setUpdateTime(updateTime);
        baseDO.setUpdateUser(updateUser);
        return baseDO;
    }

    public static <T> BaseDO<T> fillByDelete(LocalDateTime updateTime, String updateUser, BaseDO<T> baseDO) {
        baseDO.setUpdateTime(updateTime);
        baseDO.setUpdateUser(updateUser);
        baseDO.setYn(YnValueEnum.N.getCode());
        return baseDO;
    }
}
