package org.cfrehr.creepycrawler.keywords;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

//object for keyword storage

public final class Keywords {
	
	// fields
	private ArrayList<String> classSections; // a list of class sections
	private ArrayList<ArrayList<String>> matchSections; // lists of matching sections indexed by class sections
	private ArrayList<ArrayList<ArrayList<ArrayList<String>>>> keywords; // list of lists of keywords indexed by matching section, indexed by class section
	private int upCount = 0;
	private int downCount = 0;
	
	// TODO: This class really, REALLY needs to be simplified. Creating classes for "class sections" and "match sections" may help greatly in simplifying
	//       all these lengthy, nested loops and lists.
	public Keywords(List<String> keywordsList) {
		
		classSections = new ArrayList<String>(); // list of class names
		matchSections = new ArrayList<ArrayList<String>>(); // lists of match names by class name
		keywords = new ArrayList<ArrayList<ArrayList<ArrayList<String>>>>(); // lists of lists of keywords, by matches, by classes
		

		ArrayList<String> matchNames = new ArrayList<String>();
		ArrayList<ArrayList<ArrayList<String>>> matchList = new ArrayList<ArrayList<ArrayList<String>>>();
		ArrayList<String> keywordList = new ArrayList<String>();
		ArrayList<ArrayList<String>> bigKeywordList = new ArrayList<ArrayList<String>>();
		boolean storeKeys = false;
		boolean storeMatches = false;
		
		// iterate through list
		Iterator<String> itr = keywordsList.iterator();
		String currLine;
		
		// extract keyword data
		// TODO: will work with non-empty fields, but need to fine-tune for keyword cases where
		//		 no keywords are present under a matching section
		do {
			currLine = itr.next();
			// store class sections
			if (currLine.substring(0, 3).equals("## ")) {
				if (storeKeys && storeMatches) {
					matchList.add(bigKeywordList);
					keywords.add(matchList);
					matchSections.add(matchNames);
					storeMatches = false;
					storeKeys = false;
				}
				// add new class and continue building
				classSections.add(currLine.substring(3).toLowerCase());
				matchNames = new ArrayList<String>();
				matchList = new ArrayList<ArrayList<ArrayList<String>>>();
			// store matching sections	
			} else if (currLine.substring(0,2).equals("# ")) {
				if (storeKeys) {
					matchList.add(bigKeywordList);
					storeMatches = true;
					storeKeys = false;
				}
				matchNames.add(currLine.substring(2));
				keywordList = new ArrayList<String>();
				bigKeywordList = new ArrayList<ArrayList<String>>();
			// store each line of keyword(s)
			} else {
				storeKeys = true;
				String[] words = currLine.split(",");
				for (int i=0; i<words.length; i++) {
					keywordList.add(words[i].toLowerCase());
				}
				bigKeywordList.add(keywordList);
				keywordList = new ArrayList<String>();
			}
		} while(itr.hasNext());
		// store final class section (that loop was unable to account for)
		matchList.add(bigKeywordList);
		keywords.add(matchList);
		matchSections.add(matchNames);
		
		// get upCount and downCount: count POS and NEG keyword lines (iterate through arrays)
		for (int i=0; i<classSections.size(); i++) {
			for (int j=0; j<matchSections.get(i).size(); j++) {
				if (matchSections.get(i).get(j).equals("POS")) {
					upCount = keywords.get(i).get(j).size();
					
				} else if (matchSections.get(i).get(j).equals("NEG")) {
					downCount = keywords.get(i).get(j).size();
				}
			}
		}
	}
	
	// smartIndex tells you whether a key class has a particular matchName or not, and also where it's located
	public int[][][] getSmartIndex() {
		
		int matchCard = 4; // match cardinality is at most 4, since it can take values OUT, IN, POS, NEG
		int classSize = classSections.size();
		// int [][][] :
		// 		(1) match type: [ 0=OUT, 1=IN, 2=POS, 3=NEG) ]
		//		(2) class index: index of current classSection [ 1=TITLE, 2=DESCRIPTION, etc ]  **actual class name not important
		//		(3) match index: index of current matchSection IF if it equals match type, ELSE -1
		//				- a smartly ordered matchSection index makes it easier to perform OUT analysis before others, etc
		//				- a -1 indicates that the given class section has no match section that equals match type
		int[][][] smartIndex = new int[matchCard][classSize][1]; 
		
		// index OUTs
		classLoop:
		for (int i=0; i<classSize; i++) {
			int matchSize = matchSections.get(i).size();
			for (int j=0; j<matchSize; j++) {
				String currMatch = matchSections.get(i).get(j);
				// stores match section index j
				if (currMatch.equals("OUT")) {
					smartIndex[0][i][0] = j;
					continue classLoop;
				}
				// if no matches found, store -1
				if (j == matchSize-1) {
						smartIndex[0][i][0] = -1;
				}
			}
		}
		// index INs
		classLoop:
		for (int i=0; i<classSize; i++) {
			int matchSize = matchSections.get(i).size();
			for (int j=0; j<matchSize; j++) {
				String currMatch = matchSections.get(i).get(j);
				// stores match section index j
				if (currMatch.equals("IN")) {
					smartIndex[1][i][0] = j;
					continue classLoop;
				}
				// if no matches found, store -1
				if (j == matchSize-1) {
						smartIndex[1][i][0] = -1;
				}
			}
		}
		// index POSs
		classLoop:
		for (int i=0; i<classSize; i++) {
			int matchSize = matchSections.get(i).size();
			for (int j=0; j<matchSize; j++) {
				String currMatch = matchSections.get(i).get(j);
				// stores match section index j
				if (currMatch.equals("POS")) {
					smartIndex[2][i][0] = j;
					continue classLoop;
				}
				// if no matches found, store -1
				if (j == matchSize-1) {
						smartIndex[2][i][0] = -1;
				}
			}
		}
		// index NEGs
		classLoop:
		for (int i=0; i<classSize; i++) {
			int matchSize = matchSections.get(i).size();
			for (int j=0; j<matchSize; j++) {
				String currMatch = matchSections.get(i).get(j);
				// stores match section index j
				if (currMatch.equals("NEG")) {
					smartIndex[3][i][0] = j;
					continue classLoop;
				}
				// if no matches found, store -1
				if (j == matchSize-1) {
						smartIndex[3][i][0] = -1;
				}
			}
		}
		
		return smartIndex;
	}

	// returns classSection names
	public ArrayList<String> getClassSections() {
		return this.classSections;
	}

	// returns matchSection names
	public ArrayList<ArrayList<String>> getMatchSections() {
		return this.matchSections;
	}
	
	// returns keywords list
	public ArrayList<ArrayList<ArrayList<ArrayList<String>>>> getKeywords() {
		return this.keywords;
	}
	
	// TODO: Build method
	// *Cant remember why I added this... maybe to help index matching keywords, so that they could be returned?
	// returns index of classSection name in array
	public int getClassIndex() {
		return 0;
	}
	
	// TODO: Build method
	// *Cant remember why I added this... maybe to help index matching keywords, so that they could be returned?
	// returns index of matchSection name in array
	public int getMatchIndex() {
		return 0;
	}
	
	// returns number of POS keyword lines
	public int getUp() {
		return this.upCount;
	}
	
	// returns number of NEG keyword lines
	public int getDown() {
		return this.downCount;
	}
	
}