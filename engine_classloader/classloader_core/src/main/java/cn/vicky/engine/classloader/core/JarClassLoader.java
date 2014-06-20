package cn.vicky.engine.classloader.core;

import cn.vicky.engine.classloader.core.exception.JclException;
import cn.vicky.engine.classloader.core.exception.ResourceNotFoundException;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * 通过处理bytes二进制文件和其他资源加载类
 * 
 * @author Vicky.H
 * @email ecliser@163.com
 * 
 */
@SuppressWarnings("unchecked")
public class JarClassLoader extends AbstractClassLoader {
    /**
     * 类缓存
     */
    protected final Map<String, Class> classes;

    protected final ClasspathResources classpathResources;
    private char classNameReplacementChar;
    private final ProxyClassLoader localLoader = new LocalLoader();

    private static final Logger logger = Logger.getLogger( JarClassLoader.class.getName() );

    public JarClassLoader() {
        classpathResources = new ClasspathResources();
        classes = Collections.synchronizedMap( new HashMap<String, Class>() );
        initialize();
    }

    /**
     * 一些初始化操作
     */
    public final void initialize() {
        loaders.add( localLoader );
    }

    /**
     * 通过不同的资源加载
     * 
     * @param sources
     */
    public JarClassLoader(Object[] sources) {
        this();
        addAll( sources );
    }

    /**
     * 通过不同的资源加载
     * 
     * @param sources
     */
    public JarClassLoader(List sources) {
        this();
        addAll( sources );
    }

    /**
     * 添加jar或/class资源
     * 
     * @param sources
     */
    public final void addAll(Object[] sources) {
        for (Object source : sources) {
            add( source );
        }
    }

    /**
     * 添加jar或/class资源
     * 
     * @param sources
     */
    public final void addAll(List sources) {
        sources.stream().forEach((source) -> {
            add( source );
        });
    }

    /**
     * 加载本地或远程资源
     * 
     * @param source
     */
    public void add(Object source) {
        if (source instanceof InputStream)
            add( (InputStream) source );
        else if (source instanceof URL)
            add( (URL) source );
        else if (source instanceof String)
            add( (String) source );
        else
            throw new JclException( "Unknown Resource type" );

    }

    /**
     * 加载本地或远程资源
     * 
     * @param resourceName
     */
    public void add(String resourceName) {
        classpathResources.loadResource( resourceName );
    }

    /**
     * 通过输入流加载类
     * 
     * @param jarStream
     */
    public void add(InputStream jarStream) {
        classpathResources.loadJar( jarStream );
    }

    /**
     * 加载本地或远程资源
     * 
     * @param url
     */
    public void add(URL url) {
        classpathResources.loadResource( url );
    }

    /**
     * 使用ClasspathResources获得本地或远程的支援,从而读取类的二进制数据
     * 
     * @param className
     * @return byte[]
     */
    protected byte[] loadClassBytes(String className) {
        className = formatClassName( className );

        return classpathResources.getResource( className );
    }

    /**
     * 卸载已经加载的类
     * 
     * @param className
     */
    public void unloadClass(String className) {
        if (logger.isLoggable( Level.FINEST ))
            logger.log( Level.FINEST, "Unloading class {0}", className);

        if (classes.containsKey( className )) {
            if (logger.isLoggable( Level.FINEST ))
                logger.log( Level.FINEST, "Removing loaded class {0}", className);
            classes.remove( className );
            try {
                classpathResources.unload( formatClassName( className ) );
            } catch (ResourceNotFoundException e) {
                throw new JclException( "Something is very wrong!!!"
                        + "The locally loaded classes must be in synch with ClasspathResources", e );
            }
        } else {
            try {
                classpathResources.unload( formatClassName( className ) );
            } catch (ResourceNotFoundException e) {
                throw new JclException( "Class could not be unloaded "
                        + "[Possible reason: Class belongs to the system]", e );
            }
        }
    }

    /**
     * 格式化类的名称
     * 
     * @param className
     * @return String
     */
    protected String formatClassName(String className) {
        className = className.replace( '/', '~' );

        if (classNameReplacementChar == '\u0000') {
            // '/' is used to map the package to the path
            className = className.replace( '.', '/' ) + ".class";
        } else {
            // Replace '.' with custom char, such as '_'
            className = className.replace( '.', classNameReplacementChar ) + ".class";
        }

        className = className.replace( '~', '/' );
        return className;
    }

    /**
     * 本地加载器
     * 
     */
    class LocalLoader extends ProxyClassLoader {

        private final Logger logger = Logger.getLogger( LocalLoader.class.getName() );

        public LocalLoader() {
            order = 10;
            enabled = Configuration.isLocalLoaderEnabled();
        }

        @Override
        public Class loadClass(String className, boolean resolveIt) {
            Class result = null;
            byte[] classBytes;

            result = classes.get( className );
            if (result != null) {
                if (logger.isLoggable( Level.FINEST ))
                    logger.log( Level.FINEST, "Returning local loaded class [{0}] from cache", className);
                return result;
            }

            classBytes = loadClassBytes( className );
            if (classBytes == null) {
                return null;
            }

            result = defineClass( className, classBytes, 0, classBytes.length );

            if (result == null) {
                return null;
            }

            /*
             * Preserve package name.
             */
            if (result.getPackage() == null) {
                int lastDotIndex = className.lastIndexOf( '.' );
                String packageName = (lastDotIndex >= 0) ? className.substring( 0, lastDotIndex) : "";
                definePackage( packageName, null, null, null, null, null, null, null );
            }

            if (resolveIt)
                resolveClass( result );

            classes.put( className, result );
            if (logger.isLoggable( Level.FINEST ))
                logger.log( Level.FINEST, "Return new local loaded class {0}", className);
            return result;
        }

        @Override
        public InputStream loadResource(String name) {
            byte[] arr = classpathResources.getResource( name );
            if (arr != null) {
                if (logger.isLoggable( Level.FINEST ))
                    logger.log( Level.FINEST, "Returning newly loaded resource {0}", name);

                return new ByteArrayInputStream( arr );
            }

            return null;
        }

        @Override
        public URL findResource(String name) {
            URL url = classpathResources.getResourceURL( name );
            if (url != null) {
                if (logger.isLoggable( Level.FINEST ))
                    logger.log( Level.FINEST, "Returning newly loaded resource {0}", name);

                return url;
            }

            return null;
        }
    }

    public char getClassNameReplacementChar() {
        return classNameReplacementChar;
    }

    public void setClassNameReplacementChar(char classNameReplacementChar) {
        this.classNameReplacementChar = classNameReplacementChar;
    }

    /**
     * 返回所有已经加载的类和资源
     * 
     * @return Map
     */
    public Map<String, byte[]> getLoadedResources() {
        return classpathResources.getResources();
    }

    /**
     * @return 本地类加载器
     */
    public ProxyClassLoader getLocalLoader() {
        return localLoader;
    }

    /**
     * 返回所有已经加载的类
     * 
     * @return Map
     */
    public Map<String, Class> getLoadedClasses() {
        return Collections.unmodifiableMap( classes );
    }
}
