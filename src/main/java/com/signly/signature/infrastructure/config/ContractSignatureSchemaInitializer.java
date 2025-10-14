package com.signly.signature.infrastructure.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class ContractSignatureSchemaInitializer {

    private static final Logger logger = LoggerFactory.getLogger(ContractSignatureSchemaInitializer.class);

    private final JdbcTemplate jdbcTemplate;

    public ContractSignatureSchemaInitializer(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        ensureLongTextColumn("signature_data", true);
        ensureLongTextColumn("device_info", false);
        ensureSignaturePathColumn();
    }

    private void ensureLongTextColumn(String columnName, boolean notNull) {
        try {
            String dataType = jdbcTemplate.queryForObject(
                    "SELECT DATA_TYPE FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'contract_signatures' AND COLUMN_NAME = ?",
                    String.class,
                    columnName
            );

            if (dataType == null) {
                logger.warn("contract_signatures.{} 컬럼 정보를 확인할 수 없습니다.", columnName);
                return;
            }

            if (!"longtext".equalsIgnoreCase(dataType)) {
                logger.info("contract_signatures.{} 컬럼을 {} -> LONGTEXT 로 변경합니다.", columnName, dataType);
                String alterSql = String.format(
                        "ALTER TABLE contract_signatures MODIFY COLUMN %s LONGTEXT %s",
                        columnName,
                        notNull ? "NOT NULL" : "NULL"
                );
                jdbcTemplate.execute(alterSql);
            }
        } catch (EmptyResultDataAccessException e) {
            logger.warn("contract_signatures.{} 컬럼을 찾지 못했습니다.", columnName);
        } catch (DataAccessException e) {
            logger.error("contract_signatures.{} 컬럼 스키마 확인/변경 중 오류가 발생했습니다.", columnName, e);
        }
    }

    private void ensureSignaturePathColumn() {
        try {
            Integer count = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'contract_signatures' AND COLUMN_NAME = 'signature_path'",
                    Integer.class
            );

            if (count == null || count == 0) {
                logger.info("contract_signatures.signature_path 컬럼을 추가합니다.");
                jdbcTemplate.execute("ALTER TABLE contract_signatures ADD COLUMN signature_path VARCHAR(1000)");
            }
        } catch (DataAccessException e) {
            logger.error("contract_signatures.signature_path 컬럼 생성 중 오류가 발생했습니다.", e);
        }
    }
}
