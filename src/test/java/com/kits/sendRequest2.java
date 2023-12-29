package com.kits;

import static org.testng.Assert.assertEquals;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.hamcrest.core.Is;
import org.hamcrest.Matcher;
import org.hamcrest.Matchers;
import static org.hamcrest.CoreMatchers.not;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.testng.Assert;
import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.Status;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.jayway.jsonpath.JsonPath;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.path.xml.XmlPath;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;


public class sendRequest2 {

	//static RequestSpecification request = RestAssured.given();
	static JsonParser parser = new JsonParser();
	static JSONObject actualResponseValueObj;
	static JSONObject expectedResponseValueObj;
	static public String id = null;
	static List<String> list = new ArrayList<String>();
	static List<Boolean> values = new ArrayList<Boolean>();
	static boolean areTheyEqual;

	@Test
	public static int testResponse(
			String reqUrl, 
			String methodName, 
			double expectedCode1, 
			String jsonBody,
			String queryParams, 
			String formParams,
			String headers,
			String expectedResponseValue,
			String getValueFromResponse,
			String removeKey,
			String requestType,
			String expectedXMLResponse,
			String fileName,
			String sheetName,
			ExtentReports extent,
			HashMap<String, String> context,
			ExtentTest logger2) throws ParseException, JsonMappingException, JsonProcessingException, InterruptedException, TimeoutException {

		int expectedCode = Integer.valueOf((int) Math.round(expectedCode1));
		String actualResponseValue = null;

		if (!jsonBody.equals("NA") && !requestType.equals("XML")) {
			JSONObject jsonobj = new JSONObject(jsonBody);
		}

		Response resp = null;
		int code = 0;

		if(reqUrl.contains("$transactionId$")){
			reqUrl = reqUrl.replace("$transactionId$", context.get("transactionId"));
		}

		RestAssured.baseURI = reqUrl;
		RequestSpecification request = RestAssured.given();

		//ExtentTest logger2 = extent.createTest(methodName + " " + reqUrl + " || " + fileName+"_"+sheetName);

		setQueryParam(request,queryParams,context);
		setFormParam(request,formParams,context);
		setHeaders(request,headers,context);
		setRequestBody(request,jsonBody,context);

		if(!requestType.equals("XML")) {
			if (methodName.equalsIgnoreCase("get")) {
				//resp = request.request().contentType(ContentType.TEXT).get(RestAssured.baseURI);
				resp = request.request().get(RestAssured.baseURI);
				System.out.println("res code :- "+resp.getStatusCode());
			}else if (methodName.equalsIgnoreCase("postauthapi")) {
				resp = request.request().post(RestAssured.baseURI);
				System.out.println("test");
			} else if (methodName.equalsIgnoreCase("post")) {
				resp = request.post(RestAssured.baseURI);
				System.out.println("test");
			} else if (methodName.equalsIgnoreCase("put")) {
				resp = request.put(RestAssured.baseURI);
			} else if (methodName.equalsIgnoreCase("delete")) {
				resp = request.delete(RestAssured.baseURI);
			}
		}else {
			if (methodName.equalsIgnoreCase("get")) {
				resp = request.request().get(RestAssured.baseURI);
				System.out.println("res code :- "+resp.getStatusCode());
			}else if (methodName.equalsIgnoreCase("postauthapi")) {
				resp = request.request().post(RestAssured.baseURI);
				System.out.println("test");
			} else if (methodName.equalsIgnoreCase("post")) {
				resp = request.contentType(ContentType.XML).accept(ContentType.XML).post(RestAssured.baseURI);
				String xml = request.contentType(ContentType.XML).accept(ContentType.XML).post(RestAssured.baseURI).andReturn().asString();   
				System.out.println(xml);
				validateXMLResponse(resp,expectedXMLResponse);
			} else if (methodName.equalsIgnoreCase("put")) {
				resp = request.put(RestAssured.baseURI);
			} else if (methodName.equalsIgnoreCase("delete")) {
				resp = request.delete(RestAssured.baseURI);
			}
		}

		System.out.println(resp);

		if(resp == null) {
			return -1;
		}

		if (resp != null) {
			code = resp.getStatusCode();
			if(code == 404 || code == 400) {
				return -1;
			}
		}

		//		try {
		//			resp.then().assertThat().body("page", Is.is(1));
		//			resp.then().assertThat().body("data[0].first_name", Is.is("George"));
		//			resp.then().assertThat().body("data",Is.is(not(Matchers.hasSize(0))));
		//		}catch(AssertionError e) {
		//			System.out.println(e.getMessage());
		//			return -1;
		//		}


		if(!getValueFromResponse.equals("NA")){
			actualResponseValue = resp.getBody().asString();
			String[] responseValuesArray = getValueFromResponse.split("&");
			for(int i=0; i<responseValuesArray.length; i++){
				String responseValue[] = responseValuesArray[i].split("=");
				String keyToStoreValueFromResp = responseValue[0];
				String keyToSearchInResp = responseValue[1];
				String value = getValueFromResponse(keyToSearchInResp, actualResponseValue);
				System.out.println(value);
				try{
					context.put(keyToStoreValueFromResp, value);
				}catch(Exception e){
					System.out.println("Key: " +keyToSearchInResp+ " is not present in the response: " +actualResponseValue);
				}
			}
		}

		if(!expectedResponseValue.equals("NA")){
			actualResponseValue = resp.getBody().asString();
			try {
				actualResponseValueObj =new JSONObject(actualResponseValue);
			}catch(Exception e) {
				if(!requestType.equals("XML")) {
					logger2.log(Status.FAIL, "<span style='color:red;'>" +
							" <b>Failed reason: </b>Actual Response is not in JSON format. <br />"
							+ "</span>"
							+ "<br /><b>Actual response code:</b> " + code
							+ "<br /><b>Response time: </b> " + resp.getTime()
							+ "<br /><b>Actual Response Body: </b>" + resp.asString()   
							);
					return -1;
				}
			}
			expectedResponseValueObj = new JSONObject(expectedResponseValue);

			if(!expectedResponseValue.equals("NA")){
				//actualResponseValue = ignoreKeysForValidation(actualResponseValueObj, expectedResponseValueObj).toString();

				expectedResponseValueObj = new JSONObject(expectedResponseValue);
				//expectedResponseValue = ignoreKeysForValidation(expectedResponseValueObj, expectedResponseValueObj).toString();
				//actualResponseValue= removeElement(removeKey, actualResponseValueObj).toString();
				areTheyEqual = Utilities.compareJsons(actualResponseValueObj, expectedResponseValueObj);
				System.out.println(areTheyEqual);
			}
		}


		System.out.println(methodName + " method :" + code);

		System.out.println("Context: " +context);

		if(!expectedResponseValue.equals("NA")) {
			//ObjectMapper mapper = new ObjectMapper();
			//JsonNode tree1 = mapper.readTree(expectedResponseValue);
			//JsonNode tree2 = mapper.readTree(actualResponseValue);
			//boolean areTheyEqual = tree1.equals(tree2);

			if (code == Math.round(expectedCode)) {
				logger2.log(Status.PASS,"<span style='color:green;'>" +
						"</b>Actual Response code is equal to Expected response code.<br />"
						+ "</span>"
						+ " <b>Expected Response code:</b> " + expectedCode
						+ "<br /><b>Actual response code:</b> " + code
						+ "<br /><b>Response time: </b>" + resp.getTime()
						+ "<br /><b>Requested Body: </b>" + jsonBody
						);
			} else {
				logger2.log(Status.FAIL, "<span style='color:red;'>" +
						" <b>Failed reason: </b>Actual Response code is not equal to Expected response code.<br />"
						+ "</span>"
						+ "<b>Expected Response code:</b> " + expectedCode
						+ "<br /><b>Actual response code:</b> " + code
						+ "<br /><b>Response time: </b> " + resp.getTime()
						+ "<br /><b>Requested Body: </b>" + jsonBody
						);
				return -1;
			}

			if(areTheyEqual) {
				logger2.log(Status.PASS,"<span style='color:green;'>" +
						"</b>Actual Response is equal to Expected response.<br />"
						+ "</span>"
						+ "<br /><b>Expected Response Body: </b>" + expectedResponseValue
						+ "<br /><b>Actual Response Body: </b>" + resp.asString()
						);
				return 0;
			}else {
				logger2.log(Status.FAIL, "<span style='color:red;'>" +
						" <b>Failed reason: </b>Actual Response is not equal to Expected response.<br />"
						+ "</span>"
						+ "<br /><b>Difference in following values: </b>"
						+ Utilities.json
						+ "<br />"
						+ "<br /><b>Expected Response Body: </b>" + expectedResponseValue
						+ "<br /><b>Actual Response Body: </b>" + resp.asString()
						);
				return -1;
			}
		}else{
			if (code == Math.round(expectedCode)) {
				logger2.log(Status.PASS,
						" <b>Expected Response code:</b> " + expectedCode
						+ "<br /><b>Actual response code:</b> " + code
						+ "<br /><b>Response time: </b>" + resp.getTime()
						+ "<br /><b>Requested Body: </b>" + jsonBody
						+ "<br /><b>Expected Response Body: </b>" + expectedResponseValue
						+ "<br /><b>Actual Response Body: </b>" + resp.asString()
						);
				if(reqUrl.contains("reqres.in/api/users123")) {
					boolean status = checkJobStatus(resp);
					if(status == false) {
						logger2.log(Status.FAIL, "<span style='color:red;'>" +
								" <b>Failed reason: </b>Either one of the generic or job steps or activities status is not SUCCESS.<br />"
								);
						return -1;
					}else {
						logger2.log(Status.PASS,
								" <b>All status: Generic, job steps and activities are SUCCESS.<br />"
								);
						return 0;
					}
				}
				return 0;
			} else {
				logger2.log(Status.FAIL, "<span style='color:red;'>" +
						" <b>Failed reason: </b>Response code is not equal to Expected code.<br />"
						+ "</span>"
						+ "<b>Expected Response code:</b> " + expectedCode
						+ "<br /><b>Actual response code:</b> " + code
						+ "<br /><b>Response time: </b> " + resp.getTime()
						+ "<br /><b>Requested Body: </b>" + jsonBody
						+ "<br /><b>Expected Response Body: </b>" + expectedResponseValue
						+ "<br /><b>Actual Response Body: </b>" + resp.asString()
						);
				return -1;
			}

		}

	}

