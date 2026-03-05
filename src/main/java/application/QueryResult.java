package application;

public sealed interface QueryResult
		permits QuerySuccess, QueryNotFound, QueryFailure{

}
