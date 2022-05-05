package com.lzp.java8;

import org.junit.Test;

import java.util.Optional;

public class OptionalTest {

    @Test
    public void optionalTest() {
        // Optional 构造方式1 - of 传入的值不能为 null
        Optional<String> helloOption = Optional.of("hello");
        System.out.println(helloOption.isPresent());
        helloOption.ifPresent(System.out::println);
        System.out.println(helloOption.orElse(getDefault())); // 为什么还要运行else?
        System.out.println(helloOption.orElseGet(this::getDefault));
        helloOption.map(String::valueOf).map(String::length).ifPresent(System.out::println);
        helloOption.filter(num -> num.equals("hell")).ifPresent(System.out::println);


        // Optional 构造方式2 - empty 一个空 optional
        System.out.println("================================");
        Optional<String> emptyOptional = Optional.empty();
        System.out.println(emptyOptional.isPresent());
        emptyOptional.ifPresent(System.out::println);

        // Optional 构造方式3 - ofNullable 支持传入 null 值的 optional
        System.out.println("================================");
        Optional<String> nullOptional = Optional.ofNullable(null);
        System.out.println(nullOptional.isPresent());
        nullOptional.ifPresent(System.out::println);
        System.out.println(nullOptional.orElse(getDefault()));
        System.out.println(nullOptional.orElseGet(this::getDefault));

    }

    public String getDefault() {
        System.out.println("   获取默认值中..run getDefault method");
        return "xxx";
    }
}
