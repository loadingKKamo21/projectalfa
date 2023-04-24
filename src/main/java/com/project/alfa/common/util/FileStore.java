package com.project.alfa.common.util;

import com.project.alfa.domain.UploadFile;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Component
public class FileStore {
    
    @Value("${file.upload.location}")
    private String fileDir;
    
    /**
     * 파일 전체 경로
     *
     * @param filename - 파일명
     * @return - 파일 전체 경로
     */
    public String getFullPath(String filename) {
        return fileDir + filename;
    }
    
    /**
     * 다중 파일 저장
     *
     * @param multipartFiles - 첨부 파일 리스트
     * @return
     * @throws IllegalStateException
     * @throws IOException
     */
    public List<UploadFile> storeFiles(List<MultipartFile> multipartFiles) throws IllegalStateException, IOException {
        List<UploadFile> storeFileResult = new ArrayList<>();
        for (MultipartFile multipartFile : multipartFiles)
            if (!multipartFile.isEmpty()) storeFileResult.add(storeFile(multipartFile));
        return storeFileResult;
    }
    
    /**
     * 단일 파일 저장
     *
     * @param multipartFile - 첨부 파일
     * @return
     * @throws IllegalStateException
     * @throws IOException
     */
    public UploadFile storeFile(MultipartFile multipartFile) throws IllegalStateException, IOException {
        if (multipartFile.isEmpty()) return null;
        
        String originalFilename = multipartFile.getOriginalFilename();
        String storeFilename    = createStoreFilename(originalFilename);
        
        File file = new File(getFullPath(storeFilename));
        file.mkdirs();
        multipartFile.transferTo(file);
        
        return new UploadFile(originalFilename, storeFilename, getFullPath(storeFilename), multipartFile.getSize()) {};
    }
    
    /**
     * 저장된 파일 삭제
     *
     * @param fullPath - 파일 경로
     */
    public void deleteUploadedFile(String fullPath) {
        File file = new File(fullPath);
        if (file.exists()) file.delete();
    }
    
    /**
     * 파일 저장용 이름 생성
     *
     * @param originalFilename - 원래 파일명
     * @return - 저장된 파일명
     */
    private String createStoreFilename(String originalFilename) {
        return UUID.randomUUID().toString() + "." + extractExt(originalFilename);
    }
    
    /**
     * 파일 확장자 추출
     *
     * @param originalFilename - 원래 파일명
     * @return - 확장자
     */
    private String extractExt(String originalFilename) {
        return originalFilename.substring(originalFilename.lastIndexOf(".") + 1);
    }
    
}
