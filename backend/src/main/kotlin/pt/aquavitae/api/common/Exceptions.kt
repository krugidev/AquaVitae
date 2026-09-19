package pt.aquavitae.api.common

class ResourceNotFoundException(message: String) : RuntimeException(message)

class ConflictException(message: String) : RuntimeException(message)

class UnauthorizedActionException(message: String) : RuntimeException(message)

class InvalidCredentialsException(message: String) : RuntimeException(message)

// Pedido bem formado mas com valores que não fazem sentido (ex.: janela de consumo a acabar antes de
// começar). Dá 400; as validações campo a campo continuam a ser feitas por Bean Validation.
class PedidoInvalidoException(message: String) : RuntimeException(message)
