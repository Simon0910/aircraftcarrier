package com.aircraftcarrier.framework.data;

import com.aircraftcarrier.framework.enums.DeletedEnum;
import com.alibaba.fastjson.annotation.JSONField;
import com.baomidou.mybatisplus.annotation.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.util.Date;

/**
 * BaseDo
 *
 * @author lzp
 * @date 2020-11-18 15:56
 */
@ToString
@Getter
@Setter
public class BaseDO<T> implements Serializable {
    /**
     * id
     */
    public static final String ID = "id";
    /**
     * 创建人
     */
    public static final String CREATE_USER = "createUser";
    /**
     * 修改人
     */
    public static final String UPDATE_USER = "updateUser";
    /**
     * 创建时间
     */
    public static final String CREATE_TIME = "createTime";
    /**
     * 修改时间
     */
    public static final String UPDATE_TIME = "updateTime";
    /**
     * 0正常 1删除
     */
    public static final String DELETED = "deleted";
    /**
     * serialVersionUID
     */
    private static final long serialVersionUID = 1L;
    @JSONField(name = ID)
    @TableId(value = "id", type = IdType.AUTO)
    protected Long id;
    @JSONField(name = CREATE_USER)
    @TableField(fill = FieldFill.INSERT)
    protected String createUser;
    @JSONField(name = UPDATE_USER)
    @TableField(fill = FieldFill.INSERT_UPDATE)
    protected String updateUser;
    @JSONField(name = CREATE_TIME)
    @TableField(fill = FieldFill.INSERT)
    protected Date createTime;
    @JSONField(name = UPDATE_TIME)
    @TableField(fill = FieldFill.INSERT_UPDATE)
    protected Date updateTime;
    @JSONField(name = DELETED)
    @TableLogic
    protected Integer deleted;

    public DeletedEnum getDeletedEnum() {
        return DeletedEnum.convertCode(deleted);
    }

    public Serializable pkVal() {
        return id;
    }
}

