package org.example;

import model.BettingPOJO;
import Serdes.CustomSerdes;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.Grouped;
import org.apache.kafka.streams.kstream.KStream;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Properties;
import java.util.concurrent.CountDownLatch;

public class BettingAppStreams {
    public static void main(String[] args) throws IOException {

        final Serde<String> stringSerde = Serdes.String();
        final Serde<BettingPOJO> bettingPOJOSerde = CustomSerdes.bettingPOJO();

        final StreamsBuilder builder = new StreamsBuilder();

        KStream<String, BettingPOJO> bettingStream = builder.stream("BettingTopic",
                Consumed.with(stringSerde, bettingPOJOSerde));


        bettingStream.map((k, v) -> new KeyValue<>(v.getCarName(), v))
                .groupByKey(Grouped.with(stringSerde, bettingPOJOSerde))
                .reduce(((value1, value2) -> BettingPOJO.builder().carName(value1.getCarName())
                        .betPrice(value2.getBetPrice()).build()))
                .toStream().to("OddsSnapshotTopic");

        bettingStream.groupByKey(Grouped.with(stringSerde, bettingPOJOSerde))
                .reduce(((value1, value2) -> BettingPOJO.builder().carName(value1.getCarName())
                        .betPrice(value2.getBetPrice()).build()))
                .toStream().to("OddsTopic");

        final Properties props = loadConfig("/Users/himanism/.confluent/java.config");
        props.putIfAbsent(StreamsConfig.APPLICATION_ID_CONFIG, "BettingAppStreamApp1");
        props.setProperty(StreamsConfig.REPLICATION_FACTOR_CONFIG, "3");

        final KafkaStreams streams = new KafkaStreams(builder.build(), props);

        final CountDownLatch latch = new CountDownLatch(1);

        try {
            streams.start();
            latch.await();
        } catch (final Throwable e) {
            System.exit(1);
        }

        Runtime.getRuntime().addShutdownHook(new Thread("BettingAppStreamApp1") {
            @Override
            public void run() {
                streams.close();
                latch.countDown();
            }
        });
        System.out.println("Shutting down application");
        System.exit(0);
    }

    private static Properties loadConfig(final String configFile) throws IOException {
        if (!Files.exists(Paths.get(configFile))) {
            throw new IOException(configFile + " not found.");
        }
        final Properties cfg = new Properties();
        try (InputStream inputStream = new FileInputStream(configFile)) {
            cfg.load(inputStream);
        }
        return cfg;
    }
}