	//	private static boolean checkJobStatus(Response resp) {
	//		try {
	//			io.restassured.path.json.JsonPath jsnPath = resp.jsonPath();
	//			String status = jsnPath.getString("status");
	//			System.out.println("Generic status is: " +status);
	//			if(status.equalsIgnoreCase("SUCCESS")) {
	//				resp.then().assertThat().body("job_step",Is.is(not(Matchers.hasSize(0))));
	//				int s = jsnPath.getInt("job_step.size()");
	//				for(int i = 0; i < s; i++) {
	//					String jobStepStatus = jsnPath.getString("job_step["+i+"].status");
	//					System.out.println("Status of job_step["+i+"] is: " +jobStepStatus);
	//					if(jobStepStatus.equalsIgnoreCase("SUCCESS")) {
	//						int t = jsnPath.getInt("job_step["+i+"].activities");
	//						for(int j = 0; j < t; j++) {
	//							String activityStatus = jsnPath.getString("activities["+j+"].status");
	//							System.out.println("Status of activities["+j+"] of job_step["+i+"] is: " +jobStepStatus);
	//							if(activityStatus.equalsIgnoreCase("SUCCESS")) {
	//								continue;
	//							}else {
	//								return false;
	//							}
	//						}
	//					}else {
	//						return false;
	//					}
	//				}
	//			}else {
	//				return false;							
	//			}
	//		}catch(AssertionError e) {
	//			System.out.println(e.getMessage());
	//			return false;
	//		}
	//		return true;
	//
	//	}


