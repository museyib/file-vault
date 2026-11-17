package llc.devlab.app.service;


import llc.devlab.app.config.FileProperties;
import llc.devlab.app.exception.AppException;
import llc.devlab.app.exception.ResourceNotFoundException;
import llc.devlab.app.exception.UnsupportedFileTypeException;
import llc.devlab.app.mapper.FileMapper;
import llc.devlab.app.model.dto.FileDto;
import llc.devlab.app.model.dto.FileInfo;
import llc.devlab.app.model.dto.FileUploadRequest;
import llc.devlab.app.model.dto.PageResponse;
import llc.devlab.app.model.entity.FileEntity;
import llc.devlab.app.repository.FileRepository;
import llc.devlab.app.security.AccountPrincipal;
import llc.devlab.app.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.tika.mime.MimeType;
import org.apache.tika.mime.MimeTypes;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.DigestUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.UUID;
import java.util.function.Function;

@Service
@RequiredArgsConstructor
@Slf4j
public class FileService {
    private final FileProperties fileProperties;
    private final FileRepository fileRepository;
    private final FileMapper fileMapper;

    @Transactional
    public FileInfo upload(MultipartFile file, FileUploadRequest request) {
        checkFileValidation(file);

        FileEntity fileEntity = saveFileEntity(file, request);

        saveFile(file, fileEntity.getFileName());

        return new FileInfo(fileEntity.getFileName(), fileEntity.getCheckSum());
    }

    public byte[] getFileContent(Long id) {
        AccountPrincipal principal = SecurityUtils.currentPrincipal();
        FileEntity fileEntity = fileRepository.findByIdAndOwner(id, principal.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("File not found"));
        File file = new File(fileEntity.getPath(), fileEntity.getFileName());
        try {
            String checkSum = fileEntity.getCheckSum();
            byte[] bytes = FileUtils.readFileToByteArray(file);
            String actualCheckSum = DigestUtils.md5DigestAsHex(bytes);
            if (!checkSum.equals(actualCheckSum)) {
                throw new AppException("File content has been modified", null);
            }
            return bytes;
        } catch (IOException e) {
            throw new AppException("Failed to read file content", e);
        }
    }

    public PageResponse<FileDto> getAllFiles(int page, int size) {
        AccountPrincipal principal = SecurityUtils.currentPrincipal();
        Page<FileEntity> files = fileRepository.findAllByOwner(principal.getUsername(), PageRequest.of(page, size));
        return buildPageResponse(files, fileMapper::toDto);
    }

    public FileDto getFileById(Long id) {
        AccountPrincipal principal = SecurityUtils.currentPrincipal();
        FileEntity fileEntity = fileRepository.findByIdAndOwner(id, principal.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("File not found"));
        return fileMapper.toDto(fileEntity);
    }


    private void checkFileValidation(MultipartFile file) {

        String type = getFileExtension(file);
        if (type == null || !fileProperties.allowedFileTypes().contains(type)) {
            throw new UnsupportedFileTypeException("Invalid file type");
        }
    }

    private String getFileExtension(MultipartFile file) {
        String contentType = file.getContentType();
        if (contentType == null) {
            return "";
        }

        try {
            MimeTypes allTypes = MimeTypes.getDefaultMimeTypes();
            MimeType mimeType = allTypes.forName(contentType);
            return mimeType.getExtension();
        } catch (Exception e) {
            return "";
        }
    }

    private void saveFile(MultipartFile file, String fileName) {
        AccountPrincipal principal = SecurityUtils.currentPrincipal();
        Path location = Paths.get(getLocation(principal.getUsername()));
        File fileToSave = location.resolve(fileName).toFile();
        try {
            if (!Files.exists(location)) {
                Files.createDirectories(location);
            }
            file.transferTo(fileToSave);
        } catch (IOException e) {
            log.error("Error occurred while saving file", e);
            throw new AppException("Error occurred while saving file: " + e.getMessage(), e);
        }
    }

    private FileEntity saveFileEntity(MultipartFile file, FileUploadRequest request) {
        AccountPrincipal principal = SecurityUtils.currentPrincipal();
        if (fileRepository.existsByTitleAndOwner(request.title(), principal.getUsername())) {
            throw new AppException("File with the same title already exists", null);
        }

        long fileSize = file.getSize();
        String fileExtension = getFileExtension(file);
        String fileName = UUID.randomUUID().toString().concat(fileExtension);
        String checkSum;

        try {
            checkSum = DigestUtils.md5DigestAsHex(file.getInputStream());
        } catch (IOException e) {
            log.error("Error occurred while calculating checksum", e);
            throw new AppException("Error occurred while calculating checksum", e);
        }

        FileEntity fileEntity = new FileEntity();
        fileEntity.setFileName(fileName);
        fileEntity.setFileExtension(getFileExtension(file));
        fileEntity.setFileSize(fileSize);
        fileEntity.setCheckSum(checkSum);
        fileEntity.setPath(getLocation(principal.getUsername()));
        fileEntity.setUploadDate(Instant.now());
        fileEntity.setTitle(request.title());
        fileEntity.setCategory(request.category());
        fileEntity.setDescription(request.description());

        return fileRepository.save(fileEntity);
    }

    private String getLocation(String accountFolder) {
        String pathSeparator = FileSystems.getDefault().getSeparator();
        return System.getProperty("user.home")
                .concat(pathSeparator)
                .concat(fileProperties.rootFolder())
                .concat(pathSeparator)
                .concat(accountFolder);
    }

    public <T, R> PageResponse<R> buildPageResponse(Page<T> page, Function<T, R> mapper) {
        return new PageResponse<>(page.getContent().stream().map(mapper).toList(), page.getNumber(), page.getSize(), page.getTotalElements(), page.getTotalPages(), page.isFirst(), page.isLast());
    }
}
