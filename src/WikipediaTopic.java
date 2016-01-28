import java.util.HashSet;

/**
 * represents a New Zealand related wikipedia article that has some attached information that our web app needs (i.e. trove articles
 * associated with a particular wikipedia article)
 * @author max
 *
 */
public class WikipediaTopic {

	private final String title;
	private final HashSet<Integer> relatedTroveArticleIDs = new HashSet<>();
	
	WikipediaTopic(String lowerCaseArticleTitle){
		this.title = lowerCaseArticleTitle;
	}
	
	/**
	 * used —when we find a related trove article— to keep a record of that trove article.
	 * @param title the title of the trove article
	 * @return true if the title is successfully added to the set, else false.
	 */
	public boolean addRelatedTroveArticleID(int id){
		return this.relatedTroveArticleIDs.add(id);
	}
	
	
	public HashSet<Integer> getRelatedTroveArticleIDs(){
		return this.relatedTroveArticleIDs;
	}
	
	public String getTitle(){
		return this.title;
	}
	


	
	
	//NOTE THAT EQUALS() JUST USES TITLE STRING. This is so that I can just use contains() to check whether a found topic in the trove articles matches a new zealand topic.
	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((title == null) ? 0 : title.hashCode());
		return result;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof WikipediaTopic)) {
			return false;
		}
		WikipediaTopic other = (WikipediaTopic) obj;
		if (title == null) {
			if (other.title != null) {
				return false;
			}
		} else if (!title.equals(other.title)) {
			return false;
		}
		return true;
	}
	
	
	
}
