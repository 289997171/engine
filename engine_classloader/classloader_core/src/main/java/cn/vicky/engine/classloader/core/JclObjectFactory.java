package cn.vicky.engine.classloader.core;

import cn.vicky.engine.classloader.core.exception.JclException;
import java.lang.reflect.InvocationTargetException;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 * A factory class that loads classes from specified JarClassLoader and tries to instantiate their objects
 * 一个类加载类工厂,用于从指定的JarClassLoader中实例化的对象
 * 
 * @author Vicky.H
 * @email ecliser@163.com
 * 
 */
public class JclObjectFactory {
    private static final JclObjectFactory jclObjectFactory = new JclObjectFactory();
    private static final Logger logger = Logger.getLogger( JclObjectFactory.class.getName() );
    
    private static boolean autoProxy;

    /**
     * 单例模式
     */
    private JclObjectFactory() {
        autoProxy = Configuration.autoProxy();
    }

    /**
     * 单例模式
     * 
     * @return JclObjectFactory
     */
    public static JclObjectFactory getInstance() {
        return jclObjectFactory;
    }

    /**
     * 返回单例,并设置工厂类是否启用自动代理
     * 
     * @param autoProxy
     * @return JclObjectFactory
     */
    public static JclObjectFactory getInstance(boolean autoProxy) {
        JclObjectFactory.autoProxy = autoProxy;
        return jclObjectFactory;
    }

    /**
     * 通过自定的类加载器,使用默认构造函数创建指定类型的对象
     * 
     * @param jcl
     * @param className
     * @return Object
     */
    public Object create(JarClassLoader jcl, String className) {
        return create( jcl, className, (Object[]) null );
    }

    /**
     * 通过自定的类加载器,使用带参数的造函数创建指定类型的对象
     * 
     * @param jcl
     * @param className
     * @param args
     * @return Object
     */
    public Object create(JarClassLoader jcl, String className, Object... args) {
        if (args == null || args.length == 0) {
            try {
                return newInstance( jcl.loadClass( className ).newInstance() );
            } catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
                throw new JclException( e );
            }
        }

        Class[] types = new Class[args.length];

        for (int i = 0; i < args.length; i++)
            types[i] = args[i].getClass();

        return create( jcl, className, args, types );
    }

    /**
     * 通过自定的类加载器,使用带参数以及参数类型.创建指定类型的对象
     * 
     * @param jcl
     * @param className
     * @param args
     * @param types
     * @return Object
     */
    public Object create(JarClassLoader jcl, String className, Object[] args, Class[] types) {
        Object obj = null;

        if (args == null || args.length == 0) {
            try {
                obj = jcl.loadClass( className ).newInstance();
            } catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
                throw new JclException( e );
            }
        } else {
            try {
                obj = jcl.loadClass( className ).getConstructor( types ).newInstance( args );
            } catch (ClassNotFoundException | NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
                throw new JclException( e );
            }
        }

        return newInstance( obj );
    }

    /**
     * 通过自定的类加载器,通过静态函数,创建类的对象
     * 
     * @param jcl
     * @param className
     * @param methodName
     * @param args
     * @return Object
     */
    public Object create(JarClassLoader jcl, String className, String methodName, Object... args) {
        if (args == null || args.length == 0) {
            try {
                return newInstance( jcl.loadClass( className ).getMethod( methodName ).invoke( null ) );
            } catch (ClassNotFoundException | NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
                throw new JclException( e );
            }
        }
        Class[] types = new Class[args.length];

        for (int i = 0; i < args.length; i++)
            types[i] = args[i].getClass();

        return create( jcl, className, methodName, args, types );
    }

    /**
     * 通过类加载器,类名,构造函数,构造函数参数,构造函数参数类型,创建类的对象
     * 
     * @param jcl
     * @param className
     * @param methodName
     * @param args
     * @param types
     * @return Object
     */
    public Object create(JarClassLoader jcl, String className, String methodName, Object[] args, Class[] types) {
        Object obj = null;
        if (args == null || args.length == 0) {
            try {
                obj = jcl.loadClass( className ).getMethod( methodName ).invoke( null );
            } catch (Exception e) {
                throw new JclException( e );
            }
        } else {
            try {
                obj = jcl.loadClass( className ).getMethod( methodName, types ).invoke( null, args );
            } catch (Exception e) {
                throw new JclException( e );
            }
        }

        return newInstance( obj );
    }

    /**
     * 创建对象的代理
     * 
     * @param object
     * @return
     */
    private Object newInstance(Object object) {
        if (autoProxy) {

            Class superClass = null;

            // Check class
            try {
                Class.forName( object.getClass().getSuperclass().getName() );
                superClass = object.getClass().getSuperclass();
            } catch (ClassNotFoundException e) {
            }

            Class[] interfaces = object.getClass().getInterfaces();

            List<Class> il = new ArrayList<>();

            // Check available interfaces
            for (Class i : interfaces) {
                try {
                    Class.forName( i.getClass().getName() );
                    il.add( i );
                } catch (ClassNotFoundException e) {
                }
            }

            if (logger.isLoggable( Level.FINER )) {
                logger.log( Level.FINER, "Class: {0}", superClass);
                logger.log( Level.FINER, "Class Interfaces: {0}", il);
            }

            if (superClass == null && il.isEmpty()) {
                throw new JclException( "Neither the class [" + object.getClass().getSuperclass().getName()
                        + "] nor all the implemented interfaces found in the current classloader" );
            }

            return JclUtils.createProxy( object, superClass, il.toArray( new Class[il.size()] ), null );
        }

        return object;
    }
}
