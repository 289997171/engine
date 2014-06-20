package cn.vicky.engine.classloader.core.context;

import cn.vicky.engine.classloader.core.JarClassLoader;
import cn.vicky.engine.classloader.core.exception.JclContextException;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * JclContext持有所有的JarClassLoader实例,那么他可以处理任何应用
 * 
 * @author Vicky.H
 * @email  ecliser@163.com
 * 
 */
public class JclContext {
    private static final Map<String, JarClassLoader> loaders = Collections
            .synchronizedMap( new HashMap<String, JarClassLoader>() );
    public static final String DEFAULT_NAME = "jcl";

    public JclContext() {
        validate();
    }

    private void validate() {
        if( isLoaded() ) {
            throw new JclContextException( "Context already loaded. Destroy the existing context to create a new one." );
        }
    }

    public static boolean isLoaded() {
        return !loaders.isEmpty();
    }

    /**
     * Populates the context with JarClassLoader instances
     * 
     * @param name
     * @param jcl
     */
    public void addJcl(String name, JarClassLoader jcl) {
        if( loaders.containsKey( name ) )
            throw new JclContextException( "JarClassLoader[" + name + "] already exist. Name must be unique" );

        loaders.put( name, jcl );
    }

    /**
     * Clears the context
     */
    public static void destroy() {
        if( isLoaded() ) {
            loaders.clear();
        }
    }

    public static JarClassLoader get() {
        return loaders.get( DEFAULT_NAME );
    }

    public static JarClassLoader get(String name) {
        return loaders.get( name );
    }

    public static Map<String, JarClassLoader> getAll() {
        return Collections.unmodifiableMap( loaders );
    }
}
