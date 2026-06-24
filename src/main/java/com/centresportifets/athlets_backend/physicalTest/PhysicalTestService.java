package com.centresportifets.athlets_backend.physicalTest;

import java.util.HashSet;
import java.util.List;

import org.springframework.stereotype.Service;

import com.centresportifets.athlets_backend.physicalTest.dto.PhysicalTestCreateRequest;
import com.centresportifets.athlets_backend.sport.Sport;
import com.centresportifets.athlets_backend.sport.SportRepository;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class PhysicalTestService {
    private final PhysicalTestRepository physicalTestRepository;
    private final SportRepository sportRepository;

    public List<PhysicalTest> getPhysicalTests(){
        return physicalTestRepository.findAll();
    }

    public void createPhysicalTest(PhysicalTestCreateRequest request){
        PhysicalTest newTest = new PhysicalTest();

        newTest.setName(request.getTestName());
        newTest.setUnit(request.getUnit());
        newTest.setProtocol(request.getProtocol());
        newTest.setProof(request.getProof());

        if(request.getSportNames() != null && !request.getSportNames().isEmpty()){
            List<Sport> sports = sportRepository.findAllByNameIn(request.getSportNames());
            newTest.setSports(new HashSet<>(sports));
        }

        physicalTestRepository.save(newTest);
    }
}
