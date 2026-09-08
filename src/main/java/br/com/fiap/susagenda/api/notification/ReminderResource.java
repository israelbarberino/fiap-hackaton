package br.com.fiap.susagenda.api.notification;

import br.com.fiap.susagenda.application.notification.ReminderProcessorService;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/api/v1/notifications")
@Produces(MediaType.APPLICATION_JSON)
public class ReminderResource {
    private final ReminderProcessorService service;

    public ReminderResource(ReminderProcessorService service) {
        this.service = service;
    }

    @POST
    @Path("/process-reminders")
    public Response processReminders() {
        return Response.ok(new ProcessResponse(service.processDailyReminders())).build();
    }

    public record ProcessResponse(int sent) {
    }
}
