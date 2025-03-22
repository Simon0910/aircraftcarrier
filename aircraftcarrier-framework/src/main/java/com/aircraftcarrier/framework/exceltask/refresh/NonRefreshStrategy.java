package com.aircraftcarrier.framework.exceltask.refresh;

import com.aircraftcarrier.framework.exceltask.Task;
import com.aircraftcarrier.framework.exceltask.TaskConfig;

import java.io.IOException;
import java.util.Map;

/**
 * @author ext.liuzhipeng15
 * @since 2024/9/5
 */
public class NonRefreshStrategy extends AbstractRefreshStrategy {

    public NonRefreshStrategy(TaskConfig config) {
        super(config);
    }

    @Override
    void doRefreshSuccessMapSnapshot(Map<String, String> successMap) throws Exception {

    }

    @Override
    void doRefreshErrorMapSnapshot(Map<String, String> errorMap) throws Exception {

    }

    @Override
    void close() throws Exception {

    }

    @Override
    public void preHandle(Task<?> task) {

    }

    @Override
    public String loadSuccessMapSnapshot() {
        return null;
    }

    @Override
    public Map<String, String> loadErrorMapSnapshot() {
        return null;
    }

    @Override
    public void reset() {

    }

    @Override
    public void resetSuccessSheetRow(String maxSuccessSheetRow) throws IOException {

    }
}
