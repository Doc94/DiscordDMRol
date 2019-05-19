package me.mrdoc.discord.dmrol;

import discord4j.command.CommandBootstrapper;
import discord4j.core.DiscordClient;
import discord4j.core.DiscordClientBuilder;
import discord4j.core.event.domain.lifecycle.ReadyEvent;
import discord4j.core.object.entity.Guild;
import discord4j.core.object.presence.Activity;
import discord4j.core.object.presence.Presence;
import lombok.Getter;
import lombok.Setter;
import me.mrdoc.discord.dmrol.commands.DMRolCommand;
import me.mrdoc.discord.dmrol.commands.SimpleCommandDispatcher;
import me.mrdoc.discord.dmrol.commands.SimpleCommandProvider;

public class BOT {

    @Setter
    @Getter
    private boolean taskStarted = false;

    @Getter
    final DiscordClient client;

    public BOT(String token) {
        DiscordClientBuilder clientBuilder = new DiscordClientBuilder(token);

        String statusPresence = null;

        if(System.getenv().containsKey("DISCORD_PRESENCE_TEXT")) {
            statusPresence = System.getenv("DISCORD_PRESENCE_TEXT");
        }

        if(statusPresence != null && !statusPresence.isEmpty()) {
            clientBuilder.setInitialPresence(Presence.online(Activity.playing(statusPresence)));
        }

        client = clientBuilder.build();

        client.getEventDispatcher().on(ReadyEvent.class)
                .subscribe(ready -> Core.LOGGER.info("Logueado como: " + ready.getSelf().getUsername()));
        registerCommands();
        registerListeners();

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            Thread.currentThread().setName("ShutdownTask");
            Core.LOGGER.info("Apagando...");
            disconnect();
        }));
    }

    public void login() {
        client.login().block();
    }

    private void disconnect() {
        client.logout().block();
    }

    public Guild getGuild() {
        return getClient().getGuilds().single().block();
    }

    private void registerListeners() {
        client.getEventDispatcher().on(ReadyEvent.class)
                .doOnComplete(() -> Core.LOGGER.info("Completada subscripcion en ReadyEvent para tareas de moderacion/otras necesarias."))
                .subscribe(readyEvent -> {
                    if(isTaskStarted()) {
                        return;
                    }
                    setTaskStarted(true);
                });
    }

    private void registerCommands() {

        /*
         * Sistema de comandos
         * Los comandos usan el prefijo $
         */

        CommandBootstrapper bootstrapperC = new CommandBootstrapper(new SimpleCommandDispatcher("%"));
        SimpleCommandProvider simpleCommandProvider = new SimpleCommandProvider(client);

        //FEEDBACK
        simpleCommandProvider.registerCommand(new DMRolCommand());

        bootstrapperC.addProvider(simpleCommandProvider);
        bootstrapperC.attach(client).log("Commands").subscribe();
    }
}
