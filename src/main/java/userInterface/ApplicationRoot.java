package userInterface;

import java.net.URL;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
	private static final Logger log = LoggerFactory.getLogger(ApplicationRoot.class);

	@Override
	public void init() throws Exception {
		Parameters params = getParameters();
		Map<String, String> argsMap = params.getNamed();
		
		String name = argsMap.get("name");
		String role = argsMap.get("role");
		String id = argsMap.get("id");

		if(name != null && role != null && id != null) {
		    try {
		        this.actor = new Actor(id, name, Role.valueOf(role.toUpperCase()));
		    } catch (IllegalArgumentException e) {
		        log.warn("Invalid arguments format, default actor activated", e);
		        setDefaultActor();
		    }
		} else {
		    log.warn("Missing arguments, default actor activated");
		    setDefaultActor();
		}
		
		String dbUrl = getEnv("DB_URL");
		String dbUser = getEnv("DB_USER");
		String dbPassword = getEnv("DB_PASSWORD");
		
		this.config = new ConnectionConfig(dbUrl, dbUser, dbPassword);
		this.repository = new JdbcAnomalyRepository(this.config, "anomaly.anomalies");
		this.commandService = new AnomalyCommandService(this.repository, this.actor) ;
		this.queryService = new AnomalyQueryService(this.repository);
	}
	
	private void setDefaultActor() {
		this.actor = new Actor("0000", "Dupont", Role.SUPERVISOR);
	}

	@Override
	public void start(Stage stage) throws Exception {
		
		URL fxml = getClass().getResource("/views/generalView.fxml");
		FXMLLoader loader = new FXMLLoader(fxml);
		Parent mainView = loader.load();
		
		GeneralViewController generalViewController = loader.getController();
		generalViewController.setupServiceAndLoad(this.queryService, this.commandService);
		
		Scene scene = new Scene(mainView);
		scene.getStylesheets().add(getClass().getResource("/styles/style.css").toExternalForm());
		stage.setScene(scene);
		stage.show();
	}
	
	private String getEnv(String key) {
	    String value = System.getenv(key);
	    if (value == null || value.isBlank()) {
	        throw new IllegalStateException("Missing environment variable: " + key);
	    }
	    return value;
	}
}
