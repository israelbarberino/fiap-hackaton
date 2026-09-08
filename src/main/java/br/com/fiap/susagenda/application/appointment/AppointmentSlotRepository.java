package br.com.fiap.susagenda.application.appointment;

import br.com.fiap.susagenda.domain.appointment.AppointmentSlot;

import java.util.Set;

public interface AppointmentSlotRepository {
    boolean exists(AppointmentSlot slot, String professionalId);

    Set<AppointmentSlot> findOccupiedSlots(String professionalId, String unitId);

    boolean saveIfAvailable(String professionalId, String unitId, AppointmentSlot slot);
}
