package llc.devlab.app.repository;

import llc.devlab.app.model.entity.FileEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface FileRepository extends JpaRepository<FileEntity, Long> {
    Page<FileEntity> findAllByOwner(String owner, Pageable pageable);

    Optional<FileEntity> findByIdAndOwner(Long id, String owner);

    boolean existsByTitleAndOwner(String title, String owner);
}
