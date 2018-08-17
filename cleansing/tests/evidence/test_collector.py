import re
import sys
import os
import pytest

from getconfig_cleansing import cli
from getconfig_cleansing.evidence.collector import EvidenceCollector

# py.test tests/evidence/test_collector.py -v --capture=no

# @pytest.fixture
# def collector():
#     return EvidenceCollector('tests/resources/import')

def test_import_single_evidence():
    collector = EvidenceCollector('tests/resources/import/project1/build/サーバチェックシート_20180817_142016.xlsx')
    print ("Single test")
    collector.parse()
    assert 1 == 1

def test_import_multiple_evidence():
    collector = EvidenceCollector('tests/resources/import')
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


