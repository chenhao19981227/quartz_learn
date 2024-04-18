package com.ch.quartz_learn.tools;

import java.text.SimpleDateFormat;
import java.util.Date;

public class DFUtil {
    private static final SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    public static String format(Date date){
        return sdf.format(date);
    }
}
