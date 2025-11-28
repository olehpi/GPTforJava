package ch03.p3Telegram;

import ch03.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;
import org.telegram.telegrambots.longpolling.TelegramBotsLongPollingApplication;
import org.telegram.telegrambots.longpolling.util.LongPollingSingleThreadUpdateConsumer;

public class TelegramBotSpeakWithUser implements LongPollingSingleThreadUpdateConsumer {

	private static final Logger log = LoggerFactory.getLogger(TelegramBotSpeakWithUser.class);
	// Secrets are now loaded from environment variables – safe for GitHub!
	private static final String BOT_TOKEN = Utils.getRequiredEnv("TELEGRAM_BOT_TOKEN");
	private final TelegramClient client;

	public TelegramBotSpeakWithUser() {
		this.client = new OkHttpTelegramClient(BOT_TOKEN);
	}

	@Override
	public void consume(Update update) {
		if (update.getMessage() != null && update.getMessage().hasText()) {
			long chatId = update.getMessage().getChatId();
			String text = update.getMessage().getText();

			SendMessage msg = SendMessage.builder()
					.chatId(String.valueOf(chatId))
					.text("Hi, User! You told: " + text)
					.build();

			try {
				client.execute(msg);
				log.info("Replied to {}", chatId);
			} catch (TelegramApiException e) {
				log.error("Failed to send message to {}: {}", chatId, e.getMessage());
			} catch (Exception e) {
				log.error("Unexpected error while sending message", e);
			}
		}
	}

	public static void main(String[] args) {
        try (TelegramBotsLongPollingApplication app = new TelegramBotsLongPollingApplication()) {
            app.registerBot(BOT_TOKEN, new TelegramBotSpeakWithUser());
            log.info("Bot started!");
            Thread.currentThread().join();
        } catch (Exception e) {
            log.error("Bot error", e);
        }
    }
}


