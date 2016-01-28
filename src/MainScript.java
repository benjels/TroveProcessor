import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;

import javax.xml.parsers.ParserConfigurationException;

import org.wikipedia.miner.model.Wikipedia;
import org.xml.sax.SAXException;

import com.sleepycat.je.EnvironmentLockedException;


public class MainScript {
	
	//some config vars mostly file paths that are easier to alter here...
	private static final String TROVE_SOURCE_ARTICLES_DIR = "C:\\!2015SCHOLARSHIPSTUFF\\dummyNzTest\\";
	private static final String WIKI_CFG_DIR = "C:\\Users\\user\\workspace\\wikipedia-miner-1.2.0\\configs\\en.xml";
	private static final String TOPIC_RELATED_WIKIPEDIA_TITLES_DIR = "";//TODO: this should be the output of my new cool python scraper jah feel
	private static final String TOPIC_JSON_OUTPUT_DIR = "";
	private static final String TROVE_JSON_OUTPUT_DIR = "";
	
	
	
	public static void main(String[] args){
		try {
			script();
		} catch (Exception e) {
			System.out.println("exception thrown by the script.");
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
		//make our map of wikipedia article titles that are related to our topic (populated from file)
		System.out.println("creating the hashset of topic-related wikipedia articles");
		BufferedReader br = new BufferedReader(new FileReader(TOPIC_RELATED_WIKIPEDIA_TITLES_DIR));
		HashMap<String, WikipediaTopic> topicRelatedTitles = new HashMap<>();
		String eachTitle = null;
		while((eachTitle = br.readLine()) != null){
			topicRelatedTitles.put(eachTitle, new WikipediaTopic(eachTitle));
		}
		//make our wikipedia object
		System.out.println("making the wikipedia...");
		File wikipediaCFG = new File(WIKI_CFG_DIR);
		Wikipedia wikipedia = new Wikipedia(wikipediaCFG, false);
		//create our FileVisitor that will visit all of the source trove articles and the ArticleExaminer which the ArticleVisitor will pass individual Trove articles to in order for them to have their corresponding TroveArticle object created and the information in the related WikipediaTopics filled in etc.
		...
		//finally, write the WikipediaTopic and TroveArticle objects to json files for use by the TroveExplorer python API
		...
		
		todo: think of a cool way of not making those two hashmaps public, we like passing stuff around
	}
}
