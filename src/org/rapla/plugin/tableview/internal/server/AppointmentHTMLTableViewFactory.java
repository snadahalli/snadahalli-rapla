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
package org.rapla.plugin.tableview.internal.server;

import org.rapla.facade.CalendarModel;
import org.rapla.facade.RaplaComponent;
import org.rapla.framework.RaplaContext;
import org.rapla.plugin.abstractcalendar.server.HTMLViewFactory;
import org.rapla.servletpages.RaplaPageGenerator;

public class AppointmentHTMLTableViewFactory extends RaplaComponent implements HTMLViewFactory
{
    public AppointmentHTMLTableViewFactory( RaplaContext context )
    {
        super( context );
    }

    public final static String TABLE_VIEW =  "table_appointments";

    public RaplaPageGenerator createHTMLView(RaplaContext context, CalendarModel model)
    {
        return new AppointmentTableViewPage( context,  model);
    }

    public String getViewId()
    {
        return TABLE_VIEW;
    }

    public String getName()
    {
        return getString("appointments");
    }


}

