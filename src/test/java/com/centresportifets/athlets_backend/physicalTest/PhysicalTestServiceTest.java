package com.centresportifets.athlets_backend.physicalTest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.centresportifets.athlets_backend.physicalTest.dto.PhysicalTestCreateRequest;
import com.centresportifets.athlets_backend.sport.Sport;
import com.centresportifets.athlets_backend.sport.SportRepository;

@ExtendWith(MockitoExtension.class)
public class PhysicalTestServiceTest {

    @Mock
    private PhysicalTestRepository physicalTestRepository;

    @Mock
    private SportRepository sportRepository;

    private PhysicalTestService physicalTestService;

    @BeforeEach
    void setUp() {
        physicalTestService = new PhysicalTestService(physicalTestRepository, sportRepository);
    }

    @Test
    void getAllRoute() {
        PhysicalTest test1 = new PhysicalTest();
        test1.setName("Cardio Test");
        PhysicalTest test2 = new PhysicalTest();
        test2.setName("Strength Test");
        
        when(physicalTestRepository.findAll()).thenReturn(List.of(test1, test2));

        List<PhysicalTest> result = physicalTestService.getPhysicalTests();

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getName()).isEqualTo("Cardio Test");
        verify(physicalTestRepository, times(1)).findAll();
    }

    @Test
    void createTestRoute() {
        PhysicalTestCreateRequest request = new PhysicalTestCreateRequest();
        request.setTestName("Beep Test");
        request.setUnit("Level");
        request.setProtocole("20m shuttle runs");
        request.setProof("Video upload");
        request.setSportNames(List.of("Football", "Basketball"));

        Sport football = new Sport();
        football.setName("Football");
        Sport basketball = new Sport();
        basketball.setName("Basketball");

        when(sportRepository.findAllByNameIn(request.getSportNames()))
                .thenReturn(List.of(football, basketball));

        physicalTestService.createPhysicalTest(request);

        ArgumentCaptor<PhysicalTest> testCaptor = ArgumentCaptor.forClass(PhysicalTest.class);
        verify(physicalTestRepository, times(1)).save(testCaptor.capture());

        PhysicalTest savedTest = testCaptor.getValue();
        
        assertThat(savedTest.getName()).isEqualTo("Beep Test");
        assertThat(savedTest.getUnit()).isEqualTo("Level");
        assertThat(savedTest.getProtocole()).isEqualTo("20m shuttle runs");
        assertThat(savedTest.getProof()).isEqualTo("Video upload");
        
        assertThat(savedTest.getSports()).hasSize(2);
        assertThat(savedTest.getSports()).contains(football, basketball);
    }
}