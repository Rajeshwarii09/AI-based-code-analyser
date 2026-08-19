package com.example.codeanalyser.codesnippet.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.codeanalyser.codesnippet.model.CodeSnippet;

@Repository
public interface CodeSnippetRepository extends JpaRepository<CodeSnippet, Long> {
    // No extra methods needed now - JpaRepository provides CRUD
}
