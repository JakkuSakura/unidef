import copy

from typedmodel import *
from typing import Dict, Any
from jinja2 import Template, StrictUndefined
from jinja2.filters import do_indent
import re


class Code(BaseModel):
    code: str
    values: Dict[str, Any]

    def __init__(self, code: str, **kwargs):
        super().__init__(code=code, values=kwargs)
        # check
        self.__str__()

    def __str__(self) -> str:
        value_strings = copy.copy(self.values)
        for key, val in self.values.items():
            string_list = [int, str, float, Code]
            if type(val) in string_list:
                value_strings[key] = str(val)

        lines = self.code.splitlines()
        count = 0
        new_values = copy.copy(value_strings)

        def sub_func(match):
            nonlocal count
            count += 1
            ret = match.group(1) + '_' + str(count)
            indent = match.start()
            new_values[ret] = do_indent(value_strings[match.group(1)], indent)
            return '{{ ' + ret + ' }}'

        for i in range(len(lines)):
            lines[i] = re.sub(r'{{\s*([a-zA-Z_0-9]+)\s*}}', sub_func, lines[i])

        rendered = Template('\n'.join(lines), undefined=StrictUndefined).render(**new_values)
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
