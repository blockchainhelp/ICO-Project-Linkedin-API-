package com.linkedIn.data;

import java.util.Map;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlRootElement(name = "profile")
@XmlType(propOrder = { "name", "professional_Headline", "location", "industry", "publicUrl", "details" })
public class LinkedinProfile {
	private String name;
	private String professional_Headline;
	private String location;
	private String industry;
	private String publicUrl;
	private Map<String, String> details;

	@XmlElement
	public Map<String, String> getDetails() {
		return details;
	}

	public void setDetails(Map<String, String> details) {
		this.details = details;
	}

	@XmlElement
	public String getProfessional_Headline() {
		return professional_Headline;
	}

	public void setProfessional_Headline(String professional_Headline) {
		this.professional_Headline = professional_Headline;
	}

	@XmlElement
	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@XmlElement
	public String getPublicUrl() {
		return this.publicUrl;
	}

	public void setPublicUrl(String publicUrl) {
		this.publicUrl = publicUrl;
	}

	@XmlElement
	public String getLocation() {
		return this.location;
	}

	public void setLocation(String location) {
		this.location = location;
	}

	@XmlElement
	public String getIndustry() {
		return this.industry;
	}

	public void setIndustry(String industry) {
		this.industry = industry;
	}

}