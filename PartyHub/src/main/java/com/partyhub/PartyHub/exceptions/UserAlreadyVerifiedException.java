package com.partyhub.PartyHub.exceptions;

public class UserAlreadyVerifiedException extends RuntimeException{
    public UserAlreadyVerifiedException(String message){
        super(message);
    }
}
