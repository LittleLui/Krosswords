package littlelui.krosswords.model;

import java.io.Serializable;

public class Word implements Serializable {
	private static final long serialVersionUID = 1L;
	
	public static final int DIRECTION_HORIZONTAL = 1000;
	public static final int DIRECTION_VERTICAL = 2000;
	
	private int x;
	private int y;
	private int length;
	private int direction;
	private int crossDirection;
	
	private int location;
	private String key;
	private String hint;
	
	private String solution="";
	private String expectedSolution=null;
	
	//marks to indicate validation failure
	private boolean[] marks;
	
	public Word(int x, int y, int length, int direction, int key, String hint) {
		this(x, y, length, direction, ""+key, hint);
	}

	public Word(int x, int y, int length, int direction, String key, String hint) {
		super();
		this.x = x;
		this.y = y;
		this.length = length;
		this.marks = new boolean[length];
		
		if (direction != DIRECTION_HORIZONTAL && direction != DIRECTION_VERTICAL)
			throw new IllegalArgumentException("Direction has to be either horizontal or vertical.");
		
		this.direction = direction;
		if (direction == DIRECTION_HORIZONTAL)
			crossDirection = DIRECTION_VERTICAL;
		else
			crossDirection = DIRECTION_HORIZONTAL;
			
		this.key = key;
		this.hint = hint;
	}

	
	
	public void setLocation(int location) {
		this.location = location;
	}

	public int getLocation() {
		return location;
	}

	public static int getDirectionHorizontal() {
		return DIRECTION_HORIZONTAL;
	}

	public static int getDirectionVertical() {
		return DIRECTION_VERTICAL;
	}

	public int getX() {
		return x;
	}

	public int getY() {
		return y;
	}

	public int getLength() {
		return length;
	}

	public int getDirection() {
		return direction;
	}
	
	public int getCrossDirection() {
		return crossDirection;
	}

	public String getKey() {
		return key;
	}

	public String getHint() {
		return hint;
	}

	public String getSolution() {
		return solution;
	}

	public void setSolution(String solution) {
		String old = this.solution;
		this.solution = solution.toUpperCase();
		if (!old.equals(solution)) {
			clearMarks();
		}
	}

	public boolean[] getMarks() {
		return marks;
	}

	private void clearMarks() {
		for (int i=0; i<length; i++)
			marks[i] = false;
	}

	public void setSolution(int i, String text) {
		String old = this.solution;

		if (text == null || text.length() == 0)
			text = " ";
		
		if (text.length() > 1)
			text = text.substring(0, 1);
		
		if (solution.length() <= i) {
			solution = solution + spaces(length - solution.length());
		}

		solution = solution.substring(0, i) + text + solution.substring(i+1);

		if (!old.equals(solution)) {
			clearMarks();
		}
	}
	
	public String getExpectedSolution() {
		return expectedSolution;
	}

	public void setExpectedSolution(String expectedSolution) {
		this.expectedSolution = expectedSolution;
	}

	private String spaces(int i) {
		String s = "";
		
		for (int j=0; j<i; j++)
			s += " ";
		
		return s;
	}

	//return true if all letters that have been entered yet (!) are correct
	public boolean verify() {
		clearMarks();
		
		boolean allGood = true;
		
		for (int i=0; i<solution.length(); i++) {
			char s = solution.charAt(i);
			if (s != ' ' && expectedSolution != null && expectedSolution.length() > i) {
				char v = expectedSolution.charAt(i);
				if (s != v) {
					marks[i] = true;
					allGood = false;
				}
			}
		}

		return allGood;
	}
	
	
	
	

}
