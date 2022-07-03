package com.aircraftcarrier.framework.data.component;

import cn.hutool.core.thread.ThreadUtil;
import com.aircraftcarrier.framework.tookit.StringPool;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.builder.xml.XMLMapperBuilder;
import org.apache.ibatis.executor.ErrorContext;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSessionFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.NestedIOException;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.util.ClassUtils;
import org.springframework.util.ReflectionUtils;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * 刷新MyBatis Mapper XML 线程
 *
 * @author lzp
 * @version 2021-3-3
 */
@Slf4j
@ConditionalOnProperty(prefix = "mybatis.mapper.refresh", value = "enabled", havingValue = "true")
@Component
public class MapperRefresh implements ApplicationContextAware {

    private static final String XML_RESOURCE_PATTERN = "**/*.xml";
    /**
     * 刷新启用后，是否启动了刷新线程
     */
    private static boolean refresh;
    /**
     * 是否启用Mapper刷新线程功能
     */
    @Value("${mybatis.mapper.refresh.enabled:false}")
    private Boolean enabled;
    @Value("${mybatis.mapper.refresh.basePackage:/mappers}")
    private String basePackage;
    /**
     * 延迟刷新秒数
     */
    @Value("${mybatis.mapper.refresh.delaySeconds:5}")
    private Integer delaySeconds;

    private ApplicationContext applicationContext;
    /**
     * Mapper实际资源路径
     */
    private Set<String> location;
    /**
     * Mapper资源路径
     */
    private Resource[] mapperLocations;
    /**
     * MyBatis配置对象
     */
    private Configuration configuration;
    /**
     * 上一次刷新时间
     */
    private Long beforeTime = 0L;
    /**
     * 休眠时间
     */
    @Value("${mybatis.mapper.refresh.sleepSeconds:5}")
    private Integer sleepSeconds;
    /**
     * xml文件夹匹配字符串，需要根据需要修改
     */
    @Value("${mybatis.mapper.refresh.mappingPath:\\}")
    private String mappingPath;

    public static boolean isRefresh() {
        return refresh;
    }

    @PostConstruct
    public void start() throws IOException {
        log.debug("[enabled] " + enabled);
        log.debug("[basePackage] " + basePackage);
        log.debug("[delaySeconds] " + delaySeconds);
        log.debug("[sleepSeconds] " + sleepSeconds);
        log.debug("[mappingPath] " + mappingPath);
        if (Boolean.FALSE.equals(enabled)) {
            return;
        }

        SqlSessionFactory factory = applicationContext.getBean(SqlSessionFactory.class);
        this.configuration = factory.getConfiguration();
        mapperLocations = getResource(basePackage, XML_RESOURCE_PATTERN);
        exeTask();
    }

    public void exeTask() {
        beforeTime = System.currentTimeMillis();

        log.debug("[location] " + location);
        log.debug("[configuration] " + configuration);

        if (Boolean.TRUE.equals(enabled)) {
            // 启动刷新线程
            final MapperRefresh runnable = this;
            ThreadUtil.execute(() -> {
                if (location == null) {
                    location = new HashSet<>();
                    log.debug("MapperLocation's length:" + mapperLocations.length);
                    for (Resource mapperLocation : mapperLocations) {
                        String s = mapperLocation.toString().replace("\\\\", StringPool.SLASH);
                        s = s.substring("file [".length(), s.lastIndexOf(mappingPath) + mappingPath.length());
                        if (!location.contains(s)) {
                            location.add(s);
                            log.debug("Location:" + s);
                        }
                    }
                    log.debug("Location's size:" + location.size());
                }

                try {
                    Thread.sleep(delaySeconds * 1000L);
                } catch (InterruptedException e2) {
                    e2.printStackTrace();
                    Thread.currentThread().interrupt();
                }
                refresh = true;

                log.info("========= Enabled refresh mybatis mapper =========");

                while (true) {
                    try {
                        for (String s : location) {
                            runnable.refresh(s, beforeTime);
                        }
                    } catch (Exception e1) {
                        e1.printStackTrace();
                    }
                    try {
                        Thread.sleep(sleepSeconds * 1000L);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                        Thread.currentThread().interrupt();
                    }

                }
            });
        }
    }

