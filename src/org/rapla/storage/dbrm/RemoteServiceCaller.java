/*--------------------------------------------------------------------------*
 | Copyright (C) 2006 Christopher Kohlhaas                                  |
 |                                                                          |
 | This program is free software; you can redistribute it and/or modify     |
 | it under the terms of the GNU General Public License as published by the |
 | Free Software Foundation. A copy of the license has been included with   |
 | these distribution in the COPYING file, if not go to www.fsf.org         |
 |                                                                          |
 | As a special exception, you are granted the permissions to link this     |
 | program with every library, which license fulfills the Open Source       |
 | Definition as published by the Open Source Initiative (OSI).             |
 *--------------------------------------------------------------------------*/

package org.rapla.storage.dbrm;

import org.rapla.framework.RaplaException;


/** provides a mechanism to invoke a remote service on the server.
 * The server must provide a RemoteService for the specified serviceName.
 * The RemoteOperator provides the Service RemoteServiceCaller   
 * @request the webservices in the constructor instead. see RemoteOperator for an example*/
@Deprecated 
public interface RemoteServiceCaller {
   <T> T getRemoteMethod(Class<T> a) throws RaplaException;
}
    
