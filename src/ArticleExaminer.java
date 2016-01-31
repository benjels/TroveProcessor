import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONObject;
import org.wikipedia.miner.annotation.Disambiguator;
import org.wikipedia.miner.annotation.Topic;
import org.wikipedia.miner.annotation.TopicDetector;
import org.wikipedia.miner.annotation.preprocessing.DocumentPreprocessor;
import org.wikipedia.miner.annotation.preprocessing.PreprocessedDocument;
import org.wikipedia.miner.annotation.preprocessing.WikiPreprocessor;
import org.wikipedia.miner.annotation.tagging.DocumentTagger;
import org.wikipedia.miner.annotation.tagging.DocumentTagger.RepeatMode;
import org.wikipedia.miner.annotation.tagging.WikiTagger;
import org.wikipedia.miner.annotation.weighting.LinkDetector;
import org.wikipedia.miner.model.Wikipedia;


public class ArticleExaminer {

	private final Wikipedia wikipedia;
    private final Disambiguator disambiguator ;
    private final TopicDetector topicDetector ;
    private final LinkDetector linkDetector ;
    private final DocumentTagger tagger ;
    private final DocumentPreprocessor preprocessor ;

	public ArticleExaminer(Wikipedia wikipedia) throws Exception {
		//create the objects from the WikipediaMinerAPI
		this.wikipedia = wikipedia;
		this.preprocessor = new WikiPreprocessor(wikipedia) ;
    	disambiguator = new Disambiguator(wikipedia) ;//
    	topicDetector = new TopicDetector(wikipedia, disambiguator) ;//
    	linkDetector = new LinkDetector(wikipedia) ;///
    	tagger = new WikiTagger() ;
	}

	
	
	public void findTopicsInJSONArticle(JSONObject JSONArticle, HashMap<String, WikipediaTopic> topics, HashMap<Integer, TroveArticle> troves) throws Exception {
		//pre process our article's text
		String articleBodyText = JSONArticle.getString("fulltext");
		PreprocessedDocument processedArticleText = preprocessor.preprocess(articleBodyText);
		Collection<Topic> articleTopics = this.topicDetector.getTopics(processedArticleText, null);
		
		//now examine the topics in our text
		for(Topic eachTopic: articleTopics){
			if(topics.get(eachTopic.getTitle().toLowerCase()) != null){//i.e. if this topic we found in the trove articles is one of the topics we identified with out python wikipedia scraper...
				//if we found one of our topics, then this is an nz article that we want to keep and process further
				troves.put(JSONArticle.getInt("id"), new TroveArticle(JSONArticle));
				//add this trove article as a related article of this topic
				topics.get(eachTopic.getTitle().toLowerCase()).addRelatedTroveArticleID(JSONArticle.getInt("id"));
				//add this topic as a related topic of this trove article
				troves.get(JSONArticle.getInt("id")).addRelatedWikipediaTopic(eachTopic.getTitle().toLowerCase());
			}
		}
		//if we found one of our nz topics in the trove article, we should add some additional info
		if(troves.get(JSONArticle.getInt("id")) != null){
			Collection<Topic> relevantTopics = new ArrayList<>();
			for(Topic eachTopic: articleTopics){
				if(topics.containsKey(eachTopic.getTitle().toLowerCase())){
					relevantTopics.add(eachTopic);
				}
				else{//DEBUG ONLY
					System.out.println("NOT adding the topic " + eachTopic.getTitle().toLowerCase() + " to the tagging, because it is not an nz topic");
				}
			}
			String wikiMarkup = this.tagger.tag(processedArticleText, relevantTopics, RepeatMode.ALL);
			troves.get(JSONArticle.getInt("id")).setWikitext(processWikiMarkup(wikiMarkup));
		}
	}
	
	 /**
     * converts a string of wikimedia markup text to a similar string where the titles of detected topics are replaced by URLs that link to the articles for those topics
     * @param wikiText the original text
     * @return the processed text with URLs
     */
	private String processWikiMarkup(String wikiText){
		String alterredWikiText = wikiText;
		System.out.println("processing: " + alterredWikiText);
		Pattern pattern = Pattern.compile("\\[\\[[^\\]|\\|]*\\]\\]|\\[\\[[^\\[\\[]*\\|");
		Matcher titleFinder = pattern.matcher(alterredWikiText);
		int indexToProcessFrom = 0;
		while(titleFinder.find(indexToProcessFrom)){
			//find a topic title and insert the URL version of it. 
			String aTitle = alterredWikiText.substring(titleFinder.start(), titleFinder.end());
			String URLOfFoundTitle = "https://en.wikipedia.org/wiki/" + aTitle.replaceAll("\\[\\[|\\||\\]\\]", "").replaceAll(" ", "_");
			StringBuffer text = new StringBuffer(alterredWikiText);
			text = text.insert(titleFinder.start(), "[ " + URLOfFoundTitle + " ]");
			alterredWikiText = text.toString();
			indexToProcessFrom = titleFinder.start() + (titleFinder.end() - titleFinder.start()) + URLOfFoundTitle.length() + 2;//i.e. the offset where it was first found + length of inserted URL + length of detected titles
			titleFinder = pattern.matcher(alterredWikiText);
			
		}
		//we finished processing the article, so spit out the processed string
		return alterredWikiText;
	}

}

