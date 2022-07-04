package com.aircraftcarrier.marketing.store.app.test.executor;

import com.aircraftcarrier.marketing.store.common.enums.DataTypeEnum;
import com.aircraftcarrier.marketing.store.infrastructure.repository.mapper.DemoMapper;
import com.aircraftcarrier.marketing.store.infrastructure.repository.dataobject.DemoDo;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

/**
 * @author lzp
 */
@Component
public class TransactionalExe2 {

    @Resource
    private DemoMapper demoMapper;


    /**
     * 假如父默认且无异常, 子抛出异常时不同传播属性的分析:
     *
     * @param name
     */
    @Transactional(rollbackFor = Exception.class, propagation = Propagation.NESTED)
    // 1, 3成功, 2失败. 新创建的嵌套子事务独立运行, 外面捕获后不影响外部事务
    // 假如外部没有注解呢?
//        @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRES_NEW)
    // 1, 3成功, 2失败. 新创建的新事务独立运行, 外面捕获后不影响外部事务
//        @Transactional(rollbackFor = Exception.class, propagation = Propagation.SUPPORTS)
    // 全部失败, 加入的事务, 即便外面捕获后, 当前事务全部失败
//        @Transactional(rollbackFor = Exception.class, propagation = Propagation.MANDATORY)
    // 全部失败, 加入的事务, 即便外面捕获后, 当前事务全部失败
//        @Transactional(rollbackFor = Exception.class, propagation = Propagation.NOT_SUPPORTED)
    // 1,2,3 全部成功. 相当于没有注解,没有事务, 外面捕获后不影响外部事务
//        @Transactional(rollbackFor = Exception.class, propagation = Propagation.NEVER)
    // 1, 3成功, 2失败. 根本没哟进入方法体, 外面捕获后不影响外部事务
//    @Transactional(rollbackFor = Exception.class)
    // 全部失败, 加入的事务, 即便外面捕获后, 当前事务全部失败
    public void execute2(String name) {
        DemoDo configDO = new DemoDo();
        configDO.setBizNo(name);
        configDO.setDescription(name);
        configDO.setSellerNo("sellerNo");
        configDO.setSellerName("sellerName");
        configDO.setDataType(DataTypeEnum.GENERAL);
        demoMapper.insert(configDO);
        if (true) {
//            throw new RuntimeException("execute2 error : #############################################");
        }
    }
}
