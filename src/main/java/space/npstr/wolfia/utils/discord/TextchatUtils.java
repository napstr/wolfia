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

package space.npstr.wolfia.utils.discord;

import net.dv8tion.jda.core.MessageBuilder;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Invite;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.exceptions.PermissionException;
import space.npstr.wolfia.utils.Operation;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.text.NumberFormat;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Created by napster on 21.05.17.
 * <p>
 * Useful methods for the Discord chat and general working with Strings and outputs
 */
public class TextchatUtils {

    public static final String ZERO_WIDTH_SPACE = "\u200B";

    public static final DateTimeFormatter TIME_IN_BERLIN = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss z")
            .withZone(ZoneId.of("Europe/Berlin"));

    public static final int MAX_MESSAGE_LENGTH = 2000;
    public static final List<String> TRUE_TEXT = Collections.unmodifiableList(Arrays.asList("true", "yes", "enable",
            "y", "on", "1", "positive", "+", "add", "start", "join", "ja"));
    public static final List<String> FALSE_TEXT = Collections.unmodifiableList(Arrays.asList("false", "no", "disable",
            "n", "off", "0", "negative", "-", "remove", "stop", "leave", "nein"));

    public static boolean isTrue(final String input) {
        return TRUE_TEXT.contains(input);
    }

    public static boolean isFalse(final String input) {
        return FALSE_TEXT.contains(input);
    }

    public static String userAsMention(final long userId) {
        return "<@" + userId + ">";
    }

