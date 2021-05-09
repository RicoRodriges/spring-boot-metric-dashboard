package com.github.ricorodriges.metrics.extractor.jpa;

import com.github.ricorodriges.metrics.extractor.ExtractorUtils;
import com.github.ricorodriges.metrics.extractor.jpa.HibernateMetricExtractor.HibernateMetricResult.CollectionMetricResult;
import com.github.ricorodriges.metrics.extractor.jpa.HibernateMetricExtractor.HibernateMetricResult.EntityMetricResult;
import com.github.ricorodriges.metrics.extractor.jpa.HibernateMetricExtractor.HibernateMetricResult.QueryMetricResult;
import com.github.ricorodriges.metrics.extractor.jpa.HibernateMetricExtractor.HibernateMetricResult.QueryPlanMetricResult;
import com.github.ricorodriges.metrics.extractor.jpa.HibernateMetricExtractor.HibernateMetricResult.SecondLevelCacheMetricResult;
import com.github.ricorodriges.metrics.extractor.jpa.HibernateMetricExtractor.HibernateMetricResult.SessionMetricResult;
import com.github.ricorodriges.metrics.extractor.jpa.HibernateMetricExtractor.HibernateMetricResult.StatementMetricResult;
import com.github.ricorodriges.metrics.extractor.jpa.HibernateMetricExtractor.HibernateMetricResult.TimestampCacheMetricResult;
import com.github.ricorodriges.metrics.extractor.jpa.HibernateMetricExtractor.HibernateMetricResult.TransactionMetricResult;
import com.github.ricorodriges.metrics.model.MeterData;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Duration;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.github.ricorodriges.metrics.extractor.ExtractorUtils.findMetersByName;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class HibernateMetricExtractor {

    public static List<HibernateMetricResult> extractResults(Collection<MeterData> meters) {
        List<MeterData> hibernateMeters = meters.stream()
                .filter(m -> m.getName().startsWith("hibernate."))
                .collect(Collectors.toList());
        return hibernateMeters.stream()
                .map(HibernateMetricExtractor::getEntityManagerFactory)
                .distinct()
                .map(manager -> new HibernateMetricResult(manager,
                        extractSessions(hibernateMeters, manager),
                        extractTransactions(hibernateMeters, manager),
                        extractFlushCount(hibernateMeters, manager),
                        extractConnections(hibernateMeters, manager),
                        extractStatements(hibernateMeters, manager),
                        extractSecondLevelCaches(hibernateMeters, manager),
                        extractEntities(hibernateMeters, manager),
                        extractCollections(hibernateMeters, manager),
                        extractNaturalIdQueries(hibernateMeters, manager),
                        extractQueries(hibernateMeters, manager),
                        extractQueryPlans(hibernateMeters, manager),
                        extractTimestampCache(hibernateMeters, manager)))
                .collect(Collectors.toList());
    }

    private static SessionMetricResult extractSessions(Collection<MeterData> meters, String manager) {
        long openSessions = findMetersByName(meters, "hibernate.sessions.open")
                .filter(m -> manager.equals(getEntityManagerFactory(m)))
                .findAny()
                .flatMap(ExtractorUtils::getFirstValueAsLong)
                .orElse(0L);
        long closedSessions = findMetersByName(meters, "hibernate.sessions.closed")
                .filter(m -> manager.equals(getEntityManagerFactory(m)))
                .findAny()
                .flatMap(ExtractorUtils::getFirstValueAsLong)
                .orElse(0L);
        return new SessionMetricResult(openSessions, closedSessions);
    }

    private static TransactionMetricResult extractTransactions(Collection<MeterData> meters, String manager) {
        long success = findMetersByName(meters, "hibernate.transactions")
                .filter(m -> manager.equals(getEntityManagerFactory(m)))
                .filter(HibernateMetricExtractor::isTransactionSuccess)
                .findAny()
                .flatMap(ExtractorUtils::getFirstValueAsLong)
                .orElse(0L);
        long failure = findMetersByName(meters, "hibernate.transactions")
                .filter(m -> manager.equals(getEntityManagerFactory(m)))
                .filter(m -> !isTransactionSuccess(m))
                .findAny()
                .flatMap(ExtractorUtils::getFirstValueAsLong)
                .orElse(0L);
        long optimisticFailures = findMetersByName(meters, "hibernate.optimistic.failures")
                .filter(m -> manager.equals(getEntityManagerFactory(m)))
                .findAny()
                .flatMap(ExtractorUtils::getFirstValueAsLong)
                .orElse(0L);
        return new TransactionMetricResult(success, failure, optimisticFailures);
    }

    private static StatementMetricResult extractStatements(Collection<MeterData> meters, String manager) {
        long preparedStatements = findMetersByName(meters, "hibernate.statements")
                .filter(m -> manager.equals(getEntityManagerFactory(m)))
                .filter(m -> "prepared".equals(m.getTags().get("status")))
                .findAny()
                .flatMap(ExtractorUtils::getFirstValueAsLong)
                .orElse(0L);
        long closedStatements = findMetersByName(meters, "hibernate.statements")
                .filter(m -> manager.equals(getEntityManagerFactory(m)))
                .filter(m -> "closed".equals(m.getTags().get("status")))
                .findAny()
                .flatMap(ExtractorUtils::getFirstValueAsLong)
                .orElse(0L);
        return new StatementMetricResult(preparedStatements, closedStatements);
    }

    private static long extractConnections(Collection<MeterData> meters, String manager) {
        return findMetersByName(meters, "hibernate.connections.obtained")
                .filter(m -> manager.equals(getEntityManagerFactory(m)))
                .findAny()
                .flatMap(ExtractorUtils::getFirstValueAsLong)
                .orElse(0L);
    }

    private static long extractFlushCount(Collection<MeterData> meters, String manager) {
        return findMetersByName(meters, "hibernate.flushes")
                .filter(m -> manager.equals(getEntityManagerFactory(m)))
                .findAny()
                .flatMap(ExtractorUtils::getFirstValueAsLong)
                .orElse(0L);
    }

    private static EntityMetricResult extractEntities(Collection<MeterData> meters, String manager) {
        long deletes = findMetersByName(meters, "hibernate.entities.deletes")
                .filter(m -> manager.equals(getEntityManagerFactory(m)))
                .findAny()
                .flatMap(ExtractorUtils::getFirstValueAsLong)
                .orElse(0L);
        long fetches = findMetersByName(meters, "hibernate.entities.fetches")
                .filter(m -> manager.equals(getEntityManagerFactory(m)))
                .findAny()
                .flatMap(ExtractorUtils::getFirstValueAsLong)
                .orElse(0L);
        long loads = findMetersByName(meters, "hibernate.entities.loads")
                .filter(m -> manager.equals(getEntityManagerFactory(m)))
                .findAny()
                .flatMap(ExtractorUtils::getFirstValueAsLong)
                .orElse(0L);
        long updates = findMetersByName(meters, "hibernate.entities.updates")
                .filter(m -> manager.equals(getEntityManagerFactory(m)))
                .findAny()
                .flatMap(ExtractorUtils::getFirstValueAsLong)
                .orElse(0L);
        long inserts = findMetersByName(meters, "hibernate.entities.inserts")
                .filter(m -> manager.equals(getEntityManagerFactory(m)))
                .findAny()
                .flatMap(ExtractorUtils::getFirstValueAsLong)
                .orElse(0L);
        return new EntityMetricResult(deletes, fetches, inserts, loads, updates);
    }

    private static CollectionMetricResult extractCollections(Collection<MeterData> meters, String manager) {
        long deletes = findMetersByName(meters, "hibernate.collections.deletes")
                .filter(m -> manager.equals(getEntityManagerFactory(m)))
                .findAny()
                .flatMap(ExtractorUtils::getFirstValueAsLong)
                .orElse(0L);
        long fetches = findMetersByName(meters, "hibernate.collections.fetches")
                .filter(m -> manager.equals(getEntityManagerFactory(m)))
                .findAny()
                .flatMap(ExtractorUtils::getFirstValueAsLong)
                .orElse(0L);
        long loads = findMetersByName(meters, "hibernate.collections.loads")
                .filter(m -> manager.equals(getEntityManagerFactory(m)))
                .findAny()
                .flatMap(ExtractorUtils::getFirstValueAsLong)
                .orElse(0L);
        long updates = findMetersByName(meters, "hibernate.collections.updates")
                .filter(m -> manager.equals(getEntityManagerFactory(m)))
                .findAny()
                .flatMap(ExtractorUtils::getFirstValueAsLong)
                .orElse(0L);
        long recreates = findMetersByName(meters, "hibernate.collections.recreates")
                .filter(m -> manager.equals(getEntityManagerFactory(m)))
                .findAny()
                .flatMap(ExtractorUtils::getFirstValueAsLong)
                .orElse(0L);
        return new CollectionMetricResult(deletes, fetches, loads, recreates, updates);
    }

    private static QueryMetricResult extractNaturalIdQueries(Collection<MeterData> meters, String manager) {
        long max = findMetersByName(meters, "hibernate.query.natural.id.executions.max")
                .filter(m -> manager.equals(getEntityManagerFactory(m)))
                .findAny()
                .flatMap(ExtractorUtils::getFirstValue)
                .map(v -> (long) (v * 1000))
                .orElse(0L);
        long executions = findMetersByName(meters, "hibernate.query.natural.id.executions")
                .filter(m -> manager.equals(getEntityManagerFactory(m)))
                .findAny()
                .flatMap(ExtractorUtils::getFirstValueAsLong)
                .orElse(0L);
        long hits = findMetersByName(meters, "hibernate.cache.natural.id.requests")
                .filter(m -> manager.equals(getEntityManagerFactory(m)))
                .filter(HibernateMetricExtractor::isHit)
                .findAny()
                .flatMap(ExtractorUtils::getFirstValueAsLong)
                .orElse(0L);
        long misses = findMetersByName(meters, "hibernate.cache.natural.id.requests")
                .filter(m -> manager.equals(getEntityManagerFactory(m)))
                .filter(HibernateMetricExtractor::isMiss)
                .findAny()
                .flatMap(ExtractorUtils::getFirstValueAsLong)
                .orElse(0L);
        long puts = findMetersByName(meters, "hibernate.cache.natural.id.puts")
                .filter(m -> manager.equals(getEntityManagerFactory(m)))
                .findAny()
                .flatMap(ExtractorUtils::getFirstValueAsLong)
                .orElse(0L);

        return new QueryMetricResult(hits, misses, puts, executions, Duration.ofMillis(max));
    }

    private static QueryMetricResult extractQueries(Collection<MeterData> meters, String manager) {
        long max = findMetersByName(meters, "hibernate.query.executions.max")
                .filter(m -> manager.equals(getEntityManagerFactory(m)))
                .findAny()
                .flatMap(ExtractorUtils::getFirstValue)
                .map(v -> (long) (v * 1000))
                .orElse(0L);
        long executions = findMetersByName(meters, "hibernate.query.executions")
                .filter(m -> manager.equals(getEntityManagerFactory(m)))
                .findAny()
                .flatMap(ExtractorUtils::getFirstValueAsLong)
                .orElse(0L);
        long hits = findMetersByName(meters, "hibernate.cache.query.requests")
                .filter(m -> manager.equals(getEntityManagerFactory(m)))
                .filter(HibernateMetricExtractor::isHit)
                .findAny()
                .flatMap(ExtractorUtils::getFirstValueAsLong)
                .orElse(0L);
        long misses = findMetersByName(meters, "hibernate.cache.query.requests")
                .filter(m -> manager.equals(getEntityManagerFactory(m)))
                .filter(HibernateMetricExtractor::isMiss)
                .findAny()
                .flatMap(ExtractorUtils::getFirstValueAsLong)
                .orElse(0L);
        long puts = findMetersByName(meters, "hibernate.cache.query.puts")
                .filter(m -> manager.equals(getEntityManagerFactory(m)))
                .findAny()
                .flatMap(ExtractorUtils::getFirstValueAsLong)
                .orElse(0L);

        return new QueryMetricResult(hits, misses, puts, executions, Duration.ofMillis(max));
    }

    private static QueryPlanMetricResult extractQueryPlans(Collection<MeterData> meters, String manager) {
        long hits = findMetersByName(meters, "hibernate.cache.query.plan")
                .filter(m -> manager.equals(getEntityManagerFactory(m)))
                .filter(HibernateMetricExtractor::isHit)
                .findAny()
                .flatMap(ExtractorUtils::getFirstValueAsLong)
                .orElse(0L);
        long misses = findMetersByName(meters, "hibernate.cache.query.plan")
                .filter(m -> manager.equals(getEntityManagerFactory(m)))
                .filter(HibernateMetricExtractor::isMiss)
                .findAny()
                .flatMap(ExtractorUtils::getFirstValueAsLong)
                .orElse(0L);

        return new QueryPlanMetricResult(hits, misses);
    }

    private static List<SecondLevelCacheMetricResult> extractSecondLevelCaches(Collection<MeterData> meters, String manager) {
        return findMetersByName(meters, "hibernate.second.level.cache.requests")
                .filter(m -> manager.equals(getEntityManagerFactory(m)))
                .map(HibernateMetricExtractor::getRegion)
                .distinct()
                .map(region -> {
                    long hits = findMetersByName(meters, "hibernate.second.level.cache.requests")
                            .filter(m -> manager.equals(getEntityManagerFactory(m)))
                            .filter(m -> region.equals(getRegion(m)))
                            .filter(HibernateMetricExtractor::isHit)
                            .findAny()
                            .flatMap(ExtractorUtils::getFirstValueAsLong)
                            .orElse(0L);
                    long misses = findMetersByName(meters, "hibernate.second.level.cache.requests")
                            .filter(m -> manager.equals(getEntityManagerFactory(m)))
                            .filter(m -> region.equals(getRegion(m)))
                            .filter(HibernateMetricExtractor::isMiss)
                            .findAny()
                            .flatMap(ExtractorUtils::getFirstValueAsLong)
                            .orElse(0L);
                    long puts = findMetersByName(meters, "hibernate.second.level.cache.puts")
                            .filter(m -> manager.equals(getEntityManagerFactory(m)))
                            .filter(m -> region.equals(getRegion(m)))
                            .findAny()
                            .flatMap(ExtractorUtils::getFirstValueAsLong)
                            .orElse(0L);
                    return new SecondLevelCacheMetricResult(region, hits, misses, puts);
                })
                .collect(Collectors.toList());
    }

    private static TimestampCacheMetricResult extractTimestampCache(Collection<MeterData> meters, String manager) {
        long hits = findMetersByName(meters, "hibernate.cache.update.timestamps.requests")
                .filter(m -> manager.equals(getEntityManagerFactory(m)))
                .filter(HibernateMetricExtractor::isHit)
                .findAny()
                .flatMap(ExtractorUtils::getFirstValueAsLong)
                .orElse(0L);
        long misses = findMetersByName(meters, "hibernate.cache.update.timestamps.requests")
                .filter(m -> manager.equals(getEntityManagerFactory(m)))
                .filter(HibernateMetricExtractor::isMiss)
                .findAny()
                .flatMap(ExtractorUtils::getFirstValueAsLong)
                .orElse(0L);
        long puts = findMetersByName(meters, "hibernate.cache.update.timestamps.puts")
                .filter(m -> manager.equals(getEntityManagerFactory(m)))
                .findAny()
                .flatMap(ExtractorUtils::getFirstValueAsLong)
                .orElse(0L);

        return new TimestampCacheMetricResult(hits, misses, puts);
    }

    private static String getEntityManagerFactory(MeterData m) {
        return m.getTags().get("entityManagerFactory");
    }

    private static String getRegion(MeterData m) {
        return m.getTags().get("region");
    }

    private static boolean isTransactionSuccess(MeterData m) {
        return "success".equals(m.getTags().get("result"));
    }

    private static boolean isHit(MeterData m) {
        return "hit".equals(m.getTags().get("result"));
    }

    private static boolean isMiss(MeterData m) {
        return "miss".equals(m.getTags().get("result"));
    }

    @lombok.Data
    @lombok.AllArgsConstructor
    public static class HibernateMetricResult {
        private String entityManager;
        private SessionMetricResult sessions;
        private TransactionMetricResult transactions;
        private Long flushes;
        private Long obtainedConnections;
        private StatementMetricResult statements;
        private Collection<SecondLevelCacheMetricResult> secondLevelCaches;
        private EntityMetricResult entities;
        private CollectionMetricResult collections;
        private QueryMetricResult naturalIdQueries;
        private QueryMetricResult queries;
        private QueryPlanMetricResult queryPlans;
        private TimestampCacheMetricResult timestampCache;


        @lombok.Data
        @lombok.AllArgsConstructor
        public static class SessionMetricResult {
            private Long open;
            private Long closed;
        }

        @lombok.Data
        @lombok.AllArgsConstructor
        public static class TransactionMetricResult {
            private Long success;
            private Long failure;
            private Long optimisticFailures;
        }

        @lombok.Data
        @lombok.AllArgsConstructor
        public static class StatementMetricResult {
            private Long prepared;
            private Long closed;
        }

        @lombok.Data
        @lombok.AllArgsConstructor
        public static class SecondLevelCacheMetricResult {
            private String region;
            private Long hits;
            private Long misses;
            private Long puts;
        }

        @lombok.Data
        @lombok.AllArgsConstructor
        public static class EntityMetricResult {
            private Long deletes;
            private Long fetches;
            private Long inserts;
            private Long loads;
            private Long updates;
        }

        @lombok.Data
        @lombok.AllArgsConstructor
        public static class CollectionMetricResult {
            private Long deletes;
            private Long fetches;
            private Long loads;
            private Long recreates;
            private Long updates;
        }

        @lombok.Data
        @lombok.AllArgsConstructor
        public static class QueryMetricResult {
            private Long hits;
            private Long misses;
            private Long puts;

            private Long executions;
            private Duration maxExecutionTime;
        }

        @lombok.Data
        @lombok.AllArgsConstructor
        public static class QueryPlanMetricResult {
            private Long hits;
            private Long misses;
        }

        @lombok.Data
        @lombok.AllArgsConstructor
        public static class TimestampCacheMetricResult {
            private Long hits;
            private Long misses;
            private Long puts;
        }

    }
}
