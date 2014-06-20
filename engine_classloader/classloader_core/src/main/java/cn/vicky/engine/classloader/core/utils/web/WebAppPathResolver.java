package cn.vicky.engine.classloader.core.utils.web;

import cn.vicky.engine.classloader.core.utils.PathResolver;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletContext;

/**
 * 解析路径中的jar,路径必须以<b>webapp:</b>开始
 * 
 * @author Vicky.H
 * @email  ecliser@163.com
 * 
 */
public class WebAppPathResolver implements PathResolver {

    private static final Logger logger = Logger.getLogger( WebAppPathResolver.class.getName() );

    private static final String JAR = ".jar";
    private static final String WEB_APP = "webapp:";
    private final ServletContext servletContext;

    public WebAppPathResolver(ServletContext servletContext) {
        this.servletContext = servletContext;
    }

    /**
     * Resolves path to jar files and folder in a web application
     * 
     * @see org.xeustechnologies.jcl.utils.PathResolver#resolvePath(java.lang.String)
     */
    @Override
    public Object[] resolvePath(String path) {
        if (path.startsWith( WEB_APP )) {
            String webpath = "/" + path.split( ":" )[1];

            if (isJar( webpath )) {
                if (logger.isLoggable( Level.FINEST )) {
                    logger.log( Level.FINEST, "Found jar: {0}", webpath);
                }

                return new InputStream[] { servletContext.getResourceAsStream( webpath ) };
            }

            Set<String> paths = servletContext.getResourcePaths( webpath );

            if (paths.size() > 0) {
                Iterator<String> itr = paths.iterator();
                List<InputStream> streams = new ArrayList<InputStream>();

                while (itr.hasNext()) {
                    String source = itr.next();

                    if (isJar( source )) {
                        InputStream stream = servletContext.getResourceAsStream( source );

                        if (stream != null) {
                            if (logger.isLoggable( Level.FINEST )) {
                                logger.log( Level.FINEST, "Found jar: {0}", source);
                            }

                            streams.add( stream );
                        }
                    }
                }

                return streams.toArray( new InputStream[streams.size()] );
            }

        }

        return null;
    }

    private boolean isJar(String path) {
        return path.toLowerCase().endsWith( JAR );
    }
}