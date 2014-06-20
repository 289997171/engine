package cn.vicky.engine.classloader.core;

import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/**
 * 复合代理加载器类,实现了抽象类加载器。
 * 
 * @author Vicky.H
 * @email ecliser@163.com
 * 
 */
public class CompositeProxyClassLoader extends ProxyClassLoader {
	private final List<ProxyClassLoader> proxyClassLoaders = new ArrayList<ProxyClassLoader>();

	public CompositeProxyClassLoader() {
		super();
	}

        @Override
	public Class loadClass(String className, boolean resolveIt) {
		Class result = null;
		Iterator<ProxyClassLoader> iterator = proxyClassLoaders.iterator();
		while (result == null && iterator.hasNext()) {
			result = iterator.next().loadClass(className, resolveIt);
		}
		return result;
	}
 
        @Override
	public InputStream loadResource(String name) {
		InputStream result = null;
		Iterator<ProxyClassLoader> iterator = proxyClassLoaders.iterator();
		while (result == null && iterator.hasNext()) {
			result = iterator.next().loadResource(name);
		}
		return result;
	}

	@Override
	public URL findResource(String name) {
		URL result = null;
		Iterator<ProxyClassLoader> iterator = proxyClassLoaders.iterator();
		while (result == null && iterator.hasNext()) {
			result = iterator.next().findResource(name);
		}
		return result;
	}

	/**
	 * @return
	 * @see java.util.List#isEmpty()
	 */
	public boolean isEmpty() {
		return proxyClassLoaders.isEmpty();
	}

	public boolean contains(Object o) {
		return proxyClassLoaders.contains(o);
	}

	public boolean add(ProxyClassLoader e) {
		return proxyClassLoaders.add(e);
	}

	public boolean remove(ProxyClassLoader o) {
		return proxyClassLoaders.remove(o);
	}

	public boolean addAll(Collection<? extends ProxyClassLoader> c) {
		return proxyClassLoaders.addAll(c);
	}

	public List<ProxyClassLoader> getProxyClassLoaders() {
		return proxyClassLoaders;
	}
}
