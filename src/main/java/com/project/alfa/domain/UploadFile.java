package com.project.alfa.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.MappedSuperclass;

import static lombok.AccessLevel.PROTECTED;

@MappedSuperclass
@Getter
@NoArgsConstructor(access = PROTECTED)
@AllArgsConstructor
public abstract class UploadFile extends BaseTimeEntity {
    
    String originalFileName; //원본 파일명
    String storeFileName;    //저장 파일명
    String storeFilePath;    //저장 경로
    Long   fileSize;         //파일 크기
    
}
