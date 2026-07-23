package com.centresportifets.athlets_backend.physicalTest.equipment;

import com.centresportifets.athlets_backend.physicalTest.PhysicalTest;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name = "Test_Equipement")
public class TestEquipment {

    @EmbeddedId
    private TestEquipmentId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("testId")
    private PhysicalTest test;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("equipementId")
    private Equipment equipment;

    @Column(name = "quantite_requise", nullable = false)
    private Integer quantityRequired = 1;
}