package littlelui.krosswords.fetch;

import java.util.List;

public interface Fetcher {
  public List/*<String>*/ fetchAvailablePuzzleIds(FetchCallback listener);
}
