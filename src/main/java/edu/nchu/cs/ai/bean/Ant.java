package edu.nchu.cs.ai.bean;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import edu.nchu.cs.ai.util.RandomUtils;

public class Ant {
	private double pheromone;
	private double pheromoneAffectRate;
	private double distanceAffectRate;
	private List<City> tourCity;
	private List<Path> tourPath;
	private double tourDistance;
	private double standardSelectionRate;

	public Ant(double pheromone, double pheromoneAffectRate, double distanceAffectRate) {
		this.pheromone = pheromone;
		this.pheromoneAffectRate = pheromoneAffectRate;
		this.distanceAffectRate = distanceAffectRate;
		this.tourCity = new ArrayList<>();
		this.tourPath = new ArrayList<>();
		this.tourDistance = 0;
		this.standardSelectionRate = 1;
	}

	public double getPheromone() {
		return this.pheromone;
	}

	public void resetTourHistory(double standardSelectionRate) {
		this.tourCity.clear();
		this.tourPath.clear();
		this.tourDistance = 0;
		this.standardSelectionRate = standardSelectionRate;
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
		double total = 0.0;
		double randProb = RandomUtils.randDouble();
		Iterator<Path> ite = outcomes.values().iterator();
		while (ite.hasNext()) {
			Path path = ite.next();
			if (this.tourCity.contains(path.getTo())) {
				continue;
			}
			double probability = Math.pow(path.getPheromone(), this.pheromoneAffectRate) * Math.pow(1/path.getDistance(), this.distanceAffectRate);
			allowPaths.put(path, probability);
			total += probability;
		}
		if (allowPaths.size() == 0) {
			return null;
		}
		//total will be zero if overflow
		if (total == 0) {
			System.out.println(" overflow ");
			total = 1;
		}
		Path[] pathArr = allowPaths.keySet().toArray(new Path[allowPaths.size()]);
		if (randProb <= this.standardSelectionRate) {
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
		}else {
			Iterator<Path> itePath = allowPaths.keySet().iterator();
			if (this.standardSelectionRate >= 0.8) {
				//choose highest probability
				Comparator<Map.Entry<Path, Double>> valueComparator = new Comparator<Map.Entry<Path,Double>>() {
					@Override
					public int compare(Entry<Path, Double> o1, Entry<Path, Double> o2) {
						double value = o2.getValue() - o1.getValue();
						int rtnVal = 0;
						if (value > 0) {
							rtnVal = 1;
						}else if (value < 0) {
							rtnVal = -1;
						}else {
							rtnVal = 0;
						}
						return rtnVal;
					}
				};
				List<Map.Entry<Path,Double>> list = new ArrayList<Map.Entry<Path,Double>>(allowPaths.entrySet());
				Collections.sort(list,valueComparator);
				nextPath = list.get(0).getKey();
			}else {
				// choose the maximum average pheromone
				double maxPhero = 0;
				for (Path path:pathArr) {
					double avg = path.getPheromone()/path.getDistance();
					if (avg > maxPhero) {
						maxPhero = avg;
						nextPath = path;
					}
				}
			}
			this.standardSelectionRate+=0.05;
			if (this.standardSelectionRate>=1) {
				this.standardSelectionRate = 1;
			}
		}
		//a stupid way to solve the overflow issue
		if (nextPath == null && allowPaths.size() > 0) {
			System.out.println(" not found");
			if (pathArr.length == 1) {
				nextPath = pathArr[0];
			}else {
				Path maxPheroPath = null;
				double pheromone = 0;
				double pathPhero = 0;
				for (Path path:pathArr) {
					pathPhero = path.getPheromone()/path.getDistance();
					if (pheromone <= pathPhero) {
						pheromone = pathPhero;
						maxPheroPath = path;
					}
				}
				nextPath = maxPheroPath;
			}
		}
		return nextPath;
	}
}
