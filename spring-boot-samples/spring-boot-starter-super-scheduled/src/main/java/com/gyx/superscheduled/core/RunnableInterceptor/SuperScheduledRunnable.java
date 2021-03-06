package com.gyx.superscheduled.core.RunnableInterceptor;

import com.gyx.superscheduled.common.utils.proxy.Chain;
import com.gyx.superscheduled.common.utils.proxy.Point;
import com.gyx.superscheduled.exception.SuperScheduledException;
import com.gyx.superscheduled.model.ScheduledRunningContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class SuperScheduledRunnable {
    protected final Log logger = LogFactory.getLog(getClass());

    /**
     * 原始的方法
     */
    private Method method;
    /**
     * 方法所在的bean
     */
    private Object bean;
    /**
     * 增强器的调用链
     */
    private Chain chain;
    /**
     * 线程运行时的上下文
     */
    private ScheduledRunningContext context;


    public Object invoke() {
        Object result = null;
        if (!context.getCallOff()) {
            //索引自增1
            if (chain.incIndex() == chain.getList().size()) {
                //调用链中的增强方法已经全部执行结束
                try {
                    //调用链索引初始化
                    chain.resetIndex();
                    //执行原本的方法
                    result = method.invoke(bean);
                } catch (IllegalAccessException | InvocationTargetException e) {
                    throw new SuperScheduledException(e.getLocalizedMessage());
                }
            } else {
                //获取被代理后的方法增强
                Point point = chain.getList().get(chain.getIndex());
                //执行增强方法
                result = point.invoke(this);
            }
        } else {
            logger.info(getContext().getCallOffRemark());
            chain.resetIndex();
            context.setCallOff(false);
        }
        return result;
    }

    public SuperScheduledRunnable() {
        this.context = new ScheduledRunningContext();
    }

    public Method getMethod() {
        return method;
    }

    public void setMethod(Method method) {
        this.method = method;
    }

    public Object getBean() {
        return bean;
    }

    public void setBean(Object bean) {
        this.bean = bean;
    }

    public Chain getChain() {
        return chain;
    }

    public void setChain(Chain chain) {
        this.chain = chain;
    }

    public ScheduledRunningContext getContext() {
        return context;
    }

    public void setContext(ScheduledRunningContext context) {
        this.context = context;
    }
}
