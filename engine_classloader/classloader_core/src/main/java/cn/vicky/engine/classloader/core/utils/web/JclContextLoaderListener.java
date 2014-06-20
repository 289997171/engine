package cn.vicky.engine.classloader.core.utils.web;

import cn.vicky.engine.classloader.core.context.XmlContextLoader;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

/**
 * @author Vicky.H
 * @email ecliser@163.com
 *
 */
public class JclContextLoaderListener implements ServletContextListener {

    private static final String JCL_CONTEXT = "jcl-context";
    protected XmlContextLoader contextLoader;

    /**
     * Destroys the context
     *
     * @param sce
     * @see
     * javax.servlet.ServletContextListener#contextDestroyed(javax.servlet.ServletContextEvent)
     */
    public void contextDestroyed(ServletContextEvent sce) {
        contextLoader.unloadContext();
    }

    /**
     * The context is initialised from xml on web application's deploy-time
     *
     * @param sce
     * @see
     * javax.servlet.ServletContextListener#contextInitialized(javax.servlet.ServletContextEvent)
     */
    public void contextInitialized(ServletContextEvent sce) {
        String jclConfig = sce.getServletContext().getInitParameter(JCL_CONTEXT);

        contextLoader = new XmlContextLoader(jclConfig);
        contextLoader.addPathResolver(new WebAppPathResolver(sce.getServletContext()));
        contextLoader.loadContext();
    }
}
