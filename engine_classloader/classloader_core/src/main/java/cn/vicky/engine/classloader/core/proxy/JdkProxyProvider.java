package cn.vicky.engine.classloader.core.proxy;

import cn.vicky.engine.classloader.core.JclUtils;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * 创建JDK代理
 *
 * @author Vicky.H
 * @email ecliser@163.com
 *
 */
public class JdkProxyProvider implements ProxyProvider {

    private class JdkProxyHandler implements InvocationHandler {

        private final Object delegate;

        public JdkProxyHandler(Object delegate) {
            this.delegate = delegate;
        }

        /**
         *
         * @see java.lang.reflect.InvocationHandler#invoke(java.lang.Object,
         * java.lang.reflect.Method, java.lang.Object[])
         */
        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            Method delegateMethod = delegate.getClass().getMethod(method.getName(), method.getParameterTypes());
            return delegateMethod.invoke(delegate, args);
        }
    }

    @Override
    public Object createProxy(Object object, Class superClass, Class[] interfaces, ClassLoader cl) {
        JdkProxyHandler handler = new JdkProxyHandler(object);
        return Proxy.newProxyInstance(cl == null ? JclUtils.class.getClassLoader() : cl, interfaces, handler);
    }
}
