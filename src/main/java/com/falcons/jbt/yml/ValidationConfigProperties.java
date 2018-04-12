package com.falcons.jbt.yml;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import com.jayway.restassured.response.Response;

public class ValidationConfigProperties {

	protected Map<String, JsonElement> parseConfigProps(Response response) {

		Map<String, JsonElement> configPropsMap = new LinkedHashMap<>();

		JsonObject respJsonObject = new Gson().fromJson(response.body().asString(), JsonObject.class);

		String appName = respJsonObject.getAsJsonObject("applicationConfig: [classpath:/application.properties]").get("service.name").getAsString();
		String appProfileName = respJsonObject.getAsJsonObject("systemEnvironment").get("SPRING_PROFILES_ACTIVE").getAsString();

		Set<Entry<String, JsonElement>> jsonEntries = respJsonObject
				.getAsJsonObject("configService:https://github.comcast.com/TeamG/config-properties.git/" + appName + "/" + appName + "-"
						+ appProfileName + ".properties")
				.entrySet();

		for (Map.Entry<String, JsonElement> jsonEntry : jsonEntries) {
			configPropsMap.put(jsonEntry.getKey(), jsonEntry.getValue());
		}

		jsonEntries = respJsonObject.getAsJsonObject("applicationConfig: [classpath:/application.properties]").entrySet();

		for (Map.Entry<String, JsonElement> jsonEntry : jsonEntries) {

			if (!configPropsMap.containsKey(jsonEntry.getKey())) {
				configPropsMap.put(jsonEntry.getKey(), jsonEntry.getValue());
			}
		}
		return configPropsMap;

	}

	protected void validateConfigProps(Map<String, JsonElement> iCanaryMap, Map<String, JsonElement> iProdMap, String testCaseId) {

		Set<String> iKeySet = iCanaryMap.keySet();
		Iterator<String> iTerator = iKeySet.iterator();
		while (iTerator.hasNext()) {
			String iKey = iTerator.next();
			if (iProdMap.containsKey(iKey)) {
				System.out.print("Property Name = " + iKey + ", Canary value = " + iCanaryMap.get(iKey).getAsString() + ", Production value = "
						+ iProdMap.get(iKey).getAsString());
				if (iCanaryMap.get(iKey).getAsString().equals(iProdMap.get(iKey).getAsString())) {
					System.out.println(", MATCH");
				} else {
					System.out.println(", NO MATCH");
					System.err.println("TEST # " + testCaseId + ", Property Name = " + iKey + ", Canary value = " + iCanaryMap.get(iKey).getAsString()
							+ ", Production value = " + iProdMap.get(iKey).getAsString() + ", NO MATCH");
				}
			} else {
				System.out.println(
						"Property Name = " + iKey + ", Canary value = " + iCanaryMap.get(iKey).getAsString() + ", Production value = NOT PRESENT");
				System.err.println("TEST # " + testCaseId + ", Property Name = " + iKey + ", Canary value = " + iCanaryMap.get(iKey).getAsString()
						+ ", Production value = NOT PRESENT");
			}

		}

		iKeySet = iProdMap.keySet();
		iTerator = iKeySet.iterator();
		while (iTerator.hasNext()) {
			String iKey = iTerator.next();
			if (!iCanaryMap.containsKey(iKey)) {
				System.out.println(
						"Property Name = " + iKey + ", Canary value  = NOT PRESENT , Production value = " + iProdMap.get(iKey).getAsString());
				System.err.println(
						"TEST # " + testCaseId + ", Property Name = " + iKey + ", Canary value  = NOT PRESENT , Production value = " + iProdMap.get(iKey).getAsString());

			}

		}
	}

}
