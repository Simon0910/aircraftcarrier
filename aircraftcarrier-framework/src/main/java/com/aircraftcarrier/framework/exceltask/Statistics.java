package com.aircraftcarrier.framework.exceltask;

import lombok.Data;

/**
 * Statistics
 *
 * @author zhipengliu
 * @date 2025/4/4
 * @since 1.0
 */
@Data
public class Statistics {
    /**
     * 统计处理成功的总数
     */
    private int successNum;
    /**
     * excel invoke num
     */
    private int totalReadNum;
    /**
     * 统计失败数
     */
    private int failNum;
}
