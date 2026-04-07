package application.query;

public enum SortingSelection {
STATE{
	public String getFormatForQueries() {
		return "anomaly_state";
	}
},
DATE{
	public String getFormatForQueries() {
		return "created_at";
	}
},SECTOR{
	public String getFormatForQueries() {
		return "sector";
	}
},ID{
	public String getFormatForQueries() {
		return "sequence";
	}
};
	
	public abstract String getFormatForQueries();
}
