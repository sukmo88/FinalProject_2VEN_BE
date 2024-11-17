package com.sysmatic2.finalbe.cs.entity;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MessageRecipientId implements java.io.Serializable {
  private Long messageId;
  private Long recipientId;
}