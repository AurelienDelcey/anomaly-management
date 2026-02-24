package infrastructure;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import application.Repo;
import domain.anomaly.Anomaly;
import domain.traceability.EventTrace;
import domain.traceability.Traceability;
import domain.valueobject.CorrectiveAction;
import domain.valueobject.Description;
import domain.valueobject.ProvingDocument;

public class JdbcAnomalyRepository implements Repo{
	
	private final ConnectionConfig config;
	private static final String INSERT_STATEMENT = """
			INSERT INTO anomaly.anomalies(
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
			""";
	private static final String UPDATE_STATEMENT = """
			UPDATE anomaly.anomalies
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
			""";

	public JdbcAnomalyRepository(ConnectionConfig config) {
		this.config = config;
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
			if(firstAlreadyInTable && !secondAlreadyInTable) {
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
			}
		}catch (SQLException e) {
			throw new TechnicalException("Persistence error.",e);
		}
	}

	@Override
	public Optional<Anomaly> findByIdOptional(UUID id) {
		return Optional.empty();
	}

	@Override
	public Anomaly findById(UUID id) {
		return null;
	}

	@Override
	public List<Anomaly> findAll() {
		return null;
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
		ProvingDocument provingDocument = anomaly.getProvingDocument();
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
		stmt.setString(8, provingDocument == null ? null:provingDocument.documentId());
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
		ProvingDocument provingDocument = anomaly.getProvingDocument();
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
		stmt.setString(7, provingDocument == null ? null:provingDocument.documentId());
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

	private boolean isExist(Anomaly anomaly, Connection connection) throws SQLException{
		try(PreparedStatement preparedStatement = connection.prepareStatement("""
						SELECT id FROM anomaly.anomalies
						WHERE id = ?
						""")){
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
