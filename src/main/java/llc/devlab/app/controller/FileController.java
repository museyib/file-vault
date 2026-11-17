package llc.devlab.app.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import llc.devlab.app.model.dto.FileDto;
import llc.devlab.app.model.dto.FileInfo;
import llc.devlab.app.model.dto.FileUploadRequest;
import llc.devlab.app.model.dto.PageResponse;
import llc.devlab.app.service.FileService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
public class FileController {

    private final FileService fileService;

    @PostMapping(value = "/upload", consumes = "multipart/form-data")
    public ResponseEntity<FileInfo> upload(@RequestPart("file") MultipartFile file,
                                           @Valid @RequestPart("properties") FileUploadRequest request) {
        return ResponseEntity.ok(fileService.upload(file, request));
    }

    @GetMapping("/{id}/download")
    public ResponseEntity<byte[]> download(@PathVariable Long id) {
        byte[] fileContent = fileService.getFileContent(id);
        return ResponseEntity.ok(fileContent);
    }

    @GetMapping
    public ResponseEntity<PageResponse<FileDto>> getAllFiles(@RequestParam(defaultValue = "0") int page,
                                                             @RequestParam(defaultValue = "20") @Max(200) int size) {
        return ResponseEntity.ok(fileService.getAllFiles(page, size));
    }

    @GetMapping("/{id}")
    public ResponseEntity<FileDto> getAllFilesDto(@PathVariable Long id) {
        return ResponseEntity.ok(fileService.getFileById(id));
    }
}
