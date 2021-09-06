import sys
from typing import (Any, Callable, Dict, Iterable, Iterator, List, Optional,
                    Set, Tuple, Union)

# handle type annotation changes in PEP 3107, 484, 526, 544, 560, 563, 585
if sys.version_info >= (3, 8, 8):
    Dict = dict
    List = list
    Set = set
    Tuple = tuple
    import collections.abc

    Iterable = collections.abc.Iterable
    Iterator = collections.abc.Iterator
    Callable = collections.abc.Callable
