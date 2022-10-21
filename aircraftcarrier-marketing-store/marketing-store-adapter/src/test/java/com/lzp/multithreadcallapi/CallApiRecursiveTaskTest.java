package com.lzp.multithreadcallapi;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class CallApiRecursiveTaskTest {

    int num = 10;
    List<Param> params = new ArrayList<>(num);

    CallApiService callApiService;

    @Before
    public void before() {
        for (int i = 0; i < 10; i++) {
            Param param = new Param();
            params.add(param);
        }
        callApiService = (param) -> {
            Result result = new Result();
            result.setId(1L);
            result.setName("name");
            result.setDate(new Date());
            return result;
        };
    }

    @Test
    public void testCall() {
        CallApiRecursiveTask<Param, Result> task = new CallApiRecursiveTask<>((param) -> {
            Result result = new Result();
            result.setId(1L);
            result.setName("name");
            result.setDate(new Date());
            return result;
        }, params);
        task.fork();
        List<Result> results = task.join();
        System.out.println(results.size());
    }
}
