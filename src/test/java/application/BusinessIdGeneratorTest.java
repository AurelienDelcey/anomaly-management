package application;

import static org.junit.jupiter.api.Assertions.*;

import java.sql.Connection;
import java.sql.DriverManager;
import java.time.Instant;
import java.time.Year;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import application.repository.AnomalyRepository;
import domain.anomaly.Anomaly;
import domain.traceability.EventTrace;
import domain.valueobject.BusinessId;
import domain.valueobject.Sector;
import infrastructure.repository.ConnectionConfig;
import infrastructure.repository.JdbcAnomalyRepository;

class BusinessIdGeneratorTest {
	
	private static final String TABLE = "anomaly.anomalies_test";
	private final static String VALID_ACTOR_ID = "0000";
	private final static Instant FIXED_INSTANT = Instant.parse("2026-02-16T00:00:00Z");
	private final static String DESCRIPTION = "anomalyTest";
	private ConnectionConfig config ;
	private BusinessIdGenerator businessIdGenerator;
	private AnomalyRepository repository;

	@BeforeEach
	void setUp() throws Exception {
		this.config = new ConnectionConfig("jdbc:mysql://localhost:3306/anomaly", "anomaly_user", "anomaly2026");
		this.repository = new JdbcAnomalyRepository(config,TABLE);
		this.businessIdGenerator = new BusinessIdGenerator(repository);
	}

	@AfterEach
	void tearDown() throws Exception {
		try (Connection connection =
	             DriverManager.getConnection(config.url(), config.user(), config.password())) {

	        connection.createStatement()
	                  .execute("TRUNCATE TABLE " + TABLE);
	    }
	}

	@Test
	void test() {
		BusinessId BusinessId1 = businessIdGenerator.getBusinessId();
		Anomaly anomaly1 = createAnomalyWithBusinessId(BusinessId1);
		repository.save(anomaly1);
		
		BusinessId BusinessId2 = businessIdGenerator.getBusinessId();
		Anomaly anomaly2 = createAnomalyWithBusinessId(BusinessId2);
		repository.save(anomaly2);
		
		BusinessId BusinessId3 = businessIdGenerator.getBusinessId();
		Anomaly anomaly3 = createAnomalyWithBusinessId(BusinessId3);
		repository.save(anomaly3);
		
		assertEquals(1, anomaly1.getBusinessId().sequence());
		assertEquals(Year.now().getValue(), anomaly1.getBusinessId().year());
		
		assertEquals(2, anomaly2.getBusinessId().sequence());
		assertEquals(Year.now().getValue(), anomaly2.getBusinessId().year());
		
		assertEquals(3, anomaly3.getBusinessId().sequence());
		assertEquals(Year.now().getValue(), anomaly3.getBusinessId().year());
	}
	
	private Anomaly createAnomalyWithBusinessId(BusinessId businessId) {
		EventTrace creationTrace = new EventTrace(VALID_ACTOR_ID, FIXED_INSTANT);
		return assertDoesNotThrow(()->new Anomaly(businessId, DESCRIPTION, Sector.FORGING, creationTrace));
	}

}
