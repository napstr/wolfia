package space.npstr.wolfia.utils.discord;

import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.MessageBuilder;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.MessageEmbed;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.exceptions.ErrorResponseException;
import net.dv8tion.jda.core.exceptions.InsufficientPermissionException;
import net.dv8tion.jda.core.requests.ErrorResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.function.Consumer;

/**
 * Created by napster on 10.09.17.
 * <p>
 * Everything related to sending RestActions
 * Copy pastad and adjusted from FredBoat where I wrote this for
 */
public class RestActions {

    private static final Logger log = LoggerFactory.getLogger(RestActions.class);

    //this is needed for when we absolutely don't care about a rest action failing (use this only after good consideration!)
    // because if we pass null for a failure handler to JDA it uses a default handler that results in a warning/error level log
    public static final Consumer<Throwable> NOOP_THROWABLE_HANDLER = __ -> {
    };

    //use this to schedule rest actions whenever queueAfter() or similar JDA methods would be used
    // this makes it way easier to track stats + handle failures of such delayed RestActions
    // instead of implementing a ton of overloaded methods in this class
    public static final ScheduledExecutorService restService = Executors.newScheduledThreadPool(10,
            runnable -> new Thread(runnable, "central-messaging-scheduler"));


    // ********************************************************************************
    //       Thread local handling and providing of Messages and Embeds builders
    // ********************************************************************************

    //instead of creating hundreds of MessageBuilder and EmbedBuilder objects we're going to reuse the existing ones, on
    // a per-thread scope
    // this makes sense since the vast majority of message processing in the main JDA threads

//    private static ThreadLocal<MessageBuilder> threadLocalMessageBuilder = ThreadLocal.withInitial(MessageBuilder::new);
//    private static ThreadLocal<EmbedBuilder> threadLocalEmbedBuilder = ThreadLocal.withInitial(EmbedBuilder::new);

    //todo reported to JDA (jagrosh), check back for a fix
    @Nonnull
    public static MessageBuilder getClearThreadLocalMessageBuilder() {
        return new MessageBuilder();
//        return threadLocalMessageBuilder.get().clear();
    }

    //NOTE: these seem borked, they appear to be overwritten when used with a delay, due to a collection of fields
    // shared with the builder and the embed objects it builds
    @Nonnull
    public static EmbedBuilder getClearThreadLocalEmbedBuilder() {
        return new EmbedBuilder();
//        return threadLocalEmbedBuilder.get()
//                .clearFields()
//                .setTitle(null)
//                .setDescription(null)
//                .setTimestamp(null)
//                .setColor(null)
//                .setThumbnail(null)
//                .setAuthor(null, null, null)
//                .setFooter(null, null)
//                .setImage(null);
    }

    //May not be an empty string, as MessageBuilder#build() will throw an exception
    @Nonnull
    public static Message from(final String string) {
        return getClearThreadLocalMessageBuilder().append(string).build();
    }

    @Nonnull
    public static Message from(final MessageEmbed embed) {
        return getClearThreadLocalMessageBuilder().setEmbed(embed).build();
    }


    // ********************************************************************************
    //       Convenience methods that convert input to Message objects and send it
    // ********************************************************************************

    /**
     * @param channel   The channel that should be messaged
     * @param message   Message to be sent
     * @param onSuccess Optional success handler
     * @param onFail    Optional exception handler
     */
    public static void sendMessage(@Nonnull final MessageChannel channel, @Nonnull final Message message,
                                   @Nullable final Consumer<Message> onSuccess, @Nullable final Consumer<Throwable> onFail) {
        sendMessage0(
                channel,
                message,
                onSuccess,
                onFail
        );
    }

    // Message
    public static void sendMessage(@Nonnull final MessageChannel channel, @Nonnull final Message message,
                                   @Nullable final Consumer<Message> onSuccess) {
        sendMessage0(
                channel,
                message,
                onSuccess,
                null
        );
    }

