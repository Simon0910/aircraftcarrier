package com.aircraftcarrier.marketing.store.infrastructure.utils;

import com.aircraftcarrier.framework.tookit.MapUtil;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.nio.file.Paths;
import java.util.Map;

/**
 * @author lwc
 * @date 2022/5/10
 */
@Slf4j
@Getter
@Setter
@Configuration
public class OssFileHelper implements InitializingBean {

    /**
     * upload to local file path
     */
    @Value("${oss.amazonS3.uploadFilePath:}")
    private String uploadFilePath;

    /**
     * bucketName
     * example: img-dev
     */
    @Value("${oss.amazonS3.bucketName:}")
    private String bucketName;

    /**
     * AmazonS3
     */
    private static AmazonS3 amazonS3;

    /**
     * 文件上传大小限制(B)
     */
    private static final Map<String, Long> TYPE_SIZE_MAP = MapUtil.newHashMap(8);

    /**
     * 文件类型
     */
    private static final Map<String, Integer> FILE_TYPE_MAP = MapUtil.newHashMap(8);

    static {
        TYPE_SIZE_MAP.put("doc", 20971520L);
        TYPE_SIZE_MAP.put("docx", 20971520L);
        TYPE_SIZE_MAP.put("xls", 20971520L);
        TYPE_SIZE_MAP.put("xlsx", 20971520L);
        TYPE_SIZE_MAP.put("ppt", 20971520L);
        TYPE_SIZE_MAP.put("pptx", 20971520L);
        TYPE_SIZE_MAP.put("pdf", 20971520L);
        TYPE_SIZE_MAP.put("txt", 20971520L);

        FILE_TYPE_MAP.put("doc", 0);
        FILE_TYPE_MAP.put("docx", 0);
        FILE_TYPE_MAP.put("xls", 1);
        FILE_TYPE_MAP.put("xlsx", 1);
        FILE_TYPE_MAP.put("ppt", 2);
        FILE_TYPE_MAP.put("pptx", 2);
        FILE_TYPE_MAP.put("pdf", 3);
        FILE_TYPE_MAP.put("txt", 3);
    }

    /**
     * 上传文件
     *
     * @param filePath filePath
     * @return String
     */
    public String uploadFile(String filePath) throws FileNotFoundException {
        String key = Paths.get(filePath).getFileName().toString();

        //获取输入流
        InputStream inputStream = new FileInputStream(filePath);

        ObjectMetadata objectMetadata = new ObjectMetadata();
        objectMetadata.setContentType("multipart/form-data");
        objectMetadata.setContentLength(new File(filePath).length());

        //上传文件流
        try {
            amazonS3.putObject(bucketName, key, inputStream, objectMetadata);
            log.info("Uploading {} to OSS bucket {}...", key, bucketName);
        } catch (AmazonServiceException e) {
            e.printStackTrace();
        }
        return key;
    }


    /**
     * 下载文件
     *
     * @param response response
     * @param key      key
     * @param fileName fileName
     */
    public void downloadFile(HttpServletResponse response, String key, String fileName) {
        log.info("Downloading {} from S3 bucket {}...", key, bucketName);

        try {
            OutputStream fos = response.getOutputStream();
            // 清空缓冲区，状态码和响应头(headers)
            response.reset();
            // 设置ContentType，响应内容为二进制数据流，编码为utf-8，此处设定的编码是文件内容的编码
            response.setContentType("application/octet-stream;charset=utf-8");
            // 以（Content-Disposition: attachment; filename="filename.jpg"）格式设定默认文件名，设定utf编码，此处的编码是文件名的编码，使能正确显示中文文件名
            response.setHeader("Content-Disposition", "attachment;fileName=" + fileName + ";filename*=utf-8''" + URLEncoder.encode(fileName, "utf-8"));

            S3Object obj = amazonS3.getObject(bucketName, key);
            S3ObjectInputStream s3is = obj.getObjectContent();
            byte[] readBuffer = new byte[1024];
            int readLength;
            while ((readLength = s3is.read(readBuffer)) > 0) {
                fos.write(readBuffer, 0, readLength);
            }
            s3is.close();
            fos.close();
        } catch (Exception e) {
            log.error(e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 禁止static块 初始化逻辑代码
     *
     * @throws Exception
     */
    @Override
    public void afterPropertiesSet() throws Exception {
        /*ResourceBundle bundle = ResourceBundle.getBundle("project");
        final String accessKey = bundle.getString("oss.amazonS3.accesskey");
        final String secretKey = bundle.getString("oss.amazonS3.secretkey");
        final String endpoint = bundle.getString("oss.amazonS3.endpoint");
        final String region = bundle.getString("oss.amazonS3.region");
        ClientConfiguration config = new ClientConfiguration();

        AwsClientBuilder.EndpointConfiguration endpointConfig =
                new AwsClientBuilder.EndpointConfiguration(endpoint, region);

        AWSCredentials awsCredentials = new BasicAWSCredentials(accessKey, secretKey);
        AWSCredentialsProvider awsCredentialsProvider = new AWSStaticCredentialsProvider(awsCredentials);

        amazonS3 = AmazonS3Client.builder()
                .withEndpointConfiguration(endpointConfig)
                .withClientConfiguration(config)
                .withCredentials(awsCredentialsProvider)
                .disableChunkedEncoding()
                .build();*/
    }
}
