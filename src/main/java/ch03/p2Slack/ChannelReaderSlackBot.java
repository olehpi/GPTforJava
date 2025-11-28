package ch03.p2Slack;

import com.slack.api.Slack;
import com.slack.api.methods.MethodsClient;
import com.slack.api.methods.request.conversations.ConversationsHistoryRequest;
import com.slack.api.methods.response.conversations.ConversationsHistoryResponse;
import com.slack.api.methods.request.users.UsersInfoRequest;
import com.slack.api.methods.response.users.UsersInfoResponse;
import com.slack.api.model.Message;
import com.slack.api.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import java.io.IOException;
import java.time.*;
import java.util.Collections;
import java.util.List;

public class ChannelReaderSlackBot {

	private static final Logger log = LoggerFactory.getLogger(ChannelReaderSlackBot.class);

	// Secrets are now loaded from environment variables – safe for GitHub!
	private static final String SLACK_BOT_TOKEN = getRequiredEnv("SLACK_BOT_TOKEN");
	private static final String SLACK_CHANNEL_ID = getRequiredEnv("SLACK_CHANNEL_ID");


	public static void main(String[] args) {
    	Slack slack = Slack.getInstance();
    	MethodsClient methods = slack.methods(SLACK_BOT_TOKEN);

    	LocalDateTime startTimeUTC = LocalDateTime.of(2025, Month.NOVEMBER, 27, 10, 0);
    	LocalDateTime endTimeUTC = LocalDateTime.of(2025, Month.DECEMBER, 28, 15, 0);

    	long startTime = startTimeUTC.atZone(ZoneOffset.UTC).toEpochSecond();
    	long endTime = endTimeUTC.atZone(ZoneOffset.UTC).toEpochSecond();

    	ConversationsHistoryRequest request = ConversationsHistoryRequest.builder()
        	.channel(SLACK_CHANNEL_ID)
        	.oldest(String.valueOf(startTime))
        	.latest(String.valueOf(endTime))
        	.build();

    	try {
        	ConversationsHistoryResponse response = methods.conversationsHistory(request);
        	if (response != null && response.isOk()) {
            	List<Message> messages = response.getMessages();
            	Collections.reverse(messages);
            	for (Message message : messages) {
                	String userId = message.getUser();
                	String timestamp = formatTimestamp(message.getTs());

                	UsersInfoRequest userInfoRequest = UsersInfoRequest.builder()
                    	.user(userId)
                    	.build();

                	UsersInfoResponse userInfoResponse = methods.usersInfo(userInfoRequest);
                	if (userInfoResponse != null && userInfoResponse.isOk()) {
                    	User user = userInfoResponse.getUser();
                    	System.out.println("User: " + user.getName());
                    	System.out.println("Timestamp: " + timestamp);
                    	System.out.println("Message: " + message.getText());
                    	System.out.println();
                	}
            	}
        	} else {
				String error = (response != null && response.getError() != null)
						? response.getError()
						: "unknown_error (response was null or error field missing)";
				log.error("Slack API error: {}", error);
        	}
		} catch (IOException e) {
			System.err.println("Network / API error: " + e.getMessage());
		} catch (Exception e) {
			System.err.println("Unexpected error: " + e.getMessage());
		}
	}

	private static String formatTimestamp(String ts) {
    	double timestamp = Double.parseDouble(ts);
    	Instant instant = Instant.ofEpochSecond((long) timestamp);
    	LocalDateTime dateTime = LocalDateTime.ofInstant(instant, ZoneOffset.UTC);
    	return dateTime.toString();
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
