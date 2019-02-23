package org.tms;

import org.tms.db.TaskExecutor;
import org.tms.entities.RecordEntity;
import org.tms.db.localdb.DBOperations;
import org.tms.db.localdb.RetrieveDaysAverage;
import org.tms.db.localdb.RetrieveReport2;
import org.tms.db.localdb.TrafficSpeedDAO;
import org.tms.utilities.Day;
import org.tms.utilities.GlobalObjects;
import org.tms.utilities.InitializeReport2;
import com.jfoenix.controls.JFXButton;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.ResourceBundle;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableView;
import javafx.scene.layout.AnchorPane;
import org.asynchttpclient.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ReportsController implements Initializable {
	final Logger log = LoggerFactory.getLogger(ReportsController.class);
	RetrieveReport2 retrieveReport = new RetrieveReport2();
	RetrieveDaysAverage db = new RetrieveDaysAverage();
	TrafficSpeedDAO trafficSpeedDao = new TrafficSpeedDAO();
	
	@FXML
	private Tab tab_averagevolume, tab_report2;
	@FXML
	private LineChart<?, ?> chart_averagevolume;
	@FXML
	private NumberAxis axis_y;
	@FXML
	private CategoryAxis axis_x;
	@FXML
	private TableView<RecordEntity> tableview_report;
	@FXML
	private JFXButton button_refresh, button_publish;
	@FXML
	private ProgressIndicator progress_reports;
	@FXML
	private TabPane tabpane_main;
	@FXML
	private AnchorPane anchorpane_main;
	@FXML
	private Tab avgSpeedTab;
	
	@Override
	public void initialize(URL url, ResourceBundle rb) {
		CompletableFuture<Void> cf = CompletableFuture.runAsync(() -> db.retrieve(Day.Monday))
				.thenRun(() -> db.retrieve(Day.Tuesday)).thenRun(() -> db.retrieve(Day.Wednesday))
				.thenRun(() -> db.retrieve(Day.Thursday)).thenRun(() -> db.retrieve(Day.Friday))
				.thenRun(() -> db.retrieve(Day.Saturday)).thenRun(() -> db.retrieve(Day.Sunday));
		try {
			cf.get();
		} catch (InterruptedException e) {
			log.error(e.toString());
		} catch (ExecutionException e) {
			log.error(e.toString());
		}
		HashMap<Day, String> result = db.getResult();
		XYChart.Series series = new XYChart.Series();
		series.getData().add(new XYChart.Data(Day.Monday.toString(), Double.valueOf(result.get(Day.Monday))));
		series.getData().add(new XYChart.Data(Day.Tuesday.toString(), Double.valueOf(result.get(Day.Tuesday))));
		series.getData().add(new XYChart.Data(Day.Wednesday.toString(), Double.valueOf(result.get(Day.Wednesday))));
		series.getData().add(new XYChart.Data(Day.Thursday.toString(), Double.valueOf(result.get(Day.Thursday))));
		series.getData().add(new XYChart.Data(Day.Friday.toString(), Double.valueOf(result.get(Day.Friday))));
		series.getData().add(new XYChart.Data(Day.Saturday.toString(), Double.valueOf(result.get(Day.Saturday))));
		series.getData().add(new XYChart.Data(Day.Sunday.toString(), Double.valueOf(result.get(Day.Sunday))));
		chart_averagevolume.getData().add(series);
		InitializeReport2 report2 = new InitializeReport2();
		report2.setTableView(tableview_report);
		CompletableFuture<Void> completableFuture2 = CompletableFuture.runAsync(() -> retrieveReport.retrieveValue(1))
				.thenRun(() -> retrieveReport.retrieveValue(2)).thenRun(() -> retrieveReport.retrieveValue(3))
				.thenRun(() -> retrieveReport.retrieveValue(4)).thenRun(() -> retrieveReport.retrieveValue(5))
				.thenRun(() -> retrieveReport.retrieveValue(6)).thenRun(() -> retrieveReport.retrieveValue(7))
				.thenRun(() -> retrieveReport.retrieveValue(8)).thenRun(() -> retrieveReport.retrieveValue(9))
				.thenRun(() -> retrieveReport.retrieveValue(10)).thenRun(() -> retrieveReport.retrieveValue(11))
				.thenRun(() -> retrieveReport.retrieveValue(12)).thenRun(() -> retrieveReport.retrieveValue(13))
				.thenRun(() -> retrieveReport.retrieveValue(14)).thenRun(() -> retrieveReport.retrieveValue(15))
				.thenRun(() -> retrieveReport.retrieveValue(16)).thenRun(() -> retrieveReport.retrieveValue(17))
				.thenRun(() -> retrieveReport.retrieveValue(18)).thenRun(() -> retrieveReport.retrieveValue(19))
				.thenRun(() -> retrieveReport.retrieveValue(20)).thenRun(() -> retrieveReport.retrieveValue(21))
				.thenRun(() -> retrieveReport.retrieveValue(22)).thenRun(() -> retrieveReport.retrieveValue(23));
		try {
			completableFuture2.get();
		} catch (InterruptedException e) {
			log.error(e.toString());
		} catch (ExecutionException e) {
			log.error(e.toString());
		}
		report2.populateTable(retrieveReport.getResult());
		
		loadAvgSpeedReport();
	}

	@FXML
	private void button_refreshOnClick(ActionEvent event) {
	}

	@FXML
	private void button_publishOnClick(ActionEvent event) {
		TaskExecutor.getInstance().publishReports();
		GlobalObjects.getInstance().bindBtnNProgress(button_publish, progress_reports,
				TaskExecutor.getInstance().getMyTask().runningProperty());
		TaskExecutor.getInstance().getMyTask().setOnSucceeded(new EventHandler<WorkerStateEvent>() {
			@Override
			public void handle(WorkerStateEvent event) {
				GlobalObjects.getInstance().showMessage("Publish done", anchorpane_main);
				Response response = (Response) TaskExecutor.getInstance().getMyTask().getValue();
			}
		});
		TaskExecutor.getInstance().getMyTask().setOnFailed(new EventHandler<WorkerStateEvent>() {
			@Override
			public void handle(WorkerStateEvent event) {
				GlobalObjects.getInstance().showMessage("Error", anchorpane_main);
			}
		});
	}
	
	public void loadAvgSpeedReport() {
		log.info("entry");
		
		HashMap<Double, String> avgSpeedHashMap = trafficSpeedDao.getAvgSpeed();
		log.debug("avgSpeedHashMap: " + avgSpeedHashMap);
		log.info("avgSpeedHashMap: " + avgSpeedHashMap);
		
		//Defining the y axis   
		CategoryAxis xAxis = new CategoryAxis (); 
		xAxis.setLabel("Daily"); 		

		NumberAxis yAxis = new NumberAxis(0, 200, 20); 
		yAxis.setLabel("Average Speed"); 

		//Creating the line chart 
		LineChart linechart = new LineChart(xAxis, yAxis);  

		//Prepare XYChart.Series objects by setting data 
		XYChart.Series series = new XYChart.Series(); 
		series.setName("Average speed of vehicles per day"); 
		avgSpeedHashMap.forEach((k, v) -> {
			series.getData().add(new XYChart.Data(v, k)); 
		});

		//Setting the data to Line chart    
		linechart.getData().add(series);        

		//Creating a Group object  
		Group root = new Group(linechart); 

		avgSpeedTab.setContent(root);
		log.info("exit");
	}
}
