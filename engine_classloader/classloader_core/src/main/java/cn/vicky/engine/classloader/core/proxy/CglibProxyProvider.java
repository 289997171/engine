
package cn.vicky.engine.classloader.core.proxy;

import cn.vicky.engine.classloader.core.JclUtils;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;

/**
 * cglib 是一个强大的，高性能，高质量的Code生成类库，它可以在运行期扩展Java类与实现Java接口。
 *      Hibernate用它来实现PO(Persistent Object 持久化对象)字节码的动态生成。
 * 创建cglib代理
 * 
 * @author Vicky.H
 * @email ecliser@163.com
 * 
 */
public class CglibProxyProvider implements ProxyProvider {

    private class CglibProxyHandler implements MethodInterceptor {
        private final Object delegate;

        public CglibProxyHandler(Object delegate) {
            this.delegate = delegate;
        }

        /**
         * 
         * @see net.sf.cglib.proxy.MethodInterceptor#intercept(java.lang.Object,
         *      java.lang.reflect.Method, java.lang.Object[],
         *      net.sf.cglib.proxy.MethodProxy)
         */
        @Override
        public Object intercept(Object obj, Method method, Object[] args, MethodProxy proxy) throws Throwable {
            Method delegateMethod = delegate.getClass().getMethod( method.getName(), method.getParameterTypes() );
            return delegateMethod.invoke( delegate, args );
        }
    }

    @Override
    public Object createProxy(Object object, Class superClass, Class[] interfaces, ClassLoader cl) {
        CglibProxyHandler handler = new CglibProxyHandler( object );

        Enhancer enhancer = new Enhancer();

        if( superClass != null ) {
            enhancer.setSuperclass( superClass );
        }

        enhancer.setCallback( handler );

        if( interfaces != null ) {
            List<Class> il = new ArrayList<>();

            for( Class i : interfaces ) {
                if( i.isInterface() ) {
                    il.add( i );
                }
            }

            enhancer.setInterfaces( il.toArray( new Class[il.size()] ) );
        }

        enhancer.setClassLoader( cl == null ? JclUtils.class.getClassLoader() : cl );

        return enhancer.create();
    }
}
