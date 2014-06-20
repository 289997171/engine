package cn.vicky.engine.classloader.core;

import java.io.InputStream;
import java.net.URL;

/**
 * 类加载器代理器
 * 
 * @author Vicky.H
 * @email ecliser@163.com
 * 
 */
public class DelegateProxyClassLoader extends ProxyClassLoader {

	private final AbstractClassLoader delegate;

	/**
         * 构建一个新的DelegateProxyClassLoader实例
	 * 
	 * @param delegate
	 *            instance of AbstractClassLoader where to delegate
	 * @throws NullPointerException
	 *             if delegate is null
	 */
	public DelegateProxyClassLoader(AbstractClassLoader delegate) throws NullPointerException {
		super();
		if (delegate == null)
			throw new NullPointerException("delegate can't be null");
		this.delegate = delegate;
		this.order = 15;
	}

        @Override
	public Class loadClass(String className, boolean resolveIt) {
		Class result;
		try {
			result = delegate.loadClass(className, resolveIt);
		} catch (ClassNotFoundException e) {
			return null;
		}
		return result;
	}

        @Override
	public InputStream loadResource(String name) {
		return delegate.getResourceAsStream(name);
	}

	@Override
	public URL findResource(String name) {
		return delegate.getResource(name);
	}

	public AbstractClassLoader getDelegate() {
		return delegate;
	}
}
