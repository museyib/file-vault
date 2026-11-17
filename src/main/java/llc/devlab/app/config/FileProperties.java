package llc.devlab.app.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@ConfigurationProperties(prefix = "app.file")
public record FileProperties(String rootFolder,
                             List<String> allowedFileTypes) {
}
