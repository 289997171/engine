package cn.vicky.engine.classloader.core.context;

import cn.vicky.engine.classloader.core.AbstractClassLoader;
import cn.vicky.engine.classloader.core.JarClassLoader;
import cn.vicky.engine.classloader.core.ProxyClassLoader;
import cn.vicky.engine.classloader.core.exception.JclContextException;
import cn.vicky.engine.classloader.core.utils.PathResolver;
import java.io.IOException;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.SchemaFactory;

import org.objenesis.Objenesis;
import org.objenesis.ObjenesisStd;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 * 通过XML文件来配置JclContext 查看jcl-context.xsd获得配置
 *
 * @author Vicky.H
 * @email ecliser@163.com
 *
 */
public class XmlContextLoader implements JclContextLoader {

    private static final String CLASSPATH = "classpath:";
    private static final String ELEMENT_JCL = "jcl";
    private static final String ELEMENT_SOURCES = "sources";
    private static final String ELEMENT_SOURCE = "source";
    private static final String ELEMENT_LOADERS = "loaders";
    private static final String ELEMENT_LOADER = "loader";
    private static final String ELEMENT_ENABLED = "enabled";
    private static final String ELEMENT_ORDER = "order";
    private static final String ELEMENT_STRICT = "strict";
    private static final String ELEMENT_BOOT_DELEGATION = "bootDelegation";
    private static final String ATTRIBUTE_CLASS = "class";
    private static final String ATTRIBUTE_NAME = "name";

    private static final String JCL_BOOTOSGI = "jcl.bootosgi";
    private static final String JCL_SYSTEM = "jcl.system";
    private static final String JCL_THREAD = "jcl.thread";
    private static final String JCL_LOCAL = "jcl.local";
    private static final String JCL_CURRENT = "jcl.current";
    private static final String JCL_PARENT = "jcl.parent";

    private static final String XML_SCHEMA_LANG = "http://www.w3.org/2001/XMLSchema";
    private static final String JCL_CONTEXT_SCHEMA = "cn/vicky/engine/classloader/core/context/jcl-context.xsd";

    private final String file;
    private final JclContext jclContext;

    private final List<PathResolver> pathResolvers = new ArrayList<>();

    private static final Logger logger = Logger.getLogger(XmlContextLoader.class.getName());

    public XmlContextLoader(String file) {
        this.file = file;
        jclContext = new JclContext();
    }

    /**
     * Loads the JCL context from XML file
     *
     * @see org.xeustechnologies.jcl.context.JclContextLoader#loadContext()
     */
    @Override
    public void loadContext() {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setValidating(false);
        factory.setNamespaceAware(true);

        SchemaFactory schemaFactory = SchemaFactory.newInstance(XML_SCHEMA_LANG);

        try {
            factory.setSchema(schemaFactory.newSchema(new Source[]{new StreamSource(getClass().getClassLoader()
                .getResourceAsStream(JCL_CONTEXT_SCHEMA))}));
        } catch (SAXException e) {
            throw new JclContextException(e);
        }

        try {
            DocumentBuilder builder = factory.newDocumentBuilder();

            Document d = null;

            if (file.startsWith(CLASSPATH)) {
                d = builder.parse(getClass().getClassLoader().getResourceAsStream(file.split(CLASSPATH)[1]));
            } else {
                d = builder.parse(file);
            }

            NodeList nl = d.getElementsByTagName(ELEMENT_JCL);
            for (int i = 0; i < nl.getLength(); i++) {
                Node n = nl.item(i);

                String name = n.getAttributes().getNamedItem(ATTRIBUTE_NAME).getNodeValue();

                JarClassLoader jcl = new JarClassLoader();

                NodeList config = n.getChildNodes();

                for (int j = 0; j < config.getLength(); j++) {
                    Node c = config.item(j);
                    switch (c.getNodeName()) {
                        case ELEMENT_LOADERS:
                            processLoaders(jcl, c);
                            break;
                        case ELEMENT_SOURCES:
                            processSources(jcl, c);
                            break;
                    }
                }

                jclContext.addJcl(name, jcl);

                if (logger.isLoggable(Level.FINER)) {
                    logger.log(Level.FINER, "JarClassLoader[{0}] loaded into context.", name);
                }
            }

        } catch (SAXParseException e) {
            JclContextException we = new JclContextException(e.getMessage() + " [" + file + " (" + e.getLineNumber()
                    + ", " + e.getColumnNumber() + ")]");
            we.setStackTrace(e.getStackTrace());

            throw we;
        } catch (JclContextException e) {
            throw e;
        } catch (ParserConfigurationException | SAXException | IOException | DOMException e) {
            throw new JclContextException(e);
        }
    }

