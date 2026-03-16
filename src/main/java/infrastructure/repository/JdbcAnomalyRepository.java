package infrastructure.repository;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import application.repository.AnomalyRepository;
import domain.anomaly.Anomaly;
import domain.anomaly.ProlongationContext;
import domain.exception.IllegalTraceErasureTentative;
import domain.exception.InconsistentAnomalyStateException;
import domain.traceability.EventTrace;
import domain.traceability.Traceability;
import domain.valueobject.CorrectiveAction;
import domain.valueobject.Description;
import domain.valueobject.Evidence;
import infrastructure.exception.AnomalyNotFoundException;
import infrastructure.exception.TechnicalException;
import static infrastructure.repository.AnomalyRepositoryMapper.mapAnomaly;

public class JdbcAnomalyRepository implements AnomalyRepository{
	
	private static final Logger log = LoggerFactory.getLogger(JdbcAnomalyRepository.class);
	private final ConnectionConfig config;
	private final String tableName;
	private final String INSERT_STATEMENT ;
	private final String UPDATE_STATEMENT ;

	public JdbcAnomalyRepository(ConnectionConfig config, String tableName) {
		this.config = config;
		this.tableName = tableName;
		this.INSERT_STATEMENT = """
			INSERT INTO %s(
				id,
				parent_id,
				child_id,
				anomaly_state,
				description,
				corrective_action_id,
				quality_decision,
				proving_document_id,
				created_by,
				created_at,
				corrected_by,
				corrected_at,
				resolved_by,
				resolved_at,
				archived_by,
				archived_at,
				sector,
				prolongation_comment
			) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?);
			""".formatted(tableName);
		this.UPDATE_STATEMENT = """
				UPDATE %s
				SET parent_id = ?,
					child_id = ?,
					anomaly_state = ?,
					description = ?,
					corrective_action_id = ?,
					quality_decision = ?,
					proving_document_id = ?,
					created_by = ?,
					created_at = ?,
					corrected_by = ?,
					corrected_at = ?,
					resolved_by = ?,
					resolved_at = ?,
					archived_by = ?,
					archived_at = ?,
					sector = ?,
					prolongation_comment = ?
				WHERE id = ?;
				""".formatted(tableName);
	}

	@Override
	public void save(Anomaly anomaly) {
		if(anomaly==null) {
			throw new IllegalArgumentException("Anomaly should exist.");
		}
		log.debug("save requested - anomalyId={}", anomaly.getId());
		try(Connection connection = openConnection()){
			if(existsById(anomaly, connection)) {
				try(PreparedStatement preparedStatement = prepareUpdateStatement(connection, anomaly)){
					if(preparedStatement.executeUpdate() != 1) {
						throw new TechnicalException("Persistence error: update fail.");
					}
					log.debug("update success 1 row affected - anomalyId={}", anomaly.getId());
				}
			}else {
				try(PreparedStatement preparedStatement = prepareInsertStatement(connection, anomaly)){
					if(preparedStatement.executeUpdate() != 1) {
						throw new TechnicalException("Persistence error: insertion fail.");
					}
					log.debug("save success 1 row affected - anomalyId={}", anomaly.getId());
				}
			}
		}catch (SQLException e) {
			log.warn("technical SQL exception when saving anomaly - anomalyId={}", anomaly.getId());
			throw new TechnicalException("Persistence error.",e);
		}
	}

