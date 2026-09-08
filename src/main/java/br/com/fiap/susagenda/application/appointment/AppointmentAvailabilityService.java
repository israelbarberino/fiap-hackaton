package br.com.fiap.susagenda.application.appointment;

import br.com.fiap.susagenda.domain.appointment.AppointmentScheduleRules;
import br.com.fiap.susagenda.domain.appointment.AppointmentSlot;
import br.com.fiap.susagenda.domain.appointment.ScheduleRuleException;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class AppointmentAvailabilityService {
    private static final int REQUIRED_SUGGESTIONS = 5;

    public List<AppointmentSlot> suggestAvailableSlots(
            LocalDate requestedDate,
            LocalTime requestedStartTime,
            Set<AppointmentSlot> occupiedSlots) {
        if (!AppointmentScheduleRules.isBusinessDay(requestedDate)) {
            throw new ScheduleRuleException("APPOINTMENT_ON_NON_BUSINESS_DAY", "Agendamentos só podem ocorrer em dias úteis.");
        }
        if (!AppointmentScheduleRules.isValidStartTime(requestedStartTime)) {
            throw new ScheduleRuleException("APPOINTMENT_OUTSIDE_BUSINESS_HOURS",
                    "Agendamentos só podem ocorrer entre 07:00 e 20:00 em slots de uma hora.");
        }

        Set<AppointmentSlot> occupied = new HashSet<>(occupiedSlots);
        List<AppointmentSlot> suggestions = new ArrayList<>();
        LocalDate date = requestedDate;
        LocalTime startTime = requestedStartTime.plusHours(1);

        while (suggestions.size() < REQUIRED_SUGGESTIONS) {
            if (AppointmentScheduleRules.isBusinessDay(date)) {
                LocalTime candidate = date.equals(requestedDate)
                        ? startTime
                        : AppointmentScheduleRules.OPENING_TIME;
                while (AppointmentScheduleRules.isValidStartTime(candidate)) {
                    AppointmentSlot slot = new AppointmentSlot(date, candidate);
                    if (!occupied.contains(slot)) {
                        suggestions.add(slot);
                    }
                    if (suggestions.size() == REQUIRED_SUGGESTIONS) {
                        break;
                    }
                    candidate = candidate.plusHours(1);
                }
            }
            date = AppointmentScheduleRules.nextBusinessDay(date);
            startTime = AppointmentScheduleRules.OPENING_TIME;
        }

        return suggestions.stream()
                .sorted(Comparator.comparing(AppointmentSlot::date).thenComparing(AppointmentSlot::startTime))
                .toList();
    }
}
