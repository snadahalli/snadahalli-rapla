package org.rapla.components.util;

import java.util.Date;

import org.rapla.components.util.DateTools.TimeWithoutTimezone;
import org.rapla.components.util.iterator.IntIterator;


/**
Provides methods for parsing and formating dates
and times in the following format: <br>
<code>2002-25-05</code> for dates and <code>13:00:00</code> for times.
This is according to the xschema specification for dates and time.
*/
public class SerializableDateTimeFormat
{
    private Date parseDate( String date, String time, boolean fillDate ) throws ParseDateException {
    	if( date == null || date.length()==0  )
    	    throwParseDateException("empty" );
       
    	long millis = parseDate_(date, fillDate);
        if ( time != null ) {
        	long timeMillis = parseTime_(time);
        	millis+= timeMillis;
        }
        //    logger.log( "parsed to " + calendar.getTime() );
        return new Date( millis);
    }

	private long  parseTime_(String time) throws ParseDateException {
		IntIterator it = new IntIterator( time, ':' );
		if ( !it.hasNext() )
		    throwParseTimeException( time );
		int hour = it.next();
		if ( !it.hasNext() )
		    throwParseTimeException( time );
		int minute = it.next();
		if ( !it.hasNext() )
		    throwParseTimeException( time );
		int second = it.next();
		long result = DateTools.toTime( hour, minute,second);
		return result;
	}

	private long parseDate_(String date, boolean fillDate)
			throws ParseDateException {
		int indexOfSpace = date.indexOf( " " );
		if ( indexOfSpace > 0)
		{
		    date = date.substring(0,  indexOfSpace);
		}
		IntIterator it = new IntIterator(date,'-');
		if ( !it.hasNext() )
		    throwParseDateException( date );
		int year = it.next();
		if ( !it.hasNext() )
		    throwParseDateException( date);
		int month = it.next();
		if ( !it.hasNext() )
		    throwParseDateException( date);
		int day = it.next();
		if (fillDate )
		{
			day+=1;
		}
		return DateTools.toDate( year, month, day);
	}

    private void throwParseDateException( String date) throws ParseDateException {
        throw new ParseDateException( "No valid date format: " + date);
    }

    private void throwParseTimeException( String time) throws ParseDateException {
        throw new ParseDateException( "No valid time format: "  + time);
    }

    /** The date-string must be in the following format <strong>2001-10-21</strong>.
    The format of the time-string is <strong>18:00:00</strong>.
    @return The parsed date
    @throws ParseDateException when the date cannot be parsed.
    */
    public Date parseDateTime( String date, String time) throws ParseDateException {
        return parseDate( date, time, false);
    }
    
    /** 
    The format of the time-string is <strong>18:00:00</strong>.
    @return The parsed time
    @throws ParseDateException when the date cannot be parsed.
    */
    public Date parseTime(  String time) throws ParseDateException {
    	if( time == null || time.length()==0  )
    	    throwParseDateException("empty");
    	long millis = parseTime_(  time);
    	Date result = new Date( millis);
    	return result;
    }

    /** The date-string must be in the following format <strong>2001-10-21</strong>.
     * @param fillDate if this flag is set the time will be 24:00 instead of 0:00 <strong>
    When this flag is set the time parameter should be null</strong>
    @return The parsed date
    @throws ParseDateException when the date cannot be parsed.
    */
    public Date parseDate( String date, boolean fillDate ) throws ParseDateException {
        return parseDate( date, null, fillDate);
    }
    
    public Date parseTimestamp(String timestamp) throws ParseDateException
    {
        boolean fillDate = false;
        timestamp = timestamp.trim();
        long millisDate = parseDate_(timestamp, fillDate);
        int indexOfSpace = timestamp.indexOf(" ");
		if ( timestamp.indexOf(":") >=  indexOfSpace && indexOfSpace > 0)
        {
            String timeString = timestamp.substring( indexOfSpace + 1);
            if  ( timeString.length() > 0)
            {
                long time = parseTime_(  timeString);
                millisDate+= time;
            }
        }
		Date result = new Date( millisDate);
        return result;
    }


   /** returns the time object in the following format:  <strong>13:00:00</strong>. <br> */
    public String formatTime( Date date ) {
        StringBuilder buf = new StringBuilder();
        if ( date == null)
        {
            date = new Date();
        }
        TimeWithoutTimezone time = DateTools.toTime( date.getTime());
        append( buf, time.hour, 2 );
        buf.append( ':' );
        append( buf, time.minute, 2 );
        buf.append( ':' );
        append( buf, time.second, 2 );
        return buf.toString();
    }

    /** returns the date object in the following format:  <strong>2001-10-21</strong>. <br>
    @param adaptDay if the flag is set 2001-10-21 will be stored as 2001-10-20.
    This is usefull for end-dates: 2001-10-21 00:00 is then interpreted as
    2001-10-20 24:00.
    */
    public String formatDate( Date date, boolean adaptDay ) {
    	StringBuilder buf = new StringBuilder();
    	DateTools.DateWithoutTimezone splitDate;
    	splitDate = DateTools.toDate( date.getTime()  - (adaptDay ?  DateTools.MILLISECONDS_PER_DAY : 0));
        append( buf, splitDate.year, 4 );
        buf.append( '-' );
        append( buf, splitDate.month, 2 );
        buf.append( '-' );
        append( buf, splitDate.day, 2 );
        return buf.toString();

    }

    public String formatTimestamp( Date date ) {
        String timestamp = formatDate( date, false) + " " +formatTime( date);
        return timestamp;
    }

    /** same as formatDate(date, false).
    @see #formatDate(Date,boolean)
    */
    public String formatDate(  Date date ) {
        return formatDate( date, false );
    }

    private void append( StringBuilder buf, int number, int minLength ) {
        int limit = 1;
        for ( int i=0;i<minLength-1;i++ ) {
            limit *= 10;
            if ( number<limit )
                buf.append( '0' );
        }
        buf.append( number );
    }

}
