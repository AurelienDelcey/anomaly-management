package application.query;

public record QuerySuccess<T>(T payload) implements QueryResult<T>{

}
