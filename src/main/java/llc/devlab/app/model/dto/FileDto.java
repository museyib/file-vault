package llc.devlab.app.model.dto;

import java.time.Instant;

public record FileDto(Long id, String title, String description, String fileName, String fileExtension, long fileSize, Instant uploadDate) {
}
