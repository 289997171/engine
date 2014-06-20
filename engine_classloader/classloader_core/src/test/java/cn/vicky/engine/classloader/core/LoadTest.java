package cn.vicky.engine.classloader.core;

import cn.vicky.engine.classloader.core.context.DefaultContextLoader;
import cn.vicky.engine.classloader.core.context.JclContext;
import cn.vicky.engine.classloader.core.context.JclContextLoader;
import cn.vicky.engine.classloader.core.context.XmlContextLoader;
import cn.vicky.engine.classloader.core.exception.JclContextException;
import cn.vicky.engine.classloader.core.proxy.CglibProxyProvider;
import cn.vicky.engine.classloader.core.proxy.ProxyProviderFactory;

import cn.vicky.engine.classloader.core.test.TestInterface;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

import junit.framework.Assert;
import junit.framework.AssertionFailedError;
import junit.framework.TestCase;

import org.junit.Test;
import org.junit.internal.runners.JUnit4ClassRunner;
import org.junit.runner.RunWith;

@SuppressWarnings("all")
@RunWith(JUnit4ClassRunner.class)
public class LoadTest extends TestCase {

    private static final Logger logger = Logger.getLogger(LoadTest.class.getName());

    @Test
    public void testWithResourceName() throws InstantiationException, IllegalAccessException, ClassNotFoundException {
        JarClassLoader jc = new JarClassLoader(new String[]{"./target/test-jcl.jar"});

        // New class
        Object testObj = jc.loadClass("cn.vicky.engine.classloader.core.test.Test").newInstance();
        assertNotNull(testObj);

        // Locally loaded
        testObj = jc.loadClass("cn.vicky.engine.classloader.core.test.Test").newInstance();
        assertNotNull(testObj);
    }

    @Test
    public void testPackagedResource() {
        JarClassLoader jc = new JarClassLoader(new String[]{"./target/test-jcl.jar"});

        InputStream is = jc.getResourceAsStream("test/test.properties");

        if (logger.isLoggable(Level.FINER)) {
            logger.finer(is.toString());
        }

        assertNotNull(is);
    }

    @Test
    public void testPackagedResourceURL() {
        JarClassLoader jc = new JarClassLoader(new String[]{"./target/test-jcl.jar"});

        URL url = jc.getResource("test/test.properties");

        assertNotNull(url);
    }

    @Test
    public void testMissingResourceURL() {
        JarClassLoader jc = new JarClassLoader(new String[]{"./target/test-jcl.jar"});

        URL url = jc.getResource("asdf/adsf");

        assertNull(url);
    }

    @Test
    public void testWithClassFolder() throws InstantiationException, IllegalAccessException, ClassNotFoundException {
        JarClassLoader jc = new JarClassLoader(new String[]{"./target/test-jcl.jar"});

        Object testObj = jc.loadClass("cn.vicky.engine.classloader.core.test.Test").newInstance();
        assertNotNull(testObj);
    }

    @Test
    public void testWithUrl() throws MalformedURLException, InstantiationException, IllegalAccessException,
            ClassNotFoundException {
        // URL url = new URL("http://localhost:8080/blank/test-jcl.jar");
        File f = new File("./target/test-jcl.jar");

        JarClassLoader jc = new JarClassLoader(new URL[]{f.toURI().toURL()});
        Object testObj = jc.loadClass("cn.vicky.engine.classloader.core.test.Test").newInstance();
        assertNotNull(testObj);
    }

    @Test
    public void testWithInputStream() throws InstantiationException, IllegalAccessException, ClassNotFoundException,
            IOException {
        FileInputStream fis = new FileInputStream("./target/test-jcl.jar");
        JarClassLoader jc = new JarClassLoader(new FileInputStream[]{fis});
        Object testObj = jc.loadClass("cn.vicky.engine.classloader.core.test.Test").newInstance();
        assertNotNull(testObj);
        fis.close();
    }

    @Test
    public void testAddingClassSources() throws InstantiationException, IllegalAccessException, ClassNotFoundException {
        JarClassLoader jc = new JarClassLoader();
        jc.add("./target/test-jcl.jar");
        Object testObj = jc.loadClass("cn.vicky.engine.classloader.core.test.Test").newInstance();
        assertNotNull(testObj);
    }

    @Test
    public void testChangeClassLoadingOrder() throws InstantiationException, IllegalAccessException,
            ClassNotFoundException {
        JarClassLoader jc = new JarClassLoader();
        jc.getSystemLoader().setOrder(1);
        jc.getParentLoader().setOrder(3);
        jc.getLocalLoader().setOrder(2);

        jc.add("./target/test-classes");

        // Should be loaded from system
        Object testObj = jc.loadClass("cn.vicky.engine.classloader.core.test.Test").newInstance();
        assertNotNull(testObj);
    }

