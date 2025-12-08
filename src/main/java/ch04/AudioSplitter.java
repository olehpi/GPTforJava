package ch04;

import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.FFmpegFrameRecorder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class AudioSplitter {
	private static final Logger log = LoggerFactory.getLogger(AudioSplitter.class);
	public static void main(String[] args) {
		// Download the audio in the folder with inputFilePath: https://www.thisamericanlife.org/811/the-one-place-i-cant-go
    	String inputFilePath = "src/main/resources/ch04/source_TheOnePlaceICantGo/811.mp3";
    	String outputDirectory = "src/main/resources/ch04/target_TheOnePlaceICantGo/";
    	int segmentDurationInSeconds = 60;

    	try (FFmpegFrameGrabber grabber = new FFmpegFrameGrabber(inputFilePath)) {
        	grabber.start();

        	long totalDurationInSeconds = grabber.getLengthInTime() / 1000000; // Convert microseconds to seconds
        	double frameRate = grabber.getFrameRate();

        	long segmentStartTime = 0;
        	long segmentEndTime;
        	int segmentNumber = 1;

        	while (segmentStartTime < totalDurationInSeconds) {
            	String outputFilePath = outputDirectory + "segment_" + String.format("%05d", segmentNumber) + ".mp3";

            	try (FFmpegFrameRecorder recorder = new FFmpegFrameRecorder(outputFilePath, 0)) {
                	recorder.setAudioChannels(2);
                	recorder.setAudioCodecName("libmp3lame"); // Set the audio codec to MP3
                	recorder.setAudioBitrate(192000); // Adjust bitrate as needed
                	recorder.setSampleRate(44100); // Adjust sample rate as needed
                	recorder.setFrameRate(frameRate);
                	recorder.setFormat("mp3"); // Set the output format to MP3
                	recorder.start();

                	segmentEndTime = Math.min(segmentStartTime + segmentDurationInSeconds, totalDurationInSeconds);

                	grabber.setTimestamp(segmentStartTime * 1000000); // Set the grabber's timestamp to the start time in microseconds

                	while (grabber.getTimestamp() / 1000000 < segmentEndTime) {
                    	recorder.record(grabber.grabSamples());
                	}
            	}

            	segmentStartTime = segmentEndTime;
            	segmentNumber++;
        	}
		} catch (IOException e) {
			log.error("Failed to process audio file: {}. Splitting aborted.", inputFilePath, e);
		}
	}
}
