package com.bifrost.demo.service.monitoring;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.cloudwatchlogs.CloudWatchLogsClient;
import software.amazon.awssdk.services.cloudwatchlogs.model.*;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.Instant;

@Service
public class CloudWatchService implements LogService {
    private CloudWatchLogsClient cloudWatchClient;

    @Value("${monitoring.cloudwatch.group-name}")
    private String cloudWatchGroupName;

    @Value("${monitoring.cloudwatch.region}")
    private String cloudWatchRegion;

    @Value("${monitoring.cloudwatch.stream-name}")
    private String cloudWatchStreamName;

    @Value("${logging.level.remote.debug}")
    private boolean remoteDebug;

    private String sequenceToken;
    private final Logger terminalLog = LoggerFactory.getLogger(CloudWatchService.class);

    @PostConstruct
    void init() {
        this.cloudWatchClient = CloudWatchLogsClient.builder()
                .region(Region.of(cloudWatchRegion))
                .build();
    }

    private void initializeSequenceToken() {
        try {
            DescribeLogStreamsRequest request = DescribeLogStreamsRequest.builder()
                    .logGroupName(cloudWatchGroupName)
                    .logStreamNamePrefix(cloudWatchStreamName)
                    .build();

            DescribeLogStreamsResponse response = cloudWatchClient.describeLogStreams(request);

            this.sequenceToken = response.logStreams().stream()
                    .filter(stream -> stream.logStreamName().equals(cloudWatchStreamName))
                    .findFirst()
                    .map(LogStream::uploadSequenceToken)
                    .orElse(null);

        } catch (Exception e) {
            System.out.println("Failed to get sequence token: " + e.getMessage());
            this.sequenceToken = null;
        }
    }

    public void _log(String level, String message) {
        terminalLog.debug(message);

        if (!remoteDebug && level.equals("DEBUG")) {
            return;
        }

        try {
            InputLogEvent logEvent = InputLogEvent.builder()
                    .message(String.format("[%s] %s",
                            level,
                            message))
                    .timestamp(Instant.now().toEpochMilli())
                    .build();

            PutLogEventsRequest request = PutLogEventsRequest.builder()
                    .logGroupName(cloudWatchGroupName)
                    .logStreamName(cloudWatchStreamName)
                    .logEvents(logEvent)
                    .sequenceToken(sequenceToken)
                    .build();

            PutLogEventsResponse response = cloudWatchClient.putLogEvents(request);

            this.sequenceToken = response.nextSequenceToken();

        } catch (Exception e) {
            System.out.println("Failed to send log to CloudWatch: " + e.getMessage());
            System.out.println(message);
        }
    }

    @Override
    public void info(String message) {
        _log("INFO", message);
    }

    @Override
    public void error(String message) {
        _log("ERROR", message);
    }

    @Override
    public void debug(String message) {
        _log("DEBUG", message);
    }

    @Override
    public void error(Exception ex) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        ex.printStackTrace(pw);
        _log("ERROR", sw.toString());
    }
}
