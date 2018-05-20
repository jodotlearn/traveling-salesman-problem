package edu.nchu.cs.ai.bean;

public class Path {
	private int id;
	private City from;
	private City to;
	private double distance;
	private double pheromone;

	public Path(City from, City to, double distance, double pheromone) {
		this.from = from;
		this.to = to;
		this.distance = distance;
		this.pheromone = pheromone;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getId() {
		return this.id;
	}

	public City getFrom() {
		return this.from;
	}
	public void setFrom(City from) {
		this.from = from;
	}
	public City getTo() {
		return this.to;
	}
	public void setTo(City to) {
		this.to = to;
	}
	public double getDistance() {
		return this.distance;
	}
	public void setDistance(double distance) {
		this.distance = distance;
	}
	public double getPheromone() {
		return this.pheromone;
	}
	public void setPheromone(double pheromone) {
		this.pheromone = pheromone;
	}

}
