package edu.nchu.cs.ai.bean;

public class City {
	private String name;
	private double posX;
	private double posY;
	public City(String name, double posX, double posY) {
		this.name = name;
		this.posX = posX;
		this.posY = posY;
	}
	public String getName() {
		return this.name;
	}
	public double getPosX() {
		return this.posX;
	}
	public double getPosY() {
		return this.posY;
	}

}
