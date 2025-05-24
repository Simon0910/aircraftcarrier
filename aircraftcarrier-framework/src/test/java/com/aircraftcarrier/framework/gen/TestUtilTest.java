package com.aircraftcarrier.framework.gen;

import com.aircraftcarrier.framework.security.core.LoginUser;
import com.github.pagehelper.ISelect;
import org.junit.Test;

/**
 * 类注释内容
 *
 * @author zhipengliu
 * @date 2025/5/24
 * @since 1.0
 */
public class TestUtilTest {

    @Test
    public void testPrint() {
        // ISelect select = () -> xxMapper.selectList(parmas);
        ISelect select = () -> {};
        TestUtil.searchFullEntity(select, this::checkOtherCondition);
    }

    private boolean checkOtherCondition(Object obj) {
        // Entity entity = (Entity) obj;
        // 查询接口
        return true;
    }
}
