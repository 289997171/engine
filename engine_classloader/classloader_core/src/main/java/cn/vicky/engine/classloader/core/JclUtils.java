package cn.vicky.engine.classloader.core;

import cn.vicky.engine.classloader.core.exception.JclException;
import cn.vicky.engine.classloader.core.proxy.ProxyProviderFactory;
import cn.vicky.engine.classloader.core.utils.ObjectCloner;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/**
 * This class has some important utility methods commonly required when using
 * JCL
 * @author Vicky.H
 * @email  ecliser@163.com
 * 
 */
@SuppressWarnings("unchecked")
public class JclUtils {

    public static Object createProxy(Object object, Class superClass, Class[] interfaces, ClassLoader cl) {
        return ProxyProviderFactory.create().createProxy( object, superClass, interfaces, cl );
    }

    /**
     * Casts the object ref to the passed interface class ref. It actually
     * returns a dynamic proxy for the passed object
     * 
     * @param object
     * @param clazz
     * @return castable
     */
    public static Object toCastable(Object object, Class clazz) {
        return createProxy( object, clazz, new Class[] { clazz }, null );
    }

    /**
     * Casts the object ref to the passed interface class ref. It actually
     * returns a dynamic proxy for the passed object
     * 
     * @param object
     * @param clazz
     *            []
     * @return castable
     */
    public static Object toCastable(Object object, Class[] clazz) {
        return createProxy( object, clazz[0], clazz, null );
    }

    /**
     * Casts the object ref to the passed interface class ref
     * 
     * @param object
     * @param clazz
     * @param cl
     * @return castable
     */
    public static Object toCastable(Object object, Class clazz, ClassLoader cl) {
        return createProxy( object, clazz, new Class[] { clazz }, cl );
    }

    /**
     * Casts the object ref to the passed interface class ref
     * 
     * @param object
     * @param clazz
     *            []
     * @param cl
     * @return castable
     */
    public static Object toCastable(Object object, Class[] clazz, ClassLoader cl) {
        return createProxy( object, clazz[0], clazz, cl );
    }

    /**
     * Casts the object ref to the passed interface class ref and returns it
     * 
     * @param <T>
     * @param object
     * @param clazz
     * @return T reference
     */
    public static <T> T cast(Object object, Class<T> clazz) {
        return (T) toCastable( object, clazz, null );
    }

    /**
     * Casts the object ref to the passed interface class ref and returns it
     * 
     * @param <T>
     * @param object
     * @param clazz
     * @param cl
     * @return T reference
     */
    public static <T> T cast(Object object, Class<T> clazz, ClassLoader cl) {
        return (T) toCastable( object, clazz, cl );
    }

    /**
     * Deep clones the Serializable objects in the current classloader. This
     * method is slow and uses Object streams to clone Serializable objects.
     * 
     * This method is now deprecated because of its inefficiency and the
     * limitation to clone Serializable objects only. The use of deepClone or
     * shallowClone is now recommended
     * 
     * @param original
     * @return clone
     * @deprecated As of release 2.0, replaced by
     *             {@link #deepClone(Object original)}
     */
    @Deprecated
    public static Object clone(Object original) {
        Object clone = null;

        try (ByteArrayOutputStream bos = new ByteArrayOutputStream( 5120 )) { // Increased buffer size
            ObjectOutputStream out = new ObjectOutputStream( bos );
            out.writeObject( original );
            out.flush();
            out.close();

            ObjectInputStream in = new ObjectInputStream( new ByteArrayInputStream( bos.toByteArray() ) );
            clone = in.readObject();

            in.close();
        }catch (Exception e) {
            throw new JclException( e );
        }

        return clone;
    }

    /**
     * Deep clones any object
     * 
     * @param original
     * @return clone
     */
    public static Object deepClone(Object original) {
        ObjectCloner cloner = new ObjectCloner();

        return cloner.deepClone( original );
    }

    /**
     * Shallow clones any object
     * 
     * @param original
     * @return clone
     */
    public static Object shallowClone(Object original) {
        ObjectCloner cloner = new ObjectCloner();

        return cloner.shallowClone( original );
    }
}