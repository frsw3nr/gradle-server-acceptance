import re
import sys
import os
import pytest

from getconfig_cleansing import cli
from getconfig_cleansing.evidence.collector import EvidenceCollector

# py.test tests/test_import.py -v --capture=no

# class EvidenceLoader(object):
#     def list_commands(self):
#         rv = []
#         for root, dirs, files in os.walk('tests/resources/import'):
#             for excel_file in files:
#                 if excel_file.endswith('.xlsx'):
#                     rv.append(excel_file[0:-5])
#         rv.sort()
#         return rv

#     def get_module(self, name):
#         try:
#             mod = __import__('getconfig_cleansing.evidence.parser_' + name,
#                              None, None, ['EvidenceParser'])
#         except ImportError:
#             return
#         return mod

@pytest.fixture
def collector():
    return EvidenceCollector('tests/resources/import')

def test_import_multiple_evidence(collector):
    collector.parse()
    rv = collector.list_evidences()
    assert len(rv) == 4

    # evidences = collector.list_commands()
    # print(evidences)
    # for evidence in evidences:
    #     print("Evidence:%s" % (evidence))
    #     match = re.match(r'^(.+)_(\d+_\d+)$', evidence)
    #     timestamp = ''
    #     if match:
    #         evidence = match.group(1)
    #         timestamp = match.group(2)
    #     print("Check: %s, Date: %s" % (evidence, timestamp))
    #     mod = collector.get_module(evidence)
    #     if mod:
    #         mod.EvidenceParser().say('Hello')

    # mod1 = loader.get_module('check_sheet')
    # mod1.EvidenceParser().say('Hello')

    # mod2 = loader.get_module('サーバチェックシート')
    # mod2.EvidenceParser().say('Hello')
    assert 1 == 1


