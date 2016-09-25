package org.cfrehr.creepycrawler.company;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Map;
import java.util.HashMap;

// TODO: MAY WANT TO REBUILD SO THAT EACH COMPANY HAS ITS OWN DATABASE
// a complete database of individual jobs across all companies
// ** Constructed from previously known jobs, jobs.txt, NOT a complete rebuild from website **
// TODO: possibly restructure to make JobDBs specific to companies 
//		- add a field in Company.java for a JobDB
//		- change add field in JobDB.java for Company

public class JobDB {
	
	private Map<String,Job> jobs = new HashMap<String,Job>();
	
	public JobDB(File f) throws FileNotFoundException {
		
		FileReader in = new FileReader(f);
		BufferedReader br = new BufferedReader(in);
		
		// read file, build database
		try {
			String currLine;
			while ((currLine = br.readLine()) != null) {
			    String[] data = currLine.split("~~~");
			    Job job = new Job(data[0]);
			    // set available job fields
			    job.setField("company", data[1]);
			    job.setField("position", data[2]);
			    job.setField("location", data[3]);
			    job.setField("contact", data[4]);
			    job.setField("description", data[5]);
			    job.setField("applyUrlExtension", data[6]);
			    // add job to database
			    jobs.put(data[0], job);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		// close FileReader
		try {
			in.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
	// get Job from database
	public Job getJob(String ID) {
		return jobs.get(ID);
	}
	
	// key check
	public boolean containsID(String ID) {
		if (jobs.containsKey(ID)) {
			return true;
		} else {
			return false;
		}
	}
	
	// add job to database
	public void addJob(Job newJob) {
		jobs.put(newJob.getField("id"), newJob);
	}
}