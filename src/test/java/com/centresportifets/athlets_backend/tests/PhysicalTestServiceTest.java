package com.centresportifets.athlets_backend.tests;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;

import com.centresportifets.athlets_backend.auth.AuthService;
import com.centresportifets.athlets_backend.team.AthleteTeam;
import com.centresportifets.athlets_backend.team.AthleteTeamId;
import com.centresportifets.athlets_backend.team.Team;
import com.centresportifets.athlets_backend.team.TeamRepository;
import com.centresportifets.athlets_backend.tests.battery.Battery;
import com.centresportifets.athlets_backend.tests.battery.BatteryRepository;
import com.centresportifets.athlets_backend.tests.dto.BatteryModRequest;
import com.centresportifets.athlets_backend.tests.dto.EquipmentDTO;
import com.centresportifets.athlets_backend.tests.dto.PhysicalTestCreateRequest;
import com.centresportifets.athlets_backend.tests.dto.PhysicalTestResponseDTO;
import com.centresportifets.athlets_backend.tests.dto.ResultTypeDTO;
import com.centresportifets.athlets_backend.tests.equipment.Equipment;
import com.centresportifets.athlets_backend.tests.equipment.EquipmentRepository;
import com.centresportifets.athlets_backend.tests.equipment.TestEquipmentRepository;
import com.centresportifets.athlets_backend.user.UserType;
import com.centresportifets.athlets_backend.user.athlete.Athlete;
import com.centresportifets.athlets_backend.user.athlete.AthleteRepository;
import com.centresportifets.athlets_backend.user.coach.Coach;
import com.centresportifets.athlets_backend.user.coach.CoachRepository;
import com.centresportifets.athlets_backend.result.ResultService;

@ExtendWith(MockitoExtension.class)
class PhysicalTestServiceTest {

    @Mock
    private PhysicalTestRepository physicalTestRepository;

    @Mock
    private PhysicalQualityRepository physicalQualityRepository;

    @Mock
    private EquipmentRepository equipmentRepository;

    @Mock
    private TestEquipmentRepository testEquipmentRepository;

    @Mock
    private UnitMeasureRepository unitMeasureRepository;

    @Mock
    private ResultTypeRepository resultTypeRepository;

    @Mock
    private BatteryRepository batteryRepository;

    @Mock
    private AuthService authService;

    @Mock
    private CoachRepository coachRepository;

    @Mock
    private AthleteRepository athleteRepository;

    @Mock
    private TeamRepository teamRepository;

    @Mock
    private Authentication authentication;

    private PhysicalTestService physicalTestService;

    @Mock
    private ResultService resultService;

    @BeforeEach
    void setUp() {
        // Constructeur complet incluant authService, coachRepository et athleteRepository
        physicalTestService = new PhysicalTestService(
                physicalTestRepository,
                physicalQualityRepository,
                equipmentRepository,
                testEquipmentRepository,
                unitMeasureRepository,
                resultTypeRepository,
                batteryRepository,
                authService,
                coachRepository,
                athleteRepository,
                teamRepository,
                resultService
        );
    }

    // =========================================================================
    // 1. FILTRAGE ET CONSULTATION DE TESTS PAR RÔLE
    // =========================================================================

    @Nested
    @DisplayName("getPhysicalTests(Authentication) - Filtrage par rôle et équipe")
    class GetPhysicalTestsByRoleLogic {

        @Test
        @DisplayName("ADMIN : Devrait retourner tous les tests disponibles")
        void admin_ShouldReturnAllTests() {
            PhysicalTest test1 = new PhysicalTest();
            test1.setId(1L);
            test1.setName("Test VMA");

            when(authService.getAuthenticatedUserType(authentication)).thenReturn(UserType.ADMIN);
            when(physicalTestRepository.findAll()).thenReturn(List.of(test1));

            List<PhysicalTestResponseDTO> result = physicalTestService.getPhysicalTests(authentication);

            assertThat(result).hasSize(1);
            assertThat(result.get(0).name()).isEqualTo("Test VMA");
            verify(physicalTestRepository, times(1)).findAll();
        }

        @Test
        @DisplayName("COACH : Devrait retourner les tests des batterys gérées par son équipe")
        void coach_ShouldReturnTeamBatteryTests() {
            String username = "coachUser";
            Long teamId = 10L;

            Team team = new Team();
            team.setId(teamId);

            Coach coach = new Coach();
            coach.setTeam(team);

            PhysicalTest teamTest = new PhysicalTest();
            teamTest.setId(2L);
            teamTest.setName("Sprint 30m");

            when(authentication.getName()).thenReturn(username);
            when(authService.getAuthenticatedUserType(authentication)).thenReturn(UserType.COACH);
            when(coachRepository.findByUsername(username)).thenReturn(Optional.of(coach));
            when(physicalTestRepository.findAllByBatterysTeamId(teamId)).thenReturn(List.of(teamTest));

            List<PhysicalTestResponseDTO> result = physicalTestService.getPhysicalTests(authentication);

            assertThat(result).hasSize(1);
            assertThat(result.get(0).name()).isEqualTo("Sprint 30m");
            verify(physicalTestRepository, times(1)).findAllByBatterysTeamId(teamId);
        }

