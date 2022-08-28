package com.lzp.gencode;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * 类注释内容
 *
 * @author zhipengliu
 * @date 2022/8/1
 * @since 1.0
 */
@Getter
@Setter
public class ClassModel {

    private String type;

    private String className;

    private String comment;

    List<PropertyModel> propertyList = new ArrayList<>(100);
}
