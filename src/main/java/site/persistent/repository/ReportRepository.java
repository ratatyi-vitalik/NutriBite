package site.persistent.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import site.persistent.entity.Report;

public interface ReportRepository extends JpaRepository<Report, Long> {
}
