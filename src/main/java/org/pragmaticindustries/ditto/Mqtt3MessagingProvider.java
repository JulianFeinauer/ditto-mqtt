package org.pragmaticindustries.ditto;

import org.eclipse.ditto.client.configuration.AuthenticationConfiguration;
import org.eclipse.ditto.client.configuration.MessagingConfiguration;
import org.eclipse.ditto.client.messaging.MessagingProvider;
import org.eclipse.ditto.model.messages.Message;
import org.eclipse.ditto.protocoladapter.Adaptable;
import org.eclipse.ditto.protocoladapter.TopicPath;
import org.eclipse.ditto.signals.commands.base.Command;
import org.eclipse.ditto.signals.commands.base.CommandResponse;
import org.eclipse.ditto.signals.commands.things.ThingCommandResponse;
import org.eclipse.ditto.signals.events.base.Event;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.function.Consumer;

public class Mqtt3MessagingProvider implements MessagingProvider {
    @Override
    public void initialize() {

    }

    @Override
    public AuthenticationConfiguration getAuthenticationConfiguration() {
        return null;
    }

    @Override
    public MessagingConfiguration getMessagingConfiguration() {
        return null;
    }

    @Override
    public ExecutorService getExecutorService() {
        return null;
    }

    @Override
    public CompletableFuture<Adaptable> sendAdaptable(Adaptable adaptable) {
        return null;
    }

    @Override
    public void send(Message<?> message, TopicPath.Channel channel) {

    }

    @Override
    public void sendCommand(Command<?> command, TopicPath.Channel channel) {

    }

    @Override
    public void sendCommandResponse(CommandResponse<?> commandResponse, TopicPath.Channel channel) {

    }

    @Override
    public void emitEvent(Event<?> event, TopicPath.Channel channel) {

    }

    @Override
    public void registerReplyHandler(Consumer<ThingCommandResponse> consumer) {

    }

    @Override
    public boolean registerMessageHandler(String s, Map<String, String> map, Consumer<Message<?>> consumer, CompletableFuture<Void> completableFuture) {
        return false;
    }

    @Override
    public void deregisterMessageHandler(String s, CompletableFuture<Void> completableFuture) {

    }

    @Override
    public void close() {

    }
}
