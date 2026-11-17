package llc.devlab.app.model.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "file", uniqueConstraints = @UniqueConstraint(columnNames = {"title", "owner"}))
@EntityListeners(AuditingEntityListener.class)
public class FileEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    private String title;

    private String description;

    private String category;

    private String fileExtension;

    private String fileName;

    private String path;

    private String checkSum;

    @CreatedBy
    private String owner;

    private Long fileSize;

    private Instant uploadDate;
}