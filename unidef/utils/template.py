import copy

from typedmodel import *
from typing import Dict, Any
from jinja2 import Template
from jinja2.filters import do_indent
import re


class Code(BaseModel):
    code: str
    values: Dict[str, Any]

    def __init__(self, code: str, **kwargs):
        super().__init__(code=code, values=kwargs)

    def render(self) -> str:
        value_strings = copy.copy(self.values)
        for key, val in self.values.items():
            if isinstance(val, Code):
                replaced = val.render()
            else:
                replaced = str(val)
            value_strings[key] = replaced

        lines = self.code.splitlines()
        count = 0
        new_values = {}

        def sub_func(match):
            nonlocal count
            count += 1
            ret = 'val_' + str(count)
            indent = match.start()
            new_values[ret] = do_indent(value_strings[match.group(1)], indent)
            return '{{' + ret

        for i in range(len(lines)):
            lines[i] = re.sub(r'{{\s+([a-zA-Z_0-9]+)', sub_func, lines[i])

        rendered = Template('\n'.join(lines)).render(**new_values)
        return rendered


def test_basic_indentation():
    code1 = Code("""\
println!("{:?}", {{ val }});
""", val="vec![\n1,\n2\n]")
    print()

    code2 = Code("""\
for i in 0..{{ val }} {
    {{ code1 }}
}
""", val=2, code1=code1)
    print(code2.render())

    code3 = Code("""\
for i in 0..{{ val }} {
    {{ code1 }}
}
""", val=3, code1=code2)
    print(code3.render())
