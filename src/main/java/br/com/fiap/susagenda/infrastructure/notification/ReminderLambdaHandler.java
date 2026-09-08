package br.com.fiap.susagenda.infrastructure.notification;

import br.com.fiap.susagenda.application.notification.ReminderProcessorService;
import com.amazonaws.services.lambda.runtime.Context;
import io.quarkus.arc.Arc;
import io.quarkus.runtime.Quarkus;

import java.util.Map;

public class ReminderLambdaHandler {
    public Map<String, Object> handleRequest(Map<String, Object> event, Context context) {
        if (Arc.container() == null) {
            Quarkus.manualInitialize();
        }
        int sent = Arc.container().instance(ReminderProcessorService.class).get().processDailyReminders();
        return Map.of("sent", sent, "status", "processed");
    }
}
