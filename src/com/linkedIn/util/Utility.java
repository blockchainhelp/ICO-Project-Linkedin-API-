package com.linkedIn.util;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Comment;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.linkedIn.data.LinkedInProfileList;
import com.linkedIn.data.LinkedinProfile;

public class Utility {

	public static Connection.Response login(String username, String password, String url) {

		Connection.Response response = null;
		try {

			response = Jsoup.connect(url).method(Connection.Method.GET).execute();

			Document responseDocument = response.parse();
			Element loginCsrfParam = responseDocument.select("input[name=loginCsrfParam]").first();

			response = Jsoup.connect("https://www.linkedin.com/uas/login-submit").cookies(response.cookies())
					.data("loginCsrfParam", loginCsrfParam.attr("value")).data("session_key", username)
					.data("session_password", password).method(Connection.Method.POST).followRedirects(true).execute();

		} catch (IOException e) {
			e.printStackTrace();
		}

		return response;
	}

	public static String findJsonDataFromUrl(String url, Connection.Response iniResponse) throws JAXBException {

		Connection.Response response = null;
		String jsonData = null;
		try {

			response = Jsoup.connect(url).cookies(iniResponse.cookies())
					.userAgent(
							"Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/535.21 (KHTML, like Gecko) Chrome/19.0.1042.0 Safari/535.21")
					.maxBodySize(0).method(Connection.Method.GET).execute();

			Document doc = response.parse();

			Elements links = doc.select("code");

			Element element = links.get(0);
			Comment node = (Comment) element.childNodes().get(0);
			jsonData = node.getData();

		} catch (IOException e) {
			e.printStackTrace();
		}
		return jsonData;
	}

	public static ArrayList<LinkedinProfile> parsePageData(String data) throws IOException {

		if (data.contains("\\u002d1")) {
			data = data.replace("\\u002d1", "9");
		}

		ArrayList<LinkedinProfile> pageProfiles = null;
		try {
			JSONParser jsonParser = new JSONParser();
			Object obj = jsonParser.parse(data);

			JSONObject jsonObject = (JSONObject) obj;
			JSONObject content = (JSONObject) jsonObject.get("content");

			JSONObject page = (JSONObject) content.get("page");
			JSONObject unifiedJson = (JSONObject) page.get("voltron_unified_search_json");
			JSONObject search = (JSONObject) unifiedJson.get("search");

			JSONArray searchResult = (JSONArray) search.get("results");

			pageProfiles = getAllProfiles(searchResult);

		} catch (ParseException e) {
			e.printStackTrace();
		}

		return pageProfiles;
	}

	private static ArrayList<LinkedinProfile> getAllProfiles(JSONArray searchResult) {

		ArrayList<LinkedinProfile> profilesList = new ArrayList<LinkedinProfile>();

		for (int i = 0; i < searchResult.size(); i++) {

			LinkedinProfile profile = new LinkedinProfile();

			JSONObject record = (JSONObject) searchResult.get(i);
			JSONObject person = (JSONObject) record.get("person");

			String name = (String) person.get("fmt_name");
			String professionalHeadline = (String) person.get("fmt_headline");
			String location = (String) person.get("fmt_location");
			String industry = (String) person.get("fmt_industry");
			String publicUrl = (String) person.get("link_nprofile_view_4");

			profile.setName(name);
			profile.setProfessional_Headline(professionalHeadline);
			profile.setLocation(location);
			profile.setIndustry(industry);
			profile.setPublicUrl(publicUrl);

			JSONArray snippets = (JSONArray) person.get("snippets");

			Map<String, String> details = new HashMap<String, String>();
			for (int j = 0; j < snippets.size(); j++) {

				JSONObject snippet = (JSONObject) snippets.get(j);
				String fieldName = (String) snippet.get("fieldName");
				String heading = (String) snippet.get("heading");

				details.put(fieldName, heading);
			}

			profile.setDetails(details);

			profilesList.add(profile);

		}

		return profilesList;

	}

	public static void produceXmlFile(LinkedInProfileList list, String file)
			throws JAXBException, FileNotFoundException {

		JAXBContext contextObj = JAXBContext.newInstance(LinkedInProfileList.class);

		Marshaller marshallerObj = contextObj.createMarshaller();
		marshallerObj.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);

		marshallerObj.marshal(list, new FileOutputStream(file));

	}

	public static void outputToXml(ArrayList<LinkedinProfile> profilesList, String outputFileName) {
		System.out.println("Total Number of Profiles :: " + profilesList.size());
		try {
			LinkedInProfileList list = new LinkedInProfileList();
			list.setProfiles(profilesList);
			Utility.produceXmlFile(list, outputFileName);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (JAXBException e) {
			e.printStackTrace();
		}

	}

	public static Properties loadProperties(String fileName) {

		String path = System.getProperty("user.dir");

		Properties prop = new Properties();
		InputStream input = null;

		try {
			input = new FileInputStream(path + fileName);

			// load a properties file
			prop.load(input);

		} catch (IOException ex) {
			ex.printStackTrace();
		} finally {
			if (input != null) {
				try {
					input.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

		return prop;
	}

}
