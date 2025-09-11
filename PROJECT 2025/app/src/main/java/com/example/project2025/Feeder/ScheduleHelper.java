package com.example.project2025.Feeder;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import java.util.Calendar;
import java.util.List;

public class ScheduleHelper {

    public static void cancelWeekly(Context context, int requestBase) {
        if (context == null) return;
        for (int dow : new int[]{java.util.Calendar.SUNDAY, java.util.Calendar.MONDAY, java.util.Calendar.TUESDAY,
                java.util.Calendar.WEDNESDAY, java.util.Calendar.THURSDAY, java.util.Calendar.FRIDAY, java.util.Calendar.SATURDAY}) {
            Intent intent = new Intent(context, FeedingAlarmReceiver.class);
            int reqCode = requestBase + dow;
            PendingIntent pi = PendingIntent.getBroadcast(
                    context,
                    reqCode,
                    intent,
                    PendingIntent.FLAG_NO_CREATE | PendingIntent.FLAG_IMMUTABLE
            );
            if (pi != null) {
                AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
                am.cancel(pi);
            }
        }
    }

    public static void scheduleWeekly(Context context, String timeHHmm, List<String> days, int requestBase, int level, String title, String scheduleId) {
        if (timeHHmm == null || timeHHmm.isEmpty() || days == null || days.isEmpty()) {
            return;
        }

        int hour;
        int minute;
        try {
            // Accept both "HH:mm" and "h:mm a"
            if (timeHHmm.contains("AM") || timeHHmm.contains("PM") || timeHHmm.contains("am") || timeHHmm.contains("pm")) {
                java.text.SimpleDateFormat in = new java.text.SimpleDateFormat("h:mm a", java.util.Locale.getDefault());
                java.util.Date d = in.parse(timeHHmm);
                java.util.Calendar c = java.util.Calendar.getInstance();
                c.setTime(d);
                hour = c.get(java.util.Calendar.HOUR_OF_DAY);
                minute = c.get(java.util.Calendar.MINUTE);
            } else {
                String[] parts = timeHHmm.split(":");
                hour = Integer.parseInt(parts[0]);
                minute = Integer.parseInt(parts[1]);
            }
        } catch (Exception e) {
            String[] parts = timeHHmm.split(":");
            hour = Integer.parseInt(parts[0]);
            minute = Integer.parseInt(parts[1]);
        }

        for (String d : days) {
            int dow = mapDay(d);
            if (dow == -1) continue;

            Calendar cal = Calendar.getInstance();
            cal.set(Calendar.SECOND, 0);
            cal.set(Calendar.MILLISECOND, 0);
            cal.set(Calendar.HOUR_OF_DAY, hour);
            cal.set(Calendar.MINUTE, minute);

            int todayDow = cal.get(Calendar.DAY_OF_WEEK);
            int diff = dow - todayDow;
            long now = System.currentTimeMillis();
            if (diff < 0 || (diff == 0 && cal.getTimeInMillis() <= now)) {
                diff += 7;
            }
            cal.add(Calendar.DAY_OF_YEAR, diff);

            Intent intent = new Intent(context, FeedingAlarmReceiver.class);
            intent.putExtra("level", level > 0 ? level : 1);
            if (title != null) intent.putExtra("title", title);
            if (scheduleId != null) intent.putExtra("scheduleId", scheduleId);
            int reqCode = requestBase + dow;
            PendingIntent pi = PendingIntent.getBroadcast(
                    context,
                    reqCode,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
            );

            AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            AlarmManager.AlarmClockInfo info = new AlarmManager.AlarmClockInfo(cal.getTimeInMillis(), null);
            
            // Check if we can schedule exact alarms before attempting to do so
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
                if (am.canScheduleExactAlarms()) {
                    am.setAlarmClock(info, pi);
                } else {
                    // Fallback to inexact alarm if exact alarms are not allowed
                    am.set(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), pi);
                }
            } else {
                // For older Android versions, setAlarmClock is always available
                am.setAlarmClock(info, pi);
            }
        }
    }

    private static int mapDay(String d) {
        if (d == null) return -1;
        switch (d) {
            case "Sun": return Calendar.SUNDAY;
            case "Mon": return Calendar.MONDAY;
            case "Tue": return Calendar.TUESDAY;
            case "Wed": return Calendar.WEDNESDAY;
            case "Thu": return Calendar.THURSDAY;
            case "Fri": return Calendar.FRIDAY;
            case "Sat": return Calendar.SATURDAY;
        }
        return -1;
    }
}


