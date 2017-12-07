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

package space.npstr.wolfia.commands.game;

import net.dv8tion.jda.core.entities.Guild;
import space.npstr.sqlsauce.DatabaseException;
import space.npstr.wolfia.commands.BaseCommand;
import space.npstr.wolfia.commands.CommandContext;
import space.npstr.wolfia.db.entities.SetupEntity;
import space.npstr.wolfia.game.Game;
import space.npstr.wolfia.game.definitions.Games;

import javax.annotation.Nonnull;

/**
 * Created by npstr on 24.08.2016
 * <p>
 * this command should display the status of whatever is happening in a channel currently
 * <p>
 * is there a game running, whats it's state?
 * if not, is there a setup created for this channel, whats the status here, inned players etc?
 */
public class StatusCommand extends BaseCommand {

    public StatusCommand(final String trigger, final String... aliases) {
        super(trigger, aliases);
    }

    @Nonnull
    @Override
    public String help() {
        return invocation() + "\n#Post the current game status or sign up list.";
    }

    @Override
    public boolean execute(@Nonnull final CommandContext context) throws DatabaseException {

        final Game game = Games.get(context.getChannel().getIdLong());
        if (game != null) {
            context.reply(game.getStatus().build());
            return true;
        }

        //was this called from a private guild of an ongoing game? post the status of the corresponding game
        for (final Game g : Games.getAll().values()) {
            final Guild guild = context.getGuild();
            if (guild != null && guild.getIdLong() == g.getPrivateGuildId()) {
                context.reply(g.getStatus().build());
                return true;
            }
        }

        final SetupEntity setup = SetupEntity.load(context.channel.getIdLong());
        context.reply(setup.getStatus());
        return true;
    }
}
