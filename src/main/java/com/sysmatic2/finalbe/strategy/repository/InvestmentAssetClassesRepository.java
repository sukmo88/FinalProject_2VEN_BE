package com.sysmatic2.finalbe.strategy.repository;

import com.sysmatic2.finalbe.strategy.entity.InvestmentAssetClassesEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface InvestmentAssetClassesRepository extends JpaRepository<InvestmentAssetClassesEntity, Integer> {

}
