from typing import *
import sys
# handle type annotation changes in PEP 3107, 484, 526, 544, 560, 563, 585
if sys.version_info >= (3, 8, 8):
    Dict = dict
    List = list
    Set = set
    Str = str
    Tuple = tuple
