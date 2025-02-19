package org.example.tweetapi.repository;

import org.example.tweetapi.model.entity.Author;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AuthorRepository extends JpaRepository<Author, Long> {
    boolean existsByLogin(String login);
    boolean existsById(Long id);
}
