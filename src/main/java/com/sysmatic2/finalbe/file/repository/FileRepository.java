package com.sysmatic2.finalbe.file.repository;

import com.sysmatic2.finalbe.file.entity.File;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FileRepository extends JpaRepository<File, Long> {
}
