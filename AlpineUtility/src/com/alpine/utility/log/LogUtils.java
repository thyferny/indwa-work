package com.alpine.utility.log;

public class LogUtils {

    public static String entry(String className, String method,String parameter)
    {
        return "Entering method:"+className+"'s "+method+" with parameter "+parameter;
    }



    public static String exit( String className,String method,String returnValue){
        return "Exiting method:"+className+"'s "+method+" with returnValue "+returnValue;

    }
}