    // Message
    public static void sendMessage(@Nonnull final MessageChannel channel, @Nonnull final Message message) {
        sendMessage0(
                channel,
                message,
                null,
                null
        );
    }

    // Embed
    public static void sendMessage(@Nonnull final MessageChannel channel, @Nonnull final MessageEmbed embed,
                                   @Nullable final Consumer<Message> onSuccess, @Nullable final Consumer<Throwable> onFail) {
        sendMessage0(
                channel,
                from(embed),
                onSuccess,
                onFail
        );
    }

    // Embed
    public static void sendMessage(@Nonnull final MessageChannel channel, @Nonnull final MessageEmbed embed,
                                   @Nullable final Consumer<Message> onSuccess) {
        sendMessage0(
                channel,
                from(embed),
                onSuccess,
                null
        );
    }

    // Embed
    public static void sendMessage(@Nonnull final MessageChannel channel, @Nonnull final MessageEmbed embed) {
        sendMessage0(
                channel,
                from(embed),
                null,
                null
        );
    }

    // String
    public static void sendMessage(@Nonnull final MessageChannel channel, @Nonnull final String content,
                                   @Nullable final Consumer<Message> onSuccess, @Nullable final Consumer<Throwable> onFail) {
        sendMessage0(
                channel,
                from(content),
                onSuccess,
                onFail
        );
    }

    // String
    public static void sendMessage(@Nonnull final MessageChannel channel, @Nonnull final String content,
                                   @Nullable final Consumer<Message> onSuccess) {
        sendMessage0(
                channel,
                from(content),
                onSuccess,
                null
        );
    }

    // String
    public static void sendMessage(@Nonnull final MessageChannel channel, @Nonnull final String content) {
        sendMessage0(
                channel,
                from(content),
                null,
                null
        );
    }

    // private
    public static void sendPrivateMessage(@Nonnull final User user, @Nonnull final MessageEmbed embed,
                                          @Nullable final Consumer<Message> onSuccess, @Nonnull final Consumer<Throwable> onFail) {
        sendPrivateMessage(user, from(embed), onSuccess, onFail);
    }

    // private
    public static void sendPrivateMessage(@Nonnull final User user, @Nonnull final String content,
                                          @Nullable final Consumer<Message> onSuccess, @Nonnull final Consumer<Throwable> onFail) {
        sendPrivateMessage(user, from(content), onSuccess, onFail);
    }

    // private
    // in Wolfia, it is very important that messages reach their destination, that's why private messages require a failure
    // handler, so that each time a private message is coded a conscious decision is made how a failure should be handled
    public static void sendPrivateMessage(@Nonnull final User user, @Nonnull final Message message,
                                          @Nullable final Consumer<Message> onSuccess, @Nonnull final Consumer<Throwable> onFail) {
        user.openPrivateChannel().queue(
                privateChannel -> {
//                    Metrics.successfulRestActions.labels("openPrivateChannel").inc();
                    sendMessage(privateChannel, message, onSuccess, onFail);

                },
                onFail
        );
    }

//    // ********************************************************************************
//    //                            File sending methods
//    // ********************************************************************************
//
//    /**
//     * @param channel   The channel that should be messaged
//     * @param file      File to be sent
//     * @param message   Optional message
//     * @param onSuccess Optional success handler
//     * @param onFail    Optional exception handler
//     */
//    public static void sendFile(@Nonnull MessageChannel channel, @Nonnull File file, @Nullable Message message,
//                                @Nullable Consumer<Message> onSuccess, @Nullable Consumer<Throwable> onFail) {
//        sendFile0(
//                channel,
//                file,
//                message,
//                onSuccess,
//                onFail
//        );
//    }
//
//    public static void sendFile(@Nonnull MessageChannel channel, @Nonnull File file, @Nullable Message message,
//                                @Nullable Consumer<Message> onSuccess) {
//        sendFile0(
//                channel,
//                file,
//                message,
//                onSuccess,
//                null
//        );
//    }
//
//    public static void sendFile(@Nonnull MessageChannel channel, @Nonnull File file, @Nullable Message message) {
//        sendFile0(
//                channel,
//                file,
//                message,
//                null,
//                null
//        );
//    }
//
//    public static void sendFile(@Nonnull MessageChannel channel, @Nonnull File file,
//                                @Nullable Consumer<Message> onSuccess, @Nullable Consumer<Throwable> onFail) {
//        sendFile0(
//                channel,
//                file,
//                null,
//                onSuccess,
//                onFail
//        );
//    }
//
//    public static void sendFile(@Nonnull MessageChannel channel, @Nonnull File file,
//                                @Nullable Consumer<Message> onSuccess) {
//        sendFile0(
//                channel,
//                file,
//                null,
//                onSuccess,
//                null
//        );
//    }
//
//    public static void sendFile(@Nonnull MessageChannel channel, @Nonnull File file) {
//        sendFile0(
//                channel,
//                file,
//                null,
//                null,
//                null
//        );
//    }


