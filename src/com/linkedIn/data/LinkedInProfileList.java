package com.linkedIn.data;

import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name="profiles")
public class LinkedInProfileList {

	
	private List<LinkedinProfile> profiles = null;

	@XmlElement(name = "profile")
	public List<LinkedinProfile> getProfiles() {
		return profiles;
	}

	public void setProfiles(List<LinkedinProfile> profiles) {
		this.profiles = profiles;
	}
}
