package com.sysmatic2.finalbe.cs.repository;

import com.sysmatic2.finalbe.cs.entity.MessageRecipientEntity;
import com.sysmatic2.finalbe.cs.entity.MessageRecipientId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MessageRecipientRepository extends JpaRepository<MessageRecipientEntity, MessageRecipientId> {
  // 추가적인 쿼리가 필요한 경우 정의 가능
}
