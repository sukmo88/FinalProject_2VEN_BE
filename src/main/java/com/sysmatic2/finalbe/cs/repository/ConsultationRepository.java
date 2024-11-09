package com.sysmatic2.finalbe.cs.repository;

import com.sysmatic2.finalbe.cs.entity.Consultation;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ConsultationRepository extends JpaRepository<Consultation, Long> {

  List<Consultation> findBySenderIdOrReceiverId(Long senderId, Long receiverId);

  List<Consultation> findByStrategyId(Long strategyId);
}
