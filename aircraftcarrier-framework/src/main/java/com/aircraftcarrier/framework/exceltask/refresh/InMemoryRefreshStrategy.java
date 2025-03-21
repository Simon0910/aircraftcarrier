package com.aircraftcarrier.framework.exceltask.refresh;

import com.aircraftcarrier.framework.exceltask.ExcelUtil;
import com.aircraftcarrier.framework.exceltask.TaskConfig;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

/**
 * InMemoryRefreshStrategy
 *
 * @author zhipengliu
 * @date 2025/3/21
 * @since 1.0
 */
@Slf4j
public class InMemoryRefreshStrategy extends AbstractRefreshStrategy {

    private static Map<String, String> successMap;
    private static Map<String, String> errorMap;

    public InMemoryRefreshStrategy(TaskConfig config) {
        super(config);
    }

    @Override
    void doRefreshSuccessMapSnapshot(Map<String, String> successMap) throws Exception {
        InMemoryRefreshStrategy.successMap = successMap;
    }

    @Override
    void doRefreshErrorMapSnapshot(Map<String, String> errorMap) throws Exception {
        InMemoryRefreshStrategy.errorMap = errorMap;
    }

    @Override
    void close() throws Exception {
        InMemoryRefreshStrategy.successMap = null;
        InMemoryRefreshStrategy.errorMap = null;
    }

    @Override
    public void preHandle() throws Exception {

    }

    @Override
    public String loadSuccessMapSnapshot() throws Exception {
        if (successMap == null && successMap.isEmpty()) {
            return null;
        }
        String maxSuccessSnapshotPosition = "0_0";
        for (String next : successMap.values()) {
            if (ExcelUtil.comparePosition(maxSuccessSnapshotPosition, next) < 0) {
                maxSuccessSnapshotPosition = next;
            }
        }
        log.info("init - maxSuccessSnapshotPosition {}", maxSuccessSnapshotPosition);
        return maxSuccessSnapshotPosition;
    }

    @Override
    public Map<String, String> loadErrorMapSnapshot() throws Exception {
        return errorMap;
    }
}
