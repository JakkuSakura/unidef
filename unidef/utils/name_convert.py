import case_conversion
from beartype import beartype


@beartype
def to_lower(s: str) -> str:
    return s.lower()


@beartype
def to_snake_case(s: str) -> str:
    return case_conversion.snakecase(s)


@beartype
def to_pascal_case(s: str) -> str:
    return case_conversion.pascalcase(s)
