package com.rentify.core.unit;

import com.cloudinary.Cloudinary;
import com.cloudinary.Uploader;
import com.rentify.core.service.impl.CloudinaryServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.util.Map;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CloudinaryServiceImplTest {

    @Mock private Cloudinary cloudinary;
    @Mock private Uploader uploader;
    @Mock private MultipartFile file;

    private CloudinaryServiceImpl cloudinaryService;

    @BeforeEach
    void setUp() {
        cloudinaryService = new CloudinaryServiceImpl(cloudinary);
    }

    @Nested
    @DisplayName("uploadFile()")
    class UploadFileTests {

        @Test
        void shouldUploadFileAndReturnSecureUrl_whenFileIsValid() throws Exception {
            when(cloudinary.uploader()).thenReturn(uploader);
            when(file.isEmpty()).thenReturn(false);
            when(file.getContentType()).thenReturn("image/jpeg");
            when(file.getSize()).thenReturn(1024L);
            when(file.getBytes()).thenReturn(new byte[]{1, 2, 3});
            when(uploader.upload(any(byte[].class), anyMap())).thenReturn(Map.of(
                    "secure_url", "https://img.example/pic.jpg",
                    "public_id", "rentify/properties/pic"
            ));

            String result = cloudinaryService.uploadFile(file);

            assertThat(result).isEqualTo("https://img.example/pic.jpg");
            verify(uploader).upload(any(byte[].class), anyMap());
        }

        @Test
        void shouldThrowIllegalArgument_whenFileIsNull() {
            assertThatThrownBy(() -> cloudinaryService.uploadFile(null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("File is required");
        }

        @Test
        void shouldThrowIllegalArgument_whenFileTypeIsUnsupported() {
            when(file.isEmpty()).thenReturn(false);
            when(file.getContentType()).thenReturn("application/pdf");

            assertThatThrownBy(() -> cloudinaryService.uploadFile(file))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Unsupported file type");
        }

        @Test
        void shouldThrowIllegalArgument_whenFileTooLarge() {
            when(file.isEmpty()).thenReturn(false);
            when(file.getContentType()).thenReturn("image/png");
            when(file.getSize()).thenReturn(11L * 1024 * 1024);

            assertThatThrownBy(() -> cloudinaryService.uploadFile(file))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("File size exceeds 10 MB");
        }

        @Test
        void shouldWrapException_whenCloudinaryUploadFails() throws Exception {
            when(cloudinary.uploader()).thenReturn(uploader);
            when(file.isEmpty()).thenReturn(false);
            when(file.getContentType()).thenReturn("image/webp");
            when(file.getSize()).thenReturn(1024L);
            when(file.getBytes()).thenThrow(new IOException("read error"));

            assertThatThrownBy(() -> cloudinaryService.uploadFile(file))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("Image upload failed: read error");
        }
    }

    @Nested
    @DisplayName("deleteFile()")
    class DeleteFileTests {

        @Test
        void shouldDoNothing_whenImageUrlBlank() {
            cloudinaryService.deleteFile(" ");

            verifyNoInteractions(uploader);
        }

        @Test
        void shouldDeleteByExtractedPublicId_whenUrlIsValid() throws Exception {
            when(cloudinary.uploader()).thenReturn(uploader);
            String url = "https://res.cloudinary.com/demo/image/upload/v123/rentify/properties/pic.jpg";

            cloudinaryService.deleteFile(url);

            verify(uploader).destroy("rentify/properties/pic", Map.of("resource_type", "image"));
        }

        @Test
        void shouldWrapException_whenUrlIsInvalid() {
            assertThatThrownBy(() -> cloudinaryService.deleteFile("https://example.com/no-upload-segment.jpg"))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("Image deletion failed: Invalid Cloudinary URL");
        }
    }
}
