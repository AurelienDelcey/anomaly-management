package application;

public record Actor(String id, int privilegeLevel) {
	public Actor{
		if(id == null || !id.matches("[0-9]{3,}") || privilegeLevel < 0 || privilegeLevel > 3) {
			throw new IllegalArgumentException("Actor ID should have 3 numbers at least or privilege level should be 0, 1, 2 ,or 3");
		}
	}
}
