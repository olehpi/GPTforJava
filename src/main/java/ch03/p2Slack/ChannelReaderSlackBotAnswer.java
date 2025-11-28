package ch03.p2Slack;

import com.slack.api.Slack;
import com.slack.api.methods.MethodsClient;
import com.slack.api.methods.request.chat.ChatPostMessageRequest;
import com.slack.api.methods.response.chat.ChatPostMessageResponse;

public class ChannelReaderSlackBotAnswer {

	// Secrets are now loaded from environment variables – safe for GitHub!
	private static final String SLACK_BOT_TOKEN = getRequiredEnv("SLACK_BOT_TOKEN");
	private static final String SLACK_CHANNEL_ID = getRequiredEnv("SLACK_CHANNEL_ID");

	public static void main(String[] args) {
		Slack slack = Slack.getInstance();
		MethodsClient client = slack.methods(SLACK_BOT_TOKEN);

		ChatPostMessageRequest request = ChatPostMessageRequest.builder()
				.channel(SLACK_CHANNEL_ID)
				.text("I bot  " + java.time.LocalDateTime.now() + " Rocket")
				.build();

		try {
			ChatPostMessageResponse response = client.chatPostMessage(request);

			if (response.isOk()) {
				System.out.println("Message successfully sent to the channel!");
			} else {
				System.err.println("Failed to send message: " + response.getError());
			}
		} catch (java.io.IOException e) {
			System.err.println("Network error while sending message to Slack: " + e.getMessage());
		} catch (com.slack.api.methods.SlackApiException e) {
			System.err.println("Slack API error: " + e.getMessage());
		} catch (Exception e) {
			System.err.println("Unexpected error: " + e.getClass().getSimpleName() + " – " + e.getMessage());
		}
	}
	private static String getRequiredEnv(String name) {
		String value = System.getenv(name);
		if (value == null || value.isBlank()) {
			System.err.printf("ERROR: Environment variable %s is missing!%n", name);
			System.err.println("   Fix it and run again:");
			System.err.println("   export " + name);
			System.exit(1);
		}
		return value.trim();
	}
}