package edu.nchu.cs.ai.solution;

public abstract class Solution<T> {
	private T answer;
	private double objectiveValue;

	public Solution(T answer) {
		this.answer = answer;
	}

	public void setAns(T answer) {
		this.answer = answer;
	}

	public T getAns() {
		return this.answer;
	}

	public void setObjectiveValue(double objectiveValue) {
		this.objectiveValue = objectiveValue;
	}

	public double getObjectiveValue() {
		return this.objectiveValue;
	}

	@Override
	public abstract String toString();
}
