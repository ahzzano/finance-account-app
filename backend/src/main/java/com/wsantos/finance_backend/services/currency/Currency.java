package com.wsantos.finance_backend.services.currency;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Getter;

@Entity 
@Getter 
public class Currency {
    @Id 
    @Column(length = 3)
    @JdbcTypeCode(SqlTypes.CHAR)
    private String code; 

    @Column(nullable = false)
    private String name;

}
