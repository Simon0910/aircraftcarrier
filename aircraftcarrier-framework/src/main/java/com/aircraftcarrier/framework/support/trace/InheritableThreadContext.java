package com.aircraftcarrier.framework.support.trace;

import com.alibaba.fastjson.JSON;

/**
 * 线程上下文，一个线程内所需的上下文变量参数，使用InheritableThreadLocal保存副本，可以将副本传递给子线程
 * { @link https://www.cnblogs.com/yangyongjie/p/12523567.html }
 *
 * <p>
 * 可以将用户信息传递到子线程,使用此种方式
 * <pre> {@code
 *
 *  InheritableThreadContext.currentThreadContext().setUser(userinfo);
 *   new Thread(new Runnable() {
 *       @Override
 *       public void run() {
 *           InheritableThreadContext.currentThreadContext().getUser());
 *           // 移除子线程本地变量
 *           InheritableThreadContext.remove();
 *       }
 *   }).start();
 *   // 移除父线程本地变量
 *   InheritableThreadContext.remove();
 *
 * }</pre>
 * </p>
 *
 * @author lzp
 * @since 2021-12-2
 */
public class InheritableThreadContext {

    private static final InheritableThreadLocal<InheritableThreadContext> INHERITABLE_THREAD_LOCAL = new InheritableThreadLocal<InheritableThreadContext>() {
        @Override
        protected InheritableThreadContext initialValue() {
            return new InheritableThreadContext();
        }
    };
    private String threadId;
    private String user;

    public static InheritableThreadContext currentThreadContext() {
        return INHERITABLE_THREAD_LOCAL.get();
    }

    public static void remove() {
        INHERITABLE_THREAD_LOCAL.remove();
    }

    public String getThreadId() {
        return threadId;
    }

    public void setThreadId(String threadId) {
        this.threadId = threadId;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    @Override
    public String toString() {
        return JSON.toJSONString(this);
    }
}