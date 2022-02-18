package com.aircraftcarrier.framework.excel.util;

import lombok.Getter;
import lombok.Setter;

import java.util.LinkedHashMap;
import java.util.List;

/**
 * @author lzp
 */
@Getter
@Setter
public class ReadResult<T> {
    List<T> rowList;
    LinkedHashMap<Integer, String> errors;
}
