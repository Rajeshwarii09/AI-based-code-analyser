package com.example.codeanalyser.codesnippet.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

import com.example.codeanalyser.codesnippet.model.CodeSnippet;

@Repository
public interface CodeSnippetRepository extends JpaRepository<CodeSnippet, Long> {
    List<CodeSnippet> findByUserIdOrderByUploadedAtDesc(Long userId);
}
