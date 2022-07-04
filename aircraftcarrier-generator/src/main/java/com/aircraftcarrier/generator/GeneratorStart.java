package com.aircraftcarrier.generator;

import com.aircraftcarrier.generator.config.DataSourceConfig;
import com.aircraftcarrier.generator.config.GlobalConfig;
import com.aircraftcarrier.generator.config.InjectionConfig;
import com.aircraftcarrier.generator.config.OutputFile;
import com.aircraftcarrier.generator.config.PackageConfig;
import com.aircraftcarrier.generator.config.StrategyConfig;
import com.aircraftcarrier.generator.config.TemplateConfig;
import com.aircraftcarrier.generator.config.po.LikeTable;
import com.aircraftcarrier.generator.config.po.TableInfo;
import com.aircraftcarrier.generator.config.querys.MySqlQuery;
import com.aircraftcarrier.generator.config.rules.DateType;
import com.aircraftcarrier.generator.config.rules.NamingStrategy;
import com.aircraftcarrier.generator.custom.FileOutConfig;
import com.aircraftcarrier.generator.custom.MybatisMapperCache;
import com.aircraftcarrier.generator.custom.MybatisMySqlTypeConvert;
import com.aircraftcarrier.generator.fill.Column;
import com.aircraftcarrier.generator.fill.Property;
import com.aircraftcarrier.generator.keywords.MySqlKeyWordsHandler;
import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.core.toolkit.StringPool;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author lzp
 */
public class GeneratorStart {
    private static String parent = "com.aircraftcarrier.marketing.store";
    private static final String entityPackage = "infrastructure.repository.dataobject";
    private static final String commonProjectName = "marketing-store-common";
    private static final String appProjectName = "marketing-store-app";
    private static final String clientProjectName = "marketing-store-client";
    private static final String controllerProjectName = "marketing-store-adapter";
    private static final String domainProjectName = "marketing-store-domain";
    private static final String infrastructureProjectName = "marketing-store-infrastructure";
    private static final String entitySupperClassName = "com.aircraftcarrier.framework.data.BaseDO";
    private static final String controllerSupperClassName = "com.aircraftcarrier.marketing.store.adapter.common.BaseController";
    private static final String serviceSupperClassName = "";
    private static final String mapperSupperClassName = "com.aircraftcarrier.framework.data.core.MybatisBaseMapper";
    private static final String entitySuffix = "Do";
    private static String moduleName;
    private static String[] tables;
    private static String URL;
    private static String USERNAME;
    private static String PASSWORD;
    private static String author;
    private static String[] tablePrefix;
    private static String outputDir;

    public GeneratorStart() {

    }

    public GeneratorStart(String url, String user, String password, String author,
                          String outputDir, String[] tablePrefix) {
        GeneratorStart.URL = url;
        GeneratorStart.USERNAME = user;
        GeneratorStart.PASSWORD = password;
        GeneratorStart.author = author;
        GeneratorStart.tablePrefix = tablePrefix;
        GeneratorStart.outputDir = outputDir;
    }

    public GeneratorStart parent(String parent) {
        GeneratorStart.parent = parent;
        return this;
    }

