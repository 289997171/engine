package cn.vicky.engine.classloader.core.exception;

/**
 * 上下文异常
 *
 * @author Vicky.H
 * @email ecliser@163.com
 *
 */
public class JclContextException extends JclException {

    /**
     * serialVersionUID:long
     */
    private static final long serialVersionUID = -799657685317877954L;

    public JclContextException() {
        super();
    }

    public JclContextException(String message, Throwable cause) {
        super(message, cause);
    }

    public JclContextException(String message) {
        super(message);
    }

    public JclContextException(Throwable cause) {
        super(cause);
    }
}