    @Test
    public void testInterfaceCast() {
        JarClassLoader jc = new JarClassLoader();
        jc.add("./target/test-jcl.jar");

        JclObjectFactory factory = JclObjectFactory.getInstance();
        Object testObj = factory.create(jc, "cn.vicky.engine.classloader.core.test.Test");

        TestInterface ti = JclUtils.cast(testObj, TestInterface.class);

        assertNotNull(ti);

        // ti = JclUtils.cast( testObj );
        //
        // assertNotNull( ti );
        //
        // ti = (TestInterface) JclUtils.toCastable( testObj );
        //
        // assertNotNull( ti );
        ti = (TestInterface) JclUtils.toCastable(testObj, TestInterface.class);

        assertNotNull(ti);

        ti = (TestInterface) JclUtils.clone(testObj);

        assertNotNull(ti);

        // Deep clone.
        ti = (TestInterface) JclUtils.deepClone(testObj);

        assertNotNull(ti);
    }

    @Test
    public void testAutoProxy() {
        JarClassLoader jc = new JarClassLoader();
        jc.add("./target/test-jcl.jar");

        // Set default to cglib
        ProxyProviderFactory.setDefaultProxyProvider(new CglibProxyProvider());

        // Create auto proxies
        JclObjectFactory factory = JclObjectFactory.getInstance(true);
        TestInterface test = (TestInterface) factory.create(jc, "cn.vicky.engine.classloader.core.test.Test");

        assertNotNull(test);
    }

    @Test
    public void testUnloading() throws IOException, InstantiationException, IllegalAccessException,
            ClassNotFoundException, IllegalArgumentException, SecurityException, InvocationTargetException,
            NoSuchMethodException {
        JarClassLoader jc = new JarClassLoader(new String[]{"./target/test-jcl.jar"});

        Object testObj = null;
        jc.loadClass("cn.vicky.engine.classloader.core.test.Test");
        jc.unloadClass("cn.vicky.engine.classloader.core.test.Test");

        try {
            testObj = jc.loadClass("cn.vicky.engine.classloader.core.test.Test").newInstance();

            // Must have been loaded by a CL other than JCL-Local
            Assert.assertFalse(testObj.getClass().getClassLoader().equals("cn.vicky.engine.classloader.core.JarClassLoader"));
            return;
        } catch (ClassNotFoundException cnfe) {
            // expected if not found
        }

        assertNull(testObj);
    }

    @Test
    public void testEnabledFlag() {
        JarClassLoader jc = new JarClassLoader(new String[]{"./target/test-jcl.jar"});
        jc.getLocalLoader().setEnabled(false);
        jc.getCurrentLoader().setEnabled(false);
        jc.getParentLoader().setEnabled(false);
        jc.getSystemLoader().setEnabled(false);
        jc.getThreadLoader().setEnabled(false);

        String cls = "cn.vicky.engine.classloader.core.test.Test";
        try {
            jc.loadClass(cls);
        } catch (ClassNotFoundException e) {
            // expected
            return;
        }

        throw new AssertionError("Expected: ClassNotFoundException " + cls);
    }

    @Test
    public void testOsgiBootLoading() throws ClassNotFoundException {
        JarClassLoader jc = new JarClassLoader(new String[]{"./target/test-jcl.jar"});

        AbstractClassLoader.OsgiBootLoader obl = (AbstractClassLoader.OsgiBootLoader) jc.getOsgiBootLoader();
        obl.setEnabled(true);
        obl.setStrictLoading(true);

        // Load with parent among all java core classes
        obl.setBootDelagation(new String[]{"cn.vicky.engine.classloader.core.test.*"});

        assertEquals("sun.misc.Launcher$AppClassLoader", jc.loadClass("cn.vicky.engine.classloader.core.test.Test")
                .getClassLoader().getClass().getName());
    }

    @Test
    public void testXmlContextLoader() throws ClassNotFoundException {
        XmlContextLoader cl = new XmlContextLoader("classpath:jcl.xml");
        cl.loadContext();

        JclContext.get("jcl1").loadClass("cn.vicky.engine.classloader.core.test.Test");

        try {
            JclContext.get("jcl2").loadClass("cn.vicky.engine.classloader.core.test.Test");
            throw new AssertionFailedError("expected ClassNotFoundException");
        } catch (ClassNotFoundException e) {
            // expected
        }

        assertEquals("sun.misc.Launcher$AppClassLoader",
                JclContext.get("jcl3").loadClass("cn.vicky.engine.classloader.core.test.Test").getClassLoader().getClass()
                .getName());
    }

    @Test
    public void testDefaultContextLoader() throws InstantiationException, IllegalAccessException,
            ClassNotFoundException {
        JarClassLoader jc = new JarClassLoader(new String[]{"./target/test-jcl.jar"});

        try {
            JclContextLoader contextLoader = new DefaultContextLoader(jc);

            throw new AssertionFailedError("Expected JclContextException");
        } catch (JclContextException e) {
            // Expected because the context is already loaded by the previous
            // test "testXmlContextLoader()"
        }

        // Destroy existing context loaded by testXmlContextLoader()
        JclContext.destroy();

        JclContextLoader contextLoader = new DefaultContextLoader(jc);
        contextLoader.loadContext();

        // Test context
        Object testObj = JclContext.get().loadClass("cn.vicky.engine.classloader.core.test.Test").newInstance();
        assertNotNull(testObj);
        assertEquals("cn.vicky.engine.classloader.core.JarClassLoader", testObj.getClass().getClassLoader().getClass()
                .getName());
    }
}
