import unicodedata
import pyhocon
from unidef.models.type_model import *
from unidef.parsers import ApiParser
from unidef.utils.typing_compat import *
import re


class JsonParser(ApiParser):

    def accept(self, fmt: str) -> bool:
        return fmt.lower() == 'json'

    def parse_comment(self, content: str) -> Dict[(int, str), str]:
        occurrences = {}
        result = {}
        key_re = re.compile(r'"([\w\-]+)"\s*:')
        comment = []
        for line in content.splitlines():
            pos = line.find('//')
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
                result[(occurrences[key], key)] = '\n'.join(comment)
                comment.clear()
        return result

    def parse(self, fmt: str, name: str, content: str) -> Type:
        content = unicodedata.normalize('NFKC', content)
        comments = self.parse_comment(content)

        parsed = parse_data_example(dict(pyhocon.ConfigParser.parse(content)), name)
        if parsed.get_trait(Traits.Struct) and name:
            parsed.replace_trait(Traits.TypeName.init_with(name))

            def process(depth: int, i: int, key: str, ty: Type):
                if (i, key) in comments:
                    for line in comments[(i, key)].splitlines():
                        ty.append_trait(Traits.LineComment.init_with(line))

            walk_type_with_count(parsed, process)

        return parsed
