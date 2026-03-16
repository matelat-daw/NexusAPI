-- NEXUSAPI DATABASE OPTIMIZATION SCRIPTS
-- Run these scripts to optimize database performance

-- ============================================
-- 1. CREATE INDEXES FOR FREQUENTLY USED QUERIES
-- ============================================

-- Index for user lookups by email
CREATE INDEX IF NOT EXISTS idx_user_email
ON AspNetUsers(Email);

-- Index for user lookups by nick
CREATE INDEX IF NOT EXISTS idx_user_nick
ON AspNetUsers(Nick);

-- Index for favorite lookups by user
CREATE INDEX IF NOT EXISTS idx_favorite_user
ON Favorites(UserId);

-- Composite index for user + constellation
CREATE INDEX IF NOT EXISTS idx_favorite_user_constellation
ON Favorites(UserId, ConstellationId);

-- Index for comments by user
CREATE INDEX IF NOT EXISTS idx_comment_user
ON Comments(UserId);

-- Index for comments by constellation
CREATE INDEX IF NOT EXISTS idx_comment_constellation
ON Comments(ConstellationId);

-- Composite index for user + constellation in comments
CREATE INDEX IF NOT EXISTS idx_comment_user_constellation
ON Comments(UserId, ConstellationId);

-- Index for email confirmations
CREATE INDEX IF NOT EXISTS idx_email_confirmation_token
ON EmailConfirmation(Token);

-- Index for user roles
CREATE INDEX IF NOT EXISTS idx_user_roles_user
ON AspNetUserRoles(UserId);

-- ============================================
-- 2. CHECK INDEX USAGE STATISTICS
-- ============================================

-- View index fragmentation (should be < 10%)
SELECT
    OBJECT_NAME(ps.object_id) AS TableName,
    i.name AS IndexName,
    ps.avg_fragmentation_in_percent,
    ps.page_count
FROM sys.dm_db_index_physical_stats(DB_ID(), NULL, NULL, NULL, 'LIMITED') ps
INNER JOIN sys.indexes i ON ps.object_id = i.object_id
    AND ps.index_id = i.index_id
WHERE ps.avg_fragmentation_in_percent > 10
    AND ps.page_count > 1000
ORDER BY ps.avg_fragmentation_in_percent DESC;

-- ============================================
-- 3. REBUILD FRAGMENTED INDEXES (if needed)
-- ============================================

-- Rebuild indexes with fragmentation > 30%
ALTER INDEX REBUILD;

-- ============================================
-- 4. UPDATE STATISTICS
-- ============================================

-- Update statistics for better query plans
EXEC sp_updatestats;

-- ============================================
-- 5. ANALYZE MISSING INDEXES
-- ============================================

-- Find missing indexes that could improve performance
SELECT
    migs.avg_total_user_cost * migs.avg_user_impact * (migs.user_seeks + migs.user_scans) AS improvement_measure,
    'CREATE INDEX idx_' + REPLACE(REPLACE(REPLACE(mid.equality_columns, ', ', '_'), '[', ''), ']', '')
    + ' ON ' + mid.statement + '(' + mid.equality_columns + ')' AS create_index_statement,
    mid.*
FROM sys.dm_db_missing_index_details mid
INNER JOIN sys.dm_db_missing_index_groups mig ON mid.index_handle = mig.index_handle
INNER JOIN sys.dm_db_missing_index_groups_stats migs ON mig.index_group_id = migs.index_group_id
WHERE database_id = DB_ID()
ORDER BY improvement_measure DESC;

-- ============================================
-- 6. COLLECTION STATISTICS
-- ============================================

-- View table sizes
SELECT
    OBJECT_NAME(ps.object_id) AS TableName,
    SUM(ps.reserved_page_count) * 8 / 1024 AS ReservedSizeMB,
    SUM(ps.used_page_count) * 8 / 1024 AS UsedSizeMB
FROM sys.dm_db_partition_stats ps
GROUP BY ps.object_id
ORDER BY SUM(ps.reserved_page_count) DESC;

-- ============================================
-- 7. SLOW QUERY ANALYSIS
-- ============================================

