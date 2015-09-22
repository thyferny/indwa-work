package com.alpine.datamining.api.impl.hadoop;

import java.lang.reflect.Type;
import java.util.List;

import org.apache.log4j.Logger;

import com.alpine.utility.hadoop.HadoopConnection;
import com.alpine.utility.hadoop.fs.AlpineHadoopWebAppLocator;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;

public class AlpineMapReduceRESTClient {
	private static Logger itsLogger = Logger
			.getLogger(AlpineMapReduceRESTClient.class);

	public static boolean callHadoopMapReduceJob(HadoopConnection connection,
			AlpineJob job) {

		Gson ng = new GsonBuilder().excludeFieldsWithoutExposeAnnotation()
				.create();
		String alpineJobJsonObj = ng.toJson(job);

		String endpoint = AlpineHadoopWebAppLocator
				.getMapReduceHadoopClient(connection);

		Client client = Client.create();
		WebResource webResource = client.resource(endpoint);

		Gson gson = new Gson();

		ClientResponse response = webResource.type("application/json").post(
				ClientResponse.class, alpineJobJsonObj);

		if (response.getStatus() != 200) {
			String output = response.getEntity(String.class);
			itsLogger.error("Failed due to the exception of:" + output);
			throw new RuntimeException("Failed : HTTP error code : "
					+ response.getStatus());
		}

		Type listType = new TypeToken<List<String>>() {
		}.getType();
		String output = response.getEntity(String.class);
		List<String> des = gson.fromJson(output, listType);
		if (itsLogger.isDebugEnabled()) {
			itsLogger.debug(des.toString());
			itsLogger.debug(output);
		}
		return true;

	}

}
