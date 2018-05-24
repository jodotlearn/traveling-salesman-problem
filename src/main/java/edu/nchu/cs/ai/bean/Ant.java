package edu.nchu.cs.ai.bean;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class Ant {
	private double pheromone;
	private double pheromoneAffectRate;
	private double distanceAffectRate;
	private List<City> tourCity;
	private List<Path> tourPath;
	private double tourDistance;

	public Ant(double pheromone, double pheromoneAffectRate, double distanceAffectRate) {
		this.pheromone = pheromone;
		this.pheromoneAffectRate = pheromoneAffectRate;
		this.distanceAffectRate = distanceAffectRate;
		this.tourCity = new ArrayList<>();
		this.tourPath = new ArrayList<>();
		this.tourDistance = 0;
	}

	public double getPheromone() {
		return this.pheromone;
	}

	public void resetTourHistory() {
		this.tourCity.clear();
		this.tourPath.clear();
		this.tourDistance = 0;
	}

//	public void startTour(List<City> cities, List<Path> paths, City startCity) {
//		this.tourCity.add(startCity);
//		while (this.tourCity.size() < cities.size()) {
//			Path path = this.findNextPath(paths);
//			this.tourCity.add(path.getTo());
//			this.tourPath.add(path);
//			this.tourDistance += path.getDistance();
//		}
//		for (Path path:paths) {
//			if (path.getFrom() == this.tourCity.get(this.tourCity.size()-1) && path.getTo() == startCity) {
//				this.tourCity.add(path.getTo());
//				this.tourPath.add(path);
//				this.tourDistance += path.getDistance();
//				break;
//			}
//		}
//	}

	public void startTour(List<City> cities, Map<String,Map<String,Path>> paths, City startCity) {
		this.tourCity.add(startCity);
		while (this.tourCity.size() < cities.size()) {
			Path path = this.findNextPath(paths);
			this.tourCity.add(path.getTo());
			this.tourPath.add(path);
			this.tourDistance += path.getDistance();
		}
		Path pathToStart = paths.get(this.tourCity.get(this.tourCity.size()-1).getName()).get(startCity.getName());
		this.tourCity.add(pathToStart.getTo());
		this.tourPath.add(pathToStart);
		this.tourDistance += pathToStart.getDistance();
//		for (Path path:paths) {
//			if (path.getFrom() == this.tourCity.get(this.tourCity.size()-1) && path.getTo() == startCity) {
//				this.tourCity.add(path.getTo());
//				this.tourPath.add(path);
//				this.tourDistance += path.getDistance();
//				break;
//			}
//		}
	}

	public List<City> getTourCity(){
		return this.tourCity;
	}

	public List<Path> getTourPath(){
		return this.tourPath;
	}

	public double getTourDistance() {
		return this.tourDistance;
	}

	private Path findNextPath(Map<String,Map<String,Path>> paths) {
		City currentCity = this.tourCity.get(this.tourCity.size()-1);
		Path nextPath = null;
		Map<String,Path> outcomes = paths.get(currentCity.getName());
		Map<Path,Double> allowPaths = new HashMap<>();
		Iterator<Path> ite = outcomes.values().iterator();
		double total = 0.0;
		while (ite.hasNext()) {
			Path path = ite.next();
			if (this.tourCity.contains(path.getTo())) {
				continue;
			}
			double probability = Math.pow(path.getPheromone(), this.pheromoneAffectRate) * Math.pow(1/path.getDistance(), this.distanceAffectRate);
			allowPaths.put(path, probability);
			total += probability;
		}
		double randProb = new Random().nextDouble();
		double p = 0;
		double probability = 0;
		Path lastPath = null;
		Iterator<Path> itePath = allowPaths.keySet().iterator();
		while(itePath.hasNext()) {
			Path path = itePath.next();
			probability = allowPaths.get(path) / total;
			if (randProb < (p+=probability)) {
				nextPath = path;
				break;
			}
		}
//		if (nextPath == null) {
//			System.out.println("probability="+probability + ", p="+p);
//			System.out.println(this.tourCity.size());
//			System.out.println("from:" + lastPath.getFrom().getName()
//					+ ",to:" + lastPath.getTo().getName()
//					+ ",pheromone:" + lastPath.getPheromone()
//					+ ",distance:" + lastPath.getDistance()
//					+ ",total:" + total);
//		}
		return nextPath;
	}

//	private Path findNextPath(List<Path> paths) {
//		City currentCity = this.tourCity.get(this.tourCity.size()-1);
//		Path nextPath = null;
//		List<Path> outcomes = paths.stream()
//					.filter(path -> (path.getFrom() == currentCity && !this.tourCity.contains(path.getTo())))
//					.collect(Collectors.toList());
//		if (outcomes.size() > 0) {
//			double total = 0;
//			for (Path path:outcomes) {
//				total += Math.pow(path.getPheromone(), this.pheromoneAffectRate) * Math.pow(1/path.getDistance(), this.distanceAffectRate);
//			}
//			double randProb = new Random().nextDouble();
//			double p = 0;
//			for (Path path:outcomes) {
//				double probability = (Math.pow(path.getPheromone(), this.pheromoneAffectRate) * Math.pow(1/path.getDistance(), this.distanceAffectRate))/total;
//				if (randProb <= (p+=probability)) {
//					nextPath = path;
//					break;
//				}
//			}
//			if (nextPath == null) {
//				nextPath = outcomes.get(new Random().nextInt(outcomes.size() -1));
//			}
//		}else {
//			nextPath = null;
//		}
//		return nextPath;
//	}
}
