package application;

import java.util.UUID;

import domain.anomaly.Anomaly;

public interface Repo {
	
	public void save(Anomaly anomaly);
	public void saveAtomic(Anomaly anomaly1, Anomaly anomaly2);
	public Anomaly findById(UUID id);
	
}
