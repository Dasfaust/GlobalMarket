package me.dasfaust.gm.tools;

import java.util.logging.Level;
import java.util.logging.Logger;

public class GMLogger
{
	public static Logger logger;
    private static boolean DEBUG = false;
    
    public static void setDebug(boolean debug)
    {
        DEBUG = debug;
    }
    
    public static void setLogger(Logger log)
    {
        logger = log;
    }
    
    public static void info(Object ob)
    {
        logger.info(ob.toString());
    }
    
    public static void warning(Object ob)
    {
        logger.warning(ob.toString());
    }
    
    public static void severe(Object ob)
    {
        logger.severe(ob.toString());
    }
    
    public static void severe(Throwable t, Object ob)
    {
        logger.log(Level.SEVERE, ob.toString(), t);
    }
    
    public static void debug(Object ob)
    {
        if (DEBUG)
        {
            logger.info(ob.toString());
        }
    }
    
    public static void debug(Throwable t, Object ob)
    {
        if (DEBUG)
        {
            logger.log(Level.INFO, ob.toString(), t);
        }
    }
}
