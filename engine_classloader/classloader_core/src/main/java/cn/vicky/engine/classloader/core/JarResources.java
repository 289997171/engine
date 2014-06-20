package cn.vicky.engine.classloader.core;

import cn.vicky.engine.classloader.core.exception.JclException;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * JarResources 读取jar文件兵器加载class/bytes 到HashMap中
 * @author Vicky.H
 * @email  ecliser@163.com
 * 
 */
public class JarResources {

    // 地址
    protected String baseUrl;
    // jar实体保存
    protected Map<String, byte[]> jarEntryContents;
    protected boolean collisionAllowed;

    private static final Logger logger = Logger.getLogger( JarResources.class.getName() );

    /**
     * 默认构造函数
     */
    public JarResources() {
        jarEntryContents = new HashMap<>();
        collisionAllowed = Configuration.suppressCollisionException();
    }

    /**
     * 获得资源的地址
     * 
     * @param name
     * @return URL
     */
    public URL getResourceURL(String name) {
        if (baseUrl == null) {
            throw new JclException( "non-URL accessible resource" );
        }
        if (jarEntryContents.get( name ) != null) {
            try {
                return new URL( baseUrl + name );
            } catch (MalformedURLException e) {
                throw new JclException( e );
            }
        }

        return null;
    }

    /**
     * 获得资源
     * 
     * @param name
     * @return byte[]
     */
    public byte[] getResource(String name) {
        return jarEntryContents.get( name );
    }

    /**
     * 返回一个不可以修改的map,其内容包含jar中的所有资源
     * 
     * @return Map
     */
    public Map<String, byte[]> getResources() {
        return Collections.unmodifiableMap( jarEntryContents );
    }

    /**
     * 加载完整路径的jar文件
     * 
     * @param jarFile
     */
    public void loadJar(String jarFile) {
        if (logger.isLoggable( Level.FINEST ))
            logger.log( Level.FINEST, "Loading jar: {0}", jarFile);

        FileInputStream fis = null;
        try {
            File file = new File( jarFile );
            baseUrl = "jar:" + file.toURI().toString() + "!/";
            fis = new FileInputStream( file );
            loadJar( fis );
        } catch (FileNotFoundException e) {
            baseUrl = null;
            throw new JclException( e );
        } finally {
            if (fis != null)
                try {
                    fis.close();
                } catch (IOException e) {
                    throw new JclException( e );
                }
        }
    }

    /**
     * 通过指定的URL地址加载jar文件
     * 
     * @param url
     */
    public void loadJar(URL url) {
        if (logger.isLoggable( Level.FINEST ))
            logger.log( Level.FINEST, "Loading jar: {0}", url.toString());

        InputStream in = null;
        try {
            baseUrl = "jar:" + url.toString() + "!/";
            in = url.openStream();
            loadJar( in );
        } catch (IOException e) {
            baseUrl = null;
            throw new JclException( e );
        } finally {
            if (in != null)
                try {
                    in.close();
                } catch (IOException e) {
                    throw new JclException( e );
                }
        }
    }

    /**
     * 通过输入流加载jar的内容
     * 
     * @param jarStream
     */
    public void loadJar(InputStream jarStream) {

        BufferedInputStream bis = null;
        JarInputStream jis = null;

        try {
            bis = new BufferedInputStream( jarStream );
            jis = new JarInputStream( bis );

            JarEntry jarEntry = null;
            while (( jarEntry = jis.getNextJarEntry() ) != null) {
                if (logger.isLoggable( Level.FINEST ))
                    logger.finest( dump( jarEntry ) );

                if (jarEntry.isDirectory()) {
                    continue;
                }

                if (jarEntryContents.containsKey( jarEntry.getName() )) {
                    if (!collisionAllowed)
                        throw new JclException( "Class/Resource " + jarEntry.getName() + " already loaded" );
                    else {
                        if (logger.isLoggable( Level.FINEST ))
                            logger.log( Level.FINEST, "Class/Resource {0} already loaded; ignoring entry...", jarEntry.getName());
                        continue;
                    }
                }

                if (logger.isLoggable( Level.FINEST ))
                    logger.log( Level.FINEST,"Entry Name: {0}" + ", " + "Entry Size: {1}" , new Object[]{jarEntry.getName(), jarEntry.getSize()});

                byte[] b = new byte[2048];
                try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
                    int len = 0;
                    while (( len = jis.read( b ) ) > 0) {
                        out.write( b, 0, len );
                    }
                    
                    // add to internal resource HashMap
                    jarEntryContents.put( jarEntry.getName(), out.toByteArray() );
                    
                    if (logger.isLoggable( Level.FINEST ))
                        logger.log( Level.FINEST, "{0}: size={1} ,csize={2}", new Object[]{jarEntry.getName(), out.size(), jarEntry.getCompressedSize()});
                }
            }
        } catch (IOException e) {
            throw new JclException( e );
        } catch (NullPointerException e) {
            if (logger.isLoggable( Level.FINEST ))
                logger.finest( "Done loading." );
        } finally {
            if (jis != null)
                try {
                    jis.close();
                } catch (IOException e) {
                    throw new JclException( e );
                }

            if (bis != null)
                try {
                    bis.close();
                } catch (IOException e) {
                    throw new JclException( e );
                }
        }
    }

    /**
     * For debugging
     * 
     * @param je
     * @return String
     */
    private String dump(JarEntry je) {
        StringBuilder sb = new StringBuilder();
        if (je.isDirectory()) {
            sb.append( "d " );
        } else {
            sb.append( "f " );
        }

        if (je.getMethod() == JarEntry.STORED) {
            sb.append( "stored   " );
        } else {
            sb.append( "defalted " );
        }

        sb.append( je.getName() );
        sb.append( "\t" );
        sb.append( je.getSize());
        if (je.getMethod() == JarEntry.DEFLATED) {
            sb.append("/").append( je.getCompressedSize());
        }

        return ( sb.toString() );
    }
}
