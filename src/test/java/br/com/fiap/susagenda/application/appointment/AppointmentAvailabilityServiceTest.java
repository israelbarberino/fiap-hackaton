package br.com.fiap.susagenda.application.appointment;

import br.com.fiap.susagenda.domain.appointment.AppointmentSlot;
import br.com.fiap.susagenda.domain.appointment.ScheduleRuleException;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AppointmentAvailabilityServiceTest {
    private final AppointmentAvailabilityService service = new AppointmentAvailabilityService();

    @Test
    void shouldReturnFiveSuggestionsOnRequestedDayAndNextBusinessDays() {
        LocalDate requestedDate = LocalDate.of(2026, 9, 7);

        var suggestions = service.suggestAvailableSlots(
                requestedDate,
                LocalTime.of(8, 0),
                Set.of(new AppointmentSlot(requestedDate, LocalTime.of(9, 0))));

        assertEquals(5, suggestions.size());
        assertEquals(new AppointmentSlot(requestedDate, LocalTime.of(10, 0)), suggestions.getFirst());
    }

    @Test
    void shouldSkipWeekendWhenLookingForSuggestions() {
        LocalDate friday = LocalDate.of(2026, 9, 11);

        var suggestions = service.suggestAvailableSlots(friday, LocalTime.of(19, 0), Set.of());

        assertEquals(5, suggestions.size());
        assertEquals(LocalDate.of(2026, 9, 14), suggestions.getFirst().date());
    }

    @Test
    void shouldRejectNonBusinessDay() {
        assertThrows(ScheduleRuleException.class, () -> service.suggestAvailableSlots(
                LocalDate.of(2026, 9, 12), LocalTime.of(9, 0), Set.of()));
    }

    @Test
    void shouldRejectUnalignedOrOutOfRangeTime() {
        assertThrows(ScheduleRuleException.class, () -> service.suggestAvailableSlots(
                LocalDate.of(2026, 9, 7), LocalTime.of(6, 0), Set.of()));
        assertThrows(ScheduleRuleException.class, () -> service.suggestAvailableSlots(
                LocalDate.of(2026, 9, 7), LocalTime.of(9, 30), Set.of()));
    }
}
