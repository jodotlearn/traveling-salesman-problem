package edu.nchu.cs.ai;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import edu.nchu.cs.ai.bean.City;
import javafx.application.Application;
import javafx.beans.binding.Bindings;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.transform.Scale;
import javafx.stage.Stage;

/**
 * Draw tour map.
 *  - official optimum
 *  - the optimum of original implementation
 *  - the optimum of the improvement implementation
 *
 */
public class DrawTourMap extends Application {
	private static List<City> cities;
	private static List<String> opt;
	private static List<String> impOpt;
	private static List<String> impImproveOpt;
	private static double maxHeight;
	private static double maxWidth;

	public static void main(String[] args) {
		Path path = Paths.get("resources/eil51.tsp");
		Path pathOpt = Paths.get("resources/eil51.opt.tour");
		Path pathImpOpt = Paths.get("resources/eil51.imp.opt.tour");
		Path pathImproveOpt = Paths.get("resources/eil51.impimprove.opt.tour");
		try {
			Stream<String> stream = Files.lines(path);
			cities = stream.map(line -> {
					String[] fields = line.split(" ");
					City city = null;
					if (fields.length == 3) {
						city = new City(fields[0], Double.valueOf(fields[1]), Double.valueOf(fields[2]));
						if (maxWidth < city.getPosX()) {
							maxWidth = city.getPosX();
						}
						if (maxHeight < city.getPosY()) {
							maxHeight = city.getPosY();
						}
					}
					return city;
				}).filter(line -> line != null).collect(Collectors.toList());
			Stream<String> streamOpt = Files.lines(pathOpt);
			opt = streamOpt.collect(Collectors.toList());
			Stream<String> streamImpOpt = Files.lines(pathImpOpt);
			impOpt = streamImpOpt.collect(Collectors.toList());
			Stream<String> streamImpImproveOpt = Files.lines(pathImproveOpt);
			impImproveOpt = streamImpImproveOpt.collect(Collectors.toList());

//			List<City> citiesOpt = Files.lines(pathOpt).map(line -> cities.get(Integer.valueOf(line)-1)).collect(Collectors.toList());
//			List<City> improveOpt = Files.lines(pathImproveOpt).map(line -> cities.get(Integer.valueOf(line)-1)).collect(Collectors.toList());
//			double offcialShortestDist = 0;
//			double improveShortestDist = 0;
//			for (int i=0;i<citiesOpt.size();i++) {
//				if (i+1 < citiesOpt.size()) {
//					double distance = Math.sqrt(Math.pow(citiesOpt.get(i).getPosX()-citiesOpt.get(i+1).getPosX(), 2)
//							+ Math.pow(citiesOpt.get(i).getPosY()-citiesOpt.get(i+1).getPosY(), 2));
//					offcialShortestDist += distance;
//				}
//			}
//			for (int i=0;i<improveOpt.size();i++) {
//				if (i+1 < improveOpt.size()) {
//					double distance = Math.sqrt(Math.pow(improveOpt.get(i).getPosX()-improveOpt.get(i+1).getPosX(), 2)
//							+ Math.pow(improveOpt.get(i).getPosY()-improveOpt.get(i+1).getPosY(), 2));
//					improveShortestDist += distance;
//				}
//			}
//
//			System.out.println("official shortest distance:" + offcialShortestDist);
//			System.out.println("improve shortest distance:" + improveShortestDist);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		launch(args);
	}

	@Override
	public void start(Stage primaryStage) {
		HBox hbox = new HBox();
		VBox vbox = new VBox();
		VBox vbox2 = new VBox();
		VBox vbox3 = new VBox();
		Label label = new Label("Offcial Tour Map");
		Label label2 = new Label("ACO");
		Label label3 = new Label("ACO(improve)");
		Pane offcialPane = new Pane();
		Pane impPane = new Pane();
		Pane impImprovePane = new Pane();
		offcialPane.setPadding(new Insets(5,5,5,5));
		impPane.setPadding(new Insets(5,5,5,5));
		impImprovePane.setPadding(new Insets(5,5,5,5));
		Map<String,Circle> mapping1 = new HashMap<>();
		Map<String,Circle> mapping2 = new HashMap<>();
		Map<String,Circle> mapping3 = new HashMap<>();
		for (City city:cities) {
			double posX = 400/maxWidth * city.getPosX();
			double posY = 300/maxHeight * city.getPosY();
			Circle c1 = new Circle(posX, posY, 3, Color.BLACK);
			Circle c2 = new Circle(posX, posY, 3, Color.BLACK);
			Circle c3 = new Circle(posX, posY, 3, Color.BLACK);
			offcialPane.getChildren().add(c1);
			impPane.getChildren().add(c2);
			impImprovePane.getChildren().add(c3);
			mapping1.put(city.getName(), c1);
			mapping2.put(city.getName(), c2);
			mapping3.put(city.getName(), c3);
		}
		for (int i=0;i<opt.size();i++) {
			if (i+1 < opt.size()) {
				Circle c1 = mapping1.get(opt.get(i));
				Circle c2 = mapping1.get(opt.get(i+1));
				this.connect(offcialPane, c1, c2, Color.LIGHTSKYBLUE);
			}
		}
		for (int i=0;i<impOpt.size();i++) {
			if (i+1 < impOpt.size()) {
				Circle c1 = mapping2.get(impOpt.get(i));
				Circle c2 = mapping2.get(impOpt.get(i+1));
				this.connect(impPane, c1, c2, Color.LIGHTSKYBLUE);
			}
		}
		for (int i=0;i<impImproveOpt.size();i++) {
			if (i+1 < impImproveOpt.size()) {
				Circle c1 = mapping3.get(impImproveOpt.get(i));
				Circle c2 = mapping3.get(impImproveOpt.get(i+1));
				this.connect(impImprovePane, c1, c2, Color.LIGHTSKYBLUE);
			}
		}
		Scale scale1 = new Scale();
		scale1.setX(1);
		scale1.setY(-1);
		scale1.pivotYProperty().bind(Bindings.createDoubleBinding(() ->
		offcialPane.getBoundsInLocal().getMinY() + offcialPane.getBoundsInLocal().getHeight() /2,
		offcialPane.boundsInLocalProperty()));
		offcialPane.getTransforms().add(scale1);

		Scale scale2 = new Scale();
		scale2.setX(1);
		scale2.setY(-1);

		scale2.pivotYProperty().bind(Bindings.createDoubleBinding(() ->
		impPane.getBoundsInLocal().getMinY() + impPane.getBoundsInLocal().getHeight() /2,
		impPane.boundsInLocalProperty()));
		impPane.getTransforms().add(scale2);

		Scale scale3 = new Scale();
		scale3.setX(1);
		scale3.setY(-1);

		scale3.pivotYProperty().bind(Bindings.createDoubleBinding(() ->
		impImprovePane.getBoundsInLocal().getMinY() + impImprovePane.getBoundsInLocal().getHeight() /2,
		impImprovePane.boundsInLocalProperty()));
		impImprovePane.getTransforms().add(scale3);

//		flow.getChildren().addAll(pane, pane2);
		vbox.getChildren().add(label);
		vbox.getChildren().add(offcialPane);
		vbox2.getChildren().add(label2);
		vbox2.getChildren().add(impPane);
		vbox3.getChildren().add(label3);
		vbox3.getChildren().add(impImprovePane);
		hbox.getChildren().add(vbox2);
		hbox.getChildren().add(vbox3);
		VBox vb = new VBox();
		HBox hb = new HBox();
		hb.getChildren().add(vbox);
		vb.getChildren().add(hb);
		vb.getChildren().add(hbox);
		Scene scene = new Scene(vb, 900, 700);
		primaryStage.setTitle("Tour Map");
		primaryStage.setScene(scene);
		primaryStage.show();

	}

	private void connect(Pane pane, Circle n1, Circle n2, Color color) {
		Line line = new Line();
		line.setStroke(color);
		line.setStartX(n1.getCenterX());
		line.setStartY(n1.getCenterY());
		line.setEndX(n2.getCenterX());
		line.setEndY(n2.getCenterY());
		pane.getChildren().add(line);
	}

}
