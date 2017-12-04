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

package space.npstr.wolfia.db.entities;

import space.npstr.sqlsauce.DatabaseException;
import space.npstr.sqlsauce.entities.discord.DiscordGuild;
import space.npstr.sqlsauce.fp.types.EntityKey;

import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * Created by napster on 25.07.17.
 * <p>
 * Represents a discord guild
 * Caches some stuff similar to CachedUser
 * Just dump data in here
 */
@Entity
@Table(name = "guilds")
public class EGuild extends DiscordGuild<EGuild> {


    //for jpa / IEntity
    public EGuild() {
    }

    public static EGuild load(final long guildId) throws DatabaseException {
        return load(EntityKey.of(guildId, EGuild.class));
    }

}