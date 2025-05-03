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
     * serialVersionUID
     */
    private static final long serialVersionUID = 1L;

    /**
     * id
     */
    private static final String ID = "id";
    @JSONField(name = ID)
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 创建人
     */
    private static final String CREATE_USER = "createUser";
    @JSONField(name = CREATE_USER)
    @TableField(fill = FieldFill.INSERT)
    private String createUser;

    /**
     * 修改人
     */
    private static final String UPDATE_USER = "updateUser";
    @JSONField(name = UPDATE_USER)
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private String updateUser;

    /**
     * 创建时间
     */
    private static final String CREATE_TIME = "createTime";
    @JSONField(name = CREATE_TIME)
    @TableField(fill = FieldFill.INSERT)
    private Date createTime;

    /**
     * 修改时间
     */
    private static final String UPDATE_TIME = "updateTime";
    @JSONField(name = UPDATE_TIME)
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Date updateTime;

    /**
     * 0正常 1删除
     */
    private static final String DELETED = "deleted";
    @JSONField(name = DELETED)
    @TableLogic
    private Integer deleted;

    /**
     * 数据版本号
     */
    @TableField("version")
    @Version
    private Long version;

    public DeletedEnum getDeletedEnum() {
        return DeletedEnum.convertCode(deleted);
    }

    public Serializable pkVal() {
        return id;
    }
}

