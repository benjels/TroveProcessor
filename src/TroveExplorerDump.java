import java.util.HashMap;

/**
 * plain old java object container for filled out trove and topic objects ready to be written to json.
 * @author user
 *
 */
public class TroveExplorerDump {

	public final HashMap<Integer, TroveArticle> troves;
	public final HashMap<String, WikipediaTopic> topics;

	public TroveExplorerDump(HashMap<String, WikipediaTopic> topics, HashMap<Integer, TroveArticle> troves) {
		this.troves = troves;
		this.topics = topics;
	}

}
