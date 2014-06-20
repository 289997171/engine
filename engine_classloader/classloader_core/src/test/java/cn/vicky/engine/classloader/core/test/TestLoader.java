package cn.vicky.engine.classloader.core.test;

import java.io.InputStream;
import java.net.URL;

public class TestLoader extends cn.vicky.engine.classloader.core.ProxyClassLoader {

    @Override
    public Class loadClass(String className, boolean resolveIt) {
        return null;
    }

    @Override
    public InputStream loadResource(String name) {
        return null;
    }

    @Override
    public URL findResource(String name) {
        return null;
    }

}
