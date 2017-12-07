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

package space.npstr.wolfia.commands;

import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import space.npstr.sqlsauce.DatabaseException;
import space.npstr.wolfia.Config;
import space.npstr.wolfia.game.exceptions.IllegalGameStateException;
import space.npstr.wolfia.utils.discord.RestActions;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.regex.Pattern;

/**
 * Created by napster on 08.09.17.
 * <p>
 * Convenience container for values associated with an issued command, also does the parsing
 * <p>
 * Don't save these anywhere as they hold references to JDA objects, just pass them down through (short-lived) command execution
 */
public class CommandContext extends MessageContext {


    //@formatter:off
    @Nonnull public final String trigger;                        // the command trigger, e.g. "play", or "p", or "pLaY", whatever the user typed
    @Nonnull public final String[] args ;                        // the arguments split by whitespace, excluding prefix and trigger
    @Nonnull public final String rawArgs;                        // raw arguments excluding prefix and trigger, trimmed
    @Nonnull public final BaseCommand command;
//    @Nonnull private final Histogram.Timer received;             // time when we received this command
    //@formatter:on

    /**
     * @param event the event to be parsed
     * @return The full context for the triggered command, or null if it's not a command that we know.
     */
    public static CommandContext parse(final MessageReceivedEvent event)//, final Histogram.Timer received)
            throws DatabaseException {

        final String raw = event.getMessage().getRawContent();
        String input;

        final String prefix = Config.PREFIX;
        if (raw.startsWith(prefix)) {
            input = raw.substring(prefix.length());
        } else {
            return null;
        }

        input = input.trim();// eliminate possible whitespace between the prefix and the rest of the input
        if (input.isEmpty()) {
            return null;
        }

        final String[] args = input.split("\\s+"); //split by any length of white space characters (including new lines)
        if (args.length < 1) {
            return null; //while this shouldn't technically be possible due to the preprocessing of the input, better be safe than throw exceptions
        }

        final String commandTrigger = args[0];
        final BaseCommand command = CommRegistry.getRegistry().getCommand(commandTrigger.toLowerCase());

        if (command == null) {
            return null;
        } else {
            return new CommandContext(event, commandTrigger,
                    Arrays.copyOfRange(args, 1, args.length),//exclude args[0] that contains the command trigger
                    input.replaceFirst(Pattern.quote(commandTrigger), "").trim(),
                    command
            );
        }
    }

    private CommandContext(@Nonnull final MessageReceivedEvent event, @Nonnull final String trigger,
                           @Nonnull final String[] args, @Nonnull final String rawArgs, @Nonnull final BaseCommand command) {
//                           @Nonnull final Histogram.Timer received) {
        super(event);
        this.trigger = trigger;
        this.args = args;
        this.rawArgs = rawArgs;
        this.command = command;
//        this.received = received;
    }


    public boolean hasArguments() {
        return this.args.length > 0 && !this.rawArgs.isEmpty();
    }

    /**
     * Deletes the users message that triggered this command, if we have the permissions to do so
     */
    public void deleteMessage() {
        final TextChannel tc = this.msg.getTextChannel();
        if (tc != null && tc.getGuild().getSelfMember().hasPermission(tc, Permission.MESSAGE_MANAGE)) {
            RestActions.deleteMessage(this.msg);
        }
    }

    //reply with the help
    public void help() {
        reply(this.command.formatHelp(this.invoker));
    }

    public boolean invoke() throws IllegalGameStateException, DatabaseException {
        final boolean success = this.command.execute(this);
//        this.received.observeDuration();
        return success;
    }
}
