package com.falcons.jbt.yml;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang3.StringUtils;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.jayway.restassured.response.Response;

public class ExecuteRestScenarios {

	ExecuteAndValidate executeAndValidate = new ExecuteAndValidate();

	@SuppressWarnings("unchecked")
	public void execRestRequest() throws Exception {

		if (null != executeAndValidate.getmapYamlTestData()) {

			for (Entry<String, Object> entry : executeAndValidate.getmapYamlTestData().entrySet()) {

				String httpUrl;
				String httpMethod;
				String testFileName = "null";
				String contentType = null;
				int testDelay = 0;

				// process each testcase scenario
				String aKey = entry.getKey();
				Gson gson = new Gson();
				String jsonString = gson.toJson(entry.getValue(), LinkedHashMap.class);
				JsonObject jsonObj = new Gson().fromJson(jsonString, JsonObject.class);
				System.out.println("====================================================================================\n");
				System.out.println("RUNNING TEST # = " + aKey + ", TEST NAME = " + jsonObj.get("testname").getAsString() + ", TEST TYPE = "
						+ jsonObj.get("type").getAsString());

				// acquire required input for all scenarios: url, method (POST
				// or GET), and contentType (xml or json), delay (in
				// msec)
				httpUrl = jsonObj.get("url").getAsString();
				httpMethod = jsonObj.get("method").getAsString();
				contentType = jsonObj.get("contentType").getAsString();
				if (jsonObj.has("delay")) {
					testDelay = jsonObj.get("delay").getAsInt();
					System.out.println("******  DELAY SET ********\n");
				} else {
					testDelay = 0;
				}

				httpUrl = executeAndValidate.refBuildURL(httpUrl, jsonObj);
				// if POST, need to get the request template file
				// For GET, no need for request template file
				if (jsonObj.has("requestFileName")) {
					testFileName = jsonObj.get("requestFileName").getAsString();
				}

				// Do delay here if necessary before executing test
				Thread.sleep(testDelay);

				// execute soap/rest then save response
				Response response = executeAndValidate.executeRest(aKey, httpUrl, testFileName, jsonObj, httpMethod, contentType);

				System.out.println("Response Headers: \n\n[" + response.getHeaders().toString() + "] \n");

				System.out.println("Response Status: [" + response.getStatusCode() + " " + response.getStatusLine() + "] \n");

				System.out.println("Response: \n[" + response.asString() + "]\n");

				// assertEquals(response.getStatusCode(), HttpStatus.SC_OK);
				System.out.println("\nAssertions: ");

				// get the assertion key and element array
				// process each element array into a hash map, then validate
				// each element
				JsonArray asJsonArray = jsonObj.get("assertion").getAsJsonArray();
				for (JsonElement js : asJsonArray) {

					Map<String, String> mapAssertionTemplate = new LinkedHashMap<String, String>();
					mapAssertionTemplate = (Map<String, String>) gson.fromJson(js.getAsJsonObject(), mapAssertionTemplate.getClass());

					assertionFlowNew(contentType, aKey, response, mapAssertionTemplate);

				}
			}

		}

		else {
			System.err.println("ERROR: YML TEST SCENARIO FILE NOT FOUND");
		}

	}

	/**
	 * @param contentType
	 * @param aKey
	 * @param response
	 * @param mapAssertionTemplate
	 */
/*	private void assertionFlow(String contentType, String aKey, Response response, Map<String, String> mapAssertionTemplate) {

		if (StringUtils.isEmpty(mapAssertionTemplate.get("path")) || StringUtils.isBlank(mapAssertionTemplate.get("path"))) {
			executeAndValidate.validateResponse(response, mapAssertionTemplate, aKey);
		} else {

			if (mapAssertionTemplate.get("path").contains("CHECKHEALTH")) {

				List<Object> listOfHealths = executeAndValidate.parseCheckHealthResponse(response, mapAssertionTemplate, aKey);

				executeAndValidate.checkHealthValidation(listOfHealths, aKey);

			} else {

				if ("xmltojson".equals(mapAssertionTemplate.get("path"))) {

					executeAndValidate.JsonXmlAssertion(response, mapAssertionTemplate, aKey);

				} else {

					if ("httpResponseStatus".equals(mapAssertionTemplate.get("path"))) {

						executeAndValidate.validateHttpResponseStatus(response, mapAssertionTemplate, aKey);

					} else {
						if ("responseHeaders".equals(mapAssertionTemplate.get("path"))) {

							executeAndValidate.validateResponseHeaders(response, mapAssertionTemplate, aKey);

						} else {
							if (mapAssertionTemplate.get("path") != null) {

								executeAndValidate.validateResponse(response, mapAssertionTemplate, mapAssertionTemplate.get("path"), aKey,
										contentType);
							} else {
								executeAndValidate.validateResponse(response, mapAssertionTemplate, aKey);
							}

						}

					}
				}
			}

		}
	}*/
	
	
	/**
	 * @param contentType
	 * @param aKey
	 * @param response
	 * @param mapAssertionTemplate
	 */

	private void assertionFlowNew(String contentType, String aKey, Response response, Map<String, String> mapAssertionTemplate) {

		if (StringUtils.isEmpty(mapAssertionTemplate.get("path")) || StringUtils.isBlank(mapAssertionTemplate.get("path"))) {
			executeAndValidate.validateResponse(response, mapAssertionTemplate, aKey);
		} else if (mapAssertionTemplate.get("path").contains("CHECKHEALTH")) {

			List<Object> listOfHealths = executeAndValidate.parseCheckHealthResponse(response, mapAssertionTemplate, aKey);

			executeAndValidate.checkHealthValidation(listOfHealths, aKey);

		} else if ("xmltojson".equals(mapAssertionTemplate.get("path"))) {

			executeAndValidate.JsonXmlAssertion(response, mapAssertionTemplate, aKey);

		} else if ("httpResponseStatus".equals(mapAssertionTemplate.get("path"))) {

			executeAndValidate.validateHttpResponseStatus(response, mapAssertionTemplate, aKey);

		} else if ("responseHeaders".equals(mapAssertionTemplate.get("path"))) {

			executeAndValidate.validateResponseHeaders(response, mapAssertionTemplate, aKey);

		} else if (mapAssertionTemplate.get("path").contains("propertiesValidation")) {

			ValidationConfigProperties validationConfigProperties = new ValidationConfigProperties();

			if (mapAssertionTemplate.get("path").contains("canary")) {
				executeAndValidate.setIpropertyCanaryMap(validationConfigProperties.parseConfigProps(response));
			} else if (mapAssertionTemplate.get("path").contains("production")) {
				executeAndValidate.setIpropertyProdMap(validationConfigProperties.parseConfigProps(response));
				validationConfigProperties.validateConfigProps(executeAndValidate.getIpropertyCanaryMap(), executeAndValidate.getIpropertyProdMap(), aKey);
			} else {
				System.out.println("Nothing to parse");
			}
			// executeAndValidate.validateResponseHeaders(response, mapAssertionTemplate, aKey);

		} else if (mapAssertionTemplate.get("path") != null) {

			executeAndValidate.validateResponse(response, mapAssertionTemplate, mapAssertionTemplate.get("path"), aKey, contentType);
		} else {
			executeAndValidate.validateResponse(response, mapAssertionTemplate, aKey);
		}

	}

}
