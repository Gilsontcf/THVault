package com.vault.repository;

import com.vault.model.File;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FileRepository extends JpaRepository<File, Long> {
	List<File> findByUserId(Long userId);

	@EntityGraph(attributePaths = "user") // load user when assoc
	Optional<File> findById(Long id);
}
