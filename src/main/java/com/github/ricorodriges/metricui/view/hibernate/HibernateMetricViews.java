package com.github.ricorodriges.metricui.view.hibernate;

import com.github.ricorodriges.metricui.extractor.hibernate.HibernateMetricExtractor.HibernateMetricResult;
import com.github.ricorodriges.metricui.extractor.hibernate.HibernateMetricExtractor.HibernateMetricResult.*;
import com.github.ricorodriges.metricui.model.Color;
import com.github.ricorodriges.metricui.model.Section;
import com.github.ricorodriges.metricui.model.Tip;
import com.github.ricorodriges.metricui.model.Width;
import com.github.ricorodriges.metricui.model.view.CompositeMetricView;
import com.github.ricorodriges.metricui.model.view.LabelMetricView;
import com.github.ricorodriges.metricui.model.view.ProgressMetricView;
import com.github.ricorodriges.metricui.model.view.TableMetricView;
import lombok.experimental.UtilityClass;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import static com.github.ricorodriges.metricui.view.ViewUtils.durationToString;

@UtilityClass
public final class HibernateMetricViews {

    public static List<Section.SubSection> buildHibernateSubSections(Collection<HibernateMetricResult> results) {
        return results.stream()
                .sorted(Comparator.comparing(HibernateMetricResult::getEntityManager))
                .map(r -> new Section.SubSection(String.format("'%s' Hibernate entity manager factory", r.getEntityManager()),
                        List.of(
                                new CompositeMetricView(Width.HALF, null, List.of(
                                        buildSessionProgress(r.getSessions()),
                                        buildTransactionProgress(r.getTransactions())
                                )),
                                new CompositeMetricView(Width.HALF, null, List.of(
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
        return new ProgressMetricView("Transactions", total, List.of(
                new ProgressMetricView.ProgressValue(r.getSuccess(), Color.GREEN, new Tip("Successful transactions", String.format("%d transactions", r.getSuccess()), false)),
                new ProgressMetricView.ProgressValue(r.getFailure(), Color.RED, new Tip("Failed transactions", String.format("%d transactions", r.getFailure()), false))
        ), String.format("Optimistic lock failures - %d", r.getOptimisticFailures()));
    }

    private static TableMetricView buildQueryTable(HibernateMetricResult r) {
        final QueryMetricResult queries = r.getQueries();
        final QueryMetricResult naturalIdQueries = r.getNaturalIdQueries();
        final QueryPlanMetricResult queryPlans = r.getQueryPlans();
        final TimestampCacheMetricResult timestampCache = r.getTimestampCache();

        List<String> headers = List.of("Query Type", "Count", "Slowest query", "Hits", "Misses", "Puts");
        List<List<Object>> values = List.of(
                List.of("All Queries",
                        queries.getExecutions(), durationToString(queries.getMaxExecutionTime()),
                        queries.getHits(), queries.getMisses(), queries.getPuts()),
                List.of("Natural Id Queries",
                        naturalIdQueries.getExecutions(), durationToString(naturalIdQueries.getMaxExecutionTime()),
                        naturalIdQueries.getHits(), naturalIdQueries.getMisses(), naturalIdQueries.getPuts()),
                List.of("Query plans",
                        "", "",
                        queryPlans.getHits(), queryPlans.getMisses(), ""),
                List.of("Table update timestamps",
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
        List<String> headers = List.of("Type", "Inserts", "Recreates", "Loads", "Fetches", "Updates", "Deletes");
        List<List<Object>> values = List.of(
                List.of("Entities", e.getInserts(), "", e.getLoads(), e.getFetches(), e.getUpdates(), e.getDeletes()),
                List.of("Collections", "", c.getRecreates(), c.getLoads(), c.getFetches(), c.getUpdates(), c.getDeletes())
        );
        return new TableMetricView("Entity statistic", headers, values);
    }
}
