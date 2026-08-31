package com.hmg.role.rbac.resourcetype.exceptions;

import com.hmg.role.util.exceptions.GlobalException;
import org.springframework.http.HttpStatus;

public class ResourceTypeMaxException extends GlobalException {
    public ResourceTypeMaxException() {
        super(HttpStatus.BAD_REQUEST, "The maximum resource dataType data 500 length exceeded");
    }
}
