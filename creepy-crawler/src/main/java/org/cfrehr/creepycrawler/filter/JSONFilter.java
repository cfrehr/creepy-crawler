package org.cfrehr.creepycrawler.filter;

import org.cfrehr.creepycrawler.company.*;
import org.cfrehr.creepycrawler.keywords.Keywords;

import java.util.ArrayList;
import java.util.List;


import org.json.JSONArray;
import org.json.JSONObject;





// company-specific formatter of job posting code, to assist in creation of Jobs
public class JSONFilter implements Filter {
	
// #########################################################################################################################################
// *****************************************************************************************************************************************
// TODO: Refer to searchKey file.
//	     - The searchKey is not as succinct as it out to be. For example, look at the long sequence of fields; each of those fields should
//	       be brought together into a single field, with array lists of parameters and dataTypes representing all the individual fields that 
//	       that need to be crafted. This can be done for any clustering of fields!!
//       - But, can you do that for any item..? NO. If you had two listed objects that have different structures within each other, then you
//         would not be able to successfully combine them, in which case you will just have to list them as you have been.
//	     - The good news is that this will dnot destroy your searchKey concept. It will help sometimes, as in the Groupon case where you can
//         retrieve all objects (Job objects, none the less) from its parent with a "-1".
// TODO: You will have to clean up your representation of JSON and your parsing algorithms. Think it over, and decide how you want to
//       rebuild your algos to include combined objects.
//*****************************************************************************************************************************************
//#########################################################################################################################################
	
	
	// fields initialized by constructor
	private String company;
	private String postings;
	private JobDB jDB;
	private CompanyDB cDB;
	private Keywords keywords;
	private String schema;
	// TBD fields
	private List<Job> newJobs = new ArrayList<Job>();
	//private List<String> posMatches = new ArrayList<String>();
	//private List<String> negMatches = new ArrayList<String>();

	public JSONFilter(String company, String postings, JobDB jDB, CompanyDB cDB, Keywords key) {
		this.company = company;
		this.postings = postings;
		this.jDB = jDB;
		this.cDB = cDB;
		this.keywords = key;
		this.schema = cDB.getCompany(company).getSchema();
	}
	
	public List<Job> filterJobs() {
		// searches for new jobs by comparing posting code to database of know jobs.
		// adds new jobs to jobDB and returns new jobs.
		
		
		JSONObject jObject = null;
		JSONArray jArray = null;
		Job dummy = new Job();
		char type = postings.charAt(0);
		
		// build JSON
		if (type == '{') {
			jObject = new JSONObject(postings);
		} else if (type == '[') {
			jArray = new JSONArray(postings);
		} else {
			System.out.println("FATAL ERROR IN FILTER.FILTERJOBS() METHOD");
		}
		
		// extract all jobs
		List<Job> allJobs = extractJobs(jObject, jArray, dummy, schema);
		
		/*
		// JOB TEST 1: ##############################################################################################################################
		//             ##############################################################################################################################
		int size1 = allJobs.size();
		for (int i=0; i<size1; i++) {
			System.out.print("JOB " + i + " : ");
			Job tempJob = allJobs.get(i);
			if (tempJob.description!=null && tempJob.contact!=null && tempJob.position!=null && tempJob.location!=null && tempJob.company!=null) {
				System.out.print("SUCCESS!");
			}
			System.out.println();
		}
		*/
		
		// find and score new jobs
		int jobSize = allJobs.size();
		Job currJob;
		for (int i=0; i<jobSize; i++) {
			currJob = allJobs.get(i);
			if (include(currJob)) {
				match(currJob);
				newJobs.add(currJob);
			}
		}

		// sort new jobs by highest score
		// TODO: Sort list of jobs by score; may be able to do this by adding a .comparable() Method to job??
		
		// return new jobs
		return newJobs;
		
	}

