package org.delcom.ifs25026springbootcashflow.config;

import org.delcom.ifs25026springbootcashflow.adapter.storage.FileStorage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.ArgumentCaptor;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AppConfigTest {

    @TempDir
    Path tempDir;

    @Test
    void uploadResourceConfigurerServesUploadsFolderWithTrailingSlash() {
        String existingLocation = captureLocation(new FileStorage(tempDir.toString()));
        assertTrue(existingLocation.endsWith("/"));
        assertEquals(tempDir.toUri().toString(), existingLocation);
    }

    @Test
    void uploadResourceConfigurerAppendsTrailingSlashForMissingFolder() {
        Path missing = tempDir.resolve("belum-ada");
        String location = captureLocation(new FileStorage(missing.toString()));

        assertTrue(location.endsWith("/"));
        assertEquals(missing.toUri() + "/", location);
    }

    private String captureLocation(FileStorage fileStorage) {
        ResourceHandlerRegistration registration = mock(ResourceHandlerRegistration.class);
        ResourceHandlerRegistry registry = mock(ResourceHandlerRegistry.class);
        when(registry.addResourceHandler("/uploads/**")).thenReturn(registration);
        when(registration.addResourceLocations(any(String[].class))).thenReturn(registration);

        WebMvcConfigurer configurer = new AppConfig().uploadResourceConfigurer(fileStorage);
        configurer.addResourceHandlers(registry);

        ArgumentCaptor<String[]> locations = ArgumentCaptor.forClass(String[].class);
        verify(registration).addResourceLocations(locations.capture());
        return locations.getValue()[0];
    }
}
