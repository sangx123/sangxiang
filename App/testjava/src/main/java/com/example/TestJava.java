package com.example;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

public class TestJava {
    public static void main(String[] args){
        //假设今天是怀孕第一天
        Date date=new Date();
        int timespan=getTrueDaysBetween(date.getTime(),getBday(date));
        System.out.println(timespan);
        long now=date.getTime();
        long lastday=getBday(date);
        System.out.println(lastday-now);
        System.out.println(24192000000L - 1000 * 60 * 60 * 24);
    }
    public static int getTrueDaysBetween(long a, long b) {
        /*if (b < a) {
            return -getDaysBetween(b, a);
        }*/
        a = resetToDay(a);
        b = resetToDay(b);

        Calendar cal = new GregorianCalendar();
        cal.setTimeInMillis(a);
        int days = 0;
        while (cal.getTimeInMillis() < b) {
            // add another day
            cal.add(Calendar.DAY_OF_MONTH, 1);
            days++;
        }
        return days;
    }

    //取当天日期的20170421 00 ：00：00
    public static long resetToDay(long time) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(time);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTimeInMillis();
    }

    public static long getBday(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.DAY_OF_MONTH, 279);
        System.out.println(calendar.get(Calendar.YEAR) * 10000 + (calendar.get(Calendar.MONTH) + 1) * 100 + calendar.get(Calendar.DAY_OF_MONTH));
        return calendar.getTimeInMillis();
    }
}