	public List<Job> extractJobs(JSONObject jObject, JSONArray jArray, Job job, String schema) {
		// using a known company-specific schema, this method recursively parses tree-structured JSON code to instantiate and return all job objects
		// HELPFUL RECURSION THREADS: 	http://stackoverflow.com/questions/31384894/how-to-get-all-leaf-nodes-of-a-tree
		// 								http://stackoverflow.com/questions/33923/what-is-tail-recursion
		//								http://stackoverflow.com/questions/27886116/how-to-return-an-arraylist-with-an-recursive-function
		// TODO: In the near future, you will need to generalize the cases to catch missing fields, as well as adjust the content of your searchKeys
		//       EX 1: A company may have multiple jobs listed, but some are missing "city" fields. In this case, your job extractor will need to be
		//             able to handle empty objects or arrays where you'd expect new objects/arrays/fields to be present.
		//             Alternatively, instead of a field being missing, it may have the type "null", which your extractor will have to handle.
		//             Just a small note: by default, attributes of Job object are set to null.
		//             See reddit thread: https://www.reddit.com/r/learnprogramming/comments/51sz0g/basic_json_question_does_a_json_tree_always_have/
		// TODO: Also, case "<(" must be corrected to handle all fieldDataType conditions. Currently, only handles string.
		
		
		List<Job> jobList = new ArrayList<Job>();
		int openIndex;
		int closeIndex;
		// pairing: parameter(s) and type(s) to identify data structure to build
		String[] pair;
		String parameter;
		//String[] params;
		String dataType;
		//String[] types;
		//int pSize;
		//int tSize;
		
		// switch-case key
		String buildKey;
		
		// base case: if empty schema
		if (schema.isEmpty()) {
			// do nothing		
		// else, get buildKey and work towards base case
		} else {
			// get parameter/dataType pair inside parentheses
			openIndex = schema.indexOf('(');
			closeIndex = schema.indexOf(')');
			pair = schema.substring(openIndex+1, closeIndex).split(":");
			parameter = pair[0];
			//params = parameter.split(",");
			dataType = pair[1];
			//types = dataType.split(",");
			
			buildKey = schema.substring(0,2);
			
			// if asterisk, remove asterisk
			if (schema.substring(0,1).equals("*")) {
				schema = schema.substring(1);
			}
			
			String[] items;
			int iSize;
												
			
			
			// #########################################################################################################################################
			// *****************************************************************************************************************************************
			/// TODO: For all build cases, you will need to rebuild considering how to construct the
			//        new items from a list of parameters and data types. Code that has already been deviated
			//        is marked in the right margin. These changes were made purely for being able to run
			//        program and test Groupon job creation with old schemaKey. Make sure to keep generalized.
			//        - algos handle "-1" case for arrays, but still need to handle:
			//            -  "-1" for strings 
			//            -  listed parameters/dataTypes
			//*****************************************************************************************************************************************
			//#########################################################################################################################################
			
			
			// build json structure, adjust schema, recursively build inner items
			switch (buildKey) {

			// build array
			case "[(":
			case "*[": {

				
				JSONArray newJArray;
				// if parent is object
				if (dataType.equals("string")) {
					newJArray = jObject.getJSONArray(parameter);
					// else, parent is array
				} else {
					int index = Integer.parseInt(parameter);
					newJArray = jArray.getJSONArray(index);
				}
				// get new item schemas
				items = getItems(schema);
				// continue recursive exploration
				iSize = items.length;
				for (int i=0; i<iSize; i++) {
					JSONObject newJObject = null;
					// if not new Job, use parent Job
					if (buildKey.equals("[(")) {
						jobList.addAll(extractJobs(newJObject, newJArray, job, items[i]));
					// else, assert new Job
					} else {
						// create job, set company
						Job newJob = new Job();
						newJob.setField("company", this.company);
						newJob.setCompany(cDB.getCompany(company));
						jobList.add(newJob);
						jobList.addAll(extractJobs(newJObject, newJArray, newJob, items[i]));
					}
				}
				break;
			}
				 
				
			// build object
			case "{(":
			case "*{": {
				List<JSONObject> newJObjects = new ArrayList<JSONObject>();
				// if parent is object
				if (dataType.equals("string")) {
					JSONObject newJObject = jObject.getJSONObject(parameter);
					newJObjects.add(newJObject);
				// else, parent is array
				} else {
					// if get all objects from array
					if (parameter.equals("-1")) {
						int jaSize = jArray.length();
						for (int i=0; i<jaSize; i++) {
							JSONObject newJSONObject = jArray.getJSONObject(i);
							newJObjects.add(newJSONObject);
						}
					// else get single object
					} else {
					int index = Integer.parseInt(parameter);
					JSONObject newJObject = jArray.getJSONObject(index);
					newJObjects.add(newJObject);
					}
				}
				// get new item schemas
				items = getItems(schema);
				// continue recursive exploration
				iSize = items.length;
				
				// recursively explore jobs
				int oSize = newJObjects.size();
				for (int i=0; i<oSize; i++) {
					Job newJob = new Job();
					jobList.add(newJob);
					for (int j=0; j<iSize; j++) {
						JSONArray newJArray = null;
						// if not new Job, use parent Job
						if (buildKey.equals("{(")) {
							// add extracted jobs to list for only the first iteration, to avoid duplication
							if (j==0) {
								jobList.addAll(extractJobs(newJObjects.get(i), newJArray, job, items[j]));
							} else {
								extractJobs(newJObjects.get(i), newJArray, job, items[j]);
							}
						// else, assert new Job
						} else {
							// create job, set company
							newJob.setField("company", this.company);
							newJob.setCompany(cDB.getCompany(company));
							// add extracted jobs to list for only the first iteration, to avoid duplication
							if (j==0) {
								jobList.addAll(extractJobs(newJObjects.get(i), newJArray, newJob, items[j]));
							} else {
								extractJobs(newJObjects.get(i), newJArray, newJob, items[j]);

							}
						}
					}
				}
				break;
			}
			
			// set Job fields
			case "<(":
				// create field parameters
				int index1 = schema.indexOf(')') + 1;
				int index2 = schema.indexOf('>');
				String[] fieldPair = schema.substring(index1,index2).split(":");
				String fieldParam = fieldPair[0];
				String fieldDataType = fieldPair[1];
				// if parent is object
				if (dataType.equals("string")) {
					// if fieldDataType is string
					if (fieldDataType.equals("string")) {
						job.setField(fieldParam, jObject.get(parameter).toString());
					}
					// TODO: make cases for other data types!
				// else, parent is array
				} else {
					// if fieldDataType is string
					if (fieldDataType.equals("string")) {
						int index = Integer.parseInt(parameter);
						job.setField(fieldParam, jArray.get(index).toString());
					}
					// TODO: make cases for other data types!
				}				
				break;
				
			default:
				System.out.println("NEED CASE TYPE FIX FOR JSONFilter.extractJobs()");
				break;
			}
		}
		
		// pass back jobList
		return jobList;
		
	}	
	
