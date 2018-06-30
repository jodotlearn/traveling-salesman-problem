package edu.nchu.cs.ai;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import edu.nchu.cs.ai.algorithms.AntColonyOptimization;
import edu.nchu.cs.ai.bean.City;
import edu.nchu.cs.ai.solution.OptimumSolution;
import edu.nchu.cs.ai.solution.Solution;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.stage.Stage;

/**
* An application for testing Ant Colony Optimization algorithm.
* 1. running and collecting the objective every iteration.
* 2. averaging the value per iteration.
* 3. draw the convergence chart.
* @author Jo
*
*/
public class TravelingSalesmanProblem extends Application{

	private static String winTitle;
	private static String chartTitle;
	private static String axisXName;
	private static String axisYName;
	private static double width;
	private static double height;
	private static Map<String,List<Double>> data;

	public static void main(String[] args) throws InterruptedException, ExecutionException {
		//load and prepare the output files
		String citySource = "resources/eil51.tsp";
		String offcialAnswer = "resources/eil51.opt.tour";
		String impleAnswer = "resources/eil51.imp.opt.tour";
		String impleImproveAnswer = "resources/eil51.impimprove.opt.tour";

		//ant number
		int antCount = 50;//Integer.valueOf(args[0]);
		//ant's pheromone
		double pheromone = 10;//Double.valueOf(args[1]);
		//pheromone affect rate (alpha)
		double pheromoneAffectRate = 1;//Double.valueOf(args[2]);;
		//distance affect rate (beta)
		double distanceAffectRate = 2;//Double.valueOf(args[3]);;
		//Pheromone Evaporation Rate
		double evaporationRate = 0.7;//Double.valueOf(args[4]);;
		//specific a limitation value to tell this program might fall into local optimum
		int timesToLocalOptimum = 10;//Integer.valueOf(args[5]);
		//iterations
		int iteration = 2000;//Integer.valueOf(args[6]);
		//run times
		int runTimes = 30;//Integer.valueOf(args[7]);
		try {
			Path path = Paths.get(citySource);
			Stream<String> stream = Files.lines(path);
			List<List<Solution>> totalDetail = new ArrayList<>();
			List<List<Solution>> totalDetailImprove = new ArrayList<>();
			List<City> cities = stream.map(line -> {
					String[] fields = line.split(" ");
					City city = null;
					if (fields.length == 3) {
						city = new City(fields[0], Double.valueOf(fields[1]), Double.valueOf(fields[2]));
					}
					return city;
			}).filter(line -> line != null).collect(Collectors.toList());
			Solution best = null;
			Solution bestImprove = null;
			double objValue = Double.MAX_VALUE;
			double objValueImprove = Double.MAX_VALUE;
			long start = System.currentTimeMillis();
			while (runTimes > 0){
				ExecutorService executor = Executors.newFixedThreadPool(2);
				//original ACO
				AntColonyOptimization aco = new
						AntColonyOptimization(cities, antCount, pheromone, pheromoneAffectRate, distanceAffectRate, evaporationRate, timesToLocalOptimum, iteration, false);
				//improve ACO
				AntColonyOptimization acoImprove = new
						AntColonyOptimization(cities, antCount, pheromone, pheromoneAffectRate, distanceAffectRate, evaporationRate, timesToLocalOptimum, iteration, true);
				Future<OptimumSolution> acoFuture = executor.submit(aco);
				Future<OptimumSolution> acoImproveFuture = executor.submit(acoImprove);
				OptimumSolution os = acoFuture.get();
				OptimumSolution osImprove = acoImproveFuture.get();
				executor.shutdown();

				//find the best solution from 30 run times (2000 iterations)
				Solution solution = (Solution) os.getSolution();
				if (solution.getObjectiveValue() < objValue) {
					best = solution;
					objValue = best.getObjectiveValue();
				}
				Solution solutionImprove = (Solution) osImprove.getSolution();
				if (solutionImprove.getObjectiveValue() < objValueImprove) {
					bestImprove = solutionImprove;
					objValueImprove = bestImprove.getObjectiveValue();
				}
				totalDetail.add(os.getExecuteDetail());
				totalDetailImprove.add(osImprove.getExecuteDetail());
				runTimes--;
			}
			long end = System.currentTimeMillis();
			//output the best answer to a file.
			Path outputPath = Paths.get(impleAnswer);
			String output = best.toString();
			Files.write(outputPath, output.getBytes());
			//output the best improve answer to a file
			Path outputPathImprove = Paths.get(impleImproveAnswer);
			String outputImprove = bestImprove.toString();
			Files.write(outputPathImprove, outputImprove.getBytes());

			//calculate offcial answers
			Path pathOpt = Paths.get(offcialAnswer);
			Stream<String> streamOpt = Files.lines(pathOpt);
			List<City> citiesOpt = streamOpt.map(line -> cities.get(Integer.valueOf(line)-1)).collect(Collectors.toList());
			double offcialShortestDist = 0;
			for (int i=0;i<citiesOpt.size();i++) {
				if (i+1 < citiesOpt.size()) {
					double distance = Math.round(Math.sqrt(Math.pow(citiesOpt.get(i).getPosX()-citiesOpt.get(i+1).getPosX(), 2)
							+ Math.pow(citiesOpt.get(i).getPosY()-citiesOpt.get(i+1).getPosY(), 2)));
					offcialShortestDist += distance;
				}
			}

			System.out.println("official shortest distance:" + offcialShortestDist);
			System.out.println("The shortest distance of this implementation:" + best.getObjectiveValue());
			System.out.println("The shortest distance of this implementation (improve):" + bestImprove.getObjectiveValue());

			Map<String, List<Double>> chartData = new TreeMap<>();
			List<Double> avgData = new ArrayList<>();
			List<Double> avgDataImprove = new ArrayList<>();
			List<Double> offcialData = new ArrayList<>();
			List<Double> tmp, tmpImprove;
			for (int i=0;i<totalDetail.get(0).size();i++) {
				tmp = new ArrayList<>();
				tmpImprove = new ArrayList<>();
				for (int j=0;j<totalDetail.size();j++) {
					tmp.add(totalDetail.get(j).get(i).getObjectiveValue());
					tmpImprove.add(totalDetailImprove.get(j).get(i).getObjectiveValue());
				}
				avgData.add(tmp.stream().mapToDouble(val -> val).average().getAsDouble());
				avgDataImprove.add(tmpImprove.stream().mapToDouble(val -> val).average().getAsDouble());
				offcialData.add(offcialShortestDist);
			}
			System.out.println("The average shortest distance of this implementation:" + avgData.get(avgData.size()-1));
			System.out.println("The average shortest distance of this implementation(improve):" + avgDataImprove.get(avgDataImprove.size()-1));
			System.out.println("spent time:" + ((double)end-start)/1000 + " seconds");
			chartData.put("ACO", avgData);
			chartData.put("ACO (Improve)", avgDataImprove);
			chartData.put("Offcial Optimum", offcialData);
			winTitle = "Convergence Chart";
			width = 800;
			height = 600;
			chartTitle = "Ant Colony Optimization";
			axisXName = "Iteration";
			axisYName = "Shortest Distance";
			data = chartData;
			launch();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void start(Stage primaryStage) throws Exception {
		primaryStage.setTitle(this.winTitle);
		//defining the axis
		final NumberAxis xAxis = new NumberAxis();
		final NumberAxis yAxis = new NumberAxis();
		xAxis.setLabel(this.axisXName);
		yAxis.setLabel(this.axisYName);

		//creating the chart
		final LineChart<Number,Number> lineChart = new LineChart<Number,Number>(xAxis,yAxis);

		lineChart.setTitle(this.chartTitle);
		Iterator<String> ite = this.data.keySet().iterator();
		while(ite.hasNext()) {
			String seriesName = ite.next();
			XYChart.Series<Number,Number> series = new XYChart.Series<>();
			series.setName(seriesName);
			List<Double> seriesData = this.data.get(seriesName);
			for (int i=0;i<seriesData.size();i++) {
				series.getData().add(new XYChart.Data<Number,Number>(i+1, seriesData.get(i)));
			}
			lineChart.getData().add(series);
		}
		//hidden node
		for (XYChart.Series<Number,Number> series:lineChart.getData()) {
			for (XYChart.Data<Number,Number> d:series.getData()) {
				d.getNode().setVisible(false);
			}
		}
		Scene scene  = new Scene(lineChart,this.width,this.height);
		primaryStage.setScene(scene);
		primaryStage.show();
	}
}
