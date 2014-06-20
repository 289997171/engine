package cn.vicky.engine.classloader.core;

import cn.vicky.engine.classloader.core.exception.JclException;
import cn.vicky.engine.classloader.core.exception.ResourceNotFoundException;
import cn.vicky.engine.classloader.core.utils.Utils;

import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 抽象类加载器,能够从不同的资源方式加载classes
 *
 * @author Vicky.H
 * @email ecliser@163.com
 */
@SuppressWarnings("unchecked")
public abstract class AbstractClassLoader extends ClassLoader {

    protected final List<ProxyClassLoader> loaders = new ArrayList<>();

    private final ProxyClassLoader systemLoader = new SystemLoader();
    private final ProxyClassLoader parentLoader = new ParentLoader();
    private final ProxyClassLoader currentLoader = new CurrentLoader();
    private final ProxyClassLoader threadLoader = new ThreadContextLoader();
    private final ProxyClassLoader osgiBootLoader = new OsgiBootLoader();

    /**
     * 构建一个新的AbstractClassLoader实例
     *
     * @param parent parent class loader
     */
    public AbstractClassLoader(ClassLoader parent) {
        super(parent);
        addDefaultLoader();
    }

    /**
     * 无参够着函数
     */
    public AbstractClassLoader() {
        super();
        addDefaultLoader();
    }

    protected final void addDefaultLoader() {
        loaders.add(systemLoader);
        loaders.add(parentLoader);
        loaders.add(currentLoader);
        loaders.add(threadLoader);
    }

