package me.mrdoc.discord.dmrol.commands;

import discord4j.command.util.AbstractCommandDispatcher;
import discord4j.core.event.domain.message.MessageCreateEvent;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Mono;

public class SimpleCommandDispatcher extends AbstractCommandDispatcher {

    private final String prefix;

    public SimpleCommandDispatcher(String prefix) {
        this.prefix = prefix;
    }

    @Override
    protected Publisher<String> getPrefixes(MessageCreateEvent event) {
        return Mono.just(prefix);
    }
}
