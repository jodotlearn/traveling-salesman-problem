package edu.nchu.cs.ai;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
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

public class TravelingSalesmanProblem extends Application{

	private static String winTitle;
	private static String chartTitle;
	private static String axisXName;
	private static String axisYName;
	private static double width;
	private static double height;
	private static Map<String,List<Double>> data;

	public static void main(String[] args) {
		String citySource = "resources/berlin52.tsp";
		String offcialAnswer = "resources/berlin52.opt.tour";
		String impleAnswer = "resources/berlin52.imp.opt.tour";

		//ant number
		int antCount = 300;
		//ant's pheromone
		double pheromone = 2000;
		//pheromone affect rate (alpha)
		double pheromoneAffectRate = 1;
		//distance affect rate (beta)
		double distanceAffectRate = 5;
		//Pheromone Evaporation Rate
		double evaporationRate = 0.8;
		int iteration = 500;
		int runTimes = 1;
		try {
			Path path = Paths.get(citySource);
			Stream<String> stream = Files.lines(path);
			List<List<Solution>> totalDetail = new ArrayList<>();
			List<City> cities = stream.map(line -> {
					String[] fields = line.split(" ");
					City city = null;
					if (fields.length == 3) {
						city = new City(fields[0], Double.valueOf(fields[1]), Double.valueOf(fields[2]));
					}
					return city;
			}).filter(line -> line != null).collect(Collectors.toList());
			Solution best = null;
			double objValue = Double.MAX_VALUE;
			while (runTimes > 0){
				AntColonyOptimization aco = new
						AntColonyOptimization(cities, antCount, pheromone, pheromoneAffectRate, distanceAffectRate, evaporationRate, iteration);
				OptimumSolution os = aco.run();
				Solution solution = (Solution) os.getSolution();
				if (solution.getObjectiveValue() < objValue) {
					best = solution;
					objValue = best.getObjectiveValue();
				}
				totalDetail.add(os.getExecuteDetail());
				runTimes--;
			}
			//write the best answer to a file.
			Path outputPath = Paths.get(impleAnswer);
			String output = best.toString();
			Files.write(outputPath, output.getBytes());

			//calculate offcial answers
			Path pathOpt = Paths.get(offcialAnswer);
			Stream<String> streamOpt = Files.lines(pathOpt);
			List<City> citiesOpt = streamOpt.map(line -> cities.get(Integer.valueOf(line)-1)).collect(Collectors.toList());
			double offcialShortestDist = 0;
			for (int i=0;i<citiesOpt.size();i++) {
				if (i+1 < citiesOpt.size()) {
					double distance = Math.sqrt(Math.pow(citiesOpt.get(i).getPosX()-citiesOpt.get(i+1).getPosX(), 2)
							+ Math.pow(citiesOpt.get(i).getPosY()-citiesOpt.get(i+1).getPosY(), 2));
					offcialShortestDist += distance;
				}
			}

			System.out.println("official shortest distance:" + offcialShortestDist);
			System.out.println("The shortest distance of this implementation:" + best.getObjectiveValue());

			Map<String, List<Double>> chartData = new HashMap<>();
			List<Double> avgData = new ArrayList<>();
			List<Double> offcialData = new ArrayList<>();
			List<Double> tmp;
			for (int i=0;i<totalDetail.get(0).size();i++) {
				tmp = new ArrayList<>();
				for (int j=0;j<totalDetail.size();j++) {
					tmp.add(totalDetail.get(j).get(i).getObjectiveValue());
				}
				avgData.add(tmp.stream().mapToDouble(val -> val).average().getAsDouble());
				offcialData.add(offcialShortestDist);
			}
			chartData.put("Offcial Optimum", offcialData);
			chartData.put("My Implementation", avgData);
			winTitle = "Convergence Chart";
			width = 800;
			height = 600;
			chartTitle = "Ant Colony Optimization (berlin52.tsp)";
			axisXName = "Iteration";
			axisYName = "Shortest Distance";
			data = chartData;
			launch();
		} catch (IOException e) {
			// TODO Auto-generated catch block
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
		//not show node
		for (XYChart.Series<Number,Number> series:lineChart.getData()) {
			for (XYChart.Data<Number,Number> data:series.getData()) {
				data.getNode().setVisible(false);
			}
		}
		Scene scene  = new Scene(lineChart,this.width,this.height);
		primaryStage.setScene(scene);
		primaryStage.show();
	}
}
