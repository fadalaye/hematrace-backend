package com.hematrace.hematrace.repository;

import com.hematrace.hematrace.entite.TraceLog;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface TracabiliteRepository extends JpaRepository<TraceLog, Long> {
    
    // ========== REQUÊTES DE BASE AVEC PAGINATION ==========
    
    @Query("SELECT t FROM TraceLog t ORDER BY t.timestamp DESC")
    List<TraceLog> findAllOrderByTimestampDesc(Pageable pageable);
    
    @Query("SELECT t FROM TraceLog t ORDER BY t.timestamp ASC")
    List<TraceLog> findAllOrderByTimestampAsc();
    
    // ========== RECHERCHES PAR ENTITÉ ==========
    
    @Query("SELECT t FROM TraceLog t WHERE t.entityType = :entityType AND t.entityId = :entityId ORDER BY t.timestamp DESC")
    List<TraceLog> findByEntityTypeAndEntityId(@Param("entityType") String entityType, 
                                               @Param("entityId") Long entityId);
    
    @Query("SELECT t FROM TraceLog t WHERE t.entityType = :entityType ORDER BY t.timestamp DESC")
    List<TraceLog> findByEntityType(@Param("entityType") String entityType);
    
    @Query("SELECT t FROM TraceLog t WHERE t.entityId = :entityId ORDER BY t.timestamp DESC")
    List<TraceLog> findByEntityId(@Param("entityId") Long entityId);
    
    // ========== RECHERCHES PAR UTILISATEUR ==========
    
    @Query("SELECT t FROM TraceLog t WHERE t.user.id = :userId ORDER BY t.timestamp DESC")
    List<TraceLog> findByUserId(@Param("userId") Long userId);
    
    @Query("SELECT t FROM TraceLog t WHERE t.user.id = :userId AND t.timestamp BETWEEN :start AND :end ORDER BY t.timestamp DESC")
    List<TraceLog> findLogsByUserAndDateRange(@Param("userId") Long userId,
                                             @Param("start") LocalDateTime start,
                                             @Param("end") LocalDateTime end);
    
    // ========== RECHERCHES PAR ACTION ==========
    
    @Query("SELECT t FROM TraceLog t WHERE t.action = :action ORDER BY t.timestamp DESC")
    List<TraceLog> findByAction(@Param("action") String action);
    
    @Query("SELECT t FROM TraceLog t WHERE t.action IN :actions ORDER BY t.timestamp DESC")
    List<TraceLog> findByActions(@Param("actions") List<String> actions);
    
    // ========== RECHERCHES PAR DATE ==========
    
    @Query("SELECT t FROM TraceLog t WHERE t.timestamp BETWEEN :start AND :end ORDER BY t.timestamp DESC")
    List<TraceLog> findByTimestampBetween(@Param("start") LocalDateTime start, 
                                         @Param("end") LocalDateTime end);
    
    @Query("SELECT t FROM TraceLog t WHERE DATE(t.timestamp) = :date ORDER BY t.timestamp DESC")
    List<TraceLog> findByDate(@Param("date") LocalDateTime date);
    
    @Query("SELECT t FROM TraceLog t WHERE t.timestamp >= :start ORDER BY t.timestamp DESC")
    List<TraceLog> findByTimestampAfter(@Param("start") LocalDateTime start);
    
    @Query("SELECT t FROM TraceLog t WHERE t.timestamp <= :end ORDER BY t.timestamp DESC")
    List<TraceLog> findByTimestampBefore(@Param("end") LocalDateTime end);
    
    // ========== RECHERCHE AVANCÉE ==========
    
    @Query("SELECT t FROM TraceLog t WHERE " +
            "(:entityType IS NULL OR t.entityType = :entityType) AND " +
            "(:action IS NULL OR t.action = :action) AND " +
            "(:userId IS NULL OR t.user.id = :userId) AND " +
            "(t.timestamp BETWEEN :start AND :end) " +
            "ORDER BY t.timestamp DESC")
    List<TraceLog> searchLogs(@Param("entityType") String entityType,
                                @Param("action") String action,
                                @Param("userId") Long userId,
                                @Param("start") LocalDateTime start,
                                @Param("end") LocalDateTime end);
    
    // ========== RECHERCHES PAR ENTITÉ ORDONNÉES ==========
    
    @Query("SELECT t FROM TraceLog t WHERE t.entityType = :entityType AND t.entityId = :entityId ORDER BY t.timestamp DESC")
    List<TraceLog> findLogsByEntityOrderByDateDesc(@Param("entityType") String entityType, 
                                                   @Param("entityId") Long entityId);
    
    @Query("SELECT t FROM TraceLog t WHERE t.entityType = :entityType AND t.entityId = :entityId ORDER BY t.timestamp ASC")
    List<TraceLog> findLogsByEntityOrderByDateAsc(@Param("entityType") String entityType, 
                                                  @Param("entityId") Long entityId);
    
    // ========== MÉTHODES DE COMPTAGE ==========
    
    @Query("SELECT COUNT(t) FROM TraceLog t")
    long countAll();
    
    @Query("SELECT COUNT(t) FROM TraceLog t WHERE t.entityType = :entityType")
    long countByEntityType(@Param("entityType") String entityType);
    
    @Query("SELECT COUNT(t) FROM TraceLog t WHERE t.entityType = :entityType AND t.action = :action")
    long countByEntityTypeAndAction(@Param("entityType") String entityType, 
                                   @Param("action") String action);
    
    @Query("SELECT COUNT(t) FROM TraceLog t WHERE t.user.id = :userId")
    long countByUserId(@Param("userId") Long userId);
    
    @Query("SELECT COUNT(t) FROM TraceLog t WHERE t.timestamp BETWEEN :start AND :end")
    long countByTimestampBetween(@Param("start") LocalDateTime start, 
                                @Param("end") LocalDateTime end);
    
    @Query("SELECT COUNT(t) FROM TraceLog t WHERE t.action = :action")
    long countByAction(@Param("action") String action);
    
    // ========== STATISTIQUES PAR TYPE ==========
    
    @Query("SELECT t.entityType, COUNT(t) as count FROM TraceLog t GROUP BY t.entityType ORDER BY count DESC")
    List<Object[]> countLogsByEntityType();
    
    @Query("SELECT t.action, COUNT(t) as count FROM TraceLog t GROUP BY t.action ORDER BY count DESC")
    List<Object[]> countLogsByAction();
    
    @Query("SELECT t.user.id, COUNT(t) as count FROM TraceLog t " +
           "WHERE t.timestamp BETWEEN :start AND :end " +
           "GROUP BY t.user.id ORDER BY count DESC")
    List<Object[]> getTopUsersByActivity(@Param("start") LocalDateTime start, 
                                        @Param("end") LocalDateTime end);
    
    // ========== STATISTIQUES PAR DATE ==========
    
    @Query("SELECT DATE(t.timestamp) as date, COUNT(t) as count FROM TraceLog t " +
           "WHERE t.timestamp BETWEEN :start AND :end " +
           "GROUP BY DATE(t.timestamp) ORDER BY date DESC")
    List<Object[]> countLogsByDate(@Param("start") LocalDateTime start, 
                                  @Param("end") LocalDateTime end);
    
    @Query("SELECT FUNCTION('HOUR', t.timestamp) as hour, COUNT(t) as count FROM TraceLog t " +
           "WHERE DATE(t.timestamp) = CURRENT_DATE " +
           "GROUP BY FUNCTION('HOUR', t.timestamp) ORDER BY hour")
    List<Object[]> getTodayActivityByHour();
    
    @Query("SELECT FUNCTION('DAY', t.timestamp) as day, COUNT(t) as count FROM TraceLog t " +
           "WHERE YEAR(t.timestamp) = :year AND MONTH(t.timestamp) = :month " +
           "GROUP BY FUNCTION('DAY', t.timestamp) ORDER BY day")
    List<Object[]> getMonthlyActivityByDay(@Param("year") int year, 
                                          @Param("month") int month);
    
    // ========== RECHERCHES AVEC LIMITE (CORRIGÉ) ==========
    
    @Query("SELECT t FROM TraceLog t ORDER BY t.timestamp DESC")
    List<TraceLog> findRecentActivity(Pageable pageable);
    
    @Query(value = "SELECT * FROM trace_logs ORDER BY timestamp DESC", nativeQuery = true)
    List<TraceLog> findRecentActivityNative(Pageable pageable);
    
    @Query("SELECT t FROM TraceLog t WHERE t.entityType = :entityType ORDER BY t.timestamp DESC")
    List<TraceLog> findRecentByEntityType(@Param("entityType") String entityType, 
                                         Pageable pageable);
    
    @Query("SELECT t FROM TraceLog t WHERE t.user.id = :userId ORDER BY t.timestamp DESC")
    List<TraceLog> findRecentByUser(@Param("userId") Long userId, 
                                   Pageable pageable);
    
    // ========== RECHERCHES PAR MOT-CLÉ ==========
    
    @Query("SELECT t FROM TraceLog t WHERE " +
           "(t.entityType LIKE %:keyword% OR " +
           "t.action LIKE %:keyword% OR " +
           "t.details LIKE %:keyword%) " +
           "ORDER BY t.timestamp DESC")
    List<TraceLog> searchByKeyword(@Param("keyword") String keyword);
    
    @Query("SELECT t FROM TraceLog t WHERE " +
           "(t.entityType LIKE %:keyword% OR " +
           "t.action LIKE %:keyword% OR " +
           "t.details LIKE %:keyword%) AND " +
           "t.timestamp BETWEEN :start AND :end " +
           "ORDER BY t.timestamp DESC")
    List<TraceLog> searchByKeywordAndDate(@Param("keyword") String keyword,
                                         @Param("start") LocalDateTime start,
                                         @Param("end") LocalDateTime end);
    
    // ========== DISTINCT VALUES ==========
    
    @Query("SELECT DISTINCT t.entityType FROM TraceLog t ORDER BY t.entityType")
    List<String> findAllEntityTypes();
    
    @Query("SELECT DISTINCT t.action FROM TraceLog t ORDER BY t.action")
    List<String> findAllActions();
    
    @Query("SELECT DISTINCT t.user.id FROM TraceLog t WHERE t.user.id IS NOT NULL ORDER BY t.user.id")
    List<Long> findAllUserIds();
    
    // ========== RECHERCHES PAR IP ==========
    
    @Query("SELECT t FROM TraceLog t WHERE t.ipAddress LIKE %:ipAddress% ORDER BY t.timestamp DESC")
    List<TraceLog> findByIpAddress(@Param("ipAddress") String ipAddress);
    
    @Query("SELECT DISTINCT t.ipAddress FROM TraceLog t WHERE t.ipAddress IS NOT NULL")
    List<String> findAllIpAddresses();
    
    // ========== DERNIÈRE ACTIVITÉ ==========
    
    @Query("SELECT t FROM TraceLog t WHERE t.entityType = :entityType AND t.entityId = :entityId ORDER BY t.timestamp DESC")
    Optional<TraceLog> findLatestByEntity(@Param("entityType") String entityType, 
                                         @Param("entityId") Long entityId);
    
    @Query("SELECT t FROM TraceLog t WHERE t.user.id = :userId ORDER BY t.timestamp DESC")
    Optional<TraceLog> findLatestByUser(@Param("userId") Long userId);
    
    @Query("SELECT t FROM TraceLog t ORDER BY t.timestamp DESC")
    Optional<TraceLog> findLatest();
    
    // ========== RECHERCHES POUR EXPORT ==========
    
    @Query("SELECT t FROM TraceLog t WHERE " +
           "(:entityTypes IS NULL OR t.entityType IN :entityTypes) AND " +
           "(:actions IS NULL OR t.action IN :actions) AND " +
           "(:userIds IS NULL OR t.user.id IN :userIds) AND " +
           "t.timestamp BETWEEN :start AND :end " +
           "ORDER BY t.timestamp DESC")
    List<TraceLog> findForExport(@Param("entityTypes") List<String> entityTypes,
                                @Param("actions") List<String> actions,
                                @Param("userIds") List<Long> userIds,
                                @Param("start") LocalDateTime start,
                                @Param("end") LocalDateTime end);
    
    // ========== STATISTIQUES D'ACTIVITÉ DÉTAILLÉES ==========
    
    @Query("SELECT t.entityType, t.action, COUNT(t) as count FROM TraceLog t " +
           "WHERE t.timestamp BETWEEN :start AND :end " +
           "GROUP BY t.entityType, t.action ORDER BY count DESC")
    List<Object[]> getActivityByEntityTypeAndAction(@Param("start") LocalDateTime start, 
                                                   @Param("end") LocalDateTime end);
    
    @Query("SELECT FUNCTION('DATE', t.timestamp) as date, t.entityType, COUNT(t) as count FROM TraceLog t " +
           "WHERE t.timestamp BETWEEN :start AND :end " +
           "GROUP BY FUNCTION('DATE', t.timestamp), t.entityType ORDER BY date DESC, count DESC")
    List<Object[]> getDailyActivityByEntityType(@Param("start") LocalDateTime start, 
                                               @Param("end") LocalDateTime end);
    
    // ========== VÉRIFICATIONS D'EXISTENCE ==========
    
    @Query("SELECT CASE WHEN COUNT(t) > 0 THEN true ELSE false END FROM TraceLog t " +
           "WHERE t.entityType = :entityType AND t.entityId = :entityId")
    boolean existsByEntity(@Param("entityType") String entityType, 
                          @Param("entityId") Long entityId);
    
    @Query("SELECT CASE WHEN COUNT(t) > 0 THEN true ELSE false END FROM TraceLog t " +
           "WHERE t.entityType = :entityType AND t.entityId = :entityId AND t.action = :action")
    boolean existsByEntityAndAction(@Param("entityType") String entityType, 
                                   @Param("entityId") Long entityId,
                                   @Param("action") String action);
    
    // ========== REQUÊTES OPTIMISÉES POUR PERFORMANCE ==========
    
    @Query("SELECT t.id, t.action, t.entityType, t.entityId, t.timestamp, t.user.id FROM TraceLog t " +
           "WHERE t.timestamp BETWEEN :start AND :end " +
           "ORDER BY t.timestamp DESC")
    List<Object[]> findLightweightLogs(@Param("start") LocalDateTime start, 
                                      @Param("end") LocalDateTime end);
    
    @Query("SELECT t FROM TraceLog t " +
           "JOIN FETCH t.user " +
           "WHERE t.timestamp BETWEEN :start AND :end " +
           "ORDER BY t.timestamp DESC")
    List<TraceLog> findLogsWithUser(@Param("start") LocalDateTime start, 
                                   @Param("end") LocalDateTime end);
}