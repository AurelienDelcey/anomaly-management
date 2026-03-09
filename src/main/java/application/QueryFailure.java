package application;

public record QueryFailure<T>(String message) implements QueryResult<T>{

}
