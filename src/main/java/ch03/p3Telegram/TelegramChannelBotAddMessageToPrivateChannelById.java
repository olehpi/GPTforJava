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

public class TelegramChannelBotAddMessageToPrivateChannelById implements LongPollingSingleThreadUpdateConsumer {

	/**
	 * How to get the Chat ID of a Private Telegram Channel (2025 method)
	 * Telegram private channels have IDs that start with `-100` followed by a long number (e.g. `-1001234567890`).
	 * You cannot see this ID in the Telegram app UI — you must fetch it via the Bot API.
	 * Step-by-step (works 100 %)
	 * 1. **Add your bot to the private channel as an administrator**
	 *    (Settings → Administrators → Add Administrator → find your bot)
	 * 2. **Send any message to the private channel**
	 *    (you can send it yourself or let the bot do it — doesn’t matter)
	 * 3. Open your browser and go to this URL (replace `<YOUR_BOT_TOKEN>`):
	 * https://api.telegram.org/bot<YOUR_BOT_TOKEN>/getUpdates
	 * textExample:
	 * https://api.telegram.org/bot800000000:AAF-iIp1Og7MIXgkIToааааtvgnlFPqpfEg/getUpdates
	 * text4. You will get a JSON response like this:
	 * ```json
	 * {
	 *   "ok": true,
	 *   "result": [
	 *     {
	 *       "update_id": 261938770,
	 *       "channel_post": {
	 *         "message_id": 56,
	 *         "chat": {
	 *           "id": -1003376720000,
	 *           "title": "MyBotChanel",
	 *           "type": "channel"
	 *         },
	 *         "date": 1764366657,
	 *         "text": "c"
	 *       }
	 *     }
	 *   ]
	 * }
	 * Copy the value of "id" inside the chat object → this is your private channel chat ID:text-1003376700000 That’s it! Use this ID in your code:Java.chatId("-1003376726576")
	 */


	private static final Logger log = LoggerFactory.getLogger(TelegramChannelBotAddMessageToPrivateChannelById.class);
	// Secrets are now loaded from environment variables – safe for GitHub!
	private static final String BOT_TOKEN = Utils.getRequiredEnv("TELEGRAM_BOT_TOKEN");
	private static final String CHANNEL_ID = Utils.getRequiredEnv("TELEGRAM_CHANNEL_ID");
	private final TelegramClient client;

	public TelegramChannelBotAddMessageToPrivateChannelById() {
		this.client = new OkHttpTelegramClient(BOT_TOKEN);
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
					.text("You wrote: " + userText)
					.build();
			try {
				client.execute(reply);
				log.info("Replied to {}", userChatId);
			} catch (TelegramApiException e) {
				log.error("Failed to send message to {}: {}", userChatId, e.getMessage());
			} catch (Exception e) {
				log.error("Unexpected error while sending message", e);
			}

			// Send the message into private channel
			SendMessage channelMsg = SendMessage.builder()
					.chatId(CHANNEL_ID)
					.text("Message from User: " + userText)
					.build();
			try {
				client.execute(channelMsg);
			} catch (Exception e) {
				log.error("Unexpected error while sending message into private channel", e);
			}
		}
	}

	public static void main(String[] args) {
		try (TelegramBotsLongPollingApplication app = new TelegramBotsLongPollingApplication()) {
			app.registerBot(BOT_TOKEN, new TelegramChannelBotAddMessageToPrivateChannelById());
			log.info("Bot started!");
			Thread.currentThread().join();
		} catch (Exception e) {
			log.error("Bot error", e);
		}
	}
}
