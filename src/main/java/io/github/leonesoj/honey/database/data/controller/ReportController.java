package io.github.leonesoj.honey.database.data.controller;

import io.github.leonesoj.honey.Honey;
import io.github.leonesoj.honey.database.DataContainer;
import io.github.leonesoj.honey.database.cache.NoOpCache;
import io.github.leonesoj.honey.database.data.model.Report;
import io.github.leonesoj.honey.database.data.model.Report.ReportStatus;
import io.github.leonesoj.honey.database.providers.DataStore;
import io.github.leonesoj.honey.observer.subscribers.ReportSubscriber;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class ReportController extends DataController<Report> {

  public ReportController(DataStore data) {
    super(
        data,
        new NoOpCache(),
        new DataContainer(Report.STORAGE_KEY,
            Report.PRIMARY_KEY,
            Report.SCHEMA,
            Report.INDEXED_FIELDS
        ),
        Report::deserialize,
        null,
        Honey.getInstance(),
        false
    );
    getObserverService().registerObserver(new ReportSubscriber());
  }

  public CompletableFuture<Boolean> createReport(UUID issuer, UUID subject, String reason) {
    UUID uuid = UUID.randomUUID();
    return create(uuid,
        new Report(uuid,
            issuer,
            subject,
            Honey.getInstance().getServerId(),
            reason,
            ReportStatus.PENDING_REVIEW,
            Instant.now()
        ));
  }

  public CompletableFuture<Boolean> updateReport(Report report) {
    return update(report.getId(), report);
  }

  public CompletableFuture<Boolean> deleteReport(UUID uuid) {
    return delete(uuid);
  }

  public CompletableFuture<List<Report>> getPendingReports(int limit, int offset) {
    return getMany(Report.STATUS_FIELD, ReportStatus.PENDING_REVIEW.name(), limit, offset);
  }

  public CompletableFuture<Optional<Report>> getReport(UUID uuid) {
    return get(uuid);
  }

  public CompletableFuture<List<Report>> getReportsBy(UUID issuer, int limit, int offset) {
    return getMany(Report.ISSUER_FIELD, issuer.toString(), limit, offset);
  }

  public CompletableFuture<List<Report>> getReportsFor(UUID subject, int limit, int offset) {
    return getMany(Report.SUBJECT_FIELD, subject.toString(), limit, offset);
  }

}
