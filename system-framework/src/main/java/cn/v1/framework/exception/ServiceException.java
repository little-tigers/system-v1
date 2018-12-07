/**
 * Create by gen
 */
package cn.v1.framework.exception;

/**
 * Service层公用的Exception, 从由Spring管理事务的函数中抛出时会触发事务回滚.
 * @author ThinkGem
 */
public class ServiceException extends RuntimeException {

	private static final long serialVersionUID = 3950535646278499214L;

	public ServiceException() {
		super();
	}

	public ServiceException(String message) {
		super(message);
	}

	public ServiceException(Throwable cause) {
		super(cause);
	}

	public ServiceException(String message, Throwable cause) {
		super(message, cause);
	}
}
