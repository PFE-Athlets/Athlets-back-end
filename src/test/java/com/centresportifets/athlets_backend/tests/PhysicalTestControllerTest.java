package com.centresportifets.athlets_backend.tests;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.centresportifets.athlets_backend.auth.AuthService;
import com.centresportifets.athlets_backend.tests.dto.BatteryCreateRequest;
import com.centresportifets.athlets_backend.tests.dto.BatteryDTO;
import com.centresportifets.athlets_backend.tests.dto.BatteryModRequest;
import com.centresportifets.athlets_backend.tests.dto.EquipmentDTO;
import com.centresportifets.athlets_backend.tests.dto.PhysicalTestCreateRequest;
import com.centresportifets.athlets_backend.tests.dto.PhysicalTestResponseDTO;
import com.centresportifets.athlets_backend.tests.dto.ResultTypeDTO;
import com.centresportifets.athlets_backend.tests.equipment.Equipment;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import tools.jackson.databind.ObjectMapper;

import java.util.List;

@WebMvcTest(PhysicalTestController.class)
class PhysicalTestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private PhysicalTestService physicalTestService;

    @MockitoBean(name = "authService")
    private AuthService authService;

    @TestConfiguration
    @EnableMethodSecurity
    static class TestSecurityConfig {
        @Bean
        SecurityFilterChain testSecurityFilterChain(HttpSecurity http) throws Exception {
            http
                .authorizeHttpRequests(auth -> auth.anyRequest().authenticated());
            return http.build();
        }
    }

    // =========================================================================
    // 1. CRÉATION ET CONSULTATION DE TESTS PHYSIQUES (SÉCURITÉ ET PERMISSIONS)
    // =========================================================================

    @Nested
    @DisplayName("Tests Physiques - Rôles & Permissions")
    class PhysicalTestEndpoints {

        private PhysicalTestCreateRequest createDummyRequest() {
            EquipmentDTO equipmentReq = new EquipmentDTO(1, 2); 
            ResultTypeDTO resultTypeReq = new ResultTypeDTO("Temps 30m", 5);

            return new PhysicalTestCreateRequest(
                    "Sprint 30m",
                    1,
                    "Protocole de course...",
                    "Consignes terrain",
                    true,
                    true,
                    List.of(equipmentReq),
                    List.of(resultTypeReq)
            );
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("ADMIN : Création de test (HTTP 201 CREATED)")
        void admin_ShouldCreateTest() throws Exception {
            when(authService.hasPermission(any(), eq("ADMIN"))).thenReturn(true);
            doNothing().when(physicalTestService).createPhysicalTest(any());

            mockMvc.perform(post("/api/physicalTest/create")
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(createDummyRequest())))
                    .andExpect(status().isCreated());

            verify(physicalTestService).createPhysicalTest(any());
        }

        @Test
        @WithMockUser(roles = "COACH")
        @DisplayName("COACH : Création de test (HTTP 201 CREATED)")
        void coach_ShouldCreateTest() throws Exception {
            when(authService.hasPermission(any(), eq("COACH"))).thenReturn(true);
            doNothing().when(physicalTestService).createPhysicalTest(any());

            mockMvc.perform(post("/api/physicalTest/create")
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(createDummyRequest())))
                    .andExpect(status().isCreated());

            verify(physicalTestService).createPhysicalTest(any());
        }

        @Test
        @WithMockUser(roles = "ATHLETE")
        @DisplayName("ATHLETE : Création de test refusée par le service (HTTP 403 Forbidden)")
        void athlete_ShouldBeForbiddenToCreateTest() throws Exception {
            when(authService.hasPermission(any(), any())).thenReturn(false);
            doThrow(new AccessDeniedException("Access Denied")).when(physicalTestService).createPhysicalTest(any());

            mockMvc.perform(post("/api/physicalTest/create")
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(createDummyRequest())))
                    .andExpect(status().isForbidden());
        }

        @Nested
@DisplayName("GET /api/physicalTest - Consultation des tests physiques selon le rôle")
class GetPhysicalTestsByRole {

