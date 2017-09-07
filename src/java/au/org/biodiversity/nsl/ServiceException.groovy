package au.org.biodiversity.nsl

/**
 * User: pmcneil
 * Date: 11/08/17
 *
 */
class ServiceException extends RuntimeException {

    ServiceException(String reason) {
        super(reason)
    }
}

class ObjectExistsException extends ServiceException {

    ObjectExistsException(String reason) {
        super(reason)
    }
}

class ObjectNotFoundException extends ServiceException {

    ObjectNotFoundException(String reason) {
        super(reason)
    }
}

class BadArgumentsException extends ServiceException {

    BadArgumentsException(String reason) {
        super(reason)
    }
}
