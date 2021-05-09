package com.github.ricorodriges.metrics.view.jpa;

import com.github.ricorodriges.metrics.extractor.jpa.HibernateMetricExtractor.HibernateMetricResult;
import com.github.ricorodriges.metrics.extractor.jpa.HibernateMetricExtractor.HibernateMetricResult.CollectionMetricResult;
import com.github.ricorodriges.metrics.extractor.jpa.HibernateMetricExtractor.HibernateMetricResult.EntityMetricResult;
import com.github.ricorodriges.metrics.extractor.jpa.HibernateMetricExtractor.HibernateMetricResult.QueryMetricResult;
import com.github.ricorodriges.metrics.extractor.jpa.HibernateMetricExtractor.HibernateMetricResult.QueryPlanMetricResult;
import com.github.ricorodriges.metrics.extractor.jpa.HibernateMetricExtractor.HibernateMetricResult.SessionMetricResult;
import com.github.ricorodriges.metrics.extractor.jpa.HibernateMetricExtractor.HibernateMetricResult.StatementMetricResult;
import com.github.ricorodriges.metrics.extractor.jpa.HibernateMetricExtractor.HibernateMetricResult.TimestampCacheMetricResult;
import com.github.ricorodriges.metrics.extractor.jpa.HibernateMetricExtractor.HibernateMetricResult.TransactionMetricResult;
import com.github.ricorodriges.metrics.model.Color;
import com.github.ricorodriges.metrics.model.Section;
import com.github.ricorodriges.metrics.model.Tip;
import com.github.ricorodriges.metrics.model.Width;
import com.github.ricorodriges.metrics.model.view.CompositeMetricView;
import com.github.ricorodriges.metrics.model.view.LabelMetricView;
import com.github.ricorodriges.metrics.model.view.ProgressMetricView;
import com.github.ricorodriges.metrics.model.view.TableMetricView;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import static com.github.ricorodriges.metrics.view.MetricViewUtils.duration;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class HibernateMetricViews {

    public static List<Section.SubSection> buildHibernateSubSections(Collection<HibernateMetricResult> results) {
        return results.stream()
                .sorted(Comparator.comparing(HibernateMetricResult::getEntityManager))
                .map(r -> new Section.SubSection(String.format("'%s' Hibernate entity manager factory", r.getEntityManager()),
                        Arrays.asList(
                                new CompositeMetricView(Width.HALF, null, Arrays.asList(
                                        buildSessionProgress(r.getSessions()),
                                        buildTransactionProgress(r.getTransactions())
                                )),
                                new CompositeMetricView(Width.HALF, null, Arrays.asList(
                                        buildStatementsLabel(r.getStatements()),
                                        buildConnectionsLabel(r.getObtainedConnections()),
                                        buildFlushLabel(r.getFlushes())
                                )),
                                buildQueryTable(r),
                                buildEntitiesAndCollectionsTable(r.getEntities(), r.getCollections())
                        )))
                .collect(Collectors.toList());
    }

    private static ProgressMetricView buildSessionProgress(SessionMetricResult r) {
        return new ProgressMetricView("Completed sessions", r.getOpen(), r.getClosed(),
                String.format("open sessions - %d, closed sessions - %d", r.getOpen(), r.getClosed()));
    }

    private static ProgressMetricView buildTransactionProgress(TransactionMetricResult r) {
        long total = r.getSuccess() + r.getFailure();
        return new ProgressMetricView("Transactions", total, Arrays.asList(
                new ProgressMetricView.ProgressValue(r.getSuccess(), Color.GREEN, new Tip("Successful transactions", String.format("%d transactions", r.getSuccess()), false)),
                new ProgressMetricView.ProgressValue(r.getFailure(), Color.RED, new Tip("Failed transactions", String.format("%d transactions", r.getFailure()), false))
        ), String.format("Optimistic lock failures - %d", r.getOptimisticFailures()));
    }

    private static TableMetricView buildQueryTable(HibernateMetricResult r) {
        final QueryMetricResult queries = r.getQueries();
        final QueryMetricResult naturalIdQueries = r.getNaturalIdQueries();
        final QueryPlanMetricResult queryPlans = r.getQueryPlans();
        final TimestampCacheMetricResult timestampCache = r.getTimestampCache();

        List<String> headers = Arrays.asList("Query Type", "Count", "Slowest query", "Hits", "Misses", "Puts");
        List<List<Object>> values = Arrays.asList(
                Arrays.asList("All Queries",
                        queries.getExecutions(), duration(queries.getMaxExecutionTime()),
                        queries.getHits(), queries.getMisses(), queries.getPuts()),
                Arrays.asList("Natural Id Queries",
                        naturalIdQueries.getExecutions(), duration(naturalIdQueries.getMaxExecutionTime()),
                        naturalIdQueries.getHits(), naturalIdQueries.getMisses(), naturalIdQueries.getPuts()),
                Arrays.asList("Query plans",
                        "", "",
                        queryPlans.getHits(), queryPlans.getMisses(), ""),
                Arrays.asList("Table update timestamps",
                        "", "",
                        timestampCache.getHits(), timestampCache.getMisses(), timestampCache.getPuts())
        );
        return new TableMetricView("Query and cache statistic", headers, values);
    }

    private static LabelMetricView buildStatementsLabel(StatementMetricResult r) {
        String label = String.format("Prepared statements - %d, closed statements - %d", r.getPrepared(), r.getClosed());
        return new LabelMetricView(label);
    }

    private static LabelMetricView buildConnectionsLabel(long connections) {
        String label = String.format("Asked connections - %d", connections);
        return new LabelMetricView(label);
    }

    private static LabelMetricView buildFlushLabel(long flushes) {
        String label = String.format("Flushes - %d", flushes);
        return new LabelMetricView(label);
    }

    private static TableMetricView buildEntitiesAndCollectionsTable(EntityMetricResult e,
                                                                    CollectionMetricResult c) {
        List<String> headers = Arrays.asList("Type", "Inserts", "Recreates", "Loads", "Fetches", "Updates", "Deletes");
        List<List<Object>> values = Arrays.asList(
                Arrays.asList("Entities", e.getInserts(), "", e.getLoads(), e.getFetches(), e.getUpdates(), e.getDeletes()),
                Arrays.asList("Collections", "", c.getRecreates(), c.getLoads(), c.getFetches(), c.getUpdates(), c.getDeletes())
        );
        return new TableMetricView("Entity statistic", headers, values);
    }
}
