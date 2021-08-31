import stringcase


def to_lower(s: str) -> str:
    return s.lower()


def to_snake_case(s: str) -> str:
    s = stringcase.snakecase(s)
    s = s.replace('i_d', 'id')
    return s


def to_pascal_case(s: str) -> str:
    return stringcase.pascalcase(to_snake_case(s))
