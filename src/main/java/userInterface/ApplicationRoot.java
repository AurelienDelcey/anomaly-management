package userInterface;

import java.net.URL;

import application.actor.Actor;
import application.actor.Role;
import application.command.AnomalyCommandService;
import application.query.AnomalyQueryService;
import application.repository.AnomalyRepository;
import infrastructure.repository.ConnectionConfig;
import infrastructure.repository.JdbcAnomalyRepository;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class ApplicationRoot extends Application{
	
	private AnomalyCommandService commandService;
	private AnomalyQueryService queryService;
	private AnomalyRepository repository;
	private Actor actor;
	private ConnectionConfig config;
	
	

	@Override
	public void init() throws Exception {
		
		this.actor = new Actor("0000", Role.SUPERVISOR);
		this.config = new ConnectionConfig("jdbc:mysql://localhost:3306/anomaly", "anomaly_user", "anomaly2026");
		this.repository = new JdbcAnomalyRepository(this.config, "anomaly.anomalies_test");
		this.commandService = new AnomalyCommandService(this.repository, this.actor) ;
		this.queryService = new AnomalyQueryService(this.repository);
		
	}



	@Override
	public void start(Stage stage) throws Exception {
		
		URL fxml = getClass().getResource("/views/generalView.fxml");
		FXMLLoader loader = new FXMLLoader(fxml);
		Parent mainView = loader.load();
		
		GeneralViewController generalViewController = loader.getController();
		generalViewController.setupServiceAndLoad(this.queryService, this.commandService);
		
		Scene scene = new Scene(mainView);
		stage.setScene(scene);
		stage.show();
	}
}