    /**
     * Unloads the context
     *
     * @see org.xeustechnologies.jcl.context.JclContextLoader#unloadContext()
     */
    @Override
    public void unloadContext() {
        JclContext.destroy();
    }

    private void processSources(JarClassLoader jcl, Node c) {
        NodeList sources = c.getChildNodes();
        for (int k = 0; k < sources.getLength(); k++) {
            Node s = sources.item(k);

            if (s.getNodeName().equals(ELEMENT_SOURCE)) {
                String path = s.getTextContent();
                Object[] res = null;

                for (PathResolver pr : pathResolvers) {
                    res = pr.resolvePath(path);

                    if (res != null) {
                        for (Object r : res) {
                            jcl.add(r);
                        }

                        break;
                    }
                }

                if (res == null) {
                    jcl.add(path);
                }
            }
        }
    }

    private void processLoaders(JarClassLoader jcl, Node c) {
        NodeList loaders = c.getChildNodes();
        for (int k = 0; k < loaders.getLength(); k++) {
            Node l = loaders.item(k);
            if (l.getNodeName().equals(ELEMENT_LOADER)) {
                switch (l.getAttributes().getNamedItem(ATTRIBUTE_NAME).getNodeValue()) {
                    case JCL_PARENT:
                        processLoader(jcl.getParentLoader(), l);
                        break;
                    case JCL_CURRENT:
                        processLoader(jcl.getCurrentLoader(), l);
                        break;
                    case JCL_LOCAL:
                        processLoader(jcl.getLocalLoader(), l);
                        break;
                    case JCL_THREAD:
                        processLoader(jcl.getThreadLoader(), l);
                        break;
                    case JCL_SYSTEM:
                        processLoader(jcl.getSystemLoader(), l);
                        break;
                    case JCL_BOOTOSGI:
                        processLoader(jcl.getOsgiBootLoader(), l);
                        break;
                    default:
                        Objenesis objenesis = new ObjenesisStd();
                        Class<?> clazz = null;
                        try {
                            clazz = getClass().getClassLoader().loadClass(
                                    l.getAttributes().getNamedItem(ATTRIBUTE_CLASS).getNodeValue());
                        } catch (DOMException | ClassNotFoundException e) {
                            throw new JclContextException(e);
                        }   ProxyClassLoader pcl = (ProxyClassLoader) objenesis.newInstance(clazz);
                    jcl.addLoader(pcl);
                        processLoader(pcl, l);
                        break;
                }
            }
        }
    }

    private void processLoader(ProxyClassLoader loader, Node node) {
        NodeList oe = node.getChildNodes();
        for (int i = 0; i < oe.getLength(); i++) {
            Node noe = oe.item(i);
            if (noe.getNodeName().equals(ELEMENT_ORDER) && !(loader instanceof AbstractClassLoader.OsgiBootLoader)) {
                loader.setOrder(Integer.parseInt(noe.getTextContent()));
            } else if (noe.getNodeName().equals(ELEMENT_ENABLED)) {
                loader.setEnabled(Boolean.parseBoolean(noe.getTextContent()));
            } else if (noe.getNodeName().equals(ELEMENT_STRICT)
                    && loader instanceof AbstractClassLoader.OsgiBootLoader) {
                ((AbstractClassLoader.OsgiBootLoader) loader).setStrictLoading(Boolean.parseBoolean(noe
                        .getTextContent()));
            } else if (noe.getNodeName().equals(ELEMENT_BOOT_DELEGATION)
                    && loader instanceof AbstractClassLoader.OsgiBootLoader) {
                ((AbstractClassLoader.OsgiBootLoader) loader).setBootDelagation(noe.getTextContent().split(","));
            }
        }

        if (logger.isLoggable(Level.FINEST)) {
            logger.log(Level.FINEST, "Loader[{0}] configured: [{1}, {2}]", new Object[]{loader.getClass().getName(), loader.getOrder(), loader.isEnabled()});
        }
    }

    public void addPathResolver(PathResolver pr) {
        pathResolvers.add(pr);
    }
}