	@Override
	public void saveAtomic(Anomaly anomaly1, Anomaly anomaly2) {
		if(anomaly1 == null || anomaly2 == null) {
			throw new IllegalArgumentException("Anomalies should exist.");
		}
		log.debug("saveAtomic requested - parentAnomalyId={}, childAnomalyId={}", anomaly1.getId(), anomaly2.getId());
		try(Connection connection = openConnection()){
			if (!existsById(anomaly1, connection) || existsById(anomaly2, connection)) {
				log.warn("saveAtomic fail parent anomaly does not exist or child anomaly already exist - parentAnomalyId={}, childAnomalyId={}", anomaly1.getId(), anomaly2.getId());
			    throw new TechnicalException();
			}
			connection.setAutoCommit(false);
			try(PreparedStatement preparedUpdateStatement = prepareUpdateStatement(connection, anomaly1);
					PreparedStatement preparedInsertStatement = prepareInsertStatement(connection, anomaly2)){
				int resultUpdate = preparedUpdateStatement.executeUpdate();
				log.debug("saveAtomic parent anomaly update success - parentAnomalyId={}", anomaly1.getId());
				int resultInsert = preparedInsertStatement.executeUpdate();
				log.debug("saveAtomic child anomaly insert success - childAnomalyId={}", anomaly2.getId());
					if(resultInsert != 1 || resultUpdate != 1) {
						connection.rollback();
						connection.setAutoCommit(true);
						log.warn("saveAtomic fail - row affected={}, parentAnomalyId={}, childAnomalyId={}", resultInsert+resultUpdate, anomaly1.getId(), anomaly2.getId());
						throw new TechnicalException("Persistence error: transaction fail.");
					}
				}catch(Exception e) {
					connection.rollback();
					connection.setAutoCommit(true);
					log.warn("saveAtomic fail for technical exception - parentAnomalyId={}, childAnomalyId={}", anomaly1.getId(), anomaly2.getId());
					throw e;
				}
				connection.commit();
				connection.setAutoCommit(true);
				log.debug("saveAtomic success - parentAnomalyId={}, childAnomalyId={}", anomaly1.getId(), anomaly2.getId());
		}catch (SQLException e) {
			log.warn("saveAtomic fail for technical SQL exception - parentAnomalyId={}, childAnomalyId={}", anomaly1.getId(), anomaly2.getId());
			throw new TechnicalException("Persistence error.",e);
		}
	}

	@Override
	public Anomaly findById(UUID id) throws AnomalyNotFoundException, InconsistentAnomalyStateException {
		log.debug("findByID requested - anomalyId={}", id);
		try(Connection connection = openConnection()){
			try(PreparedStatement preparedStatement = connection.prepareStatement("""
					SELECT * FROM %s
					WHERE id = ?
					""".formatted(tableName))){
				preparedStatement.setString(1, id.toString());
				try(ResultSet result = preparedStatement.executeQuery()){
					if(!result.next()) {
						throw new AnomalyNotFoundException();
					}
					Anomaly anomaly = mapAnomaly(result);
					log.debug("findByID request success - anomalyId={}", id);
					return anomaly;
				}
			}
		} catch (SQLException | IllegalTraceErasureTentative e) {
			log.warn("findById failure for SQL technical exception- anomalyId={}", id);
			throw new TechnicalException("impossible to reconstruct anomaly", e);
		}
	}

	@Override
	public List<Anomaly> findAll(int page) throws InconsistentAnomalyStateException{
		log.debug("findAll requested - page={}", page);
		try(Connection connection = openConnection()){
			try(PreparedStatement preparedStatement = connection.prepareStatement("""
					SELECT * FROM %s
					ORDER BY created_at DESC
					LIMIT 50
					OFFSET ?
					""".formatted(tableName))){
				preparedStatement.setInt(1, 50*(page-1));
				try(ResultSet result = preparedStatement.executeQuery()){
					List<Anomaly> anomalies = new ArrayList<>();
					while(result.next()) {
						anomalies.add(mapAnomaly(result));
					}
					log.debug("findAll success - page={}", page);
					return anomalies;
				}
			}
		} catch (SQLException | IllegalTraceErasureTentative e) {
			log.warn("findAll failure for SQL technical exception- page={}", page);
			throw new TechnicalException("impossible to reconstruct anomaly", e);
		}
	}
	
	private Connection openConnection() throws SQLException {
		Connection connection = DriverManager.getConnection(config.url(), config.user(), config.password());
		return connection;
	}
	
