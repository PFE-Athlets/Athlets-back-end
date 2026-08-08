package com.centresportifets.athlets_backend.result;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.centresportifets.athlets_backend.auth.AuthService;
import com.centresportifets.athlets_backend.tests.PhysicalTest;
import com.centresportifets.athlets_backend.tests.PhysicalTestRepository;
import com.centresportifets.athlets_backend.tests.ResultType;
import com.centresportifets.athlets_backend.tests.ResultTypeRepository;
import com.centresportifets.athlets_backend.tests.UnitMeasure;
import com.centresportifets.athlets_backend.user.UserType;
import com.centresportifets.athlets_backend.user.athlete.Athlete;
import com.centresportifets.athlets_backend.user.athlete.AthleteRepository;
import com.centresportifets.athlets_backend.user.kine.Kine;
import com.centresportifets.athlets_backend.user.kine.KineRepository;
import com.centresportifets.athlets_backend.user.kine.KineTeam;
import com.centresportifets.athlets_backend.user.kine.KineTeamRepository;
import com.centresportifets.athlets_backend.user.coach.CoachRepository;

import java.io.ByteArrayInputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;

@ExtendWith(MockitoExtension.class)
class ResultServiceTest {

    @Mock
    private AuthService authService;
    @Mock
    private AthleteRepository athleteRepository;
    @Mock
    private CoachRepository coachRepository;
    @Mock
    private KineRepository kineRepository;
    @Mock
    private KineTeamRepository kineTeamRepository;
    @Mock
    private PhysicalTestRepository physicalTestRepository;
    @Mock
    private ResultRepository resultRepository;
    @Mock
    private ResultTypeRepository resultTypeRepository;
    @Mock
    private ResultValueRepository resultValueRepository;
    @Mock
    private Authentication authentication;

    private ResultService resultService;

    @BeforeEach
    void setUp() {
        resultService = new ResultService(
                authService,
                athleteRepository,
                coachRepository,
                kineRepository,
                kineTeamRepository,
                physicalTestRepository,
                resultRepository,
                resultTypeRepository,
                resultValueRepository);
    }

    @Test
    void getTestResults_ForKine_ReturnsAssociatedTeamResults() {
        Kine kine = new Kine();
        kine.setId(99L);

        var team = mock(com.centresportifets.athlets_backend.team.Team.class);
        when(team.getId()).thenReturn(7L);

        KineTeam kineTeam = new KineTeam();
        kineTeam.setKine(kine);
        kineTeam.setTeam(team);

        Athlete athlete = buildAthlete(10L, "Alice", "Martin");
        Result result = buildResult(1L, athlete, "Sprint 30m");

        when(authentication.getName()).thenReturn("kineUser");
        when(authService.getAuthenticatedUserType(authentication)).thenReturn(UserType.KINE);
        when(kineRepository.findByUsername("kineUser")).thenReturn(Optional.of(kine));
        when(kineTeamRepository.findByKineId(99L)).thenReturn(List.of(kineTeam));
        when(athleteRepository.findByAthleteTeamsTeamId(7L)).thenReturn(List.of(athlete));
        when(resultRepository.findByAthleteIdIn(List.of(10L))).thenReturn(List.of(result));

        List<com.centresportifets.athlets_backend.result.dto.TestData> data =
                resultService.getTestResults(authentication);

        assertThat(data).hasSize(1);
        assertThat(data.getFirst().getAthleteFirstName()).isEqualTo("Alice");
        assertThat(data.getFirst().getPhysicalTestName()).isEqualTo("Sprint 30m");
        verify(kineTeamRepository).findByKineId(99L);
    }

    @Test
    void exportTestResults_GeneratesWorkbookWithResultsAndValuesSheets() throws Exception {
        Athlete athlete = buildAthlete(10L, "Alice", "Martin");
        Result result = buildResult(1L, athlete, "Sprint 30m");
        result.setStatus("APPROVED");
        result.setTestDate(LocalDate.of(2026, 8, 7));
        result.setResultValues(List.of(buildResultValue(result, "Temps", "s", new BigDecimal("4.25"))));

        when(authService.getAuthenticatedUserType(authentication)).thenReturn(UserType.ADMIN);
        when(resultRepository.findAll()).thenReturn(List.of(result));

        byte[] workbookBytes = resultService.exportTestResults(authentication);

        assertThat(workbookBytes).isNotEmpty();
        try (var workbook = WorkbookFactory.create(new ByteArrayInputStream(workbookBytes))) {
            assertThat(workbook.getSheet("Resultats")).isNotNull();
            assertThat(workbook.getSheet("Valeurs")).isNotNull();
            assertThat(workbook.getSheet("Resultats").getRow(1).getCell(2).getStringCellValue()).isEqualTo("Alice");
            assertThat(workbook.getSheet("Valeurs").getRow(1).getCell(3).getStringCellValue()).isEqualTo("Temps");
        }
    }

    private Athlete buildAthlete(Long id, String firstName, String lastName) {
        Athlete athlete = new Athlete();
        athlete.setId(id);
        athlete.setFirstName(firstName);
        athlete.setLastName(lastName);
        athlete.setUsername(firstName.toLowerCase());
        return athlete;
    }

    private Result buildResult(Long id, Athlete athlete, String testName) {
        PhysicalTest test = new PhysicalTest();
        test.setId(3L);
        test.setName(testName);
        test.setProofRequired(true);

        Result result = new Result();
        result.setId(id);
        result.setAthlete(athlete);
        result.setTest(test);
        result.setStatus("PENDING");
        result.setProof("preuve.pdf");
        result.setCommentText("RAS");
        return result;
    }

    private ResultValue buildResultValue(Result result, String resultTypeName, String symbol, BigDecimal value) {
        UnitMeasure unitMeasure = new UnitMeasure();
        unitMeasure.setName("Secondes");
        unitMeasure.setSymbol(symbol);

        ResultType resultType = new ResultType();
        resultType.setId(55L);
        resultType.setName(resultTypeName);
        resultType.setUnitMeasure(unitMeasure);

        ResultValue resultValue = new ResultValue();
        resultValue.setId(90L);
        resultValue.setResult(result);
        resultValue.setResultType(resultType);
        resultValue.setValue(value);
        return resultValue;
    }
}
