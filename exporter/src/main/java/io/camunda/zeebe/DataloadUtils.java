package io.camunda.zeebe;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.camunda.zeebe.protocol.record.value.TimerRecordValue;

public class DataloadUtils {
    public static final Logger LOGGER = LoggerFactory.getLogger("io.camunda.zeebe");
    
    private DataloadUtils()  {}
    
    private static Timer engineIdleTimer = null;
    private static ObjectMapper objectMapper = new ObjectMapper();

    private static synchronized void schedule(TimerTask task) {
        if (engineIdleTimer != null) {
            engineIdleTimer.cancel();
        }
        engineIdleTimer = new Timer();
        engineIdleTimer.schedule(task, 3000);
    }

    public static void zeebeWorks() {
        schedule(
                new TimerTask() {

                    @Override
                    public void run() {
                        pingIdle();
                    }
                });
    }

    public static void pingIdle() {
        HttpGet httpGet = new HttpGet("http://loader:8080/api/load/engine/idle");
        httpGet.addHeader("Content-Type", "application/json");

        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            try (CloseableHttpResponse response = httpClient.execute(httpGet)) {

            }
        } catch(IOException e) {
            LOGGER.error("Error reaching the audit app", e);
        }
    }

    public static void pingAuditApp(Long timestamp, TimerRecordValue record) throws JsonProcessingException {
        HttpPost httpPost = new HttpPost("http://loader:8080/api/load/record/"+timestamp);
        httpPost.addHeader("Content-Type", "application/json");

        httpPost.setEntity(new StringEntity(objectMapper.writeValueAsString(record)));

        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            try (CloseableHttpResponse response = httpClient.execute(httpPost)) {

            }
        } catch(IOException e) {
            LOGGER.error("Error reaching the audit app", e);
        }
    }

    
}
