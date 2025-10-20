package com.signly.notification.infrastructure.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class EmailOutboxSchemaInitializer {

    private static final Logger logger = LoggerFactory.getLogger(EmailOutboxSchemaInitializer.class);

    private final JdbcTemplate jdbcTemplate;

    public EmailOutboxSchemaInitializer(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        ensureLongTextColumn("template_variables", true);
        ensureLongTextColumn("attachments", false);
    }

    private void ensureLongTextColumn(String columnName, boolean notNull) {
        try {
            String dataType = jdbcTemplate.queryForObject(
                    "SELECT DATA_TYPE FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'email_outbox' AND COLUMN_NAME = ?",
                    String.class,
                    columnName
            );

            if (dataType == null) {
                logger.warn("email_outbox.{} 컬럼 정보를 확인할 수 없습니다.", columnName);
                return;
            }

            if (!"longtext".equalsIgnoreCase(dataType)) {
                logger.info("email_outbox.{} 컬럼을 {} -> LONGTEXT 로 변경합니다.", columnName, dataType);
                String alterSql = String.format(
                        "ALTER TABLE email_outbox MODIFY COLUMN %s LONGTEXT %s",
                        columnName,
                        notNull ? "NOT NULL" : "NULL"
                );
                jdbcTemplate.execute(alterSql);
            }
        } catch (EmptyResultDataAccessException e) {
            logger.warn("email_outbox.{} 컬럼을 찾지 못했습니다.", columnName);
        } catch (DataAccessException e) {
            logger.error("email_outbox.{} 컬럼 스키마 확인/변경 중 오류가 발생했습니다.", columnName, e);
        }
    }
}
