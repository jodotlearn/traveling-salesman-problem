package edu.nchu.cs.ai.algorithms;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;

import edu.nchu.cs.ai.bean.Ant;
import edu.nchu.cs.ai.bean.City;
import edu.nchu.cs.ai.bean.Path;
import edu.nchu.cs.ai.solution.OptimumSolution;
import edu.nchu.cs.ai.solution.PathSolution;
import edu.nchu.cs.ai.solution.Solution;

public class AntColonyOptimization {
	private int iteration;
	private List<City> cities;
//	private List<Ant> ants = new ArrayList<>();
//	private List<Path> paths = new ArrayList<>();
	private Map<String,Map<String,Path>> paths;
	private int antCount;
	private double pheromone;
	private double pheromoneAffectRate;
	private double distanceAfffectRate;
	private double evaporationRate;

	private AntColonyOptimization() {
		//not allow initialize a constructor without arguments
	}
	public AntColonyOptimization(List<City> cities, int antCount, double pheromone, double pheromoneAffectRate, double distanceAffectRate, double evaporationRate, int iteration) {
		this.cities = cities;
		this.antCount = antCount;
		this.iteration = iteration;
		this.pheromone = pheromone;
		this.pheromoneAffectRate = pheromoneAffectRate;
		this.distanceAfffectRate = distanceAffectRate;
		this.evaporationRate = evaporationRate;
//		this.paths = new ArrayList<>();
		this.paths = new HashMap<>();
	}

	public OptimumSolution run() {
		OptimumSolution os = new OptimumSolution();
		this.paths = this.initPaths(this.cities);
		List<Ant> ants = this.initAnts(this.antCount, this.pheromone, this.pheromoneAffectRate, this.distanceAfffectRate);

		double minDist = Double.MAX_VALUE;
		Ant bestAnt = null;
		List<Path> shortestPath = new ArrayList<>();
		List<Solution> detail = new ArrayList<>();
		while(this.iteration > 0) {
			//random a city as a start city, and begin to tour.
			City startCity = this.initStartCity();
			for (Ant ant:ants) {
				ant.resetTourHistory();
				ant.startTour(this.cities, this.paths, startCity);
			}
			//update pheromone
			// 1. evaporation
//			for (Path path:this.paths) {
//				double pathPhero = path.getPheromone();
//				path.setPheromone((1-this.evaporationRate) * pathPhero);
//			}
			Iterator<Map<String,Path>> ite = this.paths.values().iterator();
			while (ite.hasNext()) {
				Map<String,Path> map = ite.next();
				Iterator<Path> pathIte = map.values().iterator();
				while (pathIte.hasNext()) {
					Path path = pathIte.next();
					double pathPhero = path.getPheromone();
					path.setPheromone(((1-this.evaporationRate) * pathPhero));
				}
			}
			// 2. update
			for (Ant ant:ants) {
				double updatePhero = ant.getPheromone() / ant.getTourDistance();
				for (Path path:ant.getTourPath()) {
					path.setPheromone(path.getPheromone() + updatePhero);
				}
			}
			//determination
			Solution solution = null;
			for (Ant ant:ants) {
				if (ant.getTourDistance() < minDist) {
					minDist = ant.getTourDistance();
					shortestPath = ant.getTourPath();
				}
				solution = new PathSolution(shortestPath);
				solution.setObjectiveValue(minDist);
				os.setSolution(solution);
			}
			detail.add(solution);
			this.iteration--;
		}
		os.setExecuteDetail(detail);
		return os;
	}


	private Map<String,Map<String,Path>> initPaths(List<City> cities) {
		for (int i=0;i<cities.size();i++) {
			for (int j=i+1;j<cities.size();j++) {
				City cityFrom = cities.get(i);
				City cityTo = cities.get(j);
				double distance = Math.sqrt(Math.pow(cityFrom.getPosX()-cityTo.getPosX(), 2) + Math.pow(cityFrom.getPosY()-cityTo.getPosY(), 2));
				double initialPheromone = 1.0d/distance;
				Path path = new Path(cityFrom, cityTo, distance, initialPheromone);
				Path pathReverse = new Path(cityTo, cityFrom, distance, initialPheromone);
				Map<String,Path> map = this.paths.get(cityFrom.getName());
				if (map == null) {
					map = new HashMap<>();
				}
				map.put(cityTo.getName(), path);
				Map<String,Path> reverseMap = this.paths.get(cityTo.getName());
				if (reverseMap == null) {
					reverseMap = new HashMap<>();
				}
				reverseMap.put(cityFrom.getName(), pathReverse);

				this.paths.put(cityFrom.getName(), map);
				this.paths.put(cityTo.getName(), reverseMap);
			}
		}
		return this.paths;
	}
//	private List<Path> initPaths(List<City> cities) {
//		List<Path> paths = new ArrayList<>();
//		for (int i=0;i<cities.size();i++) {
//			for (int j=i+1;j<cities.size();j++) {
//				City cityFrom = cities.get(i);
//				City cityTo = cities.get(j);
//				double distance = Math.sqrt(Math.pow(cityFrom.getPosX()-cityTo.getPosX(), 2) + Math.pow(cityFrom.getPosY()-cityTo.getPosY(), 2));
//				double initialPheromone = 1.0d/distance;
//				Path path = new Path(cityFrom, cityTo, distance, initialPheromone);
//				Path pathReverse = new Path(cityTo, cityFrom, distance, initialPheromone);
//				path.setId(paths.size());
//				paths.add(path);
//				pathReverse.setId(paths.size());
//				paths.add(pathReverse);
//			}
//		}
//		return paths;
//	}

	/**
	 * Initialize the ants.
	 * @param antCount
	 * @param pheromone
	 * @return a list contains ants
	 */
	private List<Ant> initAnts(int antCount, double pheromone, double pheromoneAffectRate, double distanceAffectRate) {
		List<Ant> ants = new ArrayList<>();
		for (int i=0;i<antCount;i++) {
			Ant ant = new Ant(pheromone, pheromoneAffectRate, distanceAffectRate);
			ants.add(ant);
		}
		return ants;
	}

	private City initStartCity() {
		Random rnd = new Random();
		return this.cities.get(rnd.nextInt(this.cities.size()-1));
	}
}