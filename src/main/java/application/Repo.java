package application;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import domain.anomaly.Anomaly;

public interface Repo {
	
	public void save(Anomaly anomaly);
	public void saveAtomic(Anomaly anomaly1, Anomaly anomaly2);
	public Optional<Anomaly> findByIdOptional(UUID id);
	public Anomaly findById(UUID id);
	public List<Anomaly> findAll();
	
}
