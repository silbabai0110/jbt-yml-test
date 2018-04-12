package com.falcons.jbt.yml;

import static com.jayway.restassured.RestAssured.given;
import static java.nio.file.Files.readAllBytes;
import static java.nio.file.Paths.get;
import static org.springframework.util.StringUtils.isEmpty;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang3.StringUtils;
import org.springframework.util.CollectionUtils;
import org.yaml.snakeyaml.Yaml;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.jayway.restassured.RestAssured;

public abstract class BaseRestEndToEndTest {

	Map<String, String> testProperties;
	String environmentName;
	String strUserName;
	String strPassword;

	Map<String, String> credCache;

	public static final String PINGFEDERATE_QA = "https://websec-qa.cable.comcast.com";
	public static final String PINGFEDERATE_INT = "https://websec-int.cable.comcast.com";
	public static final String PINGFEDERATE_STG = "https://websec-stg.cable.comcast.com";
	public static final String PINGFEDERATE_PROD = "https://websec.cable.comcast.com";

	// Map<String, Object> mapYamlTestData;
	// String testSuiteFileName;
	// @Before
	@SuppressWarnings("unchecked")
	public void setUp() throws FileNotFoundException {

		environmentName = System.getProperty("spring.profiles.active");

		String customUrl = System.getProperty("esp.url");

		strUserName = System.getenv("JBT_USERNAME");

		strPassword = System.getenv("JBT_PASSWORD");

		if (StringUtils.isEmpty(environmentName)) {
			throw new IllegalArgumentException("Test requires environment parameterization via -Dspring.profiles.active=esp-xxx");
		}

		if (StringUtils.isEmpty(strUserName)) {
			System.err.println("Test requires JBT_USERNAME environment variable set");
			throw new IllegalArgumentException("Test requires JBT_USERNAME environment variable set");
		}

		if (StringUtils.isEmpty(strPassword)) {
			System.err.println("Test requires JBT_PASSWORD environment variable set");
			throw new IllegalArgumentException("Test requires JBT_PASSWORD environment variable set");
		}
		/* if (StringUtils.isEmpty(testSuiteFileName)) { throw new IllegalArgumentException(
		 * "Test requires testSuite FileName via -Dspring.test.suite=testSuite_QA.yml" ); } */

		Yaml yaml = new Yaml();
		FileInputStream fileInputStream = new FileInputStream("src/e2eTest/resources/" + "properties.yml");
		Map<String, Map<String, String>> data = (Map<String, Map<String, String>>) yaml.load(fileInputStream);
		if (!data.keySet().contains(environmentName)) {
			throw new IllegalArgumentException("Unable to find specified environment named " + environmentName);
		}
		testProperties = data.get(environmentName);
		testProperties.put("profile", environmentName);

		if (!isEmpty(customUrl)) {
			testProperties.put("endPoint", customUrl);
		}

	}

	public Map<String, String> getTestProperties() {
		return testProperties;
	}

	public void setTestProperties(Map<String, String> testProperties) {
		this.testProperties = testProperties;
	}

	protected static Map<String, ?> getHeaders() {
		Map<String, String> headers = new HashMap<String, String>();

		headers.put("sourceSystemId", "ss-id");
		headers.put("trackingId", "trackingId-value");
		headers.put("Content-Type", "application/json");
		headers.put("Authorization", "Bearer " + System.getProperty("comcast.esp.oauth.token"));

		return headers;
	}

	@SuppressWarnings("unchecked")
	protected byte[] readFileContent(final String filePath, JsonObject jsonObj) throws IOException {
		String fileString = new String(readAllBytes(get("src/e2eTest/resources/json/" + filePath)));

		Gson gson = new Gson();

		Map<String, String> mapTemplate = new HashMap<String, String>();
		mapTemplate = (Map<String, String>) gson.fromJson(jsonObj.getAsJsonObject(), mapTemplate.getClass());

		for (Map.Entry<String, String> entry : mapTemplate.entrySet()) {
			fileString = StringUtils.replace(fileString, "[" + entry.getKey() + "]", String.valueOf(entry.getValue()));
		}

		System.out.println("Request: \n[" + fileString + "]");

		return fileString.getBytes();
	}

	public String getToken(JsonObject jsonObj) {

		String pingFederateEndPoint = null;

		String url = "/as/token.oauth2";
		String scope = null;
		String authToken = null;

		if (getTestProperties().get("profile").contains("qa")) {
			pingFederateEndPoint = PINGFEDERATE_QA;
		} else if (getTestProperties().get("profile").contains("int")) {
			pingFederateEndPoint = PINGFEDERATE_INT;
		} else if (getTestProperties().get("profile").contains("stg")) {
			pingFederateEndPoint = PINGFEDERATE_STG;
		} else if (getTestProperties().get("profile").contains("prd")) {
			pingFederateEndPoint = PINGFEDERATE_PROD;
		}

		/* if (null != jsonObj.get("userId")) { userName = jsonObj.get("userId").getAsString(); } else { userName =
		 * getTestProperties().get("userId"); } if (null != jsonObj.get("password")) { password =
		 * decrypt(jsonObj.get("password").getAsString()); } else { password = decrypt(getTestProperties().get("password")); } */

		if (null != jsonObj.get("scope")) {
			scope = jsonObj.get("scope").getAsString();
		} else {
			scope = getTestProperties().get("scope");
		}

		RestAssured.baseURI = pingFederateEndPoint;
		authToken = given().relaxedHTTPSValidation().request().contentType("application/x-www-form-urlencoded; charset=UTF-8")
				.body("client_id=" + getStrUserName() + "&client_secret=" + getStrPassword() + "&grant_type=client_credentials&scope=" + scope + "")
				.when().post(url).body().path("access_token").toString();

		return authToken;

	}

	public String getStrUserName() {
		return strUserName;
	}

	public void setStrUserName(String strUserName) {
		this.strUserName = strUserName;
	}

	public String getStrPassword() {
		return strPassword;
	}

	public void setStrPassword(String strPassword) {
		this.strPassword = strPassword;
	}

}
