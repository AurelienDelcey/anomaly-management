package application;

public record QuerySuccess<T>(T payload) implements QueryResult<T>{

}
