package com.centresportifets.athlets_backend.result;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.centresportifets.athlets_backend.auth.AuthService;
import com.centresportifets.athlets_backend.result.dto.ResultPageData;
import com.centresportifets.athlets_backend.result.dto.ResultRowData;
import com.centresportifets.athlets_backend.result.dto.ResultValueSubmissionDTO;
import com.centresportifets.athlets_backend.result.dto.TestAssignmentRequest;
import com.centresportifets.athlets_backend.result.dto.TestData;
import com.centresportifets.athlets_backend.result.dto.TestResultSubmission;
import com.centresportifets.athlets_backend.team.AthleteTeam;
import com.centresportifets.athlets_backend.team.Team;
import com.centresportifets.athlets_backend.tests.PhysicalTest;
import com.centresportifets.athlets_backend.tests.PhysicalTestRepository;
import com.centresportifets.athlets_backend.tests.ResultType;
import com.centresportifets.athlets_backend.tests.ResultTypeRepository;
import com.centresportifets.athlets_backend.tests.battery.Battery;
import com.centresportifets.athlets_backend.tests.battery.BatteryRepository;
import com.centresportifets.athlets_backend.user.UserAccount;
import com.centresportifets.athlets_backend.user.UserType;
import com.centresportifets.athlets_backend.user.athlete.Athlete;
import com.centresportifets.athlets_backend.user.athlete.AthleteRepository;
import com.centresportifets.athlets_backend.user.coach.Coach;
import com.centresportifets.athlets_backend.user.coach.CoachRepository;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class ResultService {

    private final AuthService authService;
    private final AthleteRepository athleteRepository;
    private final CoachRepository coachRepository;
    private final PhysicalTestRepository physicalTestRepository;
    private final ResultRepository resultRepository;
    private final ResultTypeRepository resultTypeRepository;
    private final ResultValueRepository resultValueRepository;
    private final BatteryRepository batteryRepository;

    private static final Logger log = LoggerFactory.getLogger(ResultService.class);

    @PreAuthorize("@authService.canManageAthletes(authentication, #request.usernames)")
    public void assignTest(TestAssignmentRequest request) {
        List<Athlete> athletes = athleteRepository.findAllByUsernameIn(request.getUsernames());
        PhysicalTest physicalTest = physicalTestRepository.findById(request.getPhysicalTestId())
                .orElseThrow(() -> new IllegalArgumentException("Physical test not found"));

        athletes.forEach(athlete -> {
            Result result = new Result();
            result.setAthlete(athlete);
            result.setTest(physicalTest);
            result.setStatus(ResultStatus.ASSIGNED.getStatus());
            resultRepository.save(result);
        });
    }

    @Transactional
    public void submitAthleteResult(TestResultSubmission resultSubmission, Authentication auth) {
        Result result = resultRepository.findById(resultSubmission.getId())
                .orElseThrow(() -> new IllegalArgumentException("Physical test result not found"));

        if (!authService.isAthleteOwner(auth, result.getAthlete())) {
            throw new AccessDeniedException("You are not authorized to submit this result.");
        }

        if (result.getTest().isProofRequired() && isMissing(resultSubmission.getProof())) {
            throw new IllegalArgumentException("Proof is required for this test");
        }

        if (resultSubmission.getResultValues() == null || resultSubmission.getResultValues().isEmpty()) {
            throw new IllegalArgumentException("At least one result value is required for this test");
        }

        resultValueRepository.deleteByResultId(result.getId());

        for (ResultValueSubmissionDTO valueDTO : resultSubmission.getResultValues()) {
            if (valueDTO.getValue() == null) {
                throw new IllegalArgumentException("Result value cannot be null for result type ID: " + valueDTO.getResultTypeId());
            }

            ResultType resultType = resultTypeRepository.findById(valueDTO.getResultTypeId())
                    .orElseThrow(() -> new IllegalArgumentException("Result type not found with ID: " + valueDTO.getResultTypeId()));

            ResultValue resultValue = new ResultValue();
            resultValue.setResult(result);
            resultValue.setResultType(resultType);
            resultValue.setValue(valueDTO.getValue());

            resultValueRepository.save(resultValue);
        }

        result.setProof(resultSubmission.getProof());
        result.setCommentText(resultSubmission.getComment());
        result.setTestDate(LocalDate.now());
        result.setStatus(ResultStatus.PENDING.getStatus());

        resultRepository.save(result);
    }

    @Transactional
    public void cancelSubmissionAthleteResult(Long testResultId, Authentication auth) {
        Result result = resultRepository.findById(testResultId)
                .orElseThrow(() -> new IllegalArgumentException("Physical test result not found"));

        if (!authService.isAthleteOwner(auth, result.getAthlete())) {
            throw new AccessDeniedException("You are not authorized to cancel this submission.");
        }

        resultValueRepository.deleteByResultId(result.getId());
        result.setStatus(ResultStatus.ASSIGNED.getStatus());
        resultRepository.save(result);
    }

    @PreAuthorize("@authService.hasPermission(authentication, 'ADMIN') or @authService.hasPermission(authentication, 'COACH')")
    public void approveAthleteResult(Long testResultId, boolean approved) {
        Result result = resultRepository.findById(testResultId)
                .orElseThrow(() -> new IllegalArgumentException("Physical test result not found"));

        String status = approved ? ResultStatus.APPROVED.getStatus() : ResultStatus.REJECTED.getStatus();
        result.setStatus(status);
        resultRepository.save(result);
    }

    @Transactional(readOnly = true)
    public List<TestData> getTestResults(Authentication auth) {
        UserType currentType = authService.getAuthenticatedUserType(auth);
        log.info("User {} with role {} is retrieving test results", auth.getName(), currentType);

        return getVisibleResults(auth).stream().map(TestData::new).toList();
    }

    @Transactional(readOnly = true)
    public ResultPageData getResultPageData(Authentication auth) {
        List<ResultRowData> rows = getVisibleResults(auth).stream()
                .map(this::toResultRow)
                .toList();

        return new ResultPageData(rows, buildFilterOptions(rows));
    }

    @Transactional(readOnly = true)
    public byte[] exportResultsWorkbook(
            Authentication auth,
            String startDate,
            String endDate,
            Long athleteId,
            Long testId,
            Long teamId,
            String statusCode,
            Long batteryId) {
        List<ResultRowData> rows = filterRows(
                getVisibleResults(auth).stream()
                        .map(this::toResultRow)
                        .toList(),
                startDate,
                endDate,
                athleteId,
                testId,
                teamId,
                statusCode,
                batteryId);

        try (XSSFWorkbook workbook = new XSSFWorkbook(); ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Résultats");

            Row headerRow = sheet.createRow(0);
            String[] headers = {
                    "Date de saisie",
                    "Athlète",
                    "Test",
                    "Batterie de tests",
                    "Équipe",
                    "Intervenant",
                    "Statut",
                    "Résumé"
            };

            for (int index = 0; index < headers.length; index++) {
                headerRow.createCell(index).setCellValue(headers[index]);
            }

            for (int index = 0; index < rows.size(); index++) {
                ResultRowData row = rows.get(index);
                Row excelRow = sheet.createRow(index + 1);

                excelRow.createCell(0).setCellValue(row.getTestDate() != null ? row.getTestDate().toString() : "");
                excelRow.createCell(1).setCellValue(row.getAthlete() != null ? row.getAthlete().getDisplayName() : "");
                excelRow.createCell(2).setCellValue(row.getTest() != null ? row.getTest().getName() : "");
                excelRow.createCell(3).setCellValue(row.getBattery() != null ? row.getBattery().getName() : "");
                excelRow.createCell(4).setCellValue(row.getTeam() != null ? row.getTeam().getName() : "");
                excelRow.createCell(5).setCellValue(row.getIntervenant() != null ? row.getIntervenant().getDisplayName() : "");
                excelRow.createCell(6).setCellValue(row.getStatusLabel() != null ? row.getStatusLabel() : "");
                excelRow.createCell(7).setCellValue(row.getResultValueSummary() != null ? row.getResultValueSummary() : "");
            }

            for (int index = 0; index < headers.length; index++) {
                sheet.autoSizeColumn(index);
            }

            workbook.write(outputStream);
            return outputStream.toByteArray();
        } catch (IOException exception) {
            throw new IllegalStateException("Impossible de générer le fichier Excel des résultats.", exception);
        }
    }

    private List<ResultRowData> filterRows(
            List<ResultRowData> rows,
            String startDate,
            String endDate,
            Long athleteId,
            Long testId,
            Long teamId,
            String statusCode,
            Long batteryId) {
        LocalDate parsedStartDate = parseDate(startDate);
        LocalDate parsedEndDate = parseDate(endDate);
        String normalizedStatusCode = statusCode == null ? null : statusCode.trim().toUpperCase();

        return rows.stream()
                .filter(row -> parsedStartDate == null || (row.getTestDate() != null && !row.getTestDate().isBefore(parsedStartDate)))
                .filter(row -> parsedEndDate == null || (row.getTestDate() != null && !row.getTestDate().isAfter(parsedEndDate)))
                .filter(row -> athleteId == null || (row.getAthlete() != null && athleteId.equals(row.getAthlete().getId())))
                .filter(row -> testId == null || (row.getTest() != null && testId.equals(row.getTest().getId())))
                .filter(row -> teamId == null || (row.getTeam() != null && teamId.equals(row.getTeam().getId())))
                .filter(row -> batteryId == null || (row.getBattery() != null && batteryId.equals(row.getBattery().getId())))
                .filter(row -> normalizedStatusCode == null || normalizedStatusCode.isBlank() || normalizedStatusCode.equalsIgnoreCase(row.getStatusCode()))
                .toList();
    }

    private LocalDate parseDate(String rawDate) {
        if (rawDate == null || rawDate.isBlank()) {
            return null;
        }

        return LocalDate.parse(rawDate);
    }

    private List<Result> getVisibleResults(Authentication auth) {
        UserType currentType = authService.getAuthenticatedUserType(auth);
        log.info("User {} with role {} is retrieving test results", auth.getName(), currentType);

        return switch (currentType) {
            case ADMIN -> resultRepository.findAll();
            case COACH -> getCoachTeamResults(auth.getName());
            default -> resultRepository.findByAthleteUsername(auth.getName());
        };
    }

    private List<Result> getCoachTeamResults(String coachUsername) {
        Coach coach = coachRepository.findByUsername(coachUsername)
                .orElseThrow(() -> new IllegalArgumentException("Coach profile not found"));

        List<Athlete> teamAthletes = athleteRepository.findByAthleteTeamsTeamId(coach.getTeam().getId());
        if (teamAthletes.isEmpty()) {
            return Collections.emptyList();
        }

        List<Long> athleteIds = teamAthletes.stream().map(Athlete::getId).toList();
        return resultRepository.findByAthleteIdIn(athleteIds);
    }

    private ResultRowData toResultRow(Result result) {
        Battery battery = resolveBattery(result);
        Team team = battery != null ? battery.getTeam() : resolvePrimaryTeam(result.getAthlete());
        List<ResultRowData.ValueSummary> valueSummaries = result.getResultValues().stream()
                .map(this::toValueSummary)
                .toList();

        return new ResultRowData(
                result.getId(),
                result.getTestDate(),
                toStatusCode(result.getStatus()),
                toStatusLabel(result.getStatus()),
                result.getCommentText(),
                result.getProof(),
                new ResultRowData.AthleteSummary(
                        result.getAthlete().getId(),
                        result.getAthlete().getUsername(),
                        result.getAthlete().getFirstName(),
                        result.getAthlete().getLastName(),
                        formatDisplayName(result.getAthlete().getFirstName(), result.getAthlete().getLastName())),
                new ResultRowData.NamedEntitySummary(
                        result.getTest().getId(),
                        result.getTest().getName()),
                team == null
                        ? null
                        : new ResultRowData.NamedEntitySummary(team.getId(), team.getName()),
                battery == null
                        ? null
                        : new ResultRowData.NamedEntitySummary(battery.getId(), battery.getName()),
                toIntervenantSummary(result.getIntervenant()),
                valueSummaries,
                buildResultValueSummary(valueSummaries));
    }

    private ResultPageData.FilterOptions buildFilterOptions(List<ResultRowData> rows) {
        LocalDate minDate = null;
        LocalDate maxDate = null;
        Map<Long, ResultPageData.NamedOption> athletes = new LinkedHashMap<>();
        Map<Long, ResultPageData.NamedOption> tests = new LinkedHashMap<>();
        Map<Long, ResultPageData.NamedOption> teams = new LinkedHashMap<>();
        Map<Long, ResultPageData.NamedOption> batteries = new LinkedHashMap<>();
        Map<String, ResultPageData.StatusOption> statuses = new LinkedHashMap<>();

        for (ResultRowData row : rows) {
            if (row.getTestDate() != null) {
                if (minDate == null || row.getTestDate().isBefore(minDate)) {
                    minDate = row.getTestDate();
                }

                if (maxDate == null || row.getTestDate().isAfter(maxDate)) {
                    maxDate = row.getTestDate();
                }
            }

            if (row.getAthlete() != null && row.getAthlete().getId() != null) {
                athletes.putIfAbsent(
                        row.getAthlete().getId(),
                        new ResultPageData.NamedOption(row.getAthlete().getId(), row.getAthlete().getDisplayName()));
            }

            if (row.getTest() != null && row.getTest().getId() != null) {
                tests.putIfAbsent(
                        row.getTest().getId(),
                        new ResultPageData.NamedOption(row.getTest().getId(), row.getTest().getName()));
            }

            if (row.getTeam() != null && row.getTeam().getId() != null) {
                teams.putIfAbsent(
                        row.getTeam().getId(),
                        new ResultPageData.NamedOption(row.getTeam().getId(), row.getTeam().getName()));
            }

            if (row.getBattery() != null && row.getBattery().getId() != null) {
                batteries.putIfAbsent(
                        row.getBattery().getId(),
                        new ResultPageData.NamedOption(row.getBattery().getId(), row.getBattery().getName()));
            }

            if (row.getStatusCode() != null && !row.getStatusCode().isBlank()) {
                statuses.putIfAbsent(
                        row.getStatusCode(),
                        new ResultPageData.StatusOption(row.getStatusCode(), row.getStatusLabel()));
            }
        }

        return new ResultPageData.FilterOptions(
                minDate,
                maxDate,
                new ArrayList<>(athletes.values()),
                new ArrayList<>(tests.values()),
                new ArrayList<>(teams.values()),
                new ArrayList<>(batteries.values()),
                new ArrayList<>(statuses.values()));
    }

    private ResultRowData.ValueSummary toValueSummary(ResultValue resultValue) {
        String unitSymbol = resultValue.getResultType().getUnitMeasure() != null
                ? resultValue.getResultType().getUnitMeasure().getSymbol()
                : null;

        return new ResultRowData.ValueSummary(
                resultValue.getResultType().getId(),
                resultValue.getResultType().getName(),
                formatResultValue(resultValue.getResultType().getName(), resultValue.getValue(), unitSymbol),
                unitSymbol);
    }

    private ResultRowData.IntervenantSummary toIntervenantSummary(UserAccount intervenant) {
        if (intervenant == null) {
            return null;
        }

        return new ResultRowData.IntervenantSummary(
                intervenant.getId(),
                intervenant.getFirstName(),
                intervenant.getLastName(),
                formatDisplayName(intervenant.getFirstName(), intervenant.getLastName()),
                toRoleLabel(intervenant.getAccessLevel()));
    }

    private Battery resolveBattery(Result result) {
        if (result.getAthlete() == null || result.getTest() == null) {
            return null;
        }

        for (AthleteTeam athleteTeam : result.getAthlete().getAthleteTeams()) {
            Team team = athleteTeam.getTeam();
            if (team == null || team.getId() == null) {
                continue;
            }

            List<Battery> batteries = batteryRepository.findByTeam_IdAndTests_Id(team.getId(), result.getTest().getId());
            if (!batteries.isEmpty()) {
                return batteries.get(0);
            }
        }

        return null;
    }

    private Team resolvePrimaryTeam(Athlete athlete) {
        if (athlete == null || athlete.getAthleteTeams().isEmpty()) {
            return null;
        }

        AthleteTeam athleteTeam = athlete.getAthleteTeams().get(0);
        return athleteTeam == null ? null : athleteTeam.getTeam();
    }

    private String buildResultValueSummary(List<ResultRowData.ValueSummary> values) {
        return values.stream()
                .map(ResultRowData.ValueSummary::getFormattedValue)
                .filter(value -> value != null && !value.isBlank())
                .reduce((first, second) -> first + " | " + second)
                .orElse("");
    }

    private String formatResultValue(String resultTypeName, BigDecimal value, String unitSymbol) {
        String formattedValue = formatDecimal(value);
        String formattedUnit = unitSymbol == null || unitSymbol.isBlank() ? "" : " " + unitSymbol;
        return resultTypeName + ": " + formattedValue + formattedUnit;
    }

    private String formatDecimal(BigDecimal value) {
        if (value == null) {
            return "";
        }

        return value.stripTrailingZeros().toPlainString();
    }

    private String formatDisplayName(String firstName, String lastName) {
        String left = firstName == null ? "" : firstName.trim();
        String right = lastName == null ? "" : lastName.trim();
        return (left + " " + right).trim();
    }

    private String toStatusCode(String rawStatus) {
        return ResultStatus.fromStatus(rawStatus).name();
    }

    private String toStatusLabel(String rawStatus) {
        return ResultStatus.fromStatus(rawStatus).getStatus();
    }

    private String toRoleLabel(int accessLevel) {
        return switch (accessLevel) {
            case 1 -> "ADMIN";
            case 2 -> "COACH";
            case 3 -> "ATHLETE";
            case 4 -> "KINE";
            default -> "USER";
        };
    }

    private boolean isMissing(String str) {
        return str == null || str.isBlank();
    }
}
