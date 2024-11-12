package com.sysmatic2.finalbe.file.service;

import com.sysmatic2.finalbe.file.entity.File;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import org.springframework.mock.web.MockMultipartFile;

import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import java.io.IOException;


import static org.junit.jupiter.api.Assertions.*;


@SpringBootTest
@ExtendWith(MockitoExtension.class)
public class FileServiceTest {

    @Autowired
    private FileService fileService;

    @Test
    public void testUploadFile() throws IOException {
        // given
        MockMultipartFile mockFile = new MockMultipartFile(
                "file",
                "test-image.jpg",
                "image/jpeg",
                "Test File Content".getBytes()
        );

        Long memberId = 1L;
        String fileType = "PROFILE";
        String relatedEntity = "USER";
        Long relatedEntityId = 1L;

        // when
        File uploadedFile = fileService.uploadFile(mockFile, memberId, fileType, relatedEntity, relatedEntityId);

        // then
        assertNotNull(uploadedFile);
        assertEquals("test-image.jpg", uploadedFile.getFileName());
        assertEquals("PROFILE", uploadedFile.getFileType());
        assertEquals("image/jpeg", uploadedFile.getContentType());
        assertEquals(mockFile.getSize(), uploadedFile.getFileSize());
        assertEquals(relatedEntity, uploadedFile.getRelatedEntity());
        assertEquals(relatedEntityId, uploadedFile.getRelatedEntityId());
    }
}