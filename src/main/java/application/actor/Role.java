package application.actor;

public enum Role {
	READ_ONLY, OPERATOR, SUPERVISOR;
	
	public boolean canModify() {
		return this != READ_ONLY;
	}
	
	public boolean canArchive() {
		return this == SUPERVISOR;
	}
}
