import re

class InventoryInfo(object):

    def __init__(self, path, name, project, timestamp, **kwargs):
        project_dir = None
        match_dir = re.search(r'^(.+?)[/|\\]build[/|\\]', path)
        if match_dir:
            project_dir = match_dir.group(1)

        self.source      = path
        self.project_dir = project_dir
        self.name        = name
        self.project     = project
        self.timestamp   = timestamp
