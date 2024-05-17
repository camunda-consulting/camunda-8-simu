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
import io.camunda.zeebe.protocol.record.value.TimerRecordValue;

public class LoaderExporter implements Exporter {
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
        //DataloadUtils.zeebeWorks();
        //if (record.getRecordType() == RecordType.EVENT) {

        /*if(record.getValueType() == ValueType.PROCESS_INSTANCE) {
                ProcessInstanceRecordValue value = (ProcessInstanceRecordValue) record.getValue();
                if (value.getBpmnElementType().equals(BpmnElementType.INTERMEDIATE_CATCH_EVENT)) {
                    try {
                        pingAuditApp(record.getTimestamp(), value);
                    } catch (JsonProcessingException e) {
                        LOGGER.error("error writing event to loader app", e);
                    }
                }
            }*/
        if(record.getValue() instanceof TimerRecordValue) {

            TimerRecordValue value = (TimerRecordValue) record.getValue();

            try {
                DataloadUtils.pingAuditApp(value.getDueDate(), value);
            } catch (JsonProcessingException e) {
                LOGGER.error("error writing event to loader app", e);
            }
        }
        // }
        this.controller.updateLastExportedRecordPosition(record.getPosition());
    }

    
}
