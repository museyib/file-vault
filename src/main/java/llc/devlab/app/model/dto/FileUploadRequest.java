package llc.devlab.app.model.dto;

import jakarta.validation.constraints.NotBlank;

public record FileUploadRequest(@NotBlank String title,
                                String description,
                                @NotBlank String category) {
}
