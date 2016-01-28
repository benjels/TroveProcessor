import java.util.HashSet;


import org.json.JSONObject;


public class TroveArticle {
/**
 * represents a Trove article as will be used by our little python web app.
 * needs a reference to the original article json as well as some other info. e.g. 
 * the wikipedia topics that are related to this article.
 */
	
	private final JSONObject originalJSON;
	private final HashSet<String> relatedTopicTitles = new HashSet<>();
	private String wikitext;
	
	
	TroveArticle(JSONObject json){
		this.originalJSON = json;
	}
	
	public boolean addRelatedWikipediaTopic(String topicTitle){
		return this.relatedTopicTitles.add(topicTitle);
	}
	
	public void setWikitext(String wikitext){
		assert (this.wikitext == null): "we should not be setting the wikitext if it has already been set";
		this.wikitext = wikitext;
	}
	
	
	
	
	public HashSet<String> getRelatedTopicTitles(){
		return this.relatedTopicTitles;
	}
	
	
	public JSONObject getOriginalJSON(){
		return this.originalJSON;
	}
	
	public String getWikitext(){
		return this.wikitext;
	}
	
	
}
