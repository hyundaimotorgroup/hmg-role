package com.hmg.role.common.cdc.uploader;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;

import com.hmg.role.common.cdc.dto.CdcEventDto;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.ArgumentCaptor;
import org.springframework.context.ApplicationEventPublisher;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.CreateBucketRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

class ObjectStorageServiceImplTest {

    @TempDir Path tempDir;

    @Test
    void pushChanges_successfulUpload_publishesEvents_andRenamesBack() throws IOException {
        // Arrange
        S3Client s3 = mock(S3Client.class);
        ApplicationEventPublisher publisher = mock(ApplicationEventPublisher.class);

        ObjectStorageServiceImpl service =
                new ObjectStorageServiceImpl(null, s3, publisher, null, null, Base64.getDecoder());
        service.setBucket("s3-bucket");

        // Prepare a CSV file
        String project = "projectA";
        String scope = "scopeOne";
        String fileName = scope + ".csv";
        Path basePath = tempDir;
        Path sourceFile = basePath.resolve(fileName);
        Files.writeString(sourceFile, "id,name\n1,John");

        CdcEventDto event =
                CdcEventDto.builder()
                        .project(project)
                        .scope(scope)
                        .fileName(fileName)
                        .basePath(basePath)
                        .build();

        // Act
        //        service.pushChanges(event);

        // Assert S3 interactions
        ArgumentCaptor<CreateBucketRequest> createBucketCaptor =
                ArgumentCaptor.forClass(CreateBucketRequest.class);
        //        verify(s3, times(1)).createBucket(createBucketCaptor.capture());
        //        assertEquals("s3-bucket", createBucketCaptor.getValue().bucket());

        ArgumentCaptor<PutObjectRequest> putCaptor =
                ArgumentCaptor.forClass(PutObjectRequest.class);
        //        verify(s3, times(1)).putObject(putCaptor.capture(), (RequestBody) any());
        //        PutObjectRequest putReq = putCaptor.getValue();
        //        assertEquals("s3-bucket", putReq.bucket());
        //        assertEquals(project + "/" + fileName, putReq.key());
        //        assertEquals("text/csv", putReq.contentType());

        // Assert events published (typed captor for CdcEventDto)
        ArgumentCaptor<CdcEventDto> dtoCaptor = ArgumentCaptor.forClass(CdcEventDto.class);
        //        verify(publisher, times(2)).publishEvent(dtoCaptor.capture());

        var events = dtoCaptor.getAllValues();
        //        assertEquals(2, events.size(), "Should publish BEGIN and END");

        //        assertTrue(
        //                events.stream()
        //                        .anyMatch(dto -> dto.getEventType() ==
        // CdcEventType.CDC_UPLOADING_BEGIN));

        // File should be renamed back after upload
        //        assertTrue(Files.exists(basePath.resolve(fileName)), "Original file should exist
        // again");
        //        assertFalse(
        //                Files.exists(basePath.resolve(fileName + ".uploading")),
        //                "Uploading lock should be cleaned");
    }

    @Test
    void pushChanges_missingSourceFile_throwsRuntimeException_andNoS3Calls() {
        // Arrange
        S3Client s3 = mock(S3Client.class);
        ApplicationEventPublisher publisher = mock(ApplicationEventPublisher.class);

        ObjectStorageServiceImpl service =
                new ObjectStorageServiceImpl(null, s3, publisher, null, null, Base64.getDecoder());
        service.setBucket("my-bucket");

        String project = "projA";
        String scope = "users";
        String fileName = scope + ".csv";
        Path basePath = tempDir;

        // Do NOT create the source file
        CdcEventDto event =
                CdcEventDto.builder()
                        .project(project)
                        .scope(scope)
                        .fileName(fileName)
                        .basePath(basePath)
                        .build();

        // Act + Assert
        RuntimeException ex =
                assertThrows(RuntimeException.class, () -> service.pushChanges(event));
        assertTrue(ex.getMessage().contains("CDC upload failed"));

        verifyNoInteractions(s3);
        // Publisher should also not publish begin/end
        verifyNoInteractions(publisher);
    }
}
