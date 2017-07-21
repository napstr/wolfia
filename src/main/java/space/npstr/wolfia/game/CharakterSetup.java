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

package space.npstr.wolfia.game;

import space.npstr.wolfia.game.definitions.Alignments;
import space.npstr.wolfia.game.definitions.Roles;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Created by napster on 05.07.17.
 * <p>
 * A configuration of roles that define a game setup, mainly used by mafia games
 */
public class CharakterSetup {

    private final List<Charakter> charakters = new ArrayList<>();

    public Collection<Charakter> getCharakters() {
        return this.charakters;
    }

    public Collection<Charakter> getRandedCharakters() {
        Collections.shuffle(this.charakters);
        return this.charakters;
    }

    public CharakterSetup addRoleAndAlignment(final Alignments alignment, final Roles role, final int... amount) {
        if (amount.length > 0) { //add a few times
            for (int i = 0; i < amount[0]; i++) {
                this.charakters.add(new Charakter(alignment, role));
            }
        } else { //add just once
            this.charakters.add(new Charakter(alignment, role));
        }
        return this;
    }

    public int size() {
        return this.charakters.size();
    }

    //deliberate misspelling with the german word for character to avoid confusion with Java's Character class
    public class Charakter {
        public final Alignments alignment;
        public final Roles role;

        public Charakter(final Alignments alignment, final Roles role) {
            this.alignment = alignment;
            this.role = role;
        }
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        for (final Charakter c : this.charakters) {
            sb.append(c.alignment.textRepMaf).append(" ").append(c.role.textRep).append("\n");
        }
        return sb.toString();
    }
}
