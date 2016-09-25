package org.cfrehr.creepycrawler.filter;

import org.cfrehr.creepycrawler.company.*;

import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;


// company-specific formatter of job posting code, to assist in creation of Jobs
public interface Filter {
	
	public List<Job> filterJobs();
	
	public List<Job> extractJobs(JSONObject jObject, JSONArray jArray, Job job, String schema);
	
	public boolean include(Job job);
	
	public void match(Job job);

	
}
