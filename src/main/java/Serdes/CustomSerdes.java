package Serdes;

import model.BettingPOJO;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import serializers.JsonDeserializer;
import serializers.JsonSerializer;

public final class CustomSerdes {
    private CustomSerdes() {}
    public static Serde<BettingPOJO> bettingPOJO() {
        JsonSerializer<BettingPOJO> serializer = new JsonSerializer<>();
        JsonDeserializer<BettingPOJO> deserializer = new JsonDeserializer<>(BettingPOJO.class);
        return Serdes.serdeFrom(serializer, deserializer);
    }
}
