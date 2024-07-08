package com.aircraftcarrier.framework.message.taghandler;

import com.aircraftcarrier.framework.message.Message;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/**
 * AbstractRmqMessageTagHandler
 *
 * @author zhipengliu
 * @date 2024/7/7
 * @since 1.0
 */
@Slf4j
public abstract class AbstractRocketMQGroupTagHandler<T> {

    private final TypeReference<T> typeReference;
    private final Class<T> tClass;

    protected AbstractRocketMQGroupTagHandler() {
        Type actualTypeArgument = ((ParameterizedType) AbstractRocketMQGroupTagHandler.this.getClass().getGenericSuperclass())
                .getActualTypeArguments()[0];

        this.tClass = (Class<T>) actualTypeArgument;

        this.typeReference = new TypeReference<T>() {
            @Override
            public Type getType() {
                return actualTypeArgument;
            }
        };
    }

    public Class<T> getTargetClass() {
        return tClass;
    }

    public T getTarget(Message<?> message) {
        return JSON.parseObject(JSON.toJSONString(message.getMsg()), typeReference.getType());
    }

    /**
     * <pre> {@code
     *
     * 幂等性是指多次执行同一操作，产生的影响均与一次执行的影响相同。在分布式系统和API设计中,幂等性是一个重要的概念。以下是关于幂等性的一些要点:
     *
     * 1. 定义:
     *    一个操作执行一次和执行多次的效果是一样的,不会产生副作用。
     *
     * 2. 重要性:
     *    - 确保系统的一致性和可靠性
     *    - 防止重复操作导致的数据错误
     *    - 简化错误处理和重试逻辑
     *
     * 3. 常见的幂等操作:
     *    - HTTP GET 请求
     *    - 数据库的SELECT操作
     *    - 设置操作(如Redis的SET)
     *    - 删除操作(如DELETE HTTP请求)
     *
     * 4. 实现幂等性的方法:
     *    - 使用唯一标识符
     *    - 状态检查
     *    - 乐观锁
     *    - 分布式锁
     *    - 去重表
     *
     * 5. 在Redis中实现幂等性:
     *    - 使用SET命令的NX选项
     *    - 使用SETNX命令
     *    - 利用Redis的原子性操作
     *
     * 6. 在消息队列中的应用:
     *    - 使用消息ID去重
     *    - 实现exactly-once语义
     *
     * 7. 在RESTful API中:
     *    - GET、PUT、DELETE方法通常是幂等的
     *    - POST方法通常不是幂等的
     *
     * 8. 注意事项:
     *    - 幂等性不等同于结果相同
     *    - 需要考虑并发情况
     *    - 幂等性可能会增加系统复杂度
     *
     * 9. 实现幂等性的挑战:
     *    - 分布式系统中的一致性问题
     *    - 性能与幂等性的权衡
     *    - 处理超时和失败情况
     *
     * 10. 最佳实践:
     *     - 设计API时考虑幂等性
     *     - 使用幂等键(Idempotency Key)
     *     - 记录操作日志
     *     - 实现幂等性检查机制
     *
     * 在设计分布式系统和API时,合理地实现幂等性可以大大提高系统的可靠性和健壮性,尤其在处理网络不稳定、重试逻辑等场景时非常重要。
     *
     * Citations:
     * [1] https://www.runoob.com/redis/redis-tutorial.html
     * [2] https://redis.io
     * [3] https://en.wikipedia.org/wiki/Redis
     * [4] https://github.com/redis/redis/actions/runs/9108126597
     * [5] https://www.cnblogs.com/vivotech/p/15497555.html
     *
     * }</pre>
     * <p>
     * 消息重试
     * <a href="https://rocketmq.apache.org/zh/docs/featureBehavior/10consumerretrypolicy">...</a>
     *
     * @param message
     */
    public void handle(Message<?> message) throws Exception {
        T target = getTarget(message);
        /*
         * 例如支付完成后订单状态为`已支付`, 同时发消息增加用户积分
         * 幂等性要求：对订单为`已支付`的多次请求 不能多次给用户增加积分，遵循exactly-once语义
         * 多次请求的情况为：生产者网络超时重试，消费者消费异常消息重试对同一条消息重放。或者生产者多次请求
         * 对于消息系统要靠重试机制来保证At-least-once至少一次处理，对于业务本身来说要靠幂等来保证exactly-once只被成功处理一次
         * 对于业务本身来说幂等要确定什么情况下才算是被成功处理，
         *    1. 正向流程: 记录消息的UUID=ok，后续无论多少次重试可以查到消息id已被成功处理，可以保证幂等
         *       异常流程: 假如消息执行需要多个步骤：a -> b -> c -> d -> 消息id=ok, 中间步骤可能经过多个系统,
         *                如果从c步骤执行失败，消息重试时，下一次是否还需要执行之前的步骤 a -> b -> c
         *    2. 对于重要消息：例如重要的消息收到银行的流水：常规操作是先记录下消息，返回成功，后续使用定时任务驱动消息表之后的步骤
         *                记录消息表-> a -> b -> c -> d -> 消息id=ok
         *                           |    |    |    |       |
         *                记录消息表->ok-> ok-> fail
         *    3. 分布式事务：要么都一个个成功，要么一个个回滚
         *                分布式事务中的最终一致性方案：使用定时任务驱动消息表之后的步骤
         *                rocketMQ中事务消息方案是：可以保证a阶段本地事务成功，消息一定会被成功发送，
         *                (不会出现a本地事务成功，消息发送失败的情况，因为a本地生产者会向broker发送事务状态，或者broker会反向询问生产者是否发送)
         *                并且b阶段收到消息后执行失败会有重试策略驱动b阶段成功
         *
         */
        if (idempotentResult(target)) {
            doHandle(target);
        }
    }

    public void exception(Message<?> message, Exception e) throws Exception {
        onException(getTarget(message), e);
    }

    public abstract String handlerGroup();

    public abstract String tag();

    public boolean idempotentResult(T t) throws Exception {
        // 业务幂等处理
        return true;
    }

    public abstract void doHandle(T t) throws Exception;

    public abstract void onException(T t, Exception e) throws Exception;

}
