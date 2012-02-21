package littlelui.krosswords.fetch;

import java.util.Collection;

import littlelui.krosswords.catalog.PuzzleListEntry;
import littlelui.krosswords.catalog.PuzzleSolution;
import littlelui.krosswords.model.Puzzle;

/** Fetcher interface. Implement this to add more puzzle sources. Don't forget to register it in the correct places!
 * 
 * @author LittleLui
 *
 * Copyright 2011-2012 Wolfgang Groiss
 * 
 * This file is part of Krosswords.
 * 
 * Krosswords is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>. 
 *
 **/
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

  public PuzzleSolution fetchSolution(PuzzleListEntry ple) throws Exception;
}
