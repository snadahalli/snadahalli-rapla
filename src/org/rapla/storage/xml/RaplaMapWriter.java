/*--------------------------------------------------------------------------*
  | Copyright (C) 2006 Christopher Kohlhaas                                  |
  |                                                                          |
  | This program is free software; you can redistribute it and/or modify     |
  | it under the terms of the GNU General Public License as published by the |
  | Free Software Foundation. A copy of the license has been included with   |
  | these distribution in the COPYING file, if not go to www.fsf.org .       |
  |                                                                          |
  | As a special exception, you are granted the permissions to link this     |
  | program with every library, which license fulfills the Open Source       |
  | Definition as published by the Open Source Initiative (OSI).             |
  *--------------------------------------------------------------------------*/

package org.rapla.storage.xml;

import java.io.IOException;
import java.util.Iterator;

import org.rapla.entities.RaplaObject;
import org.rapla.entities.configuration.RaplaMap;
import org.rapla.entities.storage.RefEntity;
import org.rapla.framework.RaplaContext;
import org.rapla.framework.RaplaException;

    
public class RaplaMapWriter extends RaplaXMLWriter {
    
    public RaplaMapWriter(RaplaContext sm) throws RaplaException {
        super(sm);
    }

    public void writeObject(RaplaObject type) throws IOException, RaplaException {
        writeMap((RaplaMap<?>) type );
    }

    public void writeMap(RaplaMap<?> map ) throws IOException, RaplaException {
        openElement("rapla:"  + RaplaMap.TYPE.getLocalName());
        for (Iterator<String> it = map.keySet().iterator();it.hasNext();) {
            Object key = it.next();
            Object obj =  map.get( key);
            printEntityReference( key, obj);
        }
        closeElement("rapla:" + RaplaMap.TYPE.getLocalName());
    }
    
    private void printEntityReference(Object key,Object obj) throws RaplaException, IOException {
        if (obj == null)
        {
            getLogger().warn( "Map contains empty value under key " + key );
            return;
        }
        int start = getIndentLevel();
        openTag("rapla:mapentry");
        att("key", key.toString());
        if ( obj instanceof String)
        {
            String value = (String) obj;
            att("value", value);
            closeElementTag();
            return;
        }
        closeTag();
        if ( obj instanceof RefEntity ) {
            printReference( (RefEntity<?>) obj);
        } else {
            RaplaObject raplaObj = (RaplaObject) obj;
            getWriterFor( raplaObj.getRaplaType()).writeObject( raplaObj );
        }
        setIndentLevel( start+1 );
        closeElement("rapla:mapentry");
        setIndentLevel( start );
    }
    
 }
 


