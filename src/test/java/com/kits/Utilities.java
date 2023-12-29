package com.kits;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

public class Utilities {
	
	static JSONObject json = new JSONObject();
	static JSONObject item = new JSONObject();
	
	public static boolean compareJsons(Object actualJsonElement, Object expectedJsonElement) {
		List<Boolean> values = new ArrayList<Boolean>();
		
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
							item.put("Expected value", expectedKeyValue);
							item.put("Actual value", actualKeyValue);
							json.put(key1, item);
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

}
