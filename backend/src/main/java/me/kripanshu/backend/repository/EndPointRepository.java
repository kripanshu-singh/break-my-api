package me.kripanshu.backend.repository;

import me.kripanshu.backend.entity.Endpoint;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
public interface EndPointRepository extends JpaRepository<Endpoint, String> {
    List<Endpoint> findByProjectId(String projectId);
}
