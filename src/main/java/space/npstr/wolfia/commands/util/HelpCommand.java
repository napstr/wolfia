/*
 * Copyright (C) 2017 Dennis Neufeld
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package space.npstr.wolfia.commands.util;

import net.dv8tion.jda.core.entities.ChannelType;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.TextChannel;
import space.npstr.wolfia.App;
import space.npstr.wolfia.Config;
import space.npstr.wolfia.commands.BaseCommand;
import space.npstr.wolfia.commands.CommRegistry;
import space.npstr.wolfia.commands.CommandContext;
import space.npstr.wolfia.commands.IOwnerRestricted;
import space.npstr.wolfia.utils.discord.TextchatUtils;

import javax.annotation.Nonnull;
import java.util.function.Consumer;

/**
 * Created by npstr on 09.09.2016
 */
public class HelpCommand extends BaseCommand {

    public HelpCommand(final String trigger, final String... aliases) {
        super(trigger, aliases);
    }

    @Nonnull
    @Override
    public String help() {
        return invocation() + " [command]"
                + "\n#Send you Wolfia's general help and links to documentation, or see the help for a specific command. Examples:"
                + "\n  " + invocation()
                + "\n  " + invocation() + " shoot";
    }

    @Override
    public boolean execute(@Nonnull final CommandContext context) {
        if (Config.C.isDebug && !context.isOwner()) {
            return true;//dont answer the help command in debug mode unless it's the owner
        }

        final MessageChannel channel = context.channel;
        if (context.hasArguments() && channel.getType() == ChannelType.TEXT && ((TextChannel) channel).canTalk()) {
            final BaseCommand command = CommRegistry.getRegistry().getCommand(context.args[0]);
            final String answer;
            if (command == null || command instanceof IOwnerRestricted) {
                answer = String.format("There is no command registered for `%s`. Use `%s` to see all available commands!",
                        TextchatUtils.defuseMentions(context.args[0]), Config.PREFIX + CommRegistry.COMM_TRIGGER_COMMANDS);
            } else {
                answer = TextchatUtils.asMarkdown(command.getHelp());
            }
            context.reply(answer);
            return true;
        }

        final String help = String.format("Hi %s,%nyou can find %s's **documentation** and a **full list of commands** under%n<%s>"
                        + "%n%n**To invite the bot to your server please follow this link**:%n<%s>"
                        + "%n%nDrop by the Wolfia Lounge to play games, get support, leave feedback, get notified of updates and vote on the roadmap:%n<%s>"
                        + "%n%nCode open sourced on Github:%n<%s>"
                        + "%n%nCreated and hosted by Napster:%n<%s>",
                context.invoker.getName(), context.invoker.getJDA().getSelfUser().getName(), App.DOCS_LINK, App.INVITE_LINK,
                App.WOLFIA_LOUNGE_INVITE, App.GITHUB_LINK, "https://npstr.space");

        final Consumer<Message> onSuccess = m -> {
            if (channel.getType() == ChannelType.TEXT && ((TextChannel) channel).canTalk()) {
                final String answer = String.format("sent you a PM with the help!"
                                + "\nUse `%s` and `%s` to start games."
                                + "\nSay `%s` to show all commands."
                                + "\nSay `%s [command]` to show help for a specific command.",
                        Config.PREFIX + CommRegistry.COMM_TRIGGER_IN, Config.PREFIX + CommRegistry.COMM_TRIGGER_START,
                        Config.PREFIX + CommRegistry.COMM_TRIGGER_COMMANDS,
                        Config.PREFIX + CommRegistry.COMM_TRIGGER_COMMANDS);
                context.replyWithMention(answer);
            }
        };
        final Consumer<Throwable> onFail = t -> {
            if (channel.getType() == ChannelType.TEXT && ((TextChannel) channel).canTalk())
                context.replyWithMention("can't send you a private message with the help."
                        + " Please unblock me or change your privacy settings.");
        };

        context.replyPrivate(help, onSuccess, onFail);
        return true;
    }
}
