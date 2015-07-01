package com.ibm.watson;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.apache.wink.json4j.JSONObject;



@Path("/getnews")
public class AlchemyNews {
	String apiURL = "/calls/data/GetNews";
	String Service_Name = "user-provided";

	@POST
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	@Produces(MediaType.APPLICATION_JSON)
	public String getInformation(@FormParam("start") String start,
			@FormParam("end") String end,
			@FormParam("outputMode") String outputMode,
			@FormParam("maxResults") String maxResults,
			@FormParam("timeSlice") String timeSlice,
			@FormParam("qEntity") String qEntity,
			@FormParam("qSentiment") String qSentiment,
			@FormParam("returns") String returns) {

		HttpURLConnection conn = null;
		try {
			// Get the service endpoint details

			// 'VCAP_APPLICATION' is in JSON format, it contains useful
			// information
			// about a deployed application
			// 'VCAP_SERVICES' contains all the credentials of services bound to
			// this application.
			// String VCAP_SERVICES = System.getenv("VCAP_SERVICES");

			// Find my service from VCAP_SERVICES in Bluemix
			
			
			
			
			String VCAP_SERVICES = System.getenv("VCAP_SERVICES"); 
			//System.out.println("vcapservice is : " + VCAP_SERVICES);
			JSONObject serviceInfo = new JSONObject(VCAP_SERVICES);
			//System.out.println("Service Info is: " + serviceInfo);
			
			// Get the Service Credentials for Watson AlchemyAPI
			JSONObject credentials = serviceInfo.getJSONArray(Service_Name)
					.getJSONObject(0).getJSONObject("credentials");


			String serverURL = credentials.getString("url");
			String apikey = credentials.getString("apikey");
			
			// Prepare the HTTP connection to the service
			String query = buildHttpGetData(apikey, outputMode, start, end, maxResults, timeSlice, qEntity, qSentiment, returns);
			conn = (HttpURLConnection) new URL(serverURL + apiURL + "?" + query)
					.openConnection();
			conn.setRequestMethod("GET");

			
			// make the connection
			conn.connect();
			// Read the response from the service
			BufferedReader br = new BufferedReader(new InputStreamReader(
					(conn.getInputStream())));
			StringBuilder sb = new StringBuilder();
			String output;
			while ((output = br.readLine()) != null) {
				sb.append(output);
			}
			br.close();
			//System.out.println("The result is: " + sb.toString());
			// Return the response from the service
			return sb.toString();
		} catch(Exception e){
			e.printStackTrace();
			return "{" + "\"status\": \"ERROR\", \"statusInfo\": \"" + e.getClass().getName() + "\"}";
		}finally {
			try {
				if (conn != null) {
					conn.disconnect();
				}
			} catch (Exception e) {
				e.printStackTrace();
				return "{" + "\"status\": \"ERROR\", \"statusInfo\": \"" + e.getClass().getName() + "\"}";
			}
		}
		

	}
	
	private static String buildHttpGetData(String apikey, String outputMode,
			String start, String end, String maxResults, String timeSlice, 
			String qEntity, String qSentiment, String returns) throws UnsupportedEncodingException {
		Map<String, Object> parameterMap = new HashMap<String, Object>();
		parameterMap.put("apikey", apikey);
		if (outputMode == null) {
			parameterMap.put("outputMode", "json");
		} else {
			parameterMap.put("outputMode", outputMode);
		}
		parameterMap.put("start", start);
		parameterMap.put("end", end);
		if (maxResults == null) {
			parameterMap.put("maxResults", 10);
		} else {
			parameterMap.put("maxResults", maxResults);
		}
		if (timeSlice != null && timeSlice.length() != 0) {
			parameterMap.put("timeSlice", timeSlice);
		}
		if (qEntity != null && qEntity.length() != 0) {
			parameterMap.put("qEntity", qEntity);
		}
		if (qSentiment != null && qSentiment.length() != 0){
			parameterMap.put("qSentiment", qSentiment);
		}
		if (returns != null && returns.length() != 0) {
			parameterMap.put("returns", returns);
		}
		return AlchemyNews.encodeHttpGetParameters(parameterMap);
	}

	// Encode HTTP Post parameters
	private static String encodeHttpGetParameters(Map<String, Object> params)
			throws UnsupportedEncodingException {
		StringBuilder getData = new StringBuilder();
		for (Map.Entry<String, Object> param : params.entrySet()) {
			if (getData.length() != 0)
				getData.append('&');
			if (param.getKey() != "qEntity" && param.getKey() != "qSentiment"
					&& param.getKey() != "returns") {
				getData.append(param.getKey());
			} else if (param.getKey() == "qEntity") {
				getData.append("q.enriched.url.entities.entity");
			} else if (param.getKey() == "qSentiment") {
				getData.append("q.enriched.url.docSentiment.type");
			} else {
				getData.append("return");
			}
			getData.append('=');

			getData.append(URLEncoder.encode(String.valueOf(param.getValue()),
					"UTF-8"));

		}
		return getData.toString();
	}

}