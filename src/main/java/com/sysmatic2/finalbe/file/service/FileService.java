//package com.sysmatic2.finalbe.file.service;
//
//import com.sysmatic2.finalbe.file.entity.File;
//import com.sysmatic2.finalbe.file.repository.FileRepository;
//import lombok.Setter;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.stereotype.Service;
//import org.springframework.web.multipart.MultipartFile;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.core.env.Environment;
//
//import java.io.IOException;
//import java.nio.file.Files;
//import java.nio.file.Path;
//import java.nio.file.Paths;
//import java.time.LocalDateTime;
//import java.util.UUID;
//
//@Service
//public class FileService {
//
//    private final FileRepository fileRepository;
//
//    @Value("${part.upload.path}")
//    private String uploadDir;
//
//    public FileService(FileRepository fileRepository) {
//        this.fileRepository = fileRepository;
//    }
//
//    public File uploadFile(MultipartFile file, Long memberId, String fileType, String relatedEntity, Long relatedEntityId) throws IOException {
//        String originalFileName = file.getOriginalFilename();
//        String storedFileName = UUID.randomUUID() + "_" + originalFileName;
//        String contentType = file.getContentType();
//        Long fileSize = file.getSize();
//        Path filePath = Paths.get(uploadDir, storedFileName); // 수정된 부분
//
//        // 디렉토리 생성
//        Files.createDirectories(filePath.getParent());
//
//        // 파일 저장
//        try {
//            file.transferTo(filePath.toFile());
//        } catch (IOException e) {
//            throw new IOException("파일 저장 중 오류가 발생했습니다.", e);
//        }
//
//        // 파일 정보 엔티티에 저장
//        File fileRecord = new File();
//        fileRecord.setMemberId(memberId);
//        fileRecord.setFileName(originalFileName);
//        fileRecord.setFileType(fileType);
//        fileRecord.setContentType(contentType);
//        fileRecord.setFileSize(fileSize);
//        fileRecord.setFilePath(filePath.toString());
//        fileRecord.setUploadedAt(LocalDateTime.now());
//        fileRecord.setRelatedEntity(relatedEntity);
//        fileRecord.setRelatedEntityId(relatedEntityId);
//
//        return fileRepository.save(fileRecord);
//    }
//}
