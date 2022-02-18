package com.aircraftcarrier.marketing.store.app.test.executor;

import com.aircraftcarrier.marketing.store.common.enums.DataTypeEnum;
import com.aircraftcarrier.marketing.store.infrastructure.repository.DemoMapper;
import com.aircraftcarrier.marketing.store.infrastructure.repository.dataobject.DemoDo;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

/**
 * @author lzp
 *
 * <pre> {@code
 * spring7种事务传播属性总结:
 *      1. PROPAGATION_REQUIRED(Spring默认): 如果当前没有事务，就创建一个新事务，如果当前存在事务，就加入该事务，这是最常见的选择.
 *          没有就自己玩, 有就一起玩, 最终就还是想玩 (正常型, required需要玩) (要么一起数据一致性, 要么自己数据一致性)
 *      2. PROPAGATION_SUPPORTS: 支持当前事务，如果当前存在事务，就加入该事务，如果当前不存在事务，就以非事务执行。
 *          没有就不玩（但是自己依然可以独立执行，挨个commit，直到自己异常停止）, 有就一起玩, 玩不玩都行  (温和型, supports支持你) (不关心自己的数据一致性)
 *      3. PROPAGATION_MANDATORY: 支持当前事务，如果当前存在事务，就加入该事务，如果当前不存在事务，就抛出异常。
 *          没有就一哭二闹三上吊, 必须要玩 (强势型, 谁调用我必须和我玩, 保证数据强一致性)
 *      4. PROPAGATION_REQUIRES_NEW: 创建新事务，无论当前存不存在事务，都创建新事务。
 *          就是不和你玩, 保证自己先提交, 外部异常了和我没关系, 我的数据我做主 (自私型, 以自我为中心的, 只保护自己的数据一致性)
 *      5. PROPAGATION_NOT_SUPPORTED: 以非事务方式执行操作，如果当前存在事务，就把当前事务挂起。
 *          就是不玩 (自虐型, 就是不保护自己的数据一致性)
 *      6. PROPAGATION_NEVER: 以非事务方式执行，如果当前存在事务，则抛出异常
 *          就是不玩, 谁玩就打谁 (自虐 + 暴力型, 就是全部不保护数据一致性)
 *      7. PROPAGATION_NESTED: 如果当前存在事务，则在嵌套事务内执行。如果当前没有事务，则按REQUIRED属性执行。
 *          如果子事务回滚或提交，不会导致父事务回滚或提交，但父事务回滚将导致子事务回滚 (讨好型， 不影响父，父可影响子)
 * }</pre>
 * <p>
 * <p>
 * 4 和 7的区别:
 * PROPAGATION_REQUIRES_NEW，原有事务B新起事务A，事务A中的commit和rollback不会影响外部事务B的commit和rollback，相互独立，如果事务A抛出异常，肯定会影响外部是B的。
 * PROPAGATION_NESTED，表示嵌套事务，看如下示例:
 * <pre> {@code
 * ServiceA {
 *     // 事务属性配置为 PROPAGATION_REQUIRED
 *     @Transactional(propagation=Propagation.REQUIRED) // 1
 *     void methodA() {
 *         insertData(); //2
 *         try {
 *             ServiceB.methodB();   //3
 *         } catch (SomeException) {
 *             // 执行其他业务, 如 ServiceC.methodC();   //5
 *         }
 *         updateData(); //6
 *     }
 * }
 *
 * ServiceB {
 *     @Transactional(propagation=Propagation.NESTED)
 *     void methodB(){
 *         updateData(); //4
 *     }
 * }
 * }</pre>
 * 说明: 在上面的1，将开起新事务A，2的时候会插入数据，此时事务A挂起，没有commit，3的时候，使用PROPAGATION_NESTED传播，将在3点的时候新建一个savepoint保存2插入的数据，不提交。
 * <p>
 * 1. 如果methodB出现异常，将回滚4的操作，不影响2的操作，同时可以处理后面的5,6逻辑，最后一起commit: 2,5,6
 * 2. 如果methodB没有出现异常，那么将一起commit: 2,4,6。
 * 3. 假如methodB使用的PROPAGATION_REQUIRES_NEW，那么B异常，会commit: 2,5,6，和NESTED一致，如果methodB没有出现异常，那么会先commit4，再commit:6，那么事务将分离开，不能保持一致，假如执行6报错，2和6将回滚，而4却没有被回滚，不能达到预期效果。
 * <p>
 * 参考链接:
 * https://juejin.cn/post/6844903996939829256#heading-0
 * https://www.jianshu.com/p/f89771cae115
 * https://waylau.com/spring-transaction/
 */
@Component
public class TransactionalExe {

    @Resource
    private DemoMapper demoMapper;
    @Resource
    private TransactionalExe2 transactionalExe2;

    @Transactional(rollbackFor = Exception.class)
    public void execute() {
        // 1
        DemoDo configDO = new DemoDo();
        configDO.setBizNo("111");
        configDO.setDescription("111");
        configDO.setSellerNo("sellerNo");
        configDO.setSellerName("sellerName");
        configDO.setDataType(DataTypeEnum.GENERAL);
        demoMapper.insert(configDO);

        // 2
        try {
            transactionalExe2.execute2();
        } catch (Exception e) {
            System.out.println(".....");
        }
//        transactionalExe2.execute2();

        // 3
//        execute3();
//        ((TransactionalExe) AopContext.currentProxy()).execute3();
    }

    //    @Transactional(rollbackFor = Exception.class)
    public void execute3() {
        DemoDo configDO = new DemoDo();
        configDO.setBizNo("333");
        configDO.setDescription("333");
        configDO.setSellerNo("sellerNo");
        configDO.setSellerName("sellerName");
        configDO.setDataType(DataTypeEnum.GENERAL);
        demoMapper.insert(configDO);
    }
}
