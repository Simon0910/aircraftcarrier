package com.aircraftcarrier.framework.exceltask.refresh;

import com.aircraftcarrier.framework.concurrent.ThreadUtil;
import com.aircraftcarrier.framework.exceltask.ExcelUtil;
import com.aircraftcarrier.framework.exceltask.Task;
import com.aircraftcarrier.framework.exceltask.TaskConfig;
import com.google.common.collect.Maps;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * InMemoryRefreshStrategy
 *
 * @author zhipengliu
 * @date 2025/3/21
 * @since 1.0
 */
@Slf4j
public class InMemoryRefreshStrategy extends AbstractRefreshStrategy {

    private static Map<String, Map<String, String>> successContainer = new ConcurrentHashMap<>();
    private static Map<String, Map<String, String>> errorContainer = new ConcurrentHashMap<>();

    private Map<String, String> successMap;
    private Map<String, String> errorMap;

    public InMemoryRefreshStrategy(TaskConfig config) {
        super(config);
    }

    @Override
    void doRefreshSuccessMapSnapshot(Map<String, String> successMap) throws Exception {
        this.successMap.putAll(successMap);
    }

    @Override
    void doRefreshErrorMapSnapshot(Map<String, String> errorMap) throws Exception {
        this.errorMap.putAll(errorMap);
    }

    @Override
    void close() throws Exception {
    }

    @Override
    public void preHandle(Task<?> task) {
        String simpleName = task.getClass().getSimpleName();
        this.successMap = successContainer.computeIfAbsent(simpleName, k -> new HashMap<>());
        this.errorMap = errorContainer.computeIfAbsent(simpleName, k -> new HashMap<>());
    }

    @Override
    public String loadSuccessMapSnapshot() throws Exception {
        if (successMap == null || successMap.isEmpty()) {
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
        if (this.errorMap == null || this.errorMap.isEmpty()) {
            return HashMap.newHashMap(16);
        }

        Map<String, String> map = Maps.newHashMapWithExpectedSize(this.errorMap.size());
        map.putAll(this.errorMap);
        return map;
    }

    @Override
    public void reset() {
        if (successMap != null && !successMap.isEmpty()) {
            this.successMap.clear();
        }
        if (errorMap != null && !errorMap.isEmpty()) {
            this.errorMap.clear();
        }

    }

    @Override
    public void resetSuccessSheetRow(String maxSuccessSheetRow) throws IOException {
        if (successMap != null) {
            this.successMap.clear();
            this.successMap.put(ThreadUtil.getThreadNo(), maxSuccessSheetRow);
        }

    }
}
