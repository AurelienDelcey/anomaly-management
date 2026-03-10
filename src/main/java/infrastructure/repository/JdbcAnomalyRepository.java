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

import application.repository.AnomalyRepository;
import domain.anomaly.Anomaly;
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
		try(Connection connection = openConnection()){
			alreadyInTable = existsById(anomaly, connection);
			if(alreadyInTable) {
				try(PreparedStatement preparedStatement = prepareUpdateStatement(connection, anomaly)){
					int result = preparedStatement.executeUpdate();
					if(result != 1) {
						throw new TechnicalException("Persistence error: update fail.");
					}
				}
			}else {
				try(PreparedStatement preparedStatement = prepareInsertStatement(connection, anomaly)){
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
		try(Connection connection = openConnection()){
			firstAlreadyInTable = existsById(anomaly1, connection);
			secondAlreadyInTable = existsById(anomaly2, connection);
			if(!(firstAlreadyInTable && !secondAlreadyInTable)) {
				throw new TechnicalException();
			}
			connection.setAutoCommit(false);
			try(PreparedStatement preparedUpdateStatement = prepareUpdateStatement(connection, anomaly1);
					PreparedStatement preparedInsertStatement = prepareInsertStatement(connection, anomaly2)){
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
					return anomaly;
				}
			}
		} catch (SQLException | IllegalTraceErasureTentative e) {
			throw new TechnicalException("impossible to reconstruct anomaly", e);
		}
	}

	@Override
	public List<Anomaly> findAll(int page) throws InconsistentAnomalyStateException{
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
					return anomalies;
				}
			}
		} catch (SQLException | IllegalTraceErasureTentative e) {
			throw new TechnicalException("impossible to reconstruct anomaly", e);
		}
	}
	
	private Connection openConnection() throws SQLException {
		Connection connection = DriverManager.getConnection(config.url(), config.user(), config.password());
		return connection;
	}
	
	private PreparedStatement prepareInsertStatement(Connection connection, Anomaly anomaly) throws SQLException {
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
	
	private PreparedStatement prepareUpdateStatement(Connection connection, Anomaly anomaly) throws SQLException {
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

	

	private boolean existsById(Anomaly anomaly, Connection connection) throws SQLException{
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
