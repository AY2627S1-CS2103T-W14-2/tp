package seedu.address.commons.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;

import seedu.address.commons.exceptions.DataLoadingException;
import seedu.address.testutil.SerializableTestClass;
import seedu.address.testutil.TestUtil;

/**
 * Tests JSON Read and Write
 */
public class JsonUtilTest {

    private static final Path SERIALIZATION_FILE = TestUtil.getFilePathInSandboxFolder("serialize.json");

    @Test
    public void serializeObjectToJsonFile_noExceptionThrown() throws IOException {
        SerializableTestClass serializableTestClass = new SerializableTestClass();
        serializableTestClass.setTestValues();

        JsonUtil.serializeObjectToJsonFile(SERIALIZATION_FILE, serializableTestClass);

        assertEquals(FileUtil.readFromFile(SERIALIZATION_FILE), SerializableTestClass.JSON_STRING_REPRESENTATION);
    }

    @Test
    public void deserializeObjectFromJsonFile_noExceptionThrown() throws IOException {
        FileUtil.writeToFile(SERIALIZATION_FILE, SerializableTestClass.JSON_STRING_REPRESENTATION);

        SerializableTestClass serializableTestClass = JsonUtil
                .deserializeObjectFromJsonFile(SERIALIZATION_FILE, SerializableTestClass.class);

        assertEquals(serializableTestClass.getName(), SerializableTestClass.getNameTestValue());
        assertEquals(serializableTestClass.getListOfLocalDateTimes(), SerializableTestClass.getListTestValues());
        assertEquals(serializableTestClass.getMapOfIntegerToString(), SerializableTestClass.getHashMapTestValues());
    }

    @Test
    public void readJsonFile_nullContent_throwsDataLoadingException() throws IOException {
        FileUtil.writeToFile(SERIALIZATION_FILE, "null");

        DataLoadingException exception = assertThrows(DataLoadingException.class, () ->
                JsonUtil.readJsonFile(SERIALIZATION_FILE, SerializableTestClass.class));
        assertEquals(JsonUtil.MESSAGE_NULL_CONTENT, exception.getCause().getMessage());
    }

    @Test
    public void fromJsonString_contentAfterFirstValue_throwsJsonProcessingException() {
        for (String trailing : List.of("{\"a\":2}", "[]", "1", "null", "text", "}")) {
            assertThrows(JsonProcessingException.class, () ->
                    JsonUtil.fromJsonString("{\"a\":1} " + trailing, JsonNode.class), trailing);
        }
    }

    @Test
    public void fromJsonString_repeatedKey_throwsJsonProcessingException() {
        assertThrows(JsonProcessingException.class, () ->
                JsonUtil.fromJsonString("{\"a\":1,\"a\":2}", JsonNode.class));
        assertThrows(JsonProcessingException.class, () ->
                JsonUtil.fromJsonString("{\"a\":[{\"b\":1,\"b\":2}]}", JsonNode.class));
    }

    @Test
    public void fromJsonString_trailingWhitespace_readsValue() throws IOException {
        assertEquals(1, JsonUtil.fromJsonString("{\"a\":1} \r\n\t ", JsonNode.class).get("a").asInt());
    }

    @Test
    public void fromJsonString_sameKeyInDifferentObjects_readsValue() throws IOException {
        JsonNode value = JsonUtil.fromJsonString("{\"a\":{\"b\":1},\"c\":{\"b\":2}}", JsonNode.class);
        assertEquals(2, value.get("c").get("b").asInt());
    }

    //TODO: @Test jsonUtil_readJsonStringToObjectInstance_correctObject()

    //TODO: @Test jsonUtil_writeThenReadObjectToJson_correctObject()
}