	public static void validateXMLResponse(Response response, String expectedXMLResponse)
	{
		try{
			if(!expectedXMLResponse.equals("NA")){
				XmlPath responsebody = new XmlPath(response.asString());
				String key = null,expectedValue = null;
				String[] expectedRespArray = expectedXMLResponse.split("&");
				for(int i=0; i<expectedRespArray.length; i++){
					System.out.println(expectedRespArray[i]);
					String queryParam[] = expectedRespArray[i].split("=");
					key = queryParam[0];
					expectedValue = queryParam[1];
					String actualValue = responsebody.getString(key);
					if(!actualValue.equalsIgnoreCase(expectedValue)) {
						Assert.fail("Actual value: "+actualValue+" is not equal to expected value: "+expectedValue);
					}else {
						System.out.println("Actual value: "+actualValue+" is equal to expected value: "+expectedValue);
					}

				}
			}
		}catch(Exception e){
			System.out.println("Exception in validateXMLResponse:"+e);
		}
	}

	//Sets Request Body
	public static RequestSpecification setRequestBody(RequestSpecification request,String jsonBody, HashMap<String, String> context){
		try{
			if(!jsonBody.equals("NA")) {
				String key = null, value = null;
				String[] jsonBodyArray = jsonBody.split(",");
				for (int i = 0; i < jsonBodyArray.length; i++) {
					String jsonBodyParam[] = jsonBodyArray[i].split(":");
					key = jsonBodyParam[0];
					value = jsonBodyParam[1];
					if (value.contains("$")) {
						String valueToBeReplaced = value.substring(value.indexOf("$") + 1, value.lastIndexOf("$"));
						jsonBody = jsonBody.replace(valueToBeReplaced, context.get(valueToBeReplaced)).replace("$", "");
					}
				}
				request.body(jsonBody);
			}
			return request;
		} catch(Exception e){
		}
		return request;
	}

