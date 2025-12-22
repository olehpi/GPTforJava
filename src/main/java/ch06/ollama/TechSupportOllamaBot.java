package ch06.ollama;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.EnumSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import ch03.Utils;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.entities.channel.unions.MessageChannelUnion;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.GatewayIntent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TechSupportOllamaBot extends ListenerAdapter {
    static ModerationOllamaClient moderationClient = new ModerationOllamaClient();
    static OllamaClientForQA ollamaClient = null;
    private static final Logger logger = LoggerFactory.getLogger(TechSupportOllamaBot.class);
    private final static String DISCORD_BOT_TOKEN = Utils.getRequiredEnv("DISCORD_BOT_TOKEN");
    static String channelToWatch = "q-and-a";

    static String contentsFromFAQ = "";
    static String pathToFAQFile = "/ch06/ollama/FAQ.txt";

    // Track users who have already received New Year greetings
    private static final Set<String> greetedUsers = ConcurrentHashMap.newKeySet();

    public static void main(String[] args) throws IOException {

        EnumSet<GatewayIntent> intents = EnumSet.of(
                GatewayIntent.GUILD_MESSAGES,
                GatewayIntent.DIRECT_MESSAGES,
                GatewayIntent.MESSAGE_CONTENT
        );

        // Read FAQ file
        contentsFromFAQ = readFileContents(pathToFAQFile);

        // Initialize Ollama client
        ollamaClient = new OllamaClientForQA(Constants.TechSupport.SYSTEM_MESSAGE, contentsFromFAQ);

        try {
            JDA jda = JDABuilder.createLight(DISCORD_BOT_TOKEN, intents)
                    .addEventListeners(new TechSupportOllamaBot())
                    .setActivity(Activity.playing("Ready to answer questions"))
                    .build();

            jda.getRestPing().queue(ping -> logger.info("Logged in with ping: " + ping));
            jda.awaitReady();

            logger.info("Guilds: " + jda.getGuildCache().size());
            logger.info("Self user: " + jda.getSelfUser());
        } catch (InterruptedException e) {
            logger.error("Bot startup was interrupted", e);
            Thread.currentThread().interrupt();
        }
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        User sender = event.getAuthor();
        MessageChannelUnion channel = event.getChannel();

        if (sender.equals(event.getJDA().getSelfUser())) return;
        if (event.getChannelType() == ChannelType.TEXT && !channel.getName().equalsIgnoreCase(channelToWatch)) return;

        var originalMessage = event.getMessage().getContentDisplay();
        if (moderate_message(event, originalMessage, channel, sender)) return;

        try {
            String ollamaReply = ollamaClient.sendMessage(originalMessage);
            String baseReply = String.format("Dear <@%s>, ", sender.getId());

            String newYearAddition;
            boolean isFirstMessage = !greetedUsers.contains(sender.getId());

            if (isFirstMessage) {
                newYearAddition = " " + Constants.NEW_YEAR_GREETING;
                greetedUsers.add(sender.getId());
            } else {
                newYearAddition = " " + Constants.NEW_YEAR_REMINDER;
            }

            String finalReply = baseReply + newYearAddition + "\n\n" + ollamaReply;
            channel.sendMessage(finalReply).queue();

        } catch (Exception e) {
            logger.error("Error processing message", e);
            String errorReply = String.format("Dear <@%s>, I apologize, but I encountered an error. %s",
                    sender.getId(), Constants.NEW_YEAR_REMINDER);
            channel.sendMessage(errorReply).queue();
        }
    }

    private static boolean moderate_message(MessageReceivedEvent event, String originalMessage, MessageChannelUnion channel, User sender) {
        boolean flagged = moderationClient.isFlagged(originalMessage);
        if (flagged) {
            event.getMessage().delete().queue();
            channel.sendMessage(sender.getAsMention() + Constants.Moderator.VIOLATE_MESSAGE).queue();
            return true;
        }
        return false;
    }

    private static String readFileContents(String resourcePath) {
        try (InputStream is = TechSupportOllamaBot.class.getResourceAsStream(resourcePath);
             BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append("\n");
            }
            return sb.toString();
        } catch (IOException e) {
            logger.error("Error reading FAQ resource: {}", resourcePath, e);
            return "Failed to read FAQ contents.";
        }
    }
}


// go ch06.ollama.OllamaClientForQA
// go ch06.ollama.TechSupportOllamaBot
