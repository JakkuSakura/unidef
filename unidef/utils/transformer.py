from unidef.utils.typing import *
from pydantic import BaseModel

Input = TypeVar("Input")
Output = TypeVar("Output")


@abstract
class NodeTransformer(BaseModel, Generic[Input, Output]):
    @beartype
    def accept(self, node: Input) -> bool:
        return NotImplemented

    @beartype
    def transform_node(self, node: Input) -> Output:
        return NotImplemented


@abstract
class NodeTransformable(BaseModel, Generic[Output]):
    def transform(self) -> Output:
        return NotImplemented