	public int[] getIndices(String schema) {
		
		// TODO: FIX: sometimes, you may pass a schema that begins with '*'; need case to handle that.
		//       Cannot be fixed in .extract method because; must manage here.
		
		// Returns indices in schema string of whole items (objects, arrays, and/or fields) present in current structure.
		// Greatly facilitates indexing of schema string for future trimming and item creation.
		
		// NOTE: The asterisk is a one-time occurrence in the schema and may seemingly throw of the indexing
		//       of items, but since the beginning of an item is indexed a set distance from the end of another, 
		//       the asterisk will always be included at the forefront of an item.
		
	
		// search parameters
		char[] charArray = schema.toCharArray();
		ArrayList<Integer> indexList = new ArrayList<Integer>();	// marks the CLOSING indices of objects
		// count total bracket occurrences of parent bracket type
		int openCount = 0;		
		int closeCount = 0;
		// count all new occurrences of all bracket types
		int openCurly = 0;
		int closeCurly = 0;
		int openSquare = 0;
		int closeSquare = 0;
		int openAngle = 0;
		int closeAngle = 0;

		// get key
		char key = schema.charAt(0);
		
		switch (key) {
		case '{':
			// do while the parent item has not been closed (when openCount=closeCount)
			// start at index 1 to enter loop (because currently, openCount=0=closeCount)
			openCount=1;
			caseLoop:
			for (int i=1; openCount!=closeCount; i++) {
				
				char currChar = charArray[i];
				
				switch (currChar) {
				case '{':
					openCurly++;
					openCount++;
					break;
				case '}':
					closeCurly++;
					closeCount++;
					break;
				case '[':
					openSquare++;
					break;
				case ']':
					closeSquare++;
					break;
				case '<':
					openAngle++;
					break;
				case '>':
					closeAngle++;
					break;
				// cases for ',' and '*' will immediately continue search loop to avoid "double indexing"
				// double indexing occurs in the gap between objects, where a comma or an asterisk may result
				//   in extra indices being created, since the (openBracket=closeBracket) conditions will still hold.
				case ',':
					continue caseLoop;
				case '*':
					continue caseLoop;
				default:
					break;
				}
				
				// new object found if both:
				//   (1) open/close count equal for all new bracket occurrences
				//   (2) any type of new bracket count is not 0
				// if new object found, add index
				if ((openCurly==closeCurly) && (openSquare==closeSquare) && (openAngle==closeAngle)) {
					if (openCurly>0 || openSquare>0 || openAngle>0)
						indexList.add(i);
				}
			}
			break;

		case '[':
			// do while the parent item has not been closed (when openCount=closeCount)
			// start at index 1 to enter loop (because currently, openCount=0=closeCount)
			openCount=1;
			caseLoop:
			for (int i=1; openCount!=closeCount; i++) {
				
				char currChar = charArray[i];
		
				switch (currChar) {
				case '{':
					openCurly++;
					break;
				case '}':
					closeCurly++;
					break;
				case '[':
					openSquare++;
					openCount++;
					break;
				case ']':
					closeSquare++;
					closeCount++;
					break;
				case '<':
					openAngle++;
					break;
				case '>':
					closeAngle++;
					break;
				// cases for ',' and '*' will immediately continue search loop to avoid "double indexing"
				// double indexing occurs in the gap between objects, where a comma or asterisk may result
				//   in extra indices being created, since the openBracket=closeBracket conditions will still hold.
				case ',':
					continue caseLoop;
				case '*':
					continue caseLoop;
				default:
					break;
				}
				
				// new object found if both:
				//   (1) open/close count equal for all new bracket occurrences
				//   (2) any type of new bracket count is not 0
				// if new object found, add index
				if ((openCurly==closeCurly) && (openSquare==closeSquare) && (openAngle==closeAngle)) {
					if (openCurly>0 || openSquare>0 || openAngle>0)
						indexList.add(i);
				}
			}			
			break;
			
		default:
			System.out.println("NEED FIX JSONFILTER.GETINDICES() METHOD");
			break;
		}
		
		int indexSize = indexList.size();
		int[] indices = new int[indexSize+1]; // +1 for additional starting index to be added
		
		// ADD STARTING INDEX FOR PROPER LOOPING IN GETITEMS() METHOD
		//     - Since object creation loop will refer to the index of previous object, and since the first
		//       object has no previous object, a dummy index must be inserted to give object a creation point.
		indices[0] = schema.indexOf(')') - 1; // -1 for proper looping in getItems() method; must be placed one to left of closing parentheses
		for (int i=0; i<indexSize; i++) {
			indices[i+1] = indexList.get(i);
		}

		
		return indices;
	}
	
