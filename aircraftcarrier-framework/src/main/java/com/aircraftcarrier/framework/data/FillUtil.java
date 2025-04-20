package com.aircraftcarrier.framework.data;

import com.aircraftcarrier.framework.enums.DeletedEnum;

import java.util.Date;

/**
 * 填充公共属性
 *
 * @author lzp
 * @version 1.0
 * @date 2021-03-01
 */
public class FillUtil {

    public static <T> BaseDO<T> fillByCreate(Date createTime, String createUser, BaseDO<T> baseDO) {
        baseDO.setUpdateTime(createTime);
        baseDO.setCreateTime(createTime);
        baseDO.setUpdateUser(createUser);
        baseDO.setCreateUser(createUser);
        baseDO.setDeleted(DeletedEnum.NORMAL.getCode());
        return baseDO;
    }

    public static <T> BaseDO<T> fillByUpdate(Date updateTime, String updateUser, BaseDO<T> baseDO) {
        baseDO.setUpdateTime(updateTime);
        baseDO.setUpdateUser(updateUser);
        return baseDO;
    }

    public static <T> BaseDO<T> fillByDelete(Date updateTime, String updateUser, BaseDO<T> baseDO) {
        baseDO.setUpdateTime(updateTime);
        baseDO.setUpdateUser(updateUser);
        baseDO.setDeleted(DeletedEnum.DELETED.getCode());
        return baseDO;
    }
}
