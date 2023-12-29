package com.kits;

import com.adp.smartconnect.library.sourcedata.PayrollDataCollectionRequest;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.Assert;

import javax.xml.bind.JAXBException;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.ExecutionException;


public class KafkaTest {

    private String kafkaBroker;
    private String autoOffsetResetConfig;
    private String topic;
    private String groupId;

    public KafkaTest(String kafkaBroker, String autoOffsetResetConfig, String topic, String groupId) {
        this.kafkaBroker = kafkaBroker;
        this.autoOffsetResetConfig = autoOffsetResetConfig;
        this.topic = topic;
        this.groupId = groupId;
    }

    public void testKafkaProducerAndConsumer() throws InterruptedException, ExecutionException {

        // Set up the producer properties
        Properties producerProps = new Properties();

        producerProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaBroker);
        producerProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        producerProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());

        // Create the Kafka producer
        KafkaProducer<String, String> producer = new KafkaProducer<>(producerProps);

        // Set up the consumer properties
        Properties consumerProps = new Properties();
        consumerProps.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaBroker);
        consumerProps.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        consumerProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        consumerProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        consumerProps.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, autoOffsetResetConfig);
        // Create the Kafka consumer
        KafkaConsumer<String, String> consumer = new KafkaConsumer<>(consumerProps);
        consumer.subscribe(Arrays.asList(topic));
        // Subscribe to the topic

        for (int i = 1; i < 2; i++) {

            String message = "test message: " + i*11;
            ProducerRecord<String, String> record = new ProducerRecord<>(topic, message);
            RecordMetadata metadata = producer.send(record).get();

            // Verify that the message was sent successfully
            Assert.assertEquals(metadata.topic(), topic);

            // Consume messages from Kafka
            ConsumerRecords<String, String> messages = null;
            while (messages == null || messages.isEmpty()) {
                messages = consumer.poll(Duration.ofMillis(1000));
                if (messages.isEmpty()) {
                    System.out.println("no messages");
                } else {
                    System.out.println("messages: " + messages.count());
                }
            }

            Iterable<ConsumerRecord<String, String>> consRecItr = messages.records(topic);
            Iterator consumerItr = consRecItr.iterator();
            while (consumerItr.hasNext()) {
                ConsumerRecord con = (ConsumerRecord) consumerItr.next();
                System.out.println("messages record: " + con.topic() + " value: " + con.value());


            }


        }
    }

    public void checkMsg(String expectedJson) throws JAXBException, ClassNotFoundException {

        String msg = "{\n" +
                "  \"transactionId\": \"88370504-d4f1-4801-9da1-6a80d64d1c17\",\n" +
                "  \"settlementId\": \"482474a0729f10011bcc43b806350000\",\n" +
                "  \"customerId\": \"PoolFX Supply LLC\",\n" +
                "  \"startDate\": \"1483257600000\",\n" +
                "  \"endDate\": \"1924934400000\",\n" +
                "  \"page\": 37,\n" +
                "  \"count\": 100,\n" +
                "  \"totalPages\": 52,\n" +
                "  \"companyIds\": [\n" +
                "    \"PFX\",\n" +
                "    \"CYP\",\n" +
                "    \"SCP\"\n" +
                "  ],\n" +
                "  \"products\": [\n" +
                "    \"EmploymentVerification\",\n" +
                "    \"WagePayments\"\n" +
                "  ],\n" +
                "  \"featureFlags\": {\n" +
                "    \"connector.et.phased.implementation-enabled\": true,\n" +
                "    \"connector.statement-display-fields\": true,\n" +
                "    \"connector.et.ytd.tax.filter.enabled\": false,\n" +
                "    \"connector.withholding-orders-api-upgrade-v34-2\": true,\n" +
                "    \"connector.value-mapping\": true,\n" +
                "    \"connector.lien-upsert\": true,\n" +
                "    \"connector.company-retrieval-mode\": true,\n" +
                "    \"connector.workday-to-cdm.long-timeouts\": true,\n" +
                "    \"connector.wage-payment.sc-notification\": true,\n" +
                "    \"connector.notification.unresponsive-transactions.excessive-logging\": true,\n" +
                "    \"connector.statement-sort-fields\": true,\n" +
                "    \"connector.notification.failed-transactions.excessive-logging\": true,\n" +
                "    \"connector.employee-suffix-lookup\": true,\n" +
                "    \"connector.payment-type\": true,\n" +
                "    \"cct.oracle-fusion-sc.cg-integration-mode\": true,\n" +
                "    \"connector.workday-ucev-earns-from-payments\": true,\n" +
                "    \"connector.employment-tax-code-exclusion\": true,\n" +
                "    \"connector.oracle-payslip-xml-splitter\": true,\n" +
                "    \"wage.payment.generate.only.adp.check.number\": true,\n" +
                "    \"connector.erp-security-validation\": true,\n" +
                "    \"wage.payment.generate.only.adp.check.numbers\": false,\n" +
                "    \"connector.canada-third-party-payments\": true,\n" +
                "    \"use-async-pipeline\": true,\n" +
                "    \"diamond.monitor-log-smartconnect.oracle\": true,\n" +
                "    \"connector.lien-history\": true,\n" +
                "    \"connector.comp-garn-api.suffix\": true,\n" +
                "    \"job-settlement.save.via-update\": true,\n" +
                "    \"connector.wage-payment.create-payment-report\": true,\n" +
                "    \"everge.cg-integration-mode\": true,\n" +
                "    \"cct.process-api.generate-clientids\": true,\n" +
                "    \"feature.minimize-redis-usage\": true,\n" +
                "    \"connector.employment-tax\": true,\n" +
                "    \"connector.wage-payment.package-revised-file-numbers-report\": true,\n" +
                "    \"connector.et.phased.impl.enabled\": true,\n" +
                "    \"cct.everge.cg-integration-mode\": true,\n" +
                "    \"connector.wage-payment.package-file-numbers-report\": true,\n" +
                "    \"connector.tax-credits\": true,\n" +
                "    \"connector.workday-get-payrate-from-summary-data\": false,\n" +
                "    \"connector.value-mapping-export\": true,\n" +
                "    \"connector.notification.failed-transactions\": true,\n" +
                "    \"feature.ev_use_new_earnings_mapping\": true,\n" +
                "    \"diamond.monitor-log-smartconnect.workday\": true,\n" +
                "    \"connector.ofc.cg.pqq.use_terminationdate\": true,\n" +
                "    \"connector.wage-payment.use_spec_20.6.1\": true,\n" +
                "    \"wp-akka.logging.memory\": true,\n" +
                "    \"connector.suppress-zero-net-gross\": true,\n" +
                "    \"job-settlement.settlements-key.use-customer-id\": true,\n" +
                "    \"connector.process_settlements_by_flow_control\": true,\n" +
                "    \"connector.wp-display-earnings-details\": false,\n" +
                "    \"connector.employment-verification\": true,\n" +
                "    \"use-new-historical-liens-service\": true,\n" +
                "    \"mock.mosaic-response\": true,\n" +
                "    \"connector.time-traveler\": true,\n" +
                "    \"worker-update-scheduler-toggle-flag\": false,\n" +
                "    \"connector.total-hours-worked\": true,\n" +
                "    \"cct.tcs.payroll.enable\": false,\n" +
                "    \"connector.value-mapping-import\": true,\n" +
                "    \"polling.apply-three-days-look-back\": true,\n" +
                "    \"connector.notification.unresponsive-transactions\": true,\n" +
                "    \"everge.integration-mode\": true,\n" +
                "    \"connector.oracle-error-messaging\": true,\n" +
                "    \"connector.cupid-analytics\": true,\n" +
                "    \"connector.transaction-restart\": true,\n" +
                "    \"cct.ui.openfile.erp.option\": true,\n" +
                "    \"connector.wp.mn.use.operating\": false,\n" +
                "    \"connector.paystatements-splitter\": true,\n" +
                "    \"connector.jms-retry-mechanism\": true,\n" +
                "    \"connector.support-liens-complete\": true,\n" +
                "    \"connector.flow-control\": true,\n" +
                "    \"oracle-fusion-sc.cg-integration-mode\": true,\n" +
                "    \"polling.do-not-use-cct-start-date\": false,\n" +
                "    \"post-message-to-external-transaction-tracking-topic\": true,\n" +
                "    \"connector.oracle-testfile-payslip\": true,\n" +
                "    \"connector.ucvendors-dropdown-display\": true,\n" +
                "    \"connector.unemployment-claims\": true,\n" +
                "    \"worker-update-scheduler-run-toggling-flag\": true,\n" +
                "    \"OracleFusionSC.integration-mode\": true,\n" +
                "    \"stop.test.files.at.production\": true,\n" +
                "    \"connector.wage-payment.cash-out-institution\": true\n" +
                "  },\n" +
                "  \"orgUnitIdByCompanyId\": {\n" +
                "    \"SCP\": \"89704879-4023-4b21-a786-40d3c1317ea7\",\n" +
                "    \"PFX\": \"320dea57-91ce-4a96-9a27-cb6385e4c1a1\",\n" +
                "    \"CYP\": \"a83e36e6-745a-40e4-afa8-fb290ddfc5b9\"\n" +
                "  },\n" +
                "  \"flow\": \"PAYROLL_SHADOW_USA\",\n" +
                "  \"payrollHistoryParentTransactionId\": null\n" +
                "}";

        String userJson = "{\n" +
                "\"name\": \"Raghav\",\n" +
                "\"favorite_number\" : 16,\n" +
                "\"favorite_color\": \"blue\"\n" +
                "}";

        String type = "com.adp.smartconnect.library.sourcedata.PayrollDataCollectionRequest";


        Class c = Class.forName(type);
        c =  from(msg, c.getClass());

        ObjectMapper mapper = new ObjectMapper();
        try {
            PayrollDataCollectionRequest payroll = mapper.readValue(msg, PayrollDataCollectionRequest.class);
            System.out.println(payroll.getCustomerId());
             payroll = mapper.readValue(msg, PayrollDataCollectionRequest.class);
            System.out.println(payroll.getCustomerId());
            System.out.println(payroll.getProducts());
        } catch (JsonProcessingException e) {
            Assert.fail("invalid json");
        }


        String invalidUserJson = "{\n" +
                "\"name\": \"Raghav\",\n" +
//                "\"favorite_number\" : \"16\",\n" +
                "\"favorite_color\": \"blue\"\n" +
                "}";


//        ExpectedResponse expectedResponse = null;
//        try {
//            expectedResponse = mapper.readValue(expectedJson, ExpectedResponse.class);
//            System.out.println(expectedResponse.getKeyValueList());
//            System.out.println(expectedResponse.getKeyArrayList());
//        } catch (JsonProcessingException e) {
//            Assert.fail("invalid json");
//        }

        JSONObject userObj = new JSONObject(userJson);

        JSONObject payObj = new JSONObject(msg);

        System.out.println(getValuesInObject(userObj, "name"));

        System.out.println(getValuesInObject(payObj, "companyIds"));

        userJson = "{\n" +
                "    \"page\": 2,\n" +
                "    \"per_page\": 6,\n" +
                "    \"total\": 12,\n" +
                "    \"total_pages\": 2,\n" +
                "    \"data\": [\n" +
                "        {\n" +
                "            \"id\": 7,\n" +
                "            \"email\": \"michael.lawson@reqres.in\",\n" +
                "            \"first_name\": \"Michael\",\n" +
                "            \"last_name\": \"Lawson\",\n" +
                "            \"avatar\": \"https://reqres.in/img/faces/7-image.jpg\"\n" +
                "        },\n" +
                "        {\n" +
                "            \"id\": 8,\n" +
                "            \"email\": \"lindsay.ferguson@reqres.in\",\n" +
                "            \"first_name\": \"Lindsay\",\n" +
                "            \"last_name\": \"Ferguson\",\n" +
                "            \"avatar\": \"https://reqres.in/img/faces/8-image.jpg\"\n" +
                "        },\n" +
                "        {\n" +
                "            \"id\": 9,\n" +
                "            \"email\": \"tobias.funke@reqres.in\",\n" +
                "            \"first_name\": \"Tobias\",\n" +
                "            \"last_name\": \"Funke\",\n" +
                "            \"avatar\": \"https://reqres.in/img/faces/9-image.jpg\"\n" +
                "        },\n" +
                "        {\n" +
                "            \"id\": 10,\n" +
                "            \"email\": \"byron.fields@reqres.in\",\n" +
                "            \"first_name\": \"Byron\",\n" +
                "            \"last_name\": \"Fields\",\n" +
                "            \"avatar\": \"https://reqres.in/img/faces/10-image.jpg\"\n" +
                "        },\n" +
                "        {\n" +
                "            \"id\": 11,\n" +
                "            \"email\": \"george.edwards@reqres.in\",\n" +
                "            \"first_name\": \"George\",\n" +
                "            \"last_name\": \"Edwards\",\n" +
                "            \"avatar\": \"https://reqres.in/img/faces/11-image.jpg\"\n" +
                "        },\n" +
                "        {\n" +
                "            \"id\": 12,\n" +
                "            \"email\": \"rachel.howell@reqres.in\",\n" +
                "            \"first_name\": \"Rachel\",\n" +
                "            \"last_name\": \"Howell\",\n" +
                "            \"avatar\": \"https://reqres.in/img/faces/12-image.jpg\"\n" +
                "        }\n" +
                "    ],\n" +
                "    \"support\": {\n" +
                "        \"url\": \"https://reqres.in/#support-heading\",\n" +
                "        \"text\": \"To keep ReqRes free, contributions towards server costs are appreciated!\"\n" +
                "    }\n" +
                "}";

        JSONObject userObject = new JSONObject(userJson);
        JSONArray userArray = userObject.getJSONArray("data");

        System.out.println(getValuesInObject(userObject, "data"));
        System.out.println(getValuesInArray(userArray, "first_name"));


    }

    public List<String> getValuesInObject(JSONObject jsonObject, String sourceKey) {
        List<String> values = new ArrayList<>();
        for (String key : jsonObject.keySet()) {
            Object value = jsonObject.get(key);
            if (key.equals(sourceKey)) {
                values.add(value.toString());
            }

            if (value instanceof JSONObject) {
                values.addAll(getValuesInObject((JSONObject) value, sourceKey));
            } else if (value instanceof JSONArray) {
                values.addAll(getValuesInArray((JSONArray) value, sourceKey));
            }
        }

        return values;
    }

    public List<String> getValuesInArray(JSONArray jsonArray, String sourceKey) {
        List<String> values = new ArrayList<>();
        for (Object obj : jsonArray) {
            if (obj instanceof JSONArray) {
                values.addAll(getValuesInArray((JSONArray) obj, sourceKey));
            } else if (obj instanceof JSONObject) {
                values.addAll(getValuesInObject((JSONObject) obj, sourceKey));
            }
        }

        return values;
    }

    public String getKafkaBroker() {
        return kafkaBroker;
    }

    public void setKafkaBroker(String kafkaBroker) {
        this.kafkaBroker = kafkaBroker;
    }

    public String getAutoOffsetResetConfig() {
        return autoOffsetResetConfig;
    }

    public void setAutoOffsetResetConfig(String autoOffsetResetConfig) {
        this.autoOffsetResetConfig = autoOffsetResetConfig;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public  <T> T from(String messageJSON, Class<T> clazz) throws JAXBException {

        ObjectMapper mapper = new ObjectMapper();
        T response = null;
        try {
            mapper.configure(JsonParser.Feature.ALLOW_BACKSLASH_ESCAPING_ANY_CHARACTER, true);
            // Convert JSON string to Object
            response = mapper.readValue(messageJSON, clazz);

        } catch (Exception e) {
            e.printStackTrace();
        }

        return response;
    }
}