	//Sets query parameter
	public static RequestSpecification setQueryParam(RequestSpecification request,String queryParams, HashMap<String, String> context){
		try{
			if(!queryParams.equals("NA")){
				String key = null,value = null;
				String[] queryParamsArray = queryParams.split("&");
				for(int i=0; i<queryParamsArray.length; i++){
					String queryParam[] = queryParamsArray[i].split("=");
					key = queryParam[0];
					value = queryParam[1];
					if(value.contains("$")){
						int index = value.indexOf("$");
						value=context.get(value.substring(index+1));
					}
					request.queryParam(key,value);
				}
			}
			return request;
		}catch(Exception e){
			System.out.println("Exception in setQueryParam:"+e);
		}
		return request;
	}

	//Sets form parameter
	public static RequestSpecification setFormParam(RequestSpecification request,String formParams, HashMap<String, String> context){
		try{
			if(!formParams.equals("NA")){
				String key = null,value = null;
				//String[] formParamsArray = formParams.split("&");
				String[] formParamsArray = formParams.split("\\r?\\n");
				for(int i=0; i<formParamsArray.length; i++){
					String formParam[] = formParamsArray[i].split("=");
					key = formParam[0];
					value = formParam[1];
					if(value.contains("$")){
						String valueToBeReplaced = value.substring(value.indexOf("$")+1,value.lastIndexOf("$"));
						value = value.replace(valueToBeReplaced, context.get(valueToBeReplaced)).replace("$", "");
					}
					request.formParam(key,value);
				}
			}
			return request;
		}catch(Exception e){
			System.out.println("Exception in setFormParam:"+e);
		}
		return request;
	}


	//Sets headers
	public static RequestSpecification setHeaders(RequestSpecification request, String headers, HashMap<String, String> context){
		try{
			if(!headers.equals("NA")){
				Map<String,Object> headerMap = new HashMap<String,Object>();
				String[] headersArray = headers.split("&");
				for(int i=0; i<headersArray.length; i++){
					String header[] = headersArray[i].split("=");
					headerMap.put(header[0], header[1]);
				}
				request.headers(headerMap);
			}
			return request;
		}catch(Exception e){
			System.out.println("Exception in setHeaders:"+e);
		}
		return request;
	}

	private static void check(String key, JsonElement jsonElement) {

		if (jsonElement.isJsonArray()) {
			for (JsonElement jsonElement1 : jsonElement.getAsJsonArray()) {
				check(key, jsonElement1);
			}
		} else {
			if (jsonElement.isJsonObject()) {
				Set<Map.Entry<String, JsonElement>> entrySet = jsonElement
						.getAsJsonObject().entrySet();
				for (Map.Entry<String, JsonElement> entry : entrySet) {
					String key1 = entry.getKey();
					if (key1.equals(key)) {
						list.add(entry.getValue().toString());
					}
					check(key, entry.getValue());
				}
			} else {
				if (jsonElement.toString().equals(key)) {
					list.add(jsonElement.toString());
				}
			}
		}
	}

	private static String getValueFromResponse(String key, String json) {
		String value = JsonPath.read(json, key);
		return value;
	}

	private static JSONObject removeElement(String key, Object jsonElement) {
		String key1 = null;
		try{
			if(((JSONObject) jsonElement).has(key)){
				((JSONObject)jsonElement).remove(key);
			}

			Iterator<String> keys1 = ((JSONObject) jsonElement).keys();

			while(keys1.hasNext()) {
				key1 = keys1.next();
				System.out.println(key1);
				System.out.println("-----------");


				if (((JSONObject) jsonElement).get(key1) instanceof JSONArray) {
					JSONArray getArray = ((JSONObject) jsonElement).getJSONArray(key1);
					for(int i = 0; i < getArray.length(); i++){
						Object object = getArray.get(i);
						removeElement(key, object);
					}
				} else {
					if (((JSONObject) jsonElement).get(key1) instanceof JSONObject) {
						JSONObject obj = (JSONObject) ((JSONObject) jsonElement).get(key1);
						//					JSONArray getArray1 = obj.toJSONArray(obj.names());
						//					for(int i = 0; i < getArray1.length(); i++){
						//						Object object = getArray1.get(i);
						removeElement(key, obj);
						//					}
					}
					else {
						System.out.println("Simple key");
					}
				}

			}

			return (JSONObject) jsonElement;
		} catch(Exception e){
			System.out.println("Error for key"+key1);
		}
		return (JSONObject) jsonElement;

	}

