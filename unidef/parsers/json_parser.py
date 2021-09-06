import re
import unicodedata

import pyhocon

from unidef.models.definitions import Definition, ModelExample
from unidef.models.type_model import *
from unidef.parsers import Parser
from unidef.utils.typing_compat import *


class JsonParser(Parser):
    def accept(self, fmt: Definition) -> bool:
        return isinstance(fmt, ModelExample) and fmt.format.lower() == "json"

    def parse_comment(self, content: str) -> Dict[(int, str), str]:
        occurrences = {}
        result = {}
        key_re = re.compile(r'"([\w\-]+)"\s*:')
        comment = []
        for line in content.splitlines():
            pos = line.find("//")
            if pos >= 0:
                comment.append(line[pos + 2:])

            try:
                key = next(key_re.finditer(line))[1]
            except StopIteration:
                continue
            if key not in occurrences:
                occurrences[key] = 0
            occurrences[key] += 1
            if comment:
                result[(occurrences[key], key)] = "\n".join(comment)
                comment.clear()
        return result

    def parse(self, name: str, fmt: ModelExample) -> Type:
        content = fmt.text
        content = unicodedata.normalize("NFKC", content)
        comments = self.parse_comment(content)

        parsed = parse_data_example(dict(pyhocon.ConfigParser.parse(content)), name)
        if parsed.get_field(Traits.Struct) and name:
            parsed.replace_field(Traits.TypeName(name))

            def process(depth: int, i: int, key: str, ty: Type):
                if (i, key) in comments:
                    ty.append_field(
                        Traits.BeforeLineComment(comments[(i, key)].splitlines())
                    )

            walk_type_with_count(parsed, process)

        return parsed