-- Check for missing indexes that would help slow queries
SELECT TOP 10
    (total_elapsed_time / execution_count) / 1000 AS avg_elapsed_time_ms,
    execution_count,
    total_elapsed_time / 1000 AS total_elapsed_time_ms,
    SUBSTRING(text, statement_start_offset/2 + 1,
        (CASE WHEN statement_end_offset = -1 THEN LEN(CONVERT(NVARCHAR(max), text))
              ELSE statement_end_offset/2 END) - statement_start_offset/2 + 1) AS query_text
FROM sys.dm_exec_query_stats qs
CROSS APPLY sys.dm_exec_sql_text(qs.sql_handle)
WHERE total_elapsed_time > 1000000  -- More than 1 second total
ORDER BY (total_elapsed_time / execution_count) DESC;

-- ============================================
-- 8. DEADLOCK DETECTION
-- ============================================

-- Enable deadlock detection
DBCC TRACEON (1222, -1);

-- ============================================
-- 9. MAINTENANCE PLAN RECOMMENDATIONS
-- ============================================

-- Schedule weekly:
-- 1. DBCC CHECKDB (Integrity check)
-- 2. Update Statistics
-- 3. Rebuild fragmented indexes (>30%)
-- 4. Reorganize slightly fragmented indexes (10-30%)

-- Sample maintenance script
-- Run weekly
BEGIN TRANSACTION;

-- Check database integrity
DBCC CHECKDB ([Nexus]) WITH NO_INFOMSGS;

-- Update statistics
EXEC sp_updatestats;

-- Rebuild indexes with high fragmentation
ALTER INDEX ALL ON AspNetUsers REBUILD;
ALTER INDEX ALL ON Favorites REBUILD;
ALTER INDEX ALL ON Comments REBUILD;

COMMIT TRANSACTION;

-- ============================================
-- 10. QUERY HINTS FOR OPTIMIZATION
-- ============================================

-- Example: Force index usage in problematic queries
-- SELECT * FROM AspNetUsers (INDEX(idx_user_email))
-- WHERE Email = 'user@example.com';

-- Enable STATISTICS IO to track query performance
SET STATISTICS IO ON;
-- Run your query here
SET STATISTICS IO OFF;

-- ============================================
-- 11. CONNECTION POOL OPTIMIZATION
-- ============================================

-- SQL Server configuration for production
-- Execute as sa or sysadmin

-- Set max degree of parallelism (recommended: number of cores / 2)
EXEC sp_configure 'show advanced options', 1;
RECONFIGURE;
EXEC sp_configure 'max degree of parallelism', 4;  -- Adjust to your CPU cores
RECONFIGURE;

-- Enable instant file initialization (faster DB growth)
-- Note: Requires restarting SQL Server service
-- Grant "Perform Volume Maintenance Tasks" to SQL Server service account

-- ============================================
-- 12. MONITORING QUERIES
-- ============================================

-- Monitor current active queries
SELECT
    session_id,
    start_time,
    status,
    command,
    sql_text = (SELECT TEXT FROM sys.dm_exec_sql_text(sql_handle))
FROM sys.dm_exec_requests
WHERE session_id > 50;

-- Monitor connection count
SELECT
    COUNT(*) AS active_connections,
    SUM(CASE WHEN status = 'sleeping' THEN 1 ELSE 0 END) AS idle_connections
FROM sys.dm_exec_sessions
WHERE session_id > 50;

-- Monitor tempdb usage
SELECT
    SUM(internal_object_reserved_page_count) * 8 / 1024 AS tempdb_internal_mb,
    SUM(user_object_reserved_page_count) * 8 / 1024 AS tempdb_user_mb
FROM sys.dm_db_session_space_usage;

-- ============================================
-- MAINTENANCE SCHEDULE
-- ============================================

/*
DAILY:
- Monitor logs for errors
- Check disk space
- Verify backups completed

WEEKLY:
- Run DBCC CHECKDB
- Update statistics
- Rebuild indexes > 30% fragmented
- Review slow queries

MONTHLY:
- Review query performance
- Analyze missing indexes
- Check growth trends
- Update documentation

QUARTERLY:
- Performance baseline review
- Capacity planning
- Archive old data if needed
*/

-- ============================================
-- END OF OPTIMIZATION SCRIPTS
-- ============================================

