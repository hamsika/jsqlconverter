package com.googlecode.jsqlconverter.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;

public class ServiceUtil {
	private ArrayList<Service> serviceList = new ArrayList<Service>();

	public ServiceUtil() throws IOException, ClassNotFoundException {
		Enumeration<URL> serviceURLs = ClassLoader.getSystemResources("META-INF/services/jsqlconverter");

		while (serviceURLs.hasMoreElements()) {
			URL serviceURL =  serviceURLs.nextElement();
			BufferedReader br = new BufferedReader(new InputStreamReader((InputStream) serviceURL.getContent()));
			String className;

			while((className = br.readLine()) != null) {
				serviceList.add(new Service(Class.forName(className)));
			}

			br.close();
		}
	}

	public Service[] getServices() {
		return serviceList.toArray(new Service[serviceList.size()]);
	}

	public Service[] getService(String name) {
		ArrayList<Service> matchedServices = new ArrayList<Service>();
		
		for (Service service : getServices()) {
			if (service.getName().equalsIgnoreCase(name)) {
				matchedServices.add(service);
			}
		}

		if (matchedServices.size() > 0) {
			return matchedServices.toArray(new Service[matchedServices.size()]);
		}

		throw new RuntimeException("Service with name " + name + " not found");
	}
}
