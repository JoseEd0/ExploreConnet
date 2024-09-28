package dbp.exploreconnet;

import dbp.exploreconnet.exceptions.ResourceNotFoundException;
import dbp.exploreconnet.exceptions.UnauthorizedOperationException;
import dbp.exploreconnet.exceptions.UniqueResourceAlreadyExist;
import dbp.exploreconnet.exceptions.UserAlreadyExistException;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(UsernameNotFoundException.class)
    public String handleUsernameNotFoundException(UsernameNotFoundException ex) {
        return ex.getMessage();
    }


    @ResponseStatus(HttpStatus.CONFLICT)
    @ExceptionHandler(UniqueResourceAlreadyExist.class)
    public String handleUniqueResourceAlreadyExist(UniqueResourceAlreadyExist ex) {
        return ex.getMessage();
    }


    @ResponseStatus(HttpStatus.CONFLICT)
    @ExceptionHandler(UserAlreadyExistException.class)
    public String handleUserAlreadyExistException(UserAlreadyExistException ex) {
        return ex.getMessage();
    }


    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    @ExceptionHandler(AuthenticationException.class)
    public String handleAuthenticationException(AuthenticationException ex) {
        return ex.getMessage();
    }


    @ResponseStatus(HttpStatus.FORBIDDEN)
    @ExceptionHandler(UnauthorizedOperationException.class)
    public String handleUnauthorizeOperationException(UnauthorizedOperationException ex) {
        return ex.getMessage();
    }


}

