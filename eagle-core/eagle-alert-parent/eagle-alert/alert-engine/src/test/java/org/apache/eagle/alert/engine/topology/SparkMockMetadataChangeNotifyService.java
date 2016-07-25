/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.eagle.alert.engine.topology;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.eagle.alert.coordination.model.AlertBoltSpec;
import org.apache.eagle.alert.coordination.model.PublishSpec;
import org.apache.eagle.alert.coordination.model.RouterSpec;
import org.apache.eagle.alert.coordination.model.SpoutSpec;
import org.apache.eagle.alert.engine.coordinator.MetadataType;
import org.apache.eagle.alert.engine.coordinator.StreamDefinition;
import org.apache.eagle.alert.engine.coordinator.impl.AbstractMetadataChangeNotifyService;
import org.apache.eagle.alert.engine.utils.MetadataSerDeser;
import org.apache.eagle.alert.service.IMetadataServiceClient;
import org.apache.eagle.alert.service.MetadataServiceClientImpl;
import org.apache.eagle.alert.service.SpecMetadataServiceClientImpl;
import org.codehaus.jackson.type.TypeReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.typesafe.config.Config;


@SuppressWarnings({"serial"})
public class SparkMockMetadataChangeNotifyService extends AbstractMetadataChangeNotifyService implements Runnable {
    private final static Logger LOG = LoggerFactory.getLogger(SparkMockMetadataChangeNotifyService.class);
    @SuppressWarnings("unused")
    private static final String[] topics = new String[]{"testTopic3", "testTopic4", "testTopic5"};
    public static final String SPARK = "/spark";
    @SuppressWarnings("unused")
    private String topologyName;
    @SuppressWarnings("unused")
    private String spoutId;
    private Map<String, StreamDefinition> sds;
    private transient SpecMetadataServiceClientImpl client;

    public SparkMockMetadataChangeNotifyService(String topologyName, String spoutId) {
        this.topologyName = topologyName;
        this.spoutId = spoutId;
    }

    @Override
    public void init(Config config, MetadataType type) {
        super.init(config, type);
        this.client = new SpecMetadataServiceClientImpl(config);
        this.sds = defineStreamDefinitions();
        new Thread(this).start();
    }

    @Override
    public void run() {
        switch (type) {
            case SPOUT:
                notifySpout(Arrays.asList("testTopic3", "testTopic4"), Arrays.asList("testTopic5"));
                break;
            case STREAM_ROUTER_BOLT:
                populateRouterMetadata();
                break;
            case ALERT_BOLT:
                populateAlertBoltSpec();
                break;
            case ALERT_PUBLISH_BOLT:
                notifyAlertPublishBolt();
                break;
            case ALL:
                LOG.info("load metadata");
                notifySpecListener();
                break;
            default:
                LOG.error("that is not possible man!");
        }
    }

    private  void notifySpecListener() {

      /*  SpoutSpec newSpec = MetadataSerDeser.deserialize(getClass().getResourceAsStream(SPARK + "/testSpoutSpec.json"), SpoutSpec.class);
        RouterSpec routerSpec = MetadataSerDeser.deserialize(getClass().getResourceAsStream(SPARK + "/testStreamRouterBoltSpec.json"), RouterSpec.class);
        AlertBoltSpec alertBoltSpec = MetadataSerDeser.deserialize(getClass().getResourceAsStream(SPARK + "/testAlertBoltSpec.json"), AlertBoltSpec.class);
        PublishSpec publishSpec = MetadataSerDeser.deserialize(getClass().getResourceAsStream(SPARK + "/testPublishSpec.json"), PublishSpec.class);*/


        SpoutSpec newSpec = client.getSpoutSpec();
        RouterSpec routerSpec = client.getRouterSpec();
        AlertBoltSpec alertBoltSpec = client.getAlertBoltSpec();
        PublishSpec publishSpec = client.getPublishSpec();
        notifySpecListener(newSpec, routerSpec, alertBoltSpec, publishSpec, sds);
    }

    private Map<String, StreamDefinition> defineStreamDefinitions() {
        return client.getSds();
    }

    private void notifySpout(List<String> plainStringTopics, List<String> jsonStringTopics) {
        SpoutSpec newSpec = MetadataSerDeser.deserialize(getClass().getResourceAsStream(SPARK + "/testSpoutSpec.json"), SpoutSpec.class);
        notifySpout(newSpec, sds);
    }

    private void populateRouterMetadata() {
        RouterSpec boltSpec = MetadataSerDeser.deserialize(getClass().getResourceAsStream(SPARK + "/testStreamRouterBoltSpec.json"), RouterSpec.class);
        notifyStreamRouterBolt(boltSpec, sds);
    }

    private void populateAlertBoltSpec() {
        AlertBoltSpec spec = MetadataSerDeser.deserialize(getClass().getResourceAsStream(SPARK + "/testAlertBoltSpec.json"), AlertBoltSpec.class);
        notifyAlertBolt(spec, sds);
    }

    private void notifyAlertPublishBolt() {
        PublishSpec spec = MetadataSerDeser.deserialize(getClass().getResourceAsStream(SPARK + "/testPublishSpec.json"), PublishSpec.class);
        notifyAlertPublishBolt(spec, sds);
    }
}
