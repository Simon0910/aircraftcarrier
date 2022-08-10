package com.aircraftcarrier.framework.tookit;

import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.Test;

import java.util.Date;

/**
 * 类注释内容
 *
 * @author zhipengliu
 * @date 2022/8/8
 * @since 1.0
 */
@Slf4j
public class JodaTimeUtilTest {

    @Test
    public void testTimeExpired() {
        long time = new Date().getTime();
        Boolean timeExpired = JodaTimeUtil.isTimeExpired(time);
        Assert.assertNotNull(timeExpired);
    }

    @Test
    public void testPlus() {
        Date date = JodaTimeUtil.plusMinutes(new Date(), 60);
        Assert.assertNotNull(date);
    }
}
