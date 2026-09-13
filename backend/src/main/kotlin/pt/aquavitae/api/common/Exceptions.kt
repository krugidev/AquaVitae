package pt.aquavitae.api.common

class ResourceNotFoundException(message: String) : RuntimeException(message)

class ConflictException(message: String) : RuntimeException(message)

class UnauthorizedActionException(message: String) : RuntimeException(message)

class InvalidCredentialsException(message: String) : RuntimeException(message)
