package me.mrdoc.discord.dmrol.utils;

import discord4j.core.object.entity.*;
import discord4j.core.object.util.Snowflake;
import me.mrdoc.discord.dmrol.Core;
import org.slf4j.Logger;
import reactor.core.publisher.Mono;
import reactor.util.annotation.Nullable;

public class ParsingUtil {

    private static final Logger LOGGER = Core.LOGGER;

    /**
     * Parse user to member
     * @param user The user string to search for
     * @return The user that was found
     * @throws IllegalArgumentException On failure
     */
    @Nullable
    public static Member parseToMember(User user) {
        LOGGER.debug(String.format("[Member] Passed in with `%s`...", user.getUsername() + "(" + user.getId().asString() + ")"));
        try {
            return user.asMember(Core.getBot().getGuild().getId()).block();
        } catch (Exception ignored) {

        }
        return null;
    }

    /**
     * Search for a user
     * @param s The user string to search for
     * @return The user that was found
     * @throws IllegalArgumentException On failure
     */
    @Nullable
    public static User getUser(String s) {
        LOGGER.debug(String.format("[User] Passed in with `%s`...", s));
        try {
            if (s.matches("<@!?\\d+>")) {
                long id = Long.parseLong(s.replaceAll("<@!?(\\d+)>", "$1"));
                return Core.getBot().getClient().getUserById(Snowflake.of(id))
                        .doOnError(throwable -> {
                            if(!throwable.getMessage().contains("code=10003")) { //not found code
                                LOGGER.error("Ocurrio un problema para obtener un User. Razon: " + throwable.getMessage(),throwable);
                            }
                        })
                        .switchIfEmpty(Mono.error(new Exception("Usuario no encontrado")))
                        .blockOptional().orElse(null);
            } else if (s.matches("\\d+")) {
                long id = Long.parseLong(s);
                return Core.getBot().getClient().getUserById(Snowflake.of(id))
                        .doOnError(throwable -> {
                            if(!throwable.getMessage().contains("code=10003")) { //not found code
                                LOGGER.error("Ocurrio un problema para obtener un User. Razon: " + throwable.getMessage(),throwable);
                            }
                        })
                        .switchIfEmpty(Mono.error(new Exception("Usuario no encontrado")))
                        .blockOptional().orElse(null);
            }
        } catch (Exception ignored) {

        }
        return null;
    }

    @Nullable
    public static Role getRole(String s) {
        return getRole(Core.getBot().getGuild(),s);
    }

    /**
     * Search for a role
     * @param guild The guild to search for the role
     * @param s The role to search for
     * @return The role that was found
     */
    @Nullable
    public static Role getRole(Guild guild, String s) {
        LOGGER.debug(String.format("[Role] Passed in with `%s`...", s));
        try {
            if (s.matches("<@&\\d+>")) {
                long id = Long.parseLong(s.replaceAll("<@&(\\d+)>", "$1"));
                return guild.getRoleById(Snowflake.of(id))
                        .doOnError(throwable -> {
                            if(!throwable.getMessage().contains("code=10003")) { //not found code
                                LOGGER.error("Ocurrio un problema para obtener un rol. Razon: " + throwable.getMessage(),throwable);
                            }
                        })
                        .switchIfEmpty(Mono.error(new Exception("Rol no encontrado")))
                        .onErrorReturn(null)
                        .block();
            } else if (s.matches("\\d+")) {
                long id = Long.parseLong(s);
                return guild.getRoleById(Snowflake.of(id))
                        .doOnError(throwable -> {
                            if(!throwable.getMessage().contains("code=10003")) { //not found code
                                LOGGER.error("Ocurrio un problema para obtener un rol. Razon: " + throwable.getMessage(),throwable);
                            }
                        })
                        .switchIfEmpty(Mono.error(new Exception("Rol no encontrado")))
                        .onErrorReturn(null)
                        .block();
            } else {
                return guild.getRoles().filter(role -> role.getName().equalsIgnoreCase(s)).blockFirst();
            }
        } catch (Exception ignored) {
            return null;
        }
    }

    @Nullable
    public static TextChannel getChannel(String s) {
        return getChannel(Core.getBot().getGuild(),s);
    }

    @Nullable
    public static TextChannel getChannel(Guild guild, String s) {
        LOGGER.debug(String.format("[Channel] Passed in with '%s'...", s));

        Channel channel;

        try {
            if (s.matches("<#\\d+>")) {
                long id = Long.parseLong(s.replaceAll("<#(\\d+)>", "$1"));
                channel = guild.getChannelById(Snowflake.of(id))
                        .doOnError(throwable -> {
                            if(!throwable.getMessage().contains("code=10003")) { //not found code
                                LOGGER.error("Ocurrio un problema para obtener un TextChannel. Razon: " + throwable.getMessage(),throwable);
                            }
                        })
                        .switchIfEmpty(Mono.error(new Exception("TextChannel no encontrado")))
                        .onErrorReturn(null)
                        .block();
            } else if (s.matches("\\d+")) {
                long id = Long.parseLong(s);
                if(id == 0) {
                    return null;
                }
                channel = guild.getChannelById(Snowflake.of(id))
                        .doOnError(throwable -> {
                            if(!throwable.getMessage().contains("code=10003")) { //not found code
                                LOGGER.error("Ocurrio un problema para obtener un TextChannel. Razon: " + throwable.getMessage(),throwable);
                            }
                        })
                        .switchIfEmpty(Mono.error(new Exception("TextChannel no encontrado")))
                        .onErrorReturn(null)
                        .block();
            } else {
                channel = guild.getChannels().filter(c -> c.getName().equalsIgnoreCase(s)).blockFirst();
            }
        } catch (Exception ignored) {
            channel = null;
        }

        if (channel instanceof TextChannel)
            return (TextChannel) channel;
        return null;
    }

    /**
     * Format a time in milliseconds to dd:hh:mm:ss
     * @param milliseconds The milliseconds to format
     * @return The formatted time
     */
    public static String formatTime(long milliseconds) {
        StringBuilder builder = new StringBuilder();
        long convert = (milliseconds / 1000);
        int days = (int) (convert / (3600 * 24));
        int hours = (int) (convert / 3600 % 24);
        int minutes = (int) (convert / 60 % 60);
        int seconds = (int) (convert % 60);

        if (days > 0) {
            builder.append(days);
            builder.append("d:");
        }

        if (hours > 0) {
            if (hours < 10)
                builder.append("0");
            builder.append(hours);
            builder.append("h:");
        } else if (days > 0) {
            builder.append("00:");
        }

        if (minutes > 0) {
            if (minutes < 10 && (hours > 0 || days > 0))
                builder.append("0");
            builder.append(minutes).append("m");
        } else {
            builder.append("0");
            if (hours > 0 || days > 0)
                builder.append("0");
            builder.append("m");
        }

        builder.append(":");
        if (seconds < 10)
            builder.append("0");
        builder.append(seconds).append("s");

        if (convert < 1f && convert > 0f) {
            return "0:0" + String.format("%.3f", Math.max(convert, 0.001f));
        }

        return builder.toString();
    }
}
