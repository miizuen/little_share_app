package com.example.little_share.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class DateUtilsClass {

    // Date Format Patterns
    public static final String PATTERN_DD_MM_YYYY = "dd/MM/yyyy";
    public static final String PATTERN_DD_MM_YYYY_HH_MM = "dd/MM/yyyy HH:mm";
    public static final String PATTERN_HH_MM = "HH:mm";
    public static final String PATTERN_DD_MMM_YYYY = "dd MMM yyyy";
    public static final String PATTERN_DD_MMMM_YYYY = "dd MMMM yyyy";
    public static final String PATTERN_YYYY_MM_DD = "yyyy-MM-dd";
    public static final String PATTERN_FULL = "EEEE, dd MMMM yyyy";
    public static final String PATTERN_TIME_12H = "hh:mm a";

    /**
     * Format Date to String với pattern mặc định (dd/MM/yyyy)
     */
    public static String formatDate(Date date) {
        if (date == null) return "";
        SimpleDateFormat sdf = new SimpleDateFormat(PATTERN_DD_MM_YYYY, new Locale("vi", "VN"));
        return sdf.format(date);
    }

    /**
     * Format Date to String với custom pattern
     */
    public static String formatDate(Date date, String pattern) {
        if (date == null) return "";
        SimpleDateFormat sdf = new SimpleDateFormat(pattern, new Locale("vi", "VN"));
        return sdf.format(date);
    }

    /**
     * Parse String to Date với pattern mặc định (dd/MM/yyyy)
     */
    public static Date parseDate(String dateString) {
        if (dateString == null || dateString.isEmpty()) return null;
        SimpleDateFormat sdf = new SimpleDateFormat(PATTERN_DD_MM_YYYY, new Locale("vi", "VN"));
        try {
            return sdf.parse(dateString);
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Parse String to Date với custom pattern
     */
    public static Date parseDate(String dateString, String pattern) {
        if (dateString == null || dateString.isEmpty()) return null;
        SimpleDateFormat sdf = new SimpleDateFormat(pattern, new Locale("vi", "VN"));
        try {
            return sdf.parse(dateString);
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static CharSequence getRelativeTimeSpanString(Date date) {
        if (date == null) return "Không rõ";

        return android.text.format.DateUtils.getRelativeTimeSpanString(
                date.getTime(),
                System.currentTimeMillis(),
                android.text.format.DateUtils.MINUTE_IN_MILLIS,
                android.text.format.DateUtils.FORMAT_ABBREV_RELATIVE
        );
    }

    /**
     * Get relative time string custom (tiếng Việt)
     */
    public static String getRelativeTime(Date date) {
        if (date == null) return "Không rõ";

        long timeInMillis = date.getTime();
        long now = System.currentTimeMillis();
        long diff = now - timeInMillis;

        // Nếu là thời gian tương lai
        if (diff < 0) {
            diff = Math.abs(diff);
            if (diff < TimeUnit.MINUTES.toMillis(1)) {
                return "Sắp diễn ra";
            } else if (diff < TimeUnit.HOURS.toMillis(1)) {
                long minutes = TimeUnit.MILLISECONDS.toMinutes(diff);
                return "Sau " + minutes + " phút nữa";
            } else if (diff < TimeUnit.DAYS.toMillis(1)) {
                long hours = TimeUnit.MILLISECONDS.toHours(diff);
                return "Sau " + hours + " giờ nữa";
            } else if (diff < TimeUnit.DAYS.toMillis(7)) {
                long days = TimeUnit.MILLISECONDS.toDays(diff);
                return "Sau " + days + " ngày nữa";
            } else {
                return formatDate(date);
            }
        }

        // Thời gian quá khứ
        if (diff < TimeUnit.MINUTES.toMillis(1)) {
            return "Vừa xong";
        } else if (diff < TimeUnit.HOURS.toMillis(1)) {
            long minutes = TimeUnit.MILLISECONDS.toMinutes(diff);
            return minutes + " phút trước";
        } else if (diff < TimeUnit.DAYS.toMillis(1)) {
            long hours = TimeUnit.MILLISECONDS.toHours(diff);
            return hours + " giờ trước";
        } else if (diff < TimeUnit.DAYS.toMillis(7)) {
            long days = TimeUnit.MILLISECONDS.toDays(diff);
            return days + " ngày trước";
        } else if (diff < TimeUnit.DAYS.toMillis(30)) {
            long weeks = TimeUnit.MILLISECONDS.toDays(diff) / 7;
            return weeks + " tuần trước";
        } else if (diff < TimeUnit.DAYS.toMillis(365)) {
            long months = TimeUnit.MILLISECONDS.toDays(diff) / 30;
            return months + " tháng trước";
        } else {
            return formatDate(date);
        }
    }

    /**
     * Check if date is today
     */
    public static boolean isToday(Date date) {
        if (date == null) return false;
        Calendar today = Calendar.getInstance();
        Calendar dateCalendar = Calendar.getInstance();
        dateCalendar.setTime(date);

        return today.get(Calendar.YEAR) == dateCalendar.get(Calendar.YEAR) &&
                today.get(Calendar.DAY_OF_YEAR) == dateCalendar.get(Calendar.DAY_OF_YEAR);
    }

    /**
     * Check if date is yesterday
     */
    public static boolean isYesterday(Date date) {
        if (date == null) return false;
        Calendar yesterday = Calendar.getInstance();
        yesterday.add(Calendar.DAY_OF_YEAR, -1);
        Calendar dateCalendar = Calendar.getInstance();
        dateCalendar.setTime(date);

        return yesterday.get(Calendar.YEAR) == dateCalendar.get(Calendar.YEAR) &&
                yesterday.get(Calendar.DAY_OF_YEAR) == dateCalendar.get(Calendar.DAY_OF_YEAR);
    }

    /**
     * Check if date is tomorrow
     */
    public static boolean isTomorrow(Date date) {
        if (date == null) return false;
        Calendar tomorrow = Calendar.getInstance();
        tomorrow.add(Calendar.DAY_OF_YEAR, 1);
        Calendar dateCalendar = Calendar.getInstance();
        dateCalendar.setTime(date);

        return tomorrow.get(Calendar.YEAR) == dateCalendar.get(Calendar.YEAR) &&
                tomorrow.get(Calendar.DAY_OF_YEAR) == dateCalendar.get(Calendar.DAY_OF_YEAR);
    }

    /**
     * Get friendly date string (Hôm nay, Hôm qua, Ngày mai, hoặc dd/MM/yyyy)
     */
    public static String getFriendlyDateString(Date date) {
        if (date == null) return "";

        if (isToday(date)) {
            return "Hôm nay";
        } else if (isYesterday(date)) {
            return "Hôm qua";
        } else if (isTomorrow(date)) {
            return "Ngày mai";
        } else {
            return formatDate(date);
        }
    }

    /**
     * Get friendly date with time (Hôm nay 14:30, Hôm qua 09:15, 12/05/2024 10:00)
     */
    public static String getFriendlyDateTime(Date date) {
        if (date == null) return "";

        String friendlyDate = getFriendlyDateString(date);
        String time = formatDate(date, PATTERN_HH_MM);

        if (friendlyDate.equals("Hôm nay") || friendlyDate.equals("Hôm qua") || friendlyDate.equals("Ngày mai")) {
            return friendlyDate + " " + time;
        } else {
            return friendlyDate + " " + time;
        }
    }

    /**
     * Calculate difference between two dates in days
     */
    public static long getDaysDifference(Date startDate, Date endDate) {
        if (startDate == null || endDate == null) return 0;
        long diff = endDate.getTime() - startDate.getTime();
        return TimeUnit.MILLISECONDS.toDays(diff);
    }

    /**
     * Calculate difference between two dates in hours
     */
    public static long getHoursDifference(Date startDate, Date endDate) {
        if (startDate == null || endDate == null) return 0;
        long diff = endDate.getTime() - startDate.getTime();
        return TimeUnit.MILLISECONDS.toHours(diff);
    }

    /**
     * Add days to date
     */
    public static Date addDays(Date date, int days) {
        if (date == null) return null;
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.DAY_OF_YEAR, days);
        return calendar.getTime();
    }

    /**
     * Add months to date
     */
    public static Date addMonths(Date date, int months) {
        if (date == null) return null;
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.MONTH, months);
        return calendar.getTime();
    }

    /**
     * Add years to date
     */
    public static Date addYears(Date date, int years) {
        if (date == null) return null;
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.YEAR, years);
        return calendar.getTime();
    }

    /**
     * Get start of day (00:00:00)
     */
    public static Date getStartOfDay(Date date) {
        if (date == null) return null;
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTime();
    }

    /**
     * Get end of day (23:59:59)
     */
    public static Date getEndOfDay(Date date) {
        if (date == null) return null;
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 59);
        calendar.set(Calendar.MILLISECOND, 999);
        return calendar.getTime();
    }

    /**
     * Check if date is in range
     */
    public static boolean isDateInRange(Date checkDate, Date startDate, Date endDate) {
        if (checkDate == null || startDate == null || endDate == null) return false;
        return !checkDate.before(startDate) && !checkDate.after(endDate);
    }

    /**
     * Check if date has passed
     */
    public static boolean isPast(Date date) {
        if (date == null) return false;
        return date.before(new Date());
    }

    /**
     * Check if date is in future
     */
    public static boolean isFuture(Date date) {
        if (date == null) return false;
        return date.after(new Date());
    }

    /**
     * Get current date without time
     */
    public static Date getCurrentDateWithoutTime() {
        return getStartOfDay(new Date());
    }

    /**
     * Get age from birth date
     */
    public static int getAge(Date birthDate) {
        if (birthDate == null) return 0;
        Calendar birth = Calendar.getInstance();
        birth.setTime(birthDate);
        Calendar now = Calendar.getInstance();

        int age = now.get(Calendar.YEAR) - birth.get(Calendar.YEAR);

        if (now.get(Calendar.DAY_OF_YEAR) < birth.get(Calendar.DAY_OF_YEAR)) {
            age--;
        }

        return age;
    }

    /**
     * Format duration in milliseconds to readable string
     */
    public static String formatDuration(long durationInMillis) {
        long seconds = TimeUnit.MILLISECONDS.toSeconds(durationInMillis);
        long minutes = TimeUnit.MILLISECONDS.toMinutes(durationInMillis);
        long hours = TimeUnit.MILLISECONDS.toHours(durationInMillis);
        long days = TimeUnit.MILLISECONDS.toDays(durationInMillis);

        if (days > 0) {
            return days + " ngày";
        } else if (hours > 0) {
            return hours + " giờ";
        } else if (minutes > 0) {
            return minutes + " phút";
        } else {
            return seconds + " giây";
        }
    }

    /**
     * Get day of week in Vietnamese
     */
    public static String getDayOfWeekInVietnamese(Date date) {
        if (date == null) return "";
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);

        switch (dayOfWeek) {
            case Calendar.MONDAY:
                return "Thứ Hai";
            case Calendar.TUESDAY:
                return "Thứ Ba";
            case Calendar.WEDNESDAY:
                return "Thứ Tư";
            case Calendar.THURSDAY:
                return "Thứ Năm";
            case Calendar.FRIDAY:
                return "Thứ Sáu";
            case Calendar.SATURDAY:
                return "Thứ Bảy";
            case Calendar.SUNDAY:
                return "Chủ Nhật";
            default:
                return "";
        }
    }

    /**
     * Get month name in Vietnamese
     */
    public static String getMonthInVietnamese(Date date) {
        if (date == null) return "";
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        int month = calendar.get(Calendar.MONTH);

        String[] months = {
                "Tháng 1", "Tháng 2", "Tháng 3", "Tháng 4",
                "Tháng 5", "Tháng 6", "Tháng 7", "Tháng 8",
                "Tháng 9", "Tháng 10", "Tháng 11", "Tháng 12"
        };

        return months[month];
    }

    /**
     * Format campaign date range
     * Example: "12/05/2024 - 20/05/2024" or "Hôm nay - 20/05/2024"
     */
    public static String formatCampaignDateRange(Date startDate, Date endDate) {
        if (startDate == null || endDate == null) return "";

        String start = getFriendlyDateString(startDate);
        String end = formatDate(endDate);

        return start + " - " + end;
    }

    /**
     * Get remaining days text for campaign
     * Example: "Còn 5 ngày", "Còn 2 giờ", "Đã kết thúc"
     */
    public static String getRemainingTimeText(Date endDate) {
        if (endDate == null) return "";

        long now = System.currentTimeMillis();
        long endTime = endDate.getTime();
        long diff = endTime - now;

        if (diff < 0) {
            return "Đã kết thúc";
        } else if (diff < TimeUnit.HOURS.toMillis(1)) {
            long minutes = TimeUnit.MILLISECONDS.toMinutes(diff);
            return "Còn " + minutes + " phút";
        } else if (diff < TimeUnit.DAYS.toMillis(1)) {
            long hours = TimeUnit.MILLISECONDS.toHours(diff);
            return "Còn " + hours + " giờ";
        } else {
            long days = TimeUnit.MILLISECONDS.toDays(diff);
            return "Còn " + days + " ngày";
        }
    }

    /**
     * Check if campaign is ongoing
     */
    public static boolean isCampaignOngoing(Date startDate, Date endDate) {
        if (startDate == null || endDate == null) return false;
        Date now = new Date();
        return !now.before(startDate) && !now.after(endDate);
    }

    /**
     * Check if campaign is upcoming
     */
    public static boolean isCampaignUpcoming(Date startDate) {
        if (startDate == null) return false;
        return startDate.after(new Date());
    }

    /**
     * Check if campaign has ended
     */
    public static boolean isCampaignEnded(Date endDate) {
        if (endDate == null) return false;
        return endDate.before(new Date());
    }

    /**
     * Get campaign status text
     */
    public static String getCampaignStatusText(Date startDate, Date endDate) {
        if (isCampaignEnded(endDate)) {
            return "Đã kết thúc";
        } else if (isCampaignOngoing(startDate, endDate)) {
            return "Đang diễn ra";
        } else if (isCampaignUpcoming(startDate)) {
            return "Sắp diễn ra";
        }
        return "";
    }
}