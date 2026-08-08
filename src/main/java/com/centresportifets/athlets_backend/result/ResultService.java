package com.centresportifets.athlets_backend.result;

import com.centresportifets.athlets_backend.auth.AuthService;
import com.centresportifets.athlets_backend.result.dto.ResultValueSubmissionDTO;
import com.centresportifets.athlets_backend.result.dto.TestAssignmentRequest;
import com.centresportifets.athlets_backend.result.dto.TestData;
import com.centresportifets.athlets_backend.result.dto.TestResultSubmission;
import com.centresportifets.athlets_backend.tests.PhysicalTest;
import com.centresportifets.athlets_backend.tests.PhysicalTestRepository;
import com.centresportifets.athlets_backend.tests.ResultType;
import com.centresportifets.athlets_backend.tests.ResultTypeRepository;
import com.centresportifets.athlets_backend.user.UserType;
import com.centresportifets.athlets_backend.user.athlete.Athlete;
import com.centresportifets.athlets_backend.user.athlete.AthleteRepository;
import com.centresportifets.athlets_backend.user.coach.Coach;
import com.centresportifets.athlets_backend.user.coach.CoachRepository;
import com.centresportifets.athlets_backend.user.kine.Kine;
import com.centresportifets.athlets_backend.user.kine.KineRepository;
import com.centresportifets.athlets_backend.user.kine.KineTeamRepository;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class ResultService {

    private final AuthService authService;
    private final AthleteRepository athleteRepository;
    private final CoachRepository coachRepository;
    private final KineRepository kineRepository;
    private final KineTeamRepository kineTeamRepository;
    private final PhysicalTestRepository physicalTestRepository;
    private final ResultRepository resultRepository;
    private final ResultTypeRepository resultTypeRepository;
    private final ResultValueRepository resultValueRepository;

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

    public List<TestData> getTestResults(Authentication auth) {
        UserType currentType = authService.getAuthenticatedUserType(auth);
        log.info("User {} with role {} is retrieving test results", auth.getName(), currentType);

        return getAccessibleResults(auth).stream().map(TestData::new).toList();
    }

    @Transactional(readOnly = true)
    public byte[] exportTestResults(Authentication auth) {
        List<Result> results = getAccessibleResults(auth);

        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            writeResultsSheet(workbook.createSheet("Resultats"), results);
            writeResultValuesSheet(workbook.createSheet("Valeurs"), results);
            workbook.write(outputStream);
            return outputStream.toByteArray();
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to generate Excel export", exception);
        }
    }

    private List<Result> getAccessibleResults(Authentication auth) {
        return switch (authService.getAuthenticatedUserType(auth)) {
            case ADMIN -> resultRepository.findAll();
            case COACH -> getCoachAccessibleResults(auth.getName());
            case KINE -> getKineAccessibleResults(auth.getName());
            default -> resultRepository.findByAthleteUsername(auth.getName());
        };
    }

    private List<Result> getCoachAccessibleResults(String coachUsername) {
        Coach coach = coachRepository.findByUsername(coachUsername)
                .orElseThrow(() -> new IllegalArgumentException("Coach profile not found"));

        if (coach.getTeam() == null) {
            return Collections.emptyList();
        }

        List<Athlete> teamAthletes = athleteRepository.findByAthleteTeamsTeamId(coach.getTeam().getId());
        if (teamAthletes.isEmpty()) {
            return Collections.emptyList();
        }

        List<Long> athleteIds = teamAthletes.stream().map(Athlete::getId).toList();
        return resultRepository.findByAthleteIdIn(athleteIds);
    }

    private List<Result> getKineAccessibleResults(String kineUsername) {
        Kine kine = kineRepository.findByUsername(kineUsername)
                .orElseThrow(() -> new IllegalArgumentException("Kinesiologist profile not found"));

        Set<Long> teamIds = kineTeamRepository.findByKineId(kine.getId()).stream()
                .map(kineTeam -> kineTeam.getTeam().getId())
                .collect(LinkedHashSet::new, Set::add, Set::addAll);

        if (teamIds.isEmpty()) {
            return Collections.emptyList();
        }

        Set<Long> athleteIds = new LinkedHashSet<>();
        for (Long teamId : teamIds) {
            athleteRepository.findByAthleteTeamsTeamId(teamId).stream()
                    .map(Athlete::getId)
                    .forEach(athleteIds::add);
        }

        if (athleteIds.isEmpty()) {
            return Collections.emptyList();
        }

        return resultRepository.findByAthleteIdIn(List.copyOf(athleteIds));
    }

    private void writeResultsSheet(Sheet sheet, List<Result> results) {
        String[] headers = {
            "Resultat ID", "Athlete ID", "Prenom", "Nom", "Test ID", "Test", "Date",
            "Statut", "Preuve requise", "Preuve", "Commentaire", "Nb valeurs"
        };
        createHeader(sheet, headers);

        int rowIndex = 1;
        for (Result result : results) {
            Row row = sheet.createRow(rowIndex++);
            int cellIndex = 0;

            writeCell(row, cellIndex++, result.getId());
            writeCell(row, cellIndex++, result.getAthlete() != null ? result.getAthlete().getId() : null);
            writeCell(row, cellIndex++, result.getAthlete() != null ? result.getAthlete().getFirstName() : null);
            writeCell(row, cellIndex++, result.getAthlete() != null ? result.getAthlete().getLastName() : null);
            writeCell(row, cellIndex++, result.getTest() != null ? result.getTest().getId() : null);
            writeCell(row, cellIndex++, result.getTest() != null ? result.getTest().getName() : null);
            writeCell(row, cellIndex++, result.getTestDate() != null ? result.getTestDate().toString() : null);
            writeCell(row, cellIndex++, result.getStatus());
            writeCell(row, cellIndex++, result.getTest() != null && result.getTest().isProofRequired() ? "Oui" : "Non");
            writeCell(row, cellIndex++, result.getProof());
            writeCell(row, cellIndex++, result.getCommentText());
            writeCell(row, cellIndex, result.getResultValues() != null ? result.getResultValues().size() : 0);
        }

        autoSize(sheet, headers.length);
    }

    private void writeResultValuesSheet(Sheet sheet, List<Result> results) {
        String[] headers = {
            "Resultat ID", "Athlete", "Test", "Type resultat", "Valeur", "Unite", "Symbole"
        };
        createHeader(sheet, headers);

        int rowIndex = 1;
        for (Result result : results) {
            if (result.getResultValues() == null || result.getResultValues().isEmpty()) {
                continue;
            }

            for (ResultValue resultValue : result.getResultValues()) {
                Row row = sheet.createRow(rowIndex++);
                int cellIndex = 0;

                writeCell(row, cellIndex++, result.getId());
                writeCell(
                        row,
                        cellIndex++,
                        result.getAthlete() != null
                                ? result.getAthlete().getFirstName() + " " + result.getAthlete().getLastName()
                                : null);
                writeCell(row, cellIndex++, result.getTest() != null ? result.getTest().getName() : null);
                writeCell(row, cellIndex++, resultValue.getResultType() != null ? resultValue.getResultType().getName() : null);
                writeCell(row, cellIndex++, resultValue.getValue() != null ? resultValue.getValue().toPlainString() : null);
                writeCell(
                        row,
                        cellIndex++,
                        resultValue.getResultType() != null && resultValue.getResultType().getUnitMeasure() != null
                                ? resultValue.getResultType().getUnitMeasure().getName()
                                : null);
                writeCell(
                        row,
                        cellIndex,
                        resultValue.getResultType() != null && resultValue.getResultType().getUnitMeasure() != null
                                ? resultValue.getResultType().getUnitMeasure().getSymbol()
                                : null);
            }
        }

        autoSize(sheet, headers.length);
    }

    private void createHeader(Sheet sheet, String[] headers) {
        Row headerRow = sheet.createRow(0);
        for (int columnIndex = 0; columnIndex < headers.length; columnIndex++) {
            writeCell(headerRow, columnIndex, headers[columnIndex]);
        }
    }

    private void writeCell(Row row, int columnIndex, Object value) {
        Cell cell = row.createCell(columnIndex);
        cell.setCellValue(value == null ? "" : String.valueOf(value));
    }

    private void autoSize(Sheet sheet, int columnCount) {
        for (int columnIndex = 0; columnIndex < columnCount; columnIndex++) {
            sheet.autoSizeColumn(columnIndex);
        }
    }

    private boolean isMissing(String str) {
        return str == null || str.isBlank();
    }
}
