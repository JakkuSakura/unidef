from pydantic import BaseModel


class AstBase(BaseModel):
    def write(self, s: str):
        raise NotImplementedError()