    // ********************************************************************************
    //                            Message editing methods
    // ********************************************************************************

    /**
     * @param oldMessage The message to be edited
     * @param newMessage The message to be set
     * @param onSuccess  Optional success handler
     * @param onFail     Optional exception handler
     */
    public static void editMessage(@Nonnull final Message oldMessage, @Nonnull final Message newMessage,
                                   @Nullable final Consumer<Message> onSuccess, @Nullable final Consumer<Throwable> onFail) {
        editMessage0(
                oldMessage.getChannel(),
                oldMessage.getIdLong(),
                newMessage,
                onSuccess,
                onFail
        );
    }

    public static void editMessage(@Nonnull final Message oldMessage, @Nonnull final Message newMessage) {
        editMessage0(
                oldMessage.getChannel(),
                oldMessage.getIdLong(),
                newMessage,
                null,
                null
        );
    }

    public static void editMessage(@Nonnull final Message oldMessage, @Nonnull final String newContent) {
        editMessage0(
                oldMessage.getChannel(),
                oldMessage.getIdLong(),
                from(newContent),
                null,
                null
        );
    }


    public static void editMessage(@Nonnull final MessageChannel channel, final long oldMessageId, @Nonnull final Message newMessage,
                                   @Nullable final Consumer<Message> onSuccess, @Nullable final Consumer<Throwable> onFail) {
        editMessage0(
                channel,
                oldMessageId,
                newMessage,
                onSuccess,
                onFail
        );
    }

    public static void editMessage(@Nonnull final MessageChannel channel, final long oldMessageId, @Nonnull final Message newMessage) {
        editMessage0(
                channel,
                oldMessageId,
                newMessage,
                null,
                null
        );
    }

    public static void editMessage(@Nonnull final MessageChannel channel, final long oldMessageId, @Nonnull final String newContent) {
        editMessage0(
                channel,
                oldMessageId,
                from(newContent),
                null,
                null
        );
    }

    // ********************************************************************************
    //                   Miscellaneous messaging related methods
    // ********************************************************************************

    public static void sendTyping(@Nonnull final MessageChannel channel) {
        try {
            channel.sendTyping().queue(
//                    __ -> Metrics.successfulRestActions.labels("sendTyping").inc(),
                    null,
                    getJdaRestActionFailureHandler("Could not send typing event in channel " + channel.getId())
            );
        } catch (final InsufficientPermissionException e) {
            handleInsufficientPermissionsException(channel, e);
        }
    }

