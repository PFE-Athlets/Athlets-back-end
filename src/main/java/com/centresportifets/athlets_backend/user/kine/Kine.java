package com.centresportifets.athlets_backend.user.kine;

import com.centresportifets.athlets_backend.user.UserAccount;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "Kine")
@PrimaryKeyJoinColumn(name = "user_id")
public class Kine extends UserAccount {
    public Kine() {
        this.setAccessLevel(2);
    }
}