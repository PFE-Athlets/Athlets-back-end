package com.centresportifets.athlets_backend.tests.equipment;

import com.centresportifets.athlets_backend.tests.PhysicalTest;

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
@Table(name = "Test_Equipment")
public class TestEquipment {

    @EmbeddedId
    private TestEquipmentId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("testId")
    private PhysicalTest test;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("equipmentId")
    private Equipment equipment;

    @Column(name = "required_quantity", nullable = false)
    private Integer quantityRequired = 1;
}