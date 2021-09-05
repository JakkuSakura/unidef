import case_conversion


def to_lower(s: str) -> str:
    return s.lower()


def to_snake_case(s: str) -> str:
    return case_conversion.snakecase(s)


def to_pascal_case(s: str) -> str:
    return case_conversion.pascalcase(s)
