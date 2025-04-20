package com.aircraftcarrier.framework.excel.util;

import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.TreeMap;

/**
 * @author lzp
 */
@Getter
@Setter
public class ReadResult<T> {
    List<T> rowList;
    TreeMap<Integer, String> errors;
}
