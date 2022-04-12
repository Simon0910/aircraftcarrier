/*
 * Copyright (c) 2011-2020, baomidou (jobob@qq.com).
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * <p>
 * https://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.aircraftcarrier.generator.custom;

import com.aircraftcarrier.generator.config.po.TableInfo;
import com.baomidou.mybatisplus.core.toolkit.Assert;

/**
 * 输出文件配置
 *
 * @author hubin
 * @since 2017-01-18
 */
public abstract class FileOutConfig {

    /**
     * 模板
     */
    private String templatePath;
    private String key;
    private String outputFilePath;
    private String classPackage;
    private String classFullName;
    private String className;
    private String classSuffix;

    /**
     * 默认构造方法
     *
     * @deprecated 3.4.1
     */
    @Deprecated
    public FileOutConfig() {
        // to do nothing
    }

    public FileOutConfig(String templatePath, String classPath, String key, String classSuffix) {
        Assert.notEmpty(templatePath, "模板路径不能为空!");
        Assert.notEmpty(classPath, "包路径不能为空!");
        Assert.notEmpty(key, "模板key不能为空!");
        this.templatePath = templatePath;
        this.key = key;
        this.classPackage = classPath.replace("/", ".");
        this.classSuffix = classSuffix;
    }

    /**
     * 输出文件
     * D:/IdeaProjects/vsc/vsc-portal/vsc-portal-domain/src/main/java/com/jd/wl/vsc/portal/domain/model/product/ProductDetails.java
     */
    public abstract String outputFile(TableInfo tableInfo);


    /**
     * 获取assembleClassName 例如: com/jd/wl/vsc/portal/domain/model/product/ProductDetailsEntity.java
     */
    public String assembleClassName(String className,String suffix) {
        this.className = className + classSuffix;
        this.classFullName = this.classPackage + "." + this.className;
        return classFullName.replace(".", "/") + suffix;
    }

    /**
     * 获取 classPackage: 例如: com.jd.wl.vsc.portal.service.impl
     */
    public String outClassPackage() {
        return this.classPackage;
    }

    /**
     * 获取 classImport: 例如: com.jd.wl.vsc.portal.service.impl.ProductDetailsServiceImp
     */
    public String outClassFullName() {
        return this.classFullName;
    }

    /**
     * 获取 classImport: 例如: ProductDetailsServiceImp
     */
    public String outClassName() {
        return this.className;
    }

    /**
     * 获取模板路径信息
     *
     * @return 模板路径信息
     */
    public String getTemplatePath() {
        return templatePath;
    }

    /**
     * @param templatePath 模块路径
     * @return this
     * @deprecated 3.4.1
     */
    @Deprecated
    public FileOutConfig setTemplatePath(String templatePath) {
        this.templatePath = templatePath;
        return this;
    }

    public String getKey() {
        return key;
    }

    public String getClassSuffix() {
        return classSuffix;
    }
}