    @NotNull
    private static StrategyConfig getStrategyConfig() {
        StrategyConfig strategyConfig = new StrategyConfig.Builder()
                .enableCapitalMode()
                .enableSkipView()
                .disableSqlFilter()
                .likeTable(new LikeTable("USER"))
                .addInclude(tables)
                .addTablePrefix(tablePrefix)
                .addFieldSuffix("_flag")
                // Entity 策略配置
                .entityBuilder()
                .superClass(entitySupperClassName)
                .disableSerialVersionUID()
                .enableLombok()
                .enableRemoveIsPrefix()
                .enableTableFieldAnnotation()
                .versionColumnName("version")
                .versionPropertyName("version")
                .logicDeleteColumnName("yn")
                .logicDeletePropertyName("yn")
                .naming(NamingStrategy.underline_to_camel)
                .columnNaming(NamingStrategy.underline_to_camel)
                .addIgnoreColumns("yn")
                .addSuperEntityColumns("id", "create_user", "create_Time", "update_user", "update_time", "yn")
                .addTableFills(new Column("create_time", FieldFill.INSERT))
                .addTableFills(new Property("updateTime", FieldFill.INSERT_UPDATE))
                .idType(IdType.AUTO)
                .formatFileName("%s" + entitySuffix)
                // Controller 策略配置
                .controllerBuilder()
                .superClass(controllerSupperClassName)
                .enableHyphenStyle()
                .enableRestStyle()
                .formatFileName("%sController")
                // Service 策略配置
                .serviceBuilder()
                .superServiceClass("com.arichina.BaseService")
                .superServiceImplClass("com.arichina.BasesServiceImpl")
                .formatServiceFileName("%sService")
                .formatServiceImplFileName("%sServiceImpl")
                // Mapper 策略配置
                .mapperBuilder()
                .superClass(mapperSupperClassName)
                .enableMapperAnnotation()
                .enableBaseResultMap()
                .enableBaseColumnList()
                .cache(MybatisMapperCache.class)
                .formatMapperFileName("%sMapper")
                .formatXmlFileName("%sMapper")
                .build();
        return strategyConfig;
    }

    private static InjectionConfig getInjectionConfig() {
        ConcurrentHashMap<String, Object> params = new ConcurrentHashMap<>();
        params.put("moduleName", moduleName);
        params.put("parentPackage", parent);
        InjectionConfig injectionConfig = new InjectionConfig.Builder()
                .beforeOutputFile((tableInfo, objectMap) -> {
                    System.out.println("tableInfo: " + "%s objectMap: " + objectMap.size());
                })
                .customMap(params)
                .fileOutConfigs(getFileOutConfigs())
                .build();
        return injectionConfig;
    }

    private static TemplateConfig getTemplateConfig() {
        TemplateConfig templateConfig = new TemplateConfig.Builder()
                .entity("/templates/entity.java")
                .service("/templates/service.java")
                .serviceImpl("/templates/serviceImpl.java")
                .mapper("/templates/mapper.java")
                .mapperXml("/templates/mapper.xml")
                .controller("/templates/controller.java")
                .disable()
                .build();
        return templateConfig;
    }

    private static PackageConfig getPackageConfig() {
        PackageConfig packageConfig = new PackageConfig.Builder()
                .parent(parent)
                .moduleName(moduleName)
                .entity("entity")
                .service("service")
                .serviceImpl("service.impl")
                .mapper("mappers")
                .xml("mappers")
                .controller("controller")
                .other("other")
                .pathInfo(Collections.singletonMap(OutputFile.mapperXml, "D://"))
                .build();
        return packageConfig;
    }

    private static GlobalConfig getGlobalConfig() {
        GlobalConfig globalConfig = new GlobalConfig.Builder()
                .outputDir(outputDir)
                .author(author)
                .enableSwagger()
                .dateType(DateType.TIME_PACK)
                .commentDate("yyyy-MM-dd")
                .disableOpenDir()
                .build();
        return globalConfig;
    }

    private static DataSourceConfig getDataSourceConfig() {
        DataSourceConfig dataSourceConfig = new DataSourceConfig.Builder(URL, USERNAME, PASSWORD)
                .dbQuery(new MySqlQuery())
                .typeConvert(new MybatisMySqlTypeConvert())
                .keyWordsHandler(new MySqlKeyWordsHandler())
                .build();
        return dataSourceConfig;
    }

