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
    private static final Logger logger = LoggerFactory.getLogger(TechSupportOllamaBot.class);
    private final static String DISCORD_BOT_TOKEN = Utils.getRequiredEnv("DISCORD_BOT_TOKEN");
    static String channelToWatch = "q-and-a";

    static String contentsFromFAQ = "";
    static String pathToFAQFile = "/ch06/ollama/FAQ.txt";

    // Track users who have already received New Year greetings
    private static final Set<String> greetedUsers = ConcurrentHashMap.newKeySet();

    // New Year greeting messages
    private static final String NEW_YEAR_GREETING = "🎄 **Happy upcoming New Year! May this year bring you joy, success, and fulfillment of all your wishes!** 🎄";
    private static final String NEW_YEAR_REMINDER = "✨ **Don't forget, the New Year is coming soon — a time of miracles and new beginnings!** ✨";


    static String systemMessage = "You are a virtual Santa Claus who grants wishes. You are a magical team of fairy-tale characters who together act as the Wish-Granting Helpers. You respond to children's (and adults') wishes in a warm, kind, and enchanting way. The team consists of:\n" +
            "\n" +
            "Buratino (the cheerful wooden boy full of curiosity and energy)\n" +
            "The Ugly Duckling (now a beautiful Swan, gentle, empathetic, and wise)\n" +
            "The Snow Queen (elegant, calm, and powerful, but with a kind heart)\n" +
            "The Blue Fairy (gracious and magical, Pinocchio's wise friend)\n" +
            "The Little Mermaid (dreamy and compassionate)\n" +
            "Alice from Wonderland (curious, brave, and full of wonder, always ready for extraordinary adventures).\n" +
            "\n" +
            "Important: Do not start your responses with greetings like 'Dear friend,' or 'Hello dear friend,'. Instead, start directly with the response content.\n" +
            "You always respond in a playful, fairy-tale style, staying in character. When answering, one or more characters can speak, and you can switch between them naturally (for example: Alice smiles curiously: \"Oh, what an interesting wish!\" or The Swan gently nods: \"We'll make it come true...\").";
    static OllamaClientForQA ollamaClient = null;

    public static void main(String[] args) throws IOException {

        EnumSet<GatewayIntent> intents = EnumSet.of(
                GatewayIntent.GUILD_MESSAGES,
                GatewayIntent.DIRECT_MESSAGES,
                GatewayIntent.MESSAGE_CONTENT
        );

        // Read FAQ file
        contentsFromFAQ = readFileContents(pathToFAQFile);

        // Initialize Ollama client
        ollamaClient = new OllamaClientForQA(systemMessage, contentsFromFAQ);

        try {
            JDA jda = JDABuilder.createLight(DISCORD_BOT_TOKEN, intents)
                    .addEventListeners(new TechSupportOllamaBot())
                    .setActivity(Activity.playing("Ready to answer questions"))
                    .build();

            jda.getRestPing().queue(ping -> System.out.println("Logged in with ping: " + ping));
            jda.awaitReady();

            System.out.println("Guilds: " + jda.getGuildCache().size());
            System.out.println("Self user: " + jda.getSelfUser());
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

        try {
            String ollamaReply = ollamaClient.sendMessage(event.getMessage().getContentDisplay());
            String baseReply = String.format("Dear <@%s>, ", sender.getId());

            String newYearAddition;
            boolean isFirstMessage = !greetedUsers.contains(sender.getId());

            if (isFirstMessage) {
                newYearAddition = " " + NEW_YEAR_GREETING;
                greetedUsers.add(sender.getId());
            } else {
                newYearAddition = " " + NEW_YEAR_REMINDER;
            }

            String finalReply = baseReply + newYearAddition + "\n\n" + ollamaReply;
            channel.sendMessage(finalReply).queue();

        } catch (Exception e) {
            logger.error("Error processing message", e);
            String errorReply = String.format("Dear <@%s>, I apologize, but I encountered an error. %s",
                    sender.getId(), NEW_YEAR_REMINDER);
            channel.sendMessage(errorReply).queue();
        }
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
