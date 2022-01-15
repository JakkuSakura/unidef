import case_conversion
from beartype import beartype


@beartype
def to_lower(s: str) -> str:
    return s.lower()


@beartype
def to_snake_case(s: str) -> str:
    if "_" in s:
        s = s.lower()
    converted = case_conversion.snakecase(s)
    return converted


@beartype
def to_pascal_case(s: str) -> str:
    return case_conversion.pascalcase(s)

@beartype
def to_camel_case(s: str) -> str:
    return case_conversion.camelcase(s)
