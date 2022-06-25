package models;

import net.time4j.calendar.PersianCalendar;

public class PersianDate {
    private int year;
    private int month;
    private int day;

    public PersianDate() {}

    public PersianDate(int year, int month, int day) {
        this.year = year;
        this.month = month;
        this.day = day;
    }

    public static PersianDate now(){
        PersianCalendar persianCalendar = PersianCalendar.nowInSystemTime();
        return new PersianDate(persianCalendar.getYear(), persianCalendar.getMonth().getValue(), persianCalendar.getDayOfMonth());
    }

    public static String getMonthNameByItNumber(int monthNumber){
        switch (monthNumber){
            case 1 : return "فروردین";
            case 2 : return "اردیبهشت";
            case 3 : return "خرداد";
            case 4 : return "تیر";
            case 5 : return "مرداد";
            case 6 : return "شهریور";
            case 7 : return "مهر";
            case 8 : return "آبان";
            case 9 : return "آذر";
            case 10: return "دی";
            case 11: return "بهمن";
            case 12: return "اسفند";
        }
        return "";
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public int getMonth() {
        return month;
    }

    public void setMonth(int month) {
        this.month = month;
    }

    public int getDay() {
        return day;
    }

    public void setDay(int day) {
        this.day = day;
    }
}
