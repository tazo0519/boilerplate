package com.example.boilerplate.goods.entity;

import com.example.boilerplate.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "goods")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Goods extends BaseEntity {

    @Column(nullable = false, unique = true)
    private String code;

    @Column(nullable = false)
    private String name;

    @Column
    private String description;

    @Builder
    private Goods(String code, String name, String description) {
        this.code = code;
        this.name = name;
        this.description = description;
    }
}
