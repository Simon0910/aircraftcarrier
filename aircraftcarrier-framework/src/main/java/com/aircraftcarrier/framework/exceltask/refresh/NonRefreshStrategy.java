package com.aircraftcarrier.framework.exceltask.refresh;

import com.aircraftcarrier.framework.exceltask.TaskConfig;

import java.util.Collections;
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
    public void preHandle() throws Exception {

    }

    @Override
    public String loadSuccessMapSnapshot() throws Exception {
        return "0_0";
    }

    @Override
    public Map<String, String> loadErrorMapSnapshot() throws Exception {
        return Collections.emptyMap();
    }
}
