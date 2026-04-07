package application.repository;

import java.util.List;
import java.util.UUID;

import application.query.QueryContext;
import domain.anomaly.Anomaly;
import domain.exception.InconsistentAnomalyStateException;
import infrastructure.exception.AnomalyNotFoundException;

public interface AnomalyRepository {
	
	public void save(Anomaly anomaly);
	public void saveAtomic(Anomaly anomaly1, Anomaly anomaly2);
	public Anomaly findById(UUID id) throws AnomalyNotFoundException, InconsistentAnomalyStateException;
	public List<Anomaly> findByContext(QueryContext queryContext) ;
	public int getMaxSequenceByYear(int year);
	
}
