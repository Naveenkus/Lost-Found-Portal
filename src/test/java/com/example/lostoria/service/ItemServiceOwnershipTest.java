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

    // ==========================================
    // LostItem PUT/DELETE Authorization Tests
    // ==========================================

    @Test
    void updateLostItem_asOwner_success() {
        LostItem existing = new LostItem();
        existing.setId(10L);
        existing.setTitle("Old Title");
        existing.setReportedBy(mockUser);

        when(lostItemRepository.findById(10L)).thenReturn(java.util.Optional.of(existing));
        when(lostItemRepository.save(any(LostItem.class))).thenAnswer(invocation -> invocation.getArgument(0));

        LostItem updateData = new LostItem();
        updateData.setTitle("New Title");

        LostItem updated = lostItemService.update(10L, updateData);
        assertThat(updated.getTitle()).isEqualTo("New Title");
    }

    @Test
    void deleteLostItem_asOwner_success() {
        LostItem existing = new LostItem();
        existing.setId(10L);
        existing.setReportedBy(mockUser);

        when(lostItemRepository.findById(10L)).thenReturn(java.util.Optional.of(existing));

        lostItemService.delete(10L);
        org.mockito.Mockito.verify(lostItemRepository).deleteById(10L);
    }

    @Test
    void updateLostItem_asNonOwnerNonAdmin_throws403() {
        User anotherUser = new User();
        anotherUser.setId(99L);
        anotherUser.setUsername("other");

        LostItem existing = new LostItem();
        existing.setId(10L);
        existing.setReportedBy(anotherUser);

        when(lostItemRepository.findById(10L)).thenReturn(java.util.Optional.of(existing));

        org.assertj.core.api.Assertions.assertThatThrownBy(() -> lostItemService.update(10L, new LostItem()))
                .isInstanceOf(org.springframework.web.server.ResponseStatusException.class)
                .hasFieldOrPropertyWithValue("statusCode", org.springframework.http.HttpStatus.FORBIDDEN);
    }

    @Test
    void deleteLostItem_asNonOwnerNonAdmin_throws403() {
        User anotherUser = new User();
        anotherUser.setId(99L);

        LostItem existing = new LostItem();
        existing.setId(10L);
        existing.setReportedBy(anotherUser);

        when(lostItemRepository.findById(10L)).thenReturn(java.util.Optional.of(existing));

        org.assertj.core.api.Assertions.assertThatThrownBy(() -> lostItemService.delete(10L))
                .isInstanceOf(org.springframework.web.server.ResponseStatusException.class)
                .hasFieldOrPropertyWithValue("statusCode", org.springframework.http.HttpStatus.FORBIDDEN);
    }

    @Test
    void updateLostItem_asAdmin_canUpdateAnyItemIncludingLegacyNullReportedBy() {
        User adminUser = new User();
        adminUser.setId(2L);
        adminUser.setUsername("admin");
        adminUser.setRole(Role.ADMIN);

        UserPrincipal adminPrincipal = new UserPrincipal(adminUser);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(adminPrincipal, null, adminPrincipal.getAuthorities())
        );

        LostItem legacyItem = new LostItem();
        legacyItem.setId(10L);
        legacyItem.setReportedBy(null); // legacy item

        when(lostItemRepository.findById(10L)).thenReturn(java.util.Optional.of(legacyItem));
        when(lostItemRepository.save(any(LostItem.class))).thenAnswer(invocation -> invocation.getArgument(0));

        LostItem updateData = new LostItem();
        updateData.setTitle("Admin Updated Title");

        LostItem updated = lostItemService.update(10L, updateData);
        assertThat(updated.getTitle()).isEqualTo("Admin Updated Title");
    }

    @Test
    void deleteLostItem_asAdmin_canDeleteLegacyNullReportedBy() {
        User adminUser = new User();
        adminUser.setId(2L);
        adminUser.setRole(Role.ADMIN);

        UserPrincipal adminPrincipal = new UserPrincipal(adminUser);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(adminPrincipal, null, adminPrincipal.getAuthorities())
        );

        LostItem legacyItem = new LostItem();
        legacyItem.setId(10L);
        legacyItem.setReportedBy(null);

        when(lostItemRepository.findById(10L)).thenReturn(java.util.Optional.of(legacyItem));

        lostItemService.delete(10L);
        org.mockito.Mockito.verify(lostItemRepository).deleteById(10L);
    }

    @Test
    void updateLostItem_asNonAdmin_onLegacyNullReportedBy_throws403() {
        LostItem legacyItem = new LostItem();
        legacyItem.setId(10L);
        legacyItem.setReportedBy(null); // legacy item

        when(lostItemRepository.findById(10L)).thenReturn(java.util.Optional.of(legacyItem));

        org.assertj.core.api.Assertions.assertThatThrownBy(() -> lostItemService.update(10L, new LostItem()))
                .isInstanceOf(org.springframework.web.server.ResponseStatusException.class)
                .hasFieldOrPropertyWithValue("statusCode", org.springframework.http.HttpStatus.FORBIDDEN);
    }

    @Test
    void deleteLostItem_asNonAdmin_onLegacyNullReportedBy_throws403() {
        LostItem legacyItem = new LostItem();
        legacyItem.setId(10L);
        legacyItem.setReportedBy(null);

        when(lostItemRepository.findById(10L)).thenReturn(java.util.Optional.of(legacyItem));

        org.assertj.core.api.Assertions.assertThatThrownBy(() -> lostItemService.delete(10L))
                .isInstanceOf(org.springframework.web.server.ResponseStatusException.class)
                .hasFieldOrPropertyWithValue("statusCode", org.springframework.http.HttpStatus.FORBIDDEN);
    }

    @Test
    void updateLostItem_notFound_throws404() {
        when(lostItemRepository.findById(999L)).thenReturn(java.util.Optional.empty());

        org.assertj.core.api.Assertions.assertThatThrownBy(() -> lostItemService.update(999L, new LostItem()))
                .isInstanceOf(org.springframework.web.server.ResponseStatusException.class)
                .hasFieldOrPropertyWithValue("statusCode", org.springframework.http.HttpStatus.NOT_FOUND);
    }

    // ==========================================
    // FoundItem PUT/DELETE Authorization Tests
    // ==========================================

    @Test
    void updateFoundItem_asOwner_success() {
        FoundItem existing = new FoundItem();
        existing.setId(20L);
        existing.setTitle("Old Title");
        existing.setReportedBy(mockUser);

        when(foundItemRepository.findById(20L)).thenReturn(java.util.Optional.of(existing));
        when(foundItemRepository.save(any(FoundItem.class))).thenAnswer(invocation -> invocation.getArgument(0));

        FoundItem updateData = new FoundItem();
        updateData.setTitle("New Found Title");

        FoundItem updated = foundItemService.update(20L, updateData);
        assertThat(updated.getTitle()).isEqualTo("New Found Title");
    }

    @Test
    void deleteFoundItem_asOwner_success() {
        FoundItem existing = new FoundItem();
        existing.setId(20L);
        existing.setReportedBy(mockUser);

        when(foundItemRepository.findById(20L)).thenReturn(java.util.Optional.of(existing));

        foundItemService.delete(20L);
        org.mockito.Mockito.verify(foundItemRepository).deleteById(20L);
    }

    @Test
    void updateFoundItem_asNonOwnerNonAdmin_throws403() {
        User anotherUser = new User();
        anotherUser.setId(99L);

        FoundItem existing = new FoundItem();
        existing.setId(20L);
        existing.setReportedBy(anotherUser);

        when(foundItemRepository.findById(20L)).thenReturn(java.util.Optional.of(existing));

        org.assertj.core.api.Assertions.assertThatThrownBy(() -> foundItemService.update(20L, new FoundItem()))
                .isInstanceOf(org.springframework.web.server.ResponseStatusException.class)
                .hasFieldOrPropertyWithValue("statusCode", org.springframework.http.HttpStatus.FORBIDDEN);
    }

    @Test
    void deleteFoundItem_asNonOwnerNonAdmin_throws403() {
        User anotherUser = new User();
        anotherUser.setId(99L);

        FoundItem existing = new FoundItem();
        existing.setId(20L);
        existing.setReportedBy(anotherUser);

        when(foundItemRepository.findById(20L)).thenReturn(java.util.Optional.of(existing));

        org.assertj.core.api.Assertions.assertThatThrownBy(() -> foundItemService.delete(20L))
                .isInstanceOf(org.springframework.web.server.ResponseStatusException.class)
                .hasFieldOrPropertyWithValue("statusCode", org.springframework.http.HttpStatus.FORBIDDEN);
    }

    @Test
    void updateFoundItem_asAdmin_canUpdateAnyItemIncludingLegacyNullReportedBy() {
        User adminUser = new User();
        adminUser.setId(2L);
        adminUser.setRole(Role.ADMIN);

        UserPrincipal adminPrincipal = new UserPrincipal(adminUser);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(adminPrincipal, null, adminPrincipal.getAuthorities())
        );

        FoundItem legacyItem = new FoundItem();
        legacyItem.setId(20L);
        legacyItem.setReportedBy(null);

        when(foundItemRepository.findById(20L)).thenReturn(java.util.Optional.of(legacyItem));
        when(foundItemRepository.save(any(FoundItem.class))).thenAnswer(invocation -> invocation.getArgument(0));

        FoundItem updateData = new FoundItem();
        updateData.setTitle("Admin Found Title");

        FoundItem updated = foundItemService.update(20L, updateData);
        assertThat(updated.getTitle()).isEqualTo("Admin Found Title");
    }

    @Test
    void deleteFoundItem_asAdmin_canDeleteLegacyNullReportedBy() {
        User adminUser = new User();
        adminUser.setId(2L);
        adminUser.setRole(Role.ADMIN);

        UserPrincipal adminPrincipal = new UserPrincipal(adminUser);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(adminPrincipal, null, adminPrincipal.getAuthorities())
        );

        FoundItem legacyItem = new FoundItem();
        legacyItem.setId(20L);
        legacyItem.setReportedBy(null);

        when(foundItemRepository.findById(20L)).thenReturn(java.util.Optional.of(legacyItem));

        foundItemService.delete(20L);
        org.mockito.Mockito.verify(foundItemRepository).deleteById(20L);
    }

    @Test
    void updateFoundItem_asNonAdmin_onLegacyNullReportedBy_throws403() {
        FoundItem legacyItem = new FoundItem();
        legacyItem.setId(20L);
        legacyItem.setReportedBy(null);

        when(foundItemRepository.findById(20L)).thenReturn(java.util.Optional.of(legacyItem));

        org.assertj.core.api.Assertions.assertThatThrownBy(() -> foundItemService.update(20L, new FoundItem()))
                .isInstanceOf(org.springframework.web.server.ResponseStatusException.class)
                .hasFieldOrPropertyWithValue("statusCode", org.springframework.http.HttpStatus.FORBIDDEN);
    }

    @Test
    void deleteFoundItem_asNonAdmin_onLegacyNullReportedBy_throws403() {
        FoundItem legacyItem = new FoundItem();
        legacyItem.setId(20L);
        legacyItem.setReportedBy(null);

        when(foundItemRepository.findById(20L)).thenReturn(java.util.Optional.of(legacyItem));

        org.assertj.core.api.Assertions.assertThatThrownBy(() -> foundItemService.delete(20L))
                .isInstanceOf(org.springframework.web.server.ResponseStatusException.class)
                .hasFieldOrPropertyWithValue("statusCode", org.springframework.http.HttpStatus.FORBIDDEN);
    }

    @Test
    void updateFoundItem_notFound_throws404() {
        when(foundItemRepository.findById(999L)).thenReturn(java.util.Optional.empty());

        org.assertj.core.api.Assertions.assertThatThrownBy(() -> foundItemService.update(999L, new FoundItem()))
                .isInstanceOf(org.springframework.web.server.ResponseStatusException.class)
                .hasFieldOrPropertyWithValue("statusCode", org.springframework.http.HttpStatus.NOT_FOUND);
    }
}
