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

import io.camunda.zeebe.exporter.api.Exporter;
import io.camunda.zeebe.exporter.api.context.Context;
import io.camunda.zeebe.exporter.api.context.Controller;
import io.camunda.zeebe.protocol.record.RecordType;
import io.camunda.zeebe.protocol.record.ValueType;
import io.camunda.zeebe.protocol.record.value.BpmnElementType;
import io.camunda.zeebe.protocol.record.value.ProcessInstanceRecordValue;

public class LoaderExporter implements Exporter {
    ObjectMapper objectMapper = new ObjectMapper();
  public static final Logger LOGGER = LoggerFactory.getLogger("io.camunda.zeebe");
    private Controller controller;
    @Override
    public void configure(Context context) {

    }
    @Override
    public void open(Controller controller) {
        this.controller = controller;
    }
    @Override
    public void close() {

    }
    @Override
    public void export(io.camunda.zeebe.protocol.record.Record<?> record) {
      if (record.getRecordType() == RecordType.EVENT) {
        if(record.getValueType() == ValueType.PROCESS_INSTANCE) {
            zeebeWorks();
            ProcessInstanceRecordValue value = (ProcessInstanceRecordValue) record.getValue();
            
            if (value.getBpmnElementType().equals(BpmnElementType.INTERMEDIATE_CATCH_EVENT)) {
              try {
                pingAuditApp(record.getTimestamp(), value);
            } catch (JsonProcessingException e) {
                LOGGER.error("error writing event to loader app", e);
            }
            }
    

        }
      }
      this.controller.updateLastExportedRecordPosition(record.getPosition());
    }
    
    public void pingAuditApp(Long timestamp, ProcessInstanceRecordValue record) throws JsonProcessingException {
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
    
    private boolean engineIdle = true;
    private Timer engineIdleTimer = null;

    private synchronized void schedule(TimerTask task) {
      if (engineIdleTimer != null) {
        engineIdleTimer.cancel();
      }
      engineIdleTimer = new Timer();
      engineIdleTimer.schedule(task, 300);
    }

    public void zeebeWorks() {
      engineIdle = false;
      schedule(
          new TimerTask() {

            @Override
            public void run() {
              engineIdle = true;
              pingIdle();
            }
          });
    }
    
    public void pingIdle() {
        HttpGet httpGet = new HttpGet("http://loader:8080/api/load/engine/idle");
        httpGet.addHeader("Content-Type", "application/json");

        //httpPost.setEntity(new StringEntity(objectMapper.writeValueAsString(record)));

        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            try (CloseableHttpResponse response = httpClient.execute(httpGet)) {
                
            }
        } catch(IOException e) {
          LOGGER.error("Error reaching the audit app", e);
        }
    }

}
