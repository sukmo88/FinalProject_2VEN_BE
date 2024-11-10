package com.sysmatic2.finalbe.cs.entity;

import jakarta.persistence.*;
import lombok.*;


@Entity
@Table(name = "faq_category")
@Getter
@Setter
@ToString
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FAQCategory {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "faq_category_id")
  private Long id;

  @Column(name = "name", nullable = false)
  private String name;

  @Column(name = "description", columnDefinition = "TEXT")
  private String description;
}