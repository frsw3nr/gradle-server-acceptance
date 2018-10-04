import re
import sys
import os
import logging
import argparse
from getconfig.config import Config
from getconfig.ticket.redmine_repository import RedmineRepository

class UtilGetRedmineProjects():

    def main(self):
        logging.basicConfig(
            level=getattr(logging, 'INFO'),
            format='%(asctime)s [%(levelname)s] %(module)s %(message)s',
            datefmt='%Y/%m/%d %H:%M:%S',
        )
        logger = logging.getLogger(__name__)

        db = RedmineRepository()
        for project in db.project_ids.keys():
            print("project : {}".format(project))

if __name__ == '__main__':
    UtilGetRedmineProjects().main()
