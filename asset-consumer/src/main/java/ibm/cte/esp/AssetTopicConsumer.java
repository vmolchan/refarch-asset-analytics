package ibm.cte.esp;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import com.google.gson.Gson;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ibm.cte.esp.model.MetricEvent;

/**
 * subscriber to asset topic
 * @author jeromeboyer
 *
 */
public class AssetTopicConsumer {
	final static Logger logger = LoggerFactory.getLogger("AssetTopicConsumer");
	private static KafkaConsumer<String, String> kafkaConsumer;
    
    private Gson gson = new Gson();
    public ApplicationConfig config;
    
    public AssetTopicConsumer(ApplicationConfig cfg) {
     this.config = cfg;	
     prepareConsumer();
    }
    
    private void prepareConsumer() {
		Properties properties = new Properties();
        properties.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, 
        		config.getProperties().getProperty(ApplicationConfig.KAFKA_BOOTSTRAP_SERVERS));
        properties.put(ConsumerConfig.GROUP_ID_CONFIG, 
        		config.getProperties().getProperty(ApplicationConfig.KAFKA_GROUPID));
        // offsets are committed automatically with a frequency controlled by the config auto.commit.interval.ms
        // here we want to use manual commit 
        properties.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG,"false");
        properties.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        // properties.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest");
        properties.put(ConsumerConfig.AUTO_COMMIT_INTERVAL_MS_CONFIG,"1000");
        properties.put(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG, "30000");
        properties.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        properties.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
 
        properties.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        
        kafkaConsumer = new KafkaConsumer<>(properties);
        kafkaConsumer.subscribe(Arrays.asList(config.getProperties().getProperty(ApplicationConfig.KAFKA_ASSET_TOPIC_NAME)));
	}
    
    public List<MetricEvent>  consume() {
    	List<MetricEvent> buffer = new ArrayList<MetricEvent>();
    	ConsumerRecords<String, String> records = kafkaConsumer.poll(
    			Long.parseLong(config.getProperties().getProperty(ApplicationConfig.KAFKA_POLL_DURATION)));
    	
	    for (ConsumerRecord<String, String> record : records) {
	    	MetricEvent a = gson.fromJson(record.value(), MetricEvent.class);
	    		logger.info((new Date()).toString() + " " + a.toString());
	            buffer.add(a);
	    }
    	return buffer;
    }
    
    public void commitOffset() {
    	kafkaConsumer.commitSync();
    }
    
    public void close() {
    	kafkaConsumer.close();
    }
}