	// parses a set/list of items from schema for further exploration
	
	public String[] getItems(String schema) {
		
		int[] indices = getIndices(schema);
		
		int indicesLength = indices.length;
		String[] items = new String[indicesLength-1];
		
		for (int i=1; i<indicesLength; i++) {
			// item schema string will begin 2 indices after the ending index of previous object
			// string will end one index after the index of the closure bracket/brace/angle
			items[i-1] = schema.substring(indices[i-1]+2, indices[i]+1);
		}
		
		return items;
	}	
	
	public boolean include(Job job) {
		// FIX: will have to adjust object type to handle JSON, HTML, etc
		// TODO: nested case matching formula for class, match, and keywords

		// TODO: Non-urgent, but seemingly required fix:
		//		 A job database may contain duplicate job IDs. Since a job database contains all jobs from all companies,
		//		 it may contain two jobs from different companies with the same ID.
		//		 This will require a redesign of JobDB and CompanyDB classes, so that CompanyDB is a database of JobDB.
		//		 Then, this method case will require minor restructuring to first check if the CompanyDB contains company.
		
		// if job not already present in database
		if (!jDB.containsID(job.getField("id"))) {
			// TODO: a non-urgent, but important fix to help generalize this code would be to build conditional statements
			// 		 for TITLE, LOCATION, etc based on the sections specified in Keywords, retrievable with .getClassSection() 
			//		 and .getMatchSections() methods

			ArrayList<String> classSections = keywords.getClassSections();
			ArrayList<ArrayList<ArrayList<ArrayList<String>>>> words = keywords.getKeywords();
			ArrayList<ArrayList<String>> bigKeywords = new ArrayList<ArrayList<String>>();
			String className = null;
			// create smartIndex for efficient/intelligent keyword analysis
			int[][][] smartIndex = keywords.getSmartIndex();
			
			// First, loop through OUTs to quickly rule out job creation
			for (int i=0; i<classSections.size(); i++) {
				className = classSections.get(i);
				// if class i has an OUT match type, loop through keywords
				if (smartIndex[0][i][0] != -1) {
					bigKeywords = words.get(i).get(smartIndex[0][i][0]);
					for (int j=0; j<bigKeywords.size(); j++) {
						for (int k=0; k<bigKeywords.get(j).size(); k++) {
							if (job.getField(className).contains(bigKeywords.get(j).get(k))) {
								// rule out job
								return false;
							}
						}
					}
				}
			}
			
			// Second, loop through INs to quickly rule out jobs
			// (INs must all be met or else a job is dropped)
			int inCount = -1;
			int matchCount = 0;
			classLoop:
			for (int i=0; i<classSections.size(); i++) {
				className = classSections.get(i);
				inCount++;
				// if class i has an OUT match type, loop through keywords
				if (smartIndex[1][i][0] != -1) {
					bigKeywords = words.get(i).get(smartIndex[1][i][0]);
					for (int j=0; j<bigKeywords.size(); j++) {
						for (int k=0; k<bigKeywords.get(j).size(); k++) {
							if (job.getField(className).contains(bigKeywords.get(j).get(k))) {
								// increase match count, and move on to next class
								matchCount++;
								continue classLoop; // to avoid double counting matches in a match sectin
							}
						}
					}
				}
			}
			
			// if not all INs have found a match, return false
			if (matchCount != inCount) {
				//System.out.println(inMatch);
				//System.out.println(inCount);
				return false;
			}
			
			// if job has not been ruled out, include job
			return true;
		}
		
		// if job is already in database, rule out
		return false;
	}

