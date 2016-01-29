import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.HashSet;

import javax.xml.parsers.ParserConfigurationException;

import org.json.JSONObject;
import org.wikipedia.miner.model.Wikipedia;
import org.xml.sax.SAXException;

import com.sleepycat.je.EnvironmentLockedException;


public class MainScript {
	
	//some config vars mostly file paths that are easier to alter here...
	private static final String TROVE_SOURCE_ARTICLES_DIR = "C:\\!2015SCHOLARSHIPSTUFF\\dummyNzTest\\";
	private static final String WIKI_CFG_DIR = "C:\\Users\\user\\workspace\\wikipedia-miner-1.2.0\\configs\\en.xml";
	private static final String TOPIC_RELATED_WIKIPEDIA_TITLES_DIR = "C:\\python_environments\\WikipediaListArticleFinderGitDir\\WikipediaListArticleFinderPycharmProj\\29thlog.txt";
	private static final String TOPIC_JSON_OUTPUT_DIR = "WikipediaJSON\\";
	private static final String TROVE_JSON_OUTPUT_DIR = "TroveJSON\\";
	
	
	
	public static void main(String[] args){
		try {
			script();
		} catch (Exception e) {
			System.out.println("exception thrown by the top level script.");
			e.printStackTrace();
		}
	}
	
	/**
	 * script that...
	 * -creates the wikipedia miner objects necessary to parse our text
	 * -reads in the set of wikipedia titles that we have determined are NZ topics and generates a WikipediaTopic object for each one
	 * -parses the supplied articles and generates a TroveArticle object for each one
	 * -after parsing all of the trove articles, writes all of the WikipediaTopic objects and TroveArticle objects to files.
	 * @throws Exception
	 *
	 */
	private static void script() throws Exception{
		//make our wikipedia object
		System.out.println("making the wikipedia...");
		File wikipediaCFG = new File(WIKI_CFG_DIR);
		Wikipedia wikipedia = new Wikipedia(wikipediaCFG, false);
		//create our FileVisitor that will visit all of the source trove articles and the ArticleExaminer which the ArticleVisitor will pass individual Trove articles to in order for them to have their corresponding TroveArticle object created and the information in the related WikipediaTopics filled in etc.
		ArticleExaminer examiner = new ArticleExaminer(wikipedia);
		TroveArticleVisitor visitor = new TroveArticleVisitor(TOPIC_RELATED_WIKIPEDIA_TITLES_DIR, examiner);
		//set the file visitor off on its journey
		TroveExplorerDump result = visitor.exploreDirectory(TROVE_SOURCE_ARTICLES_DIR);
		//finally, write our results to files for use by our web app
		
		//write trove objects
		for(TroveArticle eachTroveArticle: result.troves.values()){
	    	//fill out the json object
	    	JSONObject troveJSON = new JSONObject();
	    	JSONObject articleJSON = eachTroveArticle.getOriginalJSON();
	    	troveJSON.put("uid", articleJSON.getInt("id"));
	    	troveJSON.put("title", articleJSON.getString("heading"));
	    	troveJSON.put("relatedTopics", eachTroveArticle.getRelatedTopicTitles());
	    	troveJSON.put("fulltext", articleJSON.getString("fulltext"));
	    	troveJSON.put("wikitext", eachTroveArticle.getWikitext());
	    	//now write our json to file...
	    	String JSONText = troveJSON.toString(1);//TODO: don't know if 1 is a sensible indent factor...
	    	PrintWriter writer = new PrintWriter(TROVE_JSON_OUTPUT_DIR + articleJSON.getInt("id") + ".json");
	    	writer.write(JSONText);
	    	writer.close();
	     }
		//write topic objects
        for(WikipediaTopic eachTopic: result.topics.values()){
        	//fill out the JSON object 
        	JSONObject topicJSON = new JSONObject();
        	topicJSON.put("title", eachTopic.getTitle());
        	topicJSON.put("relatedTroveArticles", eachTopic.getRelatedTroveArticleIDs());
        	//make a writer to write this JSON to file...
        	String JSONText = topicJSON.toString(1);//TODO: no idea if 5 is a sensible indent factor. we'll see.
        	PrintWriter writer = new PrintWriter(TOPIC_JSON_OUTPUT_DIR + sanitiseFileName(eachTopic.getTitle()) + ".json");
        	writer.write(JSONText);
        	writer.close();
        }
	}
	
	
	/**
	 * removes any characters that cannot belong in a file name
	 * @param string the string that needs to have its illegal characters removed (e.g. ", ?, & etc)
	 * @return the string with all illegal characters replaced with the character "Q"
	 */
	public static String sanitiseFileName(String dirtyString) {
		//take care of illegal chars
		String cleanString = dirtyString.replaceAll(" ", "_").replaceAll("\n", "__").replaceAll("[?\"#%&{}\\<>*/$!':@+`|=]", "Q").toLowerCase();
		//truncate the file name if it is too long (note that the library in charge of writing to files throws a syntax exception if the file name exceeds approx 300 chars. For now I will set the upper limit of the article description component of the file names to 150 chars)
		if(cleanString.length() > 150){
			int amountRemoved = cleanString.length() -150;
			cleanString = cleanString.substring(0, 150);
			cleanString += "(...truncated " + amountRemoved + " characters)";
		}
		return cleanString;
	}
	
	
}