    public void addLoader(ProxyClassLoader loader) {
        loaders.add(loader);
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.ClassLoader#loadClass(java.lang.String)
     */
    @Override
    public Class loadClass(String className) throws ClassNotFoundException {
        return (loadClass(className, true));
    }

    /**
     * 覆盖JDK ClassLoader 的 loadClass函数 JarClassLoader仅仅是其子类,用于项目通过jar文件加载classes
     *
     * @param className
     * @param resolveIt
     * @return
     * @throws java.lang.ClassNotFoundException
     * @see java.lang.ClassLoader#loadClass(java.lang.String, boolean)
     */
    @Override
    public Class loadClass(String className, boolean resolveIt) throws ClassNotFoundException {
        if (className == null || className.trim().equals("")) {
            return null;
        }

        Collections.sort(loaders);

        Class clazz = null;

        // Check osgi boot delegation
        if (osgiBootLoader.isEnabled()) {
            clazz = osgiBootLoader.loadClass(className, resolveIt);
        }

        if (clazz == null) {
            for (ProxyClassLoader l : loaders) {
                if (l.isEnabled()) {
                    clazz = l.loadClass(className, resolveIt);
                    if (clazz != null) {
                        break;
                    }
                }
            }
        }

        if (clazz == null) {
            throw new ClassNotFoundException(className);
        }

        return clazz;
    }

    /**
     * 覆盖JDK ClassLoader 的 getResource 用于加载非Class的资源
     * JarClassLoader仅仅是其子类,用于项目通过非class文件加载资源
     *
     * @return
     * @see java.lang.ClassLoader#getResource(java.lang.String)
     */
    @Override
    public URL getResource(String name) {
        if (name == null || name.trim().equals("")) {
            return null;
        }

        Collections.sort(loaders);

        URL url = null;

        // Check osgi boot delegation
        if (osgiBootLoader.isEnabled()) {
            url = osgiBootLoader.findResource(name);
        }

        if (url == null) {
            for (ProxyClassLoader l : loaders) {
                if (l.isEnabled()) {
                    url = l.findResource(name);
                    if (url != null) {
                        break;
                    }
                }
            }
        }

        return url;

    }

    /**
     * 覆盖JDK ClassLoader 的 getResourceAsStream 用于加载非Class的资源
     * JarClassLoader仅仅是其子类,用于项目通过.jar文件中的非class文件加载资源
     *
     * @return
     * @see java.lang.ClassLoader#getResourceAsStream(java.lang.String)
     */
    @Override
    public InputStream getResourceAsStream(String name) {
        if (name == null || name.trim().equals("")) {
            return null;
        }

        Collections.sort(loaders);

        InputStream is = null;

        // Check osgi boot delegation
        if (osgiBootLoader.isEnabled()) {
            is = osgiBootLoader.loadResource(name);
        }

        if (is == null) {
            for (ProxyClassLoader l : loaders) {
                if (l.isEnabled()) {
                    is = l.loadResource(name);
                    if (is != null) {
                        break;
                    }
                }
            }
        }

        return is;

    }

    /**
     * 系统类加载器
     */
    class SystemLoader extends ProxyClassLoader {

        private final Logger logger = Logger.getLogger(SystemLoader.class.getName());

        public SystemLoader() {
            order = 50;
            enabled = Configuration.isSystemLoaderEnabled();
        }

        @Override
        public Class loadClass(String className, boolean resolveIt) {
            Class result;

            try {
                result = findSystemClass(className);
            } catch (ClassNotFoundException e) {
                return null;
            }

            if (logger.isLoggable(Level.FINEST)) {
                logger.log(Level.FINEST, "Returning system class {0}", className);
            }

            return result;
        }

        @Override
        public InputStream loadResource(String name) {
            InputStream is = getSystemResourceAsStream(name);

            if (is != null) {
                if (logger.isLoggable(Level.FINEST)) {
                    logger.log(Level.FINEST, "Returning system resource {0}", name);
                }

                return is;
            }

            return null;
        }

        @Override
        public URL findResource(String name) {
            URL url = getSystemResource(name);

            if (url != null) {
                if (logger.isLoggable(Level.FINEST)) {
                    logger.log(Level.FINEST, "Returning system resource {0}", name);
                }

                return url;
            }

            return null;
        }
    }

    /**
     * 父加载器
     *
     */
    class ParentLoader extends ProxyClassLoader {

        private final Logger logger = Logger.getLogger(ParentLoader.class.getName());

        public ParentLoader() {
            order = 30;
            enabled = Configuration.isParentLoaderEnabled();
        }

        @Override
        public Class loadClass(String className, boolean resolveIt) {
            Class result;

            try {
                result = getParent().loadClass(className);
            } catch (ClassNotFoundException e) {
                return null;
            }

            if (logger.isLoggable(Level.FINEST)) {
                logger.log(Level.FINEST, "Returning class {0} loaded with parent classloader", className);
            }

            return result;
        }

        @Override
        public InputStream loadResource(String name) {
            InputStream is = getParent().getResourceAsStream(name);

            if (is != null) {
                if (logger.isLoggable(Level.FINEST)) {
                    logger.log(Level.FINEST, "Returning resource {0} loaded with parent classloader", name);
                }

                return is;
            }
            return null;
        }

        @Override
        public URL findResource(String name) {
            URL url = getParent().getResource(name);

            if (url != null) {
                if (logger.isLoggable(Level.FINEST)) {
                    logger.log(Level.FINEST, "Returning resource {0} loaded with parent classloader", name);
                }

                return url;
            }
            return null;
        }
    }

    /**
     * 当前class加载器
     *
     */
    class CurrentLoader extends ProxyClassLoader {

        private final Logger logger = Logger.getLogger(CurrentLoader.class.getName());

        public CurrentLoader() {
            order = 20;
            enabled = Configuration.isCurrentLoaderEnabled();
        }

        @Override
        public Class loadClass(String className, boolean resolveIt) {
            Class result;

            try {
                result = getClass().getClassLoader().loadClass(className);
            } catch (ClassNotFoundException e) {
                return null;
            }

            if (logger.isLoggable(Level.FINEST)) {
                logger.log(Level.FINEST, "Returning class {0} loaded with current classloader", className);
            }

            return result;
        }

        @Override
        public InputStream loadResource(String name) {
            InputStream is = getClass().getClassLoader().getResourceAsStream(name);

            if (is != null) {
                if (logger.isLoggable(Level.FINEST)) {
                    logger.log(Level.FINEST, "Returning resource {0} loaded with current classloader", name);
                }

                return is;
            }

            return null;
        }

        @Override
        public URL findResource(String name) {
            URL url = getClass().getClassLoader().getResource(name);

            if (url != null) {
                if (logger.isLoggable(Level.FINEST)) {
                    logger.log(Level.FINEST, "Returning resource {0} loaded with current classloader", name);
                }

                return url;
            }

            return null;
        }
    }

    /**
     * 当前线程类加载器
     *
     */
    class ThreadContextLoader extends ProxyClassLoader {

        private final Logger logger = Logger.getLogger(ThreadContextLoader.class.getName());

        public ThreadContextLoader() {
            order = 40;
            enabled = Configuration.isThreadContextLoaderEnabled();
        }

        @Override
        public Class loadClass(String className, boolean resolveIt) {
            Class result;
            try {
                result = Thread.currentThread().getContextClassLoader().loadClass(className);
            } catch (ClassNotFoundException e) {
                return null;
            }

            if (logger.isLoggable(Level.FINEST)) {
                logger.log(Level.FINEST, "Returning class {0} loaded with thread context classloader", className);
            }

            return result;
        }

        @Override
        public InputStream loadResource(String name) {
            InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream(name);

            if (is != null) {
                if (logger.isLoggable(Level.FINEST)) {
                    logger.log(Level.FINEST, "Returning resource {0} loaded with thread context classloader", name);
                }

                return is;
            }

            return null;
        }

        @Override
        public URL findResource(String name) {
            URL url = Thread.currentThread().getContextClassLoader().getResource(name);

            if (url != null) {
                if (logger.isLoggable(Level.FINEST)) {
                    logger.log(Level.FINEST, "Returning resource {0} loaded with thread context classloader", name);
                }

                return url;
            }

            return null;
        }

    }

    /**
     * Osgi boot 加载器
     *
     */
    public final class OsgiBootLoader extends ProxyClassLoader {

        private final Logger logger = Logger.getLogger(OsgiBootLoader.class.getName());
        private boolean strictLoading;
        private String[] bootDelagation;

        private static final String JAVA_PACKAGE = "java.";

        public OsgiBootLoader() {
            enabled = Configuration.isOsgiBootDelegationEnabled();
            strictLoading = Configuration.isOsgiBootDelegationStrict();
            bootDelagation = Configuration.getOsgiBootDelegation();
            order = 0;
        }

        @Override
        public Class loadClass(String className, boolean resolveIt) {
            Class clazz = null;

            if (enabled && isPartOfOsgiBootDelegation(className)) {
                clazz = getParentLoader().loadClass(className, resolveIt);

                if (clazz == null && strictLoading) {
                    throw new JclException(new ClassNotFoundException("JCL OSGi Boot Delegation: Class " + className + " not found."));
                }

                if (logger.isLoggable(Level.FINEST)) {
                    logger.log(Level.FINEST, "Class {0} loaded via OSGi boot delegation.", className);
                }
            }

            return clazz;
        }

        @Override
        public InputStream loadResource(String name) {
            InputStream is = null;

            if (enabled && isPartOfOsgiBootDelegation(name)) {
                is = getParentLoader().loadResource(name);

                if (is == null && strictLoading) {
                    throw new ResourceNotFoundException("JCL OSGi Boot Delegation: Resource " + name + " not found.");
                }

                if (logger.isLoggable(Level.FINEST)) {
                    logger.log(Level.FINEST, "Resource {0} loaded via OSGi boot delegation.", name);
                }
            }

            return is;
        }

        @Override
        public URL findResource(String name) {
            URL url = null;

            if (enabled && isPartOfOsgiBootDelegation(name)) {
                url = getParentLoader().findResource(name);

                if (url == null && strictLoading) {
                    throw new ResourceNotFoundException("JCL OSGi Boot Delegation: Resource " + name + " not found.");
                }

                if (logger.isLoggable(Level.FINEST)) {
                    logger.log(Level.FINEST, "Resource {0} loaded via OSGi boot delegation.", name);
                }
            }

            return url;
        }

        /**
         * Check if the class/resource is part of OSGi boot delegation
         *
         * @param resourceName
         * @return
         */
        private boolean isPartOfOsgiBootDelegation(String resourceName) {
            if (resourceName.startsWith(JAVA_PACKAGE)) {
                return true;
            }

            String[] bootPkgs = bootDelagation;

            if (bootPkgs != null) {
                for (String bc : bootPkgs) {
                    Pattern pat = Pattern.compile(Utils.wildcardToRegex(bc), Pattern.CASE_INSENSITIVE);

                    Matcher matcher = pat.matcher(resourceName);
                    if (matcher.find()) {
                        return true;
                    }
                }
            }

            return false;
        }

        public boolean isStrictLoading() {
            return strictLoading;
        }

        public void setStrictLoading(boolean strictLoading) {
            this.strictLoading = strictLoading;
        }

        public String[] getBootDelagation() {
            return bootDelagation;
        }

        public void setBootDelagation(String[] bootDelagation) {
            this.bootDelagation = bootDelagation;
        }
    }

    public ProxyClassLoader getSystemLoader() {
        return systemLoader;
    }

    public ProxyClassLoader getParentLoader() {
        return parentLoader;
    }

    public ProxyClassLoader getCurrentLoader() {
        return currentLoader;
    }

    public ProxyClassLoader getThreadLoader() {
        return threadLoader;
    }

    public ProxyClassLoader getOsgiBootLoader() {
        return osgiBootLoader;
    }
}
