package application.query;

public sealed interface QueryResult<T>
		permits QuerySuccess, QueryNotFound, QueryFailure{

}
