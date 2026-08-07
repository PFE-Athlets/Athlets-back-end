package com.centresportifets.athlets_backend.tests.equipment;

import com.centresportifets.athlets_backend.tests.PhysicalTest;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
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
    @JoinColumn(name = "id_test")
    private PhysicalTest test;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("equipmentId")
    @JoinColumn(name = "id_equipment")
    private Equipment equipment;

    @Column(name = "required_quantity", nullable = false)
    private Integer quantityRequired = 1;
}