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

import org.springframework.stereotype.Component;
import space.npstr.wolfia.Launcher;
import space.npstr.wolfia.commands.BaseCommand;
import space.npstr.wolfia.commands.CommandContext;
import space.npstr.wolfia.db.entities.stats.ActionStats;
import space.npstr.wolfia.db.entities.stats.GameStats;
import space.npstr.wolfia.db.entities.stats.TeamStats;
import space.npstr.wolfia.game.definitions.Games;
import space.npstr.wolfia.game.exceptions.IllegalGameStateException;
import space.npstr.wolfia.game.tools.NiceEmbedBuilder;
import space.npstr.wolfia.utils.discord.Emojis;
import space.npstr.wolfia.utils.discord.TextchatUtils;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Created by napster on 03.06.17.
 * <p>
 * Shows replays of games that are over
 */
@Component
public class ReplayCommand implements BaseCommand {

    public static final String TRIGGER = "replay";

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ReplayCommand.class);

    @Override
    public String getTrigger() {
        return TRIGGER;
    }

    @Nonnull
    @Override
    public String help() {
        return invocation() + " #gameid"
                + "\n#Show the replay of a game. Examples:"
                + "\n  " + invocation() + " #69"
                + "\n  " + invocation() + " 9001";
    }

    @Override
    public boolean execute(@Nonnull final CommandContext context)
            throws IllegalGameStateException {

        if (!context.hasArguments()) {
            context.help();
            return false;
        }

        final long gameId;
        try {
            gameId = Long.parseLong(context.args[0].replace("#", ""));
        } catch (final NumberFormatException ex) {
            context.help();
            return false;
        }

        final String sql = "SELECT g FROM GameStats g JOIN FETCH g.startingTeams t JOIN FETCH g.actions a JOIN FETCH t.players p WHERE g.gameId = :gameId";
        final Map<String, Object> params = new HashMap<>();
        params.put("gameId", gameId);
        final List<GameStats> gameStatsList = Launcher.getBotContext().getDatabase().getWrapper().selectJpqlQuery(sql, params, GameStats.class);

        if (gameStatsList.isEmpty()) {
            context.replyWithMention("there is no such game in the database.");
            return false;
        }
        final GameStats gameStats = gameStatsList.get(0);

        final NiceEmbedBuilder eb = NiceEmbedBuilder.defaultBuilder();

        //1. post summary like game, mode, players, roles
        eb.setTitle("**Game #" + gameStats.getId() + "**");
        eb.setDescription(gameStats.getGameType().textRep + " " + gameStats.getGameMode());
        eb.addField("Game started", TextchatUtils.toUtcTime(gameStats.getStartTime()), true);

        gameStats.getStartingTeams().forEach(team ->
                eb.addField(gameStats.getGameType() == Games.POPCORN ? team.getAlignment().textRepWW : team.getAlignment().textRepMaf,
                        String.join(", ",
                                team.getPlayers().stream().map(player -> "`" + player.getNickname() + "`").collect(Collectors.toList())),
                        true)
        );


        //2. post the actions
        final List<ActionStats> sortedActions = new ArrayList<>(gameStats.getActions());
        sortedActions.sort(Comparator.comparingLong(ActionStats::getTimeStampSubmitted));
        final String fieldTitle = "Actions";
        final NiceEmbedBuilder.ChunkingField actionsField = new NiceEmbedBuilder.ChunkingField(fieldTitle, false);
        for (final ActionStats action : sortedActions) {
            final String actionStr = action.toString();
            actionsField.add(actionStr, true);
        }
        eb.addField(actionsField);

        //3. post the winners
        eb.addField("Game ended", TextchatUtils.toUtcTime(gameStats.getEndTime()), true);
        eb.addField("Game length", TextchatUtils.formatMillis(gameStats.getEndTime() - gameStats.getStartTime()), true);

        final String winText;
        final Optional<TeamStats> winners = gameStats.getStartingTeams().stream().filter(TeamStats::isWinner).findFirst();
        if (!winners.isPresent()) {
            //shouldn't happen lol
            log.error("Game #{} has no winning team in the data", gameId);
            winText = "Game has no winning team " + Emojis.WOLFTHINK + "\nReplay must be borked. Error has been reported.";
        } else {
            final TeamStats winningTeam = winners.get();
            String flavouredTeamName = winningTeam.getAlignment().textRepMaf;
            if (gameStats.getGameType() == Games.POPCORN) flavouredTeamName = winningTeam.getAlignment().textRepWW;
            winText = "**Team " + flavouredTeamName + " wins the game!**";
        }
        eb.addField("Winners", winText, true);

        context.reply(eb.build());
        return true;
    }
}
