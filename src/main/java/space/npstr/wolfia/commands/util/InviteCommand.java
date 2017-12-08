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


import space.npstr.sqlsauce.DatabaseException;
import space.npstr.wolfia.App;
import space.npstr.wolfia.commands.BaseCommand;
import space.npstr.wolfia.commands.CommandContext;

import javax.annotation.Nonnull;

/**
 * Created by napster on 21.11.17.
 */
public class InviteCommand extends BaseCommand {

    public InviteCommand(@Nonnull final String name, @Nonnull final String... aliases) {
        super(name, aliases);
    }

    @Nonnull
    @Override
    protected String help() {
        return invocation()
                + "\n#Post invite links for Wolfia and the Wolfia Lounge.";
    }

    @Override
    public boolean execute(@Nonnull final CommandContext context) throws DatabaseException {
        context.reply(String.format("**Wolfia Bot Invite**:\n<%s>\n**Wolfia Lounge**:\n%s",
                App.INVITE_LINK, App.WOLFIA_LOUNGE_INVITE));
        return true;
    }
}
