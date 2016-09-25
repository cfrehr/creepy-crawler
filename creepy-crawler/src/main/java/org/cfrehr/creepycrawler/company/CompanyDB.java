package org.cfrehr.creepycrawler.company;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.HashMap;


// a database of companies and job posting urls
public class CompanyDB {
	
	private Map<String,Company> companies = new HashMap<String,Company>();
	
	public CompanyDB(List<String> companiesList, List<String> keyList) {
		
		// iterate through searchList, architect database
		Iterator<String> itr = companiesList.iterator();
		String currLine;
		while (itr.hasNext()) {
		    currLine = itr.next();
			Company comp = new Company(currLine);
		    this.companies.put(currLine.toLowerCase(), comp);
		}
		
		// iterate through keyList, complete database
		itr = keyList.iterator();
		while (itr.hasNext()) {
			currLine = itr.next();
			String[] secrets = currLine.split("~~~");
			String key = secrets[0].toLowerCase();
		    if (this.companies.containsKey(key)) {
		    	this.companies.get(key).setJobUrl(secrets[1]);
		    	this.companies.get(key).setApplyUrl(secrets[2]);
		    	this.companies.get(key).setCode(secrets[3]);
		    	this.companies.get(key).setSchema(secrets[4]);
		    }
		}
		
	}
		
	// get Company from database
	public Company getCompany(String company) {
		return companies.get(company.toLowerCase());
	}
}