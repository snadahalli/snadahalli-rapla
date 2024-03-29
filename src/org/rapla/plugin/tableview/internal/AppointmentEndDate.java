package org.rapla.plugin.tableview.internal;

import java.util.Date;

import javax.swing.table.TableColumn;

import org.rapla.entities.domain.AppointmentBlock;
import org.rapla.facade.RaplaComponent;
import org.rapla.framework.RaplaContext;
import org.rapla.framework.RaplaLocale;
import org.rapla.plugin.tableview.AppointmentTableColumn;
import org.rapla.plugin.tableview.DateCellRenderer;

public final class AppointmentEndDate extends RaplaComponent implements AppointmentTableColumn {
	public AppointmentEndDate(RaplaContext context) {
		super(context);
	}

	public void init(TableColumn column) {
	    column.setCellRenderer( new DateCellRenderer( getRaplaLocale()));
	    column.setMaxWidth( 175 );
	    column.setPreferredWidth( 175 );
	}

	public Object getValue(AppointmentBlock block) {
		return new Date(block.getEnd());
	}

	public String getColumnName() {
		return getString("end_date");
	}

	public Class<?> getColumnClass() {
		return Date.class;
	}

	public String getHtmlValue(AppointmentBlock block) 
	{
		RaplaLocale raplaLocale = getRaplaLocale();
		final Date date = new Date(block.getEnd());
		String dateString= raplaLocale.formatDateLong(date) +  " " + raplaLocale.formatTime( date);
		return dateString;
	}
}