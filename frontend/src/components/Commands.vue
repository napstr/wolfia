<!--
  - Copyright (C) 2016-2020 the original author or authors
  -
  - This program is free software: you can redistribute it and/or modify
  - it under the terms of the GNU Affero General Public License as published
  - by the Free Software Foundation, either version 3 of the License, or
  - (at your option) any later version.
  -
  - This program is distributed in the hope that it will be useful,
  - but WITHOUT ANY WARRANTY; without even the implied warranty of
  - MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  - GNU Affero General Public License for more details.
  -
  - You should have received a copy of the GNU Affero General Public License
  - along with this program.  If not, see <http://www.gnu.org/licenses/>.
  -->

<template>
	<div class="Commands">
		<h1 class="title has-text-weight-light is-size-1-desktop">Wolfia Commands</h1>
		<div class="flex-container">
			<div>
				<h3 id="starting-a-game">Starting a game</h3>
				<table>
					<thead>
						<tr>
							<th>Command</th>
							<th>What it does</th>
							<th>Example usage / aliases</th>
						</tr>
					</thead>
					<tbody>
						<tr>
							<td><code>w.in</code></td>
							<td>sign up for a game</td>
							<td><code>w.in</code></td>
						</tr>
						<tr>
							<td><code>w.out</code></td>
							<td>drop from the sign up list; moderators can out other players</td>
							<td><code>w.out</code> <code>w.out @player</code></td>
						</tr>
						<tr>
							<td><code>w.setup</code></td>
							<td>set up the game in the current channel</td>
							<td>
								<code>w.setup daylength 5</code> <code>w.setup game mafia</code>
								<code>w.setup mode classic</code>
							</td>
						</tr>
						<tr>
							<td><code>w.start</code></td>
							<td>start a game</td>
							<td><code>w.start</code></td>
						</tr>
						<tr>
							<td><code>w.rolepm</code></td>
							<td>ask the bot to resend you your rolepm for the ongoing game</td>
							<td><code>w.rpm</code></td>
						</tr>
						<tr>
							<td><code>w.status</code></td>
							<td>show the current status of an ongoing game or sign ups</td>
							<td><code>w.st</code></td>
						</tr>
					</tbody>
				</table>
			</div>
			<div>
				<h3 id="game-actions">Game actions</h3>
				<table>
					<thead>
						<tr>
							<th>Command</th>
							<th>What it does</th>
							<th>Example usage / aliases</th>
						</tr>
					</thead>
					<tbody>
						<tr>
							<td><code>w.check</code></td>
							<td>check a players alignment</td>
							<td><code>w.check 6</code></td>
						</tr>
						<tr>
							<td><code>w.shoot</code></td>
							<td>shoot another player</td>
							<td><code>w.s @player</code></td>
						</tr>
						<tr>
							<td><code>w.unvote</code></td>
							<td>unvote</td>
							<td><code>w.u</code></td>
						</tr>
						<tr>
							<td><code>w.vote</code></td>
							<td>vote a player for lynch</td>
							<td><code>w.v @player</code></td>
						</tr>
						<tr>
							<td><code>w.votecount</code></td>
							<td>post the current votecount</td>
							<td><code>w.vc</code></td>
						</tr>
						<tr>
							<td><code>w.nightkill</code></td>
							<td>vote a player for nightkill (in mafia chat only)</td>
							<td><code>w.nk @player</code></td>
						</tr>
						<tr>
							<td><code>w.hohoho</code></td>
							<td>give a player a present (xmas mode only)</td>
							<td><code>w.h 5</code></td>
						</tr>
						<tr>
							<td><code>w.items</code></td>
							<td>list the items that you own (xmas mode only)</td>
							<td><code>w.items</code></td>
						</tr>
						<tr>
							<td><code>w.openpresent</code></td>
							<td>open a present that you received (xmas mode only)</td>
							<td><code>w.op</code></td>
						</tr>
					</tbody>
				</table>
			</div>
			<div>
				<h3 id="bot-settings">Bot Settings</h3>
				<table>
					<thead>
						<tr>
							<th>Command</th>
							<th>What it does</th>
							<th>Example usage / aliases</th>
						</tr>
					</thead>
					<tbody>
						<tr>
							<td><code>w.channelsettings</code></td>
							<td>set up settings for this channel</td>
							<td>
								<code>w.cs accessrole Mafiaplayer</code>, <code>w.channelsettings tagcooldown 10</code>,
								<code>w.cs auto-out on</code>
							</td>
						</tr>
					</tbody>
				</table>
			</div>
			<div>
				<h3 id="statistics">Statistics</h3>
				<table>
					<thead>
						<tr>
							<th>Command</th>
							<th>What it does</th>
							<th>Example usage / aliases</th>
						</tr>
					</thead>
					<tbody>
						<tr>
							<td><code>w.userstats</code></td>
							<td>show stats of a user</td>
							<td><code>w.userstats @user</code></td>
						</tr>
						<tr>
							<td><code>w.guildstats</code></td>
							<td>show stats of the current guild</td>
							<td><code>w.guildstats</code> <code>w.guildstats &lt;guildId&gt;</code></td>
						</tr>
						<tr>
							<td><code>w.botstats</code></td>
							<td>show bot wide stats</td>
							<td><code>w.botstats</code></td>
						</tr>
					</tbody>
				</table>
			</div>
			<div>
				<h3 id="other-commands">Other Commands</h3>
				<table>
					<thead>
						<tr>
							<th>Command</th>
							<th>What it does</th>
							<th>Example usage / aliases</th>
						</tr>
					</thead>
					<tbody>
						<tr>
							<td><code>w.auth</code></td>
							<td>authorize to be added to wolf chat automatically</td>
							<td><code>w.auth</code></td>
						</tr>
						<tr>
							<td><code>w.commands</code></td>
							<td>show a list of all available commands</td>
							<td><code>w.comms</code></td>
						</tr>
						<tr>
							<td><code>w.help</code></td>
							<td>send some help your way</td>
							<td><code>w.help</code></td>
						</tr>
						<tr>
							<td><code>w.info</code></td>
							<td>show some general information about Wolfia</td>
							<td><code>w.info</code></td>
						</tr>
						<tr>
							<td><code>w.invite</code></td>
							<td>post invite links for Wolfia and the Wolfia Lounge</td>
							<td><code>w.inv</code></td>
						</tr>
						<tr>
							<td><code>w.rank</code></td>
							<td>add or remove a rank in the WolfiaLounge</td>
							<td><code>w.rank AlphaWolves</code></td>
						</tr>
						<tr>
							<td><code>w.replay</code></td>
							<td>show the replay of a game</td>
							<td><code>w.replay #gameid</code></td>
						</tr>
						<tr>
							<td><code>w.tag</code></td>
							<td>post or sign up for the tag list of the channel</td>
							<td><code>w.tag +</code> <code>w.tag -</code> <code>w.tag + @role</code></td>
						</tr>
					</tbody>
				</table>
			</div>
		</div>
	</div>
</template>

<script>
export default {
	name: "Commands",
};
</script>

<style scoped lang="scss">
.Commands {
	display: flex;
	align-items: center;
	justify-content: center;
	height: 100%;
}
</style>
