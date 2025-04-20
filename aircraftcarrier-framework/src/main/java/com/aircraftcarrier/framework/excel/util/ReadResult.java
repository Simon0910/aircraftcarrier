package com.aircraftcarrier.framework.excel.util;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

/**
 * @author lzp
 */
@Getter
@Setter
public class ReadResult<T> {
    List<T> rowList = new ArrayList<>();
    TreeMap<Integer, String> errors = new TreeMap<>();

    public boolean isOk() {
        return errors.isEmpty();
    }

    public boolean isNotOk() {
        return !isOk();
    }

    public void putError(Integer rowIndex, String format) {
        errors.put(rowIndex, format);
    }
}
