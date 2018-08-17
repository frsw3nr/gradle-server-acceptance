import re
import sys
import os
import logging
from enum import Enum
from datetime import datetime
import logging
from abc import ABCMeta, abstractmethod

class EvidenceCollector(object):
    EVIDENCE_DIR = 'build'

    def __init__(self, evidence_source = EVIDENCE_DIR, **kwargs):
        self.evidence_source = evidence_source

    def analyze_evidence_path(self, evidence_dir, excel_file):
        # Extract project from '.../{project}/build'
        match_dir = re.search(r'([^/]+?)/build$', evidence_dir)
        project = match_dir.group(1) if match_dir else "unkown"

        # Extract evidence,timestamp from '{evidence}_{YYYYMMDD_HHMI}.xlsx'
        evidence = excel_file
        match_excel = re.match(r'^(.+)_(\d+_\d+).xlsx$', excel_file)
        timestamp = ''
        if match_excel:
            evidence  = match_excel.group(1)
            timestamp = match_excel.group(2)

        return {'evidence': evidence, 'project': project, 'timestamp': timestamp}

    def list_evidences(self):
        _logger = logging.getLogger(__name__)
        rv = []
        if os.path.isfile(self.evidence_source):
            evidence_dir, excel_file = os.path.split(self.evidence_source)
            if excel_file.endswith('.xlsx'):
                info = self.analyze_evidence_path(evidence_dir, excel_file)
                info['path'] = self.evidence_source
                print("INFO : %s" % (info))
                rv.append(excel_file[0:-5])
        elif os.path.isdir(self.evidence_source):
            for evidence_dir, dirs, files in os.walk(self.evidence_source):
                for excel_file in files:
                    if excel_file.endswith('.xlsx'):
                        info = self.analyze_evidence_path(evidence_dir, excel_file)
                        rv.append(excel_file[0:-5])
            rv.sort()
        return rv

    def get_module(self, name):
        _logger = logging.getLogger(__name__)
        try:
            mod = __import__('getconfig_cleansing.master.parser_' + name,
                             None, None, ['EvidenceParser'])
        except ImportError:
            return
        return mod

    def parse(self):
        _logger = logging.getLogger(__name__)
        evidences = self.list_evidences()
        for evidence in evidences:
            match = re.match(r'^(.+)_(\d+_\d+)$', evidence)
            timestamp = ''
            if match:
                evidence = match.group(1)
                timestamp = match.group(2)
            _logger.warn("Check: %s, Date: %s" % (evidence, timestamp))
            mod = self.get_module(evidence)
            if mod:
                mod.EvidenceParser().say('Hello')

