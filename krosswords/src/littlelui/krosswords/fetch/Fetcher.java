package littlelui.krosswords.fetch;

import java.util.Collection;

import littlelui.krosswords.catalog.PuzzleListEntry;
import littlelui.krosswords.model.Puzzle;

public interface Fetcher {
	/** Update the set of known puzzles.
	 * 
	 *  @param known All known puzzles.
	 *  @return a collection of new (or updated) puzzles.
	 */
  public Collection/*<PuzzleListEntry>*/ fetchAvailablePuzzleIds(Collection/*<PuzzleListEntry>*/ known);
  
  
  /** Fetch the puzzle data for the given PuzzleListEntry. 
   *  Feel free to throw exceptions here, they will be caught. 
 * @throws Exception 
   */
  public Puzzle fetchPuzzle(PuzzleListEntry listEntry) throws Exception;
}