    private static List<FileOutConfig> getFileOutConfigs() {

        String templatePath = "template/";
        String mavenPath = "/src/main/java/";
        String parentPath = parent.replace(".", "/") + "/";

        String clientGroup = "client/" + moduleName;
        String appGroup = "app/" + moduleName + "/executor";
        String appGroupConvert = "app/" + moduleName + "/convert";
        String domainGroup = "domain/model/" + moduleName;

        String entityTemplate = templatePath + "Do.java.vm";
        String mapperTemplate = templatePath + "mapper.java.vm";
        String mybatisPlusTemplate = templatePath + "MybatisPlus.java.vm";
        String xmlTemplate = templatePath + "mapper.xml.vm";
        String gatewayTemplate = templatePath + "Gateway.java.vm";
        String gatewayImplTemplate = templatePath + "GatewayImpl.java.vm";
        String repositoryTemplate = templatePath + "Repository.java.vm";
        String serviceTemplate = templatePath + "service.java.vm";
        String serviceImplTemplate = templatePath + "serviceImpl.java.vm";
        String controllerTemplate = templatePath + "controller.java.vm";


        String mapperPackage = "infrastructure.repository.mapper";
        String xmlPath = "resources/mappers";
        String rootPath = outputDir + "/";

        return Arrays.asList(
                new FileOutConfig(templatePath + "Enum.java.vm", parentPath + "common/enums/" + moduleName, "XxxEnum", "XxxEnum") {
                    @Override
                    public String outputFile(TableInfo tableInfo) {
                        // 自定义输入文件名称
                        return rootPath + commonProjectName + mavenPath + assembleClassName("", StringPool.DOT_JAVA);
                    }
                }


                // ==============================Do Start===========================================
                , new FileOutConfig(entityTemplate, parentPath + entityPackage.replace(".", "/"), "Do", entitySuffix) {
                    @Override
                    public String outputFile(TableInfo tableInfo) {
                        // 自定义输入文件名称
                        return rootPath + infrastructureProjectName + mavenPath + assembleClassName(tableInfo.getOriginEntityName(), StringPool.DOT_JAVA);
                    }
                }
                , new FileOutConfig(xmlTemplate, xmlPath, "mapperXml", "") {
                    @Override
                    public String outputFile(TableInfo tableInfo) {
                        // 自定义输入文件名称
                        return rootPath + infrastructureProjectName + "/src/main/" + assembleClassName(tableInfo.getXmlName(), StringPool.DOT_XML);
                    }
                }
                , new FileOutConfig(mapperTemplate, parentPath + mapperPackage.replace(".", "/"), "mapper", "") {
                    @Override
                    public String outputFile(TableInfo tableInfo) {
                        // 自定义输入文件名称
                        return rootPath + infrastructureProjectName + mavenPath + assembleClassName(tableInfo.getMapperName(), StringPool.DOT_JAVA);
                    }
                }
                , new FileOutConfig(mybatisPlusTemplate, parentPath + "infrastructure/mybatisplus", "MybatisPlus", "MybatisPlus") {
                    @Override
                    public String outputFile(TableInfo tableInfo) {
                        // 自定义输入文件名称
                        return rootPath + infrastructureProjectName + mavenPath + assembleClassName(tableInfo.getOriginEntityName(), StringPool.DOT_JAVA);
                    }
                }
                , new FileOutConfig(gatewayImplTemplate, parentPath + "infrastructure/gateway", "GatewayImpl", "GatewayImpl") {
                    @Override
                    public String outputFile(TableInfo tableInfo) {
                        // 自定义输入文件名称
                        return rootPath + infrastructureProjectName + mavenPath + assembleClassName(tableInfo.getOriginEntityName(), StringPool.DOT_JAVA);
                    }
                }
                , new FileOutConfig(repositoryTemplate, parentPath + "infrastructure/repository", "Repository", "Repository") {
                    @Override
                    public String outputFile(TableInfo tableInfo) {
                        // 自定义输入文件名称
                        return rootPath + infrastructureProjectName + mavenPath + assembleClassName(tableInfo.getOriginEntityName(), StringPool.DOT_JAVA);
                    }
                }
                , new FileOutConfig(templatePath + "Entity.java.vm", parentPath + domainGroup, "Entity", "Entity") {
                    @Override
                    public String outputFile(TableInfo tableInfo) {
                        // 自定义输入文件名称
                        return rootPath + domainProjectName + mavenPath + assembleClassName(tableInfo.getOriginEntityName(), StringPool.DOT_JAVA);
                    }
                }
                , new FileOutConfig(gatewayTemplate, parentPath + "domain/gateway", "Gateway", "Gateway") {
                    @Override
                    public String outputFile(TableInfo tableInfo) {
                        // 自定义输入文件名称
                        return rootPath + domainProjectName + mavenPath + assembleClassName(tableInfo.getOriginEntityName(), StringPool.DOT_JAVA);
                    }
                }
                // ==============================Do End===========================================

                // ==============================Co Start===========================================
                , new FileOutConfig(templatePath + "PageCo.java.vm", parentPath + clientGroup + "/view", "PageCo", "PageVo") {
                    @Override
                    public String outputFile(TableInfo tableInfo) {
                        // 自定义输入文件名称
                        return rootPath + clientProjectName + mavenPath + assembleClassName(tableInfo.getOriginEntityName(), StringPool.DOT_JAVA);
                    }
                }
                , new FileOutConfig(templatePath + "Co.java.vm", parentPath + clientGroup + "/view", "Co", "Vo") {
                    @Override
                    public String outputFile(TableInfo tableInfo) {
                        // 自定义输入文件名称
                        return rootPath + clientProjectName + mavenPath + assembleClassName(tableInfo.getOriginEntityName(), StringPool.DOT_JAVA);
                    }
                }
                , new FileOutConfig(templatePath + "Export.java.vm", parentPath + clientGroup + "/excel/template", "Export", "Export") {
                    @Override
                    public String outputFile(TableInfo tableInfo) {
                        // 自定义输入文件名称
                        return rootPath + clientProjectName + mavenPath + assembleClassName(tableInfo.getOriginEntityName(), StringPool.DOT_JAVA);
                    }
                }
                , new FileOutConfig(templatePath + "Import.java.vm", parentPath + clientGroup + "/excel/template", "Import", "Import") {
                    @Override
                    public String outputFile(TableInfo tableInfo) {
                        // 自定义输入文件名称
                        return rootPath + clientProjectName + mavenPath + assembleClassName(tableInfo.getOriginEntityName(), StringPool.DOT_JAVA);
                    }
                }
                // ==============================Co End===========================================

                // ==============================Cmd Start===========================================
                , new FileOutConfig(templatePath + "PageQry.java.vm", parentPath + clientGroup + "/request", "PageQry", "PageQry") {
                    @Override
                    public String outputFile(TableInfo tableInfo) {
                        // 自定义输入文件名称
                        return rootPath + clientProjectName + mavenPath + assembleClassName(tableInfo.getOriginEntityName(), StringPool.DOT_JAVA);
                    }
                }
                , new FileOutConfig(templatePath + "GetByIdQry.java.vm", parentPath + clientGroup + "/request", "GetByIdQry", "GetByIdQry") {
                    @Override
                    public String outputFile(TableInfo tableInfo) {
                        // 自定义输入文件名称
                        return rootPath + clientProjectName + mavenPath + assembleClassName(tableInfo.getOriginEntityName(), StringPool.DOT_JAVA);
                    }
                }
                , new FileOutConfig(templatePath + "DetailQry.java.vm", parentPath + clientGroup + "/request", "DetailQry", "DetailQry") {
                    @Override
                    public String outputFile(TableInfo tableInfo) {
                        // 自定义输入文件名称
                        return rootPath + clientProjectName + mavenPath + assembleClassName(tableInfo.getOriginEntityName(), StringPool.DOT_JAVA);
                    }
                }
                , new FileOutConfig(templatePath + "ExportQry.java.vm", parentPath + clientGroup + "/request", "ExportQry", "ExportQry") {
                    @Override
                    public String outputFile(TableInfo tableInfo) {
                        // 自定义输入文件名称
                        return rootPath + clientProjectName + mavenPath + assembleClassName(tableInfo.getOriginEntityName(), StringPool.DOT_JAVA);
                    }
                }
                , new FileOutConfig(templatePath + "ExportCmd.java.vm", parentPath + clientGroup + "/cmd", "ExportCmd", "ExportCmd") {
                    @Override
                    public String outputFile(TableInfo tableInfo) {
                        // 自定义输入文件名称
                        return rootPath + clientProjectName + mavenPath + assembleClassName(tableInfo.getOriginEntityName(), StringPool.DOT_JAVA);
                    }
                }
                , new FileOutConfig(templatePath + "ImportCmd.java.vm", parentPath + clientGroup + "/cmd", "ImportCmd", "ImportCmd") {
                    @Override
                    public String outputFile(TableInfo tableInfo) {
                        // 自定义输入文件名称
                        return rootPath + clientProjectName + mavenPath + assembleClassName(tableInfo.getOriginEntityName(), StringPool.DOT_JAVA);
                    }
                }
                /*, new FileOutConfig(templatePath + "CommonCmd.java.vm", parentPath + clientGroup, "CommonCmd") {
                    @Override
                    public String outputFile(TableInfo tableInfo) {
                        // 自定义输入文件名称
                        return rootPath + clientProjectName + mavenPath + assembleClassName(tableInfo.getOriginEntityName(), "CommonCmd", StringPool.DOT_JAVA);
                    }
                }*/
                , new FileOutConfig(templatePath + "AddCmd.java.vm", parentPath + clientGroup + "/cmd", "AddCmd", "AddCmd") {
                    @Override
                    public String outputFile(TableInfo tableInfo) {
                        // 自定义输入文件名称
                        return rootPath + clientProjectName + mavenPath + assembleClassName(tableInfo.getOriginEntityName(), StringPool.DOT_JAVA);
                    }
                }
                , new FileOutConfig(templatePath + "EditCmd.java.vm", parentPath + clientGroup + "/cmd", "EditCmd", "EditCmd") {
                    @Override
                    public String outputFile(TableInfo tableInfo) {
                        // 自定义输入文件名称
                        return rootPath + clientProjectName + mavenPath + assembleClassName(tableInfo.getOriginEntityName(), StringPool.DOT_JAVA);
                    }
                }
                , new FileOutConfig(templatePath + "DeleteCmd.java.vm", parentPath + clientGroup + "/cmd", "DeleteCmd", "DeleteCmd") {
                    @Override
                    public String outputFile(TableInfo tableInfo) {
                        // 自定义输入文件名称
                        return rootPath + clientProjectName + mavenPath + assembleClassName(tableInfo.getOriginEntityName(), StringPool.DOT_JAVA);
                    }
                }
                // ==============================Cmd End===========================================

                // ==============================Exe Start===========================================
                , new FileOutConfig(templatePath + "Convert.java.vm", parentPath + appGroupConvert, "Convert", "Convert") {
                    @Override
                    public String outputFile(TableInfo tableInfo) {
                        // 自定义输入文件名称
                        return rootPath + appProjectName + mavenPath + assembleClassName(tableInfo.getOriginEntityName(), StringPool.DOT_JAVA);
                    }
                }
                , new FileOutConfig(templatePath + "PageQryExe.java.vm", parentPath + appGroup + "/query", "PageQryExe", "PageQryExe") {
                    @Override
                    public String outputFile(TableInfo tableInfo) {
                        // 自定义输入文件名称
                        return rootPath + appProjectName + mavenPath + assembleClassName(tableInfo.getOriginEntityName(), StringPool.DOT_JAVA);
                    }
                }
                , new FileOutConfig(templatePath + "DetailQryExe.java.vm", parentPath + appGroup + "/query", "DetailQryExe", "DetailQryExe") {
                    @Override
                    public String outputFile(TableInfo tableInfo) {
                        // 自定义输入文件名称
                        return rootPath + appProjectName + mavenPath + assembleClassName(tableInfo.getOriginEntityName(), StringPool.DOT_JAVA);
                    }
                }
                , new FileOutConfig(templatePath + "ExportCmdExe.java.vm", parentPath + appGroup + "/excel", "ExportCmdExe", "ExportCmdExe") {
                    @Override
                    public String outputFile(TableInfo tableInfo) {
                        // 自定义输入文件名称
                        return rootPath + appProjectName + mavenPath + assembleClassName(tableInfo.getOriginEntityName(), StringPool.DOT_JAVA);
                    }
                }
                , new FileOutConfig(templatePath + "AddCmdExe.java.vm", parentPath + appGroup, "AddCmdExe", "AddCmdExe") {
                    @Override
                    public String outputFile(TableInfo tableInfo) {
                        // 自定义输入文件名称
                        return rootPath + appProjectName + mavenPath + assembleClassName(tableInfo.getOriginEntityName(), StringPool.DOT_JAVA);
                    }
                }
                , new FileOutConfig(templatePath + "EditCmdExe.java.vm", parentPath + appGroup, "EditCmdExe", "EditCmdExe") {
                    @Override
                    public String outputFile(TableInfo tableInfo) {
                        // 自定义输入文件名称
                        return rootPath + appProjectName + mavenPath + assembleClassName(tableInfo.getOriginEntityName(), StringPool.DOT_JAVA);
                    }
                }
                , new FileOutConfig(templatePath + "DeleteCmdExe.java.vm", parentPath + appGroup, "DeleteCmdExe", "DeleteCmdExe") {
                    @Override
                    public String outputFile(TableInfo tableInfo) {
                        // 自定义输入文件名称
                        return rootPath + appProjectName + mavenPath + assembleClassName(tableInfo.getOriginEntityName(), StringPool.DOT_JAVA);
                    }
                }
//                , new FileOutConfig(templatePath + "ImportCmdExe.java.vm", parentPath + appGroup + "/excel", "ImportCmdExe", "ImportCmdExe") {
//                    @Override
//                    public String outputFile(TableInfo tableInfo) {
//                        // 自定义输入文件名称
//                        return rootPath + appProjectName + mavenPath + assembleClassName(tableInfo.getOriginEntityName(), StringPool.DOT_JAVA);
//                    }
//                }
                , new FileOutConfig(templatePath + "ImportCmdExePlus.java.vm", parentPath + appGroup + "/excel", "ImportCmdExePlus", "ImportCmdExe") {
                    @Override
                    public String outputFile(TableInfo tableInfo) {
                        // 自定义输入文件名称
                        return rootPath + appProjectName + mavenPath + assembleClassName(tableInfo.getOriginEntityName(), StringPool.DOT_JAVA);
                    }
                }
                // ==============================Exe End===========================================

                , new FileOutConfig(serviceTemplate, parentPath + "client", "service", "") {
                    @Override
                    public String outputFile(TableInfo tableInfo) {
                        // 自定义输入文件名称
                        return rootPath + clientProjectName + mavenPath + assembleClassName(tableInfo.getServiceName(), StringPool.DOT_JAVA);
                    }
                }
                , new FileOutConfig(serviceImplTemplate, parentPath + "app", "serviceImpl", "") {
                    @Override
                    public String outputFile(TableInfo tableInfo) {
                        // 自定义输入文件名称
                        return rootPath + appProjectName + mavenPath + assembleClassName(tableInfo.getServiceImplName(), StringPool.DOT_JAVA);
                    }
                }
                , new FileOutConfig(controllerTemplate, parentPath + "adapter/web", "controller", "") {
                    @Override
                    public String outputFile(TableInfo tableInfo) {
                        // 自定义输入文件名称
                        return rootPath + controllerProjectName + mavenPath + assembleClassName(tableInfo.getControllerName(), StringPool.DOT_JAVA);
                    }
                }
        );
    }

    public GeneratorStart moduleName(String moduleName) {
        GeneratorStart.moduleName = moduleName;
        return this;
    }

    public GeneratorStart tables(String... tables) {
        GeneratorStart.tables = tables;
        return this;
    }

    public void doStart() {
        // 数据库配置(DataSourceConfig)
        DataSourceConfig dataSourceConfig = getDataSourceConfig();

        // 全局配置(GlobalConfig)
        GlobalConfig globalConfig = getGlobalConfig();

        // 包配置(PackageConfig)
        PackageConfig packageConfig = getPackageConfig();

        // 模板配置(TemplateConfig)
        TemplateConfig templateConfig = getTemplateConfig();

        // 注入配置(InjectionConfig)
        InjectionConfig injectionConfig = getInjectionConfig();

        // 策略配置(StrategyConfig)
        StrategyConfig strategyConfig = getStrategyConfig();

        new AutoGenerator(dataSourceConfig)
                .global(globalConfig)
                .packageInfo(packageConfig)
                .template(templateConfig)
                .injection(injectionConfig)
                .strategy(strategyConfig)
                .execute();
    }

}
