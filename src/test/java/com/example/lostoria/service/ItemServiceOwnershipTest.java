package com.example.lostoria.service;

import com.example.lostoria.dto.UserPrincipal;
import com.example.lostoria.model.FoundItem;
import com.example.lostoria.model.LostItem;
import com.example.lostoria.model.User;
import com.example.lostoria.repository.FoundItemRepository;
import com.example.lostoria.repository.ImageRepository;
import com.example.lostoria.repository.LostItemRepository;
import com.example.lostoria.util.Role;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ItemServiceOwnershipTest {

    @Mock
    private LostItemRepository lostItemRepository;

    @Mock
    private FoundItemRepository foundItemRepository;

    @Mock
    private ImageRepository imageRepository;

    @InjectMocks
    private LostItemService lostItemService;

    @InjectMocks
    private FoundItemService foundItemService;

    private User mockUser;

    @BeforeEach
    void setUp() {
        mockUser = new User();
        mockUser.setId(1L);
        mockUser.setUsername("testuser");
        mockUser.setEmail("test@example.com");
        mockUser.setRole(Role.USER);

        UserPrincipal principal = new UserPrincipal(mockUser);
        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void createLostItem_populatesReportedByFromSecurityContext() throws IOException {
        LostItem inputItem = new LostItem();
        inputItem.setTitle("Lost Wallet");

        when(lostItemRepository.save(any(LostItem.class))).thenAnswer(invocation -> invocation.getArgument(0));

        LostItem saved = lostItemService.create(inputItem, null);

        assertThat(saved).isNotNull();
        assertThat(saved.getReportedBy()).isNotNull();
        assertThat(saved.getReportedBy().getId()).isEqualTo(1L);
        assertThat(saved.getReportedBy().getUsername()).isEqualTo("testuser");
    }

    @Test
    void createFoundItemWithImage_populatesReportedByFromSecurityContext() throws IOException {
        FoundItem inputItem = new FoundItem();
        inputItem.setTitle("Found Keys");

        when(foundItemRepository.save(any(FoundItem.class))).thenAnswer(invocation -> invocation.getArgument(0));

        FoundItem saved = foundItemService.create(inputItem, null);

        assertThat(saved).isNotNull();
        assertThat(saved.getReportedBy()).isNotNull();
        assertThat(saved.getReportedBy().getId()).isEqualTo(1L);
        assertThat(saved.getReportedBy().getUsername()).isEqualTo("testuser");
    }

    @Test
    void createFoundItemJson_populatesReportedByFromSecurityContext() {
        FoundItem inputItem = new FoundItem();
        inputItem.setTitle("Found Phone");

        when(foundItemRepository.save(any(FoundItem.class))).thenAnswer(invocation -> invocation.getArgument(0));

        FoundItem saved = foundItemService.create(inputItem);

        assertThat(saved).isNotNull();
        assertThat(saved.getReportedBy()).isNotNull();
        assertThat(saved.getReportedBy().getId()).isEqualTo(1L);
        assertThat(saved.getReportedBy().getUsername()).isEqualTo("testuser");
    }
}
