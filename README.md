# CreepyCrawler
  CreepyCrawler is a web crawling program that spiders through company career pages to find new job opportunities.  Given a list of companies to search, job description keywords, and database schemas, the crawler will filter out undesirable positions, score the remaining opportunities, and return an ordered list to the user. CreepyCrawler can extract position title, ID, description, apply URL, location, and recruiter contact info.

# How to build and test yourself

1) Create new simple Maven project in Eclipse (packaging type = jar)

2) Add "CreepyCrawler.java" to /src/main/java

3) Add "creepy-crawler-0.0.1.jar" to build path

4) Add input text files to /src/main/resources

      "companies.txt"

      "jobs.txt"

      "keywords.txt"

      "searchKey.txt"

5) Add dependencies to from "crawlerPom.xml" to "pom.xml"
      
6) (Optional) Toggle arguments in "keywords.txt"

7) Run application, view output in "newJobs.txt"
      
