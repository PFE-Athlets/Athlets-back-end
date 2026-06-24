package com.centresportifets.athlets_backend.auth.userTypes;

import com.centresportifets.athlets_backend.auth.UserAccount;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "administrator")
@PrimaryKeyJoinColumn(name = "user_id")
public class Administrator extends UserAccount {

    @Column(length = 50)
    private String title;

    public Administrator() {
        this.setAccessLevel(1);
    }
}