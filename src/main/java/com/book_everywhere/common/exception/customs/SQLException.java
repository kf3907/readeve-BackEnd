package com.book_everywhere.common.exception.customs;

public class SQLException extends CustomException{
    public SQLException(CustomErrorCode errorCode) {
        super(errorCode);
    }
}
