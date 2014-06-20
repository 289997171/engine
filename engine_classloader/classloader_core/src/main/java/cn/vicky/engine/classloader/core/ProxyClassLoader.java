package cn.vicky.engine.classloader.core;

import java.io.InputStream;
import java.net.URL;

/**
 * 类加载器基础(支持排序)
 * 
 * @author Vicky.H
 * @email  ecliser@163.com
 * 
 */
public abstract class ProxyClassLoader implements Comparable<ProxyClassLoader> {
    // Default order
    protected int order = 5;
    // Enabled by default
    protected boolean enabled = true;

    public int getOrder() {
        return order;
    }

    /**
     * Set loading order
     * 
     * @param order
     */
    public void setOrder(int order) {
        this.order = order;
    }

    /**
     * Loads the class
     * 
     * @param className
     * @param resolveIt
     * @return class
     */
    public abstract Class loadClass(String className, boolean resolveIt);

    /**
     * Loads the resource
     * 
     * @param name
     * @return InputStream
     */
    public abstract InputStream loadResource(String name);

    /**
     * Finds the resource
     *
     * @param name
     * @return InputStream
     */
    public abstract URL findResource(String name);

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public int compareTo(ProxyClassLoader o) {
        return order - o.getOrder();
    }
}
