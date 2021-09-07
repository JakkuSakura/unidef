from unidef.utils.typing import *
from pydantic import BaseModel

Input = TypeVar("Input")
Output = TypeVar("Output")


class NodeTransformer(BaseModel, Generic[Input, Output]):
    @beartype
    def accept(self, node: Input) -> bool:
        return NotImplemented

    @beartype
    def transform_node(self, node: Input) -> Output:
        return NotImplemented


class NodeTransformable(BaseModel, Generic[Output]):
    def transform(self) -> Output:
        return NotImplemented
