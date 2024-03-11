package uk.gov.companieshouse.filinghistory.consumer.kafka;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class FileEncryptionUtilsTest {

    private static final String DELTA_JSON_SUFFIX = "_delta.json";
    private static final String PASSWORD = "MySecretKeyFromTheKeyStore";
    private static final String ENCRYPTED_EXT = ".enc";

    @ParameterizedTest
    @CsvSource({"officers/TM01", "capital/SH07"})
    void shouldEncryptAndDecryptTestDelta(String sourceRootName) throws IOException {

        File inputFile = Paths.get("src/test/resources/data/%s%s".formatted(sourceRootName, DELTA_JSON_SUFFIX))
                .toFile();
        String deltaContent = Files.readString(inputFile.toPath(), StandardCharsets.UTF_8);
        File encryptedFile = new File(inputFile.getPath() + ENCRYPTED_EXT);

        // when
        FileEncryptionUtils.encryptFile(PASSWORD, inputFile);
        String decryptedContent = FileEncryptionUtils.decryptFile(PASSWORD, encryptedFile);

        // then
        assertEquals(deltaContent, decryptedContent);

        encryptedFile.deleteOnExit();
    }
}

