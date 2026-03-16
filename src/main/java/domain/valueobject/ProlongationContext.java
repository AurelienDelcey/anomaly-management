package domain.valueobject;

import java.util.UUID;

public record ProlongationContext(UUID parentId, String prolongationComment) {
		public ProlongationContext{
			if(parentId == null || prolongationComment == null || prolongationComment.isBlank()) {
				throw new IllegalArgumentException("Prolongation context cannot be created: parentId and non-blank prolongationComment are required.");
			}
		}
}
