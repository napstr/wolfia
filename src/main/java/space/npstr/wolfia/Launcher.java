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

import net.dv8tion.jda.bot.sharding.ShardManager;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.JDAInfo;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.flyway.FlywayAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceTransactionManagerAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.context.event.ApplicationEnvironmentPreparedEvent;
import org.springframework.boot.context.event.ApplicationFailedEvent;
import space.npstr.prometheus_extensions.ThreadPoolCollector;
import space.npstr.wolfia.commands.debug.SyncCommand;
import space.npstr.wolfia.config.properties.WolfiaConfig;
import space.npstr.wolfia.db.Database;
import space.npstr.wolfia.game.tools.ExceptionLoggingExecutor;
import space.npstr.wolfia.utils.GitRepoState;
import space.npstr.wolfia.utils.discord.RestActions;
import space.npstr.wolfia.utils.discord.TextchatUtils;

import javax.annotation.Nonnull;
import java.util.concurrent.ScheduledThreadPoolExecutor;

/**
 * Created by napster on 10.05.18.
 *
 *  //general list of todos etc
 *  //todo rename role pm/dm -> rolecard
 */
@SpringBootApplication(exclude = { //we handle these ourselves
        DataSourceAutoConfiguration.class,
        DataSourceTransactionManagerAutoConfiguration.class,
        HibernateJpaAutoConfiguration.class,
        FlywayAutoConfiguration.class
})
public class Launcher implements ApplicationRunner {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(Launcher.class);

    @SuppressWarnings("NullableProblems")
    private static BotContext botContext;

    private final ShardManager shardManager;
    private final ThreadPoolCollector poolMetrics;
    private final ExceptionLoggingExecutor executor;
    private final WolfiaConfig wolfiaConfig;
    private final Database database;

    public static BotContext getBotContext() {
        return botContext;
    }

    @SuppressWarnings("squid:S106") // printing to sout is fine here
    public static void main(String[] args) {
        //just post the info to the console
        if (args.length > 0 &&
                (args[0].equalsIgnoreCase("-v")
                        || args[0].equalsIgnoreCase("--version")
                        || args[0].equalsIgnoreCase("-version"))) {
            System.out.println("Version flag detected. Printing version info, then exiting.");
            System.out.println(getVersionInfo());
            System.out.println("Version info printed, exiting.");
            return;
        }

        log.info(getVersionInfo());

        System.setProperty("spring.config.name", "wolfia");
        SpringApplication app = new SpringApplication(Launcher.class);
        app.addListeners(event -> {
            if (event instanceof ApplicationEnvironmentPreparedEvent) {
                log.info(getVersionInfo());
            }
            if (event instanceof ApplicationFailedEvent) {
                final ApplicationFailedEvent failed = (ApplicationFailedEvent) event;
                log.error("Application failed", failed.getException());
                System.exit(2);
            }
        });
        app.run(args);
    }

    public Launcher(final BotContext botContext, final ShardManager shardManager,
                    final ThreadPoolCollector poolMetrics, final ExceptionLoggingExecutor executor,
                    final WolfiaConfig wolfiaConfig, Database database) {

        Launcher.botContext = botContext;
        this.shardManager = shardManager;
        this.poolMetrics = poolMetrics;
        this.executor = executor;
        this.wolfiaConfig = wolfiaConfig;
        this.database = database;
    }

    @Override
    public void run(final ApplicationArguments args) throws Exception {

        this.poolMetrics.addPool("restActions", (ScheduledThreadPoolExecutor) RestActions.restService);

        if (this.wolfiaConfig.isDebug())
            log.info("Running DEBUG configuration");
        else
            log.info("Running PRODUCTION configuration");

        //try connecting in a reasonable timeframe
        boolean dbConnected = false;
        final long dbConnectStarted = System.currentTimeMillis();
        do {
            try {
                //noinspection ResultOfMethodCallIgnored
                this.database.getWrapper().selectSqlQuery("SELECT 1;", null);
                dbConnected = true;
                log.info("Initial db connection succeeded");
            } catch (final Exception e) {
                log.info("Failed initial db connection, retrying in a moment", e);
                Thread.sleep(1000);
            }
        } while (!dbConnected && System.currentTimeMillis() - dbConnectStarted < 1000 * 60 * 2); //2 minutes

        if (!dbConnected) {
            log.error("Failed to init db connection in a reasonable amount of time, exiting.");
            System.exit(2);
        }

        //wait for all shards to be online, then start doing things that expect the full bot to be online
        while (!allShardsUp()) {
            Thread.sleep(1000);
        }

        //sync guild cache
        // this takes a few seconds to do, so do it as the last thing of the main method, or put it into it's own thread
        SyncCommand.syncGuilds(this.shardManager, this.executor, this.shardManager.getGuildCache().stream(), null);
        //user cache is not synced on each start as it takes a lot of time and resources. see SyncComm for manual triggering
    }

    private boolean allShardsUp() {
        if (this.shardManager.getShards().size() < this.shardManager.getShardsTotal()) {
            return false;
        }
        for (final JDA jda : this.shardManager.getShards()) {
            if (jda.getStatus() != JDA.Status.CONNECTED) {
                return false;
            }
        }
        return true;
    }

    @Nonnull
    private static String getVersionInfo() {
        return ART
                + "\n"
                + "\n\tVersion:       " + App.VERSION
                + "\n\tBuild:         " + App.BUILD_NUMBER
                + "\n\tBuild time:    " + TextchatUtils.toBerlinTime(App.BUILD_TIME)
                + "\n\tCommit:        " + GitRepoState.getGitRepositoryState().commitIdAbbrev + " (" + GitRepoState.getGitRepositoryState().branch + ")"
                + "\n\tCommit time:   " + TextchatUtils.toBerlinTime(GitRepoState.getGitRepositoryState().commitTime * 1000)
                + "\n\tJVM:           " + System.getProperty("java.version")
                + "\n\tJDA:           " + JDAInfo.VERSION
                + "\n";
    }

    //########## vanity
    private static final String ART = "\n"
            + "\n                              __"
            + "\n                            .d$$b"
            + "\n                           .' TO$;\\"
            + "\n        Wolfia            /  : TP._;"
            + "\n    Werewolf & Mafia     / _.;  :Tb|"
            + "\n      Discord bot       /   /   ;j$j"
            + "\n                    _.-\"       d$$$$"
            + "\n                  .' ..       d$$$$;"
            + "\n                 /  /P'      d$$$$P. |\\"
            + "\n                /   \"      .d$$$P' |\\^\"l"
            + "\n              .'           `T$P^\"\"\"\"\"  :"
            + "\n          ._.'      _.'                ;"
            + "\n       `-.-\".-'-' ._.       _.-\"    .-\""
            + "\n     `.-\" _____  ._              .-\""
            + "\n    -(.g$$$$$$$b.              .'"
            + "\n      \"\"^^T$$$P^)            .(:"
            + "\n        _/  -\"  /.'         /:/;"
            + "\n     ._.'-'`-'  \")/         /;/;"
            + "\n  `-.-\"..--\"\"   \" /         /  ;"
            + "\n .-\" ..--\"\"        -'          :"
            + "\n ..--\"\"--.-\"         (\\      .-(\\"
            + "\n   ..--\"\"              `-\\(\\/;`"
            + "\n     _.                      :"
            + "\n                             ;`-"
            + "\n                            :\\"
            + "\n                            ;";
}
