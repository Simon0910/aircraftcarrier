package com.aircraftcarrier.framework.excel.util;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.lang.reflect.Field;

/**
 * Metadata
 *
 * @author zhipengliu
 * @date 2025/4/20
 * @since 1.0
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Metadata {
    private Field field;
    private Integer index;
    private String headName;
}
