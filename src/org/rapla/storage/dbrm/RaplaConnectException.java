package org.rapla.storage.dbrm;

import org.rapla.framework.RaplaException;

public class RaplaConnectException extends RaplaException {

    private static final long serialVersionUID = 1L;

    public RaplaConnectException(String text) {
        super(text, null);
    }
    
    public RaplaConnectException(String text, Throwable cause) {
        super(text, cause);
    }

}
