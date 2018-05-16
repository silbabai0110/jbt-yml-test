package com.falcons.jbt.yml;

import static com.jayway.restassured.RestAssured.given;
import static java.nio.file.Files.readAllBytes;
import static java.nio.file.Paths.get;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
//import org.json.JSONException;
import org.json.JSONObject;
import org.json.XML;
import org.yaml.snakeyaml.Yaml;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.jayway.restassured.RestAssured;
import com.jayway.restassured.path.json.JsonPath;
import com.jayway.restassured.path.xml.XmlPath;
import com.jayway.restassured.path.xml.element.Node;
import com.jayway.restassured.path.xml.element.NodeChildren;
import com.jayway.restassured.response.Response;

public class ExecuteAndValidate extends BaseRestEndToEndTest {

	private Map<String, Object> mapYamlTestData = new LinkedHashMap<>();
	private Map<String, Map<String, String>> iGlobalDataBank = new HashMap<>();
	private Map<String, JsonObject> iCheckHealthDataBank = new LinkedHashMap<>();
	private Map<String, JsonElement> ipropertyCanaryMap = new LinkedHashMap<>();
	private Map<String, JsonElement> ipropertyProdMap = new LinkedHashMap<>();
	

	private Integer intPassCount = 0;
	private Integer intFailCount = 0;

	@SuppressWarnings({ "unchecked" })
	public ExecuteAndValidate() {
		Yaml yamlTestSuite = new Yaml();
		FileInputStream fileTestInputStream = null;
		String testSuiteFileName = null;
		String resourcesFldr = null;

		testSuiteFileName = System.getenv("E2E_TESTSUITENAME");

		System.out.println("testSuiteFileName = " + testSuiteFileName);

		if (null != testSuiteFileName && !testSuiteFileName.isEmpty()) {
			try {
				setUp();
				if (testSuiteFileName.contains("xml") || testSuiteFileName.contains("XML")) {
					resourcesFldr = "src/test/resources/xml/";
				} else if (testSuiteFileName.contains("json") || testSuiteFileName.contains("JSON")) {
					resourcesFldr = "src/test/resources/json/";
				}
				fileTestInputStream = new FileInputStream(resourcesFldr + testSuiteFileName);
				setmapYamlTestData((Map<String, Object>) yamlTestSuite.load(fileTestInputStream));

			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				throw new AssertionError(e.toString());
			} catch (NullPointerException e) {
				e.printStackTrace();
				throw new AssertionError(e.toString());
			}
		} else {
			System.out.println("E2E_TESTSUITENAME environment variable is not set");
		}

	}

