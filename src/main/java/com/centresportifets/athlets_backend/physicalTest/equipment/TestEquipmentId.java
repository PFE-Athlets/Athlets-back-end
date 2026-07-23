package com.centresportifets.athlets_backend.physicalTest.equipment;

import java.io.Serializable;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Embeddable
public class TestEquipmentId implements Serializable {

    @Column(name = "id_test")
    private Long testId;

    @Column(name = "id_equipement")
    private Long equipementId;
}