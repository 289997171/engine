package cn.vicky.engine.classloader.core.content;

import cn.vicky.engine.classloader.core.JarClassLoader;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * 默认的classloader上下文,拥有一个JarClassLoader
 *
 * @author Vicky.H
 * @email ecliser@163.com
 *
 */
public class DefaultContextLoader implements JclContextLoader {

    private final JclContext jclContext;
    private final JarClassLoader jcl;

    private static final Logger logger = Logger.getLogger(DefaultContextLoader.class.getName());

    public DefaultContextLoader(JarClassLoader jcl) {
        jclContext = new JclContext();
        this.jcl = jcl;
    }

    /**
     * 加载一个classloader实例
     * 
     *
     * @see org.xeustechnologies.jcl.context.JclContextLoader#loadContext()
     */
    @Override
    public void loadContext() {
        jclContext.addJcl(JclContext.DEFAULT_NAME, jcl);

        if (logger.isLoggable(Level.FINER)) {
            logger.finer("Default JarClassLoader loaded into context.");
        }
    }

    @Override
    public void unloadContext() {
        JclContext.destroy();
    }
}
