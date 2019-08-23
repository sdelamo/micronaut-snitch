package com.agorapulse.micronaut.snitch;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Default snitch service uses the SnitchClient to report to the snitch service.
 *
 * Successful reports are send at most once per 5 minutes.
 */
public class DefaultSnitchService implements SnitchService {

    public static final String OK = "Got it, thanks!";

    private static final Logger LOGGER = LoggerFactory.getLogger(SnitchService.class);
    private static final long CALL_INTERVAL = 5L * 60 * 1000; // 5 minutes

    private final SnitchClient client;
    private final SnitchJobConfiguration configuration;

    private long lastSuccessfulCallTime;

    public DefaultSnitchService(SnitchClient client, SnitchJobConfiguration configuration) {
        this.client = client;
        this.configuration = configuration;
    }

    @Override
    public boolean snitch(boolean success) {
        if (lastSuccessfulCallTime > 0 && lastSuccessfulCallTime + CALL_INTERVAL > System.currentTimeMillis()) {
            return true;
        }
        try {
            boolean lastSuccessfulCall = OK.equals(client.snitch(configuration.getId(), success ? "1" : "0"));

            if (lastSuccessfulCall) {
                lastSuccessfulCallTime = System.currentTimeMillis();
            }

            return lastSuccessfulCall;
        } catch (Exception ex) {
            LOGGER.warn("Exception notifying snitch " + configuration.getName(), ex);
            return false;
        }
    }
}
