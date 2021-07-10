from typing import Optional, Any, Dict

from beartype import beartype
from pydantic import BaseModel


class TypeMeta(BaseModel):
    type_name: str
    generics: list[Optional['TypeMeta']] = []

    @staticmethod
    @beartype
    def from_str(name: str) -> 'TypeMeta':
        return TypeMeta(type_name=name)

    @staticmethod
    @beartype
    def from_generics(name: str, generics: list[Optional['TypeMeta']]) -> 'TypeMeta':
        meta = TypeMeta(type_name=name, generics=generics)
        return meta

    def __hash__(self):
        return hash(self.json())


TypeMeta.update_forward_refs()


class Instance(BaseModel):
    type_meta: TypeMeta
    value: Any


def is_parent_instance(instance: Instance):
    return instance.type_meta.type_name == 'parent'


class Type(BaseModel):
    """
    Type is the type model used in this program.
    It allows single inheritance and multiple traits, similar to those in Rust and Java, as used in many other languages.
    """
    type_meta: TypeMeta
    traits: list[Instance] = []

    def with_trait(self, trait: Instance) -> 'Type':
        assert isinstance(trait, Instance)
        self.traits.append(trait)
        return self

    @beartype
    def with_parent(self, parent: 'Type') -> 'Type':
        return self.with_trait(parent.as_parent())

    def as_parent(self) -> Instance:
        return Instance(type_meta=TypeMeta.from_str('parent'), value=self.type_meta)

    def as_trait(self) -> Instance:
        return Instance(type_meta=TypeMeta.from_str('trait'), value=self.type_meta)

    def as_instance(self, value: Any) -> Instance:
        return Instance(type_meta=self.type_meta, value=value)

    @staticmethod
    @beartype
    def from_str(name: str) -> 'Type':
        return Type(type_meta=TypeMeta(type_name=name))

    @staticmethod
    @beartype
    def from_meta(meta: TypeMeta) -> 'Type':
        return Type(type_meta=meta)


class TypeAlreadyExistsAndConflict(Exception):
    pass


class TypeRegistry(BaseModel):
    types: dict[TypeMeta, Type] = {}

    def insert(self, model: Type):
        if model.type_meta not in self.types:
            self.types[model.type_meta] = model
        elif self.types[model.type_meta] != model:
            raise TypeAlreadyExistsAndConflict(model.type_meta)

    def get(self, meta: TypeMeta) -> Type:
        return self.types.get(meta)

    def is_subclass(self, child: Type, parent: Type) -> bool:
        assert isinstance(child, Type)
        assert isinstance(parent, Type)
        if parent.as_parent() in child.traits:
            return True
        for i in child.traits:
            if is_parent_instance(i):
                p = self.get(i.value)
                if self.is_subclass(p, parent):
                    return True
        return False

    def list_types(self):
        for ty in self.types.values():
            print('Type', ty.json())


class Types:
    Primitive = Type.from_str('primitive')
    Bool = Type.from_str('bool').with_parent(Primitive)
    Numeric = Type.from_str('numeric').with_parent(Primitive)
    Integer = Type.from_str('integer').with_parent(Numeric)
    IntoIterator = Type.from_str('into_iterator')
    Vector = Type.from_meta(TypeMeta.from_generics('vector', [None])).with_trait(IntoIterator.as_trait())


GLOBAL_REGISTRY = TypeRegistry()
GLOBAL_REGISTRY.insert(Types.Primitive)
GLOBAL_REGISTRY.insert(Types.Bool)
GLOBAL_REGISTRY.insert(Types.Numeric)
GLOBAL_REGISTRY.insert(Types.Integer)
GLOBAL_REGISTRY.insert(Types.IntoIterator)
GLOBAL_REGISTRY.insert(Types.Vector)

if __name__ == '__main__':
    GLOBAL_REGISTRY.list_types()
