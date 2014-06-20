package cn.vicky.engine.classloader.core.utils;

/**
 * 路径解析接口
 * 
 * @author Vicky.H
 * @email  ecliser@163.com
 * 
 */
public interface PathResolver {

    /**
     * 解析class/jar的路径,可以返回多个文件类型 paths/stream/urls
     * 
     * 如果路径无法解析,返回null
     * 
     * @param path
     * @return Object[]
     */
    public Object[] resolvePath(String path);
}
