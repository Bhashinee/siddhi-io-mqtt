/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package io.siddhi.extension.io.mqtt.sink;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Class to retain results received by Mqtt client.
 */
public class ResultContainer {
    private static final Logger log = LogManager.getLogger(ResultContainer.class);
    private int eventCount;
    private List<String> results;
    private CountDownLatch latch;
    private int timeout = 90;

    public ResultContainer(int expectedEventCount) {
        eventCount = 0;
        results = new ArrayList<>(expectedEventCount);
        latch = new CountDownLatch(expectedEventCount);
    }
    public ResultContainer(int expectedEventCount, int timeoutInSeconds) {
        eventCount = 0;
        results = new ArrayList<>(expectedEventCount);
        latch = new CountDownLatch(expectedEventCount);
        timeout = timeoutInSeconds;
    }

    public void eventReceived(MqttMessage mqttMessage) throws UnsupportedEncodingException {
        eventCount++;
        String message = new String(mqttMessage.getPayload(), "UTF-8");
        results.add(message);
        latch.countDown();
    }

    public Boolean assertMessageContent(String content) {
        try {
            if (latch.await(timeout, TimeUnit.SECONDS)) {
                for (String message : results) {
                    if (message.contains(content)) {
                        return true;
                    }
                }
                return false;
            } else {
                log.error("Expected number of results not received. Only received " + eventCount + " events.");
                return false;
            }

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        return false;
    }

}
