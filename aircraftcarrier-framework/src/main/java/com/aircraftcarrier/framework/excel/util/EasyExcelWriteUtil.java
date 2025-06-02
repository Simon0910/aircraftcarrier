package com.aircraftcarrier.framework.excel.util;

import cn.hutool.core.collection.CollUtil;
import com.aircraftcarrier.framework.excel.CountingOutputStream;
import com.aircraftcarrier.framework.excel.convert.BigDecimalConvert;
import com.aircraftcarrier.framework.excel.handler.*;
import com.aircraftcarrier.framework.excel.strategy.StyleStrategy;
import com.alibaba.excel.EasyExcelFactory;
import com.alibaba.excel.write.builder.ExcelWriterBuilder;
import com.alibaba.excel.write.handler.WriteHandler;
import com.alibaba.excel.write.style.column.LongestMatchColumnWidthStyleStrategy;
import org.springframework.beans.BeanUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.GZIPOutputStream;


/**
 * excel工具类
 *
 * @author Mark sunlightcs@gmail.com
 */
public class EasyExcelWriteUtil {
    private static final ThreadLocal<HttpServletRequest> LOCAL_REQUEST = new ThreadLocal<>();
    private static final String XLSX_MEDIA_TYPE =
            MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet").toString();

    private EasyExcelWriteUtil() {
    }

    /**
     * 统一格式导出
     *
     * @param response   response
     * @param fileName   fileName
     * @param sheetName  sheetName
     * @param sourceList sourceList
     * @param modelClass modelClass
     * @param <T>        T
     * @throws Exception e
     */
    public static <T> void exportExcel(HttpServletResponse response,
                                       String fileName,
                                       String sheetName,
                                       List<?> sourceList,
                                       Class<T> modelClass) throws Exception {
        exportExcelToTarget(response, fileName, sheetName, sourceList, modelClass,
                new LongestMatchColumnWidthStyleStrategy(),
                StyleStrategy.customHorizontalCellStyleStrategy(),
                new DropDownSheetWriteHandler(modelClass),
                new ExcelSizeSheetWriteHandler(modelClass),
                new NumberSheetWriteHandler(modelClass),
                new BigDecimalSheetWriteHandler(modelClass),
                new CommentRowWriteHandler(modelClass));
    }

    /**
     * Excel导出，先sourceList转换成List<targetClass>，再导出
     *
     * @param response   response
     * @param fileName   文件名
     * @param sheetName  sheetName
     * @param sourceList 原数据List
     * @param modelClass 目标对象Class
     */
    public static <T> void exportExcelToTarget(HttpServletResponse response,
                                               String fileName,
                                               String sheetName,
                                               List<?> sourceList,
                                               Class<T> modelClass,
                                               WriteHandler... writeHandlers) throws NoSuchMethodException, IOException, InvocationTargetException, InstantiationException, IllegalAccessException {
        if (CollUtil.isEmpty(sourceList)) {
            sourceList = new ArrayList<>();
        }

        if (!sourceList.isEmpty() && sourceList.get(0).getClass() != modelClass) {
            Constructor<T> constructor = modelClass.getDeclaredConstructor();
            List<T> targetList = new ArrayList<>(sourceList.size());
            for (Object source : sourceList) {
                T target = constructor.newInstance();
                BeanUtils.copyProperties(source, target);
                targetList.add(target);
            }
            exportExcel(response, fileName, sheetName, targetList, modelClass, writeHandlers);
        } else {
            exportExcel(response, fileName, sheetName, sourceList, modelClass, writeHandlers);
        }
    }

    /**
     * Excel导出
     *
     * @param response      response
     * @param fileName      文件名
     * @param sheetName     sheetName
     * @param list          数据List
     * @param modelClass    对象Class
     * @param writeHandlers writeHandlers
     */
    public static <T> void exportExcel(HttpServletResponse response,
                                       String fileName,
                                       String sheetName,
                                       List<?> list,
                                       Class<T> modelClass,
                                       WriteHandler... writeHandlers) throws IOException {
        // download
        downloadSetting(response, fileName);

        OutputStream servletOutputStream = response.getOutputStream();

        // 下载进度
        OutputStream countingOutputStream = wrapperProgressOutputStream(servletOutputStream);
        // 启用GZIP压缩
        response.setHeader(HttpHeaders.CONTENT_ENCODING, "gzip");
        OutputStream gzipOutputStream = new GZIPOutputStream(countingOutputStream);

        ExcelWriterBuilder excelWriterBuilder = EasyExcelFactory.write(gzipOutputStream, modelClass);

        // write excel convert
        excelWriterBuilder.registerConverter(new BigDecimalConvert());

        if (writeHandlers != null) {
            for (WriteHandler writeHandler : writeHandlers) {
                excelWriterBuilder.registerWriteHandler(writeHandler);
            }
        }
        excelWriterBuilder.sheet(sheetName).doWrite(list);
    }


