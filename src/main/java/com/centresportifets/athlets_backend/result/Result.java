package com.centresportifets.athlets_backend.result;

import com.centresportifets.athlets_backend.physicalTest.PhysicalTest;
import com.centresportifets.athlets_backend.auth.userTypes.Athlete;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;

@Data
@Table(name = "result")
@Entity
public class Result {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "test_id", nullable = false)
    private PhysicalTest test;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "athlete_id", nullable = false)
    private Athlete athlete;

    @Column(name = "result_value", length = 10)
    private String resultValue;

    @Column(name = "video_proof", columnDefinition = "TEXT")
    private String videoProof;

    @Column(name = "photo_proof", columnDefinition = "TEXT")
    private String photoProof;

    @Column(nullable = false, length = 25)
    private String status = "Pending approval";

    @Column(name = "comment_text", columnDefinition = "TEXT")
    private String commentText;

    @Column(name = "test_date", nullable = false)
    private LocalDate testDate = LocalDate.now();
}