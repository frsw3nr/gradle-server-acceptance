import re
import sys
import os
import argparse

def scan_inventory_projects(inventory_source, n_import_path = 5):
    project_dirs = dict()
    for project_file in os.listdir(inventory_source):
        path = os.path.join(inventory_source, project_file)
        if os.path.isdir(path):
            timestamp = os.path.getmtime(path)
            project_dirs[project_file] = timestamp
    n = 0
    projects = []
    for k, v in sorted(project_dirs.items(), key=lambda x:x[1], reverse=True):
        n += 1
        if n > n_import_path:
            break
        projects.append(k)
    return projects

parser = argparse.ArgumentParser()
parser.add_argument("-s", "--source", default = 'cleansing/data/import', help = "import dir")
parser.add_argument("-n", "--number", type = int, default = 5, help = "limit")
args = parser.parse_args()
projects = scan_inventory_projects(args.source, args.number)
print(','.join(projects))
