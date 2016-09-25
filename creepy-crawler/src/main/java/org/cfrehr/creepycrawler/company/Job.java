package org.cfrehr.creepycrawler.company;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;


// performs company-specific job search for positions that: (1) match keywords, (2) are newly posted
// each job will contain raw data (html format or whatever) and will not be cleanly formatted unless print() is called
public class Job implements Comparable<Job> {
	
	// IF YOU ADD NEW FIELDS, MAKE SURE YOU ALSO ADJUST .SETFIELD() AND .GETFIELD() METHODS
	// VITAL FOR PROPER PROGRAM FUNCTIONALITY
	private Company comp = null;
	private String id = null;
	private String company = null;
	private String position = null;
	private String description = null;
	private String location = null;
	private String contact = null;
	private String applyUrlExtension = null;
	private int upScore = 0;
	private int downScore = 0;
	private int overallScore = 0;
	private List<String> posMatches = new ArrayList<String>();
	private List<String> negMatches = new ArrayList<String>();
	
	
	// constructor
	public Job(String jobId) {
		this.id = jobId;
	}
	
	// constructor 
	public Job(Company company) {
		this.company = company.getName().toLowerCase();
	}
	// constructor
	public Job() {
		this.id = null;
	}
	
	public Company getCompany() {
		return this.comp;
	}
	
	// get any Job String field
	public String getField(String field) {
		
		String reply = null;
		
		switch (field) {
		case "id":
			reply = this.id;
			break;
		case "company":
			reply = this.company;
			break;
		case "position":
			reply = this.position;
			break;
		case "description":
			reply = this.description;
			break;
		case "location":
			reply = this.location;
			break;
		case "contact":
			reply = this.contact;
			break;
		case "applyUrlExtension":
			reply = this.applyUrlExtension;
			break;
		default:
			reply = "YOU SCREWED UP YOUR SWITCH STATEMENT IN JOB.GETFIELD() METHOD";
			break;
		}
		return reply;
	}
		
	// Compares job to string representation of another job
	// returns true if new
	public boolean equals(Job job) {
		if (id.equals(job.getField("id"))) {
			return true;
		} else {
			return false;
		}
	}
	
	public int getScore() {
		return this.overallScore;
	}
	
	public void setCompany(Company comp) {
		this.comp = comp;
	}
	// set any Job String field
	public void setField(String field, String value) {
		
		switch (field) {

		case "id":
			this.id = value;
			break;
		case "company":
			this.company = value.toLowerCase();
			break;
		case "position":
			this.position = value.toLowerCase();
			break;
		case "description":
			if (this.description == null) {
				this.description = value;
			} else {
				this.description += "<br>" + value;											// this allows for multiple description additions in company searchKey
			}																				// += is very inefficient, however; use StringBuilder/StringBuffer
			break;
		case "location":
			this.location = value.toLowerCase();
			break;
		case "contact":
			this.contact = value;
			break;
		case "applyUrlExtension":
			this.applyUrlExtension = value;
			break;
		default:
			System.out.println("YOU SCREWED UP YOUR SWITCH STATEMENT IN JOB.SETFIELD() METHOD");
			break;
		}
	}
	
	// increase upScore
	public void up() {
		this.upScore++;
	}
	
	// increase downScore
	public void down() {
		this.downScore++;
	}
	
	// compute weighted score of job posting
	// scores are proportionately weighted so that scoring does not scale with size of keyword set 
	public void scoreJob(int upCount, int downCount) {
		double proportionUp = (double) upScore / (double) upCount;
		double proportionDown =  (double) downScore / (double) downCount;
		double exactScore = 100.0*proportionUp - 100.0*proportionDown;
		this.overallScore = (int) exactScore;
	}
	
	// add positive keyword to matches
	public void addPosKeyword(String posKeyword) {
		this.posMatches.add(posKeyword);
	}
	
	// add negative keyword to matches
	public void addNegKeyword(String negKeyword) {
		this.negMatches.add(negKeyword);
	}
	
	// TODO: returns readable, user-friendly string; 
	//       will be called by CreepyCrawler, where it will be printed to newJobs.txt output
	public String printJob() {
		
		String printJob = "";
		printJob = Integer.toString(this.upScore);
		printJob = "(" + this.overallScore + ")";
		String tempString = null;
		
		// position
		String[] positionArray = this.position.split(" ");
		for (int i=0; i<positionArray.length; i++) {
			tempString = positionArray[i];
			printJob += " " + tempString.substring(0,1).toUpperCase() + tempString.substring(1);
		}
		printJob += " ";
		int length = printJob.length();
		for (int i=0; i<150-length; i++) {
			printJob += "-";
		}
		printJob += "\n\n";
		
		// company
		printJob += "COMPANY:\t\t";
		String[] companyArray = this.company.split(" ");
		for (int i=0; i<companyArray.length; i++) {
			tempString = companyArray[i];
			printJob += tempString.substring(0,1).toUpperCase() + tempString.substring(1) + " ";
		}
		printJob += "\n";
		
		// location
		printJob += "LOCATION:\t\t";
		String[] locationArray = this.location.split(" ");
		for (int i=0; i<locationArray.length; i++) {
			tempString = locationArray[i];
			printJob += tempString.substring(0,1).toUpperCase() + tempString.substring(1) + " ";
		}
		printJob += "\n";
		
		// description
		printJob += "DESCRIPTION:\t";
		String currString = this.description;
		int stringLength = currString.length();
		if (currString.length() !=0) {
			if (stringLength < 130) {
				printJob += currString.substring(0,stringLength) + "\n";
				currString = "";
			} else {
				printJob += currString.substring(0,130) + "\n";
				currString = currString.substring(130);
			}
			stringLength = currString.length();
		}
		while (currString.length() != 0) {
			if (stringLength < 130) {
				printJob += "\t\t\t\t" + currString.substring(0,stringLength) + "\n";
				currString = "";
			} else {
				printJob += "\t\t\t\t" + currString.substring(0,130) + "\n";
				currString = currString.substring(130);
			}
			stringLength = currString.length();
		}
		
		// matching keywords
		int posCount = posMatches.size();
		int negCount = negMatches.size();
		// pos matches
		if (posCount > 0) {
			printJob += "POS MATCHES:\t";
			for (int i=0; i<posCount-1; i++) {
				printJob += posMatches.get(i) + ", ";
			}
				printJob += posMatches.get(posCount-1) + "\n";
		}
		// neg matches
		if (negCount > 0) {
			printJob += "NEG MATCHES:\t";
			for (int i=0; i<negCount-1; i++) {
				printJob += negMatches.get(i) + ", ";
			}
				printJob += negMatches.get(negCount-1) + "\n";
		}
		
		// contact
		printJob += "CONTACT:\t\t" + this.contact + "\n";
				
		// url
		printJob += "APPLY:\t\t\t" + this.comp.getApplyUrl() + this.applyUrlExtension + "\n\n\n";
		return printJob;
	}
	
	public int compareTo(Job compareJob) {
		int compareVal = ((Job) compareJob).getScore();
		return this.overallScore - compareVal;
	}
	
	public static Comparator<Job> JobScoreComparator = new Comparator<Job>() {

		public int compare(Job job1, Job job2) {

			return job2.compareTo(job1);
		}

	};
}