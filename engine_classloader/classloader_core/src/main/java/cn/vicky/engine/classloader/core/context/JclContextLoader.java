package cn.vicky.engine.classloader.core.context;

/**
 * 通过不同资源加载
 * 
 * @author Vicky.H
 * @email  ecliser@163.com
 * 
 */
public interface JclContextLoader {
    /**
     * 加载
     */
    public void loadContext();

    /**
     * 取消加载
     */
    public void unloadContext();
}
