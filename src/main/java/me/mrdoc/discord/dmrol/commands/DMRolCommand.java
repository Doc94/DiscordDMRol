package me.mrdoc.discord.dmrol.commands;

import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.MessageChannel;
import discord4j.core.object.entity.Role;
import me.mrdoc.discord.dmrol.Core;
import me.mrdoc.discord.dmrol.utils.ParsingUtil;
import org.apache.commons.lang.StringUtils;
import reactor.core.publisher.Mono;

import java.util.Arrays;

public class DMRolCommand extends BaseCommand {

    public DMRolCommand() {
        super("dmrol");
    }

    @Override
    public void run(MessageCreateEvent event, MessageChannel channel, Member member, String[] args) {
        if(args.length < 2) {
            channel.createMessage(":warning: Parametros invalidos.").subscribe();
            return;
        }

        Role rol = ParsingUtil.getRole(args[0]);

        if(rol == null) {
            channel.createMessage(":warning: El rol ingresado no existe.").subscribe();
            return;
        }

        String message = StringUtils.join(Arrays.copyOfRange(args, 1, args.length)," ");

        Core.getBot().getGuild().getMembers()
                .filterWhen(memberGuild -> memberGuild.getRoles().hasElement(rol))
                .switchIfEmpty(Mono.empty())
                .map(memberGuild -> memberGuild.getPrivateChannel().doOnError(throwable -> {}).doOnSuccess(privateChannel -> privateChannel.createMessage(message).doOnError(throwable -> {}).subscribe()).subscribe())
                .subscribe();

    }
}
