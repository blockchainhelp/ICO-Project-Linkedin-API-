package com.linkedIn;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Properties;

import javax.xml.bind.JAXBException;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.jsoup.Connection;
import org.jsoup.Connection.Response;

import com.linkedIn.data.LinkedinProfile;
import com.linkedIn.util.Utility;

public class LinkedInWebScraper {

	String globalSearchUrl;
	
	public static void main(String[] args) throws IOException, JAXBException {
		new LinkedInWebScraper().scrap();

	}

	private void scrap() throws IOException, JAXBException {

		Properties props = Utility.loadProperties("/config.properties");

		String username = props.getProperty("username");
		String password = props.getProperty("password");

		String outputFileName = props.getProperty("path");
		System.out.println("The File will be created at => " + outputFileName);
		
		
		String loginUrl = props.getProperty("login_url");
		String searchUrl = props.getProperty("search_url");
		
		System.out.println("Search Url getting used :: => " + searchUrl);
				

		globalSearchUrl=searchUrl;
		
		// Logging in LinkedIn
		Connection.Response response = Utility.login(username, password, loginUrl);

		// Getting data for search
		String initialJsonData = Utility.findJsonDataFromUrl(globalSearchUrl, response);

		// Required dummy value
		if (initialJsonData.contains("\\u002d1")) {
			initialJsonData = initialJsonData.replace("\\u002d1", "9");
		}

		JSONArray pages = getAllPages(initialJsonData, response);

		if (pages == null) {
			ArrayList<LinkedinProfile> profilesList = Utility.parsePageData(initialJsonData);
			// Output to Xml File
			Utility.outputToXml(profilesList, outputFileName);
			System.out.println("The File got created.");
			return;
		}

		ArrayList<LinkedinProfile> profilesList = getAllPagesdata(pages, response);

		// Output to Xml File
		Utility.outputToXml(profilesList, outputFileName);
		System.out.println("The File got created.");

	}

	private JSONArray getAllPages(String data, Connection.Response response) throws IOException {

		if (data != null && data.contains("\\u002d1")) {
			data = data.replace("\\u002d1", "9");
		}

		JSONArray pages = null;

		try {
			if (data != null) {

				JSONParser jsonParser = new JSONParser();

				Object obj = jsonParser.parse(data);

				JSONObject jsonObject = (JSONObject) obj;
				JSONObject content = (JSONObject) jsonObject.get("content");

				JSONObject page = (JSONObject) content.get("page");
				JSONObject unifiedJson = (JSONObject) page.get("voltron_unified_search_json");
				JSONObject search = (JSONObject) unifiedJson.get("search");

				JSONObject baseData = (JSONObject) search.get("baseData");

				JSONObject resultPagination = (JSONObject) baseData.get("resultPagination");
				if (resultPagination == null) {
					return null;
				}
				pages = (JSONArray) resultPagination.get("pages");
			}

		} catch (ParseException e) {
			e.printStackTrace();
		}

		return pages;
	}

	private ArrayList<LinkedinProfile> getAllPagesdata(JSONArray pages, Connection.Response response)
			throws IOException, JAXBException {

		ArrayList<LinkedinProfile> profilesList = new ArrayList<LinkedinProfile>();
		fillInPagesData(1, pages, profilesList, response);

		return profilesList;
	}

	private void fillInPagesData(int start, JSONArray pages, ArrayList<LinkedinProfile> profilesList, Response response)
			throws IOException, JAXBException {

		System.out.println("Have patience as large data is getting exported ..");
		
		String lastJsonData = null;

		int startPosition = start;
		int endPosition = getPagePositon(pages, pages.size() - 1);

		for (int i = startPosition; i <= endPosition; i++) {

			String actualUrl = globalSearchUrl + "&page_num=" + i;

			lastJsonData = Utility.findJsonDataFromUrl(actualUrl, response);
			ArrayList<LinkedinProfile> pageProfiles = Utility.parsePageData(lastJsonData);

			profilesList.addAll(pageProfiles);
		}

		JSONArray nextPages = getAllPages(lastJsonData, response);

		if (nextPages != null && nextPages.size() > 0) {
			fillInPagesData(endPosition + 1, nextPages, profilesList, response);
		}

	}

	private int getPagePositon(JSONArray pages, int i) {
		JSONObject page = (JSONObject) pages.get(i);
		Long pageL = (Long) page.get("pageNum");
		int pageInt = pageL.intValue();

		return pageInt;

	}

}
