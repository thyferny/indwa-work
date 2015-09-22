package com.alpine.utility.log;

import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.spi.LoggingEvent;


public class LoggingAppender extends AppenderSkeleton
{
    @Override
    public void close() {
        System.out.println("CLOSING>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
       // LogPoster.getInstance().close();
    }

    @Override
    public boolean requiresLayout(){ return true; }


    @Override
    public synchronized void append( LoggingEvent event )
    {
        LogEvent e = new LogEvent();
        e.setMessage(event.getLevel().toString());
        e.setTimestamp(event.getTimeStamp());

        if (event.getThrowableInformation() != null)
        {
            String[] stackTrace = event.getThrowableInformation().getThrowableStrRep();
            StringBuilder st = new StringBuilder();
            for (int i = 0; i < stackTrace.length; i++) {
                st.append( stackTrace[i] );
                st.append( "\n");
            }
            e.addExtra("stacktrace", st.toString());
            e.setMessagedetails(stackTrace[0]);
        }  else
        {
            //need to be careful not to send any private data - will only send location information (class name, method name,  file name, line number)
            e.setMessagedetails("Error without stacktrace");
            if (event.locationInformationExists() && event.getLocationInformation().fullInfo != null)
            {
                e.setMessagedetails(event.getLocationInformation().fullInfo);
            }
        }

        LogPoster.getInstance().addEvent(e);

   }


}
