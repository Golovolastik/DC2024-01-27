package by.bsuir.discussion.service.exceptions;

import by.bsuir.discussion.model.response.ErrorResponseTo;
import lombok.Getter;

@Getter
public class ResourceException extends RuntimeException {
    private final ErrorResponseTo errorResponseTo;

    public ResourceException(int code, String message)
    {
        super(message);
        errorResponseTo = new ErrorResponseTo(code, message, null);
    }
}
