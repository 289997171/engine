package cn.vicky.engine.classloader.core.proxy;

/**
 * @author Vicky.H
 * @email ecliser@163.com
 */
public class ProxyProviderFactory {

    // 默认使用JDK代理供应
    private static ProxyProvider proxyProvider = new JdkProxyProvider();

    public static void setDefaultProxyProvider(ProxyProvider proxyProvider) {
        ProxyProviderFactory.proxyProvider = proxyProvider;
    }

    /**
     * 返回默认的ProxyProvider实例
     *
     * @return
     */
    public static ProxyProvider create() {
        return proxyProvider;
    }
}