    public static String formatMillis(final long millis) {
        return String.format("%02d:%02d:%02d",
                TimeUnit.MILLISECONDS.toHours(millis),
                TimeUnit.MILLISECONDS.toMinutes(millis) -
                        TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(millis)),
                TimeUnit.MILLISECONDS.toSeconds(millis) -
                        TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millis)));
    }

    //this will not always create a new invite, discord/JDA reuses previously created one
    //so no worries about spammed invites in a channel
    public static String getOrCreateInviteLinkForChannel(final TextChannel channel, final Operation... onFail) {
        try {
            return channel.createInvite().complete().getURL();
        } catch (final PermissionException ignored) {
            // ignored
        }
        try {
            final List<Invite> invites = channel.getInvites().complete();
            if (!invites.isEmpty()) return invites.get(0).getURL();
        } catch (final PermissionException ignored) {
            // ignored
        }

        // if we reached this point, we failed at creating an invite for this channel
        if (onFail.length > 0) {
            onFail[0].execute();
        }
        return "";
    }

    //a more aggressive variant of getOrCreateInviteLinkForChannel() which will try to create an invite anywhere into
    // a guild
    public static String getOrCreateInviteLinkForGuild(@Nonnull final Guild guild, @Nullable final TextChannel preferred,
                                                       final Operation... onFail) {
        if (preferred != null) {
            final String preferredInvite = getOrCreateInviteLinkForChannel(preferred);
            if (!preferredInvite.isEmpty()) return preferredInvite;
        }

        for (final TextChannel tc : guild.getTextChannels()) {
            final String invite = getOrCreateInviteLinkForChannel(tc);
            if (!invite.isEmpty()) return invite;
        }

        // if we reached this point, we failed at creating an invite for this guild
        if (onFail.length > 0) {
            onFail[0].execute();
        }
        return "";
    }

    public static String percentFormat(final double value) {
        final NumberFormat nf = NumberFormat.getPercentInstance();
        nf.setMaximumFractionDigits(2);
        return nf.format(value);
    }

    /**
     * @return performs a division; returns 0 if the divisor is 0
     */
    public static double divide(final long dividend, final long divisor) {
        if (divisor == 0) return 0;
        return 1.0 * dividend / divisor;
    }

    /**
     * Case insensitive
     * Useful for fuzzy matching of two strings
     * Source: https://rosettacode.org/wiki/Levenshtein_distance#Java
     * <p>
     * expected complexity: O(b + a*b)  (a and b = lengths of a and b)
     */
    public static int levenshteinDist(@Nonnull final String x, @Nonnull final String y) {
        final String a = x.toLowerCase();
        final String b = y.toLowerCase();
        final int[] costs = new int[b.length() + 1];
        for (int j = 0; j < costs.length; j++)
            costs[j] = j;
        for (int i = 1; i <= a.length(); i++) {
            costs[0] = i;
            int nw = i - 1;
            for (int j = 1; j <= b.length(); j++) {
                final int cj = Math.min(1 + Math.min(costs[j], costs[j - 1]), a.charAt(i - 1) == b.charAt(j - 1) ? nw : nw + 1);
                nw = costs[j];
                costs[j] = cj;
            }
        }
        return costs[b.length()];
    }

    /**
     * Runs the levenshtein distance algorithm over the provided strings
     *
     * @return returns true if the distance is equal or smaller than maxDist, false otherwise
     */
    public static boolean isSimilar(@Nonnull final String x, @Nonnull final String y, final int maxDistance) {
        return levenshteinDist(x, y) <= maxDistance;
    }

    /**
     * Same as {@link TextchatUtils#isSimilar(String, String, int)}, just with a default maxDistance of 3
     */
    public static boolean isSimilar(@Nonnull final String x, @Nonnull final String y) {
        return isSimilar(x, y, 3);
    }

    /**
     * Same as {@link TextchatUtils#isSimilar(String, String)}, but forces string to be lower case before comparing them
     */
    public static boolean isSimilarLower(@Nonnull final String x, @Nonnull final String y) {
        return isSimilar(x.toLowerCase(), y.toLowerCase());
    }

    //just kept around to eval-test the above levenshtein code
    public static String levenshteinTest() {
        final String[] data = {"kitten", "sitting", "saturday", "sunday", "rosettacode", "raisethysword"};
        final StringBuilder out = new StringBuilder();
        for (int i = 0; i < data.length; i += 2)
            out.append("\nlevenshteinDist(").append(data[i]).append(", ").append(data[i + 1]).append(") = ")
                    .append(levenshteinDist(data[i], data[i + 1]));
        return out.toString();
    }

    public static String asMarkdown(final String str) {
        return "```md\n" + str + "```";
    }

    public static String toUtcTime(final long epochMillis) {
        final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss z").withZone(ZoneId.of("UTC"));
        return dtf.format(Instant.ofEpochMilli(epochMillis));
    }

    public static String toBerlinTime(final long epochMillis) {
        return TIME_IN_BERLIN.format(Instant.ofEpochMilli(epochMillis));
    }

    public static String defuseMentions(final String input) {
        return input.replace("@", "@" + ZERO_WIDTH_SPACE);
    }


    private static final List<Character> MARKDOWN_CHARS = Arrays.asList('*', '`', '~', '_');

    //thanks fredboat
    @Nonnull
    public static String escapeMarkdown(@Nonnull final String str) {
        final StringBuilder revisedString = new StringBuilder(str.length());
        for (final Character n : str.toCharArray()) {
            if (MARKDOWN_CHARS.contains(n)) {
                revisedString.append("\\");
            }
            revisedString.append(n);
        }
        return revisedString.toString();
    }

    @Nonnull
    public static Message prefaceWithName(@Nonnull final User user, @Nonnull final String msg, final boolean escape) {
        String name = user.getName();
        if (escape) {
            name = escapeMarkdown(name);
        }
        return prefaceWithString(name, msg);
    }

    @Nonnull
    public static Message prefaceWithName(@Nonnull final Member member, @Nonnull final String msg, final boolean escape) {
        String name = member.getEffectiveName();
        if (escape) {
            name = escapeMarkdown(name);
        }
        return prefaceWithString(name, msg);
    }

    @Nonnull
    public static Message prefaceWithMention(@Nonnull final User user, @Nonnull final String msg) {
        return prefaceWithString(user.getAsMention(), msg);
    }

    //thanks fredboat
    @Nonnull
    private static Message prefaceWithString(@Nonnull final String preface, @Nonnull final String msg) {
        final String message = ensureSpace(msg);
        return new MessageBuilder()
                .append(preface)
                .append(",")
                .append(message)
                .build();
    }

    //thanks fredboat
    @Nonnull
    private static String ensureSpace(@Nonnull final String msg) {
        return msg.charAt(0) == ' ' ? msg : " " + msg;
    }

    private TextchatUtils() {}
}
