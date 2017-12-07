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

package space.npstr.wolfia.events;

import net.dv8tion.jda.core.events.ReadyEvent;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import space.npstr.sqlsauce.DatabaseException;
import space.npstr.wolfia.Config;
import space.npstr.wolfia.commands.BaseCommand;
import space.npstr.wolfia.commands.CommRegistry;
import space.npstr.wolfia.commands.CommandContext;
import space.npstr.wolfia.commands.CommandHandler;
import space.npstr.wolfia.game.Game;
import space.npstr.wolfia.game.definitions.Games;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by npstr on 25.08.2016
 */
public class CommandListener extends ListenerAdapter {

    private final static Logger log = LoggerFactory.getLogger(CommandListener.class);

    private static final ExecutorService commandExecutor = Executors.newCachedThreadPool();

    private final Set<Long> IGNORED_GUILDS = new HashSet<>();

    public CommandListener(final Collection<Long> ignoredGuilds) {
        this.IGNORED_GUILDS.addAll(ignoredGuilds);
    }

    public void addIgnoredGuild(final long guildId) {
        this.IGNORED_GUILDS.add(guildId);
    }

    public static ExecutorService getCommandExecutor() {
        return commandExecutor;
    }

    //sort the checks here approximately by widest and cheapest filters higher up, and put expensive filters lower
    @Override
    public void onMessageReceived(final MessageReceivedEvent event) {
        final long received = System.currentTimeMillis();
        //ignore private channels
        if (event.getPrivateChannel() != null) {
            return;
        }

        //ignore certain guilds (private guilds for example, they have their own listener)
        if (this.IGNORED_GUILDS.contains(event.getGuild().getIdLong())) {
            return;
        }

        //update user stats todo this needs to be added to game related messages sent in private channels and private guilds
        final Game g = Games.get(event.getChannel().getIdLong());
        if (g != null) g.userPosted(event.getMessage());

        //ignore messages not starting with the prefix (prefix is accepted case insensitive)
        final String raw = event.getMessage().getRawContent();
        if (!raw.toLowerCase().startsWith(Config.PREFIX.toLowerCase())) {
            return;
        }

        //ignore bot accounts generally
        if (event.getAuthor().isBot()) {
            return;
        }

        //bot should ignore itself
        if (event.getAuthor().getId().equals(event.getJDA().getSelfUser().getId())) {
            return;
        }

        //ignore channels where we don't have sending permissions, with a special exception for the help command
        if (!event.getTextChannel().canTalk() && !raw.toLowerCase().startsWith((Config.PREFIX + CommRegistry.COMM_TRIGGER_HELP).toLowerCase())) {
            return;
        }

        final CommandContext context;
        try {
            context = CommandContext.parse(event);
        } catch (final DatabaseException e) {
            log.error("Db blew up parsing a private command", e);
            return;
        }
        if (context == null) {
            return;
        }
        commandExecutor.submit(() -> CommandHandler.handleCommand(context, this::filter));
    }

    private boolean filter(final BaseCommand command) {
        return true; //allow all commands from the command listener
    }

    @Override
    public void onReady(final ReadyEvent event) {
        log.info("Logged in as: " + event.getJDA().getSelfUser().getName());
    }
}