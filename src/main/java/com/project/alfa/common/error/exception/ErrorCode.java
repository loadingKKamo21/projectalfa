package com.project.alfa.common.error.exception;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;

@Getter
@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public enum ErrorCode {
    
    //Common
    INVALID_INPUT_VALUE(400, "C001", "Invalid Input Value"),
    METHOD_NOT_ALLOWED(405, "C002", "Invalid Input Value"),
    ENTITY_NOT_FOUND(400, "C003", "Entity Not Found"),
    INTERNAL_SERVER_ERROR(500, "C004", "Server Error"),
    INVALID_TYPE_VALUE(400, "C005", "Invalid Type Value"),
    HANDLE_ACCESS_DENIED(403, "C006", "Access is Denied"),
    
    //Member
    USERNAME_DUPLICATION(400, "M001", "Username is Duplication"),
    NICKNAME_DUPLICATION(400, "M002", "Nickname is Duplication"),
    EMAIL_AUTH_NOT_COMPLETED(400, "M003", "Email Auth Not Completed"),
    LOGIN_INPUT_INVALID(400, "M004", "Login input is invalid"),
    PASSWORD_DO_NOT_MATCH(400, "M005", "Passoword don't match"),
    
    //Post
    NOT_WRITER_OF_POST(400, "P001", "Not Writer of Post"),
    
    //Comment
    NOT_WRITER_OF_COMMENT(400, "R001", "Not Writer of Comment");
    
    //Category
    
    private final int    status;
    private final String code;
    private final String message;
    
    ErrorCode(final int status, final String code, final String message) {
        this.status = status;
        this.code = code;
        this.message = message;
    }
    
}
