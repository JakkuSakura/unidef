import copy

from typedmodel import *
from typing import Dict, Any
from jinja2 import Template, StrictUndefined
from jinja2.filters import do_indent
import re


class Code(BaseModel):
    code: str
    values: Dict[str, Any]
    cache: str

    def __init__(self, code: str, **kwargs):
        super().__init__(code=code, cache='', values=kwargs)
        self.cache = self.render()

    def render(self) -> str:
        string_cache = {}
        lines = self.code.splitlines()
        count = 0
        new_values = copy.copy(self.values)

        def sub_func(match):
            nonlocal count

            key = match.group(1)
            if isinstance(self.values[key], Code):
                count += 1
                ret = key + '_' + str(count)
                indent = match.start()
                if key not in string_cache:
                    string_cache[key] = str(self.values[key])
                new_values[ret] = do_indent(string_cache[key], indent)
                return '{{ ' + ret + ' }}'
            else:
                return match.group(0)

        for i in range(len(lines)):
            lines[i] = re.sub(r'{{\s*([a-zA-Z_0-9]+)\s*}}', sub_func, lines[i])

        rendered = Template('\n'.join(lines), undefined=StrictUndefined).render(**new_values)
        return rendered

    def __str__(self):
        return self.cache


def test_basic_indentation():
    code1 = Code("""\
println!("{:?}", {{ val }});
""", val=Code("vec![\n  1,\n  2\n]"))
    print()

    code2 = Code("""\
for i in 0..{{ val }} {
    {{ code1 }}
}
""", val=2, code1=code1)
    print(code2)

    code3 = Code("""\
for i in 0..{{ val }} {
    {{ code1 }}
}
""", val=3, code1=code2)
    print(code3)

    print(Code("""\
{{ "\n".join(values) }}
""", values=["hello", "world"]))
