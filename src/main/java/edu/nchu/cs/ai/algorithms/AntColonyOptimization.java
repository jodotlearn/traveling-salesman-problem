package edu.nchu.cs.ai.algorithms;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import edu.nchu.cs.ai.bean.Ant;
import edu.nchu.cs.ai.bean.City;
import edu.nchu.cs.ai.bean.Path;
import edu.nchu.cs.ai.solution.OptimumSolution;
import edu.nchu.cs.ai.solution.PathSolution;
import edu.nchu.cs.ai.solution.Solution;
import edu.nchu.cs.ai.util.RandomUtils;

public class AntColonyOptimization implements Callable<OptimumSolution>{
	private int iteration;
	private List<City> cities;
	private Map<String,Map<String,Path>> paths;
	private int antCount;
	private double pheromone;
	private double pheromoneAffectRate;
	private double distanceAfffectRate;
	private double evaporationRate;
	private boolean improve;

	@Override
	public OptimumSolution call() throws Exception {
		return this.run();
	}

	private AntColonyOptimization() {
		//not allow initialize a constructor without arguments
	}
	public AntColonyOptimization(List<City> cities, int antCount, double pheromone, double pheromoneAffectRate, double distanceAffectRate, double evaporationRate, int iteration, boolean improve) {
		this.cities = cities;
		this.antCount = antCount;
		this.iteration = iteration;
		this.pheromone = pheromone;
		this.pheromoneAffectRate = pheromoneAffectRate;
		this.distanceAfffectRate = distanceAffectRate;
		this.evaporationRate = evaporationRate;
		this.paths = new HashMap<>();
		this.improve = improve;
	}

	public OptimumSolution run() {
		int timesToLocalOptimum = 10;
		OptimumSolution os = new OptimumSolution();
		this.paths = this.initPaths(this.cities);
		List<Ant> ants = this.initAnts(this.antCount, this.pheromone, this.pheromoneAffectRate, this.distanceAfffectRate);
		int sameCnt = 0;
		double keepDist = 0;
		double minDist = Double.MAX_VALUE;
		Ant bestAnt = null;
		List<Path> shortestPath = new ArrayList<>();
		List<Solution> detail = new ArrayList<>();

		Path forward;
		Path reverse;
		while(this.iteration > 0) {
			//random a city as a start city, and begin to tour.
			City startCity = this.randomSelectCity();
			Solution solution = null;
			Map<Path,Double> pathUpdatePhero = new HashMap<Path,Double>();
			for (Ant ant:ants) {
				if (this.improve) {
					ant.resetTourHistory(sameCnt>timesToLocalOptimum?0.5:1);
				}else {
					ant.resetTourHistory(1);
				}
				ant.startTour(this.cities, this.paths, startCity);

				//determination
				if (ant.getTourDistance() < minDist) {
					minDist = ant.getTourDistance();
					shortestPath = ant.getTourPath();
				}
				double avgPhero = ant.getPheromone() / ant.getTourDistance();
				for (Path path:ant.getTourPath()) {
					forward = this.paths.get(path.getFrom().getName()).get(path.getTo().getName());
					reverse = this.paths.get(path.getTo().getName()).get(path.getFrom().getName());
					double originalPhero = 0;
					if (pathUpdatePhero.get(forward) != null) {
						originalPhero = pathUpdatePhero.get(forward);
					}
					pathUpdatePhero.put(forward, originalPhero + avgPhero*path.getDistance());
					pathUpdatePhero.put(reverse, originalPhero + avgPhero*path.getDistance());
				}
				solution = new PathSolution(shortestPath);
				solution.setObjectiveValue(minDist);
				os.setSolution(solution);
			}
			detail.add(solution);

			if (this.improve) {
				if (keepDist == minDist) {
					sameCnt++;
				}else {
					keepDist = minDist;
					sameCnt = 0;
				}
				//disturb
				if (sameCnt > timesToLocalOptimum) {
					for (Path path:shortestPath) {
						double ratio = RandomUtils.randDouble();
						if (ratio <= 0.6) {
							double lambda = RandomUtils.randDouble();
							forward = this.paths.get(path.getFrom().getName()).get(path.getTo().getName());
							reverse = this.paths.get(path.getTo().getName()).get(path.getFrom().getName());
							double updatePhero = forward.getPheromone() * lambda;
							forward.setPheromone(updatePhero);
							reverse.setPheromone(updatePhero);
						}
					}
				}
			}

			//evaporation and update pheromone
			Iterator<Map<String,Path>> ite = this.paths.values().iterator();
			while (ite.hasNext()) {
				Map<String,Path> map = ite.next();
				Iterator<Path> pathIte = map.values().iterator();
				while (pathIte.hasNext()) {
					Path path = pathIte.next();
					forward = this.paths.get(path.getFrom().getName()).get(path.getTo().getName());
					reverse = this.paths.get(path.getTo().getName()).get(path.getFrom().getName());
					double pathPhero = path.getPheromone();
					forward.setPheromone(((1-this.evaporationRate) * pathPhero));
					reverse.setPheromone(((1-this.evaporationRate) * pathPhero));
					if (pathUpdatePhero.get(path) != null) {
						forward.setPheromone(forward.getPheromone() + pathUpdatePhero.get(forward));
						reverse.setPheromone(reverse.getPheromone() + pathUpdatePhero.get(reverse));
					}
				}
			}

			if (this.improve) {
				//update twice
				double minPhero = this.pheromone/(10*this.cities.size());
				double maxPhero = this.pheromone;
				for (Path path:shortestPath) {
					forward = this.paths.get(path.getFrom().getName()).get(path.getTo().getName());
					reverse = this.paths.get(path.getTo().getName()).get(path.getFrom().getName());
					double additionPhero = forward.getPheromone() + this.pheromone * path.getDistance()/minDist;
					if (additionPhero < minPhero) {
						additionPhero = minPhero;
					}else if (additionPhero > maxPhero) {
						additionPhero = maxPhero;
					}
					forward.setPheromone(additionPhero);
					reverse.setPheromone(additionPhero);
				}
			}
			this.iteration--;
		}
		os.setExecuteDetail(detail);
		return os;
	}


	/**
	 * Initializes the path between every city. Stores the distance and pheromone on every path.
	 * @param cities
	 * @return Paths
	 */
	private Map<String,Map<String,Path>> initPaths(List<City> cities) {
		for (int i=0;i<cities.size();i++) {
			for (int j=i+1;j<cities.size();j++) {
				City cityFrom = cities.get(i);
				City cityTo = cities.get(j);
				double distance = Math.round(Math.sqrt(Math.pow(cityFrom.getPosX()-cityTo.getPosX(), 2) + Math.pow(cityFrom.getPosY()-cityTo.getPosY(), 2)));
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

	/**
	 * Initializes the ants.
	 * @param antCount ant colony size
	 * @param pheromone pheromone of an anot
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

	/**
	 * Chooses a city to be the start city randomly.
	 * @return a city
	 */
	private City randomSelectCity() {
		return this.cities.get(RandomUtils.randIntStartFromOne(this.cities.size()-1));
	}
}