    //make sure that all the messages are from the channel you provide
    public static void deleteMessages(@Nonnull final TextChannel channel, @Nonnull final Collection<Message> messages) {
        if (!messages.isEmpty()) {
            try {
                channel.deleteMessages(messages).queue(
//                        __ -> Metrics.successfulRestActions.labels("bulkDeleteMessages").inc(),
                        null,
                        getJdaRestActionFailureHandler(String.format("Could not bulk delete %s messages in channel %s",
                                messages.size(), channel.getId()))
                );
            } catch (final InsufficientPermissionException e) {
                handleInsufficientPermissionsException(channel, e);
            }
        }
    }

    public static void deleteMessageById(@Nonnull final MessageChannel channel, final long messageId) {
        try {
            channel.getMessageById(messageId).queue(
                    message -> {
//                        Metrics.successfulRestActions.labels("getMessageById").inc();
                        deleteMessage(message);
                    },
                    NOOP_THROWABLE_HANDLER //prevent logging an error if that message could not be found in the first place
            );
        } catch (final InsufficientPermissionException e) {
            handleInsufficientPermissionsException(channel, e);
        }
    }

    //make sure that the message passed in here is actually existing in Discord
    // e.g. dont pass messages in here that were created with a MessageBuilder in our code
    public static void deleteMessage(@Nonnull final Message message) {
        try {
            message.delete().queue(
//                    __ -> Metrics.successfulRestActions.labels("deleteMessage").inc(),
                    null,
                    getJdaRestActionFailureHandler(String.format("Could not delete message %s in channel %s with content\n%s",
                            message.getId(), message.getChannel().getId(), message.getRawContent()),
                            ErrorResponse.UNKNOWN_MESSAGE) //user deleted their message, dun care
            );
        } catch (final InsufficientPermissionException e) {
            handleInsufficientPermissionsException(message.getChannel(), e);
        }
    }

    @Nonnull
    public static EmbedBuilder addFooter(@Nonnull final EmbedBuilder eb, @Nonnull final Member author) {
        return eb.setFooter(author.getEffectiveName(), author.getUser().getAvatarUrl());
    }

    // ********************************************************************************
    //                           Class internal methods
    // ********************************************************************************

    //class internal message sending method
    private static void sendMessage0(@Nonnull final MessageChannel channel, @Nonnull final Message message,
                                     @Nullable final Consumer<Message> onSuccess, @Nullable final Consumer<Throwable> onFail) {
        final Consumer<Message> successWrapper = m -> {
//            Metrics.successfulRestActions.labels("sendMessage").inc();
            if (onSuccess != null) {
                onSuccess.accept(m);
            }
        };
        final Consumer<Throwable> failureWrapper = t -> {
            if (onFail != null) {
                onFail.accept(t);
            } else {
                final String info = String.format("Could not sent message\n%s\nwith %s embeds to channel %s in guild %s",
                        message.getRawContent(), message.getEmbeds().size(), channel.getId(),
                        (channel instanceof TextChannel) ? ((TextChannel) channel).getGuild().getIdLong() : "private");
                getJdaRestActionFailureHandler(info).accept(t);
            }
        };

        try {
            channel.sendMessage(message).queue(successWrapper, failureWrapper);
        } catch (final InsufficientPermissionException e) {
            if (onFail != null) {
                onFail.accept(e);
            }
            if (e.getPermission() == Permission.MESSAGE_EMBED_LINKS) {
                handleInsufficientPermissionsException(channel, e);
            } else {
                //do not call RestActions#handleInsufficientPermissionsException() from here as that will result in a loop
                log.warn("Could not send message with content {} and {} embeds to channel {} due to missing permission {}",
                        message.getRawContent(), message.getEmbeds().size(), channel.getIdLong(), e.getPermission().getName(), e);
            }
        }
    }

