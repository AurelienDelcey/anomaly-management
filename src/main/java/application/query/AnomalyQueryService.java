package application.query;

import java.util.List;
import java.util.UUID;

import application.dto.AnomalyDto;
import application.dto.AnomalyDtoMapper;
import application.repository.AnomalyRepository;
import domain.anomaly.Anomaly;
import infrastructure.exception.AnomalyNotFoundException;
import infrastructure.exception.TechnicalException;

public class AnomalyQueryService {
	
	private final AnomalyRepository repository;

	public AnomalyQueryService(AnomalyRepository repository) {
		this.repository = repository;
	}
	
	public QueryResult<AnomalyDto> findById(UUID id) {
		try {
			AnomalyDto result = AnomalyDtoMapper.mapToDto(repository.findById(id));
			return new QuerySuccess<AnomalyDto>(result);
		}catch(AnomalyNotFoundException e) {
			return new QueryNotFound<AnomalyDto>();
		}catch(TechnicalException e) {
			return new QueryFailure<AnomalyDto>(e.getMessage());
		}
	}
	
	public QueryResult<List<AnomalyDto>> findPage(int page) {
		try {
			List<Anomaly> anomalies = repository.findAll(page);
			List<AnomalyDto> anomalyDtos = anomalies.stream()
					.map(AnomalyDtoMapper::mapToDto)
					.toList();
			return new QuerySuccess<List<AnomalyDto>>(anomalyDtos);
		}catch(TechnicalException e) {
			return new QueryFailure<List<AnomalyDto>>(e.getMessage());
		}
	}
}