    /**
     * 执行刷新
     *
     * @param filePath   刷新目录
     * @param beforeTime 上次刷新时间
     * @throws NestedIOException 解析异常
     * @author ThinkGem
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    private void refresh(String filePath, Long beforeTime) throws NestedIOException {

        // 本次刷新时间
        long refreshTime = System.currentTimeMillis();

        // 获取需要刷新的Mapper文件列表
        List<File> fileList = this.getRefreshFile(new File(filePath), beforeTime);
        if (!fileList.isEmpty()) {
            log.debug("Refresh file: " + fileList.size());
        }
        for (File file : fileList) {
            String resource = file.getAbsolutePath();
            try (InputStream inputStream = new FileInputStream(file)) {
                // 清理原有资源，更新为自己的StrictMap方便，增量重新加载
                String[] mapFieldNames = new String[]{
                        "mappedStatements", "caches",
                        "resultMaps", "parameterMaps",
                        "keyGenerators", "sqlFragments"
                };
                for (String fieldName : mapFieldNames) {
                    Field field = configuration.getClass().getDeclaredField(fieldName);
                    ReflectionUtils.makeAccessible(field);
                    Map<String, Object> map = ((Map) field.get(configuration));
                    if (!(map instanceof StrictMap)) {
                        Map newMap = new StrictMap(org.springframework.util.StringUtils.capitalize(fieldName) + "collection");
                        for (Map.Entry<String, Object> entry : map.entrySet()) {
                            newMap.put(entry.getKey(), map.get(entry.getKey()));
                        }
                        field.set(configuration, newMap);
                    }
                }

                // 清理已加载的资源标识，方便让它重新加载。
                boolean isSupper = configuration.getClass().getSuperclass() == Configuration.class;
                Field field = isSupper ?
                        configuration.getClass().getSuperclass().getDeclaredField("loadedResources")
                        : configuration.getClass().getDeclaredField("loadedResources");
                ReflectionUtils.makeAccessible(field);
                Set loadedResourcesSet = ((Set) field.get(configuration));
                loadedResourcesSet.remove(resource);

                //重新编译加载资源文件。
                XMLMapperBuilder xmlMapperBuilder = new XMLMapperBuilder(inputStream, configuration,
                        resource, configuration.getSqlFragments());
                xmlMapperBuilder.parse();
            } catch (IOException | NoSuchFieldException | IllegalAccessException e) {
                e.printStackTrace();
                throw new NestedIOException("Failed to parse mapping resource: '" + resource + "'", e);
            } finally {
                ErrorContext.instance().reset();
            }


            if (log.isDebugEnabled()) {
                log.debug("Refresh file: " + file.getAbsolutePath());
                log.debug("Refresh filename: " + file.getName());
            }
        }

        if (!fileList.isEmpty()) {
            this.beforeTime = refreshTime;
        }
    }

    /**
     * 获取需要刷新的文件列表
     *
     * @param dir        目录
     * @param beforeTime 上次刷新时间
     * @return 刷新文件列表
     */
    private List<File> getRefreshFile(File dir, Long beforeTime) {
        List<File> fileList = new ArrayList<>();

        File[] files = dir.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    fileList.addAll(this.getRefreshFile(file, beforeTime));
                } else if (file.isFile()) {
                    if (this.checkFile(file, beforeTime)) {
                        fileList.add(file);
                    }
                } else {
                    log.info("Error file. {}", file.getName());
                }
            }
        }
        return fileList;
    }

    /**
     * 判断文件是否需要刷新
     *
     * @param file       文件
     * @param beforeTime 上次刷新时间
     * @return 需要刷新返回true，否则返回false
     */
    private boolean checkFile(File file, Long beforeTime) {
        return file.lastModified() > beforeTime;
    }

    @Override
    public void setApplicationContext(@NonNull ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    public Resource[] getResource(String basePackage, String pattern) throws IOException {
        String packageSearchPath = ResourcePatternResolver.CLASSPATH_ALL_URL_PREFIX
                + ClassUtils.convertClassNameToResourcePath(applicationContext.getEnvironment().resolveRequiredPlaceholders(
                basePackage)) + StringPool.SLASH + pattern;
        ResourcePatternResolver resourcePatternResolver = new PathMatchingResourcePatternResolver();
        return resourcePatternResolver.getResources(packageSearchPath);
    }

    /**
     * 重写 org.apache.ibatis.session.Configuration.StrictMap 类
     * 来自 MyBatis3.4.0版本，修改 put 方法，允许反复 put更新。
     */
    public static class StrictMap<V> extends HashMap<String, V> {

        private static final long serialVersionUID = -4950446264854982944L;
        private final String name;

        public StrictMap(String name, int initialCapacity, float loadFactor) {
            super(initialCapacity, loadFactor);
            this.name = name;
        }

        public StrictMap(String name, int initialCapacity) {
            super(initialCapacity);
            this.name = name;
        }

        public StrictMap(String name) {
            super();
            this.name = name;
        }

        public StrictMap(String name, Map<String, ? extends V> m) {
            super(m);
            this.name = name;
        }

        @Override
        @SuppressWarnings("unchecked")
        public V put(String key, V value) {
            // ThinkGem 如果现在状态为刷新，则刷新(先删除后添加)
            if (MapperRefresh.isRefresh()) {
                remove(key);
            }
            // ThinkGem end
            if (containsKey(key)) {
                throw new IllegalArgumentException(name + " already contains value for " + key);
            }
            if (key.contains(StringPool.DOT)) {
                final String shortKey = getShortName(key);
                if (super.get(shortKey) == null) {
                    super.put(shortKey, value);
                } else {
                    super.put(shortKey, (V) new Ambiguity(shortKey));
                }
            }
            return super.put(key, value);
        }

        @Override
        public V get(Object key) {
            V value = super.get(key);
            if (value == null) {
                throw new IllegalArgumentException(name + " does not contain value for " + key);
            }
            if (value instanceof Ambiguity) {
                throw new IllegalArgumentException(((Ambiguity) value).getSubject() + " is ambiguous in " + name
                        + " (try using the full name including the namespace, or rename one of the entries)");
            }
            return value;
        }

        private String getShortName(String key) {
            final String[] keyParts = key.split("\\.");
            return keyParts[keyParts.length - 1];
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            if (!super.equals(o)) {
                return false;
            }
            StrictMap<?> strictMap = (StrictMap<?>) o;
            return Objects.equals(name, strictMap.name);
        }

        @Override
        public int hashCode() {
            return Objects.hash(super.hashCode(), name);
        }

        protected static class Ambiguity {
            private final String subject;

            public Ambiguity(String subject) {
                this.subject = subject;
            }

            public String getSubject() {
                return subject;
            }
        }
    }


//    /**
//     * 刷新mapper
//     * https://vimsky.com/examples/detail/java-method-org.springframework.core.io.Resource.toString.html
//     * @throws Exception
//     */
//    @SuppressWarnings("rawtypes")
//    private void refresh(Resource resource) throws ClassNotFoundException, NoSuchFieldException, IllegalAccessException {
//        this.configuration = sqlSessionFactory.getConfiguration();
//        boolean isSupper = configuration.getClass().getSuperclass() == Configuration.class;
//        try {
//            Field field = isSupper ? configuration.getClass().getSuperclass().getDeclaredField("loadedResources")
//                    : configuration.getClass().getDeclaredField("loadedResources");
//            ReflectionUtils.makeAccessible(field);
//            Set loadedResourcesSet = ((Set) field.get(configuration));
//            XPathParser xPathParser = new XPathParser(resource.getInputStream(), true, configuration.getVariables(),
//                    new XMLMapperEntityResolver());
//            XNode context = xPathParser.evalNode("/mapper");
//            String namespace = context.getStringAttribute("namespace");
//            field = MapperRegistry.class.getDeclaredField("knownMappers");
//            ReflectionUtils.makeAccessible(field);
//            Map mapConfig = (Map) field.get(configuration.getMapperRegistry());
//            mapConfig.remove(Resources.classForName(namespace));
//            loadedResourcesSet.remove(resource.toString());
//            configuration.getCacheNames().remove(namespace);
//            cleanParameterMap(context.evalNodes("/mapper/parameterMap"), namespace);
//            cleanResultMap(context.evalNodes("/mapper/resultMap"), namespace);
//            cleanKeyGenerators(context.evalNodes("insert|update"), namespace);
//            cleanSqlElement(context.evalNodes("/mapper/sql"), namespace);
//            XMLMapperBuilder xmlMapperBuilder = new XMLMapperBuilder(resource.getInputStream(),
//                    sqlSessionFactory.getConfiguration(), // 注入的sql先不进行处理了
//                    resource.toString(), sqlSessionFactory.getConfiguration().getSqlFragments());
//            xmlMapperBuilder.parse();
//            logger.debug("refresh: '" + resource + "', success!");
//        } catch (IOException e) {
//            logger.error("Refresh IOException :" + e.getMessage());
//        } finally {
//            ErrorContext.instance().reset();
//        }
//    }

}

