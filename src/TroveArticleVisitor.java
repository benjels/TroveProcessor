import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.*;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONObject;

/**
 * visits all of the files in a given directory and uses the supplied ArticleTopicExaminer on them.
 * NOTE: This will not gracefully handle exceptions incurred when supplied; the files must be valid json, have the .json file extension, and include a string retreived with the "fulltext" key.
 * Every Trove article that I have seen meets these condition. 
 * TODO: this will need to be alterred somewhat when we process the actual trove data because at the moment I am using 1 file per article but in reality it will be 1 file per issue.
 * @author max
 *
 */
public class TroveArticleVisitor implements FileVisitor{


	private final ArticleExaminer troveExaminer;
	private final HashMap<String, WikipediaTopic> topics;
	private final HashMap<Integer, TroveArticle> troves;



	/**
	 * makes a new ArticleVisitor that will be able to use the supplied ArticleTopicExaminer on each article it encounters.
	 * @param troveSourceArticlesDir 
	 * @param topicRelatedWikipediaTitlesDir 
	 * @param examiner the ArticleTopicExaminer that uses an instance of a Wikipedia object (from the wikipedia miner API) to determine which topics are key in some article.
	 * @throws IOException when reading and parsing the topics file fails
	 */
	public TroveArticleVisitor(String topicRelatedWikipediaTitlesDir,  ArticleExaminer examiner) throws IOException {
		this.troveExaminer = examiner;
		//make our map of wikipedia article titles that are related to our topic (populated from file)
		System.out.println("creating the hashset of topic-related wikipedia articles");
		BufferedReader br = new BufferedReader(new FileReader(topicRelatedWikipediaTitlesDir)); 
		HashMap<String, WikipediaTopic> topicRelatedTitles = new HashMap<>();
		String eachTitle = null;
		while((eachTitle = br.readLine()) != null){
			topicRelatedTitles.put(eachTitle, new WikipediaTopic(eachTitle));
		}
		this.topics = topicRelatedTitles;
		//make our map of trove articles (populated as we traverse the trove article files later on)
		this.troves = new HashMap<>();
	}
	
	/**
	 * starts recursively visiting files starting from the supplied directory. When a .json files is encountered, passes the string resultant from
	 * the 'fulltext' key to the findTopicsInText method of the ArticleTopicExaminer. 
	 * @param directoryToExplore
	 */
	public TroveExplorerDump exploreDirectory(String directoryToExplore) {
		//start the file visitor on its journey
		Path startPath = FileSystems.getDefault().getPath(directoryToExplore);
		System.out.println("starting walk of file tree...");
		long startTime = System.currentTimeMillis();
		try {
			Files.walkFileTree(startPath, this);
			System.out.println("file visiting finished naturally :)");
		} catch (IOException e) {
			//TODO: this will be thrown e.g. with those unexpected json things. WHAT HAPPENS WITH CONSTRUCTING A NEW JSON FILE WHEN THE EXTENSION IS .json BUT NOT VALID JSON?
			System.out.println("IO EXCEPTION INCURRED DURING THE VISITOR'S WALK :(");
			e.printStackTrace();
		}finally{
			System.out.println("finished walking file tree after approx " + ((System.currentTimeMillis() - startTime)/1000)  + " seconds : )" );
			return new TroveExplorerDump(this.topics, this.troves);
		}
		
	}

	
	
	@Override
	public FileVisitResult visitFile(Object filePath, BasicFileAttributes arg1) throws IOException{
		Path possibleJsonFilePath = (Path)filePath;
		File possibleJsonFile = possibleJsonFilePath.toFile();
		//at this stage we have a file, but we do not know if it is useful to us.
		if(possibleJsonFile.toString().substring(possibleJsonFile.toString().length() - 5, possibleJsonFile.toString().length()).equals(".json")){
			JSONObject JSONArticle = new JSONObject(Files.readAllLines(possibleJsonFilePath, Charset.availableCharsets().get("ISO-8859-1")).get(0));
			//now we have our json version of the article, run the topic examiner over it
			try {
				this.troveExaminer.findTopicsInJSONArticle(JSONArticle, this.topics, this.troves); 
			} catch (Exception e) {
				System.out.println("exception incurred trying to get the value associated with \"fulltext\" key in the file: " + filePath);
				e.printStackTrace();
			}
		}else{
			System.out.println("encountered a non-json file: " + filePath);
		}
		
		
		return java.nio.file.FileVisitResult.CONTINUE;
	}
	
	@Override
	public FileVisitResult postVisitDirectory(Object arg0, IOException arg1)
			throws IOException {
		return java.nio.file.FileVisitResult.CONTINUE;
	}

	@Override
	public FileVisitResult preVisitDirectory(Object arg0,
			BasicFileAttributes arg1) throws IOException {
		return java.nio.file.FileVisitResult.CONTINUE;
	}

	@Override
	public FileVisitResult visitFileFailed(Object arg0, IOException arg1)
			throws IOException {
		throw new RuntimeException("visiting a file failed: "  + arg0);
	}



}
