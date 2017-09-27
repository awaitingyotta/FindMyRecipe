package food2fork.com.findmyrecipe.json;

import java.io.IOException;
import java.util.logging.Level;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.DeserializationFeature;

/**
 * @author Alexei Ivanov
 */
public class BaseJson {
	private static ObjectMapper mapper = new ObjectMapper();

	public BaseJson() {

	}
	
	/**
	 * Converts this JSON object to a string (returns its string representation)
	 * @return 	String json
	 */
	public String jsonToString() {
		//Parse Java object to JSON-string:
		String json;
		configureObjectMapper();	// should not be necessary, but do it anyway
		try {
			json = mapper.writeValueAsString(this);
		} catch (Exception e) {
			return null;
		}
		return json;
	}

	public static <T extends BaseJson> BaseJson stringToJsonObject(String jsonString, Class<? extends BaseJson> jsonClass)
			throws JsonParseException, JsonMappingException, IOException {
		configureObjectMapper();
		//Parse JSON-input to variables:
		return mapper.readValue(jsonString, jsonClass);
	}

	/**
	 * Configures the object mapper. Currently, this method only sets the mapper not to fail on unknown properties. 
	 */
	private static void configureObjectMapper() {
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
	}
}
	