	public Response executeRest(String testId, String url, String fileName, JsonObject jsonObj, String httpMethod, String contentType)
			throws Exception {

		// comment: try / catch here?
		Response response = null;
		String sourceSystemId;
		String trackingId;
		String endPoint = getTestProperties().get("endPoint");

		if (null != jsonObj.get("sourceSystemId")) {
			sourceSystemId = jsonObj.get("sourceSystemId").toString();
		} else {
			// Defaulting to grandslam ssid
			sourceSystemId = "ESP-GRANDSLAM";
		}

		if (null != jsonObj.get("trackingId")) {
			trackingId = jsonObj.get("trackingId").toString();
		} else {
			// Defaulting to random trackingid
			trackingId = "ESP_TRACKINGID_" + String.valueOf(System.currentTimeMillis());
		}

		if (null != jsonObj.get("baseURI")) {
			System.out.println("baseURI : " + jsonObj.get("baseURI").getAsString());
			RestAssured.baseURI = jsonObj.get("baseURI").getAsString();
		} else {
			RestAssured.baseURI = endPoint;
		}

		if ("POST".equalsIgnoreCase(httpMethod)) {
			if ("xml".equalsIgnoreCase(contentType)) {
				response = given().relaxedHTTPSValidation().request().headers(getHttpHeaders(sourceSystemId, trackingId, jsonObj))
						.contentType(getContentType(jsonObj)).body(new String(readFileContent(fileName, jsonObj, "xml"))).when().log().all()
						.post(url);
			} else {

				if (null != fileName && !fileName.equals("null")) {
					response = given().log().path().log().params().log().method().headers(getHttpHeaders(sourceSystemId, trackingId, jsonObj))
							.body(readFileContent(fileName, jsonObj, "json")).when().log().all().post(url).thenReturn();

				} else {
					response = given().log().path().log().params().log().method().headers(getHttpHeaders(sourceSystemId, trackingId, jsonObj)).when()
							.log().all().post(url).thenReturn();
				}
			}

		}

		if ("GET".equalsIgnoreCase(httpMethod)) {
			response = given().log().path().log().params().log().method().headers(getHttpHeaders(sourceSystemId, trackingId, jsonObj)).when().log()
					.all().get(url).thenReturn();
		}

		if ("DELETE".equalsIgnoreCase(httpMethod)) {

			if (null != fileName && !fileName.equals("null")) {
				response = given().log().path().log().params().log().method().headers(getHttpHeaders(sourceSystemId, trackingId, jsonObj))
						.body(readFileContent(fileName, jsonObj, "json")).when().log().all().delete(url).thenReturn();

			} else {
				response = given().log().path().log().params().log().method().headers(getHttpHeaders(sourceSystemId, trackingId, jsonObj)).when()
						.log().all().delete(url).thenReturn();
			}
		}
		
		if ("PUT".equalsIgnoreCase(httpMethod)) {

			if (null != fileName && !fileName.equals("null")) {
				response = given().log().path().log().params().log().method().headers(getHttpHeaders(sourceSystemId, trackingId, jsonObj))
						.body(readFileContent(fileName, jsonObj, "json")).when().log().all().put(url).thenReturn();

			} else {
				response = given().log().path().log().params().log().method().headers(getHttpHeaders(sourceSystemId, trackingId, jsonObj)).when()
						.log().all().put(url).thenReturn();
			}
		}

		// Process depending on complexity
		switch (Integer.parseInt(jsonObj.get("complexity").toString())) {

		case 1:
			// ********** Complexity 1 ***********
			// do nothing here
			//
			break;

		case 2:
			// ********** Complexity 2 ***********
			// Saves value(s) into a databank (cached) to be used during
			// subsequent test(s)
			// databank is stored specifically in iGlobalDataBank; temp data,
			// only lives within the test run
			//
			JsonArray tmpArrayOfDataBankPath = jsonObj.get("databank").getAsJsonArray();

			Map<String, String> iDataMap = new HashMap<>();

			for (JsonElement iJSPath : tmpArrayOfDataBankPath) {

				String[] iArrayOfiJSPath = iJSPath.getAsString().split("~");
				if ("xml".equalsIgnoreCase(contentType)) {
					if (null != response.body().xmlPath().getString(iArrayOfiJSPath[0])) {
						iDataMap.put(iArrayOfiJSPath[1], response.body().xmlPath().getString(iArrayOfiJSPath[0]));
					} else {
						System.out.println("Data bank path = " + iArrayOfiJSPath[0] + "can't be found");
					}

				} else {
					iArrayOfiJSPath[0] = "'" + iArrayOfiJSPath[0] + "'";
					iArrayOfiJSPath[0] = iArrayOfiJSPath[0].replace(".", "'.'");
					if (null != response.body().path(iArrayOfiJSPath[0]).toString()) {
						iDataMap.put(iArrayOfiJSPath[1], response.body().path(iArrayOfiJSPath[0]).toString());
					} else {
						System.out.println("Data bank path = " + iArrayOfiJSPath[0] + "can't be found");
					}
				} // if
			} // for

			// save in global cache (databank)
			iGlobalDataBank.put(testId, iDataMap);
			System.out.println(iGlobalDataBank);
			break;

		case 3:
			// ********** Complexity 3 ***********
			// Verification of response against what is in actually found in the
			// database
			//
			System.out.println("Complexity 3 implementation is in progress, and not currently supported");
			break;

		default:
			// **** Complexity Not Supported *****
			//
			System.out.println("Complexity entered is currently not supported.");
			break;
		} // switch

		return response;
	} // executeRest

	/**
	 * @return String content type
	 */
	private String getContentType(JsonObject jsonObj) {

		if (jsonObj.has("headers")) {
			if (null != jsonObj.get("headers").getAsJsonObject().get("Content-Type").getAsString()) {
				return jsonObj.get("headers").getAsJsonObject().get("Content-Type").getAsString();
			} else {
				return "text/xml; charset=UTF-8";
			}
		} else {
			return "text/xml; charset=UTF-8";
		}

	}

	public void validateGetcibaAccountInfoResponse(Response response, Map<String, String> assertions) {

		for (Map.Entry<String, String> entry : assertions.entrySet()) {

			assertEquals(response.body().path(entry.getKey()), entry.getValue());

		}

	}

	public void validateResponse(Response response, Map<String, String> assertions, String testId) {

		String aKey, aVal;

		try {

			for (Map.Entry<String, String> entry : assertions.entrySet()) {
				aKey = entry.getKey();
				if ((!"path".equals(aKey) || !aKey.equals("identifier"))) {
					aVal = String.valueOf(entry.getValue());

					String[] arAVal = aVal.split("~");

					String expectedVal = getDataValue(arAVal[1]);
					
					String actualVal=null;

					if (null != response.body().path(aKey)){
						actualVal=response.body().path(aKey).toString().trim();
					} else {
						actualVal="null";
					}
					System.out.print("Element = " + aKey + ", Expected = " + arAVal[0] + "~" + expectedVal + " , Actual = "
							+ actualVal);
					asserts(String.valueOf(arAVal[0] + "~" + expectedVal), actualVal, testId);

				}
			}

		} catch (Exception e) {
			System.err.println("Test # = " + testId + " >> " + e.getMessage());
			System.out.println(assertions.get("path") + " is empty in the response, FAIL");
		}

	}

