package application.query;

public record QueryFailure<T>(String message) implements QueryResult<T>{

}
