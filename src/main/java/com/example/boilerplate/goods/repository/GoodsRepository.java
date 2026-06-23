package com.example.boilerplate.goods.repository;

import com.example.boilerplate.goods.entity.Goods;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GoodsRepository extends JpaRepository<Goods, Long> {
}
