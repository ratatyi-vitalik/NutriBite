package site.persistent.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import site.persistent.entity.Report;

import java.util.List;

public interface ReportRepository extends JpaRepository<Report, Long> {
    long countByStatus(Report.ReportStatus status);

    List<Report> findByStatus(Report.ReportStatus status, Pageable pageable);
}