    private static void downloadSetting(HttpServletResponse response, String fileName) throws UnsupportedEncodingException {
        // 在生成文件名时过滤非法字符
        fileName = fileName.replaceAll("[\\\\/:*?\"<>|]", "").trim();

        // 生成带时间戳的文件名
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        fileName = fileName + "_" + timestamp;
        if (!fileName.toLowerCase().endsWith(".xlsx")) {
            fileName += ".xlsx";
        }

        response.setCharacterEncoding(StandardCharsets.UTF_8.displayName());
        // 设置响应内容类型
        response.setContentType(XLSX_MEDIA_TYPE);

        // String version = getVersion(getHttpServletRequest());

        // 设置 Content-Disposition 响应头（处理浏览器兼容性）
//        ContentDisposition contentDisposition = ContentDisposition.attachment()
////                .filename(fileName, StandardCharsets.US_ASCII)  // 兼容旧浏览器
//                .filename(fileName, StandardCharsets.UTF_8) // 现代浏览器
//                .build();
//        response.setHeader(HttpHeaders.CONTENT_DISPOSITION, contentDisposition.toString());

        fileName = URLEncoder.encode(fileName, StandardCharsets.UTF_8.displayName()).replace("+", "%20");
        response.setHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment;filename=" + fileName);

        // 禁用缓存防止敏感数据留存
        // 防止 MIME 类型嗅探攻击
        // 内容安全策略限制
        // 防止点击劫持
        // 4. 禁用缓存
        response.setHeader(HttpHeaders.CACHE_CONTROL, "no-store, no-cache, must-revalidate");
        response.setHeader(HttpHeaders.PRAGMA, "no-cache");
        response.setHeader(HttpHeaders.EXPIRES, "0");

        // 5. 设置安全相关响应头
        response.setHeader("X-Content-Type-Options", "nosniff");
        response.setHeader("Content-Security-Policy", "default-src 'none'");
        response.setHeader("X-Frame-Options", "DENY");

        // 提供文件类型元数据
        response.setHeader("X-File-Type", "Excel Workbook");
        response.setHeader("X-File-Version", "Office 2007+");

        // 设置下载速度限制（防止带宽滥用）
        response.setBufferSize(1024 * 32); // 32KB缓冲区
    }

    /**
     * // 设置内容长度（显示进度条）
     * response.setHeader(HttpHeaders.CONTENT_LENGTH, String.valueOf(excelData.length));
     */
    private static CountingOutputStream wrapperProgressOutputStream(OutputStream outputStream) {
        // 创建进度监听输出流
        return new CountingOutputStream(outputStream) {
            @Override
            protected void afterWrite(int n) throws IOException {
                // 获取已写入字节数
                long bytesWritten = getCount();
                // 这里可以发送进度到前端 (实际项目中通过WebSocket或轮询)
                System.out.println("已写入: " + bytesWritten + " 字节");
            }
        };
    }


    /**
     * 处理文件名编码（解决中文乱码问题）
     *
     * @param request HttpServletRequest 对象
     * @return 编码后的文件名
     */
    private static String getVersion(HttpServletRequest request) {
        if (request == null) {
            return "0";
        }
        // 3. 浏览器检测
        String userAgent = request.getHeader("User-Agent").toLowerCase();

        // 4. 现代浏览器处理 (Chrome, Firefox, Edge, Safari 15+)
        if (userAgent.contains("chrome") ||
                userAgent.contains("firefox") ||
                userAgent.contains("safari") ||
                userAgent.contains("edge")) {
            // RFC 5987 标准格式
            return "0";
        }
        // 5. IE 浏览器处理
        else if (userAgent.contains("msie") || userAgent.contains("trident")) {
            // 使用 GBK 编码解决中文问题
            return "1";
        }
        // 6. 其他浏览器/旧版 Safari
        else {
            // ISO 编码回退方案
            return "2";
        }
    }

    private static HttpServletRequest getHttpServletRequest() {
        HttpServletRequest httpServletRequest = LOCAL_REQUEST.get();
        if (httpServletRequest != null) {
            clear();
        }
        return httpServletRequest;
    }

    public static void setHttpServletRequest(HttpServletRequest request) {
        LOCAL_REQUEST.set(request);
    }

    private static void clear() {
        LOCAL_REQUEST.remove();
    }
}
