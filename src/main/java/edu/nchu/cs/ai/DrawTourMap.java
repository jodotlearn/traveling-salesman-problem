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
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.stage.Stage;

/**
 * Hello world!
 *
 */
public class DrawTourMap extends Application {
	private static List<City> cities;
	private static List<String> opt;
	private static List<String> impOpt;
//	private static Map<City,Circle> mapping;
//	private static Map<String,Circle> mapping1;
//	private static Map<String,Circle> mapping2;
	private static double maxHeight;
	private static double maxWidth;

	public static void main(String[] args) {
		Path path = Paths.get("resources/berlin52.tsp");
		Path pathOpt = Paths.get("resources/berlin52.opt.tour");
		Path pathImpOpt = Paths.get("resources/berlin52.imp.opt.tour");
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
		Label label = new Label("Offcial Tour Map");
		Label label2 = new Label("My Implementation");
		Pane pane = new Pane();
		Pane pane2 = new Pane();
//		flow.setPadding(new Insets(5,5,5,5));
		pane.setPadding(new Insets(5,5,5,5));
		pane2.setPadding(new Insets(5,5,5,5));
		Map<String,Circle> mapping1 = new HashMap<>();
		Map<String,Circle> mapping2 = new HashMap<>();
		for (City city:cities) {
			double posX = 400/maxWidth * city.getPosX();
			double posY = 300/maxHeight * city.getPosY();
			Circle c1 = new Circle(posX, posY, 3, Color.BLACK);
			Circle c2 = new Circle(posX, posY, 3, Color.BLACK);
			pane.getChildren().add(c1);
			pane2.getChildren().add(c2);
			mapping1.put(city.getName(), c1);
			mapping2.put(city.getName(), c2);
		}
		for (int i=0;i<opt.size();i++) {
			if (i+1 < opt.size()) {
				Circle c1 = mapping1.get(opt.get(i));
				Circle c2 = mapping1.get(opt.get(i+1));
				this.connect(pane, c1, c2, Color.LIGHTSKYBLUE);
			}
		}
		for (int i=0;i<impOpt.size();i++) {
			if (i+1 < impOpt.size()) {
				Circle c1 = mapping2.get(impOpt.get(i));
				Circle c2 = mapping2.get(impOpt.get(i+1));
				this.connect(pane2, c1, c2, Color.LIGHTSKYBLUE);
			}
		}
//		flow.getChildren().addAll(pane, pane2);
		vbox.getChildren().add(label);
		vbox.getChildren().add(pane);
		vbox2.getChildren().add(label2);
		vbox2.getChildren().add(pane2);
		hbox.getChildren().add(vbox);
		hbox.getChildren().add(vbox2);
		Scene scene = new Scene(hbox, 900, 400);
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
