package application.actor;

public record Actor(String id, Role role) {
	public Actor{
		if(id == null || !id.matches("[0-9]{3,}")) {
			throw new IllegalArgumentException("Actor ID should have 3 numbers at least");
		}
	}
}
