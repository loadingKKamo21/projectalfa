package com.project.alfa.domain;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;

import static lombok.AccessLevel.PROTECTED;

@Entity
@Table(name = "tb_profile_image")
@Getter
@NoArgsConstructor(access = PROTECTED)
@EqualsAndHashCode(callSuper = false)
public class ProfileImage extends UploadFile {
    
    @Id
    @GeneratedValue
    @Column(name = "profile_image_id")
    private Long id;           //PK
    
    @Lob
    private String base64Data; //Base64 인코딩 값
    
    protected ProfileImage(String originalFileName, String storeFileName, String storeFilePath, Long fileSize) {
        super(originalFileName, storeFileName, storeFilePath, fileSize);
    }
    
    @Builder
    public ProfileImage(String originalFileName,
                        String storeFileName,
                        String storeFilePath,
                        Long fileSize,
                        String base64Data) {
        super(originalFileName, storeFileName, storeFilePath, fileSize);
        this.base64Data = base64Data;
    }
    
    //==================== 프로필 사진 수정 메서드 ====================//
    
    /**
     * 프로필 사진 변경
     *
     * @param newOriginalFileName - 변경할 파일 원본 파일명
     * @param newStoreFileName    - 변경할 파일 저장 파일명
     * @param newStoreFilePath    - 변경할 파일 저장 경로
     * @param newFileSize         - 변경할 파일 크기
     * @param newBase64Data       - 변경할 Bas64 인코딩 값
     */
    public void updateImage(String newOriginalFileName,
                            String newStoreFileName,
                            String newStoreFilePath,
                            Long newFileSize,
                            String newBase64Data) {
        originalFileName = newOriginalFileName;
        storeFileName = newStoreFileName;
        storeFilePath = newStoreFilePath;
        fileSize = newFileSize;
        base64Data = newBase64Data;
    }
    
    /**
     * 프로필 사진 삭제
     */
    public void delete() {
        originalFileName = null;
        storeFileName = null;
        storeFilePath = null;
        fileSize = null;
        base64Data = null;
    }
    
}