	public static String hitUserAuthAPI(String authApiURL) {
		// TODO Auto-generated method stub
		return null;
	}


	public static boolean compareJsons(Object actualJsonElement, Object expectedJsonElement) {
		String key1 = null;
		boolean areEqual = true;

		try{
			Iterator<String> keys1 = ((JSONObject) expectedJsonElement).keys();
			while(keys1.hasNext()) {
				key1 = keys1.next();
				System.out.println(key1);
				System.out.println("-----------");


				if (((JSONObject) expectedJsonElement).get(key1) instanceof JSONArray) {
					JSONArray getArray1 = ((JSONObject) expectedJsonElement).getJSONArray(key1);
					JSONArray getArray2 = ((JSONObject) actualJsonElement).getJSONArray(key1);
					for(int i = 0; i < getArray1.length(); i++){
						Object object1 = getArray1.get(i);
						Object object2 = getArray2.get(i);
						compareJsons(object2,object1);
					}
				} else {
					if (((JSONObject) expectedJsonElement).get(key1) instanceof JSONObject) {
						JSONObject obj1 = (JSONObject) ((JSONObject) expectedJsonElement).get(key1);
						JSONObject obj2 = (JSONObject) ((JSONObject) actualJsonElement).get(key1);
						compareJsons(obj2,obj1);
					}
					else {
						System.out.println("Simple key");

						String expectedKeyValue = ((JSONObject) expectedJsonElement).get(key1).toString();
						String actualKeyValue = ((JSONObject) actualJsonElement).get(key1).toString();

						if(!expectedKeyValue.equals(actualKeyValue)) {
							if(expectedKeyValue.equals("IGNORE_FIELD")){
								continue;
							}
							values.add(false);
						}
					}
				}

			}

		} catch(Exception e){
			System.out.println(e.getMessage());
		}
		if(values.contains(false)) {
			areEqual = false;
		}
		return areEqual;

	}


	private static boolean checkJobStatus(Response resp) {
		try {
			io.restassured.path.json.JsonPath jsnPath = resp.jsonPath();
			String status = jsnPath.getString("status");
			System.out.println("Generic status is: " +status);
			if(status.equalsIgnoreCase("SUCCESS")) {
				resp.then().assertThat().body("job_step",Is.is(not(Matchers.hasSize(0))));
				int s = jsnPath.getInt("job_step.size()");
				for(int i = 0; i < s; i++) {
					String job_name = jsnPath.getString("job_step["+i+"].job_name");
					if(job_name.equalsIgnoreCase("Orchestration")) {
						String jobStepStatus = jsnPath.getString("job_step["+i+"].status");
						if(jobStepStatus.equalsIgnoreCase("SUCCESS")  || jobStepStatus.equalsIgnoreCase("WARNING")) {
							continue;
						}else {
							return false;
						}
					}
					if(job_name.equalsIgnoreCase("Akka Wage Payment processing")) {
						String jobStepStatus = jsnPath.getString("job_step["+i+"].status");
						if(jobStepStatus.equalsIgnoreCase("SUCCESS") || jobStepStatus.equalsIgnoreCase("WARNING")) {
							int t = jsnPath.getInt("job_step["+i+"].activities");
							for(int j = 0; j < t; j++) {
								String activity_name = jsnPath.getString("activities["+j+"].activity_name");
								if(activity_name.equalsIgnoreCase("Zip file")) {
									String activityStatus = jsnPath.getString("activities["+j+"].status");
									System.out.println("Status of activities["+j+"] of job_step["+i+"] is: " +jobStepStatus);
									if(activityStatus.equalsIgnoreCase("SUCCESS") || activityStatus.equalsIgnoreCase("WARNING")) {
										continue;
									}else {
										return false;
									}
								}
							}
						}else {
							return false;
						}
					}
				}
			}else {
				return false;							
			}

		}catch(AssertionError e) {
			System.out.println(e.getMessage());
			return false;
		}
		return true;

	}


}
