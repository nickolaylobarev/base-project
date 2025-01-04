package utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

/**
 * Class for handling JSON messages in tests
 *
 * <p>Provides methods for converting JSON files or strings to objects and serializing objects to JSON</p>
 */

public class JsonMessageTestUtils {

    public static ObjectMapper objectMapper = new ObjectMapper();

    public static <T> T jsonMessage(String templateFilePath, Class<T> clazz) throws IOException {
        String body = new String(Files.readAllBytes(Paths.get(
                JsonMessageTestUtils.class.getResource(templateFilePath).getPath())));
        return objectMapper.readValue(body, clazz);
    }

    public static <T> T jsonMessageFromBody(String body, Class<T> clazz) {
        try {
            return objectMapper.readValue(body, clazz);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static <T> String toArrayJson(T obj) throws IOException {
        return objectMapper.writeValueAsString(List.of(obj));
    }

    public static <T> String toArrayJson(List<T> objList) throws IOException {
        return objectMapper.writeValueAsString(objList);
    }

    public static <T> String toJson(T obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
