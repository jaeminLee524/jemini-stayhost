package com.jemini.stayhost.partner.domain.model;

import com.jemini.stayhost.common.domain.BaseEntity;
import com.jemini.stayhost.common.exception.AuthorizationException;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "partner")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Partner extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String businessName;

    @Column(nullable = false, unique = true, length = 20)
    private String businessNumber;

    @Column(nullable = false, length = 100)
    private String representative;

    @Column(length = 20)
    private String phone;

    @Column(length = 200)
    private String email;

    @Column(nullable = false, unique = true, length = 100)
    private String loginId;

    @Column(nullable = false)
    private String password;

    @Column(length = 50)
    private String bankName;

    @Column(length = 50)
    private String bankAccount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PartnerStatus status;

    public static Partner create(
        final String businessName,
        final String businessNumber,
        final String representative,
        final String phone,
        final String email,
        final String loginId,
        final String encodedPassword
    ) {
        final Partner partner = new Partner();
        partner.businessName = businessName;
        partner.businessNumber = businessNumber;
        partner.representative = representative;
        partner.phone = phone;
        partner.email = email;
        partner.loginId = loginId;
        partner.password = encodedPassword;
        partner.status = PartnerStatus.PENDING;
        return partner;
    }

    public void activate() {
        this.status = PartnerStatus.ACTIVE;
    }

    public void suspend() {
        this.status = PartnerStatus.SUSPENDED;
    }

    public void update(final String phone, final String email, final String bankName, final String bankAccount) {
        this.phone = phone;
        this.email = email;
        this.bankName = bankName;
        this.bankAccount = bankAccount;
    }

    public void validateActive() {
        if (this.status != PartnerStatus.ACTIVE) {
            throw new AuthorizationException("활성 상태가 아닌 파트너입니다.");
        }
    }
}