        @Test
        @DisplayName("ATHLETE : Devrait retourner les tests des batterys de ses équipes")
        void athlete_ShouldReturnTheirTeamsBatteryTests() {
            String username = "athleteUser";
            Long teamId = 10L;

            AthleteTeam athleteTeam = new AthleteTeam();
            AthleteTeamId athleteTeamId = new AthleteTeamId();
            athleteTeamId.setTeamId(teamId);
            athleteTeam.setId(athleteTeamId);

            Athlete athlete = new Athlete();
            athlete.setAthleteTeams(List.of(athleteTeam));

            PhysicalTest athleteTest = new PhysicalTest();
            athleteTest.setId(3L);
            athleteTest.setName("Saut Vertical");

            when(authentication.getName()).thenReturn(username);
            when(authService.getAuthenticatedUserType(authentication)).thenReturn(UserType.ATHLETE);
            when(athleteRepository.findByUsername(username)).thenReturn(Optional.of(athlete));
            when(physicalTestRepository.findAllByBatterysTeamIdIn(List.of(teamId))).thenReturn(List.of(athleteTest));

            List<PhysicalTestResponseDTO> result = physicalTestService.getPhysicalTests(authentication);

            assertThat(result).hasSize(1);
            assertThat(result.get(0).name()).isEqualTo("Saut Vertical");
            verify(physicalTestRepository, times(1)).findAllByBatterysTeamIdIn(List.of(teamId));
        }
    }

    // =========================================================================
    // 2. CRÉATION ET MODIFICATION DE TESTS PHYSIQUES
    // =========================================================================

    @Nested
    @DisplayName("Tests des méthodes métier - PhysicalTest")
    class PhysicalTestServiceLogic {

        @Test
        @DisplayName("createPhysicalTest() - Devrait correctement transformer et sauvegarder la requête")
        void createPhysicalTest_ShouldSaveTest() {
            EquipmentDTO eqDto = new EquipmentDTO(1, 2);
            ResultTypeDTO resDto = new ResultTypeDTO("Temps 30m", 5);

            PhysicalTestCreateRequest request = new PhysicalTestCreateRequest(
                    "Sprint 30m",
                    1,
                    "Protocole de course...",
                    "Consignes terrain",
                    true,
                    true,
                    List.of(eqDto),
                    List.of(resDto)
            );

            PhysicalQuality quality = new PhysicalQuality();
            quality.setId(1);
            quality.setName("Endurance");

            Equipment equipment = new Equipment();
            equipment.setId(1L);
            equipment.setName("Chronameètre");

            UnitMeasure unit = new UnitMeasure();
            unit.setId(5L);
            unit.setName("Secondes");

            when(physicalQualityRepository.findById(1)).thenReturn(Optional.of(quality));
            when(equipmentRepository.findById(1)).thenReturn(Optional.of(equipment));
            when(unitMeasureRepository.findById(5L)).thenReturn(Optional.of(unit));

            // STUB REQUIS : évite la NullPointerException lors de savedTest.getId()
            when(physicalTestRepository.save(any(PhysicalTest.class))).thenAnswer(invocation -> {
                PhysicalTest testToSave = invocation.getArgument(0);
                testToSave.setId(1L);
                return testToSave;
            });

            physicalTestService.createPhysicalTest(request);

            ArgumentCaptor<PhysicalTest> testCaptor = ArgumentCaptor.forClass(PhysicalTest.class);
            verify(physicalTestRepository, times(1)).save(testCaptor.capture());

            PhysicalTest savedTest = testCaptor.getValue();
            assertThat(savedTest.getName()).isEqualTo("Sprint 30m");
            assertThat(savedTest.getProtocol()).isEqualTo("Protocole de course...");
            assertThat(savedTest.getInformations()).isEqualTo("Consignes terrain");
            assertThat(savedTest.isSupervised()).isTrue();
            assertThat(savedTest.isProofRequired()).isTrue();
            assertThat(savedTest.getPhysicalQuality()).isEqualTo(quality);
        }
    }

    // =========================================================================
    // 3. BATTERIES DE TESTS
    // =========================================================================

    @Nested
    @DisplayName("Tests des méthodes métier - Battery")
    class BatteryServiceLogic {

        @Test
        @DisplayName("modifyBattery() - Devrait ajouter de nouveaux tests sans écraser les existants")
        void modifyBattery_ShouldAddTestsWithoutRemovingExisting() {
            Battery existingBattery = new Battery();
            existingBattery.setId(1L);
            existingBattery.setName("Vieille Battery");
            existingBattery.setStatus(true);

            PhysicalTest existingTest = new PhysicalTest();
            existingTest.setId(10L);
            existingBattery.setTests(new ArrayList<>(List.of(existingTest)));

            PhysicalTest newTest = new PhysicalTest();
            newTest.setId(20L);

            BatteryModRequest modRequest = new BatteryModRequest(
                    1L,
                    "Battery Mise à Jour",
                    List.of(20L),
                    true
            );

            when(batteryRepository.findById(1L)).thenReturn(Optional.of(existingBattery));
            when(physicalTestRepository.findAllById(List.of(20L))).thenReturn(List.of(newTest));

            physicalTestService.modifyBattery(modRequest);

            assertThat(existingBattery.getName()).isEqualTo("Battery Mise à Jour");
            assertThat(existingBattery.getTests()).hasSize(2).contains(existingTest, newTest);
        }
    }
}