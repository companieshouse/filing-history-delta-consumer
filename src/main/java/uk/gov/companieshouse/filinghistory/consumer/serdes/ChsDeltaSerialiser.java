package uk.gov.companieshouse.filinghistory.consumer.serdes;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import org.apache.avro.io.DatumWriter;
import org.apache.avro.io.Encoder;
import org.apache.avro.io.EncoderFactory;
import org.apache.avro.reflect.ReflectDatumWriter;
import org.apache.kafka.common.serialization.Serializer;
import uk.gov.companieshouse.delta.ChsDelta;
import uk.gov.companieshouse.filinghistory.consumer.exception.NonRetryableException;

public class ChsDeltaSerialiser implements Serializer<ChsDelta> {

    @Override
    public byte[] serialize(String topic, ChsDelta data) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        Encoder encoder = EncoderFactory.get().directBinaryEncoder(outputStream, null);
        DatumWriter<ChsDelta> writer = getDatumWriter();
        try {
            writer.write(data, encoder);
        } catch (IOException e) {
            throw new NonRetryableException("Error serialising delta", e);
        }
        return outputStream.toByteArray();
    }

    public DatumWriter<ChsDelta> getDatumWriter() {
        return new ReflectDatumWriter<>(ChsDelta.class);
    }
}
