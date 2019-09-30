/*
 * Copyright (C) 2017-2018 Dennis Neufeld
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

package space.npstr.wolfia;

import org.springframework.stereotype.Component;
import space.npstr.wolfia.config.properties.ListingsConfig;
import space.npstr.wolfia.config.properties.WolfiaConfig;
import space.npstr.wolfia.db.Database;
import space.npstr.wolfia.events.PrivateGuildProvider;
import space.npstr.wolfia.game.tools.ExceptionLoggingExecutor;

/**
 * Created by napster on 10.05.18.
 * <p>
 * Temporary uber class that allows resources that were previously accessed statically to continue to be accessed
 * that way through {@link Launcher#getBotContext()}, until the whole project is refactored into non-static components.
 * <p>
 * todo resolve this temporary file
 */
@Component
public class BotContext {

    private final Database database;
    private final WolfiaConfig wolfiaConfig;
    private final ListingsConfig listingsConfig;
    private final PrivateGuildProvider privateGuildProvider;
    private final ExceptionLoggingExecutor executor;

    public BotContext(final Database database, final WolfiaConfig wolfiaConfig, final ListingsConfig listingsConfig,
                      PrivateGuildProvider privateGuildProvider, ExceptionLoggingExecutor executor) {
        this.database = database;
        this.wolfiaConfig = wolfiaConfig;
        this.listingsConfig = listingsConfig;
        this.privateGuildProvider = privateGuildProvider;
        this.executor = executor;
    }

    public Database getDatabase() {
        return this.database;
    }

    public WolfiaConfig getWolfiaConfig() {
        return this.wolfiaConfig;
    }

    public ListingsConfig getListingsConfig() {
        return this.listingsConfig;
    }

    public PrivateGuildProvider getPrivateGuildProvider() {
        return this.privateGuildProvider;
    }

    public ExceptionLoggingExecutor getExecutor() {
        return this.executor;
    }
}
