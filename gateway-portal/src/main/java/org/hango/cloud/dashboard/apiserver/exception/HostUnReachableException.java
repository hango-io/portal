package org.hango.cloud.dashboard.apiserver.exception;

/**
 * remote host is unreachable.
 *
 * @author Feng Changjian (hzfengchj)
 * @version $Id: HostUnReachableException.java, v 1.0 2013-8-2 下午04:02:15
 */
public class HostUnReachableException extends Exception {

    private static final long serialVersionUID = -3316039062933001109L;

    public HostUnReachableException() {
    }

    /**
     * @param message
     */
    public HostUnReachableException(String message) {
        super(message);
    }

    /**
     * @param cause
     */
    public HostUnReachableException(Throwable cause) {
        super(cause);
    }

    /**
     * @param message
     * @param cause
     */
    public HostUnReachableException(String message, Throwable cause) {
        super(message, cause);
    }
}
