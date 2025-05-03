package com.aircraftcarrier.framework.data.core;

import com.aircraftcarrier.framework.enums.DeletedEnum;
import com.aircraftcarrier.framework.security.core.LoginUserUtil;
import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.reflection.MetaObject;

import java.time.LocalDateTime;
import java.util.Date;

/**
 * 自定义Sql注入
 *
 * @author zh
 */
@Slf4j
public class MybatisMetaObjectHandler implements MetaObjectHandler {

    private static final String CREATE_TIME = "createTime";
    private static final String CREATE_USER = "createUser";
    private static final String UPDATE_TIME = "updateTime";
    private static final String UPDATE_USER = "updateUser";
    private static final String DELETED = "deleted";

    @Override
    public void insertFill(MetaObject metaObject) {
//        LocalDateTime now = LocalDateTime.now();
        Date now = new Date();
        String loginUserName = LoginUserUtil.getLoginUserName();
        this.setFieldValByName(CREATE_TIME, now, metaObject);
        this.setFieldValByName(CREATE_USER, loginUserName, metaObject);
        this.setFieldValByName(UPDATE_TIME, now, metaObject);
        this.setFieldValByName(UPDATE_USER, loginUserName, metaObject);
        this.setFieldValByName(DELETED, DeletedEnum.NORMAL.getCode(), metaObject);
    }

    @Override
    public void updateFill(MetaObject metaObject) {
        this.setFieldValByName(UPDATE_TIME, new Date(), metaObject);
        this.setFieldValByName(UPDATE_USER, LoginUserUtil.getLoginUserName(), metaObject);
    }
}
