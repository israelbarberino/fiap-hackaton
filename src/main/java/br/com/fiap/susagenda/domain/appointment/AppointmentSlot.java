package br.com.fiap.susagenda.domain.appointment;

import java.time.LocalDate;
import java.time.LocalTime;

public record AppointmentSlot(LocalDate date, LocalTime startTime) {
}
