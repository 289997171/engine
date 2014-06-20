package cn.vicky.engine.classloader.core.proxy;

/**
 * 代理供应器
 * 
 * @author Vicky.H
 * @email ecliser@163.com
 * 
 */
public interface ProxyProvider {
    public Object createProxy(Object object, Class superClass, Class[] interfaces, ClassLoader cl);
}
