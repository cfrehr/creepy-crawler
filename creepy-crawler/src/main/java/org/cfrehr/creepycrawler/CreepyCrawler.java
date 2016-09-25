package org.cfrehr.creepycrawler;

import org.cfrehr.creepycrawler.company.*;
import org.cfrehr.creepycrawler.filter.*;
import org.cfrehr.creepycrawler.keywords.*;


import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

// JSON Parser: http://json.parser.online.fr/
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;






///#############
/// TODO:
///			1) Handle "null" property printouts
///			2) Fix JSONFilter.extractJobs() "arrays" case to match "objects" case
///         3) Print to doc, instead of text file
///         4) Parse HTML from description field
///         5) Clean HTML element tags from descriptions


public class CreepyCrawler {
	
	// User Agent String: Mozilla/5.0 (Macintosh; Intel Mac OS X 10.9; rv:48.0) Gecko/20100101 Firefox/48.0
	// Groupon: https://jobs.groupon.com/careers_front_api/v1/location/jobs/chicago
	
	// URL class: https://docs.oracle.com/javase/7/docs/api/java/net/URL.html

	// URLConnection class: https://docs.oracle.com/javase/7/docs/api/java/net/URLConnection.html
	
	public static void main(String[] Args) throws MalformedURLException, FileNotFoundException {
		
		// get resources
		File searchKey = new File(ClassLoader.getSystemResource("searchKey.txt").getFile());
		if(!searchKey.exists() || searchKey.isDirectory()) { 
			// TODO: create a File exception for all input/output files (e.g. "searchKeyNotFoundException")
		}
		File companies = new File(ClassLoader.getSystemResource("companies.txt").getFile());
		File jobs = new File(ClassLoader.getSystemResource("jobs.txt").getFile());
		File keywords = new File(ClassLoader.getSystemResource("keywords.txt").getFile());
		
		// validate companies (CreepyCrawler.validate() method removes subset of companies that the searchKey does not cover)
		List<String> companiesList = readFile(companies);
		List<String> searchKeyList = readFile(searchKey);
		List<String> newCompaniesList = validate(companiesList, searchKeyList);
		List<String> keywordsList = readFile(keywords);
			
		// initialize companyDB, jobDB, Keywords
		CompanyDB cDB = new CompanyDB(newCompaniesList, searchKeyList);
		// TODO: may want to change jobDB class so that it doesn't have to read files, just a list, like companyDB
		JobDB jDB = new JobDB(jobs);
		Keywords key = new Keywords(keywordsList);
		
		// search websites and compare to databases to find new jobs
		List<Job> newJobs = new ArrayList<Job>();
		
		Iterator<String> itr = newCompaniesList.iterator();
		while (itr.hasNext()) {
			String compString = itr.next();
			Company tempComp = cDB.getCompany(compString);
			String tempCode = tempComp.getCode();

			switch (tempCode) {
			case "json":
				String postings = getJSON(new URL(tempComp.getJobUrl()));
				Filter jobFilter = new JSONFilter(compString, postings, jDB, cDB, key);
				newJobs.addAll(jobFilter.filterJobs());

				break;
			case "html":
				// TODO
				break;
			case "xml":
				// TODO
				break;
			default:
				System.out.println("NEED CASE CODE FIX IN CREEPYCRAWLER.MAIN() OR METHOD USED IN CREEPYCRALWER.MAIN()");
				break;
			}
		}
		
		// score jobs
		int size = newJobs.size();
		for (int i=0; i<size; i++) {
			newJobs.get(i).scoreJob(key.getUp(), key.getDown());
		}

		// write new jobs to user-friendly file and jobs database
		Collections.sort(newJobs, Job.JobScoreComparator);
		writeFile(newJobs);
		

	}
	
	// removes companies from search that are not covered by searchKey
	public static List<String> validate(List<String> companiesList, List<String> searchKeyList) {
		
		// build list of companies from searchKeyList
		List<String> keyList = new ArrayList<String>();
		Iterator<String> keyItr = searchKeyList.iterator();
		String currKey;
		while (keyItr.hasNext()) {
			currKey = keyItr.next();
			List<String> currKeyList = Arrays.asList(currKey.split("~~~"));
			keyList.add(currKeyList.get(0).toLowerCase());
		}
		
		// add companies to newCompaniesList that are in keyList
		List<String> newCompaniesList = new ArrayList<String>();
		Iterator<String> compItr = companiesList.iterator();
		String currComp;
		while(compItr.hasNext()) {
			currComp = compItr.next().toLowerCase();
			if (keyList.contains(currComp)) {
				newCompaniesList.add(currComp);
			}
		}
		
		return newCompaniesList;
	}
	
	// file reader that builds list from "$$$" START/STOP TOKENS
	public static List<String> readFile(File f) throws FileNotFoundException {
		
		List<String> list = new ArrayList<String>();
		
		FileReader in = new FileReader(f);
		BufferedReader br = new BufferedReader(in);
		try {
			// skip file header
			String currLine;
			do {
				currLine = br.readLine();
			} while (!currLine.equals("$$$ START"));
			
			// extract relevant data
			currLine = br.readLine();
			while (!currLine.equals("$$$ STOP")) {
				list.add(currLine);
				currLine = br.readLine();
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
			
		return list;
	}
	
	public static void writeFile(List<Job> newJobs) throws FileNotFoundException {
		
		// append jobs to database
		
		
		// print output file
		try (FileWriter fw = new FileWriter("newJobs.txt", false);
				BufferedWriter bw = new BufferedWriter(fw);
				PrintWriter pw = new PrintWriter(bw);) {
			int jobSize = newJobs.size();
			for (int i=0; i<jobSize; i++) {
				pw.print(newJobs.get(i).printJob());
			}
		} catch (IOException e) {
			e.printStackTrace();
		} 
		
	}
	
	// Gets all JSON-type job posting data
	// Source: http://stackoverflow.com/questions/16826345/why-do-i-get-a-403-error-when-i-try-open-a-url
	// JSON Formatter: http://jsonviewer.stack.hu/
	public static String getJSON(URL companyURL) {
		
		URL url = companyURL;
        String line = null;
        String jsonString = "";
       
        InputStream is = null;
        System.setProperty("http.agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10.9; rv:48.0) Gecko/20100101 Firefox/48.0");
        try {
            is = url.openConnection().getInputStream();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
        BufferedReader reader = new BufferedReader( new InputStreamReader( is )  );  
        try {
            while( ( line = reader.readLine() ) != null )  {
            	jsonString += line;
            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
        try {
            reader.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    	
        return jsonString;
    }
    
	// TODO: as opposed to json, specific job links will have to be accessed in a more traditional crawling, loopwise fashion
	//		 from company root-url to gather and consolidate job data.
	// LINKS:
	//       - HTML button scraper: http://stackoverflow.com/questions/16421074/newbie-how-to-overcome-javascript-onclick-button-to-scrape-web-page
	//       - jsoup for SCRAPING, htmlunit for NAVIGATING BUTTONS http://stackoverflow.com/questions/7508813/can-jsoup-simulate-a-button-press
	//       - WAIT: Selenium is a webdriver that does things the easy way, by simulating interaction with a webpage (rather than parsing code)
	//			READ MORE HERE: http://denvycom.com/blog/data-scraping-selenium-java/
	public static String getHTML(URL companyURL) {
		String line = null;
		return line;
	}

}