	private PreparedStatement prepareInsertStatement(Connection connection, Anomaly anomaly) throws SQLException {
		PreparedStatement query = connection.prepareStatement(INSERT_STATEMENT);
		Traceability traceability = anomaly.getTraceability();
		ProlongationContext prolongationContext = anomaly.getProlongationContext();
		UUID childId = anomaly.getChildId();
		Description description = anomaly.getDescription();
		CorrectiveAction correctiveAction = anomaly.getCorrectiveAction();
		Evidence evidence = anomaly.getEvidence();
		EventTrace created = traceability.getCreation();
		EventTrace corrected = traceability.getToCorrected();
		EventTrace resolved = traceability.getToResolved();
		EventTrace archived = traceability.getToArchived();
		
		query.setString(1, anomaly.getId().toString());
		query.setString(2, prolongationContext == null ? null:prolongationContext.parentId().toString());
		query.setString(3, stringOrNullFromUuid(childId));
		query.setString(4, anomaly.getAnomalyState().name());
		query.setString(5, description.description());
		query.setString(6, correctiveAction == null ? null:correctiveAction.documentId());
		query.setString(7, anomaly.getQualityDecision().name());
		query.setString(8, evidence == null ? null:evidence.documentId());
		query.setString(9, stringOrNullFromEventTrace(created));
		query.setTimestamp(10, timestampOrNullFromEventTrace(created));
		query.setString(11, stringOrNullFromEventTrace(corrected));
		query.setTimestamp(12, timestampOrNullFromEventTrace(corrected));
		query.setString(13, stringOrNullFromEventTrace(resolved));
		query.setTimestamp(14, timestampOrNullFromEventTrace(resolved));
		query.setString(15, stringOrNullFromEventTrace(archived));
		query.setTimestamp(16, timestampOrNullFromEventTrace(archived));
		query.setString(17, anomaly.getSector().name());
		query.setString(18, prolongationContext == null ? null:prolongationContext.prolongationComment());

		return query;
	}
	
	private PreparedStatement prepareUpdateStatement(Connection connection, Anomaly anomaly) throws SQLException {
		PreparedStatement query = connection.prepareStatement(UPDATE_STATEMENT);
		Traceability traceability = anomaly.getTraceability();
		ProlongationContext prolongationContext = anomaly.getProlongationContext();
		UUID childId = anomaly.getChildId();
		Description description = anomaly.getDescription();
		CorrectiveAction correctiveAction = anomaly.getCorrectiveAction();
		Evidence evidence = anomaly.getEvidence();
		EventTrace created = traceability.getCreation();
		EventTrace corrected = traceability.getToCorrected();
		EventTrace resolved = traceability.getToResolved();
		EventTrace archived = traceability.getToArchived();
		
		query.setString(1, prolongationContext == null ? null:prolongationContext.parentId().toString());
		query.setString(2, stringOrNullFromUuid(childId));
		query.setString(3, anomaly.getAnomalyState().name());
		query.setString(4, description.description());
		query.setString(5, correctiveAction == null ? null:correctiveAction.documentId());
		query.setString(6, anomaly.getQualityDecision().name());
		query.setString(7, evidence == null ? null:evidence.documentId());
		query.setString(8, stringOrNullFromEventTrace(created));
		query.setTimestamp(9, timestampOrNullFromEventTrace(created));
		query.setString(10, stringOrNullFromEventTrace(corrected));
		query.setTimestamp(11, timestampOrNullFromEventTrace(corrected));
		query.setString(12, stringOrNullFromEventTrace(resolved));
		query.setTimestamp(13, timestampOrNullFromEventTrace(resolved));
		query.setString(14, stringOrNullFromEventTrace(archived));
		query.setTimestamp(15, timestampOrNullFromEventTrace(archived));
		query.setString(16, anomaly.getSector().name());
		query.setString(17, prolongationContext == null ? null:prolongationContext.prolongationComment());
		query.setString(18, anomaly.getId().toString());

		return query;
	}

	

	private boolean existsById(Anomaly anomaly, Connection connection) throws SQLException{
		try(PreparedStatement preparedStatement = connection.prepareStatement("""
						SELECT id FROM %s
						WHERE id = ?
						""".formatted(tableName))){
			preparedStatement.setString(1, anomaly.getId().toString());
			try (ResultSet result = preparedStatement.executeQuery()) {
			        return result.next();
			}
		}
	}
	
	private String stringOrNullFromUuid(UUID id) {
		return id == null ? null : id.toString();
	}
	
	private String stringOrNullFromEventTrace(EventTrace trace) {
		return trace == null ? null : trace.actorId();
	}
	
	private Timestamp timestampOrNullFromEventTrace(EventTrace trace) {
		return trace == null ? null : Timestamp.from(trace.instant());
	}
}
