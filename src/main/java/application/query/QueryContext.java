package application.query;

public record QueryContext(boolean includeArchived, SortingSelection sortingSelection, int page) {
	public QueryContext{
		if(page <= 0) {
			throw new IllegalArgumentException("Page must be a positive number.");
		}
	}
}