    //commented out until message-rw is fixed, as we dont use send files anyways right now
//    //class internal file sending method
//    private static void sendFile0(@Nonnull MessageChannel channel, @Nonnull File file, @Nullable Message message,
//                                  @Nullable Consumer<Message> onSuccess, @Nullable Consumer<Throwable> onFail) {
//        Consumer<Message> successWrapper = m -> {
//            Metrics.successfulRestActions.labels("sendFile").inc();
//            if (onSuccess != null) {
//                onSuccess.accept(m);
//            }
//        };
//        Consumer<Throwable> failureWrapper = t -> {
//            if (onFail != null) {
//                onFail.accept(t);
//            } else {
//                String info = String.format("Could not send file %s to channel %s in guild %s",
//                        file.getAbsolutePath(), channel.getId(),
//                        (channel instanceof TextChannel) ? ((TextChannel) channel).getGuild().getIdLong() : "null");
//                getJdaRestActionFailureHandler(info).accept(t);
//            }
//        };
//
//        try {
//            // ATTENTION: Do not use JDA's MessageChannel#sendFile(File file, Message message)
//            // as it will skip permission checks, since TextChannel does not override that method
//            // this is scheduled to be fixed through JDA's message-rw branch
//            channel.sendFile(FileUtils.readFileToByteArray(file), file.getName(), message).queue(successWrapper, failureWrapper);
//        } catch (InsufficientPermissionException e) {
//            if (onFail != null) {
//                onFail.accept(e);
//            }
//            handleInsufficientPermissionsException(channel, e);
//        } catch (IOException e) {
//            log.error("Could not send file {}, it appears to be borked", file.getAbsolutePath(), e);
//        }
//    }

    //class internal editing method
    private static void editMessage0(@Nonnull final MessageChannel channel, final long oldMessageId, @Nonnull final Message newMessage,
                                     @Nullable final Consumer<Message> onSuccess, @Nullable final Consumer<Throwable> onFail) {
        final Consumer<Message> successWrapper = m -> {
//            Metrics.successfulRestActions.labels("editMessage").inc();
            if (onSuccess != null) {
                onSuccess.accept(m);
            }
        };
        final Consumer<Throwable> failureWrapper = t -> {
            if (onFail != null) {
                onFail.accept(t);
            } else {
                final String info = String.format("Could not edit message %s in channel %s in guild %s with new content %s and %s embeds",
                        oldMessageId, channel.getId(),
                        (channel instanceof TextChannel) ? ((TextChannel) channel).getGuild().getIdLong() : "null",
                        newMessage.getRawContent(), newMessage.getEmbeds().size());
                getJdaRestActionFailureHandler(info).accept(t);
            }
        };

        try {
            channel.editMessageById(oldMessageId, newMessage).queue(successWrapper, failureWrapper);
        } catch (final InsufficientPermissionException e) {
            if (onFail != null) {
                onFail.accept(e);
            }
            handleInsufficientPermissionsException(channel, e);
        }
    }

    private static void handleInsufficientPermissionsException(@Nonnull final MessageChannel channel,
                                                               @Nonnull final InsufficientPermissionException e) {
        //only ever try sending a simple string from here so we don't end up handling a loop of insufficient permissions
        sendMessage(channel, "Please give me the permission to " + " **" + e.getPermission().getName() + "!**");
    }


    //handles failed JDA rest actions by logging them with an informational string and optionally ignoring some error response codes
    //todo unite with Wolfia.defaultOnFail
    public static Consumer<Throwable> getJdaRestActionFailureHandler(final String info, final ErrorResponse... ignored) {
        return t -> {
            if (t instanceof ErrorResponseException) {
                final ErrorResponseException e = (ErrorResponseException) t;
//                Metrics.failedRestActions.labels(Integer.toString(e.getErrorCode())).inc();
                if (Arrays.asList(ignored).contains(e.getErrorResponse())
                        || e.getErrorCode() == -1 //socket timeout, fuck those
                        || e.getErrorCode() == ErrorResponse.MISSING_ACCESS.getCode() //how even? means we're not in the server (anymore) ? definitely dont need to log this
                        ) {
                    return;
                }
            }
            log.error(info, t);
        };
    }
}