    @Test
    @WithMockUser(username = "athleteUser", roles = "ATHLETE")
    @DisplayName("ATHLÈTE : Devrait recevoir uniquement les tests associés à ses équipes (HTTP 200)")
    void athlete_ShouldGetOnlyTheirAssignedTests() throws Exception {
        PhysicalTestResponseDTO testAthlete = new PhysicalTestResponseDTO(
                1L, "Sprint 30m Athlète", "Protocole", true, "Info", true, null, List.of(), List.of()
        );

        // On vérifie que le service est appelé avec l'Authentication et retourne la liste spécifique
        when(physicalTestService.getPhysicalTests(any())).thenReturn(List.of(testAthlete));

        mockMvc.perform(get("/api/physicalTest")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].name").value("Sprint 30m Athlète"));

        verify(physicalTestService).getPhysicalTests(any());
    }

    @Test
    @WithMockUser(username = "coachUser", roles = "COACH")
    @DisplayName("COACH : Devrait recevoir les tests associés aux équipes qu'il entraîne (HTTP 200)")
    void coach_ShouldGetTestsForTheirTeams() throws Exception {
        PhysicalTestResponseDTO testCoach = new PhysicalTestResponseDTO(
                2L, "Test VMA Équipe Coach", "Protocole", true, "Info", true, null, List.of(), List.of()
        );

        when(physicalTestService.getPhysicalTests(any())).thenReturn(List.of(testCoach));

        mockMvc.perform(get("/api/physicalTest")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].name").value("Test VMA Équipe Coach"));

        verify(physicalTestService).getPhysicalTests(any());
    }
}

        // @Test
        // @WithMockUser(roles = "ATHLETE")
        // @DisplayName("ATHLÈTE : Consultation globale refusée par le service (HTTP 403 Forbidden)")
        // void athlete_ShouldBeForbiddenToGetAllTests() throws Exception {
        //     when(authService.hasPermission(any(), any())).thenReturn(false);
        //     doThrow(new AccessDeniedException("Access Denied")).when(physicalTestService).getPhysicalTests();

        //     mockMvc.perform(get("/api/physicalTest"))
        //             .andExpect(status().isForbidden());
        // }
    }

    // =========================================================================
    // 2. BATTERIES DE TESTS (SÉCURITÉ ET PERMISSIONS)
    // =========================================================================

    @Nested
    @DisplayName("Batterys de Tests - Rôles & Permissions")
    class BatteryEndpoints {

        private BatteryModRequest createModRequest() {
            return new BatteryModRequest(1L, "Nouveau name", List.of(2L), true);
        }

        private BatteryCreateRequest createBatteryRequest() {
            return new BatteryCreateRequest("Battery Pré-Saison", 10, true, List.of(1L, 2L));
        }

        @Test
        @WithMockUser(username = "adminUser", roles = "ADMIN")
        @DisplayName("ADMIN : Création de battery autorisée (HTTP 200 OK)")
        void admin_ShouldCreateBattery() throws Exception {
            doNothing().when(physicalTestService).createBattery(any());

            mockMvc.perform(post("/api/physicalTest/battery/create")
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(createBatteryRequest())))
                    .andExpect(status().isOk());

            verify(physicalTestService).createBattery(any());
        }

        @Test
        @WithMockUser(username = "coachUser", roles = "COACH")
        @DisplayName("COACH : Consultation de la liste des batterys (HTTP 200 OK)")
        void coach_ShouldGetBatterys() throws Exception {
            BatteryDTO dto = new BatteryDTO(1, "Équipe A", "Battery Vitesse", true, List.of());
            when(physicalTestService.getBatterys()).thenReturn(List.of(dto));

            mockMvc.perform(get("/api/physicalTest/battery")
                    .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(1))
                    .andExpect(jsonPath("$[0].id").value(1))
                    .andExpect(jsonPath("$[0].teamName").value("Équipe A"))
                    .andExpect(jsonPath("$[0].name").value("Battery Vitesse"));

            verify(physicalTestService).getBatterys();
        }

        @Test
        @WithMockUser(username = "adminUser", roles = "ADMIN")
        @DisplayName("ADMIN : Modification de battery autorisée (HTTP 200 OK)")
        void admin_ShouldModifyBattery() throws Exception {
            when(authService.hasPermission(any(), eq("ADMIN"))).thenReturn(true);

            mockMvc.perform(post("/api/physicalTest/battery")
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(createModRequest())))
                    .andExpect(status().isOk());

            verify(physicalTestService).modifyBattery(any());
        }

        @Test
        @WithMockUser(username = "coachUser", roles = "COACH")
        @DisplayName("COACH : Modification de battery autorisée (HTTP 200 OK)")
        void coach_ShouldModifyBattery() throws Exception {
            when(authService.hasPermission(any(), eq("COACH"))).thenReturn(true);

            mockMvc.perform(post("/api/physicalTest/battery")
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(createModRequest())))
                    .andExpect(status().isOk());

            verify(physicalTestService).modifyBattery(any());
        }

        @Test
        @WithMockUser(username = "athleteUser", roles = "ATHLETE")
        @DisplayName("ATHLÈTE / Non Autorisé : Modification de battery rejetée par le service (HTTP 403 Forbidden)")
        void unauthorizedUser_ShouldBeForbiddenToModifyBattery() throws Exception {
            when(authService.hasPermission(any(), any())).thenReturn(false);
            doThrow(new AccessDeniedException("Access Denied")).when(physicalTestService).modifyBattery(any());

            mockMvc.perform(post("/api/physicalTest/battery")
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(createModRequest())))
                    .andExpect(status().isForbidden());
        }
    }

    // =========================================================================
    // 3. RÉFÉRENTIELS (UNITS, EQUIPMENTS, QUALITIES)
    // =========================================================================

    @Nested
    @DisplayName("Référentiels - Endpoints")
    class ReferenceEndpoints {

        @Test
        @WithMockUser(roles = "COACH")
        @DisplayName("GET /api/physicalTest/units - Devrait retourner la liste des unités (HTTP 200 OK)")
        void getTestUnits_ShouldReturnUnits() throws Exception {
            UnitMeasure unit = new UnitMeasure();
            unit.setId(1L);
            unit.setName("Secondes");
            unit.setSymbol("s");

            when(physicalTestService.getUnits()).thenReturn(List.of(unit));

            mockMvc.perform(get("/api/physicalTest/units")
                    .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(1))
                    .andExpect(jsonPath("$[0].id").value(1))
                    .andExpect(jsonPath("$[0].name").value("Secondes"))
                    .andExpect(jsonPath("$[0].symbol").value("s"));

            verify(physicalTestService).getUnits();
        }

        @Test
        @WithMockUser(roles = "COACH")
        @DisplayName("GET /api/physicalTest/equipments - Devrait retourner la liste des équipements (HTTP 200 OK)")
        void getTestEquipments_ShouldReturnEquipments() throws Exception {
            Equipment equipment = new Equipment();
            equipment.setId(1L);
            equipment.setName("Chronameètre");

            when(physicalTestService.getEquipments()).thenReturn(List.of(equipment));

            mockMvc.perform(get("/api/physicalTest/equipments")
                    .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(1))
                    .andExpect(jsonPath("$[0].id").value(1))
                    .andExpect(jsonPath("$[0].name").value("Chronameètre"));

            verify(physicalTestService).getEquipments();
        }

        @Test
        @WithMockUser(roles = "COACH")
        @DisplayName("GET /api/physicalTest/qualities - Devrait retourner les qualités physiques (HTTP 200 OK)")
        void getTestQualities_ShouldReturnQualities() throws Exception {
            PhysicalQuality quality = new PhysicalQuality();
            quality.setId(1);
            quality.setName("Endurance");
            quality.setDescription("Capacité d'effort prolongé");

            when(physicalTestService.getPhysicalQualities()).thenReturn(List.of(quality));

            mockMvc.perform(get("/api/physicalTest/qualities")
                    .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(1))
                    .andExpect(jsonPath("$[0].id").value(1))
                    .andExpect(jsonPath("$[0].name").value("Endurance"))
                    .andExpect(jsonPath("$[0].description").value("Capacité d'effort prolongé"));

            verify(physicalTestService).getPhysicalQualities();
        }
    }
}