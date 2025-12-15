package com.signly.common.storage;

public interface FileStorageStrategy {

    StoredFile storeFile(
            byte[] data,
            String originalFilename,
            String contentType,
            String category
    );

    /**
     * 파일을 저장소에서 읽어옵니다.
     *
     * @param filePath 파일 경로
     * @return 파일 데이터
     */
    byte[] loadFile(String filePath);

    /**
     * 파일을 저장소에서 삭제합니다.
     *
     * @param filePath 파일 경로
     */
    void deleteFile(String filePath);

    /**
     * 파일이 존재하는지 확인합니다.
     *
     * @param filePath 파일 경로
     * @return 파일 존재 여부
     */
    boolean fileExists(String filePath);
}