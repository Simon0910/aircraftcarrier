package com.aircraftcarrier.framework.concurrent.multithreadcallapi;

import lombok.Getter;
import lombok.Setter;

/**
 * @author liuzhipeng
 */
@Setter
@Getter
public class Param {
    int i;

    public Param() {
    }

    public Param(int i) {
        this.i = i;
    }
}
