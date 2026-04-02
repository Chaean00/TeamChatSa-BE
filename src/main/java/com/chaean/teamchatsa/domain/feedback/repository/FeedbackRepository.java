package com.chaean.teamchatsa.domain.feedback.repository;

import com.chaean.teamchatsa.domain.feedback.model.Feedback;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FeedbackRepository extends JpaRepository<Feedback, Long> {
}
