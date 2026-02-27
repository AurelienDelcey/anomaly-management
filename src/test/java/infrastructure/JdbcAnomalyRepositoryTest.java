package infrastructure;

import static org.junit.jupiter.api.Assertions.*;

import java.sql.Connection;
import java.sql.DriverManager;
import java.time.Instant;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import domain.anomaly.Anomaly;
import domain.anomaly.AnomalyState;
import domain.traceability.EventTrace;
import domain.valueobject.Description;
import domain.valueobject.QualityDecision;

class JdbcAnomalyRepositoryTest {
	
	private final static Instant FIXED_INSTANT = Instant.parse("2026-02-16T00:00:00Z");
	private final static String DESCRIPTION = "anomalyTest";
	private final static String VALID_DOC_ID = "XXX-000-091991";
	private final static String VALID_ACTOR_ID = "0000";
	private static final String TABLE = "anomaly.anomalies_test";
	private static JdbcAnomalyRepository repo;
	private static ConnectionConfig config;
	
	@BeforeAll
	static void setUpBeforeClass() throws Exception {
		config = new ConnectionConfig("jdbc:mysql://localhost:3306/anomaly", "anomaly_user", "anomaly2026");
		try {
			repo = new JdbcAnomalyRepository(config, TABLE);
		} catch (Exception e) {
			e.printStackTrace();
		}
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
	void saveAndFindById_shouldReturntheSameAnomaly() {
		Anomaly anomaly = new Anomaly(DESCRIPTION, new EventTrace(VALID_ACTOR_ID,FIXED_INSTANT));
		repo.save(anomaly);
		Anomaly newAnomaly = assertDoesNotThrow(()->repo.findById(anomaly.getId()));
		
		assertEquals(anomaly.getId().toString(), newAnomaly.getId().toString());
		assertNull(newAnomaly.getChildId());
		assertNull(newAnomaly.getParentId());
		assertEquals(anomaly.getTraceability().getCreation().actorId(), newAnomaly.getTraceability().getCreation().actorId());
		assertEquals(anomaly.getTraceability().getCreation().instant(), newAnomaly.getTraceability().getCreation().instant());
		assertNull(newAnomaly.getTraceability().getToCorrected());
		assertNull(newAnomaly.getTraceability().getToResolved());
		assertNull(newAnomaly.getTraceability().getToArchived());
		assertEquals(anomaly.getAnomalyState(), newAnomaly.getAnomalyState());
		assertNull(newAnomaly.getCorrectiveAction());
		assertNull(newAnomaly.getProvingDocument());
		assertEquals(anomaly.getQualityDecision(), newAnomaly.getQualityDecision());
		assertEquals(anomaly.getDescription().description(), newAnomaly.getDescription().description());
	}
}
