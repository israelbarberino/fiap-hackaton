package br.com.fiap.susagenda.domain.appointment;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Set;

public final class AppointmentScheduleRules {
    public static final LocalTime OPENING_TIME = LocalTime.of(7, 0);
    public static final LocalTime LAST_SLOT_TIME = LocalTime.of(19, 0);
    private static final Set<DayOfWeek> BUSINESS_DAYS = Set.of(
            DayOfWeek.MONDAY,
            DayOfWeek.TUESDAY,
            DayOfWeek.WEDNESDAY,
            DayOfWeek.THURSDAY,
            DayOfWeek.FRIDAY);

    private AppointmentScheduleRules() {
    }

    public static void validate(AppointmentSlot slot, LocalDate today, LocalTime currentTime) {
        if (slot.date().isBefore(today)
                || (slot.date().equals(today) && !slot.startTime().isAfter(currentTime))) {
            throw new ScheduleRuleException("APPOINTMENT_IN_THE_PAST", "O agendamento não pode ocorrer no passado.");
        }
        if (!isBusinessDay(slot.date())) {
            throw new ScheduleRuleException("APPOINTMENT_ON_NON_BUSINESS_DAY", "Agendamentos só podem ocorrer em dias úteis.");
        }
        if (!isValidStartTime(slot.startTime())) {
            throw new ScheduleRuleException("APPOINTMENT_OUTSIDE_BUSINESS_HOURS",
                    "Agendamentos só podem ocorrer entre 07:00 e 20:00 em slots de uma hora.");
        }
    }

    public static boolean isBusinessDay(LocalDate date) {
        return BUSINESS_DAYS.contains(date.getDayOfWeek());
    }

    public static boolean isValidStartTime(LocalTime time) {
        return !time.isBefore(OPENING_TIME)
                && !time.isAfter(LAST_SLOT_TIME)
                && time.getMinute() == 0
                && time.getSecond() == 0
                && time.getNano() == 0;
    }

    public static LocalDate nextBusinessDay(LocalDate date) {
        LocalDate next = date.plusDays(1);
        while (!isBusinessDay(next)) {
            next = next.plusDays(1);
        }
        return next;
    }
}
