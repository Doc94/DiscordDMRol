package me.mrdoc.discord.dmrol.commands;

import com.google.common.collect.ImmutableList;
import discord4j.command.CommandProvider;
import discord4j.command.ProviderContext;
import discord4j.core.DiscordClient;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.MessageChannel;
import discord4j.core.object.entity.Role;
import discord4j.core.object.entity.TextChannel;
import discord4j.core.object.util.Permission;
import lombok.Getter;
import me.mrdoc.discord.dmrol.Core;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.HashMap;
import java.util.Objects;

public class SimpleCommandProvider implements CommandProvider<Void> {

    @Getter
    private final HashMap<String, BaseCommand> commands = new HashMap<>();

    public SimpleCommandProvider(DiscordClient client) {
    }

    /**
     * Registra un comando en el sistema
     * @param command Comando a registrar
     */
    public void registerCommand(BaseCommand command) {
        getCommands().put(command.getName(),command);
    }

    @Override
    public Flux<ProviderContext<Void>> provide(MessageCreateEvent messageCreateEvent,
                                               String cmdName,
                                               int startIndex,
                                               int endIndex) {
        return Mono.just(messageCreateEvent.getMessage())
                .filterWhen(message -> message.getChannel().ofType(TextChannel.class).hasElement())
                .filter(message -> message.getAuthor().isPresent() && !message.getAuthor().get().isBot())
                .switchIfEmpty(Mono.empty())
                .flatMapMany($ -> Mono.just(cmdName.toLowerCase())
                        .filter(getCommands()::containsKey)
                        .switchIfEmpty(messageCreateEvent.getMessage().delete().delayElement(Duration.ofSeconds(1)).doOnError(throwable -> Core.LOGGER.error("Error al borrar mensaje de proceso vacio de SimpleCommandProvider: " + throwable.getMessage(),throwable)).then(Mono.empty()))
                        .map(cmd -> {
                            Member member = messageCreateEvent.getMember().get();
                            BaseCommand command = getCommands().get(cmd);

                            if(!hasPermission(member)) {
                                return ProviderContext.of(new BaseCommand(null) {
                                    @Override
                                    public void run(MessageCreateEvent event, MessageChannel channel, Member member, String[] args) {
                                        Core.LOGGER.debug("El usuario [" + member.getDisplayName() + "] intento usar el comando comando [" + cmd + "] del cual no tiene permisos.");
                                    }
                                });

                            }

                            return ProviderContext.of(command);
                        }))
                ;
    }

    private boolean hasPermission(Member user) {
        if(Core.getBot().getGuild().getOwnerId().equals(user.getId())) {
            return true;
        }

        if(user.getRoles().collectList().block() == null) {
            return false;
        }

        for(Role role : ImmutableList.copyOf(Objects.requireNonNull(user.getRoles().collectList().block())).reverse()) {
            if(role.getPermissions().contains(Permission.ADMINISTRATOR)) {
                return true;
            }
        }
        return false;
    }
}

