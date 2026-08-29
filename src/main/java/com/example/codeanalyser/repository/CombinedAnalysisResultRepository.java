package com.example.codeanalyser.repository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.codeanalyser.model.CombinedAnalysisResult;
@Repository
public interface CombinedAnalysisResultRepository extends JpaRepository<CombinedAnalysisResult, Long> {
    // Custom query methods can be added here if necessary
}
