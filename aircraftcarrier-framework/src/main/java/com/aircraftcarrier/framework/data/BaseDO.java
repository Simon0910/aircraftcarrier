package com.aircraftcarrier.framework.data;

import com.aircraftcarrier.framework.enums.YnValueEnum;
import com.alibaba.fastjson.annotation.JSONField;
import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.time.LocalDateTime;

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
    public static final String ID = "id";
    @JSONField(name = ID)
    @TableId(value = "id", type = IdType.AUTO)
    protected Long id;

    /**
     * 创建人
     */
    public static final String CREATE_USER = "createUser";
    @JSONField(name = CREATE_USER)
    @TableField(fill = FieldFill.INSERT)
    protected String createUser;

    /**
     * 修改人
     */
    public static final String UPDATE_USER = "updateUser";
    @JSONField(name = UPDATE_USER)
    @TableField(fill = FieldFill.INSERT_UPDATE)
    protected String updateUser;

    /**
     * 创建时间
     */
    public static final String CREATE_TIME = "createTime";
    @JSONField(name = CREATE_TIME)
    @TableField(fill = FieldFill.INSERT)
    protected LocalDateTime createTime;

    /**
     * 修改时间
     */
    public static final String UPDATE_TIME = "updateTime";
    @JSONField(name = UPDATE_TIME)
    @TableField(fill = FieldFill.INSERT_UPDATE)
    protected LocalDateTime updateTime;

    /**
     * 0正常 1删除
     */
    public static final String YN = "yn";
    @JSONField(name = YN)
    @TableLogic
    protected Integer yn;

    public YnValueEnum getYnEnum() {
        return YnValueEnum.convertCode(yn);
    }

    public Serializable pkVal() {
        return id;
    }
}

