package me.mrdoc.discord.dmrol.commands;

import discord4j.command.Command;
import discord4j.command.CommandProvider;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.MessageChannel;
import lombok.Getter;
import me.mrdoc.discord.dmrol.Core;
import reactor.core.publisher.Mono;
import reactor.util.annotation.Nullable;

import java.util.Arrays;

/**
 * Created on 16-02-2019 for Discord Fortnite.
 *
 * @author Doc
 */
@Getter
public abstract class BaseCommand implements Command<Void> {

    private String name;

    public BaseCommand(String commandName) {
        name = commandName;
    }

    /**
     * Called to execute this command.
     *
     * @param event The event that triggered this command's execution.
     * @param context Additional context by this command's {@link CommandProvider}.
     * @return A mono, whose completion signals that this command has been executed.
     */
    @Override
    public Mono<Void> execute(MessageCreateEvent event, @Nullable Void context) {
        if(!event.getMessage().getContent().isPresent()) {
            Core.LOGGER.error("El comando " + getName() + " fue ejecutado en un mensaje sin contenido (Es posible?)");
            return Mono.empty();
        }

        if(!event.getMessage().getAuthor().isPresent()) {
            Core.LOGGER.error("El comando " + getName() + " fue ejecutado en un mensaje sin autor.");
            return Mono.empty();
        }

        if(event.getMessage().getAuthor().get().isBot()) {
            return Mono.empty();
        }

        String message = event.getMessage().getContent().get();
        String[] preArgs = message.split(" ");
        String[] args;
        if(preArgs.length == 1) {
            args = new String[0];
        } else {
            args = Arrays.copyOfRange(preArgs,1,preArgs.length);
        }

        return Mono.create(a -> event.getMessage().delete()
                .doOnSuccess(aVoid -> run(event,event.getMessage().getChannel().block(), Core.getBot().getGuild().getMemberById(event.getMessage().getAuthor().get().getId()).block(),args))
                .doOnError(throwable -> {
                    if(!throwable.getMessage().contains("404")) { //Esto ocurre cuando se pisan funciones de borrado
                        Core.LOGGER.error("Ocurrio un problema al borrar un mensaje para ejecutar un comando. Detalles: " + throwable.getMessage(),throwable);
                        if(Core.isDebug()) {
                            throwable.printStackTrace();
                        }
                    }
                })
                .subscribe());
    }

    public abstract void run(MessageCreateEvent event, MessageChannel channel, Member member, String[] args);
}

