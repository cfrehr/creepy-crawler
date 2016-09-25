package org.cfrehr.creepycrawler.company;

public class Company {
	
	private String name;
	private String jobUrl = null;
	private String applyUrl = null;
	private String code = null;
	private String schema = null;
	
	public Company(String name) {
		// name
		this.name = name.toLowerCase();
	}
	
	// returns name of company
	public String getName() {
		return this.name;
	}
	
	// returns URL String associated with company job postings
	public String getJobUrl() {	
		return this.jobUrl;
	}
	
	public String getApplyUrl() {
		return this.applyUrl;
	}
	
	// returns type of coding
	public String getCode() {
		return this.code;
	}
	
	// returns the coding schema
	public String getSchema() {
		return this.schema;
	}
	
	// sets company URL
	public void setJobUrl(String urlString) {
		this.jobUrl = urlString;
	}
	
	// sets company URL
	public void setApplyUrl(String urlString) {
            this.applyUrl = urlString;
	}
	
	// sets type of coding format for job postings
	public void setCode(String codeString) {
		this.code = codeString.toLowerCase();
	}
	
	// sets coding schema
	public void setSchema(String schemaString) {
		this.schema = schemaString;
	} 
}