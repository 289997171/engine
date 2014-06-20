package cn.vicky.engine.classloader.core.exception;

/**
 * 类加载器自定义异常类型
 * 
 * @author Vicky.H
 * @email  ecliser@163.com
 * 
 */
public class JclException extends RuntimeException {
    /**
     * Default serial id
     */
    private static final long serialVersionUID = 1L;

    /**
     * Default constructor
     */
    public JclException() {
        super();
    }

    /**
     * @param message
     */
    public JclException(String message) {
        super( message );
    }

    /**
     * @param cause
     */
    public JclException(Throwable cause) {
        super( cause );
    }

    /**
     * @param message
     * @param cause
     */
    public JclException(String message, Throwable cause) {
        super( message, cause );
    }
}