	public void validateResponseHeaders(Response response, Map<String, String> assertions, String testId) {

		String aKey, aVal;

		try {

			for (Map.Entry<String, String> entry : assertions.entrySet()) {
				aKey = entry.getKey();
				if (!"path".equals(aKey)) {
					aVal = String.valueOf(entry.getValue());

					String[] arAVal = aVal.split("~");

					String expectedVal = getDataValue(arAVal[1]);
					System.out.print("Header Element = " + aKey + ", Expected = " + arAVal[0] + "~" + expectedVal + " , Actual = "
							+ response.getHeaders().getValue(aKey).toString());

					asserts(String.valueOf(arAVal[0] + "~" + expectedVal), response.getHeaders().getValue(aKey).toString(), testId);
				}

			}

		} catch (Exception e) {
			System.err.println("Test # = " + testId + " >> " + e.getMessage());
			System.out.println(assertions.get("path") + " is empty in the response, FAIL");
		}

	}

	public void validateHttpResponseStatus(Response response, Map<String, String> assertions, String testId) {

		String aKey, aVal;

		try {

			for (Map.Entry<String, String> entry : assertions.entrySet()) {
				aKey = entry.getKey();
				if (!"path".equals(aKey)) {
					aVal = String.valueOf(entry.getValue());

					String[] arAVal = aVal.split("~");

					String expectedVal = getDataValue(arAVal[1]);
					System.out.print("Http Status Code = " + aKey + ", Expected = " + arAVal[0] + "~" + expectedVal + " , Actual = "
							+ response.getStatusCode());

					asserts(String.valueOf(arAVal[0] + "~" + expectedVal), Integer.toString(response.getStatusCode()), testId);
				}

			}

		} catch (Exception e) {
			System.err.println("Test # = " + testId + " >> " + e.getMessage());
			System.out.println(assertions.get("path") + " is empty in the response, FAIL");
		}

	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void validateResponse(Response response, Map<String, String> assertions, String type, String testId, String contentType) {

		String aKey, aVal;
		List<Map> devices = new ArrayList<Map>();
		String[] pathValue = assertions.get("path").split("~");

		if (pathValue[0].equals("ARRAY")) {

			try {
				XmlPath xmlPath = null;

				if ("xml".equalsIgnoreCase(contentType)) {

					xmlPath = new XmlPath(response.asString());
					NodeChildren nodeChildren = null;
					nodeChildren = xmlPath.get(pathValue[1]);
					List<Node> nodeList = nodeChildren.list();
					for (Node node : nodeList) {

						NodeChildren nodeChildrenOfNode = node.children();
						List<Node> nodeListOfnodeChildren = nodeChildrenOfNode.list();
						Map<String, String> tempMap = new HashMap<String, String>();
						for (Node iNode : nodeListOfnodeChildren) {
							tempMap.put(iNode.name(), iNode.value());
						}
						devices.add(tempMap);
					}

				} else {

					devices = response.body().jsonPath().getList(pathValue[1]);

				}

				int iCount = 0;
				for (Map<String, String> device : devices) {

					// System.out.println(device.values().toString());
					String strIdentifier = String.valueOf(assertions.get("identifier"));
					String[] arStrIdentifier = strIdentifier.split("~");

					if (StringUtils.isNotEmpty(device.get(arStrIdentifier[0]))) {
						if (device.get(arStrIdentifier[0]).equals(getDataValue(arStrIdentifier[1]))) {

							for (Map.Entry<String, String> entry : assertions.entrySet()) {

								aKey = entry.getKey();
								aVal = String.valueOf(entry.getValue());
								if (!("path".equals(aKey) || aKey.equals("identifier"))) {

									aVal = String.valueOf(entry.getValue());
									String[] arAVal = aVal.split("~");

									String expectedVal = getDataValue(arAVal[1]);

									System.out.print("Element = " + aKey + ", Expected = " + arAVal[0] + "~" + expectedVal + " , Actual = "
											+ String.valueOf(device.get(aKey)).trim());
									// assertEquals(device.get(aKey), aVal);

									asserts(String.valueOf(arAVal[0] + "~" + expectedVal), String.valueOf(device.get(aKey)), testId);
								}

							}
						} else {

							iCount++;
						}

					}

				}

				if (iCount == devices.size()) {
					System.out.println(assertions.get("path") + " with " + assertions.get("identifier") + " no match found, FAIL");
				}

			} catch (Exception e) {

				System.err.println("Test # = " + testId + " >> " + e.getMessage());

				System.out.println(assertions.get("path") + " is empty in the response, FAIL");
			}

		} else {

			// System.out.println("List >> False");

			try {

				Map<String, String> device = null;
				XmlPath xmlPath = null;
				if ("xml".equalsIgnoreCase(contentType)) {

					xmlPath = new XmlPath(response.asString()).setRoot(assertions.get("path"));
				} else {

					device = response.body().jsonPath().get(assertions.get("path"));
				}

				for (Map.Entry<String, String> entry : assertions.entrySet()) {

					aKey = entry.getKey();
					if (!("path".equals(aKey) || "identifier".equals(aKey))) {

						aVal = String.valueOf(entry.getValue());

						String[] arAVal = aVal.split("~");

						String expectedVal = getDataValue(arAVal[1]);

						if ("xml".equalsIgnoreCase(contentType)) {

							// <<<<<<< HEAD
							System.out.print("Element = " + aKey + ", Expected = " + arAVal[0] + "~" + expectedVal + " , Actual = "
									+ xmlPath.getString(aKey).trim());

							if ("HAS".equalsIgnoreCase(arAVal[0])) {
								// assertAdvanced(String.valueOf(arAVal[0] + "~"
								// + expectedVal), assertions.get("path") + "."
								// + aKey, response, testId);
								List<String> listActualValue = new ArrayList<>();
								listActualValue = xmlPath.getList(aKey);
								assertsAdvanced(String.valueOf(arAVal[0] + "~" + expectedVal), listActualValue, testId);
								//
								/* ======= System.out.print("Element = " + aKey + ", Expected = " + arAVal[0] + "~" + expectedVal +
								 * " , Actual = " + xmlPath.getString(aKey).trim()); if ("HAS".equalsIgnoreCase(arAVal[0])) {
								 * assertAdvanced(String.valueOf(arAVal[0] + "~" + expectedVal), assertions.get("path") + "." +
								 * aKey, response, testId); >>>>>>> 70c876e8fe4ef423bb2e398adf15fb5afa8eb97c */
							} else {
								asserts(String.valueOf(arAVal[0] + "~" + expectedVal), xmlPath.getString(aKey), testId);
							}
						} else {

							System.out.print("Element = " + aKey + ", Expected = " + arAVal[0] + "~" + expectedVal + " , Actual = "
									+ String.valueOf(device.get(aKey)).trim());
							asserts(String.valueOf(arAVal[0] + "~" + expectedVal), String.valueOf(device.get(aKey)), testId);
						}

					}
				}
			} catch (Exception e) {

				System.err.println("Test # = " + testId + " >> " + e.getMessage());
				System.out.println(assertions.get("path") + " is empty in the response, FAIL");
			}

		}

	}

	protected void asserts(String expectedValue, String actualValue, String testId) {

		String[] expectedPartsValue = expectedValue.split("~");

		try {
			switch (expectedPartsValue[0]) {

			case "EQUALS":
				assertEquals("Assert equals", expectedPartsValue[1].trim(), actualValue.trim());
				System.out.println(", PASS");
				break;

			case "CONTAINS":
				assertThat("Assert contains", actualValue.trim(), containsString(expectedPartsValue[1].trim()));
				System.out.println(", PASS");
				break;

			case "!CONTAINS":
				assertThat("Assert not contains", actualValue.trim(), not(containsString(expectedPartsValue[1].trim())));
				System.out.println(", PASS");
				break;

			case "HASCONTENT":
				if ("true".equals(expectedPartsValue[1].trim())) {
					assertNotNull("Assert has content - True", actualValue);
					System.out.println(", PASS");
				} else {
					// assertNull("Assert has content - False", actualValue);
					if (actualValue.equals("null")) {
						actualValue = "";
					}
					assertTrue("Assert has content - False", actualValue.isEmpty());
					System.out.println(", PASS");
				}

				break;

			}
			intPassCount++;

		} catch (AssertionError e) {
			// e.printStackTrace();
			System.err.println("Test # = " + testId + " >> " + e.getMessage());
			intFailCount++;
			System.out.println(", FAIL");

		}
	}

	protected void assertsAdvanced(String expectedValue, List<String> listActualValue, String testId) {

		String[] expectedPartsValue = expectedValue.split("~");

		try {
			switch (expectedPartsValue[0]) {

			case "EQUALS":
				assertEquals("Assert equals", expectedPartsValue[1].trim(), listActualValue.get(0).toString().trim());
				System.out.println(", PASS");
				break;

			case "CONTAINS":
				assertThat("Assert contains", listActualValue.get(0).toString().trim(), containsString(expectedPartsValue[1].trim()));
				System.out.println(", PASS");
				break;

			case "!CONTAINS":
				assertThat("Assert not contains", listActualValue.get(0).toString().trim(), not(containsString(expectedPartsValue[1].trim())));
				System.out.println(", PASS");
				break;

			case "HAS":
				List<String> tempListActualValue = new ArrayList<>();
				for (String str : listActualValue) {
					tempListActualValue.add(str.trim());

				}
				listActualValue = tempListActualValue;
				assertThat("Assert Has item", listActualValue, hasItem(expectedPartsValue[1].trim()));
				System.out.println(", PASS");
				break;

			case "HASCONTENT":
				if ("true".equals(expectedPartsValue[1].trim())) {
					assertNotNull("Assert has content - True", listActualValue.get(0).toString().trim());
					System.out.println(", PASS");
				} else {
					// assertNull("Assert has content - False",
					// listActualValue.get(0).toString().trim());
					assertTrue("Assert has content - False", listActualValue.get(0).toString().trim().isEmpty());
					System.out.println(", PASS");
				}

				break;

			}
			intPassCount++;

		} catch (AssertionError e) {
			// e.printStackTrace();
			System.err.println("Test # = " + testId + " >> " + e.getMessage());
			intFailCount++;
			System.out.println(", FAIL");

		}

	}

	public Map<String, Object> getmapYamlTestData() {
		return mapYamlTestData;
	}

	public void setmapYamlTestData(Map<String, Object> mapYamlTestData) {
		this.mapYamlTestData = mapYamlTestData;
	}

	@SuppressWarnings("unchecked")
	protected byte[] readFileContent(final String filePath, JsonObject jsonObj, String contentType) throws IOException {

		String resourceFldr = null;

		if ("xml".equalsIgnoreCase(contentType)) {
			resourceFldr = "src/e2eTest/resources/xml/";
		} else {
			resourceFldr = "src/e2eTest/resources/json/";
		}

		String fileString = new String(readAllBytes(get(resourceFldr + filePath)));

		Gson gson = new Gson();

		Map<String, String> mapTemplate = new HashMap<String, String>();
		mapTemplate = (Map<String, String>) gson.fromJson(jsonObj.getAsJsonObject(), mapTemplate.getClass());
		mapTemplate.put("userName", getStrUserName());
		mapTemplate.put("password", getStrPassword());

		for (Map.Entry<String, String> entry : mapTemplate.entrySet()) {

			if (!(String.valueOf(entry.getKey()).equals("assertion") || String.valueOf(entry.getKey()).equals("databank"))) {

				String strDataValue = String.valueOf(entry.getValue());

				strDataValue = getDataValue(strDataValue);

				fileString = StringUtils.replace(fileString, "[" + String.valueOf(entry.getKey()) + "]", strDataValue);

			}

		}

		System.out.println("Request: \n[" + fileString + "]");

		return fileString.getBytes();
	}

	public String buildURL(String url, JsonObject jsonObj) {

		if (url.contains("{")) {
			String[] arUrlSplit = url.split("\\{");
			arUrlSplit = arUrlSplit[1].split("\\}");

			String strPartUrl = arUrlSplit[0];
			String strDataValue = jsonObj.get(strPartUrl).toString();
			strDataValue = getDataValue(strDataValue);
			url = StringUtils.replace(url, "{" + strPartUrl + "}", strDataValue);

		}

		return url;

	}

	@SuppressWarnings("unchecked")
	public String refBuildURL(String url, JsonObject jsonObj) {

		/* if (url.contains("{")) { String[] arUrlSplit = url.split("\\{"); arUrlSplit = arUrlSplit[1].split("\\}"); String
		 * strPartUrl = arUrlSplit[0]; String strDataValue = jsonObj.get(strPartUrl).toString(); strDataValue =
		 * getDataValue(strDataValue); url = StringUtils.replace(url, "{" + strPartUrl + "}", strDataValue); } */

		Gson gson = new Gson();

		Map<String, String> hasMapTemplateForJson = new LinkedHashMap<String, String>();
		hasMapTemplateForJson = (Map<String, String>) gson.fromJson(jsonObj.getAsJsonObject(), hasMapTemplateForJson.getClass());

		for (Map.Entry<String, String> entry : hasMapTemplateForJson.entrySet()) {

			if (!(String.valueOf(entry.getKey()).equals("assertion") || String.valueOf(entry.getKey()).equals("databank"))) {

				String strDataValue = String.valueOf(entry.getValue());

				strDataValue = getDataValue(strDataValue);
				url = StringUtils.replace(url, "{" + String.valueOf(entry.getKey()) + "}", strDataValue);

			}

		}

		// System.out.println(url);

		return url;

	}

	protected String getDataValue(String strValue) {

		String retValue;

		if (!strValue.contains("|")) {
			retValue = strValue;
		} else {
			String[] arReqInput = strValue.split("\\|");
			arReqInput[0] = StringUtils.removeStart(arReqInput[0], "\"");
			arReqInput[1] = StringUtils.removeEnd(arReqInput[1], "\"");
			retValue = iGlobalDataBank.get(arReqInput[0]).get(arReqInput[1]);
		}

		return retValue;

	}

	@SuppressWarnings("unchecked")
	protected Map<String, ?> getHttpHeaders(String sourceSystemId, String trackingId, JsonObject jsonObj) {

		Gson gson = new Gson();

		Map<String, String> headers = new HashMap<String, String>();
		String authToken = null;

		/* String pingFederateEndPoint = null; if (getTestProperties().get("profile").contains("qa")) { pingFederateEndPoint =
		 * "https://websec-qa.cable.comcast.com"; } else if (getTestProperties().get("profile").contains("int")) {
		 * pingFederateEndPoint = "https://websec-int.cable.comcast.com"; } else if
		 * (getTestProperties().get("profile").contains("stg")) { pingFederateEndPoint = "https://websec-stg.cable.comcast.com"; }
		 * else if (getTestProperties().get("profile").contains("prd")) { pingFederateEndPoint = "https://websec.cable.comcast.com";
		 * } String url = "/as/token.oauth2"; String userName = null; String password = null; String scope= null; String authToken;
		 * if (null != jsonObj.get("userId")){ userName = jsonObj.get("userId").getAsString(); } else { userName =
		 * getTestProperties().get("userId"); } if (null != jsonObj.get("password")){ password =
		 * jsonObj.get("password").getAsString(); } else { password = getTestProperties().get("password"); } if (null !=
		 * jsonObj.get("scope")){ scope = jsonObj.get("scope").getAsString(); } else { scope = getTestProperties().get("scope"); } */

		headers.put("sourceSystemId", sourceSystemId);
		headers.put("trackingId", trackingId);

		if (jsonObj.has("headers")) {
			JsonElement js = jsonObj.get("headers");
			Map<String, String> mapHeadersTemplate = new LinkedHashMap<String, String>();
			mapHeadersTemplate = (Map<String, String>) gson.fromJson(js.getAsJsonObject(), mapHeadersTemplate.getClass());

			for (Map.Entry<String, String> entry : mapHeadersTemplate.entrySet()) {
				headers.put(entry.getKey(), getDataValue(String.valueOf(entry.getValue())));
			}

		}

		if ("json".equals(jsonObj.get("contentType").getAsString())) {
			headers.put("Content-Type", "application/json");
		}
		/* RestAssured.baseURI = pingFederateEndPoint; authToken = given().relaxedHTTPSValidation().request()
		 * .contentType("application/x-www-form-urlencoded; charset=UTF-8").body ("client_id=" + userName + "&client_secret=" +
		 * password + "&grant_type=client_credentials&scope=" + scope + "")
		 * .when().post(url).body().path("access_token").toString(); // headers.put("Authorization", "Bearer " + //
		 * System.getProperty("comcast.esp.oauth.token")); */

		if (jsonObj.has("authToken") || "json".equals(jsonObj.get("contentType").getAsString())) {
			authToken = getToken(jsonObj);
			headers.put("Authorization", "Bearer " + authToken);
		}

		return headers;
	}

	public Integer getIntPassCount() {
		return intPassCount;
	}

	public void setIntPassCount(Integer intPassCount) {
		this.intPassCount = intPassCount;
	}

	public Integer getIntFailCount() {
		return intFailCount;
	}

	public void setIntFailCount(Integer intFailCount) {
		this.intFailCount = intFailCount;
	}

	public Map<String, JsonObject> getiCheckHealthDataBank() {
		return iCheckHealthDataBank;
	}

	public void setiCheckHealthDataBank(Map<String, JsonObject> iCheckHealthDataBank) {
		this.iCheckHealthDataBank = iCheckHealthDataBank;
	}

	public void JsonXmlAssertion(Response response, Map<String, String> assertions, String testId) {

		String aKey, aVal;
		try {
			JSONObject jsonObject2 = new JSONObject();
			/* XmlPath xmlpath=new XmlPath(response.asInputStream()); String xmlMsg1=xmlpath.getString("Envelope");
			 * response.andReturn().peek().path("Envelope"); String xmlMsg2=xmlpath.get().getPath("Envelope").toString();
			 * xmlMsg1=StringUtils.replace(xmlMsg1, "xsi:nil=\"true\"", ""); xmlMsg1=StringUtils.replace(xmlMsg1,
			 * "xsi:type=\"ns2:CableModem\"", ""); */

			String xmlMsg = response.asString();
			xmlMsg = StringUtils.replacePattern(xmlMsg, "<SOAP-ENV:Envelope.*device/types\">", "");
			xmlMsg = StringUtils.replacePattern(xmlMsg, "</ns3:checkHealthResponse>.*</SOAP-ENV:Envelope>", "");
			// xmlMsg=StringUtils.replacePattern(xmlMsg, "xmlns.*\">",
			// ">").replaceAll("ns2:", "").replaceAll("ns3", "");
			xmlMsg = xmlMsg.replaceAll("ns2:", "").replaceAll("ns3:", "");
			xmlMsg = StringUtils.replace(xmlMsg, "xsi:nil=\"true\"", "");
			// xmlMsg=StringUtils.replace(xmlMsg, "xsi:type=\"ns2:CableModem\"",
			// "");

			jsonObject2 = XML.toJSONObject(xmlMsg);
			String jsonMsg = jsonObject2.toString();
			jsonMsg = jsonMsg.replaceAll("\\{\\}", "null");
			// jsonMsg = jsonMsg.replaceAll("\"\",", "null,");
			jsonMsg = jsonMsg.replaceAll("\"device\"", "\"devices\"");
			System.out.println("XML to JSON Response = " + jsonMsg);
			JsonObject jsonObj = new Gson().fromJson(jsonMsg, JsonObject.class);

			iCheckHealthDataBank.put(testId, jsonObj);

		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		/* try { for (Map.Entry<String, String> entry : assertions.entrySet()) { aKey = entry.getKey(); if (!"path".equals(aKey)) {
		 * aVal = String.valueOf(entry.getValue()); String[] arAVal = aVal.split("~"); String expectedVal = getDataValue(arAVal[1]);
		 * System.out.print("Http Status Code = " + aKey + ", Expected = " + arAVal[0] + "~" + expectedVal + " , Actual = " +
		 * response.getStatusCode()); asserts(String.valueOf(arAVal[0] + "~" + expectedVal),
		 * Integer.toString(response.getStatusCode()), testId); } } } catch (Exception e) { System.err.println("Test # = " + testId
		 * + " >> " + e.getMessage()); System.out.println(assertions.get("path") + " is empty in the response, FAIL"); } */

	}

	@SuppressWarnings("unchecked")
	public <T> List<T> parseCheckHealthResponse(Response response, Map<String, String> assertions, String testId) {

		String[] pathValue = assertions.get("path").split("~");

		List<Object> healthList = new ArrayList<>();
		List<HashMap> responseDeviceList = new ArrayList<>();
		List<JsonObject> soapDeviceList = new ArrayList<>();
		JsonArray soapDeviceArray = new JsonArray();
		JsonObject sDeviceList = new JsonObject();
		List<HashMap> sMapDeviceList = new ArrayList<>();

		if (pathValue[1].equals("ARRAY")) {
			responseDeviceList = response.body().jsonPath().getList(pathValue[2]);
		} else {
			responseDeviceList = response.body().jsonPath().getList(pathValue[1]);
		}

		for (HashMap tMapResponseDevice : responseDeviceList) {

			String jsonString = new Gson().toJson(tMapResponseDevice, LinkedHashMap.class);
			JsonObject tResponseDevice = new Gson().fromJson(jsonString, JsonObject.class);

			String strIdentifier = String.valueOf(assertions.get("identifier"));
			String[] arStrIdentifier = strIdentifier.split("~");
			System.out.println("Validating Device Type = " + arStrIdentifier[1]);
			if (tResponseDevice.get(arStrIdentifier[0]).getAsString().equals(arStrIdentifier[1])) {

				for (Map.Entry<String, String> entry : assertions.entrySet()) {

					String aKey = entry.getKey();
					String aVal = String.valueOf(entry.getValue());
					if ("healthCheckResults".equals(aKey)) {

						// healthList.add(tResponseDevice.get("health.healthCheckResults.healthCheckResults").getAsJsonArray());
						healthList.add((List<Map<String, String>>) JsonPath.from(tResponseDevice.toString())
								.get("health.healthCheckResults.healthCheckResults"));
						String[] aVals = aVal.split("\\|");
						// soapDeviceArray =
						// iCheckHealthDataBank.get(aVals[1]).get(pathValue[2]).getAsJsonArray();
						// soapDeviceArray =
						// JsonPath.from(iCheckHealthDataBank.get(aVals[1]).getAsString()).get(pathValue[2]).getAsJsonArray();

						if (pathValue[1].equals("ARRAY")) {
							sMapDeviceList = JsonPath.from(iCheckHealthDataBank.get(aVals[1]).toString()).getList(pathValue[2]);
						} else {
							sMapDeviceList.add(JsonPath.from(iCheckHealthDataBank.get(aVals[1]).toString()).get(pathValue[1]));
						}

					}

				}

			}

		}

		/* for (JsonElement tSoapDevice: soapDeviceArray){ String strIdentifier = String.valueOf(assertions.get("identifier"));
		 * String[] arStrIdentifier = strIdentifier.split("~"); if (tSoapDevice.getAsJsonObject().get(arStrIdentifier[0]).
		 * getAsString().equals(arStrIdentifier[1])){ if (tSoapDevice.getAsJsonObject().has("healthCheckResults")){
		 * healthList.add(tSoapDevice.getAsJsonObject().get( "health.healthCheckResults.healthCheckResult").getAsJsonArray()); } } } */

		for (HashMap tMapSoapDevice : sMapDeviceList) {

			String jsonString = new Gson().toJson(tMapSoapDevice, LinkedHashMap.class);
			JsonObject tResponseDevice = new Gson().fromJson(jsonString, JsonObject.class);

			String strIdentifier = String.valueOf(assertions.get("identifier"));
			String[] arStrIdentifier = strIdentifier.split("~");
			if (tResponseDevice.get(arStrIdentifier[0]).getAsString().equals(arStrIdentifier[1])) {

				for (Map.Entry<String, String> entry : assertions.entrySet()) {

					String aKey = entry.getKey();
					String aVal = String.valueOf(entry.getValue());
					if ("healthCheckResults".equals(aKey)) {

						// healthList.add(tResponseDevice.get("health.healthCheckResults.healthCheckResults").getAsJsonArray());
						healthList.add((List<Map<String, String>>) JsonPath.from(tResponseDevice.toString())
								.get("health.healthCheckResults.healthCheckResult"));
					}

				}

			}

		}

		/* } else { } */

		return (List<T>) healthList;

	}

	@SuppressWarnings("unchecked")
	public <T> void checkHealthValidation(List<T> healthList, String testId) {

		for (T t : healthList) {
			if (t instanceof ArrayList) {
				validateHealths((List<List<Map<String, String>>>) healthList, testId);
			}
		}

	}

	public void validateHealths(List<List<Map<String, String>>> healthList, String testId) {

		List<Map<String, String>> healthResponseJson = healthList.get(0);
		List<Map<String, String>> healthSoapJson = healthList.get(1);

		for (Map<String, String> tSoapJson : healthSoapJson) {

			System.out.println("Validating Health: " + String.valueOf(tSoapJson.get("description")));

			for (Map.Entry<String, String> soapEntry : tSoapJson.entrySet()) {

				String strDescription = String.valueOf(tSoapJson.get("description"));
				String strHierarcyId = String.valueOf(tSoapJson.get("hierarchyId"));
				for (Map<String, String> tResponseJson : healthResponseJson) {
					if (tResponseJson.get("description").equals(strDescription)
							&& String.valueOf(tResponseJson.get("hierarchyId")).equals(strHierarcyId)) {

						if (!String.valueOf(soapEntry.getKey()).equals("value")) {
							if (StringUtils.isBlank(String.valueOf(soapEntry.getValue()))) {
								System.out.print("Element = " + String.valueOf(soapEntry.getKey()) + ", Expected (SOAP) = " + "EQUALS~" + "null"
										+ " , Actual (REST) = " + String.valueOf(tResponseJson.get(soapEntry.getKey())));
								asserts("EQUALS~" + "null".toLowerCase(), String.valueOf(tResponseJson.get(soapEntry.getKey())).toLowerCase(),
										testId);
							} else {
								System.out.print("Element = " + String.valueOf(soapEntry.getKey()) + ", Expected (SOAP) = " + "EQUALS~"
										+ String.valueOf(soapEntry.getValue()) + " , Actual (REST) = "
										+ String.valueOf(tResponseJson.get(soapEntry.getKey())));
								asserts("EQUALS~" + String.valueOf(soapEntry.getValue()).toLowerCase(),
										String.valueOf(tResponseJson.get(soapEntry.getKey())).toLowerCase(), testId);
							}
						} else {
							System.out.print("Element = " + String.valueOf(soapEntry.getKey()) + ", Expected (REST) = " + "HASCONTENT~"
									+ String.valueOf(soapEntry.getValue()) + " , Actual (REST) = "
									+ String.valueOf(tResponseJson.get(soapEntry.getKey())));
							asserts("HASCONTENT~true", String.valueOf(tResponseJson.get(soapEntry.getKey())).toLowerCase(), testId);
						}

						break;
					}
				}

			}

		}

	}

	public Map<String, JsonElement> getIpropertyCanaryMap() {
		return ipropertyCanaryMap;
	}

	public void setIpropertyCanaryMap(Map<String, JsonElement> ipropertyCanaryMap) {
		this.ipropertyCanaryMap = ipropertyCanaryMap;
	}

	public Map<String, JsonElement> getIpropertyProdMap() {
		return ipropertyProdMap;
	}

	public void setIpropertyProdMap(Map<String, JsonElement> ipropertyProdMap) {
		this.ipropertyProdMap = ipropertyProdMap;
	}
	
	

}
