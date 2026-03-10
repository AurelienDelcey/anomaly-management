package infrastructure.repository;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import application.repository.Repo;
import domain.anomaly.Anomaly;
import domain.anomaly.AnomalyState;
import domain.exception.IllegalTraceErasureTentative;
import domain.exception.InconsistentAnomalyStateException;
import domain.traceability.EventTrace;
import domain.traceability.Traceability;
import domain.valueobject.CorrectiveAction;
import domain.valueobject.Description;
import domain.valueobject.Evidence;
import domain.valueobject.QualityDecision;
import infrastructure.exception.AnomalyNotFoundException;
import infrastructure.exception.TechnicalException;

public class JdbcAnomalyRepository implements Repo{
	
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
				archived_at
			) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?);
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
					archived_at = ?
				WHERE id = ?;
				""".formatted(tableName);
	}

	@Override
	public void save(Anomaly anomaly) {
		if(anomaly==null) {
			throw new IllegalArgumentException("Anomaly should exist.");
		}
		boolean alreadyInTable = false;
		try(Connection connection = getConnection()){
			alreadyInTable = isExist(anomaly, connection);
			if(alreadyInTable) {
				try(PreparedStatement preparedStatement = bindUpdateStatement(connection, anomaly)){
					int result = preparedStatement.executeUpdate();
					if(result != 1) {
						throw new TechnicalException("Persistence error: update fail.");
					}
				}
			}else {
				try(PreparedStatement preparedStatement = bindInsertStatement(connection, anomaly)){
					int result = preparedStatement.executeUpdate();
					if(result != 1) {
						throw new TechnicalException("Persistence error: insertion fail.");
					}
				}
			}
		}catch (SQLException e) {
			throw new TechnicalException("Persistence error.",e);
		}
	}

	@Override
	public void saveAtomic(Anomaly anomaly1, Anomaly anomaly2) {
		if(anomaly1 == null || anomaly2 == null) {
			throw new IllegalArgumentException("Anomalies should exist.");
		}
		boolean firstAlreadyInTable = false;
		boolean secondAlreadyInTable = false;
		try(Connection connection = getConnection()){
			firstAlreadyInTable = isExist(anomaly1, connection);
			secondAlreadyInTable = isExist(anomaly2, connection);
			if(!(firstAlreadyInTable && !secondAlreadyInTable)) {
				throw new TechnicalException();
			}
			connection.setAutoCommit(false);
			try(PreparedStatement preparedUpdateStatement = bindUpdateStatement(connection, anomaly1);
					PreparedStatement preparedInsertStatement = bindInsertStatement(connection, anomaly2)){
				int resultUpdate = preparedUpdateStatement.executeUpdate();
				int resultInsert = preparedInsertStatement.executeUpdate();
					if(resultInsert != 1 || resultUpdate != 1) {
						connection.rollback();
						connection.setAutoCommit(true);
						throw new TechnicalException("Persistence error: transaction fail.");
					}
				}catch(Exception e) {
					connection.rollback();
					connection.setAutoCommit(true);
					throw e;
				}
				connection.commit();
				connection.setAutoCommit(true);
		}catch (SQLException e) {
			throw new TechnicalException("Persistence error.",e);
		}
	}

	@Override
	public Anomaly findById(UUID id) throws AnomalyNotFoundException, InconsistentAnomalyStateException {
		try(Connection connection = getConnection()){
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
					return anomaly;
				}
			}
		} catch (SQLException | IllegalTraceErasureTentative e) {
			throw new TechnicalException("impossible to reconstruct anomaly", e);
		}
	}

	@Override
	public List<Anomaly> findAll(int page) throws InconsistentAnomalyStateException{
		try(Connection connection = getConnection()){
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
					return anomalies;
				}
			}
		} catch (SQLException | IllegalTraceErasureTentative e) {
			throw new TechnicalException("impossible to reconstruct anomaly", e);
		}
	}
	
	private Connection getConnection() throws SQLException {
		Connection connection = DriverManager.getConnection(config.url(), config.user(), config.password());
		return connection;
	}
	
	private PreparedStatement bindInsertStatement(Connection connection, Anomaly anomaly) throws SQLException {
		PreparedStatement stmt = connection.prepareStatement(INSERT_STATEMENT);
		Traceability traceability = anomaly.getTraceability();
		UUID parentId = anomaly.getParentId();
		UUID childId = anomaly.getChildId();
		Description description = anomaly.getDescription();
		CorrectiveAction correctiveAction = anomaly.getCorrectiveAction();
		Evidence evidence = anomaly.getEvidence();
		EventTrace created = traceability.getCreation();
		EventTrace corrected = traceability.getToCorrected();
		EventTrace resolved = traceability.getToResolved();
		EventTrace archived = traceability.getToArchived();
		
		stmt.setString(1, anomaly.getId().toString());
		stmt.setString(2, parentId == null ? null:parentId.toString());
		stmt.setString(3, childId == null ? null:childId.toString());
		stmt.setString(4, anomaly.getAnomalyState().toString());
		stmt.setString(5, description.description());
		stmt.setString(6, correctiveAction == null ? null:correctiveAction.documentId());
		stmt.setString(7, anomaly.getQualityDecision().toString());
		stmt.setString(8, evidence == null ? null:evidence.documentId());
		stmt.setString(9, created.actorId());
		stmt.setTimestamp(10, Timestamp.from(created.instant()));
		stmt.setString(11, corrected == null ? null:corrected.actorId());
		stmt.setTimestamp(12, corrected == null ? null:Timestamp.from(corrected.instant()));
		stmt.setString(13, resolved == null ? null:resolved.actorId());
		stmt.setTimestamp(14, resolved == null ? null:Timestamp.from(resolved.instant()));
		stmt.setString(15, archived == null ? null:archived.actorId());
		stmt.setTimestamp(16, archived == null ? null:Timestamp.from(archived.instant()));

		return stmt;
	}
	
	private PreparedStatement bindUpdateStatement(Connection connection, Anomaly anomaly) throws SQLException {
		PreparedStatement stmt = connection.prepareStatement(UPDATE_STATEMENT);
		Traceability traceability = anomaly.getTraceability();
		UUID parentId = anomaly.getParentId();
		UUID childId = anomaly.getChildId();
		Description description = anomaly.getDescription();
		CorrectiveAction correctiveAction = anomaly.getCorrectiveAction();
		Evidence evidence = anomaly.getEvidence();
		EventTrace created = traceability.getCreation();
		EventTrace corrected = traceability.getToCorrected();
		EventTrace resolved = traceability.getToResolved();
		EventTrace archived = traceability.getToArchived();
		
		stmt.setString(1, parentId == null ? null:parentId.toString());
		stmt.setString(2, childId == null ? null:childId.toString());
		stmt.setString(3, anomaly.getAnomalyState().toString());
		stmt.setString(4, description.description());
		stmt.setString(5, correctiveAction == null ? null:correctiveAction.documentId());
		stmt.setString(6, anomaly.getQualityDecision().toString());
		stmt.setString(7, evidence == null ? null:evidence.documentId());
		stmt.setString(8, created.actorId());
		stmt.setTimestamp(9, Timestamp.from(created.instant()));
		stmt.setString(10, corrected == null ? null:corrected.actorId());
		stmt.setTimestamp(11, corrected == null ? null:Timestamp.from(corrected.instant()));
		stmt.setString(12, resolved == null ? null:resolved.actorId());
		stmt.setTimestamp(13, resolved == null ? null:Timestamp.from(resolved.instant()));
		stmt.setString(14, archived == null ? null:archived.actorId());
		stmt.setTimestamp(15, archived == null ? null:Timestamp.from(archived.instant()));
		stmt.setString(16, anomaly.getId().toString());

		return stmt;
	}

	private Anomaly mapAnomaly(ResultSet result) throws SQLException, IllegalTraceErasureTentative, InconsistentAnomalyStateException {
		String id = result.getString("id");
		String parentId = result.getString("parent_id");
		String childId = result.getString("child_id");
		String state = result.getString("anomaly_state");
		String decision = result.getString("quality_decision");
	
		UUID anomalyId = id == null?null:UUID.fromString(id);
		UUID anomalyParentId = parentId == null?null:UUID.fromString(parentId);
		UUID anomalyChildId = childId == null?null:UUID.fromString(childId);
		
		String description = result.getString("description");
		Description anomalyDescription = description == null?null:new Description(description);
		
		String correctiveAction = result.getString("corrective_action_id");
		CorrectiveAction anomalyCorrectiveAction = correctiveAction == null?null:new CorrectiveAction(correctiveAction);
		
		String evidence = result.getString("proving_document_id");
		Evidence anomalyEvidence = evidence == null?null:new Evidence(evidence);
		
		String createdBy = result.getString("created_by");
		Timestamp createdAt = result.getTimestamp("created_at");
		String correctedBy = result.getString("corrected_by");
		Timestamp correctedAt = result.getTimestamp("corrected_at");
		String resolvedBy = result.getString("resolved_by");
		Timestamp resolvedAt = result.getTimestamp("resolved_at");
		String archivedBy = result.getString("archived_by");
		Timestamp archivedAt = result.getTimestamp("archived_at");
		
		Instant createInstant = createdAt == null?null:createdAt.toInstant();
		Instant correctedInstant = correctedAt == null?null:correctedAt.toInstant();
		Instant resolvedInstant = resolvedAt == null?null:resolvedAt.toInstant();
		Instant archivedInstant = archivedAt == null?null:archivedAt.toInstant();
		
		EventTrace created = new EventTrace(createdBy, createInstant);
		EventTrace corrected = correctedInstant == null?null:new EventTrace(correctedBy, correctedInstant);
		EventTrace resolved = resolvedInstant == null?null:new EventTrace(resolvedBy, resolvedInstant);
		EventTrace archived = archivedInstant == null?null:new EventTrace(archivedBy, archivedInstant);
		Traceability traceability = new Traceability(created);
		traceability = corrected == null?traceability:traceability.addToCorrectedTrace(corrected);
		traceability = resolved == null?traceability:traceability.addToResolvedTrace(resolved);
		traceability = archived == null?traceability:traceability.addToArchivedTrace(archived);
		
		QualityDecision qualityDecision = switch(decision) {
		case "EMPTY" -> QualityDecision.EMPTY;
		case "NA" -> QualityDecision.NA;
		case "REPAIR" -> QualityDecision.REPAIR;
		case "SCRAP" -> QualityDecision.SCRAP;
		default -> throw new TechnicalException("Unknown decision: " + decision);
		};
		
		AnomalyState anomalyState = switch(state) {
		case "PENDING" -> AnomalyState.PENDING;
		case "CORRECTED" -> AnomalyState.CORRECTED;
		case "RESOLVED" -> AnomalyState.RESOLVED;
		case "ARCHIVED" -> AnomalyState.ARCHIVED;
		default -> throw new TechnicalException("Unknown state: " + state);
		};
		
		Anomaly anomaly = domain.anomaly.AnomalyConstructor.rehydrate(
				anomalyId, anomalyParentId, anomalyChildId, 
				anomalyCorrectiveAction, anomalyEvidence, 
				traceability, qualityDecision, anomalyState, anomalyDescription);
		
		return anomaly;
	}

	private boolean isExist(Anomaly anomaly, Connection connection) throws SQLException{
		try(PreparedStatement preparedStatement = connection.prepareStatement("""
						SELECT id FROM %s
						WHERE id = ?
						""".formatted(tableName))){
			preparedStatement.setString(1, anomaly.getId().toString());
			try (ResultSet result = preparedStatement.executeQuery()) {
			    if (result.next()) {
			        return true;
			    }
			}
		}
		return false;
	}
}
