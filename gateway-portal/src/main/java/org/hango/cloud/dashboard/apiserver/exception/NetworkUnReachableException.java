package org.hango.cloud.dashboard.apiserver.exception;

/**
 * remote host is unreachable.
 *
 * @author hzfengchj
 * @version $Id: NetworkUnReachableException.java, v 0.1 2012-6-13 下午02:55:01
 */
public class NetworkUnReachableException extends Exception {

    /**
     *
     */
    public NetworkUnReachableException() {
    }

    /**
     * @param message
     */
    public NetworkUnReachableException(String message) {
        super(message);
    }

    /**
     * @param cause
     */
    public NetworkUnReachableException(Throwable cause) {
        super(cause);
    }

    /**
     * @param message
     * @param cause
     */
    public NetworkUnReachableException(String message, Throwable cause) {
        super(message, cause);
    }

}