	public void match(Job job) {
		// matches POS/NEG keywords of job; adjusts job score
		// TODO: make it return a List of matching keywords
		
		ArrayList<String> classSections = keywords.getClassSections();
		ArrayList<ArrayList<ArrayList<ArrayList<String>>>> words = keywords.getKeywords();
		ArrayList<ArrayList<String>> bigKeywords = new ArrayList<ArrayList<String>>();
		String className = null;
		// create smartIndex for efficient/intelligent keyword analysis
		int[][][] smartIndex = keywords.getSmartIndex();
		
		// Third, loop through POS to score job
		for (int i=0; i<classSections.size(); i++) {
			className = classSections.get(i);
			// if class i has an POS match type, loop through keywords
			if (smartIndex[2][i][0] != -1) {
				bigKeywords = words.get(i).get(smartIndex[2][i][0]);
				keywordLoop:
				for (int j=0; j<bigKeywords.size(); j++) {
					for (int k=0; k<bigKeywords.get(j).size(); k++) {
						if (job.getField(className).contains(bigKeywords.get(j).get(k))) {
							// increase job upScore, add word to posMatches, move on to next row of keywords
							job.up();
							job.addPosKeyword(bigKeywords.get(j).get(k));
							continue keywordLoop; // to avoid double counting matches in a keyword row
						}
					}
				}
			}
		}
		
		// Fourth, loop through NEG to score job
		for (int i=0; i<classSections.size(); i++) {
			className = classSections.get(i);
			// if class i has an POS match type, loop through keywords
			if (smartIndex[3][i][0] != -1) {
				bigKeywords = words.get(i).get(smartIndex[3][i][0]);
				keywordLoop:
				for (int j=0; j<bigKeywords.size(); j++) {
					for (int k=0; k<bigKeywords.get(j).size(); k++) {
						if (job.getField(className).contains(bigKeywords.get(j).get(k))) {
							// increase job downScore, add word to negMatches, move on to next row of keywords
							job.down();
							job.addNegKeyword(bigKeywords.get(j).get(k));
							continue keywordLoop; // to avoid double counting matches in a keyword row
						}
					}
				}
			}
		}
	}
}
