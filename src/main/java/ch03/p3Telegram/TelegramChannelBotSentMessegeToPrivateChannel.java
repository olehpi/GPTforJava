package ch03.p3Telegram;

import ch03.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient;
import org.telegram.telegrambots.longpolling.TelegramBotsLongPollingApplication;
import org.telegram.telegrambots.longpolling.util.LongPollingSingleThreadUpdateConsumer;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.util.Timer;
import java.util.TimerTask;

public class TelegramChannelBotSentMessegeToPrivateChannel implements LongPollingSingleThreadUpdateConsumer {

	private static final Logger log = LoggerFactory.getLogger(TelegramChannelBotSentMessegeToPrivateChannel.class);
	// Secrets are now loaded from environment variables – safe for GitHub!
	private static final String BOT_TOKEN = Utils.getRequiredEnv("TELEGRAM_BOT_TOKEN");
	private static final String CHANNEL_ID = Utils.getRequiredEnv("TELEGRAM_CHANNEL_ID");

	private final TelegramClient client;

	public TelegramChannelBotSentMessegeToPrivateChannel() {
		this.client = new OkHttpTelegramClient(BOT_TOKEN);
	}

	/**
	 * Sends a message to the configured private Telegram channel.
	 * @param text the text to send
	 */
	public void sendMessageToChannel(String text) {
		SendMessage msg = SendMessage.builder()
				.chatId(CHANNEL_ID)
				.text(text)
				.build();
		try {
			client.execute(msg);
			log.info("Message successfully sent to channel {} → {}", CHANNEL_ID, text);
		} catch (TelegramApiException e) {
			log.error("Failed to send message to channel {}: {} ",
					CHANNEL_ID, e.getMessage(), e);
		} catch (Exception e) {
			log.error("Unexpected error while sending message to channel {}", CHANNEL_ID, e);
		}
	}

	@Override
	public void consume(Update update) {
		if (update.getMessage() != null && update.getMessage().hasText()) {
			long userChatId = update.getMessage().getChatId();
			String userText = update.getMessage().getText();

			System.out.println("User ChatId: " + userChatId);
			System.out.println("User text: " + userText);

			// Answer to User
			SendMessage reply = SendMessage.builder()
					.chatId(String.valueOf(userChatId))
					.text("User wrote: " + userText)
					.build();
			try {
				client.execute(reply);
			} catch (Exception e) {
				log.error("Unexpected error while replying to user {}", userChatId, e);
			}

			// send the massege into private channel
			SendMessage channelMsg = SendMessage.builder()
					.chatId(CHANNEL_ID)
					.text("Message from User: " + userText)
					.build();
			try {
				client.execute(channelMsg);
			} catch (Exception e) {
				log.error("Unexpected error while forwarding to channel {}", CHANNEL_ID, e);
			}
		}
	}

	public static void main(String[] args) {
		try (TelegramBotsLongPollingApplication app = new TelegramBotsLongPollingApplication()) {
			TelegramChannelBotSentMessegeToPrivateChannel bot = new TelegramChannelBotSentMessegeToPrivateChannel();
			app.registerBot(BOT_TOKEN, bot);
			log.info("Bot started and is listening for incoming messages...!");
			// Bot sends the message after start
			bot.sendMessageToChannel("Hi Liza! Please, answer me!");
			// Automatic message from the bot every given interval
			new Timer().schedule(
					new TimerTask() {
						@Override
						public void run() {
							bot.sendMessageToChannel("How are you!");
						}
					},
					10000,  // delay before first launch (10 seconds)
					20000         // interval (10 seconds)
			);
			Thread.currentThread().join();
		} catch (Exception e) {
			log.error("Bot error", e);
		}
	}
}
