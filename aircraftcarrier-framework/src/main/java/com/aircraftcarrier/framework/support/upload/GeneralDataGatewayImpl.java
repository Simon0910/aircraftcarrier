package com.aircraftcarrier.framework.support.upload;

import com.aircraftcarrier.framework.exception.SysException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.util.List;

/**
 * 数据接口Gateway实现
 *
 * @author lzp
 * @version 1.0
 * @date 2020/6/16
 */
@Slf4j
@Service
public class GeneralDataGatewayImpl implements GeneralDataGateway {

    @Override
    public <T> T downloadObject(String resultId, Class<T> responseType) {
        log.info("downloadObject [{}] from JFS want [{}] start...", resultId, responseType);
        long start = System.currentTimeMillis();
        T t;
        try (InputStream inputStream = getStorageObject(resultId);
             ObjectInputStream ois = new ObjectInputStream(inputStream)) {
            Object object = ois.readObject();
            if (!responseType.isInstance(object)) {
                log.error("object [{}] cannot be cast to [{}]", object.getClass(), responseType);
                throw new SysException("object [" + object.getClass().getSimpleName() + "] cannot be cast to [" + responseType.getSimpleName() + "]");
            }
            t = responseType.cast(object);
        } catch (IOException | ClassNotFoundException e) {
            log.error("downloadObject [{}] error", responseType, e);
            throw new SysException(String.format("downloadObject [%s] error", responseType.getSimpleName()));
        }
        log.info("downloadObject end 耗时[{}]", System.currentTimeMillis() - start);
        return t;
    }


    @Override
    public <T> List<T> downloadList(String resultId, Class<T> responseType) {
        log.info("downloadList [{}] from JFS want [{}] start...", resultId, responseType);
        long start = System.currentTimeMillis();
        List<T> list;
        try (InputStream inputStream = getStorageObject(resultId);
             ObjectInputStream ois = new ObjectInputStream(inputStream)) {
            Object object = ois.readObject();
            if (!(object instanceof List)) {
                log.error("object [{}] cannot be cast to List<{}>", object.getClass(), responseType);
                throw new SysException("object [" + object.getClass().getSimpleName() + "] cannot be cast to List<" + responseType.getSimpleName() + ">");
            }
            List<Object> objList = (List<Object>) object;
            Object obj = objList.get(0);
            if (!responseType.isInstance(obj)) {
                log.error("element [{}] cannot be cast to [{}]", obj.getClass(), responseType);
                throw new SysException("element [" + obj.getClass().getSimpleName() + "] cannot be cast to [" + responseType.getSimpleName() + "]");
            }
            list = (List<T>) objList;
        } catch (IOException | ClassNotFoundException e) {
            log.error("downloadList [{}] error", responseType, e);
            throw new SysException(String.format("downloadList [%s] error", responseType.getSimpleName()));
        }
        log.info("downloadList end 耗时[{}]", System.currentTimeMillis() - start);
        return list;
    }


    /**
     * 获取存储对象
     */
    private InputStream getStorageObject(String fileJfs) {
        try {
            return new InputStream() {
                @Override
                public int read() throws IOException {
                    return 0;
                }
            };
        } catch (Exception e) {
            log.error("getStorageObject [{}] error: ", fileJfs, e);
            throw new SysException(String.format("getStorageObject [%s] error", fileJfs));
        }
    }
}