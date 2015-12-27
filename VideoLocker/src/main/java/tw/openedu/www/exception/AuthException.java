package tw.openedu.www.exception;

import tw.openedu.www.model.api.AuthErrorResponse;

public class AuthException extends Exception {

    private AuthErrorResponse authResponseObject;

    public AuthException(AuthErrorResponse res) {
        this.authResponseObject = res;
    }

    @Override
    public String getMessage() {
        return authResponseObject.detail;
    }
}
