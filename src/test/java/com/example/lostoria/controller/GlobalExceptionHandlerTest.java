package com.example.lostoria.controller;

import com.example.lostoria.exception.GlobalExceptionHandler;
import com.example.lostoria.model.LostItem;
import com.example.lostoria.service.FoundItemService;
import com.example.lostoria.service.LostItemService;
import com.example.lostoria.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.server.ResponseStatusException;

import java.util.NoSuchElementException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class GlobalExceptionHandlerTest {

    @RestController
    @RequestMapping("/test/errors")
    static class TestErrorController {

        @GetMapping("/not-found")
        public void throwNotFound() {
            throw new NoSuchElementException("Test resource not found");
        }

        @GetMapping("/forbidden")
        public void throwForbidden() {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Custom forbidden message");
        }

        @GetMapping("/server-error")
        public void throwServerError() {
            throw new RuntimeException("Sensitive internal database connection string failed");
        }

        @PostMapping("/upload-limit")
        public void throwMaxUploadSize() {
            throw new MaxUploadSizeExceededException(5242880L);
        }

        @GetMapping("/access-denied")
        public void throwAccessDenied() {
            throw new org.springframework.security.access.AccessDeniedException("Access denied custom");
        }

        @GetMapping("/conflict")
        public void throwConflict() {
            throw new org.springframework.dao.DataIntegrityViolationException("Duplicate key");
        }

        @GetMapping("/illegal-argument")
        public void throwIllegalArgument() {
            throw new IllegalArgumentException("Invalid argument provided for search");
        }

        @PostMapping("/service-save-violation")
        public void simulateServiceLayerSaveViolation() {
            jakarta.validation.Validator validator = jakarta.validation.Validation.buildDefaultValidatorFactory().getValidator();
            LostItem invalidItem = new LostItem();
            invalidItem.setTitle(null); // triggers @NotBlank
            invalidItem.setLocationLost(null); // triggers @NotBlank
            java.util.Set<jakarta.validation.ConstraintViolation<LostItem>> violations = validator.validate(invalidItem);
            throw new jakarta.validation.ConstraintViolationException(violations);
        }
    }

    private MockMvc mockMvc;

    @Mock
    private LostItemService lostItemService;
    @Mock
    private FoundItemService foundItemService;
    @Mock
    private com.example.lostoria.service.UserService userService;

    @InjectMocks
    private LostItemController lostItemController;
    @InjectMocks
    private FoundItemController foundItemController;
    @InjectMocks
    private AuthController authController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(lostItemController, foundItemController, authController, new TestErrorController())
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void testNoSuchElementException_returns404WithEnvelope() throws Exception {
        mockMvc.perform(get("/test/errors/not-found"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("Test resource not found"))
                .andExpect(jsonPath("$.path").value("/test/errors/not-found"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void testResponseStatusException_returnsCustomStatusWithEnvelope() throws Exception {
        mockMvc.perform(get("/test/errors/forbidden"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.error").value("Forbidden"))
                .andExpect(jsonPath("$.message").value("Custom forbidden message"))
                .andExpect(jsonPath("$.path").value("/test/errors/forbidden"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void testMaxUploadSizeExceeded_returns400WithEnvelope() throws Exception {
        mockMvc.perform(post("/test/errors/upload-limit"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value("File upload exceeds maximum allowed size limit of 5MB"))
                .andExpect(jsonPath("$.path").value("/test/errors/upload-limit"));
    }

    @Test
    void testGenericException_returns500WithoutLeakingDetails() throws Exception {
        mockMvc.perform(get("/test/errors/server-error"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.error").value("Internal Server Error"))
                .andExpect(jsonPath("$.message").value("An unexpected internal error occurred. Please try again later."))
                .andExpect(jsonPath("$.path").value("/test/errors/server-error"))
                .andExpect(jsonPath("$.stackTrace").doesNotExist());
    }

    @Test
    void testMethodArgumentTypeMismatch_returns400WithEnvelope() throws Exception {
        mockMvc.perform(get("/api/lost-items/invalid-number-id"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.path").value("/api/lost-items/invalid-number-id"));
    }

    @Test
    void testMalformedJson_returns400WithEnvelope() throws Exception {
        mockMvc.perform(put("/api/lost-items/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{ malformed json body"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value("Malformed or unreadable JSON request body"))
                .andExpect(jsonPath("$.path").value("/api/lost-items/1"));
    }

    @Test
    void testAccessDenied_returns403WithEnvelope() throws Exception {
        mockMvc.perform(get("/test/errors/access-denied"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.error").value("Forbidden"))
                .andExpect(jsonPath("$.message").value("Access denied: You do not have permission to perform this action"))
                .andExpect(jsonPath("$.path").value("/test/errors/access-denied"));
    }

    @Test
    void testDataIntegrityViolation_returns409WithEnvelope() throws Exception {
        mockMvc.perform(get("/test/errors/conflict"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.error").value("Conflict"))
                .andExpect(jsonPath("$.message").value("Database constraint violation or duplicate record found"))
                .andExpect(jsonPath("$.path").value("/test/errors/conflict"));
    }

    @Test
    void testIllegalArgument_returns400WithEnvelope() throws Exception {
        mockMvc.perform(get("/test/errors/illegal-argument"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value("Invalid argument provided for search"))
                .andExpect(jsonPath("$.path").value("/test/errors/illegal-argument"));
    }

    @Test
    void testRegister_blankUserFields_returns400WithFieldErrors() throws Exception {
        String invalidUserJson = """
                {
                    "username": "",
                    "email": "not-an-email",
                    "password": "123"
                }
                """;

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidUserJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.path").value("/api/auth/register"))
                .andExpect(jsonPath("$.errors.username").exists())
                .andExpect(jsonPath("$.errors.email").exists())
                .andExpect(jsonPath("$.errors.password").exists());
    }

    @Test
    void testLostItemMultipartCreate_blankTitleLocation_returns400WithFieldErrors() throws Exception {
        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart("/api/lost-items")
                        .param("title", "")
                        .param("locationLost", "")
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.path").value("/api/lost-items"))
                .andExpect(jsonPath("$.errors.title").exists())
                .andExpect(jsonPath("$.errors.locationLost").exists());
    }

    @Test
    void testLostItemJsonUpdate_blankTitle_returns400WithFieldErrors() throws Exception {
        String invalidLostItemJson = """
                {
                    "title": "",
                    "locationLost": "Library"
                }
                """;

        mockMvc.perform(put("/api/lost-items/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidLostItemJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.path").value("/api/lost-items/1"))
                .andExpect(jsonPath("$.errors.title").exists());
    }

    @Test
    void testFoundItemJsonCreate_blankLocationFound_returns400WithFieldErrors() throws Exception {
        String invalidFoundItemJson = """
                {
                    "title": "Keys",
                    "locationFound": ""
                }
                """;

        mockMvc.perform(post("/api/found-items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidFoundItemJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.path").value("/api/found-items"))
                .andExpect(jsonPath("$.errors.locationFound").exists());
    }

    @Test
    void testConstraintViolationException_directServiceSave_returns400WithEnvelope() throws Exception {
        mockMvc.perform(post("/test/errors/service-save-violation"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.path").value("/test/errors/service-save-violation"))
                .andExpect(jsonPath("$.errors.title").value("Title is required"))
                .andExpect(jsonPath("$.errors.locationLost").value("Location lost is required"))
                .andExpect(jsonPath("$.timestamp").exists());
    }
}
