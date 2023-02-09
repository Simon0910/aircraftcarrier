package com.aircraftcarrier.framework.basic.threadlocal;

import java.util.concurrent.TimeUnit;

/**
 * https://zhuanlan.zhihu.com/p/304240519
 */
public class ThreadLocalDemo {
    public static void main(String[] args) throws InterruptedException {
        firstStack();
        System.gc();
        TimeUnit.SECONDS.sleep(1);
        Thread thread = Thread.currentThread();
        System.out.println(thread); // 在这里打断点，观察thread对象里的ThreadLocalMap数据

    }

    // 通过是否获取返回值观察A对象里的local对象是否被回收
    private static A firstStack() {
        A a = new A();
        System.out.println("value: " + a.get());
        return a;
    }

    private static class A {
        private ThreadLocal<String> local = ThreadLocal.withInitial(() -> "in class A");

        public String get() {
            return local.get();
        }

        public void set(String str) {
            local.set(str);
        }

    }
}