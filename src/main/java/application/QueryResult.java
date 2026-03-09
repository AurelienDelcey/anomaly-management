package application;

public sealed interface QueryResult<T>
		permits QuerySuccess, QueryNotFound, QueryFailure{

}
