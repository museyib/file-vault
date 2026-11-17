package llc.devlab.app.mapper;

import llc.devlab.app.model.dto.FileDto;
import llc.devlab.app.model.entity.FileEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface FileMapper {

    FileDto toDto(FileEntity fileEntity);
}
