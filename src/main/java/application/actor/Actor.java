package application.actor;

public record Actor(String id,String name, Role role) {
	public Actor{
		if(name == null || name.isBlank() || id == null || !id.matches("[0-9]{3,}")) {
			throw new IllegalArgumentException("Actor name must not be blank and ID must contain at least 3 digits");
		}
	}
}
