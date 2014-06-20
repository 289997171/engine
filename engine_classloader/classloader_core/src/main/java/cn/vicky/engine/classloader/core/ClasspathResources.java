package cn.vicky.engine.classloader.core;

import cn.vicky.engine.classloader.core.exception.JclException;
import cn.vicky.engine.classloader.core.exception.ResourceNotFoundException;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * 通过files/paths加载资源.从而构建一个位于本地classpath的类
 *
 * @author Vicky.H
 * @email ecliser@163.com
 */
public class ClasspathResources extends JarResources {

    private static final Logger logger = Logger.getLogger(ClasspathResources.class.getName());
    private boolean ignoreMissingResources;

    public ClasspathResources() {
        super();
        ignoreMissingResources = Configuration.suppressMissingResourceException();
    }

    /**
     * 记载本地资源
     *
     * @param resource
     */
    private void loadResourceContent(String resource, String pack) {
        File resourceFile = new File(resource);
        String entryName = "";
        FileInputStream fis = null;
        byte[] content = null;
        try {
            fis = new FileInputStream(resourceFile);
            content = new byte[(int) resourceFile.length()];

            if (fis.read(content) != -1) {

                if (pack.length() > 0) {
                    entryName = pack + "/";
                }

                entryName += resourceFile.getName();

                if (jarEntryContents.containsKey(entryName)) {
                    if (!collisionAllowed) {
                        throw new JclException("Resource " + entryName + " already loaded");
                    } else {
                        if (logger.isLoggable(Level.FINEST)) {
                            logger.log(Level.FINEST, "Resource {0} already loaded; ignoring entry...", entryName);
                        }
                        return;
                    }
                }

                if (logger.isLoggable(Level.FINEST)) {
                    logger.log(Level.FINEST, "Loading resource: {0}", entryName);
                }

                jarEntryContents.put(entryName, content);
            }
        } catch (IOException e) {
            throw new JclException(e);
        } finally {
            try {
                fis.close();
            } catch (IOException e) {
                throw new JclException(e);
            }
        }
    }

    /**
     * 尝试加载远程资源(jars, properties files, etc)
     *
     * @param url
     */
    private void loadRemoteResource(URL url) {
        if (logger.isLoggable(Level.FINEST)) {
            logger.finest("Attempting to load a remote resource.");
        }

        if (url.toString().toLowerCase().endsWith(".jar")) {
            loadJar(url);
            return;
        }

        InputStream stream = null;
        ByteArrayOutputStream out = null;
        try {
            stream = url.openStream();
            out = new ByteArrayOutputStream();

            int byt;
            while (((byt = stream.read()) != -1)) {
                out.write(byt);
            }

            byte[] content = out.toByteArray();

            if (jarEntryContents.containsKey(url.toString())) {
                if (!collisionAllowed) {
                    throw new JclException("Resource " + url.toString() + " already loaded");
                } else {
                    if (logger.isLoggable(Level.FINEST)) {
                        logger.log(Level.FINEST, "Resource {0} already loaded; ignoring entry...", url.toString());
                    }
                    return;
                }
            }

            if (logger.isLoggable(Level.FINEST)) {
                logger.finest("Loading remote resource.");
            }

            jarEntryContents.put(url.toString(), content);
        } catch (IOException e) {
            throw new JclException(e);
        } finally {
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e) {
                    throw new JclException(e);
                }
            }
            if (stream != null) {
                try {
                    stream.close();
                } catch (IOException e) {
                    throw new JclException(e);
                }
            }
        }
    }

    /**
     * 加载类的内容
     *
     * @param clazz
     * @param pack
     */
    private void loadClassContent(String clazz, String pack) {
        File cf = new File(clazz);
        FileInputStream fis = null;
        String entryName = "";
        byte[] content = null;

        try {
            fis = new FileInputStream(cf);
            content = new byte[(int) cf.length()];

            if (fis.read(content) != -1) {
                entryName = pack + "/" + cf.getName();

                if (jarEntryContents.containsKey(entryName)) {
                    if (!collisionAllowed) {
                        throw new JclException("Class " + entryName + " already loaded");
                    } else {
                        if (logger.isLoggable(Level.FINEST)) {
                            logger.log(Level.FINEST, "Class {0} already loaded; ignoring entry...", entryName);
                        }
                        return;
                    }
                }

                if (logger.isLoggable(Level.FINEST)) {
                    logger.log(Level.FINEST, "Loading class: {0}", entryName);
                }

                jarEntryContents.put(entryName, content);
            }
        } catch (IOException e) {
            throw new JclException(e);
        } finally {
            if (fis != null) {
                try {
                    fis.close();
                } catch (IOException e) {
                    throw new JclException(e);
                }
            }
        }

    }

    /**
     * 读取本地或远程的地缘
     *
     * @param url
     */
    public void loadResource(URL url) {
        try {
            // Is Local
            loadResource(new File(url.toURI()), "");
        } catch (IllegalArgumentException iae) {
            // Is Remote
            loadRemoteResource(url);
        } catch (URISyntaxException e) {
            throw new JclException("URISyntaxException", e);
        }
    }

    /**
     * 读取本地的.jar  或 /class/*.class  或 lib/*.jar 文件资源
     *
     * @param path
     */
    public void loadResource(String path) {
        if (logger.isLoggable(Level.FINEST)) {
            logger.log(Level.FINEST, "Resource: {0}", path);
        }

        File fp = new File(path);

        if (!fp.exists() && !ignoreMissingResources) {
            throw new JclException("File/Path does not exist");
        }

        loadResource(fp, "");
    }

    /**
     * 读取本地的.jar  或 /class/*.class  或 lib/*.jar 文件资源
     *
     * @param fol
     * @param packName
     */
    private void loadResource(File fol, String packName) {
        if (fol.isFile()) {
            if (fol.getName().toLowerCase().endsWith(".class")) {
                loadClassContent(fol.getAbsolutePath(), packName);
            } else {
                if (fol.getName().toLowerCase().endsWith(".jar")) {
                    loadJar(fol.getAbsolutePath());
                } else {
                    loadResourceContent(fol.getAbsolutePath(), packName);
                }
            }

            return;
        }

        if (fol.list() != null) {
            for (String f : fol.list()) {
                File fl = new File(fol.getAbsolutePath() + "/" + f);

                String pn = packName;

                if (fl.isDirectory()) {

                    if (!pn.equals("")) {
                        pn = pn + "/";
                    }

                    pn = pn + fl.getName();
                }

                loadResource(fl, pn);
            }
        }
    }

    /**
     * 删除已经加载的资源
     *
     * @param resource
     */
    public void unload(String resource) {
        if (jarEntryContents.containsKey(resource)) {
            if (logger.isLoggable(Level.FINEST)) {
                logger.log(Level.FINEST, "Removing resource {0}", resource);
            }
            jarEntryContents.remove(resource);
        } else {
            throw new ResourceNotFoundException(resource, "Resource not found in local ClasspathResources");
        }
    }

    public boolean isCollisionAllowed() {
        return collisionAllowed;
    }

    public void setCollisionAllowed(boolean collisionAllowed) {
        this.collisionAllowed = collisionAllowed;
    }

    public boolean isIgnoreMissingResources() {
        return ignoreMissingResources;
    }

    public void setIgnoreMissingResources(boolean ignoreMissingResources) {
        this.ignoreMissingResources = ignoreMissingResources;
    }
}
